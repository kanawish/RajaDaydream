package com.kanawish.raja;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kanawish.raja.rajademo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DemoFragment fragment = DemoFragment.buildInstance();
        String name = DemoFragment.class.getName();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment, name)
                .commit();

    }
}
