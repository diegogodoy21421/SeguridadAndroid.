package com.example.seguridadandroid;

import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtils {

    private static final int ITERACIONES = 600_000;
    private static final int LONGITUD_SAL = 16;
    private static final int LONGITUD_HASH_BITS = 256;

    private PasswordUtils() {
    }

    public static String crearHash(char[] password)
            throws GeneralSecurityException {

        byte[] sal = new byte[LONGITUD_SAL];
        new SecureRandom().nextBytes(sal);

        byte[] hash = derivar(password, sal, ITERACIONES);

        try {
            return ITERACIONES + ":" +
                    Base64.encodeToString(sal, Base64.NO_WRAP) + ":" +
                    Base64.encodeToString(hash, Base64.NO_WRAP);
        } finally {
            Arrays.fill(hash, (byte) 0);
        }
    }

    public static boolean verificar(
            char[] password,
            String registro
    ) throws GeneralSecurityException {

        if (registro == null) {
            return false;
        }

        try {
            String[] partes = registro.split(":");

            if (partes.length != 3) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[0]);

            if (iteraciones != ITERACIONES) {
                return false;
            }

            byte[] sal = Base64.decode(partes[1], Base64.NO_WRAP);
            byte[] esperado = Base64.decode(partes[2], Base64.NO_WRAP);

            if (sal.length != LONGITUD_SAL ||
                    esperado.length != LONGITUD_HASH_BITS / 8) {
                return false;
            }

            byte[] obtenido = derivar(password, sal, iteraciones);

            try {
                return MessageDigest.isEqual(esperado, obtenido);
            } finally {
                Arrays.fill(obtenido, (byte) 0);
                Arrays.fill(esperado, (byte) 0);
            }

        } catch (IllegalArgumentException error) {
            return false;
        }
    }

    private static byte[] derivar(
            char[] password,
            byte[] sal,
            int iteraciones
    ) throws GeneralSecurityException {

        PBEKeySpec especificacion = new PBEKeySpec(
                password,
                sal,
                iteraciones,
                LONGITUD_HASH_BITS
        );

        try {
            SecretKeyFactory generador =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            return generador.generateSecret(
                    especificacion
            ).getEncoded();

        } finally {
            especificacion.clearPassword();
        }
    }
}