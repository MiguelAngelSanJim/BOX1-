package com.boxuno.vista;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private EditText editTextCalle, editTextPortal, editTextCiudad, editTextProvincia, editTextCodigoPostal;
    private Button btnGuardar, btnEditar;
    private FirebaseFirestore db;
    private String userId;

    public Direccion() {
    }

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
        btnEditar = view.findViewById(R.id.btnEditarDireccion);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        cargarDireccion();

        btnGuardar.setOnClickListener(v -> guardarDireccion());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setDireccionEditable(false); // Bloquea los campos al inicio.

        btnEditar.setOnClickListener(v -> {
            setDireccionEditable(true);
            btnEditar.setVisibility(View.GONE);
            btnGuardar.setVisibility(View.VISIBLE);
        });
    }

    private void setDireccionEditable(boolean editable) {
        editTextCalle.setEnabled(editable);
        editTextPortal.setEnabled(editable);
        editTextCiudad.setEnabled(editable);
        editTextProvincia.setEnabled(editable);
        editTextCodigoPostal.setEnabled(editable);
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

                editTextCalle.setTextColor(Color.BLACK);
                editTextPortal.setTextColor(Color.BLACK);
                editTextCiudad.setTextColor(Color.BLACK);
                editTextProvincia.setTextColor(Color.BLACK);
                editTextCodigoPostal.setTextColor(Color.BLACK);

                btnEditar.setVisibility(View.VISIBLE);
                btnGuardar.setVisibility(View.GONE);

            } else {
                btnEditar.setVisibility(View.VISIBLE);
                btnGuardar.setVisibility(View.GONE);
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
                    setDireccionEditable(false);
                    btnGuardar.setVisibility(View.GONE);
                    btnEditar.setVisibility(View.VISIBLE);

                    Toast.makeText(getContext(), "Dirección guardada correctamente", Toast.LENGTH_SHORT).show();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        NavHostFragment.findNavController(Direccion.this).navigate(R.id.perfil);
                    }, 2500);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al guardar la dirección", Toast.LENGTH_SHORT).show()
                );
    }
}
