package com.example.proyectojavalenguajemarcas.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioDAO {

    // =========================================================================
    // MÉTODO 1: VALIDAR LOGIN (Busca si el usuario y la contraseña coinciden)
    // =========================================================================
    public Usuario validarLogin(String username, String password) {
        // Hacemos una consulta segura para evitar Inyección SQL
        String sql = "SELECT id, username, email, rol FROM usuarios WHERE username = ? AND password = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Sustituimos las interrogaciones por los datos que ha escrito el usuario
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Si hay resultado, el usuario existe y la contraseña es correcta.
                    // Creamos el objeto Usuario en Java y lo devolvemos.
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
        return null; // Retorna null si falla el login o no existe el usuario
    }

    // =========================================================================
    // MÉTODO 2: REGISTRAR USUARIO (Crea una nueva cuenta en la base de datos)
    // =========================================================================
    public boolean registrarUsuario(String username, String email, String password) {
        // Consulta SQL: Inserta los datos. Forzamos que todo el que se registre sea 'JUGADOR' por defecto
        String sql = "INSERT INTO usuarios (username, email, password, rol) VALUES (?, ?, ?, 'JUGADOR')";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Sustituimos las interrogaciones por los datos del formulario de registro
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);

            // executeUpdate devuelve el número de filas insertadas. Si es > 0, fue un éxito.
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false; // Si explota (ej: el email ya existe), devolvemos false
        }
    }
}