package com.boxuno.vista;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.boxuno.R;
import com.boxuno.adapter.MaquetaAdapter;
import com.boxuno.modelo.Maqueta;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class Inicio extends Fragment {
    private List<Maqueta> maquetaList;
    private MaquetaAdapter maquetaAdapter;

    public Inicio() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Inicio.
     */
    // TODO: Rename and change types and number of parameters
    public static Inicio newInstance(String param1, String param2) {
        Inicio fragment = new Inicio();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewinicio);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columnas
        maquetaList = new ArrayList<>();
        maquetaAdapter = new MaquetaAdapter(maquetaList, getContext(), maqueta -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("maqueta", maqueta);

            NavHostFragment.findNavController(Inicio.this).navigate(R.id.action_inicio_to_detalle_producto, bundle);
        });
        recyclerView.setAdapter(maquetaAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("maquetas").whereNotEqualTo("usuarioId", uidActual).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Maqueta maqueta = doc.toObject(Maqueta.class);
                        if (maqueta == null) continue;

                        String userId = maqueta.getUsuarioId();

                        db.collection("usuarios").document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        maqueta.setNombreUsuario(userDoc.getString("nombre"));
                                    } else {
                                        maqueta.setNombreUsuario("Desconocido");
                                    }
                                    maquetaList.add(maqueta);
                                    maquetaAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    maqueta.setNombreUsuario("Error");
                                    maquetaList.add(maqueta);
                                    maquetaAdapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar maquetas", Toast.LENGTH_SHORT).show();
                });



        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomnavigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }


}