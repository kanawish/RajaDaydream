package com.kanawish.tangotalk.rajademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*
        BasicFragment fragment = BasicFragment.buildInstance();
        String name = BasicFragment.class.getName();
*/
        DemoFragment fragment = DemoFragment.buildInstance();
        String name = DemoFragment.class.getName();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment, name)
                .commit();

        /*
            Fragment fragment = (Fragment) aClass.newInstance();
            fragment.setArguments(bundle);

        */
    }
}
