package com.example.proyectojavalenguajemarcas.security;

import com.example.proyectojavalenguajemarcas.model.ConexionBD;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.*;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicLong;

/** Counters are shared by every container; no sticky sessions or paid Redis required. */
public class SharedRateLimit implements Filter {
    private final AtomicLong lastCleanup = new AtomicLong(-1);

    static String clientIp(HttpServletRequest req) {
        // Vercel overwrites X-Forwarded-For at its ingress. Locally, ignore this header.
        String forwarded = "1".equals(System.getenv("VERCEL")) ? req.getHeader("x-forwarded-for") : null;
        return forwarded != null && forwarded.matches("[0-9a-fA-F:.]{2,64}") ? forwarded : req.getRemoteAddr();
    }

    public static boolean allow(Connection con, String key, long bucket, int limit) throws SQLException {
        boolean auto = con.getAutoCommit();
        con.setAutoCommit(false);
        try (PreparedStatement increment = con.prepareStatement("INSERT INTO genesis_rate_limits (key_hash, bucket_id, attempts) VALUES (?, ?, 1) "
                + "ON DUPLICATE KEY UPDATE attempts = LEAST(attempts + 1, 1000000)");
             PreparedStatement read = con.prepareStatement("SELECT attempts FROM genesis_rate_limits WHERE key_hash = ? AND bucket_id = ?")) {
            increment.setString(1, key); increment.setLong(2, bucket); increment.executeUpdate();
            read.setString(1, key); read.setLong(2, bucket);
            boolean allowed;
            try (ResultSet rs = read.executeQuery()) { allowed = rs.next() && rs.getInt(1) <= limit; }
            con.commit();
            return allowed;
        } catch (SQLException e) { con.rollback(); throw e; }
        finally { con.setAutoCommit(auto); }
    }

    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getServletPath();
        int limit = path.equals("/api/login") ? 20 : path.equals("/api/registro") ? 5 : 120;
        long bucket = System.currentTimeMillis() / 60000;
        try (Connection con = ConexionBD.getConnection()) {
            long previous = lastCleanup.get();
            if (previous != bucket && lastCleanup.compareAndSet(previous, bucket)) {
                try (PreparedStatement cleanup = con.prepareStatement("DELETE FROM genesis_rate_limits WHERE bucket_id < ?")) {
                    cleanup.setLong(1, bucket - 2); cleanup.executeUpdate();
                }
                try (PreparedStatement cleanup = con.prepareStatement("DELETE FROM GENESIS_SESSION WHERE EXPIRY_TIME < ?")) {
                    cleanup.setLong(1, System.currentTimeMillis()); cleanup.executeUpdate();
                }
            }
            String key = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest((path + ":" + clientIp(req)).getBytes(StandardCharsets.UTF_8)));
            if (!allow(con, key, bucket, limit)) {
                resp.setHeader("Retry-After", Long.toString(60 - (System.currentTimeMillis() / 1000) % 60));
                HttpJson.error(resp, 429, "Demasiados intentos. Espera un minuto.");
                return;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            HttpJson.error(resp, 503, "Servicio temporalmente no disponible.");
            return;
        }
        chain.doFilter(request, response);
    }
}
