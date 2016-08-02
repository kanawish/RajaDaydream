package com.kanawish.raja.vr;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.plugins.FogMaterialPlugin;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.postprocessing.PostProcessingManager;
import org.rajawali3d.postprocessing.effects.BloomEffect;
import org.rajawali3d.postprocessing.passes.BlendPass;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.vr.renderer.RajaStereoRenderer;

public final class DemoVRRenderer extends RajaStereoRenderer {

    private Material sphereAMaterial;
    private Material lookedAtMaterial;
    private Object3D sphereA;

    private PostProcessingManager effectsManager;

    public DemoVRRenderer(Context context) {
        super(context);
    }

    @Override
    public void initScene() {

        getCurrentCamera().setFarPlane(1000);

        // Skybox images by Emil Persson, aka Humus. http://www.humus.name humus@comhem.se
        try {
            getCurrentScene().setSkybox(R.drawable.posx, R.drawable.negx,
                R.drawable.posy, R.drawable.negy, R.drawable.posz, R.drawable.negz);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        sphereAMaterial = buildMaterial(Color.YELLOW);
        lookedAtMaterial = buildMaterial(Color.RED);

        sphereA = new Sphere(1, 30, 30);
        sphereA.setPosition(-4.5f, -1.75f, -10f);
        sphereA.setMaterial(sphereAMaterial);
        getCurrentScene().addChild(sphereA);

        getCurrentScene().addLight(buildDirectionalLight(-2.0, -8.0, -5.0, 1.5f));

        Object3D landscape = buildLandscape();
        landscape.setPosition(0,-3,-8);
        getCurrentScene().addChild(landscape);

        Object3D tree = buildOBJ(R.raw.demo_1_obj);
        tree.setPosition(3, -3, -8);
        getCurrentScene().addChild(tree);

        getCurrentScene().setFog(new FogMaterialPlugin.FogParams(FogMaterialPlugin.FogType.LINEAR, 0xCCCCCC, 1, 150));

        // Raja-effects don't play nice with Gvr.
        // TODO: GPU post processing? Using render to texture?
/*
        effectsManager = new PostProcessingManager(this);
        BloomEffect bloomEffect = new BloomEffect(
            getCurrentScene(), getCurrentCamera(), getViewportWidth(),
            getViewportHeight(), 0x111111, 0xffffff, BlendPass.BlendMode.SCREEN);
        effectsManager.addEffect(bloomEffect);
        bloomEffect.setRenderToScreen(true);
*/
    }

    @Override
    public void onRender(long elapsedTime, double deltaTime) {
        // Let's highlight an object when it's looked-at.
        sphereA.setMaterial(isLookingAtObject(sphereA)?lookedAtMaterial:sphereAMaterial);

        // Parent will render the scene.
        super.onRender(elapsedTime, deltaTime);
    }

    private Object3D buildOBJ(int rawObjResId) {
        Object3D o;
        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), getTextureManager(), rawObjResId);
        try {
            objParser.parse();
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        o = objParser.getParsedObject();
        return o;
    }

    private Object3D buildLandscape() {
        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.landscape_v2_obj);
        try {
            objParser.parse();
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        Object3D o = objParser.getParsedObject();
        return o;
    }


    @NonNull
    private DirectionalLight buildDirectionalLight(double x, double y, double z, float power) {
        final DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setPosition(x, y, z);
        directionalLight.setPower(power);
        directionalLight.setLookAt(Vector3.ZERO);
        directionalLight.enableLookAt();
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
