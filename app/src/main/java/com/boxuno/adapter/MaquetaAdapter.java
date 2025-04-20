package com.boxuno.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.boxuno.R;
import com.boxuno.modelo.Maqueta;

import java.util.List;

public class MaquetaAdapter extends RecyclerView.Adapter<MaquetaAdapter.MaquetaViewHolder> {
    private List<Maqueta> maquetaList;
    private Context context;

    public MaquetaAdapter(List<Maqueta> maquetaList, Context context) {
        this.maquetaList = maquetaList;
        this.context = context;
    }

    @NonNull
    @Override
    public MaquetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_maqueta, parent, false);
        return new MaquetaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaquetaViewHolder holder, int position) {
        Maqueta maqueta = maquetaList.get(position);
        holder.titulo.setText(maqueta.getTitulo());
        holder.precio.setText(maqueta.getPrecio() + " €");

        if (maqueta.getImagenes() != null && !maqueta.getImagenes().isEmpty()) {
            Glide.with(context)
                    .load(maqueta.getImagenes().get(0))
                    .placeholder(R.drawable.placeholder)// imagen por defecto
                    .into(holder.imagen);
        }
    }

    @Override
    public int getItemCount() {
        return maquetaList.size();
    }

    public static class MaquetaViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView titulo, precio;

        public MaquetaViewHolder(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imagen_maqueta);
            titulo = itemView.findViewById(R.id.titulo_maqueta);
            precio = itemView.findViewById(R.id.precio_maqueta);
        }
    }
}

