package com.example.proyectojavalenguajemarcas.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JuegoDAO {


    public List<Juego> obtenerJuegosPublicados() {
        List<Juego> listaJuegos = new ArrayList<>();


        String sql = "SELECT * FROM juegos WHERE estado = 'PUBLICADO' ORDER BY fecha_subida DESC";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {


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
        return listaJuegos;
    }


    public boolean insertarJuego(Juego juego) {

        String sql = "INSERT INTO juegos (titulo, descripcion, archivo_zip_url, estado, id_editor) VALUES (?, ?, ?, 'PENDIENTE', ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {


            ps.setString(1, juego.getTitulo());
            ps.setString(2, juego.getDescripcion());
            ps.setString(3, juego.getArchivoZipUrl());
            ps.setInt(4, juego.getIdEditor());


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
                            rs.getInt("id"), rs.getString("titulo"), rs.getString("descripcion"),
                            rs.getDouble("precio"), rs.getString("caratula_url"), rs.getString("archivo_zip_url"),
                            rs.getString("estado"), rs.getInt("id_editor"), rs.getTimestamp("fecha_subida")
                    );
                    listaJuegos.add(juego);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaJuegos;
    }


    public List<Juego> obtenerJuegosPendientes() {
        List<Juego> listaJuegos = new ArrayList<>();

        String sql = "SELECT * FROM juegos WHERE estado = 'PENDIENTE' ORDER BY fecha_subida DESC";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaJuegos.add(new Juego(
                        rs.getInt("id"), rs.getString("titulo"), rs.getString("descripcion"),
                        rs.getDouble("precio"), rs.getString("caratula_url"), rs.getString("archivo_zip_url"),
                        rs.getString("estado"), rs.getInt("id_editor"), rs.getTimestamp("fecha_subida")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaJuegos;
    }


    public Juego obtenerJuegoPorId(int id) {

        String sql = "SELECT * FROM juegos WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new Juego(
                            rs.getInt("id"), rs.getString("titulo"), rs.getString("descripcion"),
                            rs.getDouble("precio"), rs.getString("caratula_url"), rs.getString("archivo_zip_url"),
                            rs.getString("estado"), rs.getInt("id_editor"), rs.getTimestamp("fecha_subida")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean actualizarEstadoJuego(int id, String estado) {

        String sql = "UPDATE juegos SET estado = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {


            ps.setString(1, estado);
            ps.setInt(2, id);

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
        String sql = "INSERT IGNORE INTO biblioteca (id_usuario, id_juego) VALUES (?, ?)";
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
                while (rs.next()) {
                    lista.add(new Juego(rs.getInt("id"), rs.getString("titulo"), rs.getString("descripcion"),
                            rs.getDouble("precio"), rs.getString("caratula_url"), rs.getString("archivo_zip_url"),
                            rs.getString("estado"), rs.getInt("id_editor"), rs.getTimestamp("fecha_subida")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}