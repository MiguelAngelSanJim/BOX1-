package com.boxuno.modelo;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String uid;
    private String nombre;
    private String email;
    private String imagenPerfil;
    private List<String> favoritos; // IDs de productos

    public Usuario() {
        favoritos = new ArrayList<>();
    }

    public Usuario(String uid, String nombre, String email, String imagenPerfil) {
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
        this.imagenPerfil = imagenPerfil;
        this.favoritos = new ArrayList<>();
    }

    // Getters y setters

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImagenPerfil() {
        return imagenPerfil;
    }

    public void setImagenPerfil(String imagenPerfil) {
        this.imagenPerfil = imagenPerfil;
    }

    public List<String> getFavoritos() {
        return favoritos;
    }

    public void setFavoritos(List<String> favoritos) {
        this.favoritos = favoritos;
    }
}

