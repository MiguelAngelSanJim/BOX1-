package com.boxuno.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boxuno.R;
import com.boxuno.modelo.Mensaje;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MensajeAdapter extends RecyclerView.Adapter<MensajeAdapter.MensajeViewHolder> {

    private final List<Mensaje> listaMensajes;
    private final Context context;
    private final String uidActual;

    public MensajeAdapter(Context context, List<Mensaje> listaMensajes) {
        this.context = context;
        this.listaMensajes = listaMensajes;
        this.uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int getItemViewType(int position) {
        String autorId = listaMensajes.get(position).getAutorId();
        if (autorId != null && autorId.equals(uidActual)) {
            return 1; // Mensaje enviado por el usuario actual
        } else {
            return 0; // Mensaje recibido
        }
    }

    @NonNull
    @Override
    public MensajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.item_mensaje_enviado, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_mensaje_recibido, parent, false);
        }
        return new MensajeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensajeViewHolder holder, int position) {
        holder.textoMensaje.setText(listaMensajes.get(position).getTexto());
    }

    @Override
    public int getItemCount() {
        return listaMensajes.size();
    }

    public static class MensajeViewHolder extends RecyclerView.ViewHolder {
        TextView textoMensaje;

        public MensajeViewHolder(@NonNull View itemView) {
            super(itemView);
            textoMensaje = itemView.findViewById(R.id.textoMensaje);
        }
    }
}
