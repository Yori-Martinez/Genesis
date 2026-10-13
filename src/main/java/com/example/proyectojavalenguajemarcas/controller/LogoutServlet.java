package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.security.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        Sessions.clearCookie(req, resp);
        HttpJson.write(resp, 200, Map.of("exito", true));
    }
}
