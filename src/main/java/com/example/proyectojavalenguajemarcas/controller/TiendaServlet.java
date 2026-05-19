package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.Juego;
import com.example.proyectojavalenguajemarcas.model.JuegoDAO;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

// Esta es la URL a la que JS hará la petición "fetch"
@WebServlet("/api/tienda")
public class TiendaServlet extends HttpServlet {

    private JuegoDAO juegoDAO = new JuegoDAO();
    private Gson gson = new Gson();

    // Usamos doGet porque solo queremos LEER datos, no enviar nada nuevo
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Configuramos la respuesta para avisar de que enviaremos JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            // 2. Le pedimos a MySQL la lista de juegos publicados
            List<Juego> juegos = juegoDAO.obtenerJuegosPublicados();

            // 3. ¡La magia de Gson! Traduce la lista de Java a texto JSON
            String jsonRespuesta = gson.toJson(juegos);

            // 4. Se lo mandamos al navegador web
            resp.getWriter().write(jsonRespuesta);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Error al obtener los juegos\"}");
        }
    }
}