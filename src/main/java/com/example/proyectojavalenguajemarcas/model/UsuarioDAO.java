package com.example.proyectojavalenguajemarcas.model;

import com.example.proyectojavalenguajemarcas.security.*;
import java.sql.*;

public class UsuarioDAO {
    public record Account(Usuario user, String hash, boolean blocked, int version) {}
    public enum Registration { CREATED, DUPLICATE }
    private static Account map(ResultSet rs) throws SQLException {
        return new Account(new Usuario(rs.getInt("id"), rs.getString("username"), rs.getString("email"), rs.getString("rol")),
                rs.getString("password_hash"), rs.getBoolean("credencial_bloqueada"), rs.getInt("auth_version"));
    }

    public Account findById(int id) throws SQLException {
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, username, email, rol, password_hash, credencial_bloqueada, auth_version FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public Account validarLogin(String username, String password) throws SQLException {
        Account account;
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, username, email, rol, password_hash, credencial_bloqueada, auth_version FROM usuarios WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { account = rs.next() ? map(rs) : null; }
        }
        boolean correct = Passwords.verify(password, account == null ? null : account.hash());
        return correct && account != null && !account.blocked() && AccessPolicy.validRole(account.user().getRol()) ? account : null;
    }

    public Registration registrarUsuario(String username, String email, String password) throws SQLException {
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO usuarios (username, email, password_hash, rol) VALUES (?, ?, ?, 'JUGADOR')")) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, Passwords.hash(password));
            ps.executeUpdate();
            return Registration.CREATED;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) return Registration.DUPLICATE;
            throw e;
        }
    }
}
