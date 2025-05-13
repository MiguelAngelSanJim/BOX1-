package com.boxuno.vista;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.boxuno.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;


public class Comprar extends Fragment {

    private EditText calle, portal, ciudad, provincia, cp;
    private CheckBox checkCorreos, checkExpress;
    private RadioButton rbReembolso, rbGooglePay;
    private TextView textPrecioProducto, textPrecioEnvio, textPrecioTotal;
    private Button btnConfirmar;
    private double precioBase = 0.0;

    public Comprar() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comprar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        // --- Referencias UI ---
        calle = view.findViewById(R.id.editTextCalle);
        portal = view.findViewById(R.id.editTextPortal);
        ciudad = view.findViewById(R.id.editTextCiudad);
        provincia = view.findViewById(R.id.editTextProvincia);
        cp = view.findViewById(R.id.editTextCodigoPostal);

        calle.setTextColor(Color.BLACK);
        portal.setTextColor(Color.BLACK);
        ciudad.setTextColor(Color.BLACK);
        provincia.setTextColor(Color.BLACK);
        cp.setTextColor(Color.BLACK);

        checkCorreos = view.findViewById(R.id.checkboxCorreos);
        checkExpress = view.findViewById(R.id.checkboxExpress);

        rbReembolso = view.findViewById(R.id.radioContraReembolso);
        rbGooglePay = view.findViewById(R.id.radioGooglePay);

        textPrecioProducto = view.findViewById(R.id.textPrecioProducto);
        textPrecioEnvio = view.findViewById(R.id.textPrecioEnvio);
        textPrecioTotal = view.findViewById(R.id.textPrecioTotal);

        btnConfirmar = view.findViewById(R.id.btnConfirmarCompra);

        // --- Precio recibido por argumentos ---
        if (getArguments() != null) {
            precioBase = getArguments().getDouble("precio", 0.0);
        }

        textPrecioProducto.setText("Precio del producto: " + precioBase + "€");
        textPrecioEnvio.setText("Envío: 0€");
        textPrecioTotal.setText("Total: " + precioBase + "€");

        // --- Cargar dirección si existe ---
        db.collection("usuarios").document(uid).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                Map<String, Object> direccion = (Map<String, Object>) document.get("direccion");

                if (direccion != null) {
                    calle.setText((String) direccion.get("calle"));
                    portal.setText((String) direccion.get("portal"));
                    ciudad.setText((String) direccion.get("ciudad"));
                    provincia.setText((String) direccion.get("provincia"));
                    cp.setText((String) direccion.get("codigoPostal"));
                }
            }
        });

        // --- Lógica de selección única para envío ---
        CompoundButton.OnCheckedChangeListener envioChangeListener = (buttonView, isChecked) -> {
            if (isChecked) {
                if (buttonView.getId() == R.id.checkboxCorreos) checkExpress.setChecked(false);
                if (buttonView.getId() == R.id.checkboxExpress) checkCorreos.setChecked(false);

                double envioPrecio = checkExpress.isChecked() ? 5.0 : 0.0;
                textPrecioEnvio.setText("Envío: " + envioPrecio + "€");
                textPrecioTotal.setText("Total: " + (precioBase + envioPrecio) + "€");
            }
        };

        checkCorreos.setOnCheckedChangeListener(envioChangeListener);
        checkExpress.setOnCheckedChangeListener(envioChangeListener);

        // --- Confirmar compra ---
        btnConfirmar.setOnClickListener(v -> {
            String direccionCompleta = calle.getText().toString() + ", " + portal.getText().toString() +
                    ", " + ciudad.getText().toString() + ", " + provincia.getText().toString() +
                    ", CP: " + cp.getText().toString();

            String metodoEnvio = checkCorreos.isChecked() ? "correos" : checkExpress.isChecked() ? "express" : "";
            String metodoPago = rbReembolso.isChecked() ? "contrareembolso" : rbGooglePay.isChecked() ? "googlepay" : "";

            if (metodoEnvio.isEmpty() || metodoPago.isEmpty()) {
                Toast.makeText(getContext(), "Selecciona método de envío y pago.", Toast.LENGTH_SHORT).show();
                return;
            }

            double precioFinal = checkExpress.isChecked() ? precioBase + 5.0 : precioBase;

            String idMaqueta = getArguments() != null ? getArguments().getString("id") : null;
            Log.d("Debug", "ID recibido: " + idMaqueta);

            if (idMaqueta != null) {
                FirebaseFirestore.getInstance().collection("maquetas").document(idMaqueta)
                        .update("vendido", true)
                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Marcado como vendido"))
                        .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar", e));
            }


            btnConfirmar.setEnabled(false);
            btnConfirmar.setText("Comprado");


            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Compra confirmada");

            String mensaje = "Dirección:\n" + direccionCompleta +
                    "\n\nEnvío: " + metodoEnvio +
                    "\nPago: " + metodoPago +
                    "\n\nTotal: " + precioFinal + "€";

            builder.setMessage(mensaje);

            builder.setPositiveButton("Aceptar", (dialog, which) -> {
                // Opcional: volver al inicio o cerrar fragmento
                Navigation.findNavController(v).navigate(R.id.action_comprar_to_inicio);
            });

            builder.setCancelable(false);
            builder.show();
        });
    }
}
