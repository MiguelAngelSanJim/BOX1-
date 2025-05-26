package com.boxuno.vista;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.boxuno.R;
import com.boxuno.adapter.ImagenCarruselAdapter;
import com.boxuno.adapter.MaquetaAdapter;
import com.boxuno.modelo.Maqueta;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetalleProducto extends Fragment {

    private Maqueta maqueta;
    private ViewPager2 viewPagerImagenes;
    private ImageView imagenPerfilUser;
    private TextView tituloDetalleProducto, precioDetalleProducto, descripcionDetalleProducto, subidoPor, textoEscala, textoEstado;
    private Button btnComprar, btnMandarMensaje;
    private RatingBar ratingBarUsuario;

    public DetalleProducto() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_producto, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPagerImagenes = view.findViewById(R.id.imagenesViewPager);
        imagenPerfilUser = view.findViewById(R.id.imagenPerfilUser);
        tituloDetalleProducto = view.findViewById(R.id.tituloDetalleProducto);
        precioDetalleProducto = view.findViewById(R.id.precioDetalleProducto);
        textoEscala = view.findViewById(R.id.textoEscala);
        textoEstado = view.findViewById(R.id.textoEstado);
        descripcionDetalleProducto = view.findViewById(R.id.descripcionDetalleProducto);
        subidoPor = view.findViewById(R.id.subidoPor);
        ratingBarUsuario = view.findViewById(R.id.ratingBarUsuario);
        btnComprar = view.findViewById(R.id.btn_comprar);
        btnMandarMensaje = view.findViewById(R.id.btn_mandarMensaje);

        // Recuperar la maqueta desde el bundle.
        if (getArguments() != null && getArguments().containsKey("maqueta")) {
            maqueta = (Maqueta) getArguments().getSerializable("maqueta");

            if (maqueta != null) {
                tituloDetalleProducto.setText(maqueta.getTitulo());
                precioDetalleProducto.setText(maqueta.getPrecio() + " €");
                textoEscala.setText("Escala: "+maqueta.getEscala());
                textoEstado.setText("Estado: "+maqueta.getEstado());
                descripcionDetalleProducto.setText(maqueta.getDescripcion());

                // Cargar nombre e imagen de perfil del usuario.
                FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(maqueta.getUsuarioId())
                        .get()
                        .addOnSuccessListener(doc -> {
                            String nombre = doc.getString("nombre");
                            subidoPor.setText("Subido por " + (nombre != null ? nombre : "Desconocido"));

                            String urlFoto = doc.getString("fotoPerfilUrl");
                            if (urlFoto != null && !urlFoto.isEmpty()) {
                                Glide.with(requireContext())
                                        .load(urlFoto)
                                        .circleCrop()
                                        .into(imagenPerfilUser);
                            } else {
                                Glide.with(requireContext())
                                        .load(R.drawable.imagenpordefecto)
                                        .circleCrop()
                                        .into(imagenPerfilUser);
                            }
                            // Obtener la valoración media del vendedor desde Firebase
                            FirebaseFirestore.getInstance()
                                    .collection("valoraciones")
                                    .document(maqueta.getUsuarioId())
                                    .collection("usuarios")
                                    .get()
                                    .addOnSuccessListener(snapshot -> {
                                        double suma = 0;
                                        int total = 0;

                                        for (DocumentSnapshot docVal : snapshot.getDocuments()) {
                                            Double puntuacion = docVal.getDouble("puntuacion");
                                            if (puntuacion != null) {
                                                suma += puntuacion;
                                                total++;
                                            }
                                        }

                                        if (total > 0) {
                                            float media = (float) (suma / total);
                                            ratingBarUsuario.setRating(media);
                                            ratingBarUsuario.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#0B1B4E")));
                                            ratingBarUsuario.setSecondaryProgressTintList(ColorStateList.valueOf(Color.parseColor("#0B1B4E")));
                                            ratingBarUsuario.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BBDEFB")));
                                        } else {
                                            ratingBarUsuario.setRating(0);
                                        }

                                    })
                                    .addOnFailureListener(e -> {
                                        ratingBarUsuario.setRating(0);
                                    });


                        })
                        .addOnFailureListener(e -> {
                            subidoPor.setText("Subido por Desconocido");
                            Glide.with(requireContext())
                                    .load(R.drawable.imagenpordefecto)
                                    .circleCrop()
                                    .into(imagenPerfilUser);
                        });

                // Cargar carrusel de imágenes.
                if (maqueta.getImagenes() != null && !maqueta.getImagenes().isEmpty()) {
                    ImagenCarruselAdapter carruselAdapter = new ImagenCarruselAdapter(getContext(), new ArrayList<>(maqueta.getImagenes()));
                    viewPagerImagenes.setAdapter(carruselAdapter);

                }
                cargarSimilares(view);

                if (maqueta.isVendido()) {
                    btnComprar.setEnabled(false);
                    btnComprar.setText("Vendido");
                    btnComprar.setBackgroundTintList(getResources().getColorStateList(R.color.gray, null)); // Opcional
                }

            }
        }

        btnComprar.setOnClickListener(v -> {
            if (maqueta != null) {
                Bundle bundle = new Bundle();
                bundle.putDouble("precio", maqueta.getPrecio());
                bundle.putString("id", maqueta.getId());
                bundle.putString("vendedorId", maqueta.getUsuarioId());
                bundle.putString("titulo", maqueta.getTitulo());

                Navigation.findNavController(v).navigate(R.id.action_detalle_to_comprar, bundle);
            }
        });


        btnMandarMensaje.setOnClickListener(v -> {
            if (maqueta == null) return;

            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(maqueta.getUsuarioId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        String nombre = doc.getString("nombre");
                        String fotoUrl = doc.getString("fotoPerfilUrl");

                        Bundle bundle = new Bundle();
                        bundle.putString("uidDestino", maqueta.getUsuarioId());
                        bundle.putString("nombreDestino", nombre != null ? nombre : "Usuario");
                        bundle.putString("fotoDestino", fotoUrl != null ? fotoUrl : "");

                        bundle.putString("productoId", maqueta.getId());
                        bundle.putString("productoTitulo", maqueta.getTitulo());
                        bundle.putDouble("productoPrecio", maqueta.getPrecio());


                        NavHostFragment.findNavController(DetalleProducto.this).navigate(R.id.chatConversacion, bundle);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "No se pudo iniciar el chat", Toast.LENGTH_SHORT).show();
                    });
        });

        LinearLayout layoutDenunciar = view.findViewById(R.id.layoutDenunciar);
        layoutDenunciar.setOnClickListener(v -> mostrarDialogoDenuncia());

        imagenPerfilUser.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("usuarioId", maqueta.getUsuarioId());
            NavHostFragment.findNavController(DetalleProducto.this)
                    .navigate(R.id.visitarPerfil, bundle); // cambia el ID al correcto
        });


        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email != null && email.equals("box1coleccion@gmail.com")) {
            btnComprar.setVisibility(View.GONE);
            btnMandarMensaje.setVisibility(View.GONE);
        }


    }

    private void cargarSimilares(View view) {
        RecyclerView recyclerSimilares = view.findViewById(R.id.recycler_similares);
        recyclerSimilares.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        List<Maqueta> similaresList = new ArrayList<>();
        MaquetaAdapter similaresAdapter = new MaquetaAdapter(similaresList, getContext(), maqueta -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("maqueta", maqueta);
            NavHostFragment.findNavController(DetalleProducto.this).navigate(R.id.detalleProducto, bundle);
        }, false, false);
        recyclerSimilares.setAdapter(similaresAdapter);
        String uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("maquetas")
                .whereEqualTo("categoria", maqueta.getCategoria())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Maqueta similar = doc.toObject(Maqueta.class);

                        if (similar == null) continue;

                        if (similar.getId() != null &&
                                similar.getUsuarioId() != null &&
                                !similar.getId().equals(maqueta.getId()) &&
                                !similar.getUsuarioId().equals(uidActual)) {
                            similaresList.add(similar);
                        }
                    }

                    similaresAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("SIMILARES", "ERROR: Error al cargar maquetas similares", e));
    }

    private void mostrarDialogoDenuncia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Box1DialogEstilo);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_denuncia, null);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        RadioGroup radioGroup = view.findViewById(R.id.radioGroupMotivos);
        EditText otrosTexto = view.findViewById(R.id.editTextOtros);
        Button btnEnviar = view.findViewById(R.id.btnEnviarDenuncia);

        // Forzar color azul al círculo seleccionado
        ColorStateList azul = ColorStateList.valueOf(Color.parseColor("#0B1B4E"));
        RadioButton radioPrecio = view.findViewById(R.id.radioPrecio);
        RadioButton radioNoRelacionado = view.findViewById(R.id.radioNoRelacionado);
        RadioButton radioOtros = view.findViewById(R.id.radioOtros);
        radioPrecio.setButtonTintList(azul);
        radioNoRelacionado.setButtonTintList(azul);
        radioOtros.setButtonTintList(azul);

        otrosTexto.setVisibility(View.GONE); // Ocultar hasta que seleccione "Otros".

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioOtros) {
                otrosTexto.setVisibility(View.VISIBLE);
            } else {
                otrosTexto.setVisibility(View.GONE);
            }
        });

        btnEnviar.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            String motivo = "";
            if (selectedId == R.id.radioPrecio) motivo = "Precio especulativo";
            else if (selectedId == R.id.radioNoRelacionado)
                motivo = "El producto no está relacionado";
            else if (selectedId == R.id.radioOtros)
                motivo = "Otros: " + otrosTexto.getText().toString().trim();

            if (!motivo.isEmpty()) {
                enviarDenuncia(motivo);
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "Selecciona un motivo", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }


    private void enviarDenuncia(String motivo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String productoId = maqueta.getId();
        String usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> denuncia = new HashMap<>();
        denuncia.put("productoId", productoId);
        denuncia.put("usuarioId", usuarioId);
        denuncia.put("motivo", motivo);
        denuncia.put("timestamp", FieldValue.serverTimestamp());

        db.collection("denuncias")
                .add(denuncia)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(requireContext(), "Denuncia enviada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al enviar la denuncia", Toast.LENGTH_SHORT).show();
                });
    }

}
