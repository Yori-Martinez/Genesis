package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.Usuario;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;


@WebFilter(urlPatterns = {"/editor.html", "/mis_publicaciones.html", "/revision.html", "/lista_revision.html", "/biblioteca.html", "/tienda.html"})
public class FiltroSeguridad implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {


        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;


        HttpSession session = req.getSession(false);


        String path = req.getRequestURI();


        boolean logueado = (session != null && session.getAttribute("usuarioLogueado") != null);


        if (!logueado) {
            resp.sendRedirect("index.html");
            return;
        }


        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        String rol = usuario.getRol();


        if ((path.endsWith("editor.html") || path.endsWith("mis_publicaciones.html")) && rol.equals("JUGADOR")) {

            resp.sendRedirect("tienda.html");
            return;
        }


        if ((path.endsWith("revision.html") || path.endsWith("lista_revision.html")) && !rol.equals("ADMIN")) {

            resp.sendRedirect("tienda.html");
            return;
        }


        chain.doFilter(request, response);
    }
}