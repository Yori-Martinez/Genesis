package com.example.proyectojavalenguajemarcas.security;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.*;
import org.junit.jupiter.api.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ProductionCookieIT {
    @Test void productionBootstrapIssuesHostOnlySecureHttpOnlySameSiteCookie() throws Exception {
        Assumptions.assumeTrue("production".equals(System.getenv("APP_ENV")));
        Path temp=Files.createTempDirectory("genesis-cookie-test-");
        Tomcat tomcat=new Tomcat();
        try {
            tomcat.setBaseDir(temp.toString()); tomcat.setPort(0);
            tomcat.getConnector().setProperty("address","127.0.0.1");
            Context context=tomcat.addWebapp("",Path.of("src/main/webapp").toAbsolutePath().toString());
            context.setParentClassLoader(getClass().getClassLoader());
            StandardRoot resources=new StandardRoot(context);
            resources.addPreResources(new DirResourceSet(resources,"/WEB-INF/classes",Path.of("target/classes").toAbsolutePath().toString(),"/"));
            context.setResources(resources);
            tomcat.start();
            assertTrue(context.getState().isAvailable());
            var response=HttpClient.newHttpClient().send(HttpRequest.newBuilder(
                    URI.create("http://127.0.0.1:"+tomcat.getConnector().getLocalPort()+"/api/sesion"))
                    .header("X-CSRF-Token","Fetch").GET().build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(200,response.statusCode());
            String cookie=response.headers().firstValue("Set-Cookie").orElseThrow().toLowerCase();
            assertTrue(cookie.contains("; secure"),cookie);
            assertTrue(cookie.contains("; httponly"),cookie);
            assertTrue(cookie.contains("samesite=lax"),cookie);
            assertFalse(cookie.contains("domain="),cookie);
        } finally {
            tomcat.stop(); tomcat.destroy();
            try (var files=Files.walk(temp)) {
                for (Path path:files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
}
