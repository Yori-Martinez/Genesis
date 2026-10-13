package com.example.proyectojavalenguajemarcas.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.example.proyectojavalenguajemarcas.security.AccessPolicy.*;

class AccessPolicyTest {
    @Test void protectsEveryReviewMethod() {
        for (String method : new String[]{"GET", "HEAD", "POST"}) {
            assertEquals(Access.ADMIN, required("/api/revision", method));
            assertFalse(permits(required("/api/revision", method), "JUGADOR"));
            assertFalse(permits(required("/api/revision", method), "EDITOR"));
            assertTrue(permits(required("/api/revision", method), "ADMIN"));
        }
    }
    @Test void deniesUnknownRoutesRolesAndMethods() {
        assertEquals(Access.DENY, required("/api/unknown", "GET"));
        assertEquals(Access.DENY, required("/api/revision", "DELETE"));
        assertEquals(Access.DENY, required("/css/styles.css", "POST"));
        assertEquals(Access.DENY, required("/secret.html", "GET"));
        assertFalse(permits(Access.EDITOR, "UNKNOWN"));
        assertFalse(permits(Access.USER, null));
        assertFalse(permits(Access.EDITOR, "JUGADOR"));
    }
    @Test void preservesPublicBootstrapAndPrivateEndpoints() {
        assertEquals(Access.PUBLIC, required("/api/sesion", "GET"));
        assertEquals(Access.PUBLIC, required("/api/login", "POST"));
        assertEquals(Access.PUBLIC, required("/api/registro", "POST"));
        assertEquals(Access.PUBLIC, required("/js/api.js", "GET"));
        for (String endpoint : new String[]{"/api/tienda", "/api/zip", "/api/caratula", "/api/avatar", "/api/biblioteca"}) {
            assertEquals(Access.USER, required(endpoint, "GET"));
        }
        assertEquals(Access.EDITOR, required("/api/mispublicaciones", "DELETE"));
        assertEquals(Access.EDITOR, required("/api/editor", "POST"));
    }
}
