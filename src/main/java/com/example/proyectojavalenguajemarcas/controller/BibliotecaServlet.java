package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.Juego;
import com.example.proyectojavalenguajemarcas.model.JuegoDAO;
import com.example.proyectojavalenguajemarcas.model.Usuario;
import com.google.gson.Gson;
import com.example.proyectojavalenguajemarcas.security.*;
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

@WebServlet("/api/biblioteca")
public class BibliotecaServlet extends HttpServlet {

    private JuegoDAO juegoDAO = new JuegoDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        List<Juego> juegos = juegoDAO.obtenerJuegosBiblioteca(usuario.getId());
        resp.getWriter().write(gson.toJson(juegos));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JsonObject jsonRespuesta = new JsonObject();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Debes iniciar sesión");
            resp.getWriter().write(gson.toJson(jsonRespuesta));
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        try {
            BufferedReader reader = req.getReader();
            JsonObject jsonRecibido = HttpJson.read(req);
            int idJuego = HttpJson.positiveId(jsonRecibido, "id_juego");

            boolean agregado = juegoDAO.agregarABiblioteca(usuario.getId(), idJuego);

            if (agregado) {
                jsonRespuesta.addProperty("exito", true);
                jsonRespuesta.addProperty("mensaje", "¡Añadido al carrito!");
            } else {
                jsonRespuesta.addProperty("exito", false);
                jsonRespuesta.addProperty("mensaje", "El juego no está publicado o ya está en tu biblioteca");
            }
        } catch (IllegalArgumentException e) {
            HttpJson.error(resp, 400, "Identificador no válido.");
            return;
        } catch (Exception e) {
            jsonRespuesta.addProperty("exito", false);
        }

        resp.getWriter().write(gson.toJson(jsonRespuesta));
    }
}