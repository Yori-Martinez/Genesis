package com.example.proyectojavalenguajemarcas.security;

import com.example.proyectojavalenguajemarcas.model.ConexionBD;
import java.nio.file.*;
import java.sql.*;

public final class DatabaseFixture {
    private DatabaseFixture() {}
    public static Connection connection() throws SQLException {
        String db = System.getenv("MYSQLDATABASE");
        if (db == null || !db.endsWith("_test") || !"development".equals(System.getenv("APP_ENV"))) {
            throw new IllegalStateException("Integration tests require APP_ENV=development and a disposable database ending in _test.");
        }
        return ConexionBD.getConnection();
    }

    public static void reset() throws Exception {
        try (Connection con = connection(); Statement st = con.createStatement()) {
            st.executeUpdate("DROP TABLE IF EXISTS GENESIS_SESSION_ATTRIBUTES");
            st.executeUpdate("DROP TABLE IF EXISTS GENESIS_SESSION");
            st.executeUpdate("DROP TABLE IF EXISTS genesis_rate_limits");
            st.executeUpdate("DROP TABLE IF EXISTS biblioteca");
            st.executeUpdate("DROP TABLE IF EXISTS juegos");
            st.executeUpdate("DROP TABLE IF EXISTS usuarios");
            String sql = Files.readString(Path.of("init.sql"));
            for (String statement : sql.split(";")) {
                if (!statement.isBlank() && !statement.stripLeading().startsWith("CREATE DATABASE") && !statement.stripLeading().startsWith("USE ")) {
                    st.executeUpdate(statement);
                }
            }
        }
    }

    public static void seed() throws Exception {
        reset();
        try (Connection con = connection(); Statement st = con.createStatement();
             PreparedStatement insert = con.prepareStatement("INSERT INTO usuarios (id, username, email, password_hash, rol) VALUES (?, ?, ?, ?, ?)")) {
            String[] names = {"player", "editor", "othereditor", "admin", "outsider"};
            String[] roles = {"JUGADOR", "EDITOR", "EDITOR", "ADMIN", "JUGADOR"};
            String hash = Passwords.hash("Test password with spaces ");
            for (int i=0; i<names.length; i++) {
                insert.setInt(1,i+1); insert.setString(2,names[i]); insert.setString(3,names[i]+"@example.com");
                insert.setString(4,hash); insert.setString(5,roles[i]); insert.executeUpdate();
            }
            st.executeUpdate("INSERT INTO juegos (id,titulo,descripcion,estado,id_editor,zip_contenido,caratula_contenido) VALUES "
                    + "(10,'Published','Description','PUBLICADO',2,X'504B0304',X'89504E47'),"
                    + "(11,'Pending','Description','PENDIENTE',2,X'504B0304',X'89504E47'),"
                    + "(12,'Rejected','Description','RECHAZADO',3,X'504B0304',X'89504E47')");
            st.executeUpdate("INSERT INTO biblioteca (id_usuario,id_juego) VALUES (1,10)");
        }
    }

    public static String scalar(String sql) throws SQLException {
        try (Connection con=connection(); Statement st=con.createStatement(); ResultSet rs=st.executeQuery(sql)) {
            rs.next(); return rs.getString(1);
        }
    }
}
