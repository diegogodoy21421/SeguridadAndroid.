package com.example.seguridadandroid;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class CryptoUtils {

    private static final String ALIAS = "SeguridadAndroidNotas";
    private static final String PROVEEDOR = "AndroidKeyStore";
    private static final String TRANSFORMACION = "AES/GCM/NoPadding";

    private CryptoUtils() {
    }

    private static synchronized SecretKey obtenerClave(boolean crear)
            throws Exception {

        KeyStore almacen = KeyStore.getInstance(PROVEEDOR);
        almacen.load(null);

        if (almacen.containsAlias(ALIAS)) {
            return (SecretKey) almacen.getKey(ALIAS, null);
        }

        if (!crear) {
            throw new IllegalStateException(
                    "La clave de las notas no está disponible"
            );
        }

        KeyGenerator generador = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                PROVEEDOR
        );

        generador.init(
                new KeyGenParameterSpec.Builder(
                        ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT |
                                KeyProperties.PURPOSE_DECRYPT
                )
                        .setKeySize(256)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(
                                KeyProperties.ENCRYPTION_PADDING_NONE
                        )
                        .setRandomizedEncryptionRequired(true)
                        .build()
        );

        return generador.generateKey();
    }

    public static String cifrar(String texto, long idUsuario)
            throws Exception {

        Cipher cipher = Cipher.getInstance(TRANSFORMACION);

        // Android genera un IV aleatorio para esta operación.
        cipher.init(Cipher.ENCRYPT_MODE, obtenerClave(true));

        // Vincula el contenido cifrado con su propietario.
        cipher.updateAAD(datosPropietario(idUsuario));

        byte[] cifrado = cipher.doFinal(
                texto.getBytes(StandardCharsets.UTF_8)
        );

        return "v1:" +
                Base64.encodeToString(
                        cipher.getIV(),
                        Base64.NO_WRAP
                ) + ":" +
                Base64.encodeToString(
                        cifrado,
                        Base64.NO_WRAP
                );
    }

    public static String descifrar(String registro, long idUsuario)
            throws Exception {

        String[] partes = registro.split(":");

        if (partes.length != 3 || !"v1".equals(partes[0])) {
            throw new IllegalArgumentException(
                    "Formato de nota no válido"
            );
        }

        byte[] iv = Base64.decode(partes[1], Base64.NO_WRAP);
        byte[] cifrado = Base64.decode(partes[2], Base64.NO_WRAP);

        if (iv.length != 12 || cifrado.length < 16) {
            throw new IllegalArgumentException(
                    "Contenido cifrado no válido"
            );
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMACION);

        cipher.init(
                Cipher.DECRYPT_MODE,
                obtenerClave(false),
                new GCMParameterSpec(128, iv)
        );

        cipher.updateAAD(datosPropietario(idUsuario));

        return new String(
                cipher.doFinal(cifrado),
                StandardCharsets.UTF_8
        );
    }

    private static byte[] datosPropietario(long idUsuario) {
        return ("notas-v1:" + idUsuario)
                .getBytes(StandardCharsets.UTF_8);
    }
}