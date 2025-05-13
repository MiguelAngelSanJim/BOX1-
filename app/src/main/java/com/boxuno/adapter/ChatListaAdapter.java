package com.boxuno.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boxuno.R;
import com.boxuno.modelo.ChatPreview;
import com.bumptech.glide.Glide;

import java.util.List;

public class ChatListaAdapter extends RecyclerView.Adapter<ChatListaAdapter.ChatViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(ChatPreview chat);
    }

    private final List<ChatPreview> chatList;
    private final OnChatClickListener listener;

    public ChatListaAdapter(List<ChatPreview> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_lista, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatPreview chat = chatList.get(position);
        holder.nombreTextView.setText(chat.getNombre());

        Glide.with(holder.itemView.getContext())
                .load(chat.getFotoPerfilUrl())
                .placeholder(R.drawable.imagenpordefecto)
                .circleCrop()
                .into(holder.fotoImageView);

        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView fotoImageView;
        TextView nombreTextView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            fotoImageView = itemView.findViewById(R.id.imagenPerfilChat);
            nombreTextView = itemView.findViewById(R.id.nombreUsuarioChat);
        }
    }
}
