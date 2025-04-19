package com.boxuno.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.boxuno.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {
    private boolean irAlInicio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.splash_screen);
        recuerdame();
        splashscreenstart();

    }

    public void recuerdame() {
        SharedPreferences prefs = getSharedPreferences("box1_prefs", MODE_PRIVATE);
        boolean recordar = prefs.getBoolean("recordar", false);
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (recordar && usuario != null) {
            irAlInicio = true;
        }
    }

    public void splashscreenstart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Iniciar el activity después del retraso.
                if (!irAlInicio) {
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(SplashScreen.this, Inicio.class));
                    finish();
                }
            }
        }, 3500);
    }
}