package com.example.proyectojavalenguajemarcas.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JuegoDAO {

    // Método para la Tienda: Solo trae los juegos aprobados (PUBLICADOS)
    public List<Juego> obtenerJuegosPublicados() {
        List<Juego> listaJuegos = new ArrayList<>();

        // La consulta SQL: Trae todos los juegos publicados ordenados por los más recientes
        String sql = "SELECT * FROM juegos WHERE estado = 'PUBLICADO' ORDER BY fecha_subida DESC";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorremos los resultados que nos devuelve MySQL
            while (rs.next()) {
                Juego juego = new Juego(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        rs.getString("caratula_url"),
                        rs.getString("archivo_zip_url"),
                        rs.getString("estado"),
                        rs.getInt("id_editor"),
                        rs.getTimestamp("fecha_subida")
                );
                listaJuegos.add(juego);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaJuegos; // Devolvemos la lista llena (o vacía si no hay juegos)
    }
    // Método para el Editor: Inserta un nuevo juego en estado PENDIENTE
    public boolean insertarJuego(Juego juego) {
        // Fíjate que forzamos el estado 'PENDIENTE' para que no salga en la tienda automáticamente
        String sql = "INSERT INTO juegos (titulo, descripcion, archivo_zip_url, estado, id_editor) VALUES (?, ?, ?, 'PENDIENTE', ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, juego.getTitulo());
            ps.setString(2, juego.getDescripcion());
            ps.setString(3, juego.getArchivoZipUrl());
            ps.setInt(4, juego.getIdEditor()); // El ID del usuario que lo está subiendo

            // executeUpdate devuelve el número de filas afectadas. Si es > 0, se guardó bien.
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Juego> obtenerJuegosPorEditor(int idEditor) {
        List<Juego> listaJuegos = new ArrayList<>();
        String sql = "SELECT * FROM juegos WHERE id_editor = ? ORDER BY fecha_subida DESC";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEditor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Juego juego = new Juego(
                            rs.getInt("id"),
                            rs.getString("titulo"),
                            rs.getString("descripcion"),
                            rs.getDouble("precio"),
                            rs.getString("caratula_url"),
                            rs.getString("archivo_zip_url"),
                            rs.getString("estado"),
                            rs.getInt("id_editor"),
                            rs.getTimestamp("fecha_subida")
                    );
                    listaJuegos.add(juego);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaJuegos;
    }
}