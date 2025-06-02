package com.boxuno.modelo;

import com.google.firebase.firestore.PropertyName;

public class Mensaje {
    @PropertyName("remitenteId")
    private String autorId;

    @PropertyName("uidDestinatario")
    private String uidDestinatario;

    private String texto;
    private long timestamp;

    public Mensaje() {} // Necesario para Firestore

    public Mensaje(String autorId, String uidDestinatario, String texto, long timestamp) {
        this.autorId = autorId;
        this.uidDestinatario = uidDestinatario;
        this.texto = texto;
        this.timestamp = timestamp;
    }

    @PropertyName("remitenteId")
    public String getAutorId() { return autorId; }
    public void setAutorId(String autorId) { this.autorId = autorId; }

    @PropertyName("uidDestinatario")
    public String getUidDestinatario() { return uidDestinatario; }
    public void setUidDestinatario(String uidDestinatario) { this.uidDestinatario = uidDestinatario; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
