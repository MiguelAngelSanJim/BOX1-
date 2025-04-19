package com.boxuno.modelo;

public class Mensaje {
    private String remitenteId;       // UID del que envía el mensaje.
    private String destinatarioId;    // UID del que recibe el mensaje.
    private String contenido;         // Texto del mensaje.
    private long timestamp;           // Fecha de envío del mensaje.

    public Mensaje() {}

    public Mensaje(String remitenteId, String destinatarioId, String contenido, long timestamp) {
        this.remitenteId = remitenteId;
        this.destinatarioId = destinatarioId;
        this.contenido = contenido;
        this.timestamp = timestamp;
    }

    public String getRemitenteId() {
        return remitenteId;
    }

    public void setRemitenteId(String remitenteId) {
        this.remitenteId = remitenteId;
    }

    public String getDestinatarioId() {
        return destinatarioId;
    }

    public void setDestinatarioId(String destinatarioId) {
        this.destinatarioId = destinatarioId;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}