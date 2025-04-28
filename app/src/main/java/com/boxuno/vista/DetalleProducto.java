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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boxuno.R;
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
    private ImageView imagenProducto, imagenPerfilUser;
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

        imagenProducto = view.findViewById(R.id.imagenProducto);
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
                // Aquí seañade la consulta para obtener el nombre del usuario.
                FirebaseFirestore.getInstance().collection("usuarios").document(maqueta.getUsuarioId()).get().addOnSuccessListener(doc -> {
                            String nombre = doc.getString("nombre");
                            subidoPor.setText("Subido por " + (nombre != null ? nombre : "Desconocido"));
                        })
                        .addOnFailureListener(e -> {
                            subidoPor.setText("Subido por Desconocido");
                        });

                // Imagen del producto.
                if (maqueta.getImagenes() != null && !maqueta.getImagenes().isEmpty()) {
                    Glide.with(this)
                            .load(maqueta.getImagenes().get(0))
                            .placeholder(R.drawable.placeholder)
                            .into(imagenProducto);
                }

                // Imagen de perfil por defecto.
                Glide.with(this)
                        .load(R.drawable.imagenpordefecto)
                        .circleCrop()
                        .into(imagenPerfilUser);

                cargarSimilares(view);
            }
        }

        btnComprar.setOnClickListener(v -> {
            if (maqueta != null)
                Toast.makeText(getContext(), "Comprar: " + maqueta.getTitulo(), Toast.LENGTH_SHORT).show();
        });

        btnMandarMensaje.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Abrir chat con el usuario", Toast.LENGTH_SHORT).show();
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
        });
        recyclerSimilares.setAdapter(similaresAdapter);
        String uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 🔍 Log del usuario actual de la maqueta
        Log.d("SIMILARES", "Usuario de la maqueta actual: " + maqueta.getUsuarioId());
        Log.d("SIMILARES", "Categoría actual: " + maqueta.getCategoria());

        FirebaseFirestore.getInstance()
                .collection("maquetas")
                .whereEqualTo("categoria", maqueta.getCategoria())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("SIMILARES", "Maquetas encontradas en misma categoría: " + queryDocumentSnapshots.size());

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Maqueta similar = doc.toObject(Maqueta.class);

                        if (similar == null) {
                            continue;
                        }

                        if (similar.getId() != null &&
                                similar.getUsuarioId() != null &&
                                !similar.getId().equals(maqueta.getId()) &&
                                !similar.getUsuarioId().equals(uidActual)){


                            similaresList.add(similar);
                        }
                    }

                    similaresAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("SIMILARES", "ERROR: Error al cargar maquetas similares", e);
                });
    }

}
