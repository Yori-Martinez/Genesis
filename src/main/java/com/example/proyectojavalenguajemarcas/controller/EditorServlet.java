package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.JuegoDAO;
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

// 🚨 ANOTACIÓN CLAVE: Permite al Servlet desempaquetar archivos físicos de la petición HTTP
@MultipartConfig
@WebServlet("/api/editor")
public class EditorServlet extends HttpServlet {

    private JuegoDAO juegoDAO = new JuegoDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JsonObject jsonRespuesta = new JsonObject();

        // 1. VERIFICACIÓN DE SESIÓN
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Acceso denegado. Debes iniciar sesión.");
            resp.getWriter().write(gson.toJson(jsonRespuesta));
            return;
        }

        // 2. VERIFICACIÓN DE ROL (Solo el EDITOR puede subir juegos)
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado.getRol().equals("JUGADOR")) {
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "No tienes permisos para subir juegos.");
            resp.getWriter().write(gson.toJson(jsonRespuesta));
            return;
        }

        try {
            // 3. LECTURA MULTIPART: Como no es JSON, usamos getParameter() para los textos
            String titulo = req.getParameter("titulo");
            String descripcion = req.getParameter("descripcion");

            // Extraemos la pieza binaria que contiene el archivo ZIP
            Part zipPart = req.getPart("zip");

            // Comprobación de seguridad en el backend por si fallase el frontend
            if (zipPart == null || zipPart.getSize() == 0) {
                jsonRespuesta.addProperty("exito", false);
                jsonRespuesta.addProperty("mensaje", "El archivo ZIP es obligatorio.");
                resp.getWriter().write(gson.toJson(jsonRespuesta));
                return;
            }

            // 4. PERSISTENCIA EN BASE DE DATOS
            // Convertimos el archivo ZIP a un flujo de bytes continuo (InputStream)
            InputStream zipStream = zipPart.getInputStream();

            // Llamamos al NUEVO método del DAO que inyecta el InputStream en la columna LONGBLOB
            boolean guardado = juegoDAO.insertarJuegoConZip(titulo, descripcion, usuarioLogueado.getId(), zipStream);

            // 5. RESPUESTA AL CLIENTE
            if (guardado) {
                jsonRespuesta.addProperty("exito", true);
                jsonRespuesta.addProperty("mensaje", "Juego y ZIP subidos con éxito. Pendiente de revisión.");
            } else {
                jsonRespuesta.addProperty("exito", false);
                jsonRespuesta.addProperty("mensaje", "Error al guardar los binarios en la base de datos.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Error procesando el formulario multipart.");
        }

        resp.getWriter().write(gson.toJson(jsonRespuesta));
    }
}