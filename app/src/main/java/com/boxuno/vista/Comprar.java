package com.boxuno.vista;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.boxuno.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentData;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.IntentSenderRequest;

import org.json.JSONObject;

public class Comprar extends Fragment {

    private EditText calle, portal, ciudad, provincia, cp;
    private CheckBox checkCorreos, checkExpress;
    private RadioButton rbReembolso, rbGooglePay;
    private TextView textPrecioProducto, textPrecioEnvio, textPrecioTotal;
    private Button btnConfirmar;
    private double precioBase = 0.0;
    private PaymentsClient paymentsClient;
    private ActivityResultLauncher<IntentSenderRequest> googlePayLauncher;

    public Comprar() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comprar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        paymentsClient = Wallet.getPaymentsClient(
                requireActivity(),
                new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                        .build()
        );

        googlePayLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        PaymentData paymentData = PaymentData.getFromIntent(result.getData());

                        double envio = checkExpress.isChecked() ? 5.0 : 0.0;
                        double total = precioBase + envio;
                        String metodoEnvio = checkCorreos.isChecked() ? "correos" : checkExpress.isChecked() ? "express" : "";
                        String direccionCompleta = calle.getText().toString() + ", " + portal.getText().toString() + ", " +
                                ciudad.getText().toString() + ", " + provincia.getText().toString() + ", CP: " + cp.getText().toString();

