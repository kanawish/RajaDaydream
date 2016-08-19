/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kanawish.raja.controlroom;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;

import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoPoseData;
import com.kanawish.raja.raja.ScenePoseCalculator;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Very simple example augmented reality renderer which displays a cube fixed in place.
 * The position of the cube in the OpenGL world is updated using the {@code updateObjectPose}
 * method.
 */
public class ControlRoomRenderer extends Renderer {

    private static final String TAG = ControlRoomRenderer.class.getSimpleName();

    private static final float CUBE_SIDE_LENGTH = 0.5f;

    // Augmented Reality related fields
    private ATexture tangoCameraTexture;
    private boolean sceneCameraConfigured;

    // TODO: Floor, Viewer's camera, 2-3 Models
    private Object3D cube;
    private Object3D sphere;
    private Object3D plane;

    private Matrix4 objectTransform;
    private boolean objectPoseUpdated = false;

    public ControlRoomRenderer(Context context) {
        super(context);
    }

    @Override
    protected void initScene() {
        // Create a quad covering the whole background and assign a texture to it where the
        // Tango color camera contents will be rendered.
        ScreenQuad backgroundQuad = new ScreenQuad();
        backgroundQuad.setDoubleSided(true);
        Material tangoCameraMaterial = new Material();
        tangoCameraMaterial.setColorInfluence(0);

        // We need to use Rajawali's {@code StreamingTexture} since it sets up the texture
        // for GL_TEXTURE_EXTERNAL_OES rendering
        tangoCameraTexture =
                new StreamingTexture("camera", (StreamingTexture.ISurfaceListener) null);
        try {
            tangoCameraMaterial.addTexture(tangoCameraTexture);
            backgroundQuad.setMaterial(tangoCameraMaterial);
        } catch (ATexture.TextureException e) {
            Log.e(TAG, "Exception creating texture for RGB camera contents", e);
        }
        getCurrentScene().addChildAt(backgroundQuad, 0);
        backgroundQuad.rotate(Vector3.Axis.X,180);

        // Add a directional light in an arbitrary direction.
        DirectionalLight light = new DirectionalLight(1, -0.5, -1);
        light.setColor(1, 1, 1);
        light.setPower(1.2f);
        light.setPosition(0, 10, 0);
        getCurrentScene().addLight(light);

        // Set-up a material
        Material cubeMaterial = buildMaterial(Color.RED);

        // Build a Cube
        cube = new Cube(CUBE_SIDE_LENGTH);
        cube.setMaterial(cubeMaterial);
        cube.setPosition(0, 0, 0);
        cube.setRotation(Vector3.Axis.Z, 180);
        cube.setVisible(false);
        getCurrentScene().addChild(cube);

        // Set-up a material
        Material sphereMaterial = buildMaterial(Color.BLUE);

        // Build a Sphere
        sphere = new Sphere(0.25f,20,20);
        sphere.setMaterial(sphereMaterial);
        sphere.setPosition(0, 0, 0);
        sphere.setVisible(false);
        getCurrentScene().addChild(sphere);

        Material checkerboard = new Material();
        try {
            checkerboard.addTexture(new Texture("checkerboard", R.drawable.checkerboard));
            checkerboard.setColorInfluence(0);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        plane = new Plane();
        plane.setMaterial(checkerboard);
        plane.setDoubleSided(true);
        plane.setColor(0xff0000ff);
        plane.setVisible(false);
        getCurrentScene().addChild(plane);
    }

    @NonNull
    private Material buildMaterial(int color) {
        Material material = new Material();
        material.setColor(color);
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        material.setSpecularMethod(new SpecularMethod.Phong());
        return material;
    }

    enum Furniture {
        PLANE, CUBE, SPHERE ;
        Furniture next() {
            int i = (this.ordinal() + 1) % Furniture.values().length;
            return Furniture.values()[i];
        }
    }
    Furniture currentFurniture = Furniture.PLANE;

    @Override
    protected void onRender(long elapsedRealTime, double deltaTime) {
        // Update the AR object if necessary
        // Synchronize against concurrent access with the setter below.
        synchronized (this) {
            if (objectPoseUpdated) {
                // Place the 3D object in the location of the detected plane.
                switch ( currentFurniture ) {
                    case PLANE:
                        plane.setPosition(objectTransform.getTranslation());
                        plane.setOrientation(new Quaternion().fromMatrix(objectTransform).conjugate());
                        plane.setVisible(true);
                        break;
                    case CUBE:
                        cube.setPosition(objectTransform.getTranslation());
                        cube.setOrientation(new Quaternion().fromMatrix(objectTransform).conjugate());
                        // Move it forward by half of the size of the cube to make it
                        // flush with the plane surface.
                        cube.moveForward(CUBE_SIDE_LENGTH / 2.0f);
                        cube.setVisible(true);
                        break;
                    case SPHERE:
                        sphere.setPosition(objectTransform.getTranslation());
                        sphere.setOrientation(new Quaternion().fromMatrix(objectTransform).conjugate());
                        sphere.moveForward(0.25f);
                        sphere.setVisible(true);
                        break;
                }
                currentFurniture = currentFurniture.next();

                // TODO: Add a way to orient things placed on the floor.

                objectPoseUpdated = false;
            }
        }

        super.onRender(elapsedRealTime, deltaTime);
    }

    /**
     * Save the updated plane fit pose to update the AR object on the next render pass.
     * This is synchronized against concurrent access in the render loop above.
     */
    public synchronized void updateObjectPose(float[] planeFitTransform) {
        objectTransform = new Matrix4(planeFitTransform);
        objectPoseUpdated = true;

    }

    /**
     * Update the scene camera based on the provided pose in Tango start of service frame.
     * The camera pose should match the pose of the camera color at the time the last rendered RGB
     * frame, which can be retrieved with this.getTimestamp();
     * <p/>
     * NOTE: This must be called from the OpenGL render thread - it is not thread safe.
     */
    public void updateRenderCameraPose(TangoPoseData cameraPose) {
        float[] translation = cameraPose.getTranslationAsFloats();
        float[] rotation = cameraPose.getRotationAsFloats();

        getCurrentCamera().setPosition(translation[0], translation[1], translation[2]);
        Quaternion quaternion = new Quaternion(rotation[3], rotation[0], rotation[1], rotation[2]);
//        getCurrentCamera().setRotation(quaternion.conjugate());
        getCurrentCamera().setRotation(quaternion.conjugate());
    }

    /**
     * It returns the ID currently assigned to the texture where the Tango color camera contents
     * should be rendered.
     * NOTE: This must be called from the OpenGL render thread - it is not thread safe.
     */
    public int getTextureId() {
        return tangoCameraTexture == null ? -1 : tangoCameraTexture.getTextureId();
    }

    /**
     * We need to override this method to mark the camera for re-configuration (set proper
     * projection matrix) since it will be reset by Rajawali on surface changes.
     */
    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
        sceneCameraConfigured = false;
    }

    public boolean isSceneCameraConfigured() {
        return sceneCameraConfigured;
    }

    /**
     * Sets the projection matrix for the scene camera to match the parameters of the color camera,
     * provided by the {@code TangoCameraIntrinsics}.
     */
    public void setProjectionMatrix(TangoCameraIntrinsics intrinsics) {
        Matrix4 projectionMatrix = ScenePoseCalculator.calculateProjectionMatrix(
                intrinsics.width, intrinsics.height,
                intrinsics.fx, intrinsics.fy, intrinsics.cx, intrinsics.cy);
        getCurrentCamera().setProjectionMatrix(projectionMatrix);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset,
                                 float xOffsetStep, float yOffsetStep,
                                 int xPixelOffset, int yPixelOffset) {
    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    private Object3D buildOBJ() {
        Object3D o ;
        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.untitled_3_obj);
        try {
            objParser.parse();
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        o = objParser.getParsedObject();
        o.setPosition(0,-8,-1);

        getCurrentScene().addChild(o);

        Animation3D anim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
        anim.setDurationMilliseconds(16000);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        anim.setTransformable3D(o);
        getCurrentScene().registerAnimation(anim);
        anim.play();

        return o;
    }

}
