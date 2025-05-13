package com.boxuno.modelo;

public class ChatPreview {
    private String uid;
    private String nombre;
    private String fotoPerfilUrl;
    private String productoId;
    private String productoTitulo;
    private double productoPrecio;
    private String chatId;

    public ChatPreview() {}

    public ChatPreview(String uid, String nombre, String fotoPerfilUrl,
                       String productoId, String productoTitulo, double productoPrecio, String chatId) {
        this.uid = uid;
        this.nombre = nombre;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.productoId = productoId;
        this.productoTitulo = productoTitulo;
        this.productoPrecio = productoPrecio;
        this.chatId = chatId;
    }

    public String getUid() { return uid; }
    public String getNombre() { return nombre; }
    public String getFotoPerfilUrl() { return fotoPerfilUrl; }

    public String getProductoId() {
        return productoId;
    }

    public String getProductoTitulo() {
        return productoTitulo;
    }

    public double getProductoPrecio() {
        return productoPrecio;
    }

    public String getChatId() {
        return chatId;
    }
}
