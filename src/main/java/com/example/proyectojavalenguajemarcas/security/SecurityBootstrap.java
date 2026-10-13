package com.example.proyectojavalenguajemarcas.security;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class SecurityBootstrap implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().getSessionCookieConfig()
                .setSecure(!"development".equals(System.getenv("APP_ENV")));
    }
}
