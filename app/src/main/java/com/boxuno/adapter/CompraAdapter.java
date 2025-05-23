package com.boxuno.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boxuno.R;
import com.boxuno.modelo.Compra;

import java.util.List;

public class CompraAdapter extends RecyclerView.Adapter<CompraAdapter.CompraViewHolder> {
    private final List<Compra> compras;

    public CompraAdapter(List<Compra> compras) {
        this.compras = compras;
    }

    @NonNull
    @Override
    public CompraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_compra, parent, false);
        return new CompraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompraViewHolder holder, int position) {
        Compra compra = compras.get(position);
        holder.textNombre.setText(compra.getNombreProducto());
        holder.textPrecio.setText(compra.getPrecio() + " €");
        holder.textFecha.setText(compra.getFecha());
    }

    @Override
    public int getItemCount() {
        return compras.size();
    }

    static class CompraViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre, textPrecio, textFecha;

        CompraViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textNombreCompra);
            textPrecio = itemView.findViewById(R.id.textPrecioCompra);
            textFecha = itemView.findViewById(R.id.textFechaCompra);
        }
    }
}

