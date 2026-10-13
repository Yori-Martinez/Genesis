package com.example.proyectojavalenguajemarcas.security;

import com.example.proyectojavalenguajemarcas.model.*;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SessionsTest {
    static class StoredSession {
        final Map<String, Object> values = new HashMap<>();
        boolean invalidated;
        final HttpSession session = (HttpSession) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{HttpSession.class}, (p,m,a) -> {
            switch (m.getName()) {
                case "getAttribute": return values.get(a[0]);
                case "setAttribute": values.put((String)a[0], a[1]); return null;
                case "invalidate": invalidated = true; values.clear(); return null;
                default: throw new UnsupportedOperationException(m.getName());
            }
        });
    }
    private StoredSession stored() {
        StoredSession state = new StoredSession();
        state.values.put("usuarioLogueado", new Usuario(1, "player", "p@example.com", "JUGADOR"));
        state.values.put("authenticatedAt", System.currentTimeMillis());
        state.values.put("authVersion", 0);
        state.values.put("loginRole", "JUGADOR");
        return state;
    }
    private UsuarioDAO dao(String role, boolean blocked, int version) {
        return new UsuarioDAO() {
            @Override public Account findById(int id) {
                return new Account(new Usuario(id, "player", "p@example.com", role), null, blocked, version);
            }
        };
    }
    @Test void revokesExpiredChangedAndBlockedSessions() throws Exception {
        for (UsuarioDAO changed : new UsuarioDAO[]{dao("ADMIN",false,0), dao("JUGADOR",true,0), dao("JUGADOR",false,1), dao("UNKNOWN",false,0)}) {
            StoredSession state = stored();
            assertNull(Sessions.resolve(state.session, changed));
            assertTrue(state.invalidated);
        }
        StoredSession expired = stored();
        expired.values.put("authenticatedAt", System.currentTimeMillis() - Sessions.MAX_AGE_MS);
        assertNull(Sessions.resolve(expired.session, dao("JUGADOR",false,0)));
        assertTrue(expired.invalidated);
    }
    @Test void rejectsLegacyAndDeletedUsersAndRefreshesValidIdentity() throws Exception {
        StoredSession legacy = stored();
        legacy.values.remove("authVersion");
        assertNull(Sessions.resolve(legacy.session, dao("JUGADOR",false,0)));
        StoredSession deleted = stored();
        assertNull(Sessions.resolve(deleted.session, new UsuarioDAO() { @Override public Account findById(int id) { return null; } }));
        StoredSession valid = stored();
        assertEquals("JUGADOR", Sessions.resolve(valid.session, dao("JUGADOR",false,0)).getRol());
        assertFalse(valid.invalidated);
    }
}
