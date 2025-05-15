package com.boxuno.vista;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Login extends Fragment {

    public Login() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                    FirebaseFirestore.getInstance().collection("usuarios")
                            .document(user.getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (!documentSnapshot.exists()) {
                                    Map<String, Object> usuario = new HashMap<>();
                                    usuario.put("uid", user.getUid());
                                    usuario.put("email", user.getEmail());
                                    usuario.put("nombre", "");
                                    usuario.put("fotoPerfilUrl", "");
                                    usuario.put("timestamp", FieldValue.serverTimestamp());
                                    usuario.put("favoritos", new ArrayList<>());
                                    usuario.put("productos", new ArrayList<>());

                                    FirebaseFirestore.getInstance().collection("usuarios")
                                            .document(user.getUid())
                                            .set(usuario)
                                            .addOnSuccessListener(unused -> {
                                                continuarLogin(checkBox.isChecked());
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Error al guardar datos del usuario.", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    continuarLogin(checkBox.isChecked());
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "Contraseña incorrecta o usuario inexistente.", Toast.LENGTH_SHORT).show();
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
    }

    private void continuarLogin(boolean recordar) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("recordar", recordar).apply();

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.login, true)
                .build();

        NavHostFragment.findNavController(Login.this).navigate(R.id.action_login_to_inicio, null, navOptions);
    }

}
