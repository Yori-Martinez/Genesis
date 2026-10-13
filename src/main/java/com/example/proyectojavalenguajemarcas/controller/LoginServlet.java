package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.UsuarioDAO;
import com.example.proyectojavalenguajemarcas.security.*;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    private final UsuarioDAO dao = new UsuarioDAO();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject input = HttpJson.read(req);
            String username = HttpJson.string(input, "username", 50).trim();
            String password = HttpJson.string(input, "password", 512);
            if (username.isEmpty()) throw new IllegalArgumentException();
            UsuarioDAO.Account account = dao.validarLogin(username, password);
            if (account == null) {
                HttpJson.error(resp, 401, "Usuario o contraseña incorrectos");
                return;
            }
            Sessions.login(req, account);
            JsonObject body = new JsonObject();
            body.addProperty("exito", true);
            body.addProperty("nombre", account.user().getUsername());
            body.addProperty("rol", account.user().getRol());
            HttpJson.write(resp, 200, body);
        } catch (IllegalArgumentException e) {
            HttpJson.error(resp, 400, "Datos de acceso no válidos.");
        } catch (SQLException e) {
            HttpJson.error(resp, 503, "Servicio temporalmente no disponible.");
        }
    }
}
