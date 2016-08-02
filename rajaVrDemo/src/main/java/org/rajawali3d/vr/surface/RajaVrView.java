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

import org.rajawali3d.vr.renderer.RajaStereoRenderer;

import static org.rajawali3d.view.ISurface.*;

/**
 *
 */
public class RajaVrView extends GvrView {

    protected ANTI_ALIASING_CONFIG mAntiAliasingConfig = ANTI_ALIASING_CONFIG.NONE;

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
        final TypedArray array = context.obtainStyledAttributes(attrs, org.rajawali3d.R.styleable.SurfaceView);
        final int count = array.getIndexCount();
        for (int i = 0; i < count; ++i) {
            int attr = array.getIndex(i);
            if (attr == org.rajawali3d.R.styleable.SurfaceView_antiAliasingType) {
                mAntiAliasingConfig = ANTI_ALIASING_CONFIG
                    .fromInteger(array.getInteger(attr, ANTI_ALIASING_CONFIG.NONE.ordinal()));
            }
            // TODO: Add other xml configuration attributes.
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
