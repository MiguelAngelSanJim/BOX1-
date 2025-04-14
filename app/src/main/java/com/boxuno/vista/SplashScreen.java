package com.boxuno.vista;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.boxuno.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.splash_screen);
        splashscreenstart();
    }

    public void splashscreenstart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Iniciar el activity después del retraso.
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                finish();
            }
        }, 3500);
    }
}