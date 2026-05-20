package com.example.proyectojavalenguajemarcas.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Definimos la ruta que escuchará nuestro fetch de cerrar sesión
@WebServlet("/api/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Obtenemos la sesión actual (false significa que no cree una nueva si no existe)
        HttpSession session = req.getSession(false);

        if (session != null) {
            // ¡LA MAGIA!: invalidate() destruye la sesión por completo en el servidor
            session.invalidate();
        }

        // Respondemos al JavaScript que todo ha ido bien
        JsonObject jsonRespuesta = new JsonObject();
        jsonRespuesta.addProperty("exito", true);
        resp.getWriter().write(new Gson().toJson(jsonRespuesta));
    }
}