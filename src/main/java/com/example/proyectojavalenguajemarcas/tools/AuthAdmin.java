package com.example.proyectojavalenguajemarcas.tools;

import com.example.proyectojavalenguajemarcas.model.ConexionBD;
import com.example.proyectojavalenguajemarcas.security.*;
import java.io.Console;
import java.sql.*;
import java.util.*;

public final class AuthAdmin {
    private AuthAdmin() {}

    public static void main(String[] args) {
        try (Connection con = ConexionBD.getConnection()) {
            if (args.length == 1 && args[0].equals("migrate")) {
                migrate(con);
                System.out.println("Migración verificada; se conservaron cuentas y relaciones.");
            } else if (args.length == 3 && args[0].equals("set-role")) {
                setRole(con, args[1], args[2]);
                System.out.println("Rol actualizado; las sesiones anteriores quedan revocadas.");
            } else if (args.length == 2 && args[0].equals("set-password")) {
                Console console = System.console();
                if (console == null) throw new IllegalArgumentException("Ejecuta Java directamente en un terminal para introducir la contraseña oculta.");
                char[] password = console.readPassword("Nueva contraseña (15–128 caracteres): ");
                char[] repeat = console.readPassword("Repite la contraseña: ");
                try {
                    if (password == null || repeat == null || !Arrays.equals(password, repeat)) throw new IllegalArgumentException("Las contraseñas no coinciden.");
                    setPassword(con, args[1], new String(password));
                    System.out.println("Credencial renovada y desbloqueada; sesiones anteriores revocadas.");
                } finally {
                    if (password != null) Arrays.fill(password, '\0');
                    if (repeat != null) Arrays.fill(repeat, '\0');
                }
            } else {
                throw new IllegalArgumentException("Uso: migrate | set-role USUARIO JUGADOR/EDITOR/ADMIN | set-password USUARIO");
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("Operación interrumpida. Comprueba la configuración, el esquema y los permisos de MySQL. No se deben reactivar credenciales antiguas.");
            System.exit(1);
        }
    }

    private static boolean column(Connection con, String name) throws SQLException {
        try (ResultSet rs = con.getMetaData().getColumns(con.getCatalog(), null, "usuarios", name)) { return rs.next(); }
    }

    public static void migrate(Connection con) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet lock = st.executeQuery("SELECT GET_LOCK('genesis_auth_migration', 0)")) {
            if (!lock.next() || lock.getInt(1) != 1) throw new SQLException("Otra migración está en curso.");
        }
        try {
            try (Statement st = con.createStatement()) {
                if (!column(con, "password_hash")) st.executeUpdate("ALTER TABLE usuarios ADD COLUMN password_hash VARCHAR(255) NULL");
                if (!column(con, "credencial_bloqueada")) st.executeUpdate("ALTER TABLE usuarios ADD COLUMN credencial_bloqueada BOOLEAN NOT NULL DEFAULT FALSE");
                if (!column(con, "auth_version")) st.executeUpdate("ALTER TABLE usuarios ADD COLUMN auth_version INT NOT NULL DEFAULT 0");
            }
            boolean legacy = column(con, "password");
            // MySQL DDL commits implicitly. Each phase is resumable; run with the app stopped.
            if (legacy) {
                try (Statement st = con.createStatement();
                     ResultSet rows = st.executeQuery("SELECT id, password, password_hash FROM usuarios");
                     PreparedStatement save = con.prepareStatement("UPDATE usuarios SET password_hash = ? WHERE id = ? AND password_hash IS NULL")) {
                    while (rows.next()) {
                        String password = rows.getString("password");
                        String hash = rows.getString("password_hash");
                        if (password == null) throw new SQLException("Credencial antigua no válida.");
                        if (hash == null) {
                            hash = Passwords.hash(password);
                            save.setString(1, hash);
                            save.setInt(2, rows.getInt("id"));
                            save.executeUpdate();
                        }
                        if (!Passwords.verify(password, hash)) throw new SQLException("La verificación ha fallado.");
                    }
                }
            }
            try (Statement st = con.createStatement(); ResultSet rows = st.executeQuery(legacy ? "SELECT password, password_hash FROM usuarios" : "SELECT password_hash FROM usuarios")) {
                while (rows.next()) {
                    String hash = rows.getString("password_hash");
                    if (legacy && !Passwords.verify(rows.getString("password"), hash)) throw new SQLException("La credencial guardada no supera la verificación.");
                    if (hash == null || !hash.startsWith("$argon2id$")) throw new SQLException("Quedan credenciales sin migrar.");
                }
            }
            try (PreparedStatement block = con.prepareStatement("UPDATE usuarios SET credencial_bloqueada = TRUE, auth_version = auth_version + 1 "
                    + "WHERE ((username = ? AND email = ?) OR (username = ? AND email = ?) OR (username = ? AND email = ?)) AND credencial_bloqueada = FALSE");
                 Statement st = con.createStatement()) {
                // Apply only before removing the legacy field, so rerunning cannot re-block renewed accounts.
                if (legacy) {
                    String[] published = {"GabeNewell", "papigave@tienda.com", "ElDonJuan", "editor@tienda.com", "Elpepe67", "jugador@tienda.com"};
                    for (int i = 0; i < published.length; i++) block.setString(i + 1, published[i]);
                    block.executeUpdate();
                }
                st.executeUpdate("ALTER TABLE usuarios MODIFY password_hash VARCHAR(255) NOT NULL");
                if (legacy) st.executeUpdate("ALTER TABLE usuarios DROP COLUMN password");
            }
        } finally {
            try (Statement st = con.createStatement()) { st.executeQuery("SELECT RELEASE_LOCK('genesis_auth_migration')").close(); }
        }
    }

    public static void setRole(Connection con, String username, String role) throws SQLException {
        if (!AccessPolicy.validRole(role)) throw new IllegalArgumentException("Rol no válido.");
        try (PreparedStatement ps = con.prepareStatement("UPDATE usuarios SET rol = ?, auth_version = auth_version + 1 WHERE username = ?")) {
            ps.setString(1, role);
            ps.setString(2, username);
            if (ps.executeUpdate() != 1) throw new IllegalArgumentException("Usuario no encontrado.");
        }
    }

    public static void setPassword(Connection con, String username, String password) throws SQLException {
        if (!Passwords.validNewPassword(password)) throw new IllegalArgumentException("La contraseña debe tener entre 15 y 128 caracteres.");
        try (PreparedStatement ps = con.prepareStatement("UPDATE usuarios SET password_hash = ?, credencial_bloqueada = FALSE, auth_version = auth_version + 1 WHERE username = ?")) {
            ps.setString(1, Passwords.hash(password));
            ps.setString(2, username);
            if (ps.executeUpdate() != 1) throw new IllegalArgumentException("Usuario no encontrado.");
        }
    }
}
