package com.boxuno.modelo;

import java.util.ArrayList;
import java.util.List;

public class Maqueta {
    private String id;                  // ID del documento.
    private String titulo;
    private String escala;
    private String descripcion;
    private double precio;
    private String marca;
    private String categoria;
    private String estado;             // "Nuevo", "Usado", etc.
    private List<String> imagenes;     // URLs a Firebase Storage.
    private String usuarioId;          // UID del usuario que publica.
    private boolean vendido;
    private long timestamp;


    public Maqueta() {
        imagenes = new ArrayList<>();
    }

    public Maqueta(String id, String titulo, String escala, String descripcion, double precio,
                   String marca, String categoria, String estado, List<String> imagenes,
                   String usuarioId, boolean vendido, long timestamp) {
        this.id = id;
        this.titulo = titulo;
        this.escala = escala;
        this.descripcion = descripcion;
        this.precio = precio;
        this.marca = marca;
        this.categoria = categoria;
        this.estado = estado;
        this.imagenes = (imagenes != null) ? imagenes : new ArrayList<>();
        this.usuarioId = usuarioId;
        this.vendido = vendido;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getEscala() {
        return escala;
    }

    public void setEscala(String escala) {
        this.escala = escala;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = (imagenes != null) ? imagenes : new ArrayList<>();
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public boolean isVendido() {
        return vendido;
    }

    public void setVendido(boolean vendido) {
        this.vendido = vendido;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
