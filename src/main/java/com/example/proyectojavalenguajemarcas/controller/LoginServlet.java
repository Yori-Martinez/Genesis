package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.Usuario;
import com.example.proyectojavalenguajemarcas.model.UsuarioDAO;
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

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // 1. Leer el JSON que nos envía el frontend (login.js)
        BufferedReader reader = req.getReader();
        JsonObject jsonRecibido = gson.fromJson(reader, JsonObject.class);

        String username = jsonRecibido.get("username").getAsString();
        String password = jsonRecibido.get("password").getAsString();

        // 2. Verificar en la base de datos
        Usuario usuarioValido = usuarioDAO.validarLogin(username, password);

        JsonObject jsonRespuesta = new JsonObject();

        if (usuarioValido != null) {
            // 3. ¡AQUÍ ESTÁ LA SEGURIDAD! Creamos la sesión en el servidor.
            // Aunque el usuario cambie su F12, el servidor manda en esta sesión.
            HttpSession session = req.getSession(true);
            session.setAttribute("usuarioLogueado", usuarioValido);

            jsonRespuesta.addProperty("exito", true);
            jsonRespuesta.addProperty("rol", usuarioValido.getRol());
            jsonRespuesta.addProperty("nombre", usuarioValido.getUsername());
        } else {
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Usuario o contraseña incorrectos");
        }

        // 4. Devolvemos la respuesta al navegador
        resp.getWriter().write(gson.toJson(jsonRespuesta));
    }
}