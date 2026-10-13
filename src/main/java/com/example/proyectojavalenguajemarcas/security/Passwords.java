package com.example.proyectojavalenguajemarcas.security;

import com.password4j.Argon2Function;
import com.password4j.Password;
import com.password4j.types.Argon2;

public final class Passwords {
    private static final Argon2Function ARGON2 = Argon2Function.getInstance(19456, 2, 1, 32, Argon2.ID);
    private static final String DUMMY = hash("Dummy credential used only for timing");
    private Passwords() {}

    public static String hash(String password) {
        return Password.hash(password).addRandomSalt(16).with(ARGON2).getResult();
    }

    public static boolean verify(String password, String hash) {
        if (hash == null || !hash.startsWith("$argon2id$")) {
            Password.check(password, DUMMY).with(ARGON2);
            return false;
        }
        try {
            return Password.check(password, hash).withArgon2();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean validNewPassword(String password) {
        int length = password.codePointCount(0, password.length());
        return length >= 15 && length <= 128;
    }
}
