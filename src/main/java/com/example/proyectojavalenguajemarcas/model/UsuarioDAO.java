package com.example.proyectojavalenguajemarcas.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioDAO {


    public Usuario validarLogin(String username, String password) {

        String sql = "SELECT id, username, email, rol FROM usuarios WHERE username = ? AND password = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {


            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {


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
        return null;
    }


    public boolean registrarUsuario(String username, String email, String password) {

        String sql = "INSERT INTO usuarios (username, email, password, rol) VALUES (?, ?, ?, 'JUGADOR')";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {


            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);


            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}