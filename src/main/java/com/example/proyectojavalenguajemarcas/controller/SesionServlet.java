package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.Usuario;
import com.example.proyectojavalenguajemarcas.security.HttpJson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/sesion")
public class SesionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Usuario user = (Usuario) req.getAttribute("authenticatedUser");
        JsonObject body = new JsonObject();
        body.addProperty("autenticado", user != null);
        body.addProperty("nombre", user == null ? null : user.getUsername());
        body.addProperty("rol", user == null ? null : user.getRol());
        HttpJson.write(resp, 200, body);
    }
}
