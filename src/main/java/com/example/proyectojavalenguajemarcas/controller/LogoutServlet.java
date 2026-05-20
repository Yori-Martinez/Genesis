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


@WebServlet("/api/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");


        HttpSession session = req.getSession(false);

        if (session != null) {

            session.invalidate();
        }


        JsonObject jsonRespuesta = new JsonObject();
        jsonRespuesta.addProperty("exito", true);
        resp.getWriter().write(new Gson().toJson(jsonRespuesta));
    }
}