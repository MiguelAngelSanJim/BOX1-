package com.boxuno.vista;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boxuno.R;
import com.boxuno.adapter.MensajeAdapter;
import com.boxuno.modelo.Mensaje;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.*;

import java.util.*;

public class ChatConversacion extends Fragment {

    private RecyclerView recyclerMensajes;
    private EditText editTextoMensaje;
    private TextView textoTituloProductoChat;
    private TextView textoPrecioProductoChat;
    private ImageButton btnEnviar;
    private ImageView imagenPerfilOtroUsuario;
    private TextView nombreOtroUsuario;

    private MensajeAdapter adapter;
    private List<Mensaje> listaMensajes = new ArrayList<>();

    private FirebaseFirestore db;
    private String uidActual, uidDestino, productoId;
    private String chatId;
    private String fotoUrlDestino;
    private String tituloProducto;
    private Double precioProducto;

    public ChatConversacion() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_conversacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerMensajes = view.findViewById(R.id.recyclerMensajes);
        editTextoMensaje = view.findViewById(R.id.editTextoMensaje);
        btnEnviar = view.findViewById(R.id.btnEnviarMensaje);
        imagenPerfilOtroUsuario = view.findViewById(R.id.imagenPerfilOtroUsuario);
        nombreOtroUsuario = view.findViewById(R.id.nombreOtroUsuario);
        textoPrecioProductoChat = view.findViewById(R.id.textoPrecioProductoChat);
        textoTituloProductoChat = view.findViewById(R.id.textoTituloProductoChat);
        uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recyclerMensajes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MensajeAdapter(getContext(), listaMensajes, uidActual);
        recyclerMensajes.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();


        // Recoger UID y datos del otro usuario desde los argumentos.
        if (getArguments() != null) {
            chatId = getArguments().getString("chatId");
            uidDestino = getArguments().getString("uidDestino");
            String nombre = getArguments().getString("nombreDestino");
            fotoUrlDestino = getArguments().getString("fotoDestino");
            tituloProducto = getArguments().getString("productoTitulo");
            precioProducto = getArguments().getDouble("productoPrecio");

            productoId = getArguments().getString("productoId");
            nombreOtroUsuario.setText(nombre);
            textoTituloProductoChat.setText(tituloProducto);
            textoPrecioProductoChat.setText(precioProducto + "€");
            Glide.with(requireContext()).load(fotoUrlDestino).circleCrop().into(imagenPerfilOtroUsuario);
        }

        if (chatId == null || chatId.isEmpty()) {
            chatId = generarChatId(uidActual, uidDestino, productoId);
        }

        escucharMensajes();

        btnEnviar.setOnClickListener(v -> enviarMensaje());

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomnavigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.INVISIBLE);
        }
    }

    private void enviarMensaje() {
        String texto = editTextoMensaje.getText().toString().trim();
        if (TextUtils.isEmpty(texto)) return;

        Mensaje mensaje = new Mensaje(uidActual,uidDestino, texto, System.currentTimeMillis());

        db.collection("chats")
                .document(chatId)
                .collection("mensajes")
                .add(mensaje)
                .addOnSuccessListener(aVoid -> {
                    editTextoMensaje.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                });
        guardarEnListaChatsRealtime(uidDestino, nombreOtroUsuario.getText().toString(), fotoUrlDestino);

    }

    private void escucharMensajes() {
        db.collection("chats")
                .document(chatId)
                .collection("mensajes")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error al cargar mensajes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaMensajes.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Mensaje mensaje = doc.toObject(Mensaje.class);
                        listaMensajes.add(mensaje);
                    }
                    adapter.notifyDataSetChanged();
                    recyclerMensajes.scrollToPosition(listaMensajes.size() - 1);
                });
    }

    private String generarChatId(String uid1, String uid2, String productoId) {
        String ordenadoUID = uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
        return ordenadoUID + "_" + productoId;
    }


    private void guardarEnListaChatsRealtime(String uidDestino, String nombreDestino, String fotoDestino) {
        Log.d("CHAT_RTD", "Guardando chat para " + uidDestino + " con nombre: " + nombreDestino + ", foto: " + fotoDestino);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("chats");

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombreDestino);
        datos.put("fotoPerfilUrl", fotoDestino);
        datos.put("productoId", productoId);
        datos.put("productoTitulo", tituloProducto);
        datos.put("productoPrecio", precioProducto);


        dbRef.child(uidActual).child(chatId).updateChildren(datos)
                .addOnSuccessListener(aVoid -> Log.d("CHAT_RTD", "Guardado en nodo del usuario actual"))
                .addOnFailureListener(e -> Log.e("CHAT_RTD", "Error al guardar en RTDB (usuario actual)", e));

        FirebaseFirestore.getInstance().collection("usuarios").document(uidActual).get()
                .addOnSuccessListener(doc -> {
                    String nombreActual = doc.getString("nombre");
                    String fotoActual = doc.getString("fotoPerfilUrl");

                    Map<String, Object> datosActual = new HashMap<>();
                    datosActual.put("nombre", nombreActual != null ? nombreActual : "Usuario");
                    datosActual.put("fotoPerfilUrl", fotoActual != null ? fotoActual : "");
                    datosActual.put("productoId", productoId);
                    datosActual.put("productoTitulo", tituloProducto);
                    datosActual.put("productoPrecio", precioProducto);

                    dbRef.child(uidDestino).child(chatId).updateChildren(datosActual)
                            .addOnSuccessListener(aVoid -> Log.d("CHAT_RTD", "Guardado en nodo del otro usuario"))
                            .addOnFailureListener(e -> Log.e("CHAT_RTD", "Error al guardar en RTDB (otro usuario)", e));
                });
    }


}
