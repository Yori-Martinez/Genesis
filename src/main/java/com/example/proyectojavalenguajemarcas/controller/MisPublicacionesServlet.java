package com.example.proyectojavalenguajemarcas.controller;

import com.example.proyectojavalenguajemarcas.model.Juego;
import com.example.proyectojavalenguajemarcas.model.JuegoDAO;
import com.example.proyectojavalenguajemarcas.model.Usuario;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

// Definimos la ruta en la que este Servlet va a escuchar las peticiones de JavaScript
@WebServlet("/api/mispublicaciones")
public class MisPublicacionesServlet extends HttpServlet {

    // Instanciamos el DAO para poder hablar con la base de datos
    private JuegoDAO juegoDAO = new JuegoDAO();
    // Instanciamos Gson para traducir entre Objetos Java y texto JSON
    private Gson gson = new Gson();

    // =========================================================================
    // MÉTODO GET: Sirve para LEER datos (Traer los juegos del editor)
    // =========================================================================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Preparamos la respuesta diciendo que vamos a devolver un texto en formato JSON y con tildes (UTF-8)
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // 2. SEGURIDAD: Intentamos recuperar la sesión actual.
        // El 'false' significa: "Dame la sesión que ya existe, pero si no hay ninguna, NO crees una nueva, devuélveme null".
        HttpSession session = req.getSession(false);

        // Si la sesión es null, o si dentro de la sesión no existe el atributo "usuarioLogueado"...
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            // Devolvemos un error 401 (No autorizado) y cortamos la ejecución con el 'return'
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"No autorizado\"}");
            return;
        }

        // 3. Extraemos el objeto Usuario que guardamos en la sesión durante el Login
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        // Si el usuario es un simple "JUGADOR", no tiene permisos para ver esta pantalla de editores
        if (usuario.getRol().equals("JUGADOR")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN); // Error 403 (Prohibido)
            resp.getWriter().write("{\"error\": \"Permisos insuficientes\"}");
            return;
        }

        // 4. LÓGICA PRINCIPAL: Usamos el DAO para buscar solo los juegos cuyo 'id_editor' coincida con el ID de este usuario
        List<Juego> misJuegos = juegoDAO.obtenerJuegosPorEditor(usuario.getId());

        // 5. RESPUESTA: Convertimos esa lista de Java a un texto JSON con Gson y se la enviamos al navegador (al JavaScript)
        resp.getWriter().write(gson.toJson(misJuegos));
    }


    // =========================================================================
    // MÉTODO DELETE: Sirve para BORRAR datos (Eliminar un juego)
    // =========================================================================
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Configuramos el formato de respuesta igual que antes
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Creamos un objeto JSON vacío donde iremos metiendo la respuesta (ej: {"exito": true, "mensaje": "..."})
        JsonObject jsonRespuesta = new JsonObject();

        // 2. SEGURIDAD: Comprobamos de nuevo que el usuario tiene la sesión iniciada
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            jsonRespuesta.addProperty("exito", false);
            resp.getWriter().write(gson.toJson(jsonRespuesta));
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        try {
            // 3. LEER LA PETICIÓN: El navegador nos envía el ID del juego a borrar oculto en el "cuerpo" (body) de la petición.
            // Usamos un BufferedReader para leer ese texto plano...
            BufferedReader reader = req.getReader();
            // ... y le pedimos a Gson que convierta ese texto plano en un objeto JsonObject para poder manipularlo en Java
            JsonObject jsonRecibido = gson.fromJson(reader, JsonObject.class);

            // Extraemos el número ID del JSON que nos ha enviado JavaScript (ej: {"id": 5})
            int idJuego = jsonRecibido.get("id").getAsInt();

            // 4. LÓGICA PRINCIPAL: Le pedimos al DAO que borre el juego.
            // Le pasamos el ID del juego Y TAMBIÉN el ID del usuario como medida de seguridad,
            // para asegurarnos de que un editor no pueda borrar el juego de otro editor usando trucos en el navegador.
            boolean borrado = juegoDAO.eliminarJuego(idJuego, usuario.getId());

            // 5. RESPUESTA: Evaluamos si el DAO logró borrarlo (true) o no (false) y montamos el JSON de respuesta
            if (borrado) {
                jsonRespuesta.addProperty("exito", true);
                jsonRespuesta.addProperty("mensaje", "Juego eliminado de la base de datos");
            } else {
                jsonRespuesta.addProperty("exito", false);
                jsonRespuesta.addProperty("mensaje", "No se pudo eliminar el juego");
            }

        } catch (Exception e) {
            // Si algo explota (ej: se cae la base de datos, el JSON está mal formado...), lo capturamos aquí para que el servidor no se apague
            e.printStackTrace();
            jsonRespuesta.addProperty("exito", false);
            jsonRespuesta.addProperty("mensaje", "Error procesando la solicitud");
        }

        // 6. Finalmente, enviamos nuestro JSON de respuesta de vuelta al navegador
        resp.getWriter().write(gson.toJson(jsonRespuesta));
    }
}