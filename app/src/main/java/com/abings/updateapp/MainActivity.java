package com.abings.updateapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.abings.updateapp.upapp.CheckVersionThread;
import com.abings.updateapp.upapp.Const;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new CheckVersionThread(this, Const.apkDownPath, Const.url_baby).start();
    }
}
