package com.boxuno.vista;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Registro#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Registro extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Registro() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Registro.
     */
    // TODO: Rename and change types and number of parameters
    public static Registro newInstance(String param1, String param2) {
        Registro fragment = new Registro();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registro, container, false);
    }

    private void guardarUsuarioEnFirestore(FirebaseUser user, String nombreDesdeEditText) {
        if (user == null) return;

        String uid = user.getUid();
        String email = user.getEmail();
        subirFotoPerfilPorDefecto(uid, new OnFotoSubidaCallback() {
            @Override
            public void onSubidaCorrecta(String urlFoto) {
                Map<String, Object> usuario = new HashMap<>();
                usuario.put("uid", uid);
                usuario.put("email", email);
                usuario.put("nombre", nombreDesdeEditText); // ← pásalo desde el EditText
                usuario.put("fotoPerfilUrl", urlFoto); // o súbela y pon la URL
                usuario.put("timestamp", FieldValue.serverTimestamp());
                usuario.put("favoritos", new ArrayList<>());
                usuario.put("productos", new ArrayList<>());

                FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(uid)
                        .set(usuario)
                        .addOnSuccessListener(aVoid -> Log.d("FIRESTORE", "Usuario guardado correctamente"))
                        .addOnFailureListener(e -> Log.e("FIRESTORE", "Error al guardar usuario", e));
            }

            @Override
            public void onError(String error) {
                Log.e("FOTO PERFIL", "Error al subir imagen: " + error);
            }
        });

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


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        registrarse.setOnClickListener(v -> {
            String contrasenia = campoContrasenia.getText().toString().trim();
            String contraseniaConfirmada = campoConfirmacionContrasenia.getText().toString().trim();

            if (contrasenia.isEmpty() || contraseniaConfirmada.isEmpty()) {
                builder.setTitle("Campos vacíos");
                builder.setMessage("Por favor, completa todos los campos antes de continuar.");
                builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                builder.show();
            } else if (!contrasenia.equals(contraseniaConfirmada)) {
                builder.setTitle("Error");
                builder.setMessage("Las contraseñas no coinciden.");
                builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                builder.show();
            } else {
                NavController navController = NavHostFragment.findNavController(Registro.this);

                String email = emailEditText.getText().toString().trim();
                String nombre = nombreUsuario.getText().toString();
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, contrasenia).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        NavOptions navOptions = new NavOptions.Builder()
                                .setPopUpTo(R.id.registro, true) // Elimina 'registro' del backstack
                                .build();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        guardarUsuarioEnFirestore(user, nombre);
                        navController.navigate(R.id.action_registro_to_inicio, null, navOptions);
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

        // Convertir drawable a InputStream
        InputStream stream = getResources().openRawResource(R.raw.imagenpordefecto);

        UploadTask uploadTask = ref.putStream(stream);
        uploadTask
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    callback.onSubidaCorrecta(uri.toString());
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    interface OnFotoSubidaCallback {
        void onSubidaCorrecta(String url);

        void onError(String error);
    }


}