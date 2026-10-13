package com.example.proyectojavalenguajemarcas.security;

import com.example.proyectojavalenguajemarcas.tools.AuthAdmin;
import com.google.gson.*;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.*;
import org.junit.jupiter.api.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthHttpIT {
    private Tomcat tomcat;
    private Context context;
    private String origin;
    private Path temp;

    @BeforeEach void start() throws Exception {
        DatabaseFixture.seed();
        temp = Files.createTempDirectory("genesis-http-test-");
        tomcat = new Tomcat();
        tomcat.setBaseDir(temp.toString());
        tomcat.setPort(0);
        tomcat.getConnector().setProperty("address","127.0.0.1");
        context = tomcat.addWebapp("",Path.of("src/main/webapp").toAbsolutePath().toString());
        context.setParentClassLoader(getClass().getClassLoader());
        StandardRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(resources,"/WEB-INF/classes",Path.of("target/classes").toAbsolutePath().toString(),"/"));
        context.setResources(resources);
        tomcat.start();
        assertTrue(context.getState().isAvailable(), "The real web.xml must start successfully");
        origin = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort();
    }

    @AfterEach void stop() throws Exception {
        if (tomcat != null) { tomcat.stop(); tomcat.destroy(); }
        if (temp != null) try (var files = Files.walk(temp)) {
            for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }

    class Browser {
        final CookieManager cookies = new CookieManager(null,CookiePolicy.ACCEPT_ALL);
        final HttpClient client = HttpClient.newBuilder().cookieHandler(cookies).build();
        String csrf;
        HttpResponse<String> get(String path) throws Exception {
            return client.send(HttpRequest.newBuilder(URI.create(origin+path)).GET().build(),HttpResponse.BodyHandlers.ofString());
        }
        HttpResponse<String> bootstrap() throws Exception {
            var response = client.send(HttpRequest.newBuilder(URI.create(origin+"/api/sesion")).header("X-CSRF-Token","Fetch").GET().build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(200,response.statusCode(),response.body());
            csrf = response.headers().firstValue("X-CSRF-Token").orElseThrow();
            return response;
        }
        HttpResponse<String> send(String path,String method,String body,String type,boolean token) throws Exception {
            var builder = HttpRequest.newBuilder(URI.create(origin+path)).header("Content-Type",type)
                    .method(method,HttpRequest.BodyPublishers.ofString(body));
            if (token) builder.header("X-CSRF-Token",csrf);
            return client.send(builder.build(),HttpResponse.BodyHandlers.ofString());
        }
        void login(String username) throws Exception {
            bootstrap();
            var response = send("/api/login","POST",new Gson().toJson(Map.of("username",username,"password","Test password with spaces ")),"application/json",true);
            assertEquals(200,response.statusCode(),response.body());
            bootstrap();
        }
        String id() {
            return cookies.getCookieStore().getCookies().stream().filter(c->c.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
        }
    }

    private static JsonObject object(HttpResponse<String> response) {
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    @Test void anonymousAndForgedBrowserRolesCannotUsePrivateApi() throws Exception {
        Browser anon = new Browser();
        for (String path : List.of("/api/tienda","/api/revision","/api/zip?id=11","/api/avatar","/api/biblioteca")) {
            assertEquals(401,anon.get(path).statusCode(),path);
        }
        assertEquals(403,anon.get("/api/unlisted").statusCode());
        assertEquals(302,anon.get("/revision.html").statusCode());
        Browser player = new Browser(); player.login("player");
        assertEquals(403,player.get("/api/revision").statusCode());
        var verdict = player.send("/api/revision","POST","id=11&estado=RECHAZADO&rol=ADMIN","application/x-www-form-urlencoded",true);
        assertEquals(403,verdict.statusCode());
        assertEquals("PENDIENTE",DatabaseFixture.scalar("SELECT estado FROM juegos WHERE id=11"));
        Browser editor = new Browser(); editor.login("editor");
        assertEquals(403,editor.send("/api/revision","POST","id=11&estado=RECHAZADO","application/x-www-form-urlencoded",true).statusCode());
        Browser admin = new Browser(); admin.login("admin");
        assertEquals(200,admin.get("/api/revision").statusCode());
        assertEquals(200,admin.send("/api/revision","POST","id=11&estado=RECHAZADO","application/x-www-form-urlencoded",true).statusCode());
        assertEquals("RECHAZADO",DatabaseFixture.scalar("SELECT estado FROM juegos WHERE id=11"));
    }

    @Test void filesAndLibraryEnforceOwnershipAndPublication() throws Exception {
        Browser player = new Browser(); player.login("player");
        assertEquals(200,player.get("/api/zip?id=10").statusCode());
        assertEquals(403,player.get("/api/zip?id=11").statusCode());
        assertEquals(400,player.get("/api/zip?id=invalid").statusCode());
        assertEquals(403,player.get("/api/caratula?id=11").statusCode());
        assertFalse(object(player.send("/api/biblioteca","POST","{\"id_juego\":11}","application/json",true)).get("exito").getAsBoolean());
        assertEquals("0",DatabaseFixture.scalar("SELECT COUNT(*) FROM biblioteca WHERE id_usuario=1 AND id_juego=11"));
        Browser outsider = new Browser(); outsider.login("outsider");
        assertEquals(403,outsider.get("/api/zip?id=10").statusCode());
        assertEquals(200,outsider.get("/api/caratula?id=10").statusCode());
        Browser editor = new Browser(); editor.login("editor");
        assertEquals(200,editor.get("/api/zip?id=11").statusCode());
        assertEquals(403,editor.get("/api/zip?id=12").statusCode());
        assertFalse(object(editor.send("/api/mispublicaciones","DELETE","{\"id\":12}","application/json",true)).get("exito").getAsBoolean());
        assertEquals("1",DatabaseFixture.scalar("SELECT COUNT(*) FROM juegos WHERE id=12"));
        Browser admin = new Browser(); admin.login("admin");
        assertEquals(200,admin.get("/api/zip?id=12").statusCode());
    }

    @Test void csrfRotationCookieFlagsLogoutAndUrlTracking() throws Exception {
        Browser browser = new Browser();
        var boot = browser.bootstrap();
        var cookie = boot.headers().firstValue("Set-Cookie").orElseThrow().toLowerCase();
        assertTrue(cookie.contains("httponly"));
        assertTrue(cookie.contains("samesite=lax"));
        assertFalse(cookie.contains("domain="));
        assertEquals(Set.of("autenticado","nombre","rol"),object(boot).keySet());
        String anonymousId = browser.id(), oldNonce = browser.csrf;
        String credentials = "{\"username\":\"player\",\"password\":\"Test password with spaces \"}";
        assertEquals(403,browser.send("/api/login","POST",credentials,"application/json",false).statusCode());
        assertEquals(200,browser.send("/api/login","POST",credentials,"application/json",true).statusCode());
        assertNotEquals(anonymousId,browser.id());
        // The old nonce must not authorize actions in the new session.
        assertEquals(403,browser.send("/api/biblioteca","POST","{\"id_juego\":10}","application/json",true).statusCode());
        browser.bootstrap();
        assertNotEquals(oldNonce,browser.csrf);
        assertEquals(403,browser.send("/api/biblioteca","POST","{\"id_juego\":10}","application/json",false).statusCode());
        String authId = browser.id();
        var noCookies = HttpClient.newHttpClient();
        var urlTracked = noCookies.send(HttpRequest.newBuilder(URI.create(origin+"/api/sesion;jsessionid="+authId)).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertFalse(object(urlTracked).get("autenticado").getAsBoolean());
        assertEquals(200,browser.send("/api/logout","POST","{}","application/json",true).statusCode());
        assertEquals(401,browser.get("/api/biblioteca").statusCode());
        var replay = noCookies.send(HttpRequest.newBuilder(URI.create(origin+"/api/biblioteca")).header("Cookie","JSESSIONID="+authId).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(401,replay.statusCode());
    }

    @Test void revokesSessionsOnRolePasswordAbsoluteAndIdleExpiry() throws Exception {
        Browser browser = new Browser(); browser.login("editor");
        try (Connection con=DatabaseFixture.connection()) { AuthAdmin.setRole(con,"editor","JUGADOR"); }
        assertEquals(401,browser.get("/api/mispublicaciones").statusCode());
        browser.login("player");
        context.getManager().findSession(browser.id()).getSession().setAttribute("authenticatedAt",System.currentTimeMillis()-Sessions.MAX_AGE_MS);
        assertEquals(401,browser.get("/api/biblioteca").statusCode());
        browser.login("player");
        assertEquals(1800, context.getManager().findSession(browser.id()).getMaxInactiveInterval());
        context.getManager().findSession(browser.id()).setMaxInactiveInterval(1);
        Thread.sleep(1100);
        assertEquals(401,browser.get("/api/biblioteca").statusCode());
        browser.login("player");
        try (Connection con=DatabaseFixture.connection()) { AuthAdmin.setPassword(con,"player","New valid password for player"); }
        assertEquals(401,browser.get("/api/biblioteca").statusCode());
    }

    @Test void registrationCannotChooseRoleAndRejectsMalformedCredentials() throws Exception {
        Browser browser = new Browser(); browser.bootstrap();
        var input = "{\"username\":\"newplayer\",\"email\":\"new@example.com\",\"password\":\"Long registration password \",\"rol\":\"ADMIN\"}";
        assertEquals(201,browser.send("/api/registro","POST",input,"application/json",true).statusCode());
        assertEquals("JUGADOR",DatabaseFixture.scalar("SELECT rol FROM usuarios WHERE username='newplayer'"));
        assertEquals(409,browser.send("/api/registro","POST",input,"application/json",true).statusCode());
        assertEquals(400,browser.send("/api/registro","POST",input.replace("Long registration password ","short"),"application/json",true).statusCode());
        assertEquals(400,browser.send("/api/login","POST","{bad json","application/json",true).statusCode());
        assertEquals(400,browser.send("/api/login","POST","{}","application/json",true).statusCode());
        assertEquals(400,browser.send("/api/login","POST","x".repeat(9000),"application/json",true).statusCode());
        var missing = browser.send("/api/login","POST","{\"username\":\"missing\",\"password\":\"wrong\"}","application/json",true);
        var wrong = browser.send("/api/login","POST","{\"username\":\"player\",\"password\":\"wrong\"}","application/json",true);
        assertEquals(401,missing.statusCode()); assertEquals(missing.body(),wrong.body());
    }

    @Test void appliesIndependentExactRateLimitsAndIgnoresForgedIpHeaders() throws Exception {
        Browser browser = new Browser(); browser.bootstrap();
        for (int i=0; i<20; i++) {
            var response = browser.client.send(HttpRequest.newBuilder(URI.create(origin+"/api/login"))
                    .header("Content-Type","application/json").header("X-CSRF-Token",browser.csrf)
                    .header("X-Forwarded-For","198.51.100."+i).POST(HttpRequest.BodyPublishers.ofString("{}")).build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(400,response.statusCode());
        }
        assertEquals(429,browser.send("/api/login","POST","{}","application/json",true).statusCode());
        for (int i=0; i<5; i++) assertEquals(400,browser.send("/api/registro","POST","{}","application/json",true).statusCode());
        assertEquals(429,browser.send("/api/registro","POST","{}","application/json",true).statusCode());
    }

    @Test void preservesEditorAdministratorAndPlayerHappyPaths() throws Exception {
        Browser editor = new Browser(); editor.login("editor");
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(bytes)) {
            zip.putNextEntry(new java.util.zip.ZipEntry("index.html"));
            zip.write("<p>Demo game</p>".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        assertTrue(object(multipart(editor,"/api/editor",Map.of("titulo","Demo title","descripcion","Demo description"),
                "zip","demo.zip","application/zip",bytes.toByteArray())).get("exito").getAsBoolean());
        int game = Integer.parseInt(DatabaseFixture.scalar("SELECT id FROM juegos WHERE titulo='Demo title'"));
        assertEquals("PENDIENTE",DatabaseFixture.scalar("SELECT estado FROM juegos WHERE id="+game));
        assertTrue(editor.get("/api/mispublicaciones").body().contains("Demo title"));
        Browser admin = new Browser(); admin.login("admin");
        byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aY3sAAAAASUVORK5CYII=");
        assertTrue(object(multipart(admin,"/api/revision",Map.of("id",String.valueOf(game),"estado","PUBLICADO"),
                "caratula","cover.png","image/png",png)).get("exito").getAsBoolean());
        Browser player = new Browser(); player.login("player");
        assertTrue(player.get("/api/tienda").body().contains("Demo title"));
        assertEquals(200,player.get("/api/caratula?id="+game).statusCode());
        assertTrue(object(player.send("/api/biblioteca","POST","{\"id_juego\":"+game+"}","application/json",true)).get("exito").getAsBoolean());
        assertTrue(player.get("/api/biblioteca").body().contains("Demo title"));
        assertEquals(200,player.get("/api/zip?id="+game).statusCode());
        assertTrue(object(multipart(player,"/api/avatar",Map.of(),"avatar","avatar.png","image/png",png)).get("exito").getAsBoolean());
        assertEquals(200,player.get("/api/avatar").statusCode());
        assertEquals("1",DatabaseFixture.scalar("SELECT COUNT(*) FROM usuarios WHERE id=1 AND avatar_contenido IS NOT NULL"));
    }

    private HttpResponse<String> multipart(Browser browser,String path,Map<String,String> fields,
            String field,String filename,String type,byte[] file) throws Exception {
        String boundary = "genesis-test-boundary";
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        for (var entry : fields.entrySet()) {
            bytes.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+entry.getKey()+"\"\r\n\r\n"+entry.getValue()+"\r\n")
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        bytes.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+field+"\"; filename=\""+filename+"\"\r\nContent-Type: "+type+"\r\n\r\n")
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        bytes.write(file);
        bytes.write(("\r\n--"+boundary+"--\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return browser.client.send(HttpRequest.newBuilder(URI.create(origin+path))
                .header("Content-Type","multipart/form-data; boundary="+boundary).header("X-CSRF-Token",browser.csrf)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
    }
}
