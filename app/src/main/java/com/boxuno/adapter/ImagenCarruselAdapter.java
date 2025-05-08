package com.boxuno.adapter;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.boxuno.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class ImagenCarruselAdapter extends RecyclerView.Adapter<ImagenCarruselAdapter.ImagenViewHolder> {

    private Context context;
    private List<Object> imagenes;

    public ImagenCarruselAdapter(Context context, List<Object> imagenes) {
        this.context = context;
        this.imagenes = imagenes;
    }

    @NonNull
    @Override
    public ImagenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carrusel_imagen, parent, false);
        return new ImagenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagenViewHolder holder, int position) {
        Object imagen = imagenes.get(position);

        if (imagen instanceof Uri) {
            Glide.with(context).load((Uri) imagen).into(holder.imageView);
            holder.btnEliminar.setVisibility(View.VISIBLE); // Mostrar botón solo para nuevas
        } else if (imagen instanceof String) {
            Glide.with(context).load((String) imagen).into(holder.imageView);
            holder.btnEliminar.setVisibility(View.GONE); // Ocultar si es imagen ya subida
        }

        holder.btnEliminar.setOnClickListener(v -> {
            imagenes.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, imagenes.size());
        });

        holder.imageView.setOnClickListener(v -> {
            Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.dialog_ampliar_imagen);
            ViewPager2 dialogViewPager = dialog.findViewById(R.id.dialogViewPager);
            ImageButton btnCerrar = dialog.findViewById(R.id.btnCerrarCarrusel);

            ImagenCarruselAdapter dialogAdapter = new ImagenCarruselAdapter(context, imagenes);
            dialogViewPager.setAdapter(dialogAdapter);
            dialogViewPager.setCurrentItem(position, false);

            btnCerrar.setOnClickListener(v1 -> dialog.dismiss());
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return imagenes.size();
    }

    public static class ImagenViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnEliminar;

        public ImagenViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewCarrusel);
            btnEliminar = itemView.findViewById(R.id.btnEliminarImagen);
        }
    }
}

