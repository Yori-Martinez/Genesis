package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.Usuario;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

// 1. @WebFilter le dice a Java qué URLs debe "vigilar".
// Si alguien intenta entrar a cualquiera de estas páginas, el código de abajo se ejecutará primero.
@WebFilter(urlPatterns = {"/editor.html", "/mis_publicaciones.html", "/revision.html", "/lista_revision.html", "/biblioteca.html", "/tienda.html"})
public class FiltroSeguridad implements Filter {

    // El método doFilter es el "portero de la discoteca"
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // Convertimos las peticiones genéricas a peticiones HTTP para poder leer la URL y la Sesión
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Intentamos recuperar la sesión actual. (false) significa que si no existe, no cree una nueva.
        HttpSession session = req.getSession(false);

        // Obtenemos qué página exacta está intentando visitar el usuario (ej: "/editor.html")
        String path = req.getRequestURI();

        // 2. PRIMERA BARRERA: ¿Tiene el usuario la sesión iniciada?
        // Comprobamos si la sesión existe y si tiene el atributo "usuarioLogueado" que creamos en LoginServlet
        boolean logueado = (session != null && session.getAttribute("usuarioLogueado") != null);

        // Si NO está logueado, lo mandamos directo al index.html (pantalla de login) y cortamos la ejecución con return.
        if (!logueado) {
            resp.sendRedirect("index.html");
            return;
        }

        // 3. SEGUNDA BARRERA: Control de Permisos (Roles)
        // Si ha pasado la barrera anterior, extraemos el usuario de la memoria del servidor para ver su Rol
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        String rol = usuario.getRol();

        // REGLA PARA EL JUGADOR: Si intenta entrar a las pantallas de Editor y su rol es JUGADOR...
        if ((path.endsWith("editor.html") || path.endsWith("mis_publicaciones.html")) && rol.equals("JUGADOR")) {
            // ...le denegamos el acceso y lo mandamos a la tienda
            resp.sendRedirect("tienda.html");
            return;
        }

        // REGLA PARA TODOS LOS QUE NO SEAN ADMIN: Si alguien (Jugador o Editor) intenta entrar a las pantallas de Revisión...
        if ((path.endsWith("revision.html") || path.endsWith("lista_revision.html")) && !rol.equals("ADMIN")) {
            // ...le denegamos el acceso y lo mandamos a la tienda
            resp.sendRedirect("tienda.html");
            return;
        }

        // 4. EL PASE VIP
        // Si el código llega hasta aquí, significa que el usuario tiene permisos válidos para esa pantalla.
        // chain.doFilter le dice al servidor: "Todo está correcto, deja que el usuario pase y cargue la página HTML".
        chain.doFilter(request, response);
    }
}