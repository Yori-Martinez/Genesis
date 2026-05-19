package com.example.proyectojavalenguajemarcas.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.example.proyectojavalenguajemarcas.model.ConexionBD;

public class UsuarioDAO {

    public Usuario validarLogin(String username, String password) {
        // Hacemos una consulta segura para evitar Inyección SQL
        String sql = "SELECT id, username, email, rol FROM usuarios WHERE username = ? AND password = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Si hay resultado, el usuario existe y la contraseña es correcta
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("rol")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Retorna null si falla el login
    }
}