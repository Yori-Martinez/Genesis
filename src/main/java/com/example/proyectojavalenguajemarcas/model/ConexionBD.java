package com.example.proyectojavalenguajemarcas.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    // Leemos las variables de entorno de Railway. Si son null (estamos en local), usamos las de nuestro Docker.
    private static final String URL = System.getenv("MYSQL_URL") != null ?
            System.getenv("MYSQL_URL") : "jdbc:mysql://localhost:3306/tienda_juegos_db";

    private static final String USER = System.getenv("MYSQLUSER") != null ?
            System.getenv("MYSQLUSER") : "admin";

    private static final String PASSWORD = System.getenv("MYSQLPASSWORD") != null ?
            System.getenv("MYSQLPASSWORD") : "admin123";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver de MySQL.");
            throw new SQLException(e);
        }
    }
}