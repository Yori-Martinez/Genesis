package com.example.proyectojavalenguajemarcas.security;

import com.google.gson.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public final class HttpJson {
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private HttpJson() {}

    public static JsonObject read(HttpServletRequest req) throws IOException {
        if (req.getContentType() == null || !req.getContentType().split(";")[0].trim().equalsIgnoreCase("application/json")) {
            throw new IllegalArgumentException("Se requiere JSON.");
        }
        // Bound credentials parsing even when Content-Length is absent.
        char[] buffer = new char[8193];
        int count = 0, n;
        while (count < buffer.length && (n = req.getReader().read(buffer, count, buffer.length - count)) != -1) count += n;
        if (count == buffer.length) throw new IllegalArgumentException("Petición demasiado grande.");
        try {
            JsonElement parsed = JsonParser.parseString(new String(buffer, 0, count));
            if (!parsed.isJsonObject()) throw new IllegalArgumentException("JSON no válido.");
            return parsed.getAsJsonObject();
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("JSON no válido.");
        }
    }

    public static String string(JsonObject json, String key, int max) {
        JsonElement value = json.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Campos incompletos.");
        }
        String text = value.getAsString();
        if (text.isEmpty() || text.length() > max) throw new IllegalArgumentException("Campo no válido.");
        return text;
    }

    public static int positiveId(String value) {
        try {
            int id = Integer.parseInt(value);
            if (id > 0) return id;
        } catch (NumberFormatException ignored) {}
        throw new IllegalArgumentException("Identificador no válido.");
    }

    public static int positiveId(JsonObject json, String key) {
        JsonElement value = json.get(key);
        if (value == null || !value.isJsonPrimitive()) throw new IllegalArgumentException("Identificador no válido.");
        return positiveId(value.getAsString());
    }

    public static void write(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(GSON.toJson(body));
    }

    public static void error(HttpServletResponse resp, int status, String message) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("exito", false);
        body.addProperty("mensaje", message);
        write(resp, status, body);
    }
}
