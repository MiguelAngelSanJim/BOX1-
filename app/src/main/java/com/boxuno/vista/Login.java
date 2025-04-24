package com.boxuno.vista;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.boxuno.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Login#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Login extends Fragment {

    public Login() {
        // Required empty public constructor
    }

    public static Login newInstance() {
        Login fragment = new Login();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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

            if (email.isEmpty() && password.isEmpty()) {
                Toast.makeText(getContext(), "Email o contraseña no pueden estar en blanco.", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseFirestore.getInstance().collection("usuarios").whereEqualTo("email", email).get().addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Bundle bundle = new Bundle();
                        bundle.putString("emailNoRegistrado", email);
                        NavHostFragment.findNavController(Login.this).navigate(R.id.action_login_to_registro, bundle);
                    } else {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                NavOptions navOptions = new NavOptions.Builder()
                                        .setPopUpTo(R.id.login, true) // Elimina 'login' del backstack
                                        .build();
                                SharedPreferences prefs = requireActivity().getSharedPreferences("box1_prefs", Context.MODE_PRIVATE);
                                prefs.edit().putBoolean("recordar", checkBox.isChecked()).apply();

                                NavHostFragment.findNavController(Login.this).navigate(R.id.action_login_to_inicio, null, navOptions);
                            } else {
                                Toast.makeText(getContext(), "Contraseña incorrecta.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

        botonRegistro.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_login_to_registro);
        });


        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomnavigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.INVISIBLE);
        }
    }
}