/**
 * Copyright 2015 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.rajawali3d.vr.renderer;

import android.content.Context;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;

import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

/**
 * original: @author dennis.ippel
 *
 * modifications: etienne.caron
 *
 * NOTE: RajaStereoRenderer, when used as the base class for your own renderer implementation,
 * will feed all of Gvr's head-tracking data into the Raja-scene.
 *
 * This class is almost identical to the original Rajawali VR renderer.
 */
public abstract class RajaStereoRenderer extends Renderer implements GvrView.StereoRenderer {
    private static final float MAX_LOOKAT_ANGLE = 10;

    protected Matrix4 currentEyeMatrix;
    protected Matrix4 headViewMatrix;
    protected Quaternion currentEyeOrientation;
    protected Quaternion headViewQuaternion;
    protected Vector3 cameraPosition;
    private Vector3 forwardVec;
    private Vector3 headTranslation;

    private Matrix4 lookingAtMatrix;
    private float[] headView;

    // Using relays allows for less spammy logs, possibly other features as well.

    PublishSubject<float[]> headTransforms = PublishSubject.create();

	public RajaStereoRenderer(Context context) {
		super(context);
        currentEyeMatrix = new Matrix4();
        headViewMatrix = new Matrix4();
        lookingAtMatrix = new Matrix4();
        currentEyeOrientation = new Quaternion();
        headViewQuaternion = new Quaternion();
        headView = new float[16];
        cameraPosition = new Vector3();
        forwardVec = new Vector3();
        headTranslation = new Vector3();

        headTransforms
            .sample(1500, TimeUnit.MILLISECONDS)
            .subscribe((headView) -> Timber.d("HeadTransform: %f,%f,%f,%f", headView[0], headView[1], headView[2], headView[3]));
    }

	@Override
	public void onRender(long elapsedTime, double deltaTime) {
		super.onRender(elapsedTime, deltaTime);
	}

    // We don't care about this, only relevant for wallpapers.
    @Override
    public final void onOffsetsChanged(float v, float v2, float v3, float v4, int i, int i2) {
    }

    // NOTE: Head tracking info from cardboard is passed for every frame.
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        float[] headView = headTransform.getHeadView();
        headTransforms.onNext(headView);
        // Copies values from headTransform into this.headView.
        headTransform.getHeadView(this.headView, 0);
        // Then into this.headViewMatrix
        headViewMatrix.setAll(this.headView);
    }

    // NOTE: Eye 'camera' info from cardboard is passed for every frame.
    @Override
    public void onDrawEye(Eye eye) {
        getCurrentCamera().updatePerspective(
                eye.getFov().getLeft(),
                eye.getFov().getRight(),
                eye.getFov().getBottom(),
                eye.getFov().getTop());
        currentEyeMatrix.setAll(eye.getEyeView());
        currentEyeOrientation.fromMatrix(currentEyeMatrix);
        getCurrentCamera().setOrientation(currentEyeOrientation.invertAndCreate());
        getCurrentCamera().setPosition(cameraPosition);
        getCurrentCamera().getPosition().add(currentEyeMatrix.getTranslation().inverse());
        super.onRenderFrame(null);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onRenderSurfaceSizeChanged(null, width, height);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        super.onRenderSurfaceCreated(eglConfig, null, -1, -1);
    }

    @Override
    public void onRendererShutdown() {
        super.onRenderSurfaceDestroyed(null);
    }

    public boolean isLookingAtObject(Object3D target) {
        return this.isLookingAtObject(target, MAX_LOOKAT_ANGLE);
    }

    // NOTE: This convenience method allows you to interact with the VR world.
    public boolean isLookingAtObject(Object3D target, float maxAngle) {
        headViewQuaternion.fromMatrix(headViewMatrix);
        headViewQuaternion.inverse();
        forwardVec.setAll(0, 0, 1);
        forwardVec.rotateBy(headViewQuaternion);

        headTranslation.setAll(headViewMatrix.getTranslation());
        headTranslation.subtract(target.getPosition());
        headTranslation.normalize();

        return headTranslation.angle(forwardVec) < maxAngle;
    }
}
