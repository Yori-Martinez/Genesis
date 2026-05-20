package com.example.proyectojavalenguajemarcas.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JuegoDAO {

    // =========================================================================
    // MÉTODO 1: PARA LA TIENDA (Solo muestra los aprobados)
    // =========================================================================
    public List<Juego> obtenerJuegosPublicados() {
        List<Juego> listaJuegos = new ArrayList<>();

        // Consulta SQL: Busca todos los juegos, pero FILTRA (WHERE) solo los que tengan estado 'PUBLICADO'
        String sql = "SELECT * FROM juegos WHERE estado = 'PUBLICADO' ORDER BY fecha_subida DESC";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // rs.next() va fila por fila en los resultados de MySQL
            while (rs.next()) {
                // Creamos un objeto 'Juego' en Java y lo rellenamos con los datos de esa fila de MySQL
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
                // Metemos ese juego en nuestra lista
                listaJuegos.add(juego);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaJuegos; // Devolvemos la lista llena de juegos
    }

    // =========================================================================
    // MÉTODO 2: PARA EL EDITOR - SUBIR JUEGO (CRUD: Create)
    // =========================================================================
    public boolean insertarJuego(Juego juego) {
        // Consulta SQL: Inserta los datos. Fíjate que el estado lo forzamos a 'PENDIENTE' directamente en el SQL
        String sql = "INSERT INTO juegos (titulo, descripcion, archivo_zip_url, estado, id_editor) VALUES (?, ?, ?, 'PENDIENTE', ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Sustituimos las interrogaciones (?) del SQL por los datos reales del objeto 'juego'
            ps.setString(1, juego.getTitulo());
            ps.setString(2, juego.getDescripcion());
            ps.setString(3, juego.getArchivoZipUrl());
            ps.setInt(4, juego.getIdEditor()); // El ID del usuario que está subiendo el juego

            // executeUpdate() ejecuta el INSERT. Devuelve el número de filas afectadas.
            // Si es mayor que 0, significa que se guardó correctamente.
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false; // Si falla, devolvemos false
        }
    }

    // =========================================================================
    // MÉTODO 3: PARA "MIS PUBLICACIONES" (Muestra los juegos de un usuario específico)
    // =========================================================================
    public List<Juego> obtenerJuegosPorEditor(int idEditor) {
        List<Juego> listaJuegos = new ArrayList<>();
        // Consulta SQL: Busca todos los juegos FILTRANDO por el ID del editor que se lo pide
        String sql = "SELECT * FROM juegos WHERE id_editor = ? ORDER BY fecha_subida DESC";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Ponemos el ID del editor en la interrogación (?)
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

    // =========================================================================
    // MÉTODO 4: PARA EL PANEL ADMIN (Solo muestra los que esperan revisión)
    // =========================================================================
    public List<Juego> obtenerJuegosPendientes() {
        List<Juego> listaJuegos = new ArrayList<>();
        // Consulta SQL: Busca juegos FILTRADOS por el estado 'PENDIENTE'
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

    // =========================================================================
    // MÉTODO 5: PARA EL PANEL ADMIN (Trae los datos de UN solo juego por su ID)
    // =========================================================================
    public Juego obtenerJuegoPorId(int id) {
        // Consulta SQL: Busca una sola fila que coincida con el ID
        String sql = "SELECT * FROM juegos WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                // Como solo esperamos UN resultado, usamos 'if' en vez de 'while'
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
        return null; // Si no encuentra el juego, devuelve nulo
    }

    // =========================================================================
    // MÉTODO 6: PARA EL PANEL ADMIN (Cambia el estado a PUBLICADO o RECHAZADO - CRUD: Update)
    // =========================================================================
    public boolean actualizarEstadoJuego(int id, String estado) {
        // Consulta SQL: Actualiza (UPDATE) la columna 'estado' del juego que tenga ese 'id'
        String sql = "UPDATE juegos SET estado = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Rellenamos las dos interrogaciones (?)
            ps.setString(1, estado); // Puede ser "PUBLICADO" o "RECHAZADO"
            ps.setInt(2, id);

            return ps.executeUpdate() > 0; // Si afectó a alguna fila, devuelve true

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // MÉTODO 7: PARA "MIS PUBLICACIONES" (Permite al editor borrar su juego - CRUD: Delete)
    // =========================================================================
    public boolean eliminarJuego(int idJuego, int idEditor) {
        // Consulta SQL: Borra (DELETE) de la tabla el juego que coincida con ese ID
        // ADEMÁS, verificamos que el id_editor coincida. Esto es pura seguridad para que un
        // editor no pueda borrar el juego de otro editor alterando los IDs en el navegador.
        String sql = "DELETE FROM juegos WHERE id = ? AND id_editor = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idJuego);
            ps.setInt(2, idEditor);

            return ps.executeUpdate() > 0; // Devuelve true si el borrado tuvo éxito

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