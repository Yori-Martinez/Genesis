package com.example.proyectojavalenguajemarcas.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    public static Connection getConnection() throws SQLException {
        String host = System.getenv("MYSQLHOST") != null ? System.getenv("MYSQLHOST") : "localhost";
        String port = System.getenv("MYSQLPORT") != null ? System.getenv("MYSQLPORT") : "3306";
        String db = System.getenv("MYSQLDATABASE") != null ? System.getenv("MYSQLDATABASE") : "tienda_juegos_db";
        String user = System.getenv("MYSQLUSER") != null ? System.getenv("MYSQLUSER") : "admin";
        String pass = System.getenv("MYSQLPASSWORD") != null ? System.getenv("MYSQLPASSWORD") : "admin123";

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
    }
}