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
import java.util.List;


@WebServlet("/api/mispublicaciones")
public class MisPublicacionesServlet extends HttpServlet {


    private JuegoDAO juegoDAO = new JuegoDAO();

    private Gson gson = new Gson();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");


        HttpSession session = req.getSession(false);


        if (session == null || session.getAttribute("usuarioLogueado") == null) {

            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"No autorizado\"}");
            return;
        }


        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");


        if (usuario.getRol().equals("JUGADOR")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\": \"Permisos insuficientes\"}");
            return;
        }


        List<Juego> misJuegos = juegoDAO.obtenerJuegosPorEditor(usuario.getId());


        resp.getWriter().write(gson.toJson(misJuegos));
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");


        JsonObject jsonRespuesta = new JsonObject();


        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            jsonRespuesta.addProperty("exito", false);
            resp.getWriter().write(gson.toJson(jsonRespuesta));
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        try {


            BufferedReader reader = req.getReader();

            JsonObject jsonRecibido = gson.fromJson(reader, JsonObject.class);


            int idJuego = jsonRecibido.get("id").getAsInt();


            boolean borrado = juegoDAO.eliminarJuego(idJuego, usuario.getId());


            if (borrado) {
                jsonRespuesta.addProperty("exito", true);
                jsonRespuesta.addProperty("mensaje", "Juego eliminado de la base de datos");
            } else {
                jsonRespuesta.addProperty("exito", false);
                jsonRespuesta.addProperty("mensaje", "No se pudo eliminar el juego");
            }

        } catch (Exception e) {

            e.printStackTrace();
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Error procesando la solicitud");
        }


        resp.getWriter().write(gson.toJson(jsonRespuesta));
    }
}