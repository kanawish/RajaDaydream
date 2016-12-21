package com.kanawish.raja;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;

import com.kanawish.raja.rajademo.R;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Sphere;

class SimpleRenderer extends BaselineRenderer {

    SimpleRenderer(Context context, @Nullable BaselineRenderingFragment fragment) {
        super(context, fragment);
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
            DirectionalLight key = new DirectionalLight(0,0,0);
            key.setPosition(0.0,10.0,0.1);
            key.enableLookAt();
            key.setPower(2);
            getCurrentScene().addLight(key);

            // Models
            Material material = new Material();
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            material.setColor(0xffff0000);
            material.setColorInfluence(1);
            material.enableLighting(true);
            Cube cube = new Cube(1);
            cube.setMaterial(material);
            getCurrentScene().addChild(cube);

            Sphere sphere = new Sphere(0.5f,10,10);
            sphere.setMaterial(material);
            getCurrentScene().addChild(sphere);
            animateOrbit(new Vector3(0),sphere);

            // Camera,
            getCurrentCamera().setPosition(0, 5, 4);
            getCurrentCamera().setLookAt(0.0, 0.0, -0.1);

            // Action!
            RotateOnAxisAnimation anim = new RotateOnAxisAnimation(new Vector3(1,1,1), 360);
            anim.setTransformable3D(cube);
            anim.setDurationMilliseconds(20000);
            anim.setRepeatMode(Animation.RepeatMode.INFINITE);
            getCurrentScene().registerAnimation(anim);
//            anim.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateOrbit(Vector3 focalPoint, ATransformable3D transformable3D) {
        EllipticalOrbitAnimation3D lightAnim = new EllipticalOrbitAnimation3D(
                new Vector3(1,0,0), new Vector3(3, 0, 0), Vector3.getAxisVector(Vector3.Axis.Y), 0, 360,
                EllipticalOrbitAnimation3D.OrbitDirection.CLOCKWISE);
        lightAnim.setDurationMilliseconds(2000);
        lightAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
        lightAnim.setTransformable3D(transformable3D);
        getCurrentScene().registerAnimation(lightAnim);
        lightAnim.play();
    }

    @Override
    public void onRender(final long ellapsedTime, final double deltaTime) {
        super.onRender(ellapsedTime, deltaTime);
    }
}
