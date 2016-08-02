package com.kanawish.raja.controlroom;

import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.jakewharton.rxrelay.PublishRelay;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.projecttango.tangosupport.TangoSupport;

import org.rajawali3d.scene.ASceneFrameCallback;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static com.google.atap.tangoservice.Tango.*;
import static com.projecttango.tangosupport.TangoSupport.*;

public class ControlRoomActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = ControlRoomActivity.class.getSimpleName();

    private static final int INVALID_TEXTURE_ID = 0;

    private TangoCameraIntrinsics tangoCameraIntrinsics;
    private TangoPointCloudManager tangoPointCloudManager;

    private Tango tango;
    private boolean isConnected = false;
    private double cameraPoseTimestamp = 0;

    // Texture rendering related fields
    // NOTE: Naming indicates which thread is in charge of updating this variable
    private int connectedTextureIdGlThread = INVALID_TEXTURE_ID;
    private AtomicBoolean isFrameAvailableTangoThread = new AtomicBoolean(false);
    private double rgbTimestampGlThread;

    PublishRelay<String> log = PublishRelay.create();

    private TextView logTextView;

    private RajawaliSurfaceView surfaceView;
    private ControlRoomRenderer renderer;

    private Subscription logSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        logTextView = (TextView) findViewById(R.id.log_text);
        surfaceView = new RajawaliSurfaceView(this);
//        surfaceView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        renderer = new ControlRoomRenderer(this);
        surfaceView.setSurfaceRenderer(renderer);
        surfaceView.setOnTouchListener(this);
        ((LinearLayout)findViewById(R.id.parent)).addView(surfaceView);

        tangoPointCloudManager = new TangoPointCloudManager();
