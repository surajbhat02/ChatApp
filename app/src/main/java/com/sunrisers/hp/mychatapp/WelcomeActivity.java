package com.sunrisers.hp.mychatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Thread thread=new Thread(){
            @Override
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    Intent mainIntent =new Intent(WelcomeActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();

                }
            }
        };
        thread.start();
    }
    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }
}
