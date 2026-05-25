package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.Juego;
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
import java.util.List;

@MultipartConfig
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
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (!usuario.getRol().equals("ADMIN")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam != null) {
            Juego juego = juegoDAO.obtenerJuegoPorId(Integer.parseInt(idParam));
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
            resp.getWriter().write(gson.toJson(jsonRespuesta));
            return;
        }

        try {
            int idJuego = Integer.parseInt(req.getParameter("id"));
            String estado = req.getParameter("estado");

            if ("RECHAZADO".equals(estado)) {

                boolean exito = juegoDAO.actualizarEstadoJuego(idJuego, "RECHAZADO");
                jsonRespuesta.addProperty("exito", exito);
                jsonRespuesta.addProperty("mensaje", exito ? "Juego rechazado." : "Error al rechazar.");

            } else if ("PUBLICADO".equals(estado)) {

                Part archivoPart = req.getPart("caratula");
                if (archivoPart != null && archivoPart.getSize() > 0) {
                    InputStream inputStream = archivoPart.getInputStream();
                    boolean exito = juegoDAO.guardarArchivosYPublicar(idJuego, inputStream);
                    jsonRespuesta.addProperty("exito", exito);
                    jsonRespuesta.addProperty("mensaje", exito ? "Aprobado." : "Error en base de datos.");
                } else {
                    jsonRespuesta.addProperty("exito", false);
                    jsonRespuesta.addProperty("mensaje", "Falta imagen de carátula.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Error procesando el veredicto.");
        }

        resp.getWriter().write(gson.toJson(jsonRespuesta));
    }
}