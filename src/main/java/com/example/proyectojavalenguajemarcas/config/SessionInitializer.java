package com.example.proyectojavalenguajemarcas.config;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

public class SessionInitializer extends AbstractHttpSessionApplicationInitializer {
    public SessionInitializer() { super(SessionConfiguration.class); }
}
