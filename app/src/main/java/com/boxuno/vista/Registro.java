package com.boxuno.vista;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.boxuno.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Registro extends Fragment {

    public Registro() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String emailRecibido = getArguments().getString("emailNoRegistrado");

        EditText emailEditText = view.findViewById(R.id.emailRegistro);
        emailEditText.setText(emailRecibido);
        EditText nombreUsuario = view.findViewById(R.id.nombreRegistro);

        Button registrarse = view.findViewById(R.id.btn_registro);
        EditText campoContrasenia = view.findViewById(R.id.passwordRegistro);
        EditText campoConfirmacionContrasenia = view.findViewById(R.id.confirmpasswordRegistro);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Box1DialogEstilo);

        registrarse.setOnClickListener(v -> {
            String contrasenia = campoContrasenia.getText().toString().trim();
            String contraseniaConfirmada = campoConfirmacionContrasenia.getText().toString().trim();

            if (contrasenia.isEmpty() || contraseniaConfirmada.isEmpty()) {
                builder.setTitle("Campos vacíos");
                builder.setMessage("Por favor, completa todos los campos antes de continuar.");
                builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0B1B4E"));
            } else if (!contrasenia.equals(contraseniaConfirmada)) {
                builder.setTitle("Error");
                builder.setMessage("Las contraseñas no coinciden.");
                builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0B1B4E"));
            } else {
                NavController navController = NavHostFragment.findNavController(Registro.this);

                String email = emailEditText.getText().toString().trim();
                String nombre = nombreUsuario.getText().toString();

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, contrasenia).addOnCompleteListener(task -> {
                    ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (connectivityManager != null) {
                        Network network = connectivityManager.getActiveNetwork();
                        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

                        if (capabilities == null ||
                                (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                                        !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
                                        !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) {

                            Toast.makeText(getContext(), "No tienes conexión a internet.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnSuccessListener(aVoid -> {
                                        subirFotoPerfilPorDefecto(user.getUid(), new OnFotoSubidaCallback() {
                                            @Override
                                            public void onSubidaCorrecta(String urlFoto) {
                                                Map<String, Object> usuario = new HashMap<>();
                                                usuario.put("uid", user.getUid());
                                                usuario.put("email", user.getEmail());
                                                usuario.put("nombre", nombre);
                                                usuario.put("fotoPerfilUrl", urlFoto);
                                                usuario.put("timestamp", FieldValue.serverTimestamp());
                                                usuario.put("favoritos", new ArrayList<>());
                                                usuario.put("productos", new ArrayList<>());

                                                FirebaseFirestore.getInstance()
                                                        .collection("usuarios")
                                                        .document(user.getUid())
                                                        .set(usuario)
                                                        .addOnSuccessListener(unused -> {
                                                            AlertDialog verifDialog = new AlertDialog.Builder(getContext(), R.style.Box1DialogEstilo)
                                                                    .setTitle("Verificación de correo")
                                                                    .setMessage("Se ha enviado un correo de verificación a " + user.getEmail() + ". Por favor, verifica tu correo antes de iniciar sesión.")
                                                                    .setPositiveButton("Aceptar", (dialog, which) -> {
                                                                        FirebaseAuth.getInstance().signOut();
                                                                        NavOptions navOptions = new NavOptions.Builder()
                                                                                .setPopUpTo(R.id.registro, true)
                                                                                .build();
                                                                        navController.navigate(R.id.login, null, navOptions);
                                                                    })
                                                                    .setCancelable(false)
                                                                    .create();

                                                            verifDialog.show();
                                                            verifDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0B1B4E"));
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(getContext(), "Error al guardar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            }

                                            @Override
                                            public void onError(String error) {
                                                Toast.makeText(getContext(), "Error al subir imagen: " + error, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Error al enviar verificación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomnavigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.INVISIBLE);
        }
    }

    private void subirFotoPerfilPorDefecto(String userId, OnFotoSubidaCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child("fotos_perfil/" + userId + ".jpg");

        InputStream stream = getResources().openRawResource(R.raw.imagenpordefecto);

        UploadTask uploadTask = ref.putStream(stream);
        uploadTask
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> callback.onSubidaCorrecta(uri.toString()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    interface OnFotoSubidaCallback {
        void onSubidaCorrecta(String url);
        void onError(String error);
    }
}
