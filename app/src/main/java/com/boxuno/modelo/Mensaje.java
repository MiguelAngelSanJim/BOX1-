package com.boxuno.modelo;

public class Mensaje {
    private String autorId;
    private String texto;
    private long timestamp;

    public Mensaje() {} // Necesario para Firestore

    public Mensaje(String autorId, String texto, long timestamp) {
        this.autorId = autorId;
        this.texto = texto;
        this.timestamp = timestamp;
    }

    public String getAutorId() { return autorId; }
    public void setAutorId(String autorId) { this.autorId = autorId; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
