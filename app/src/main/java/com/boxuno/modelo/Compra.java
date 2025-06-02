package com.boxuno.modelo;

public class Compra {
    private String nombreUsuario;
    private String nombreProducto;
    private double precio;
    private String fecha;

    public Compra() {
    }

    public Compra(String nombreUsuario, String nombreProducto, double precio, String fecha) {
        this.nombreUsuario = nombreUsuario;
        this.nombreProducto = nombreProducto;
        this.precio = precio;
        this.fecha = fecha;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public double getPrecio() {
        return precio;
    }

    public String getFecha() {
        return fecha;
    }
}
