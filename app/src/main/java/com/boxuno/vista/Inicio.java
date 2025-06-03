package com.boxuno.vista;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boxuno.R;
import com.boxuno.adapter.MaquetaAdapter;
import com.boxuno.modelo.Maqueta;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inicio extends Fragment {
    private List<Maqueta> maquetaList;
    private MaquetaAdapter maquetaAdapter;
    private SearchView buscador;
    private String categoriaSeleccionada = null;

    public Inicio() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buscador = view.findViewById(R.id.buscador);
        // Cambiar el color del texto a negro
        int searchEditTextId = buscador.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = buscador.findViewById(searchEditTextId);
        searchEditText.setTextColor(Color.BLACK);
        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                buscarMaquetasOUsuarios(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    NavHostFragment.findNavController(Inicio.this).navigate(R.id.inicio);
                }
                return true;
            }
        });

        buscador.setOnCloseListener(() -> {
            NavHostFragment.findNavController(Inicio.this).navigate(R.id.inicio);
            return false;
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewinicio);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        maquetaList = new ArrayList<>();
        maquetaAdapter = new MaquetaAdapter(maquetaList, getContext(), maqueta -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("maqueta", maqueta);
            NavHostFragment.findNavController(Inicio.this).navigate(R.id.action_inicio_to_detalle_producto, bundle);
        }, true, false);
        recyclerView.setAdapter(maquetaAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("maquetas").whereNotEqualTo("usuarioId", uidActual).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Maqueta maqueta = doc.toObject(Maqueta.class);
                if (maqueta == null) continue;

                String userId = maqueta.getUsuarioId();

                db.collection("usuarios").document(userId).get().addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        maqueta.setNombreUsuario(userDoc.getString("nombre"));
                    } else {
                        maqueta.setNombreUsuario("Desconocido");
                    }
                    maquetaList.add(maqueta);
                    maquetaList.sort((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()));
                    maquetaAdapter.notifyDataSetChanged();
                }).addOnFailureListener(e -> {
                    maqueta.setNombreUsuario("Error");
                    maquetaList.add(maqueta);
                    maquetaAdapter.notifyDataSetChanged();
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error al cargar maquetas", Toast.LENGTH_SHORT).show();
        });

        TabLayout categorias = view.findViewById(R.id.tabLayoutCategorias);
        categorias.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        categoriaSeleccionada = null;
                        break;
                    case 1:
                        categoriaSeleccionada = "F1";
                        break;
                    case 2:
                        categoriaSeleccionada = "WRC";
                        break;
                    case 3:
                        categoriaSeleccionada = "Resistencia";
                        break;
                    case 4:
                        categoriaSeleccionada = "Otros";
                        break;
                }
                cargarMaquetas(categoriaSeleccionada);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                cargarMaquetas(categoriaSeleccionada);
            }
        });

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomnavigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }

        comprobarValoracionPendiente();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish(); // Cierra la app si estás en Inicio
            }
        });

    }

    private void cargarMaquetas(@Nullable String categoria) {
        maquetaList.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("maquetas").whereNotEqualTo("usuarioId", uidActual).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Maqueta maqueta = doc.toObject(Maqueta.class);
                if (maqueta == null) continue;
                if (categoria == null || categoria.equalsIgnoreCase(maqueta.getCategoria())) {
                    String userId = maqueta.getUsuarioId();
                    db.collection("usuarios").document(userId).get().addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            maqueta.setNombreUsuario(userDoc.getString("nombre"));
                        } else {
                            maqueta.setNombreUsuario("Desconocido");
                        }
                        maquetaList.add(maqueta);
                        maquetaAdapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                        maqueta.setNombreUsuario("Error");
                        maquetaList.add(maqueta);
                        maquetaAdapter.notifyDataSetChanged();
                    });
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error al cargar maquetas", Toast.LENGTH_SHORT).show();
        });
    }

    private void buscarMaquetasOUsuarios(String query) {
        if (query.isEmpty()) {
            cargarMaquetas(categoriaSeleccionada);  // ⬅ usamos la categoría actual
            return;
        }

        maquetaList.clear();
        maquetaAdapter.notifyDataSetChanged();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("maquetas").whereNotEqualTo("usuarioId", uidActual).get().addOnSuccessListener(snapshot -> {
            boolean encontrado = false;
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Maqueta maqueta = doc.toObject(Maqueta.class);
                if (maqueta != null) {

                    if (categoriaSeleccionada != null && !categoriaSeleccionada.equalsIgnoreCase(maqueta.getCategoria())) {
                        continue;  // saltamos si no es de la categoría seleccionada
                    }

                    if (maqueta.getTitulo().toLowerCase().contains(query.toLowerCase())) {
                        maquetaList.add(maqueta);
                        encontrado = true;
                    }
                }
            }
            if (encontrado) {
                maquetaAdapter.notifyDataSetChanged();
            } else {
                buscarPorUsuario(query);
            }
        });
    }


    private void buscarPorUsuario(String nombreUsuario) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios").whereEqualTo("nombre", nombreUsuario).get().addOnSuccessListener(userSnapshot -> {
            if (!userSnapshot.isEmpty()) {
                String userIdBuscado = userSnapshot.getDocuments().get(0).getId();
                db.collection("maquetas").whereEqualTo("usuarioId", userIdBuscado).get().addOnSuccessListener(maquetaSnapshot -> {
                    maquetaList.clear();
                    for (DocumentSnapshot doc : maquetaSnapshot) {
                        Maqueta maqueta = doc.toObject(Maqueta.class);
                        if (maqueta != null) {
                            maquetaList.add(maqueta);
                        }
                    }
                    maquetaAdapter.notifyDataSetChanged();
                });
            } else {
                Toast.makeText(getContext(), "No se encontró ningún usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void comprobarValoracionPendiente() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("valoracionesPendientes")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String vendedorId = doc.getString("vendedorId");
                        String productoId = doc.getString("productoId"); // Por si quieres guardar esta info
                        if (vendedorId == null) return;

                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Box1DialogEstilo);
                        builder.setTitle("Producto entregado");
                        builder.setMessage("Valora tu experiencia con el vendedor:");

                        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_valoracion, null);
                        builder.setView(view);

                        RatingBar ratingBar = view.findViewById(R.id.ratingBarValoracion);
                        ratingBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#0B1B4E"))); // Color estrellas activas
                        ratingBar.setSecondaryProgressTintList(ColorStateList.valueOf(Color.parseColor("#B0BEC5"))); // Color intermedio (opcional)
                        ratingBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CFD8DC")));
                        builder.setPositiveButton("Enviar", (dialogInterface, which) -> {
                            float puntuacion = ratingBar.getRating();

                            Map<String, Object> valoracion = new HashMap<>();
                            valoracion.put("usuarioId", uid);
                            valoracion.put("puntuacion", puntuacion);
                            valoracion.put("timestamp", System.currentTimeMillis());
                            if (productoId != null) {
                                valoracion.put("productoId", productoId);
                            }

                            FirebaseFirestore.getInstance()
                                    .collection("valoraciones")
                                    .document(vendedorId)
                                    .collection("usuarios")
                                    .add(valoracion)
                                    .addOnSuccessListener(unused ->
                                            Toast.makeText(getContext(), "¡Gracias por tu valoración!", Toast.LENGTH_SHORT).show()
                                    );

                            FirebaseFirestore.getInstance().collection("valoracionesPendientes").document(uid).delete();
                        });

                        builder.setCancelable(false);

                        AlertDialog dialogo = builder.create();
                        dialogo.show();

                        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0B1B4E"));

                    }
                });
    }
}