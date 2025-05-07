package com.boxuno.vista;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.boxuno.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Direccion extends Fragment {

    public Direccion() {
        // Required empty public constructor
    }

    private EditText editTextCalle, editTextPortal, editTextCiudad, editTextProvincia, editTextCodigoPostal;
    private Button btnGuardar;
    private FirebaseFirestore db;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_direccion, container, false);

        editTextCalle = view.findViewById(R.id.editTextCalle);
        editTextPortal = view.findViewById(R.id.editTextPortal);
        editTextCiudad = view.findViewById(R.id.editTextCiudad);
        editTextProvincia = view.findViewById(R.id.editTextProvincia);
        editTextCodigoPostal = view.findViewById(R.id.editTextCodigoPostal);
        btnGuardar = view.findViewById(R.id.btnGuardarDireccion);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        cargarDireccion();

        btnGuardar.setOnClickListener(v -> guardarDireccion());

        return view;
    }

    private void cargarDireccion() {
        DocumentReference userRef = db.collection("usuarios").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("direccion")) {
                Map<String, Object> direccion = (Map<String, Object>) documentSnapshot.get("direccion");
                editTextCalle.setText((String) direccion.get("calle"));
                editTextPortal.setText((String) direccion.get("portal"));
                editTextCiudad.setText((String) direccion.get("ciudad"));
                editTextProvincia.setText((String) direccion.get("provincia"));
                editTextCodigoPostal.setText((String) direccion.get("codigoPostal"));
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Error al cargar dirección", Toast.LENGTH_SHORT).show()
        );
    }

    private void guardarDireccion() {
        String calle = editTextCalle.getText().toString().trim();
        String portal = editTextPortal.getText().toString().trim();
        String ciudad = editTextCiudad.getText().toString().trim();
        String provincia = editTextProvincia.getText().toString().trim();
        String codigoPostal = editTextCodigoPostal.getText().toString().trim();

        if (calle.isEmpty() || ciudad.isEmpty() || provincia.isEmpty() || codigoPostal.isEmpty()) {
            Toast.makeText(getContext(), "Rellena todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> direccionMap = new HashMap<>();
        direccionMap.put("calle", calle);
        direccionMap.put("portal", portal);
        direccionMap.put("ciudad", ciudad);
        direccionMap.put("provincia", provincia);
        direccionMap.put("codigoPostal", codigoPostal);

        DocumentReference userRef = db.collection("usuarios").document(userId);
        userRef.update("direccion", direccionMap)
                .addOnSuccessListener(aVoid -> {
                            btnGuardar.setClickable(false);
                            Toast.makeText(getContext(), "Dirección guardada correctamente", Toast.LENGTH_SHORT).show();
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                NavHostFragment.findNavController(Direccion.this).navigate(R.id.perfil);
                            }, 2500);
                        }
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al guardar la dirección", Toast.LENGTH_SHORT).show()
                );
    }


}