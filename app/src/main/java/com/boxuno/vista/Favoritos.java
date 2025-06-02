package com.boxuno.vista;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.boxuno.R;
import com.boxuno.adapter.CompraAdapter;
import com.boxuno.adapter.MaquetaAdapter;
import com.boxuno.modelo.Compra;
import com.boxuno.modelo.Maqueta;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class Favoritos extends Fragment {
    private MaquetaAdapter maquetaAdapter;
    private List<Maqueta> maquetaList;

    public Favoritos() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favoritos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerFavoritos);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columnas

        maquetaList = new ArrayList<>();

        maquetaAdapter = new MaquetaAdapter(maquetaList, getContext(), maqueta -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("maqueta", maqueta);
            NavHostFragment.findNavController(Favoritos.this).navigate(R.id.action_favoritos_to_detalle_producto, bundle);
        }, false, false);

        recyclerView.setAdapter(maquetaAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("favoritos").document(uidActual).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> favoritosIds = (List<String>) documentSnapshot.get("favoritos");

                        if (favoritosIds == null || favoritosIds.isEmpty()) {
                            Toast.makeText(getContext(), "No tienes maquetas en favoritos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (String maquetaId : favoritosIds) {
                            db.collection("maquetas").document(maquetaId).get()
                                    .addOnSuccessListener(maquetaDoc -> {
                                        Maqueta maqueta = maquetaDoc.toObject(Maqueta.class);
                                        if (maqueta == null) return;

                                        String userId = maqueta.getUsuarioId();

                                        db.collection("usuarios").document(userId).get()
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
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FAVORITOS", "Error al cargar maqueta: " + maquetaId, e);
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "No tienes maquetas en favoritos", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar favoritos", Toast.LENGTH_SHORT).show();
                    Log.e("FAVORITOS", "Error Firestore", e);
                });

        TabLayout tabLayout = view.findViewById(R.id.tabLayoutFavoritos);
        RecyclerView recycler = view.findViewById(R.id.recyclerFavoritos);

        List<Compra> compraList = new ArrayList<>();
        CompraAdapter compraAdapter = new CompraAdapter(compraList);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Mostrar favoritos
                    recycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    recycler.setAdapter(maquetaAdapter);
                } else {
                    // Mostrar historial
                    recycler.setLayoutManager(new LinearLayoutManager(getContext()));
                    recycler.setAdapter(compraAdapter);
                    cargarHistorial(compraList, compraAdapter);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

    }

    private void cargarHistorial(List<Compra> lista, CompraAdapter adapter) {
        lista.clear();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("compras")
                .whereEqualTo("compradorId", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        String nombreUsuario = doc.getString("usuarioNombre");
                        String nombre = doc.getString("productoNombre");
                        Double precio = doc.getDouble("productoPrecio");
                        String fecha = doc.getString("fecha");

                        if (nombre != null && precio != null && fecha != null) {
                            lista.add(new Compra(nombreUsuario,nombre, precio, fecha));
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }


}