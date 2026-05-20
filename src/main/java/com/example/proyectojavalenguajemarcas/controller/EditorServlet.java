package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.Juego;
import com.example.proyectojavalenguajemarcas.model.JuegoDAO;
import com.example.proyectojavalenguajemarcas.model.Usuario;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/api/editor")
public class EditorServlet extends HttpServlet {

    private JuegoDAO juegoDAO = new JuegoDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JsonObject jsonRespuesta = new JsonObject();


        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Acceso denegado. Debes iniciar sesión.");
            resp.getWriter().write(gson.toJson(jsonRespuesta));
            return;
        }


        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado.getRol().equals("JUGADOR")) {
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "No tienes permisos para subir juegos.");
            resp.getWriter().write(gson.toJson(jsonRespuesta));
            return;
        }

        try {

            BufferedReader reader = req.getReader();
            JsonObject jsonRecibido = gson.fromJson(reader, JsonObject.class);


            Juego nuevoJuego = new Juego();
            nuevoJuego.setTitulo(jsonRecibido.get("titulo").getAsString());
            nuevoJuego.setDescripcion(jsonRecibido.get("descripcion").getAsString());
            nuevoJuego.setArchivoZipUrl(jsonRecibido.get("archivoZipUrl").getAsString());
            nuevoJuego.setIdEditor(usuarioLogueado.getId());


            boolean guardado = juegoDAO.insertarJuego(nuevoJuego);

            if (guardado) {
                jsonRespuesta.addProperty("exito", true);
                jsonRespuesta.addProperty("mensaje", "Juego subido con éxito. Pendiente de revisión.");
            } else {
                jsonRespuesta.addProperty("exito", false);
                jsonRespuesta.addProperty("mensaje", "Error al guardar en la base de datos.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Error procesando los datos.");
        }

        resp.getWriter().write(gson.toJson(jsonRespuesta));
    }
}