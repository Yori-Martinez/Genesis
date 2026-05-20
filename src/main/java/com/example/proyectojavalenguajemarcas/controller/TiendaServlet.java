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


@WebServlet("/api/tienda")
public class TiendaServlet extends HttpServlet {

    private JuegoDAO juegoDAO = new JuegoDAO();
    private Gson gson = new Gson();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {

            List<Juego> juegos = juegoDAO.obtenerJuegosPublicados();


            String jsonRespuesta = gson.toJson(juegos);


            resp.getWriter().write(jsonRespuesta);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Error al obtener los juegos\"}");
        }
    }
}