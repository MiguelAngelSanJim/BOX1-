package com.boxuno.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boxuno.R;
import com.boxuno.adapter.ChatListaAdapter;
import com.boxuno.modelo.ChatPreview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChatLista extends Fragment {

    private RecyclerView recyclerView;
    private ChatListaAdapter adapter;
    private List<ChatPreview> chats = new ArrayList<>();
    private DatabaseReference chatRef;
    private String uidActual;
    private boolean isActive = false;

    public ChatLista() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_lista, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerListaChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatListaAdapter(chats, chat -> {
            Bundle bundle = new Bundle();
            bundle.putString("uidDestino", chat.getUid());
            bundle.putString("nombreDestino", chat.getNombre());
            bundle.putString("fotoDestino", chat.getFotoPerfilUrl());
            bundle.putString("chatId", chat.getChatId());
            bundle.putString("productoTitulo", chat.getProductoTitulo());
            bundle.putDouble("productoPrecio", chat.getProductoPrecio());
            bundle.putString("productoId", chat.getProductoId());

            NavHostFragment.findNavController(this).navigate(R.id.chatConversacion, bundle);
        });
        recyclerView.setAdapter(adapter);

        uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(uidActual);

        cargarChats();
        isActive = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isActive = false;
    }

    private void cargarChats() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats.clear();
                for (DataSnapshot chatSnap : snapshot.getChildren()) {
                    String uid = chatSnap.getKey();
                    String nombre = chatSnap.child("nombre").getValue(String.class);
                    String foto = chatSnap.child("fotoPerfilUrl").getValue(String.class);
                    String productoId = chatSnap.child("productoId").getValue(String.class);
                    String productoTitulo = chatSnap.child("productoTitulo").getValue(String.class);
                    Double productoPrecio = chatSnap.child("productoPrecio").getValue(Double.class);

                    String chatIdCompleto = uid;  // ya es el chatId real
                    String uidOtro = extraerUidOtro(uid, uidActual);
                    chats.add(new ChatPreview(uidOtro, nombre, foto, productoId, productoTitulo, productoPrecio, chatIdCompleto));

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isActive && getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar chats", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String extraerUidOtro(String chatId, String miUid) {
        String[] partes = chatId.split("_");
        if (partes.length >= 2) {
            return partes[0].equals(miUid) ? partes[1] : partes[0];
        }
        return "";
    }

}
