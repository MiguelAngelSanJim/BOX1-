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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.boxuno.R;
import com.boxuno.adapter.MaquetaAdapter;
import com.boxuno.modelo.Maqueta;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class VisitarPerfil extends Fragment {

    private TextView nombreTextView;
    private ImageView imagenPerfil;
    private RatingBar ratingBar;
    private RecyclerView recyclerView;
    private MaquetaAdapter adapter;
    private List<Maqueta> listaMaquetas = new ArrayList<>();
    private String usuarioId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visitar_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nombreTextView = view.findViewById(R.id.textNombreUsuario);
        imagenPerfil = view.findViewById(R.id.imagenPerfilUsuario);
        ratingBar = view.findViewById(R.id.ratingBarUsuario);
        recyclerView = view.findViewById(R.id.recyclerProductosUsuario);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new MaquetaAdapter(listaMaquetas, getContext(), maqueta -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("maqueta", maqueta);
            NavHostFragment.findNavController(this).navigate(R.id.detalleProducto, bundle);
        }, false, false);

        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            usuarioId = getArguments().getString("usuarioId");

            cargarPerfil(usuarioId);
        }
    }

    private void cargarPerfil(String usuarioId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios").document(usuarioId).get().addOnSuccessListener(doc -> {
            String nombre = doc.getString("nombre");
            String fotoUrl = doc.getString("fotoPerfilUrl");

            nombreTextView.setText(nombre != null ? nombre : "Usuario");
            Glide.with(requireContext())
                    .load(fotoUrl != null ? fotoUrl : R.drawable.imagenpordefecto)
                    .circleCrop()
                    .into(imagenPerfil);
        });

        db.collection("valoraciones").document(usuarioId).collection("usuarios")
                .get().addOnSuccessListener(snapshot -> {
                    float total = 0;
                    for (DocumentSnapshot val : snapshot) {
                        Double puntuacion = val.getDouble("puntuacion");
                        if (puntuacion != null) total += puntuacion;
                    }
                    float media = snapshot.isEmpty() ? 0f : total / snapshot.size();
                    ratingBar.setRating(media);
                });

        db.collection("maquetas")
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .addOnSuccessListener(query -> {
                    listaMaquetas.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Maqueta m = doc.toObject(Maqueta.class);
                        if (m != null) listaMaquetas.add(m);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}