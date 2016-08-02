package com.kanawish.raja;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kanawish.raja.rajademo.R;

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
import org.rajawali3d.materials.plugins.FogMaterialPlugin;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.postprocessing.PostProcessingManager;
import org.rajawali3d.postprocessing.effects.BloomEffect;
import org.rajawali3d.postprocessing.passes.BlendPass;

public class DemoFragment extends BaselineRenderingFragment {

    private PostProcessingManager effectsManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        LinearLayout uiOverlay = new LinearLayout(getActivity());
        uiOverlay.setOrientation(LinearLayout.VERTICAL);
        uiOverlay.setGravity(Gravity.BOTTOM);

        // TODO: Add ui elements here.
        layout.addView(uiOverlay);

        return layout;
    }

    @Override
    public BaselineRenderer createRenderer() {
        return new FBXRenderer(getActivity(), this);
    }

    public static DemoFragment buildInstance() {
        return new DemoFragment();
    }

    private final class FBXRenderer extends BaselineRenderer {

        public FBXRenderer(Context context, @Nullable BaselineRenderingFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected void initScene() {

            // Add overall light to our scene. Directional behaves like sunlight.
            DirectionalLight light = buildDirectionalLight();
            getCurrentScene().addLight(light);

            // For skybox below.
            getCurrentCamera().setFarPlane(1000);

             // Skybox images by Emil Persson, aka Humus. http://www.humus.name humus@comhem.se
            try {
                getCurrentScene().setSkybox(R.drawable.posx, R.drawable.negx,
                    R.drawable.posy, R.drawable.negy, R.drawable.posz, R.drawable.negz);
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }

            getCurrentScene().addChild(buildLandscape());

            getCurrentCamera().setPosition(0,2,8);
            getCurrentCamera().setLookAt(0,1,-40);

            getCurrentScene().setFog(new FogMaterialPlugin.FogParams(FogMaterialPlugin.FogType.LINEAR, 0xCCCCCC, 1, 150));

            Material planeMaterial = new Material();
            planeMaterial.enableLighting(true);
            planeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

            Material sphereMaterial = new Material();
            sphereMaterial.enableLighting(true);
            sphereMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

            //
            // -- Create a post processing manager. We can add multiple passes to this.
            //

            effectsManager = new PostProcessingManager(this);

            BloomEffect bloomEffect = new BloomEffect(
                getCurrentScene(), getCurrentCamera(), getViewportWidth(),
                getViewportHeight(), 0x111111, 0xffffff, BlendPass.BlendMode.SCREEN);
            effectsManager.addEffect(bloomEffect);
            bloomEffect.setRenderToScreen(true);

        }

        public void onRender(final long ellapsedTime, final double deltaTime) {
            effectsManager.render(ellapsedTime, deltaTime);
        }

        private Object3D buildLandscape() {
            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.minecart_scene_obj);
            try {
                objParser.parse();
            } catch (ParsingException e) {
                e.printStackTrace();
            }

            Object3D o = objParser.getParsedObject();
            return o;
        }

        private Object3D buildTree() {
            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.small_tree_color_obj);
            try {
                objParser.parse();
            } catch (ParsingException e) {
                e.printStackTrace();
            }

            Object3D o = objParser.getParsedObject();
            o.setPosition(0,0,0);

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
            directionalLight.setPosition(0.20, 5.0, 0.6);
            directionalLight.setPower(0.95f);
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
    }
}