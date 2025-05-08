package com.boxuno.vista;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.boxuno.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditarPerfil extends Fragment {

    private EditText editTextNombre, editTextTelefono;
    private TextView textViewEmail;
    private Button btnEditar, btnGuardar, btnModificarEmail;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    public EditarPerfil() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editar_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextNombre = view.findViewById(R.id.editTextNombre);
        editTextTelefono = view.findViewById(R.id.editTextTelefono);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        btnEditar = view.findViewById(R.id.btnEditarPerfil);
        btnGuardar = view.findViewById(R.id.btnGuardarPerfil);
        btnModificarEmail = view.findViewById(R.id.btnModificarEmail);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        userId = user.getUid();
        textViewEmail.setText(user.getEmail());

        setCamposEditables(false);
        cargarDatosUsuario();

        btnEditar.setOnClickListener(v -> {
            setCamposEditables(true);
            btnEditar.setVisibility(View.GONE);
            btnGuardar.setVisibility(View.VISIBLE);
        });

        btnGuardar.setOnClickListener(v -> guardarDatos());

        btnModificarEmail.setOnClickListener(v -> mostrarDialogoModificarEmail());

        editTextNombre.setTextColor(Color.BLACK);
        editTextTelefono.setTextColor(Color.BLACK);

        btnGuardar.setVisibility(View.GONE);
        btnEditar.setVisibility(View.VISIBLE);

    }

    private void setCamposEditables(boolean enabled) {
        editTextNombre.setEnabled(enabled);
        editTextTelefono.setEnabled(enabled);
    }

    private void cargarDatosUsuario() {
        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String telefono = documentSnapshot.getString("telefono");

                        if (nombre != null) editTextNombre.setText(nombre);
                        if (telefono != null) editTextTelefono.setText(telefono);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
                );
    }

    private void guardarDatos() {
        String nuevoNombre = editTextNombre.getText().toString().trim();
        String nuevoTelefono = editTextTelefono.getText().toString().trim();

        if (nuevoNombre.isEmpty()) {
            Toast.makeText(getContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> actualizacion = new HashMap<>();
        actualizacion.put("nombre", nuevoNombre);
        actualizacion.put("telefono", nuevoTelefono);

        db.collection("usuarios").document(userId).update(actualizacion)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    setCamposEditables(false);
                    btnGuardar.setVisibility(View.GONE);
                    btnEditar.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                );
    }

    private void mostrarDialogoModificarEmail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Modificar correo electrónico");

        final EditText input = new EditText(requireContext());
        input.setHint("Nuevo correo");
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Actualizar", (dialog, which) -> {
            String nuevoEmail = input.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(nuevoEmail).matches()) {
                Toast.makeText(getContext(), "Correo no válido", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.updateEmail(nuevoEmail)
                        .addOnSuccessListener(aVoid -> {
                            user.sendEmailVerification();
                            textViewEmail.setText(nuevoEmail);
                            Toast.makeText(getContext(), "Correo actualizado. Verifica el nuevo email.", Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error al actualizar el correo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
