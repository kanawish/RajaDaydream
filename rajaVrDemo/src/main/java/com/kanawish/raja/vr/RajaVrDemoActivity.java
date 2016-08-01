/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kanawish.raja.vr;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.GvrActivity;

import org.rajawali3d.vr.surface.RajaVrView;

/**
 * Raja/Cardboard VR sample application.
 */
public class RajaVrDemoActivity extends GvrActivity {

    private static final String TAG = "RajaVrDemoActivity";

    private GvrAudioEngine gvrAudioEngine;
    private Vibrator vibrator;

    /**
     * Sets the view to our GvrView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // "Click" feedback.
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Initialize Gvr
        setContentView(R.layout.common_ui);

        RajaVrView gvrView = (RajaVrView) findViewById(R.id.gvr_view);

        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        gvrView.setRenderer(new DemoVRRenderer(this));
        gvrView.setTransitionViewEnabled(true);
        gvrView.setOnCardboardBackButtonListener(() -> vibrator.vibrate(50));
        setGvrView(gvrView);

        // Initialize 3D audio engine.
        gvrAudioEngine = new GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);

    }

    public void initializeGvrView() {
    }

    @Override
    public void onPause() {
        gvrAudioEngine.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        gvrAudioEngine.resume();
    }

    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        Log.i(TAG, "onCardboardTrigger");

        // Always give user feedback.
        vibrator.vibrate(50);
    }
}
