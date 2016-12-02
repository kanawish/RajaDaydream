package com.kanawish.raja;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.postprocessing.PostProcessingManager;
import org.rajawali3d.postprocessing.effects.ShadowEffect;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.Sphere;

public class ShadowMappingRenderer extends BaselineRenderer {

    private PostProcessingManager mPostProcessingManager;

    public ShadowMappingRenderer(Context context, @Nullable BaselineRenderingFragment fragment) {
        super(context, fragment);
    }

    @Override
    protected void initScene() {
        try {
            getCurrentScene().setBackgroundColor(Color.BLUE);

            DirectionalLight key = new DirectionalLight(0,-2,0);
            key.setPosition(0,2,0.1);
//            key.setLookAt(0,-2,0);
            key.enableLookAt();
            key.setPower(4);
            getCurrentScene().addLight(key);

            Material material = new Material();
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            material.enableLighting(true);
            Cube cube = new Cube(1);
            cube.setMaterial(material);
            getCurrentScene().addChild(cube);

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

    public void initScene2() {
        Vector3 focalPoint = new Vector3(0,7,0);

        DirectionalLight mLight;
        mLight = new DirectionalLight();
        mLight.setPosition(0.0, 1.0, 0.01f);
        mLight.setPower(0.95f);
        mLight.setLookAt(Vector3.ZERO);
        mLight.enableLookAt();
        getCurrentScene().addLight(mLight);

        Material lightMaterial = new Material();
        lightMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

        // Build a Sphere
        Sphere sphere = new Sphere(0.55f,20,20);
        sphere.setMaterial(lightMaterial);
        sphere.setColor(Color.YELLOW);
        sphere.setPosition(mLight.getPosition());
        sphere.setVisible(true);

//        animate(focalPoint, mLight);
//        getCurrentScene().addLight(mLight);
//
//        animate(focalPoint, sphere);
//        getCurrentScene().addChild(sphere);

        getCurrentCamera().setFarPlane(50);
        getCurrentCamera().setPosition(0, 7, 15);
        getCurrentCamera().setLookAt(0, 0, 0);
        getCurrentCamera().enableLookAt();

        Material planeMaterial = new Material();
        planeMaterial.enableLighting(true);
        planeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

        Plane plane = new Plane(Vector3.Axis.Y);
        plane.setScale(7.5);
        plane.setMaterial(planeMaterial);
        plane.setColor(Color.GREEN);
        getCurrentScene().addChild(plane);

        Material regCubeMaterial = new Material();
        regCubeMaterial.enableLighting(true);
        regCubeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

        for (int z = 0; z < 10; z++) {
            for (int x = 0; x < 10; x++) {
                Cube cube = new Cube(.7f);
                cube.setMaterial(regCubeMaterial);
                cube.setColor(Color.rgb(100 + 10 * x, 0, 0));
                cube.setPosition(-4.5f + x, 5, -4.5f + z);

                getCurrentScene().addChild(cube);
            }
        }

        Material cubeMaterial = new Material();
        cubeMaterial.enableLighting(true);
        cubeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

        Cube cube = new Cube(2);
        cube.setMaterial(cubeMaterial);
        cube.setColor(Color.GRAY);
        cube.setY(1.5f);
        getCurrentScene().addChild(cube);


/*
        mPostProcessingManager = new PostProcessingManager(this);
        ShadowEffect shadowEffect = new ShadowEffect(getCurrentScene(), getCurrentCamera(), mLight, 2048);
        shadowEffect.setShadowInfluence(2.5f);
        mPostProcessingManager.addEffect(shadowEffect);
        shadowEffect.setRenderToScreen(true);
*/
    }

    private void animate(Vector3 focalPoint, ATransformable3D transformable3D) {
        EllipticalOrbitAnimation3D lightAnim = new EllipticalOrbitAnimation3D(
            focalPoint, new Vector3(0, 12, 5), 0, 360,
            EllipticalOrbitAnimation3D.OrbitDirection.CLOCKWISE);
        lightAnim.setDurationMilliseconds(20000);
        lightAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
        lightAnim.setTransformable3D(transformable3D);
        getCurrentScene().registerAnimation(lightAnim);
        lightAnim.play();
    }

    @Override
    public void onRender(final long ellapsedTime, final double deltaTime) {
        super.onRender(ellapsedTime, deltaTime);
//        mPostProcessingManager.render(ellapsedTime, deltaTime);
    }
}
