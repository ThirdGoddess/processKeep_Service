package com.keep.process.process_service;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置前台Service，提升App进程优先级
        startService(new Intent(this,ForegroundService.class));
    }
}
