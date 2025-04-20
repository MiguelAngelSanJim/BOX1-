package com.boxuno.vista;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boxuno.R;
import com.boxuno.adapter.ImagenesAdapter;
import com.boxuno.modelo.Maqueta;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.*;

import java.util.*;

public class SubirProducto extends Fragment {

    private EditText titulo, escala, precio, marca, categoria, estado, descripcion;
    private Button btnPublicar, btnSubirImagen;
    private RecyclerView recyclerImagenes;
    private List<Uri> imagenesSeleccionadas = new ArrayList<>();
    private ImagenesAdapter imagenesAdapter;

    private final ActivityResultLauncher<Intent> seleccionarImagen = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imagen = result.getData().getData();
                    imagenesSeleccionadas.add(imagen);
                    imagenesAdapter.notifyDataSetChanged();
                }
            });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subir_producto, container, false);

        titulo = view.findViewById(R.id.tituloProducto);
        escala = view.findViewById(R.id.escalaProducto);
        precio = view.findViewById(R.id.precioProducto);
        marca = view.findViewById(R.id.marcaProducto);
        categoria = view.findViewById(R.id.categoriaProducto);
        estado = view.findViewById(R.id.estadoProducto);
        descripcion = view.findViewById(R.id.descripcionProducto);
        btnPublicar = view.findViewById(R.id.btn_publicar_producto);
        btnSubirImagen = view.findViewById(R.id.btn_subir_imagenes);
        recyclerImagenes = view.findViewById(R.id.imagenesRecyclerView);

        imagenesAdapter = new ImagenesAdapter(getContext(),imagenesSeleccionadas);
        recyclerImagenes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerImagenes.setAdapter(imagenesAdapter);

        btnSubirImagen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            seleccionarImagen.launch(intent);
        });

        btnPublicar.setOnClickListener(v -> subirProducto());
        Log.d("SUBIR_PRODUCTO", "Fragment cargado correctamente");

        return view;
    }

    private void subirProducto() {
        String tituloStr = titulo.getText().toString().trim();
        String escalaStr = escala.getText().toString().trim();
        String precioStr = precio.getText().toString().trim();
        String marcaStr = marca.getText().toString().trim();
        String categoriaStr = categoria.getText().toString().trim();
        String estadoStr = estado.getText().toString().trim();
        String descripcionStr = descripcion.getText().toString().trim();

        if (TextUtils.isEmpty(tituloStr) || TextUtils.isEmpty(precioStr)) {
            Toast.makeText(getContext(), "Título y precio son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imagenesSeleccionadas.isEmpty()) {
            Toast.makeText(getContext(), "Debes añadir al menos una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        subirImagenesAFirebase(imagenesSeleccionadas, urls -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Maqueta maqueta = new Maqueta(
                    null,
                    tituloStr,
                    escalaStr,
                    descripcionStr,
                    Double.parseDouble(precioStr),
                    marcaStr,
                    categoriaStr,
                    estadoStr,
                    urls,
                    userId,
                    false,
                    System.currentTimeMillis()
            );

            FirebaseFirestore.getInstance()
                    .collection("maquetas")
                    .add(maqueta)
                    .addOnSuccessListener(doc -> Toast.makeText(getContext(), "Maqueta publicada", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show());
        });
    }

    private void subirImagenesAFirebase(List<Uri> imagenes, ImagenesSubidasCallback callback) {
        List<String> urls = new ArrayList<>();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        for (Uri imagenUri : imagenes) {
            String nombreArchivo = "imagenes/" + UUID.randomUUID().toString();
            StorageReference ref = storage.getReference().child(nombreArchivo);

            ref.putFile(imagenUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) throw task.getException();
                        return ref.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {
                        urls.add(uri.toString());
                        if (urls.size() == imagenes.size()) {
                            callback.onFinalizado(urls);
                        }
                    });
        }
    }

    private interface ImagenesSubidasCallback {
        void onFinalizado(List<String> urls);
    }
}