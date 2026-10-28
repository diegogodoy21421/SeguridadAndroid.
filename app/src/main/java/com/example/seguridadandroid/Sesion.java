package com.example.seguridadandroid;

public final class Sesion {

    private static long idUsuario = -1;
    private static String username = "";

    private Sesion() {
    }

    public static synchronized void iniciar(long id, String nombre) {
        idUsuario = id;
        username = nombre;
    }

    public static synchronized long obtenerId() {
        return idUsuario;
    }

    public static synchronized String obtenerUsername() {
        return username;
    }

    public static synchronized void cerrar() {
        idUsuario = -1;
        username = "";
    }
}