package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.UsuarioDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/api/registro")
public class RegistroServlet extends HttpServlet {
    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JsonObject jsonRespuesta = new JsonObject();

        try {
            BufferedReader reader = req.getReader();
            JsonObject jsonRecibido = gson.fromJson(reader, JsonObject.class);

            String username = jsonRecibido.get("username").getAsString();
            String email = jsonRecibido.get("email").getAsString();
            String password = jsonRecibido.get("password").getAsString();

            boolean registrado = usuarioDAO.registrarUsuario(username, email, password);

            if (registrado) {
                jsonRespuesta.addProperty("exito", true);
                jsonRespuesta.addProperty("mensaje", "Cuenta creada. Ya puedes iniciar sesión.");
            } else {
                jsonRespuesta.addProperty("exito", false);
                jsonRespuesta.addProperty("mensaje", "El usuario o email ya existe.");
            }
        } catch (Exception e) {
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Error procesando el registro.");
        }
        resp.getWriter().write(gson.toJson(jsonRespuesta));
    }
}