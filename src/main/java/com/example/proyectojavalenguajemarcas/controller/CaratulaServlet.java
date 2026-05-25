package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.ConexionBD;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/api/caratula")
public class CaratulaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        String sql = "SELECT caratula_contenido FROM juegos WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] imagenBytes = rs.getBytes("caratula_contenido");
                    if (imagenBytes != null) {

                        resp.setContentType("image/png");
                        resp.setContentLength(imagenBytes.length);
                        try (OutputStream os = resp.getOutputStream()) {
                            os.write(imagenBytes);
                            os.flush();
                        }
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}