package com.example.proyectojavalenguajemarcas.security;

import java.util.Set;

public final class AccessPolicy {
    public enum Access { PUBLIC, USER, EDITOR, ADMIN, DENY }
    private static final Set<String> ROLES = Set.of("JUGADOR", "EDITOR", "ADMIN");
    private AccessPolicy() {}

    public static boolean validRole(String role) { return role != null && ROLES.contains(role); }

    public static Access required(String path, String method) {
        boolean read = method.equals("GET") || method.equals("HEAD");
        return switch (path) {
            case "/", "/index.html", "/registro.html" -> read ? Access.PUBLIC : Access.DENY;
            case "/tienda.html", "/biblioteca.html" -> read ? Access.USER : Access.DENY;
            case "/editor.html", "/mis_publicaciones.html" -> read ? Access.EDITOR : Access.DENY;
            case "/revision.html", "/lista_revision.html" -> read ? Access.ADMIN : Access.DENY;
            case "/api/login", "/api/registro" -> method.equals("POST") ? Access.PUBLIC : Access.DENY;
            case "/api/sesion" -> read ? Access.PUBLIC : Access.DENY;
            case "/api/logout" -> method.equals("POST") ? Access.USER : Access.DENY;
            case "/api/tienda", "/api/zip", "/api/caratula" -> read ? Access.USER : Access.DENY;
            case "/api/avatar", "/api/biblioteca" -> read || method.equals("POST") ? Access.USER : Access.DENY;
            case "/api/editor" -> method.equals("POST") ? Access.EDITOR : Access.DENY;
            case "/api/mispublicaciones" -> read || method.equals("DELETE") ? Access.EDITOR : Access.DENY;
            case "/api/revision" -> read || method.equals("POST") ? Access.ADMIN : Access.DENY;
            default -> read && (path.startsWith("/css/") && path.endsWith(".css")
                    || path.startsWith("/js/") && path.endsWith(".js")) ? Access.PUBLIC : Access.DENY;
        };
    }

    public static boolean permits(Access required, String role) {
        if (required == Access.PUBLIC) return true;
        if (!validRole(role)) return false;
        return switch (required) {
            case USER -> true;
            case EDITOR -> role.equals("EDITOR") || role.equals("ADMIN");
            case ADMIN -> role.equals("ADMIN");
            default -> false;
        };
    }
}
