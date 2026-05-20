package com.example.proyectojavalenguajemarcas.model;

import java.sql.Timestamp;

public class Juego {
    private int id;
    private String titulo;
    private String descripcion;
    private double precio;
    private String caratulaUrl;
    private String archivoZipUrl;
    private String estado;
    private int idEditor;
    private Timestamp fechaSubida;


    public Juego() {
    }


    public Juego(int id, String titulo, String descripcion, double precio, String caratulaUrl,
                 String archivoZipUrl, String estado, int idEditor, Timestamp fechaSubida) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.caratulaUrl = caratulaUrl;
        this.archivoZipUrl = archivoZipUrl;
        this.estado = estado;
        this.idEditor = idEditor;
        this.fechaSubida = fechaSubida;
    }


    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public String getCaratulaUrl() {
        return caratulaUrl;
    }

    public String getArchivoZipUrl() {
        return archivoZipUrl;
    }

    public String getEstado() {
        return estado;
    }

    public int getIdEditor() {
        return idEditor;
    }

    public Timestamp getFechaSubida() {
        return fechaSubida;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setCaratulaUrl(String caratulaUrl) {
        this.caratulaUrl = caratulaUrl;
    }

    public void setArchivoZipUrl(String archivoZipUrl) {
        this.archivoZipUrl = archivoZipUrl;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setIdEditor(int idEditor) {
        this.idEditor = idEditor;
    }

    public void setFechaSubida(Timestamp fechaSubida) {
        this.fechaSubida = fechaSubida;
    }
}