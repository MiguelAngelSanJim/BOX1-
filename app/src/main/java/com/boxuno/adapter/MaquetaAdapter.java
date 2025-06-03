package com.boxuno.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.boxuno.R;
import com.boxuno.modelo.Maqueta;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MaquetaAdapter extends RecyclerView.Adapter<MaquetaAdapter.MaquetaViewHolder> {

    private List<Maqueta> maquetaList;
    private Context context;
    private Set<String> maquetasFavoritas = new HashSet<>();
    private String userId;
    private OnMaquetaClickListener listener;
    private boolean mostrarMeGusta;
    private boolean mostrarEliminar;

    private Map<String, String> motivosMap = new HashMap<>();

    public interface OnEliminarClickListener {
        void onEliminarClick(Maqueta maqueta);
    }

    private OnEliminarClickListener eliminarClickListener;

    public void setOnEliminarClickListener(OnEliminarClickListener listener) {
        this.eliminarClickListener = listener;
    }

    public interface OnMaquetaClickListener {
        void onMaquetaClick(Maqueta maqueta);
    }

    public MaquetaAdapter(List<Maqueta> maquetaList, Context context, OnMaquetaClickListener listener, boolean mostrarMeGusta, boolean mostrarEliminar) {
        this.maquetaList = maquetaList;
        this.context = context;
        this.listener = listener;
        this.mostrarMeGusta = mostrarMeGusta;
        this.mostrarEliminar = mostrarEliminar;

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario != null) {
            userId = usuario.getUid();
            cargarFavoritosDesdeFirebase();
        } else {
            userId = null;
        }
    }

    private void cargarFavoritosDesdeFirebase() {
        FirebaseFirestore.getInstance()
                .collection("favoritos")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> favoritos = (List<String>) documentSnapshot.get("favoritos");
                    if (favoritos != null) {
                        maquetasFavoritas.addAll(favoritos);
                        notifyDataSetChanged();
                    }
                });
    }

    @NonNull
    @Override
    public MaquetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_maqueta, parent, false);
        return new MaquetaViewHolder(view);
    }

    private void actualizarIconoCorazon(ImageView icono, String maquetaId) {
        if (maquetasFavoritas.contains(maquetaId)) {
            icono.setImageResource(R.drawable.ic_heart_red);
        } else {
            icono.setImageResource(R.drawable.ic_heart_white);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MaquetaViewHolder holder, int position) {
        Maqueta maqueta = maquetaList.get(position);
        String maquetaID = maqueta.getId();

        holder.titulo.setText(maqueta.getTitulo());
        holder.precio.setText(maqueta.getPrecio() + " €");
        holder.subidoPor.setText(maqueta.getNombreUsuario());

        if (maqueta.getImagenes() != null && !maqueta.getImagenes().isEmpty()) {
            Glide.with(context)
                    .load(maqueta.getImagenes().get(0))
                    .placeholder(R.drawable.placeholder)
                    .into(holder.imagen);
        }


        if (maqueta.isVendido()) {
            holder.textoVendido.setVisibility(View.VISIBLE);
        } else {
            holder.textoVendido.setVisibility(View.GONE);
        }

        if (mostrarMeGusta) {
            holder.meGusta.setVisibility(View.VISIBLE);
            actualizarIconoCorazon(holder.meGusta, maquetaID);
        } else {
            holder.meGusta.setVisibility(View.GONE);
        }

        holder.meGusta.setOnClickListener(v -> {
            DocumentReference docRef = FirebaseFirestore.getInstance()
                    .collection("favoritos")
                    .document(userId);

            boolean yaEsFavorito = maquetasFavoritas.contains(maquetaID);

            if (yaEsFavorito) {
                maquetasFavoritas.remove(maquetaID);
                holder.meGusta.setImageResource(R.drawable.ic_heart_white);
                docRef.update("favoritos", FieldValue.arrayRemove(maquetaID));
            } else {
                maquetasFavoritas.add(maquetaID);
                holder.meGusta.setImageResource(R.drawable.ic_heart_red);
                Map<String, Object> datos = new HashMap<>();
                datos.put("favoritos", FieldValue.arrayUnion(maquetaID));
                docRef.set(datos, SetOptions.merge());
            }
        });

        if (mostrarEliminar) {
            holder.eliminar.setVisibility(View.VISIBLE);
            holder.eliminar.setOnClickListener(v -> {
                if (eliminarClickListener != null) {
                    eliminarClickListener.onEliminarClick(maqueta);
                }
            });
        } else {
            holder.eliminar.setVisibility(View.GONE);
        }

        holder.carta.setOnClickListener(v -> {
            listener.onMaquetaClick(maqueta);
        });

        String motivo = motivosMap.get(maquetaID);
        if (motivo != null && !motivo.isEmpty()) {
            holder.motivoDenuncia.setText("Motivo: " + motivo);
            holder.motivoDenuncia.setVisibility(View.VISIBLE);
        } else {
            holder.motivoDenuncia.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return maquetaList.size();
    }

    public static class MaquetaViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen, meGusta, eliminar;
        TextView titulo, precio, subidoPor, motivoDenuncia, textoVendido;
        CardView carta;

        public MaquetaViewHolder(@NonNull View itemView) {
            super(itemView);
            carta = itemView.findViewById(R.id.carta);
            imagen = itemView.findViewById(R.id.imagen_maqueta);
            titulo = itemView.findViewById(R.id.titulo_maqueta);
            precio = itemView.findViewById(R.id.precio_maqueta);
            subidoPor = itemView.findViewById(R.id.subido_por);
            meGusta = itemView.findViewById(R.id.megusta);
            eliminar = itemView.findViewById(R.id.eliminar);
            motivoDenuncia = itemView.findViewById(R.id.motivo_denuncia);
            textoVendido = itemView.findViewById(R.id.texto_vendido); // NUEVO
        }
    }
}
