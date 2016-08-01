package org.rajawali3d.vr.surface;

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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.google.vr.sdk.base.GvrView;

import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.IRajawaliSurface.ANTI_ALIASING_CONFIG;
import org.rajawali3d.vr.renderer.RajaStereoRenderer;

/**
 *
 */
public class RajaVrView extends GvrView {

//    protected RendererDelegate mRendererDelegate;

    protected double               mFrameRate          = 60.0;
    protected int                  mRenderMode         = IRajawaliSurface.RENDERMODE_WHEN_DIRTY;
    protected ANTI_ALIASING_CONFIG mAntiAliasingConfig = ANTI_ALIASING_CONFIG.NONE;
    protected boolean              mIsTransparent      = false;
    protected int                  mBitsRed            = 5;
    protected int                  mBitsGreen          = 6;
    protected int                  mBitsBlue           = 5;
    protected int                  mBitsAlpha          = 0;
    protected int                  mBitsDepth          = 16;
    protected int                  mMultiSampleCount   = 0;

    private RajaStereoRenderer rajaRenderer;

    public RajaVrView(Context context) {
        super(context);
    }

    public RajaVrView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttributes(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        final TypedArray array = context.obtainStyledAttributes(attrs, org.rajawali3d.R.styleable.RajawaliSurfaceView);
        final int count = array.getIndexCount();
        for (int i = 0; i < count; ++i) {
            int attr = array.getIndex(i);
            if (attr == org.rajawali3d.R.styleable.RajawaliSurfaceView_frameRate) {
                mFrameRate = array.getFloat(attr, 60.0f);
            } else if (attr == org.rajawali3d.R.styleable.RajawaliSurfaceView_renderMode) {
                mRenderMode = array.getInt(attr, IRajawaliSurface.RENDERMODE_WHEN_DIRTY);
            } else if (attr == org.rajawali3d.R.styleable.RajawaliSurfaceView_antiAliasingType) {
                mAntiAliasingConfig = ANTI_ALIASING_CONFIG
                    .fromInteger(array.getInteger(attr, ANTI_ALIASING_CONFIG.NONE.ordinal()));
            } else if (attr == org.rajawali3d.R.styleable.RajawaliSurfaceView_multiSampleCount) {
                mMultiSampleCount = array.getInteger(attr, 0);
            } else if (attr == org.rajawali3d.R.styleable.RajawaliSurfaceView_isTransparent) {
                mIsTransparent = array.getBoolean(attr, false);
            } else if (attr == org.rajawali3d.R.styleable.RajawaliSurfaceView_bitsRed) {
                mBitsRed = array.getInteger(attr, 5);
            } else if (attr == org.rajawali3d.R.styleable.RajawaliSurfaceView_bitsGreen) {
                mBitsGreen = array.getInteger(attr, 6);
            } else if (attr == org.rajawali3d.R.styleable.RajawaliSurfaceView_bitsBlue) {
                mBitsBlue = array.getInteger(attr, 5);
            } else if (attr == org.rajawali3d.R.styleable.RajawaliSurfaceView_bitsAlpha) {
                mBitsAlpha = array.getInteger(attr, 0);
            } else if (attr == org.rajawali3d.R.styleable.RajawaliSurfaceView_bitsDepth) {
                mBitsDepth = array.getInteger(attr, 16);
            }
        }
        array.recycle();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (rajaRenderer != null) {
            rajaRenderer.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rajaRenderer != null) {
            rajaRenderer.onResume();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            rajaRenderer.onRenderSurfaceDestroyed(null);
        } catch (NullPointerException ignored) {
            // Don't care, activity is terminating.
        }
    }

    @Override
    public void setRenderer(GvrView.StereoRenderer renderer) throws IllegalStateException {
        if (rajaRenderer != null) {
            throw new IllegalStateException("A renderer has already been set for this view.");
        }

        if( !(renderer instanceof RajaStereoRenderer)) {
            throw new IllegalStateException("Only accepts RajaStereoRenderer");
        }

        rajaRenderer = (RajaStereoRenderer) renderer;
        rajaRenderer.setAntiAliasingMode(mAntiAliasingConfig);

        super.setRenderer(renderer);
    }

}