                        procesarCompra("googlepay", total, metodoEnvio, direccionCompleta, requireView());
                    } else {
                        Toast.makeText(requireContext(), "Pago cancelado o fallido", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

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

        int[][] estados = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };

        int[] colores = new int[]{
                Color.parseColor("#0B1B4E"),
                Color.parseColor("#000000")
        };

        ColorStateList colorStateList = new ColorStateList(estados, colores);
        checkCorreos.setButtonTintList(colorStateList);
        checkExpress.setButtonTintList(colorStateList);
        rbReembolso.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#0B1B4E")));
        rbGooglePay.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#0B1B4E")));

        textPrecioProducto = view.findViewById(R.id.textPrecioProducto);
        textPrecioEnvio = view.findViewById(R.id.textPrecioEnvio);
        textPrecioTotal = view.findViewById(R.id.textPrecioTotal);

        btnConfirmar = view.findViewById(R.id.btnConfirmarCompra);

        rbGooglePay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                btnConfirmar.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
                btnConfirmar.setTextColor(Color.WHITE);
                btnConfirmar.setText("Pagar con Google Pay");
                btnConfirmar.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(requireContext(), R.drawable.logogoogle),
                        null, null, null);
                btnConfirmar.setCompoundDrawablePadding(6);
            } else {
                btnConfirmar.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue)));
                btnConfirmar.setTextColor(Color.WHITE);
                btnConfirmar.setText("Confirmar compra");
                btnConfirmar.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }
        });

        if (getArguments() != null) {
            precioBase = getArguments().getDouble("precio", 0.0);
        }

        textPrecioProducto.setText("Precio del producto: " + precioBase + "€");
        textPrecioEnvio.setText("Envío: 0€");
        textPrecioTotal.setText("Total: " + precioBase + "€");

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

        btnConfirmar.setOnClickListener(v -> {
            String direccionCompleta = calle.getText().toString() + ", " + portal.getText().toString() +
                    ", " + ciudad.getText().toString() + ", " + provincia.getText().toString() +
                    ", CP: " + cp.getText().toString();

            if (!checkCorreos.isChecked() && !checkExpress.isChecked()) {
                Toast.makeText(getContext(), "Selecciona un método de envío.", Toast.LENGTH_SHORT).show();
                return;
            }

            String metodoEnvio = checkCorreos.isChecked() ? "correos" : checkExpress.isChecked() ? "express" : "";
            String metodoPago = rbReembolso.isChecked() ? "contrareembolso" : rbGooglePay.isChecked() ? "googlepay" : "";
            double precioFinal = checkExpress.isChecked() ? precioBase + 5.0 : precioBase;

            if (metodoPago.isEmpty()) {
                Toast.makeText(getContext(), "Selecciona un método de pago.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (metodoPago.equals("googlepay")) {
                try {
                    JSONObject paymentDataRequestJson = new JSONObject(
                            com.boxuno.util.GooglePayUtil.getPaymentDataRequest(precioFinal)
                    );
                    PaymentDataRequest request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString());
                    paymentsClient.loadPaymentData(request)
                            .addOnSuccessListener(paymentData -> {
                                double envio = checkExpress.isChecked() ? 5.0 : 0.0;
                                double total = precioBase + envio;
                                String metodoEnvioGoogle = checkCorreos.isChecked() ? "correos" : checkExpress.isChecked() ? "express" : "";
                                String direccionCompletaGoogle = calle.getText().toString() + ", " + portal.getText().toString() + ", " +
                                        ciudad.getText().toString() + ", " + provincia.getText().toString() + ", CP: " + cp.getText().toString();
                                procesarCompra("googlepay", total, metodoEnvioGoogle, direccionCompletaGoogle, v);
                            })
                            .addOnFailureListener(e -> {
                                if (e instanceof com.google.android.gms.common.api.ResolvableApiException) {
                                    try {
                                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(
                                                ((com.google.android.gms.common.api.ResolvableApiException) e)
                                                        .getResolution().getIntentSender()
                                        ).build();
                                        googlePayLauncher.launch(intentSenderRequest);
                                    } catch (Exception ex) {
                                        Toast.makeText(requireContext(), "Error al iniciar Google Pay", Toast.LENGTH_SHORT).show();
                                        ex.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "No se puede iniciar Google Pay", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Error al crear la solicitud de pago", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            procesarCompra(metodoPago, precioFinal, metodoEnvio,
                    direccionCompleta, v);
        });
    }

    private void procesarCompra(String metodoPago, double precioFinal, String metodoEnvio, String direccionCompleta, View view) {
        String idMaqueta = getArguments() != null ? getArguments().getString("id") : null;
        String vendedorId = getArguments() != null ? getArguments().getString("vendedorId") : null;
        String tituloMaqueta = getArguments() != null ? getArguments().getString("titulo") : "Producto";
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (idMaqueta != null) {
            FirebaseFirestore.getInstance().collection("maquetas").document(idMaqueta)
                    .update("vendido", true);

            FirebaseFirestore.getInstance().collection("usuarios").document(vendedorId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String nombreVendedor = documentSnapshot.getString("nombre");

                        Map<String, Object> compra = new HashMap<>();
                        compra.put("productoId", idMaqueta);
                        compra.put("productoNombre", tituloMaqueta);
                        compra.put("productoPrecio", precioFinal);
                        compra.put("fecha", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
                        compra.put("compradorId", uid);
                        compra.put("usuarioNombre", nombreVendedor); // 🔥 Aquí lo guardas

                        FirebaseFirestore.getInstance()
                                .collection("compras")
                                .add(compra);
                    });

        }

        btnConfirmar.setEnabled(false);
        btnConfirmar.setText("Comprado");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Box1DialogEstilo);
        String mensaje = "Dirección:\n" + direccionCompleta +
                "\n\nEnvío: " + metodoEnvio +
                "\nPago: " + metodoPago +
                "\n\nTotal: " + precioFinal + "€";

        builder.setTitle("Compra confirmada");
        builder.setMessage(mensaje);
        builder.setPositiveButton("Aceptar", null); // Se configura después
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button botonAceptar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            botonAceptar.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue));

            botonAceptar.setOnClickListener(v -> {
                if (idMaqueta != null && vendedorId != null) {
                    Map<String, Object> valoracionPendiente = new HashMap<>();
                    valoracionPendiente.put("vendedorId", vendedorId);
                    valoracionPendiente.put("productoId", idMaqueta);
                    valoracionPendiente.put("timestamp", System.currentTimeMillis());

                    FirebaseFirestore.getInstance()
                            .collection("valoracionesPendientes")
                            .document(uid)
                            .set(valoracionPendiente);
                }
                dialog.dismiss();
                Navigation.findNavController(view).navigate(R.id.action_comprar_to_inicio);
            });
        });

        dialog.show();

    }
}
