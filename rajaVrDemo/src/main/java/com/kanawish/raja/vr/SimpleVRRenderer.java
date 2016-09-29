package com.kanawish.raja.vr;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;

import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.vr.renderer.RajaStereoRenderer;

class SimpleVrRenderer extends RajaStereoRenderer {

    SimpleVrRenderer(Context context) {
        super(context);
    }

    @Override
    protected void initScene() {
        try {
            getCurrentScene().setBackgroundColor(Color.BLUE);

            getCurrentCamera().setFarPlane(1000);

            // Skybox images by Emil Persson, aka Humus. http://www.humus.name humus@comhem.se
            try {
                getCurrentScene().setSkybox(R.drawable.posx, R.drawable.negx,
                        R.drawable.posy, R.drawable.negy, R.drawable.posz, R.drawable.negz);
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }

            // Lights,
            DirectionalLight key = new DirectionalLight(0, 0.1, 0.2);
            key.setPosition(0.0, 10.0, 2.5);
            key.enableLookAt();
            key.setPower(2);
            getCurrentScene().addLight(key);

            // Models
            Material material = new Material();
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            material.enableLighting(true);
            Cube cube = new Cube(1);
            cube.setPosition(0, -1, -3);
            cube.setMaterial(material);
            getCurrentScene().addChild(cube);

            // NOTE: Camera is controlled via VR headset orientation.

            // Action!
            RotateOnAxisAnimation anim = new RotateOnAxisAnimation(new Vector3(1, 1, 1), 360);
            anim.setTransformable3D(cube);
            anim.setDurationMilliseconds(20000);
            anim.setRepeatMode(Animation.RepeatMode.INFINITE);
            getCurrentScene().registerAnimation(anim);
            anim.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRender(final long ellapsedTime, final double deltaTime) {
        super.onRender(ellapsedTime, deltaTime);
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
    }
}
