package com.boxuno.vista;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.boxuno.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class Login extends Fragment {

    public Login() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button botonInicioSesion = view.findViewById(R.id.btn_inicioSesion);
        Button botonRegistro = view.findViewById(R.id.btn_registro);
        EditText campoEmail = view.findViewById(R.id.email);
        EditText contrasenia = view.findViewById(R.id.password);
        CheckBox checkBox = view.findViewById(R.id.checkBox);
        int[][] estados = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };

        int[] colores = new int[]{
                Color.parseColor("#0B1B4E"),
                Color.parseColor("#FFFFFF")
        };

        ColorStateList colorStateList = new ColorStateList(estados, colores);
        checkBox.setButtonTintList(colorStateList);

        botonInicioSesion.setOnClickListener(v -> {
            String email = campoEmail.getText().toString().trim();
            String password = contrasenia.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Email o contraseña no pueden estar en blanco.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user != null && !user.isEmailVerified()) {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(getContext(), "Debes verificar tu correo electrónico antes de iniciar sesión.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseFirestore.getInstance().collection("usuarios")
                            .document(user.getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    SharedPreferences prefs = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                                    prefs.edit().putBoolean("recordar", checkBox.isChecked()).apply();

                                    // 🔽 Guardar fcmToken
                                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String token = tokenTask.getResult();
                                            FirebaseFirestore.getInstance().collection("usuarios")
                                                    .document(user.getUid())
                                                    .update("fcmToken", token)
                                                    .addOnSuccessListener(aVoid -> {
                                                        NavOptions navOptions = new NavOptions.Builder()
                                                                .setPopUpTo(R.id.login, true)
                                                                .build();

                                                        NavHostFragment.findNavController(Login.this)
                                                                .navigate(R.id.inicio, null, navOptions);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(getContext(), "Error al guardar el token FCM.", Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            Toast.makeText(getContext(), "No se pudo obtener el token FCM.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } else {
                                    FirebaseAuth.getInstance().signOut();
                                    Toast.makeText(getContext(), "Tu perfil no está configurado. Vuelve a registrarte.", Toast.LENGTH_LONG).show();
                                }
                            });

                } else {
                    Toast.makeText(getContext(), "Email o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        botonRegistro.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_login_to_registro);
        });

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomnavigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.INVISIBLE);
        }

        TextView textViewOlvideContrasena = view.findViewById(R.id.editTextRecuperarContrasenia);
        textViewOlvideContrasena.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_login_to_recuperar_contrasenia);
        });

        // ✅ Pedir permiso de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}
