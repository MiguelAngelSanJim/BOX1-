package com.boxuno.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boxuno.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class ImagenesAdapter extends RecyclerView.Adapter<ImagenesAdapter.ImagenViewHolder>{
    private List<Uri> listaImagenes;
    private Context context;

    public ImagenesAdapter(Context context, List<Uri> listaImagenes) {
        this.context = context;
        this.listaImagenes = listaImagenes;
    }

    @NonNull
    @Override
    public ImagenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.item_imagen, parent, false);
        return new ImagenViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagenViewHolder holder, int position) {
        String url = String.valueOf(listaImagenes.get(position));
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.placeholder) // Imagen por defecto
                .into(holder.imagen);
    }

    @Override
    public int getItemCount() {
        return listaImagenes.size();
    }

    public static class ImagenViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;

        public ImagenViewHolder(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imagen_item);
        }
    }
}
