package com.example.proyectojavalenguajemarcas.security;

import com.example.proyectojavalenguajemarcas.model.Usuario;
import java.sql.*;

public final class GameAccess {
    private GameAccess() {}

    public static boolean canDownload(Connection con, Usuario user, int id) throws SQLException {
        String sql = "SELECT j.id FROM juegos j WHERE j.id = ? AND (? = 'ADMIN' OR (j.id_editor = ? AND ? = 'EDITOR') OR "
                + "(j.estado = 'PUBLICADO' AND EXISTS (SELECT 1 FROM biblioteca b WHERE b.id_juego = j.id AND b.id_usuario = ?)))";
        return allowed(con, sql, user, id);
    }

    public static boolean canViewCover(Connection con, Usuario user, int id) throws SQLException {
        String sql = "SELECT j.id FROM juegos j WHERE j.id = ? AND (j.estado = 'PUBLICADO' OR ? = 'ADMIN' OR (j.id_editor = ? AND ? = 'EDITOR') OR "
                + "(j.estado = 'PUBLICADO' AND EXISTS (SELECT 1 FROM biblioteca b WHERE b.id_juego = j.id AND b.id_usuario = ?)))";
        return allowed(con, sql, user, id);
    }

    private static boolean allowed(Connection con, String sql, Usuario user, int id) throws SQLException {
        if (user == null || !AccessPolicy.validRole(user.getRol())) return false;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, user.getRol());
            ps.setInt(3, user.getId());
            ps.setString(4, user.getRol());
            ps.setInt(5, user.getId());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
}
