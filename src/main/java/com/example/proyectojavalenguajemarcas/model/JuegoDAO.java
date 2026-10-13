package com.example.proyectojavalenguajemarcas.model;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JuegoDAO {


    private Juego mapearJuego(ResultSet rs) throws Exception {
        return new Juego(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("descripcion"),
                rs.getDouble("precio"),
                "api/caratula?id=" + rs.getInt("id"),
                "api/zip?id=" + rs.getInt("id"),
                rs.getString("estado"),
                rs.getInt("id_editor"),
                rs.getTimestamp("fecha_subida")
        );
    }

    public List<Juego> obtenerJuegosPublicados() {
        List<Juego> lista = new ArrayList<>();
        String sql = "SELECT * FROM juegos WHERE estado = 'PUBLICADO' ORDER BY fecha_subida DESC";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearJuego(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Juego> obtenerJuegosPorEditor(int idEditor) {
        List<Juego> lista = new ArrayList<>();
        String sql = "SELECT * FROM juegos WHERE id_editor = ? ORDER BY fecha_subida DESC";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEditor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearJuego(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Juego> obtenerJuegosPendientes() {
        List<Juego> lista = new ArrayList<>();
        String sql = "SELECT * FROM juegos WHERE estado = 'PENDIENTE' ORDER BY fecha_subida DESC";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearJuego(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Juego obtenerJuegoPorId(int id) {
        String sql = "SELECT * FROM juegos WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearJuego(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean insertarJuegoConZip(String titulo, String descripcion, int idEditor, InputStream zipStream) {
        String sql = "INSERT INTO juegos (titulo, descripcion, estado, id_editor, zip_contenido) VALUES (?, ?, 'PENDIENTE', ?, ?)";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, titulo);
            ps.setString(2, descripcion);
            ps.setInt(3, idEditor);
            ps.setBinaryStream(4, zipStream);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean guardarArchivosYPublicar(int idJuego, InputStream caratulaStream) {
        String sql = "UPDATE juegos SET estado = 'PUBLICADO', caratula_contenido = ? WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBinaryStream(1, caratulaStream);
            ps.setInt(2, idJuego);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean actualizarEstadoJuego(int idJuego, String estado) {
        String sql = "UPDATE juegos SET estado = ? WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idJuego);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarJuego(int idJuego, int idEditor) {
        String sql = "DELETE FROM juegos WHERE id = ? AND id_editor = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idJuego);
            ps.setInt(2, idEditor);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean agregarABiblioteca(int idUsuario, int idJuego) {
        String sql = "INSERT IGNORE INTO biblioteca (id_usuario, id_juego) SELECT ?, id FROM juegos WHERE id = ? AND estado = 'PUBLICADO'";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idJuego);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Juego> obtenerJuegosBiblioteca(int idUsuario) {
        List<Juego> lista = new ArrayList<>();
        String sql = "SELECT j.* FROM juegos j INNER JOIN biblioteca b ON j.id = b.id_juego WHERE b.id_usuario = ? ORDER BY b.fecha_adquisicion DESC";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearJuego(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}