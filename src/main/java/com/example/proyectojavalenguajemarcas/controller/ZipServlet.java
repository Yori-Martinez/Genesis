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

@WebServlet("/api/zip")
public class ZipServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int id = Integer.parseInt(idParam);
        String sql = "SELECT zip_contenido, titulo FROM juegos WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] zipBytes = rs.getBytes("zip_contenido");
                    String titulo = rs.getString("titulo");

                    if (zipBytes != null) {

                        resp.setContentType("application/zip");
                        resp.setContentLength(zipBytes.length);


                        String nombreArchivo = titulo.replaceAll("[^a-zA-Z0-9.-]", "_") + ".zip";
                        resp.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"");


                        try (OutputStream os = resp.getOutputStream()) {
                            os.write(zipBytes);
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