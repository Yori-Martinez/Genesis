package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.UsuarioDAO;
import com.example.proyectojavalenguajemarcas.security.*;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/registro")
public class RegistroServlet extends HttpServlet {
    private final UsuarioDAO dao = new UsuarioDAO();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject input = HttpJson.read(req);
            String username = HttpJson.string(input, "username", 50).trim();
            String email = HttpJson.string(input, "email", 100).trim();
            String password = HttpJson.string(input, "password", 512);
            if (username.isBlank() || username.chars().anyMatch(Character::isISOControl)
                    || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || !Passwords.validNewPassword(password)) {
                throw new IllegalArgumentException();
            }
            if (dao.registrarUsuario(username, email, password) == UsuarioDAO.Registration.DUPLICATE) {
                HttpJson.error(resp, 409, "El usuario o email ya existe.");
                return;
            }
            HttpJson.write(resp, 201, Map.of("exito", true, "mensaje", "Cuenta creada. Ya puedes iniciar sesión."));
        } catch (IllegalArgumentException e) {
            HttpJson.error(resp, 400, "Datos no válidos. La contraseña debe tener entre 15 y 128 caracteres.");
        } catch (SQLException e) {
            HttpJson.error(resp, 503, "Servicio temporalmente no disponible.");
        }
    }
}
