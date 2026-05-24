package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.ConexionBD;
import com.example.proyectojavalenguajemarcas.model.Usuario;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Habilitamos el procesamiento de datos multipart para recibir la foto de perfil (Límite: 5MB)
@MultipartConfig(maxFileSize = 1024 * 1024 * 5)
@WebServlet("/api/avatar")
public class AvatarServlet extends HttpServlet {

    // =========================================================================
    // MÉTODO GET: Recupera los bytes de MySQL y los transmite a la cabecera
    // =========================================================================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        String sql = "SELECT avatar_contenido FROM usuarios WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuario.getId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] imagenBytes = rs.getBytes("avatar_contenido");
                    if (imagenBytes != null && imagenBytes.length > 0) {
                        // Forzamos al navegador a interpretar los datos binarios como imagen
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
        // Si el usuario no tiene foto aún, devolvemos un código 404 controlado para usar el fondo por defecto del CSS
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    // =========================================================================
    // MÉTODO POST: Actualiza la foto del usuario actual de forma persistente
    // =========================================================================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JsonObject jsonRespuesta = new JsonObject();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Sesión no válida.");
            resp.getWriter().write(new Gson().toJson(jsonRespuesta));
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        try {
            Part filePart = req.getPart("avatar");
            if (filePart != null && filePart.getSize() > 0) {
                InputStream inputStream = filePart.getInputStream();
                String sql = "UPDATE usuarios SET avatar_contenido = ? WHERE id = ?";

                try (Connection con = ConexionBD.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setBinaryStream(1, inputStream);
                    ps.setInt(2, usuario.getId());

                    boolean exito = ps.executeUpdate() > 0;
                    jsonRespuesta.addProperty("exito", exito);
                    jsonRespuesta.addProperty("mensaje", exito ? "¡Foto de perfil actualizada!" : "Error al escribir en la base de datos.");
                }
            } else {
                jsonRespuesta.addProperty("exito", false);
                jsonRespuesta.addProperty("mensaje", "Archivo no válido o vacío.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Error procesando el flujo binario.");
        }

        resp.getWriter().write(new Gson().toJson(jsonRespuesta));
    }
}