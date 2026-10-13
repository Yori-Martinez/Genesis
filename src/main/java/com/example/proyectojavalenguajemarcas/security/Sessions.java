package com.example.proyectojavalenguajemarcas.security;

import com.example.proyectojavalenguajemarcas.model.*;
import jakarta.servlet.http.*;
import java.sql.SQLException;

public final class Sessions {
    public static final long MAX_AGE_MS = 8L * 60 * 60 * 1000;
    private Sessions() {}

    public static void login(HttpServletRequest req, UsuarioDAO.Account account) {
        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();
        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute("usuarioLogueado", account.user());
        session.setAttribute("authenticatedAt", System.currentTimeMillis());
        session.setAttribute("authVersion", account.version());
        session.setAttribute("loginRole", account.user().getRol());
    }

    public static Usuario resolve(HttpSession session, UsuarioDAO dao) throws SQLException {
        if (session == null || session.getAttribute("usuarioLogueado") == null) return null;
        if (!(session.getAttribute("usuarioLogueado") instanceof Usuario previous)
                || !(session.getAttribute("authenticatedAt") instanceof Long started)
                || !(session.getAttribute("authVersion") instanceof Integer version)
                || System.currentTimeMillis() - started >= MAX_AGE_MS) {
            session.invalidate();
            return null;
        }
        UsuarioDAO.Account current = dao.findById(previous.getId());
        if (current == null || current.blocked() || current.version() != version
                || !AccessPolicy.validRole(current.user().getRol())
                || !current.user().getRol().equals(session.getAttribute("loginRole"))) {
            session.invalidate();
            return null;
        }
        session.setAttribute("usuarioLogueado", current.user());
        return current.user();
    }

    public static void clearCookie(HttpServletRequest req, HttpServletResponse resp) {
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setMaxAge(0);
        cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        cookie.setHttpOnly(true);
        cookie.setSecure(!"development".equals(System.getenv("APP_ENV")));
        resp.addCookie(cookie);
    }
}
