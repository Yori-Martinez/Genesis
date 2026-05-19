package com.example.proyectojavalenguajemarcas.model;

public class Usuario {
    private int id;
    private String username;
    private String email;
    private String rol;

    public Usuario(int id, String username, String email, String rol) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.rol = rol;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
}