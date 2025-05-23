package com.boxuno.vista;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.boxuno.R;
import com.boxuno.adapter.MaquetaAdapter;
import com.boxuno.modelo.Maqueta;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Perfil extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView fotoPerfil;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String userId;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private TextView textViewNombrePerfil;
    private RatingBar ratingBarValoraciones;

    public Perfil() {
    }

    private void confirmar() {
        AlertDialog dialogo = new AlertDialog.Builder(requireContext(), R.style.Box1DialogEstilo)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setIcon(R.drawable.logopng)
                .setPositiveButton("Sí", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();

                    SharedPreferences prefs = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.login);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0B1B4E"));
        dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#0B1B4E"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        subirImagen();
                    }
                }
        );

        fotoPerfil = view.findViewById(R.id.imagenUserPerfil);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("fotos_perfil");

        cargarFotoPerfil();

        fotoPerfil.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Foto de perfil")
                    .setIcon(R.drawable.logopng)
                    .setPositiveButton("Cambiar foto de perfil", (dialog, which) -> openFileChooser())
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        return view;
    }

    private void subirImagen() {
        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(userId + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        db.collection("usuarios").document(userId)
                                .update("fotoPerfilUrl", imageUrl)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(getContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error al guardar URL en Firestore", Toast.LENGTH_SHORT).show()
                                );
                    }))
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error al subir imagen a Storage", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TabLayout opcionesPerfil = view.findViewById(R.id.tabLayoutPerfil);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        String email = auth.getCurrentUser().getEmail();

        textViewNombrePerfil = view.findViewById(R.id.textViewNombrePerfil);
        ratingBarValoraciones = view.findViewById(R.id.ratingBarValoraciones);


        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        if (nombre != null) {
                            textViewNombrePerfil.setText(nombre);
                        }

                        db.collection("valoraciones").document(userId).collection("usuarios")
                                .get()
                                .addOnSuccessListener(valoraciones -> {
                                    double suma = 0;
                                    int total = valoraciones.size();

                                    for (DocumentSnapshot v : valoraciones) {
                                        Double estrellas = v.getDouble("estrellas");
                                        if (estrellas != null) suma += estrellas;
                                    }

                                    if (total > 0) {
                                        float media = (float) (suma / total);
                                        ratingBarValoraciones.setRating(media);
                                    } else {
                                        ratingBarValoraciones.setRating(0f);
                                    }
                                });
                    }
                });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        ImageView fotoperfil = view.findViewById(R.id.imagenUserPerfil);
        TextView textoMisProductos = view.findViewById(R.id.textMisProductos);
        List<Maqueta> maquetaList = new ArrayList<>();

        MaquetaAdapter adapter = new MaquetaAdapter(maquetaList, getContext(), maqueta -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("maqueta", maqueta);
            NavHostFragment.findNavController(Perfil.this).navigate(R.id.detalleProducto, bundle);
        }, false, true);

        recyclerView.setAdapter(adapter);

        adapter.setOnEliminarClickListener(maqueta -> {
            AlertDialog dialogo = new AlertDialog.Builder(requireContext(), R.style.Box1DialogEstilo)
                    .setTitle("Eliminar producto")
                    .setMessage("¿Seguro que quieres eliminar esta maqueta?")
                    .setPositiveButton("Sí", (dialog, which) -> eliminarMaqueta(maqueta, maquetaList, adapter))
                    .setNegativeButton("Cancelar", null)
                    .show();

            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0B1B4E"));
            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#0B1B4E"));
        });

        Glide.with(this)
                .load(R.drawable.imagenpordefecto)
                .circleCrop()
                .into(fotoperfil);

        if (email != null && email.equals("box1coleccion@gmail.com")) {
            textoMisProductos.setText("Reclamaciones");
            cargarProductosDenunciados(adapter, maquetaList);
        } else {
            db.collection("maquetas")
                    .whereEqualTo("usuarioId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        maquetaList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Maqueta maqueta = document.toObject(Maqueta.class);
                            maquetaList.add(maqueta);
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al cargar maquetas", Toast.LENGTH_SHORT).show();
                    });
        }

        opcionesPerfil.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                handleTabAction(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                handleTabAction(tab);
            }
        });
    }

    private void eliminarMaqueta(Maqueta maqueta, List<Maqueta> maquetaList, MaquetaAdapter adapter) {
        FirebaseFirestore.getInstance().collection("maquetas")
                .document(maqueta.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    maquetaList.remove(maqueta);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Maqueta eliminada", Toast.LENGTH_SHORT).show();
                    enviarMensajeAdmin(maqueta);

                    eliminarDenunciasAsociadas(maqueta.getId());
                    eliminarImagenesDeStorage(maqueta);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al eliminar la maqueta", Toast.LENGTH_SHORT).show()
                );
    }

    private void eliminarDenunciasAsociadas(String productoId) {
        FirebaseFirestore.getInstance().collection("denuncias")
                .whereEqualTo("productoId", productoId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        doc.getReference().delete();
                    }
                });
    }

    private void eliminarImagenesDeStorage(Maqueta maqueta) {
        if (maqueta.getImagenes() != null) {
            for (String url : maqueta.getImagenes()) {
                FirebaseStorage.getInstance().getReferenceFromUrl(url).delete();
            }
        }
    }

    private void handleTabAction(TabLayout.Tab tab) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        String tabText = tab.getText().toString();

        if (tabText.equals("Cerrar sesión")) {
            confirmar();
        } else if (tabText.equals("Editar perfil")) {
            navController.navigate(R.id.editarPerfil2);
        } else if (tabText.equals("Mi dirección")) {
            navController.navigate(R.id.direccion);
        }
    }

    private void cargarFotoPerfil() {
        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String url = documentSnapshot.getString("fotoPerfilUrl");
                        if (url != null && !url.isEmpty()) {
                            Glide.with(getContext()).load(url).circleCrop().into(fotoPerfil);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar foto de perfil", Toast.LENGTH_SHORT).show()
                );
    }

    private void cargarProductosDenunciados(MaquetaAdapter adapter, List<Maqueta> maquetaList) {
        db.collection("denuncias")
                .get()
                .addOnSuccessListener(denuncias -> {
                    maquetaList.clear();
                    for (DocumentSnapshot denuncia : denuncias) {
                        String productoId = denuncia.getString("productoId");
                        String motivo = denuncia.getString("motivo");
                        String otros = denuncia.getString("otros");

                        db.collection("maquetas").document(productoId)
                                .get()
                                .addOnSuccessListener(producto -> {
                                    if (producto.exists()) {
                                        Maqueta maqueta = producto.toObject(Maqueta.class);

                                        if (motivo != null) {
                                            if (motivo.equals("Otros") && otros != null && !otros.isEmpty()) {
                                                maqueta.setDescripcion("Motivo: " + otros);
                                            } else {
                                                maqueta.setDescripcion("Motivo: " + motivo);
                                            }
                                        }

                                        if (!maquetaList.contains(maqueta)) {
                                            maquetaList.add(maqueta);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar productos denunciados", Toast.LENGTH_SHORT).show();
                });
    }

    private void enviarMensajeAdmin(Maqueta maqueta) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseDatabase realtimeDB = FirebaseDatabase.getInstance();
        String adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String usuarioId = maqueta.getUsuarioId();

        db.collection("denuncias")
                .whereEqualTo("productoId", maqueta.getId())
                .get()
                .addOnSuccessListener(denuncias -> {
                    String motivo = "sin especificar";
                    if (!denuncias.isEmpty()) {
                        motivo = denuncias.getDocuments().get(0).getString("motivo");
                    }

                    String mensaje = "Hola, hemos eliminado tu maqueta \"" + maqueta.getTitulo() +
                            "\" tras revisar una denuncia. \nMotivo: " + motivo +
                            "\n   " +
                            "\nPara cualquier consulta o reclamación, envíe un email a: box1coleccion@gmail.com";

                    String chatId = generarChatId(adminId, usuarioId);

                    Map<String, Object> mensajeData = new HashMap<>();
                    mensajeData.put("remitenteId", adminId);
                    mensajeData.put("texto", mensaje);
                    mensajeData.put("timestamp", System.currentTimeMillis());

                    db.collection("chats")
                            .document(chatId)
                            .collection("mensajes")
                            .add(mensajeData);

                    Map<String, Object> chatResumen = new HashMap<>();
                    chatResumen.put("usuarios", Arrays.asList(adminId, usuarioId));
                    chatResumen.put("ultimoMensaje", mensaje);
                    chatResumen.put("timestamp", System.currentTimeMillis());

                    db.collection("chats").document(chatId)
                            .set(chatResumen, SetOptions.merge());

                    db.collection("usuarios").document(usuarioId).get()
                            .addOnSuccessListener(usuarioDoc -> {
                                final String fotoUsuario = usuarioDoc.getString("fotoPerfilUrl");
                                final String nombreUsuario = usuarioDoc.getString("nombre");

                                db.collection("usuarios").document(adminId).get()
                                        .addOnSuccessListener(adminDoc -> {
                                            String fotoAdmin = adminDoc.getString("fotoPerfilUrl");
                                            String nombreAdmin = adminDoc.getString("nombre");

                                            if (fotoAdmin == null) fotoAdmin = "";
                                            if (nombreAdmin == null) nombreAdmin = "Administrador";

                                            DatabaseReference realtimeRef = realtimeDB.getReference("chats");

                                            Map<String, Object> chatDataAdmin = new HashMap<>();
                                            chatDataAdmin.put("nombre", nombreUsuario != null ? nombreUsuario : "Usuario");
                                            chatDataAdmin.put("fotoPerfilUrl", fotoUsuario != null ? fotoUsuario : "");
                                            chatDataAdmin.put("productoId", maqueta.getId());
                                            chatDataAdmin.put("productoTitulo", maqueta.getTitulo());
                                            chatDataAdmin.put("productoPrecio", maqueta.getPrecio());

                                            Map<String, Object> chatDataUsuario = new HashMap<>();
                                            chatDataUsuario.put("nombre", nombreAdmin);
                                            chatDataUsuario.put("fotoPerfilUrl", fotoAdmin);
                                            chatDataUsuario.put("productoId", maqueta.getId());
                                            chatDataUsuario.put("productoTitulo", maqueta.getTitulo());
                                            chatDataUsuario.put("productoPrecio", maqueta.getPrecio());

                                            realtimeRef.child(adminId).child(chatId).setValue(chatDataAdmin);
                                            realtimeRef.child(usuarioId).child(chatId).setValue(chatDataUsuario);
                                        });
                            });
                });
    }

    private String generarChatId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }
}
