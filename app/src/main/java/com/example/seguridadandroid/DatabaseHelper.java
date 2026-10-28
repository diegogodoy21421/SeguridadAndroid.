package com.example.seguridadandroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "seguridad_android.db";
    private static final int VERSION_BD = 2;

    public DatabaseHelper(Context context) {
        super(
                context.getApplicationContext(),
                NOMBRE_BD,
                null,
                VERSION_BD
        );
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE usuarios (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL COLLATE NOCASE UNIQUE, " +
                        "password_hash TEXT NOT NULL)"
        );

        crearTablaNotas(db);
    }

    private void crearTablaNotas(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE notas (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "usuario_id INTEGER NOT NULL, " +
                        "contenido_cifrado TEXT NOT NULL, " +
                        "FOREIGN KEY(usuario_id) REFERENCES usuarios(id) " +
                        "ON DELETE CASCADE)"
        );

        db.execSQL(
                "CREATE INDEX index_notas_usuario " +
                        "ON notas(usuario_id)"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        if (oldVersion < 2) {
            crearTablaNotas(db);
        }
    }

    public long registrarUsuario(
            String username,
            char[] password
    ) throws GeneralSecurityException {

        String hash = PasswordUtils.crearHash(password);

        ContentValues datos = new ContentValues();
        datos.put("username", username);
        datos.put("password_hash", hash);

        return getWritableDatabase().insertOrThrow(
                "usuarios",
                null,
                datos
        );
    }

    public long autenticar(
            String username,
            char[] password
    ) throws GeneralSecurityException {

        long idUsuario;
        String hashGuardado;

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, password_hash " +
                        "FROM usuarios WHERE username = ?",
                new String[]{username}
        )) {
            if (!cursor.moveToFirst()) {
                return -1;
            }

            idUsuario = cursor.getLong(
                    cursor.getColumnIndexOrThrow("id")
            );

            hashGuardado = cursor.getString(
                    cursor.getColumnIndexOrThrow("password_hash")
            );
        }

        if (PasswordUtils.verificar(password, hashGuardado)) {
            return idUsuario;
        }

        return -1;
    }

    public long guardarNota(long idUsuario, String contenido)
            throws Exception {

        validarIdUsuario(idUsuario);

        if (contenido == null || contenido.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La nota no puede estar vacía"
            );
        }

        if (contenido.length() > 5000) {
            throw new IllegalArgumentException(
                    "La nota admite hasta 5000 caracteres"
            );
        }

        String contenidoCifrado = CryptoUtils.cifrar(
                contenido,
                idUsuario
        );

        ContentValues datos = new ContentValues();
        datos.put("usuario_id", idUsuario);
        datos.put("contenido_cifrado", contenidoCifrado);

        return getWritableDatabase().insertOrThrow(
                "notas",
                null,
                datos
        );
    }

    public List<Nota> obtenerNotas(long idUsuario)
            throws Exception {

        validarIdUsuario(idUsuario);

        List<Nota> notas = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, contenido_cifrado FROM notas " +
                        "WHERE usuario_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(idUsuario)}
        )) {
            while (cursor.moveToNext()) {
                long idNota = cursor.getLong(
                        cursor.getColumnIndexOrThrow("id")
                );

                String cifrado = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "contenido_cifrado"
                        )
                );

                String contenido = CryptoUtils.descifrar(
                        cifrado,
                        idUsuario
                );

                notas.add(new Nota(idNota, contenido));
            }
        }

        return notas;
    }

    public boolean eliminarNota(long idNota, long idUsuario) {
        validarIdUsuario(idUsuario);

        int filas = getWritableDatabase().delete(
                "notas",
                "id = ? AND usuario_id = ?",
                new String[]{
                        String.valueOf(idNota),
                        String.valueOf(idUsuario)
                }
        );

        return filas > 0;
    }

    private void validarIdUsuario(long idUsuario) {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "Identificador de usuario no válido"
            );
        }
    }

    public static final class Nota {

        public final long id;
        public final String contenido;

        public Nota(long id, String contenido) {
            this.id = id;
            this.contenido = contenido;
        }

        @Override
        public String toString() {
            return contenido;
        }
    }
}