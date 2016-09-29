package com.kanawish.raja;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class DemoFragment extends BaselineRenderingFragment {

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
        // NOTE: If you build your own renderer, simply switch them in here.
        return new SimpleRenderer(getActivity(), this);
    }

    public static DemoFragment buildInstance() {
        return new DemoFragment();
    }
}