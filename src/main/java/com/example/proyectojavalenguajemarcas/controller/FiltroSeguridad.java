package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.*;
import com.example.proyectojavalenguajemarcas.security.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

public class FiltroSeguridad implements Filter {
    private final UsuarioDAO dao = new UsuarioDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        String path = req.getServletPath();
        AccessPolicy.Access access = AccessPolicy.required(path, req.getMethod());
        if (access == AccessPolicy.Access.DENY) {
            HttpJson.error(resp, 403, "Acceso denegado.");
            return;
        }
        if (path.startsWith("/api/") || access != AccessPolicy.Access.PUBLIC) {
            resp.setHeader("Cache-Control", "no-store");
            Usuario user;
            try {
                user = Sessions.resolve(req.getSession(false), dao);
            } catch (SQLException e) {
                HttpJson.error(resp, 503, "Servicio temporalmente no disponible.");
                return;
            }
            req.setAttribute("authenticatedUser", user);
            if (access != AccessPolicy.Access.PUBLIC && user == null) {
                Sessions.clearCookie(req, resp);
                if (!path.startsWith("/api/")) resp.sendRedirect(req.getContextPath() + "/index.html");
                else HttpJson.error(resp, 401, "Debes iniciar sesión.");
                return;
            }
            if (access != AccessPolicy.Access.PUBLIC && !AccessPolicy.permits(access, user.getRol())) {
                if (!path.startsWith("/api/")) resp.sendRedirect(req.getContextPath() + "/tienda.html");
                else HttpJson.error(resp, 403, "Permisos insuficientes.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
