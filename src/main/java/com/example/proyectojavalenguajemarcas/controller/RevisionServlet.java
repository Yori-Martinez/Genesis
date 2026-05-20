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

@WebServlet("/api/revision")
public class RevisionServlet extends HttpServlet {

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
        if (!usuario.getRol().equals("ADMIN")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\": \"Permisos insuficientes\"}");
            return;
        }


        String idParam = req.getParameter("id");

        if (idParam != null) {

            int id = Integer.parseInt(idParam);
            Juego juego = juegoDAO.obtenerJuegoPorId(id);
            resp.getWriter().write(gson.toJson(juego));
        } else {

            List<Juego> pendientes = juegoDAO.obtenerJuegosPendientes();
            resp.getWriter().write(gson.toJson(pendientes));
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JsonObject jsonRespuesta = new JsonObject();


        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "No autorizado");
            resp.getWriter().write(gson.toJson(jsonRespuesta));
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (!usuario.getRol().equals("ADMIN")) {
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Permisos insuficientes");
            resp.getWriter().write(gson.toJson(jsonRespuesta));
            return;
        }

        try {

            BufferedReader reader = req.getReader();
            JsonObject jsonRecibido = gson.fromJson(reader, JsonObject.class);

            int id = jsonRecibido.get("id").getAsInt();
            String nuevoEstado = jsonRecibido.get("estado").getAsString();


            boolean actualizado = juegoDAO.actualizarEstadoJuego(id, nuevoEstado);


            if (actualizado) {
                jsonRespuesta.addProperty("exito", true);
                jsonRespuesta.addProperty("mensaje", "Estado actualizado correctamente");
            } else {
                jsonRespuesta.addProperty("exito", false);
                jsonRespuesta.addProperty("mensaje", "Error al actualizar en la base de datos");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Error al procesar la solicitud");
        }

        resp.getWriter().write(gson.toJson(jsonRespuesta));
    }
}