//        setContentView(surfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        logSubscription = log
            .throttleFirst(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                log -> logTextView.setText(log),
                throwable -> Log.e(TAG, "log error", throwable));

        if (!isConnected) {
            tango = new Tango(ControlRoomActivity.this, () -> {
                try {
                    TangoSupport.initialize();
                    connectTango();
                    connectRenderer();
                    isConnected = true;
                } catch (TangoOutOfDateException e) {
                    Log.e(TAG, getString(R.string.exception_out_of_date), e);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        logSubscription.unsubscribe();

        synchronized (this) {
            if (isConnected) {
                renderer.getCurrentScene().clearFrameCallbacks();
                tango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
                // We need to invalidate the connected texture ID so that we cause a re-connection
                // in the OpenGL thread after resume
                connectedTextureIdGlThread = INVALID_TEXTURE_ID;
                tango.disconnect();
                isConnected = false;
            }
        }
    }

    /**
     * Configures the Tango service and connect it to callbacks.
     */
    private void connectTango() {

        // Use default configuration for Tango Service, plus low latency IMU integration.
        TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);

        // NOTE: Low latency integration is necessary to achieve a precise alignment of
        // NOTE: virtual objects with the RBG image and produce a good AR effect.
        config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);

        // NOTE: These are extra motion tracking flags.
        config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORECOVERY, true);

        tango.connect(config);

        // Defining the coordinate frame pairs we are interested in.
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
            TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
            TangoPoseData.COORDINATE_FRAME_DEVICE));

        tango.connectListener(framePairs, new OnTangoUpdateListener() {
            @Override
            public void onPoseAvailable(TangoPoseData pose) {
                // We could process pose data here, but we are not
                // directly using onPoseAvailable() for this app.
                logPose(pose);
            }

            @Override
            public void onFrameAvailable(int cameraId) {
                // Check if the frame available is for the camera we want and update its frame on the view.
                if (cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
                    // Mark a camera frame is available for rendering in the OpenGL thread
                    isFrameAvailableTangoThread.set(true);
                    surfaceView.requestRender();
                }
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                // Save the cloud and point data for later use.
                tangoPointCloudManager.updateXyzIj(xyzIj);
            }

            @Override
            public void onTangoEvent(TangoEvent event) {
                // Information about events that occur in the Tango system.
                // Allows you to monitor the health of services at runtime.
            }
        });

        tangoCameraIntrinsics = tango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
    }

    /**
     * Log the Position and Orientation of the given pose in the Logcat as information.
     *
     * @param pose the pose to log.
     */
    private void logPose(TangoPoseData pose) {
        StringBuilder stringBuilder = new StringBuilder();
        float translation[] = pose.getTranslationAsFloats();
        float orientation[] = pose.getRotationAsFloats();

        stringBuilder.append(String.format("[%+3.3f,%+3.3f,%+3.3f]\n",translation[0],translation[1],translation[2]));
        stringBuilder.append(String.format("(%+3.3f,%+3.3f,%+3.3f,%+3.3f)",orientation[0],orientation[1],orientation[2],orientation[3]));

        log.call(stringBuilder.toString());
    }

    /**
     * Connects the view and renderer to the color camara and callbacks.
     */
    private void connectRenderer() {
        // Register a Rajawali Scene Frame Callback to update the scene camera pose whenever a new RGB frame is rendered.
        // (@see https://github.com/Rajawali/Rajawali/wiki/Scene-Frame-Callbacks)
        renderer.getCurrentScene().registerFrameCallback(new ASceneFrameCallback() {
            @Override
            public void onPreFrame(long sceneTime, double deltaTime) {
                // NOTE: This is called from the OpenGL render thread, after all the renderer
                // onRender callbacks had a chance to run and before scene objects are rendered
                // into the scene.

                synchronized (ControlRoomActivity.this) {
                    // Don't execute any tango API actions if we're not connected to the service
                    if (!isConnected) {
                        return;
                    }

                    // Set-up scene camera projection to match RGB camera intrinsics
                    if (!renderer.isSceneCameraConfigured()) {
                        renderer.setProjectionMatrix(tangoCameraIntrinsics);
                    }

                    // Connect the camera texture to the OpenGL Texture if necessary
                    // NOTE: When the OpenGL context is recycled, Rajawali may re-generate the
                    // texture with a different ID.
                    if (connectedTextureIdGlThread != renderer.getTextureId()) {
                        tango.connectTextureId(TangoCameraIntrinsics.TANGO_CAMERA_COLOR,
                            renderer.getTextureId());
                        connectedTextureIdGlThread = renderer.getTextureId();
                        Log.d(TAG, "connected to texture id: " + renderer.getTextureId());
                    }

                    // If there is a new RGB camera frame available, update the texture with it
                    if (isFrameAvailableTangoThread.compareAndSet(true, false)) {
                        rgbTimestampGlThread =
                            tango.updateTexture(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
                    }

                    if (rgbTimestampGlThread > cameraPoseTimestamp) {
                        // Calculate the camera color pose at the camera frame update time in
                        // OpenGL engine.
                        TangoPoseData lastFramePose = getPoseAtTime(
                            rgbTimestampGlThread,
                            TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                            TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR,
                            TANGO_SUPPORT_ENGINE_OPENGL, 0);
                        if (lastFramePose.statusCode == TangoPoseData.POSE_VALID) {
                            // Update the camera pose from the renderer
                            renderer.updateRenderCameraPose(lastFramePose);
                            cameraPoseTimestamp = lastFramePose.timestamp;
                        } else {
                            Log.w(TAG, "Can't get device pose at time: " +
                                rgbTimestampGlThread);
                        }
                    }
                }
            }

            @Override
            public void onPreDraw(long sceneTime, double deltaTime) {

            }

            @Override
            public void onPostFrame(long sceneTime, double deltaTime) {

            }

            @Override
            public boolean callPreFrame() {
                return true;
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

            // Calculate click location in u,v (0;1) coordinates.
            float u = motionEvent.getX() / view.getWidth();
            float v = motionEvent.getY() / view.getHeight();

            try {
                // Fit a plane on the clicked point using the latest point cloud data
                // Synchronize against concurrent access to the RGB timestamp
                // in the OpenGL thread and a possible service disconnection
                // due to an onPause event.
                float[] planeFitTransform;
                synchronized (this) {
                    planeFitTransform = doFitPlane(u, v, rgbTimestampGlThread);
                }

                if (planeFitTransform != null) {
                    // Update the position of the rendered cube to the pose of the detected plane
                    // This update is made thread safe by the renderer
                    renderer.updateObjectPose(planeFitTransform);
                }

            } catch (TangoException t) {
                Toast.makeText(getApplicationContext(),
                    R.string.failed_measurement,
                    Toast.LENGTH_SHORT).show();
                Log.e(TAG, getString(R.string.failed_measurement), t);
            } catch (SecurityException t) {
                Toast.makeText(getApplicationContext(),
                    R.string.failed_permissions,
                    Toast.LENGTH_SHORT).show();
                Log.e(TAG, getString(R.string.failed_permissions), t);
            }
        }
        return true;
    }

    /**
     * Use the TangoSupport library with point cloud data to calculate the plane
     * of the world feature pointed at the location the camera is looking.
     * It returns the transform of the fitted plane in a double array.
     */
    private float[] doFitPlane(float u, float v, double rgbTimestamp) {
        TangoXyzIjData xyzIj = tangoPointCloudManager.getLatestXyzIj();

        if (xyzIj == null) {
            return null;
        }

        // We need to calculate the transform between the color camera at the
        // time the user clicked, and the depth camera at the time the depth
        // cloud was acquired.
        TangoPoseData colorTdepthPose =
            TangoSupport.calculateRelativePose(
                rgbTimestamp, TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR,
                xyzIj.timestamp, TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH);

        // Perform plane fitting with the latest available point cloud data.
        IntersectionPointPlaneModelPair intersectionPointPlaneModelPair =
            TangoSupport.fitPlaneModelNearClick(xyzIj, tangoCameraIntrinsics, colorTdepthPose, u, v);

        // Get the transform from depth camera to OpenGL world at
        // the timestamp of the cloud.
        TangoMatrixTransformData transform =
            TangoSupport.getMatrixTransformAtTime(
                xyzIj.timestamp,
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH,
                TANGO_SUPPORT_ENGINE_OPENGL,
                TANGO_SUPPORT_ENGINE_TANGO);

        if (transform.statusCode == TangoPoseData.POSE_VALID) {
            float[] openGlTPlane = calculatePlaneTransform(
                intersectionPointPlaneModelPair.intersectionPoint,
                intersectionPointPlaneModelPair.planeModel, transform.matrix);

            return openGlTPlane;
        } else {
            Log.w(TAG, "Can't get depth camera transform at time " + xyzIj.timestamp);
            return null;
        }
    }

    /**
     * Calculate the pose of the plane based on the position and normal orientation of the plane
     * and align it with gravity.
     */
    private float[] calculatePlaneTransform(double[] point, double normal[],
                                            float[] openGlTdepth) {
        // Vector aligned to gravity.
        float[] openGlUp = new float[]{0, 1, 0, 0};
        float[] depthTOpenGl = new float[16];
        Matrix.invertM(depthTOpenGl, 0, openGlTdepth, 0);
        float[] depthUp = new float[4];
        Matrix.multiplyMV(depthUp, 0, depthTOpenGl, 0, openGlUp, 0);
        // Create the plane matrix transform in depth frame from a point, the plane normal and the
        // up vector.
        float[] depthTplane = matrixFromPointNormalUp(point, normal, depthUp);
        float[] openGlTplane = new float[16];
        Matrix.multiplyMM(openGlTplane, 0, openGlTdepth, 0, depthTplane, 0);
        return openGlTplane;
    }

    /**
     * Calculates a transformation matrix based on a point, a normal and the up gravity vector.
     * The coordinate frame of the target transformation will be Z forward, X left, Y up.
     */
    private float[] matrixFromPointNormalUp(double[] point, double[] normal, float[] up) {
        float[] zAxis = new float[]{(float) normal[0], (float) normal[1], (float) normal[2]};
        normalize(zAxis);
        float[] xAxis = crossProduct(zAxis, up);
        normalize(xAxis);
        float[] yAxis = crossProduct(zAxis, xAxis);
        normalize(yAxis);
        float[] m = new float[16];
        Matrix.setIdentityM(m, 0);
        m[0] = xAxis[0];
        m[1] = xAxis[1];
        m[2] = xAxis[2];
        m[4] = yAxis[0];
        m[5] = yAxis[1];
        m[6] = yAxis[2];
        m[8] = zAxis[0];
        m[9] = zAxis[1];
        m[10] = zAxis[2];
        m[12] = (float) point[0];
        m[13] = (float) point[1];
        m[14] = (float) point[2];
        return m;
    }

    /**
     * Normalize a vector.
     */
    private void normalize(float[] v) {
        double norm = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] /= norm;
        v[1] /= norm;
        v[2] /= norm;
    }

    /**
     * Cross product between two vectors following the right hand rule.
     */
    private float[] crossProduct(float[] v1, float[] v2) {
        float[] result = new float[3];
        result[0] = v1[1] * v2[2] - v2[1] * v1[2];
        result[1] = v1[2] * v2[0] - v2[2] * v1[0];
        result[2] = v1[0] * v2[1] - v2[0] * v1[1];
        return result;
    }
}
