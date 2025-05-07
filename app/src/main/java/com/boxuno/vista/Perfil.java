package com.boxuno.vista;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.boxuno.R;
import com.boxuno.adapter.MaquetaAdapter;
import com.boxuno.modelo.Maqueta;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class Perfil extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView fotoPerfil;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String userId;
    private ActivityResultLauncher<Intent> imagePickerLauncher;


    public Perfil() {
        // Required empty public constructor
    }

    private void confirmar() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setIcon(R.drawable.logopng)
                .setPositiveButton("Sí", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();

                    SharedPreferences prefs = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("recordar", false).apply();

                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.login);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
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

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        ImageView fotoperfil = view.findViewById(R.id.imagenUserPerfil);
        List<Maqueta> maquetaList = new ArrayList<>();
        MaquetaAdapter adapter = new MaquetaAdapter(maquetaList, getContext(), maqueta -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("maqueta", maqueta);
            NavHostFragment.findNavController(Perfil.this).navigate(R.id.detalleProducto, bundle);
        }, false, true);
        recyclerView.setAdapter(adapter);

        Glide.with(this)
                .load(R.drawable.imagenpordefecto)
                .circleCrop()
                .into(fotoperfil);

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
}