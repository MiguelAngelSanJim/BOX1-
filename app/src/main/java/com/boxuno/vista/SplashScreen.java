package com.boxuno.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.boxuno.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.splash_screen);

        // Espera 3,5 segundos antes de comprobar si debe ir a Inicio o Login
        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            boolean recordar = prefs.getBoolean("recordar", false);
            FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            intent.putExtra("irAlInicio", recordar && usuario != null);
            startActivity(intent);
            finish();
        }, 3500);
    }
}