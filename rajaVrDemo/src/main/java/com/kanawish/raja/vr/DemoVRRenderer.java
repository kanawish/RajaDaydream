package com.kanawish.raja.vr;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.vr.renderer.RajaStereoRenderer;

public final class DemoVRRenderer extends RajaStereoRenderer {

    private DirectionalLight directionalLight;

    public DemoVRRenderer(Context context) {
        super(context);
    }

    @Override
    public void initScene() {

        buildDirectionalLight();

        getCurrentCamera().setFarPlane(1000);
        /**
         * Skybox images by Emil Persson, aka Humus. http://www.humus.name humus@comhem.se
         */
        try {
            getCurrentScene().setSkybox(R.drawable.posx, R.drawable.negx,
                R.drawable.posy, R.drawable.negy, R.drawable.posz, R.drawable.negz);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        int plColor = Color.CYAN;
        PointLight pointLight = new PointLight();
        pointLight.setPosition(0f, -2f, -9.75f);
        pointLight.setPower(0.95f);
        pointLight.setColor(plColor);
        getCurrentScene().addLight(pointLight);

        Object3D plMarker = new Sphere(0.2f, 10, 10);
        plMarker.setPosition(pointLight.getPosition());
        plMarker.setMaterial(buildMaterial(plColor));
        getCurrentScene().addChild(plMarker);

        Material yellowMaterial = buildMaterial(Color.YELLOW);

        Object3D sphereA = new Sphere(1, 30, 30);
        sphereA.setPosition(-2, 0, -10);
        sphereA.setMaterial(yellowMaterial);
        getCurrentScene().addChild(sphereA);

        Object3D sphereB = new Sphere(1, 30, 30);
        sphereB.setPosition(2, 0, -10);
        sphereB.setMaterial(buildTextureMaterial(0));
        getCurrentScene().addChild(sphereB);

        buildOBJ();
    }


    private Object3D buildOBJ() {
        Object3D o;
        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), getTextureManager(), R.raw.demo_1_obj);
        try {
            objParser.parse();
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        o = objParser.getParsedObject();
        o.setPosition(0, -4, -5);

        getCurrentScene().addChild(o);

        Animation3D anim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
        anim.setDurationMilliseconds(16000);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        anim.setTransformable3D(o);
        getCurrentScene().registerAnimation(anim);
        anim.play();

        return o;
    }

    @NonNull
    private DirectionalLight buildDirectionalLight() {
        final DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setPosition(0.20, 1.0, 0.6);
        directionalLight.setPower(0.5f);
        directionalLight.setLookAt(Vector3.ZERO);
        directionalLight.enableLookAt();
        getCurrentScene().addLight(directionalLight);
        return directionalLight;
    }

    @NonNull
    private Material buildMaterial(int color) {
        Material material = new Material();
        material.enableLighting(true);
        material.setColor(color);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        return material;
    }

    private Material buildTextureMaterial(int color) {
        Material material = new Material();
        Texture texture = new Texture("earthColors",
            R.drawable.earthtruecolor_nasa_big);
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        material.setSpecularMethod(new SpecularMethod.Phong());

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        material.setColorInfluence(0);
        texture.setInfluence(1.0f);

        return material;
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        // Nothing to do.
    }
}
