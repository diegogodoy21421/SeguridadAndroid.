package com.example.seguridadandroid;

import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText edtUsuario;
    private EditText edtClave;
    private Button btnIngresar;
    private Button btnCrearCuenta;
    private TextView tvMensaje;

    private DatabaseHelper databaseHelper;

    // Las operaciones de base de datos y hash se ejecutan
    // fuera del hilo de la interfaz.
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private boolean procesando = false;

    private int intentosFallidos = 0;
    private long bloqueoHasta = 0;

    private static final int MAX_INTENTOS = 5;
    private static final long BLOQUEO_MS = 30_000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (vista, insets) -> {
                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    vista.setPadding(
                            barras.left,
                            barras.top,
                            barras.right,
                            barras.bottom
                    );

                    return insets;
                }
        );

        edtUsuario = findViewById(R.id.edtUsuario);
        edtClave = findViewById(R.id.edtClave);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        tvMensaje = findViewById(R.id.tvMensaje);

        // No conservar el contenido de la contraseña al recrear la pantalla.
        edtClave.setSaveEnabled(false);

        databaseHelper = new DatabaseHelper(this);

        if (savedInstanceState != null) {
            intentosFallidos = savedInstanceState.getInt(
                    "intentosFallidos",
                    0
            );

            bloqueoHasta = savedInstanceState.getLong(
                    "bloqueoHasta",
                    0
            );
        }

        btnCrearCuenta.setOnClickListener(v -> crearCuenta());
        btnIngresar.setOnClickListener(v -> iniciarSesion());
    }

    private void crearCuenta() {
        if (procesando) {
            return;
        }

        limpiarErrores();

        String username = leerUsuario();
        String password = edtClave.getText().toString();

        if (!validarUsuario(username)) {
            return;
        }

        if (password.length() < 10 || password.length() > 128) {
            edtClave.setError(
                    "La contraseña debe tener entre 10 y 128 caracteres"
            );
            edtClave.requestFocus();
            return;
        }

        char[] clave = password.toCharArray();

        cambiarEstadoProcesando(true);
        mostrarMensaje("Creando cuenta…", false);

        executor.execute(() -> {
            String mensaje;
            boolean correcto;

            try {
                databaseHelper.registrarUsuario(username, clave);

                mensaje = "Cuenta creada. Ya puedes iniciar sesión.";
                correcto = true;

            } catch (SQLiteConstraintException error) {
                mensaje = "No se pudo crear la cuenta con ese usuario.";
                correcto = false;

            } catch (Exception error) {
                mensaje = "No se pudo crear la cuenta. Inténtalo nuevamente.";
                correcto = false;

            } finally {
                Arrays.fill(clave, '\0');
            }

            final String resultado = mensaje;
            final boolean exito = correcto;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                cambiarEstadoProcesando(false);
                mostrarMensaje(resultado, exito);

                if (exito) {
                    edtClave.setText("");
                    edtClave.requestFocus();
                }
            });
        });
    }

    private void iniciarSesion() {
        if (procesando) {
            return;
        }

        limpiarErrores();

        long ahora = SystemClock.elapsedRealtime();

        if (ahora < bloqueoHasta) {
            long segundos = (bloqueoHasta - ahora + 999) / 1000;

            mostrarMensaje(
                    "Demasiados intentos. Espera " +
                            segundos + " segundos.",
                    false
            );

            return;
        }

        if (bloqueoHasta != 0) {
            bloqueoHasta = 0;
            intentosFallidos = 0;
        }

        String username = leerUsuario();
        String password = edtClave.getText().toString();

        if (!validarUsuario(username)) {
            return;
        }

        if (password.isEmpty()) {
            edtClave.setError("Ingresa tu contraseña");
            edtClave.requestFocus();
            return;
        }

        if (password.length() > 128) {
            edtClave.setError("La contraseña es demasiado larga");
            return;
        }

        char[] clave = password.toCharArray();

        cambiarEstadoProcesando(true);
        mostrarMensaje("Comprobando credenciales…", false);

        executor.execute(() -> {
            long idUsuario = -1;
            boolean errorInterno = false;

            try {
                idUsuario = databaseHelper.autenticar(username, clave);

            } catch (Exception error) {
                errorInterno = true;

            } finally {
                Arrays.fill(clave, '\0');
            }

            final long usuarioEncontrado = idUsuario;
            final boolean falloInterno = errorInterno;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                cambiarEstadoProcesando(false);
                edtClave.setText("");

                if (falloInterno) {
                    mostrarMensaje(
                            "No se pudo comprobar el acceso. Inténtalo nuevamente.",
                            false
                    );
                    return;
                }

                if (usuarioEncontrado > 0) {
                    intentosFallidos = 0;
                    bloqueoHasta = 0;

                    Sesion.iniciar(usuarioEncontrado, username);

                    android.content.Intent intent =
                            new android.content.Intent(
                                    MainActivity.this,
                                    NotasActivity.class
                            );

                    startActivity(intent);
                    finish();

                } else {
                    intentosFallidos++;

                    if (intentosFallidos >= MAX_INTENTOS) {
                        bloqueoHasta =
                                SystemClock.elapsedRealtime() + BLOQUEO_MS;

                        mostrarMensaje(
                                "Demasiados intentos. Espera 30 segundos.",
                                false
                        );

                    } else {
                        // Mismo mensaje para usuario inexistente
                        // y contraseña incorrecta.
                        mostrarMensaje(
                                "Usuario o contraseña incorrectos.",
                                false
                        );
                    }
                }
            });
        });
    }

    private String leerUsuario() {
        return edtUsuario.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private boolean validarUsuario(String username) {
        if (!username.matches("[a-z0-9_]{3,40}")) {
            edtUsuario.setError(
                    "Usa entre 3 y 40 letras, números o guion bajo"
            );

            edtUsuario.requestFocus();
            return false;
        }

        return true;
    }

    private void limpiarErrores() {
        edtUsuario.setError(null);
        edtClave.setError(null);
        tvMensaje.setText("");
    }

    private void mostrarMensaje(String mensaje, boolean correcto) {
        tvMensaje.setText(mensaje);

        tvMensaje.setTextColor(
                Color.parseColor(correcto ? "#20643A" : "#9C2525")
        );
    }

    private void cambiarEstadoProcesando(boolean ocupado) {
        procesando = ocupado;

        btnIngresar.setEnabled(!ocupado);
        btnCrearCuenta.setEnabled(!ocupado);
        edtUsuario.setEnabled(!ocupado);
        edtClave.setEnabled(!ocupado);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("intentosFallidos", intentosFallidos);
        outState.putLong("bloqueoHasta", bloqueoHasta);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        // Cierra la base después de terminar las operaciones pendientes.
        executor.execute(() -> databaseHelper.close());
        executor.shutdown();

        super.onDestroy();
    }
}