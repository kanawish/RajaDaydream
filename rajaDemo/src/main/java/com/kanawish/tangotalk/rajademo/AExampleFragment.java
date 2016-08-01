package com.kanawish.tangotalk.rajademo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import org.rajawali3d.IRajawaliDisplay;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.IRajawaliSurfaceRenderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class AExampleFragment extends Fragment implements IRajawaliDisplay, View.OnClickListener {

    public static final String BUNDLE_EXAMPLE_URL = "BUNDLE_EXAMPLE_URL";

    protected ProgressBar mProgressBarLoader;
//    protected GitHubLogoView mImageViewExampleLink;
//    protected String mExampleUrl;
    protected FrameLayout mLayout;
    protected IRajawaliSurface mRenderSurface;
    protected IRajawaliSurfaceRenderer mRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*
        final Bundle bundle = getArguments();
        if (bundle == null || !bundle.containsKey(BUNDLE_EXAMPLE_URL)) {
            throw new IllegalArgumentException(getClass().getSimpleName()
                    + " requires " + BUNDLE_EXAMPLE_URL + " argument at runtime!");
        }

        mExampleUrl = bundle.getString(BUNDLE_EXAMPLE_URL);
*/
    }

    @Override
    public int getLayoutID() {
        throw new IllegalStateException("Don't call layout ID, assign your own!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the view
        mLayout = (FrameLayout) inflater.inflate(R.layout.rajawali_textureview_fragment, container, false);

        // Find the TextureView
        mRenderSurface = (IRajawaliSurface) mLayout.findViewById(R.id.rajwali_surface);

        // Create the renderer
        mRenderer = createRenderer();
        onBeforeApplyRenderer();
        applyRenderer();
        return mLayout;
    }

    protected void onBeforeApplyRenderer() {
    }

    @CallSuper
    protected void applyRenderer() {
        mRenderSurface.setSurfaceRenderer(mRenderer);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mLayout != null)
            mLayout.removeView((View) mRenderSurface);
    }

    protected static abstract class AExampleRenderer extends RajawaliRenderer {

        final AExampleFragment exampleFragment;

        public AExampleRenderer(Context context, @Nullable AExampleFragment fragment) {
            super(context);
            exampleFragment = fragment;
        }

        @Override
        public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
//            if (exampleFragment != null) exampleFragment.showLoader();
            super.onRenderSurfaceCreated(config, gl, width, height);
//            if (exampleFragment != null) exampleFragment.hideLoader();
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
        }

    }

}