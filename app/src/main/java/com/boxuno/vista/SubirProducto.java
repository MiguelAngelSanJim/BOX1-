package com.boxuno.vista;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.boxuno.R;
import com.boxuno.adapter.ImagenCarruselAdapter;
import com.boxuno.modelo.Maqueta;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.*;

import android.provider.MediaStore;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;


import java.util.*;

public class SubirProducto extends Fragment {

    private EditText titulo, escala, precio, marca, descripcion;
    private Spinner spinnerCategoria, spinnerEstado;
    private Button btnPublicar, btnSubirImagen;
    private ViewPager2 viewPagerImagenes;
    private List<Object> imagenesSeleccionadas = new ArrayList<>();
    private ImagenCarruselAdapter carruselAdapter;
    private Uri imagenUriCamara;


    private final ActivityResultLauncher<Intent> seleccionarImagen = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        count = Math.min(count, 4);
                        for (int i = 0; i < count; i++) {
                            if (imagenesSeleccionadas.size() >= 4) break;
                            Uri imagenUri = result.getData().getClipData().getItemAt(i).getUri();
                            imagenesSeleccionadas.add(imagenUri);
                        }
                    } else if (result.getData().getData() != null) {
                        if (imagenesSeleccionadas.size() < 4) {
                            imagenesSeleccionadas.add(result.getData().getData());
                        }
                    }
                    carruselAdapter.notifyDataSetChanged();
                }
            });

    private final ActivityResultLauncher<Intent> hacerFoto = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && imagenUriCamara != null) {
                    if (imagenesSeleccionadas.size() < 4) {
                        imagenesSeleccionadas.add(imagenUriCamara);
                        carruselAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Solo puedes subir 4 imágenes", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> solicitarPermisoCamara = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    abrirCamara(); // El permiso fue aceptado, lanza la cámara
                } else {
                    Toast.makeText(getContext(), "Se necesita permiso de cámara para hacer fotos", Toast.LENGTH_SHORT).show();
                }
            }
    );


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subir_producto, container, false);

        titulo = view.findViewById(R.id.tituloProducto);
        escala = view.findViewById(R.id.escalaProducto);
        precio = view.findViewById(R.id.precioProducto);
        marca = view.findViewById(R.id.marcaProducto);
        descripcion = view.findViewById(R.id.descripcionProducto);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        spinnerEstado = view.findViewById(R.id.spinnerEstado);
        btnPublicar = view.findViewById(R.id.btn_publicar_producto);
        btnSubirImagen = view.findViewById(R.id.btn_subir_imagenes);
        viewPagerImagenes = view.findViewById(R.id.imagenesViewPager);

        carruselAdapter = new ImagenCarruselAdapter(getContext(), imagenesSeleccionadas);
        viewPagerImagenes.setAdapter(carruselAdapter);


        // Configurar el Spinner de Categoría.
        String[] categorias = {"Seleccione una opción...", "F1", "WRC", "Resistencia", "Otros"};
        ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categorias);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriaAdapter);

        // Configurar el Spinner de Estado.
        String[] estados = {"Seleccione una opción...", "Nuevo", "Semi-nuevo", "Usado", "Mal estado"};
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, estados);
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(estadoAdapter);

        btnSubirImagen.setOnClickListener(v -> mostrarDialogoSeleccionImagen());


        btnPublicar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Subiendo anuncio...", Toast.LENGTH_SHORT).show();
            subirProducto();
            btnPublicar.setClickable(false);
        });

        Log.d("SUBIR_PRODUCTO", "Fragment cargado correctamente");
        return view;
    }

    private void subirProducto() {
        String tituloStr = titulo.getText().toString().trim();
        String escalaStr = escala.getText().toString().trim();
        String precioStr = precio.getText().toString().trim();
        String marcaStr = marca.getText().toString().trim();
        String categoriaStr = spinnerCategoria.getSelectedItem().toString();
        String estadoStr = spinnerEstado.getSelectedItem().toString();
        String descripcionStr = descripcion.getText().toString().trim();

        if (TextUtils.isEmpty(tituloStr) || TextUtils.isEmpty(precioStr)) {
            Toast.makeText(getContext(), "Título y precio son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imagenesSeleccionadas.isEmpty()) {
            Toast.makeText(getContext(), "Debes añadir al menos una imagen", Toast.LENGTH_SHORT).show();
            return;
        }
        if (escalaStr.isEmpty()) {
            Toast.makeText(getContext(), "Por favor introduce una escala.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!escalaStr.matches("^\\d+\\s*[/|:]\\s*\\d+$")) {
            Toast.makeText(getContext(), "Formato de escala inválido. Ejemplos válidos: 1/43, 1:18, 1/5", Toast.LENGTH_LONG).show();
            return;
        }

        if (categoriaStr.equals("Seleccione una opción...") || estadoStr.equals("Seleccione una opción...")) {
            Toast.makeText(getContext(), "Debe seleccionar categoría y/o estado válidos", Toast.LENGTH_SHORT).show();
            return;
        }
        subirImagenesAFirebase(imagenesSeleccionadas, urls -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String maquetaId = db.collection("maquetas").document().getId();

            Maqueta maqueta = new Maqueta(
                    maquetaId,
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

            db.collection("maquetas")
                    .document(maquetaId)
                    .set(maqueta)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Maqueta publicada", Toast.LENGTH_LONG).show();

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            NavOptions navOptions = new NavOptions.Builder()
                                    .setPopUpTo(R.id.subirProducto, true)
                                    .build();

                            NavHostFragment.findNavController(SubirProducto.this).navigate(R.id.inicio, null, navOptions);
                        }, 2500);

                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show());
        });
    }

    private void subirImagenesAFirebase(List<Object> imagenes, ImagenesSubidasCallback callback) {
        List<String> urls = new ArrayList<>();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        for (Object imagenObj : imagenes) {
            if (imagenObj instanceof Uri) {
                Uri imagenUri = (Uri) imagenObj;
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
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File imagenArchivo;
            try {
                String nombreArchivo = "foto_" + System.currentTimeMillis();
                File directorio = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                imagenArchivo = File.createTempFile(nombreArchivo, ".jpg", directorio);
                imagenUriCamara = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".provider",
                        imagenArchivo
                );
            } catch (IOException e) {
                Toast.makeText(getContext(), "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUriCamara);
            hacerFoto.launch(intent);
        }
    }

    private void mostrarDialogoSeleccionImagen() {
        String[] opciones = {"Hacer foto", "Seleccionar de galería"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Selecciona una opción")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        solicitarPermisoCamara.launch(android.Manifest.permission.CAMERA);
                    } else {
                        abrirGaleria();
                    }
                })
                .show();
    }


    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        seleccionarImagen.launch(Intent.createChooser(intent, "Selecciona hasta 4 imágenes"));
    }


    private interface ImagenesSubidasCallback {
        void onFinalizado(List<String> urls);
    }
}
