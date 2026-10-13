package com.example.proyectojavalenguajemarcas.security;

import com.example.proyectojavalenguajemarcas.tools.AuthAdmin;
import org.junit.jupiter.api.Test;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class MigrationIT {
    @Test void migratesWithoutLosingRelationsAndCanResumeAndRenewPublishedAccounts() throws Exception {
        DatabaseFixture.seed();
        try (Connection con=DatabaseFixture.connection(); Statement st=con.createStatement()) {
            st.executeUpdate("ALTER TABLE usuarios ADD COLUMN password VARCHAR(255)");
            st.executeUpdate("UPDATE usuarios SET password=' Test legacy password '");
            st.executeUpdate("ALTER TABLE usuarios DROP COLUMN password_hash");
            st.executeUpdate("ALTER TABLE usuarios DROP COLUMN credencial_bloqueada");
            st.executeUpdate("ALTER TABLE usuarios DROP COLUMN auth_version");
            st.executeUpdate("UPDATE usuarios SET username='GabeNewell',email='papigave@tienda.com' WHERE id=4");
            // Simulate a previously interrupted migration with one completed hash.
            st.executeUpdate("ALTER TABLE usuarios ADD COLUMN password_hash VARCHAR(255)");
            try (PreparedStatement ps=con.prepareStatement("UPDATE usuarios SET password_hash=? WHERE id=1")) {
                ps.setString(1,Passwords.hash(" Test legacy password ")); ps.executeUpdate();
            }
            AuthAdmin.migrate(con);
            assertEquals("5", DatabaseFixture.scalar("SELECT COUNT(*) FROM usuarios"));
            assertEquals("3", DatabaseFixture.scalar("SELECT COUNT(*) FROM juegos"));
            assertEquals("1", DatabaseFixture.scalar("SELECT COUNT(*) FROM biblioteca WHERE id_usuario=1 AND id_juego=10"));
            assertEquals("1", DatabaseFixture.scalar("SELECT credencial_bloqueada FROM usuarios WHERE id=4"));
            assertEquals("0", DatabaseFixture.scalar("SELECT credencial_bloqueada FROM usuarios WHERE id=1"));
            try (ResultSet columns=con.getMetaData().getColumns(con.getCatalog(),null,"usuarios","password")) { assertFalse(columns.next()); }
            try (ResultSet hashes=st.executeQuery("SELECT password_hash FROM usuarios")) {
                while (hashes.next()) assertTrue(Passwords.verify(" Test legacy password ", hashes.getString(1)));
            }
            AuthAdmin.setPassword(con,"GabeNewell","Renewed administrator password ");
            AuthAdmin.setRole(con,"player","EDITOR");
            AuthAdmin.migrate(con);
            assertEquals("0", DatabaseFixture.scalar("SELECT credencial_bloqueada FROM usuarios WHERE id=4"));
            assertEquals("EDITOR", DatabaseFixture.scalar("SELECT rol FROM usuarios WHERE id=1"));
            assertEquals("2", DatabaseFixture.scalar("SELECT auth_version FROM usuarios WHERE id=4"));
            assertThrows(IllegalArgumentException.class, ()->AuthAdmin.setPassword(con,"player","too short"));
            assertThrows(IllegalArgumentException.class, ()->AuthAdmin.setRole(con,"player","OWNER"));
        }
    }
    @Test void refusesToDropPlaintextWhenAnExistingHashDoesNotMatch() throws Exception {
        DatabaseFixture.seed();
        try (Connection con=DatabaseFixture.connection(); Statement st=con.createStatement()) {
            st.executeUpdate("ALTER TABLE usuarios ADD COLUMN password VARCHAR(255)");
            st.executeUpdate("UPDATE usuarios SET password='Different from stored hash'");
            assertThrows(SQLException.class, ()->AuthAdmin.migrate(con));
            try (ResultSet columns=con.getMetaData().getColumns(con.getCatalog(),null,"usuarios","password")) { assertTrue(columns.next()); }
        }
    }
}
