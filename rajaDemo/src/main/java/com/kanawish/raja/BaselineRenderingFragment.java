package com.kanawish.raja;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.kanawish.raja.rajademo.R;

import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.view.IDisplay;
import org.rajawali3d.view.ISurface;

public abstract class BaselineRenderingFragment extends Fragment implements IDisplay, View.OnClickListener {

    protected FrameLayout layout;

    protected ISurface surface;
    protected ISurfaceRenderer renderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the view
        layout = (FrameLayout) inflater.inflate(R.layout.rajawali_textureview_fragment, container, false);

        // Find the TextureView
        surface = (ISurface) layout.findViewById(R.id.rajwali_surface);

        // Create the renderer
        renderer = createRenderer();
        onBeforeApplyRenderer();
        applyRenderer();
        return layout;
    }

    protected void onBeforeApplyRenderer() {
    }

    @CallSuper
    protected void applyRenderer() {
        surface.setSurfaceRenderer(renderer);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (layout != null)
            layout.removeView((View) surface);
    }


}