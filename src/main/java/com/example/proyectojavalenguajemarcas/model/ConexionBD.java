package com.example.proyectojavalenguajemarcas.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    public static Connection getConnection() throws SQLException {
        String host = System.getenv("MYSQLHOST") != null ? System.getenv("MYSQLHOST") : "mysql.railway.internal";
        String port = System.getenv("MYSQLPORT") != null ? System.getenv("MYSQLPORT") : "3306";
        String db = System.getenv("MYSQLDATABASE") != null ? System.getenv("MYSQLDATABASE") : "railway";
        String user = System.getenv("MYSQLUSER") != null ? System.getenv("MYSQLUSER") : "root";
        String pass = System.getenv("MYSQLPASSWORD") != null ? System.getenv("MYSQLPASSWORD") : "uzMGPEfjgMbcbLCKzNPafNTBMeWBdFND";

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
    }
}