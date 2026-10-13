package com.example.proyectojavalenguajemarcas.model;

import java.sql.*;
import java.util.Properties;

public class ConexionBD {
    private static String required(String key) throws SQLException {
        String value = System.getenv(key);
        if (value == null) {
            String tidbKey = switch (key) {
                case "MYSQLHOST" -> "TIDB_HOST";
                case "MYSQLPORT" -> "TIDB_PORT";
                case "MYSQLDATABASE" -> "TIDB_DATABASE";
                case "MYSQLUSER" -> "TIDB_USER";
                case "MYSQLPASSWORD" -> "TIDB_PASSWORD";
                default -> key;
            };
            value = System.getenv(tidbKey);
        }
        if (value == null || value.isBlank()) throw new SQLException("Falta configuración de base de datos: " + key);
        return value;
    }

    public static Connection getConnection() throws SQLException {
        String host = required("MYSQLHOST");
        String port = required("MYSQLPORT");
        String db = required("MYSQLDATABASE");
        if (!host.matches("[a-zA-Z0-9.-]+") || !port.matches("[0-9]{1,5}") || !db.matches("[a-zA-Z0-9_]+")) {
            throw new SQLException("Configuración de base de datos no válida.");
        }
        Properties props = new Properties();
        props.setProperty("user", required("MYSQLUSER"));
        props.setProperty("password", required("MYSQLPASSWORD"));
        props.setProperty("serverTimezone", "UTC");
        props.setProperty("connectTimeout", "5000");
        props.setProperty("socketTimeout", "10000");
        String sslMode = System.getenv("MYSQL_SSL_MODE");
        if (sslMode == null) sslMode = "development".equals(System.getenv("APP_ENV")) ? "DISABLED" : "VERIFY_IDENTITY";
        if (!"development".equals(System.getenv("APP_ENV")) && !sslMode.equals("VERIFY_IDENTITY")) {
            throw new SQLException("Producción requiere MYSQL_SSL_MODE=VERIFY_IDENTITY.");
        }
        props.setProperty("sslMode", sslMode);
        props.setProperty("allowPublicKeyRetrieval", "development".equals(System.getenv("APP_ENV")) ? "true" : "false");
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new SQLException("Driver no disponible.", e); }
        return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, props);
    }
}
