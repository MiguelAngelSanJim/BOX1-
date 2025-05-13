package com.boxuno.vista;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DetalleProducto extends Fragment {

    private Maqueta maqueta;
    private ViewPager2 viewPagerImagenes;
    private ImageView imagenPerfilUser;
    private TextView tituloDetalleProducto, precioDetalleProducto, descripcionDetalleProducto, subidoPor;
    private Button btnComprar, btnMandarMensaje;

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
        descripcionDetalleProducto = view.findViewById(R.id.descripcionDetalleProducto);
        subidoPor = view.findViewById(R.id.subidoPor);
        btnComprar = view.findViewById(R.id.btn_comprar);
        btnMandarMensaje = view.findViewById(R.id.btn_mandarMensaje);

        // Recuperar la maqueta desde el bundle.
        if (getArguments() != null && getArguments().containsKey("maqueta")) {
            maqueta = (Maqueta) getArguments().getSerializable("maqueta");

            if (maqueta != null) {
                tituloDetalleProducto.setText(maqueta.getTitulo());
                precioDetalleProducto.setText(maqueta.getPrecio() + " €");
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
                        })
                        .addOnFailureListener(e -> {
                            subidoPor.setText("Subido por Desconocido");
                            Glide.with(requireContext())
                                    .load(R.drawable.imagenpordefecto)
                                    .circleCrop()
                                    .into(imagenPerfilUser);
                        });

                // Cargar carrusel de imágenes
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
}
