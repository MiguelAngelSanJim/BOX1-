package com.boxuno.vista;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.boxuno.R;
import com.google.firebase.auth.FirebaseAuth;


public class RecuperarContrasenia extends Fragment {

    public RecuperarContrasenia() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recuperar_contrasenia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button buttonEnviarCorreo = view.findViewById(R.id.buttonEnviarCorreo);
        EditText editTextCorreo = view.findViewById(R.id.editTextCorreo);

        buttonEnviarCorreo.setOnClickListener(v -> {
            String correo = editTextCorreo.getText().toString().trim();
            if (correo.isEmpty()) {
                editTextCorreo.setError("Introduce tu correo");
                return;
            }

            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.sendPasswordResetEmail(correo)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Correo de recuperación enviado", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).popBackStack();
                        } else {
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }
}