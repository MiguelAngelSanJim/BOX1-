package com.boxuno.vista;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.boxuno.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottombar = findViewById(R.id.bottomnavigation);

        // Configurar el comportamiento de los ítems de la barra de navegación inferior
        bottombar.setOnItemSelectedListener(item -> {
            int idItemn = item.getItemId();
            if (idItemn == R.id.inicio) {
                loadFragment(new Inicio());
                return false;
            } else if (idItemn == R.id.favoritos) {
                loadFragment(new Favoritos());
                return false;
            } else if (idItemn == R.id.detalleProducto) {
                loadFragment(new DetalleProducto());
                return false;
            } else if (idItemn == R.id.chatLista) {
                loadFragment(new ChatLista());
                return false;
            } else {
                loadFragment(new Perfil());
                return false;
            }
        });
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }
}