package com.boxuno.vista;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.boxuno.R;
import com.boxuno.adapter.MaquetaAdapter;
import com.boxuno.modelo.Maqueta;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class Inicio extends Fragment {
    private List<Maqueta> maquetaList;
    private MaquetaAdapter maquetaAdapter;
    private SearchView buscador;

    public Inicio() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                buscarMaquetasOUsuarios(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty()){
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
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columnas
        maquetaList = new ArrayList<>();
        maquetaAdapter = new MaquetaAdapter(maquetaList, getContext(), maqueta -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("maqueta", maqueta);

            NavHostFragment.findNavController(Inicio.this).navigate(R.id.action_inicio_to_detalle_producto, bundle);
        }, true);
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
                                })
                                .addOnFailureListener(e -> {
                                    maqueta.setNombreUsuario("Error");
                                    maquetaList.add(maqueta);
                                    maquetaAdapter.notifyDataSetChanged();
                                });

                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar maquetas", Toast.LENGTH_SHORT).show();
                });

        TabLayout categorias = view.findViewById(R.id.tabLayoutCategorias);

        categorias.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String categoriaSeleccionada = null;

                switch (tab.getPosition()) {
                    case 0:
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

            }
        });


        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomnavigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    private void cargarMaquetas(@Nullable String categoria) {
        maquetaList.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("maquetas")
                .whereNotEqualTo("usuarioId", uidActual)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Maqueta maqueta = doc.toObject(Maqueta.class);
                        if (maqueta == null) continue;

                        if (categoria == null || categoria.equalsIgnoreCase(maqueta.getCategoria())) {
                            String userId = maqueta.getUsuarioId();
                            db.collection("usuarios").document(userId)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            maqueta.setNombreUsuario(userDoc.getString("nombre"));
                                        } else {
                                            maqueta.setNombreUsuario("Desconocido");
                                        }
                                        maquetaList.add(maqueta);
                                        maquetaAdapter.notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> {
                                        maqueta.setNombreUsuario("Error");
                                        maquetaList.add(maqueta);
                                        maquetaAdapter.notifyDataSetChanged();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar maquetas", Toast.LENGTH_SHORT).show();
                });
    }

    private void buscarMaquetasOUsuarios(String query) {
        if (query.isEmpty()) {
            cargarMaquetas(null); // Si el campo está vacío, recarga normalmente
            return;
        }

        maquetaList.clear();
        maquetaAdapter.notifyDataSetChanged();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("maquetas")
                .whereNotEqualTo("usuarioId", uidActual)
                .get()
                .addOnSuccessListener(snapshot -> {
                    boolean encontrado = false;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Maqueta maqueta = doc.toObject(Maqueta.class);
                        if (maqueta != null && maqueta.getTitulo().toLowerCase().contains(query.toLowerCase())) {
                            maquetaList.add(maqueta);
                            encontrado = true;
                        }
                    }
                    if (encontrado) {
                        maquetaAdapter.notifyDataSetChanged();
                    } else {
                        // 2. Si no encontramos en títulos, buscamos en usuarios
                        buscarPorUsuario(query);
                    }
                });
    }

    private void buscarPorUsuario(String nombreUsuario) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios")
                .whereEqualTo("nombre", nombreUsuario)
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    if (!userSnapshot.isEmpty()) {
                        String userIdBuscado = userSnapshot.getDocuments().get(0).getId();

                        db.collection("maquetas")
                                .whereEqualTo("usuarioId", userIdBuscado)
                                .get()
                                .addOnSuccessListener(maquetaSnapshot -> {
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

}