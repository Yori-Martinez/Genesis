package com.example.proyectojavalenguajemarcas.config;

import com.example.proyectojavalenguajemarcas.model.ConexionBD;
import java.sql.*;
import javax.sql.DataSource;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.*;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.http.*;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
@EnableJdbcHttpSession(tableName = "GENESIS_SESSION", maxInactiveIntervalInSeconds = 1800, cleanupCron = "-")
public class SessionConfiguration {
    @Bean public DataSource dataSource() {
        return new AbstractDataSource() {
            @Override public Connection getConnection() throws SQLException { return ConexionBD.getConnection(); }
            @Override public Connection getConnection(String username, String password) throws SQLException {
                throw new SQLException("Las credenciales se configuran mediante variables de entorno.");
            }
        };
    }

    @Bean public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookie = new DefaultCookieSerializer();
        cookie.setCookieName("JSESSIONID");
        cookie.setUseBase64Encoding(false);
        cookie.setCookiePath("/");
        cookie.setUseHttpOnlyCookie(true);
        cookie.setUseSecureCookie(!"development".equals(System.getenv("APP_ENV"))
                || "true".equals(System.getenv("SESSION_COOKIE_SECURE")));
        cookie.setSameSite("Lax");
        return cookie;
    }
}
