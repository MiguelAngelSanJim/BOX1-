package com.boxuno.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boxuno.R;
import com.boxuno.adapter.MaquetaAdapter;
import com.boxuno.modelo.Maqueta;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DetalleProducto extends Fragment {

    private Maqueta maqueta;

    public DetalleProducto() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_producto, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String maquetaId = getArguments() != null ? getArguments().getString("idMaqueta") : null;
        if (maquetaId == null) return;

        FirebaseFirestore.getInstance()
                .collection("maquetas")
                .document(maquetaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    maqueta = documentSnapshot.toObject(Maqueta.class);
                    if (maqueta == null) return;

                    // Aquí ya puedes llamar a cargarSimilares
                    cargarSimilares(view);
                });
    }

    private void cargarSimilares(View view) {
        RecyclerView recyclerSimilares = view.findViewById(R.id.recycler_similares);
        recyclerSimilares.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        List<Maqueta> similaresList = new ArrayList<>();
        MaquetaAdapter similaresAdapter = new MaquetaAdapter(similaresList, getContext(), maqueta -> {
            Bundle bundle = new Bundle();
            bundle.putString("idMaqueta", maqueta.getId());
            NavHostFragment.findNavController(DetalleProducto.this).navigate(R.id.detalleProducto, bundle);
        });
        recyclerSimilares.setAdapter(similaresAdapter);

        FirebaseFirestore.getInstance()
                .collection("maquetas")
                .whereEqualTo("categoria", maqueta.getCategoria())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Maqueta similar = doc.toObject(Maqueta.class);
                        if (similar != null && !similar.getId().equals(maqueta.getId())) {
                            similaresList.add(similar);
                        }
                    }
                    similaresAdapter.notifyDataSetChanged();
                });
    }
}
