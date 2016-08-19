package com.kanawish.raja;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.postprocessing.PostProcessingManager;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.Sphere;

class SimpleRenderer extends BaselineRenderer {

    SimpleRenderer(Context context, @Nullable BaselineRenderingFragment fragment) {
        super(context, fragment);
    }

    @Override
    protected void initScene() {
        try {
            getCurrentScene().setBackgroundColor(Color.BLUE);

            // Lights,
            DirectionalLight key = new DirectionalLight(0,0.1,0.2);
            key.setPosition(0.0,10.0,2.5);
            key.enableLookAt();
            key.setPower(2);
            getCurrentScene().addLight(key);

            // Models
            Material material = new Material();
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            material.enableLighting(true);
            Cube cube = new Cube(1);
            cube.setMaterial(material);
            getCurrentScene().addChild(cube);

            // Camera,
            getCurrentCamera().setPosition(0, 2, 3);
            getCurrentCamera().setLookAt(0, 0, 0);

            // Action!
            RotateOnAxisAnimation anim = new RotateOnAxisAnimation(new Vector3(1,1,1), 360);
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
}
