package com.example.proyectojavalenguajemarcas.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordsTest {
    @Test void hashesWithIndependentSaltsAndVerifiesExactly() {
        String password = " Una contraseña segura con espacios ";
        String first = Passwords.hash(password), second = Passwords.hash(password);
        assertTrue(first.startsWith("$argon2id$v=19$m=19456,t=2,p=1$"));
        assertNotEquals(first, second);
        assertTrue(Passwords.verify(password, first));
        assertFalse(Passwords.verify(password.trim(), first));
        assertFalse(Passwords.verify("incorrecta", first));
        assertFalse(Passwords.verify(password, null));
        assertFalse(Passwords.verify(password, password));
    }
    @Test void enforcesBoundariesIncludingUnicode() {
        assertFalse(Passwords.validNewPassword("x".repeat(14)));
        assertTrue(Passwords.validNewPassword("x".repeat(15)));
        assertTrue(Passwords.validNewPassword("🔐".repeat(128)));
        assertFalse(Passwords.validNewPassword("🔐".repeat(129)));
    }
}
