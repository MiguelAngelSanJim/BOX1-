package com.boxuno.vista;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.boxuno.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Registro#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Registro extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Registro() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Registro.
     */
    // TODO: Rename and change types and number of parameters
    public static Registro newInstance(String param1, String param2) {
        Registro fragment = new Registro();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String emailRecibido = getArguments().getString("emailNoRegistrado");

        EditText emailEditText = view.findViewById(R.id.emailRegistro);
        emailEditText.setText(emailRecibido);

        Button registrarse = view.findViewById(R.id.btn_registro);
        EditText contrasenia = view.findViewById(R.id.textContraseniaRegistro);
        EditText contraseniaConfirmada = view.findViewById(R.id.confirmpasswordRegistro);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        registrarse.setOnClickListener(v -> {
            if (contrasenia.getText().toString().isEmpty() || contraseniaConfirmada.getText().toString().isEmpty()) {
                builder.setTitle("Campos vacíos");
                builder.setMessage("Por favor, completa todos los campos antes de continuar.");
                builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                builder.show();
            } else if (!contrasenia.getText().toString().equals(contraseniaConfirmada.getText().toString())) {
                builder.setTitle("Error");
                builder.setMessage("Las contraseñas no coinciden.");
                builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                builder.show();
            }else{
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_registro_to_inicio);

            }
        });
    }

}