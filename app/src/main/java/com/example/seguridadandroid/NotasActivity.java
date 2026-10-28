package com.example.seguridadandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotasActivity extends AppCompatActivity {

    private long idUsuario;

    private DatabaseHelper databaseHelper;
    private EditText edtNota;
    private TextView tvEstadoNotas;
    private Button btnGuardarNota;
    private Button btnCerrarSesion;
    private Button btnAbrirMapa;

    private ArrayAdapter<String> adapter;

    private final List<DatabaseHelper.Nota> notas = new ArrayList<>();
    private final List<String> resumenes = new ArrayList<>();

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private boolean procesando = false;
    private boolean saliendo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        idUsuario = Sesion.obtenerId();

        if (idUsuario <= 0) {
            volverAlLogin();
            return;
        }

        setContentView(R.layout.activity_notas);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.mainNotas),
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

        databaseHelper = new DatabaseHelper(this);

        TextView tvUsuarioActual =
                findViewById(R.id.tvUsuarioActual);

        edtNota = findViewById(R.id.edtNota);
        tvEstadoNotas = findViewById(R.id.tvEstadoNotas);
        btnGuardarNota = findViewById(R.id.btnGuardarNota);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnAbrirMapa = findViewById(R.id.btnAbrirMapa);

        // Evita guardar el borrador en el estado de la Activity.
        edtNota.setSaveEnabled(false);

        tvUsuarioActual.setText(
                "Notas de " + Sesion.obtenerUsername()
        );

        ListView listaNotas = findViewById(R.id.listaNotas);

        listaNotas.setEmptyView(
                findViewById(R.id.tvSinNotas)
        );

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                resumenes
        );

        listaNotas.setAdapter(adapter);

        btnGuardarNota.setOnClickListener(v -> guardarNota());
        btnCerrarSesion.setOnClickListener(v -> confirmarSalida());
        btnAbrirMapa.setOnClickListener(v -> abrirMapa());

        listaNotas.setOnItemClickListener(
                (parent, view, position, id) -> {
                    if (procesando || !sesionValida()) {
                        return;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Nota privada")
                            .setMessage(notas.get(position).contenido)
                            .setPositiveButton("Cerrar", null)
                            .show();
                }
        );

        listaNotas.setOnItemLongClickListener(
                (parent, view, position, id) -> {
                    if (procesando || !sesionValida()) {
                        return true;
                    }

                    long idNota = notas.get(position).id;

                    new AlertDialog.Builder(this)
                            .setTitle("Eliminar nota")
                            .setMessage("¿Deseas eliminar esta nota?")
                            .setPositiveButton(
                                    "Eliminar",
                                    (dialog, which) -> eliminarNota(idNota)
                            )
                            .setNegativeButton("Cancelar", null)
                            .show();

                    return true;
                }
        );

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        confirmarSalida();
                    }
                }
        );

        cargarNotas();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!saliendo && !sesionValida()) {
            volverAlLogin();
        }
    }

    private boolean sesionValida() {
        return idUsuario > 0
                && Sesion.obtenerId() == idUsuario;
    }

    private boolean puedeActualizarPantalla() {
        return !isFinishing()
                && !isDestroyed()
                && sesionValida();
    }

    private void abrirMapa() {
        if (procesando || saliendo) {
            return;
        }

        if (!sesionValida()) {
            volverAlLogin();
            return;
        }

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void cargarNotas() {
        if (!sesionValida()) {
            volverAlLogin();
            return;
        }

        cambiarEstado(true);
        tvEstadoNotas.setText("Cargando notas…");

        executor.execute(() -> {
            try {
                List<DatabaseHelper.Nota> resultado =
                        databaseHelper.obtenerNotas(idUsuario);

                runOnUiThread(() -> {
                    if (!puedeActualizarPantalla()) {
                        return;
                    }

                    actualizarListado(resultado);
                    tvEstadoNotas.setText("");
                    cambiarEstado(false);
                });

            } catch (Exception error) {
                mostrarError("No se pudieron cargar las notas.");
            }
        });
    }

    private void guardarNota() {
        if (procesando) {
            return;
        }

        if (!sesionValida()) {
            volverAlLogin();
            return;
        }

        String contenido = edtNota.getText().toString();

        if (contenido.trim().isEmpty()) {
            edtNota.setError("Escribe una nota");
            return;
        }

        cambiarEstado(true);
        tvEstadoNotas.setText("Guardando nota…");

        executor.execute(() -> {
            try {
                if (!sesionValida()) {
                    return;
                }

                databaseHelper.guardarNota(
                        idUsuario,
                        contenido
                );

                List<DatabaseHelper.Nota> resultado =
                        databaseHelper.obtenerNotas(idUsuario);

                runOnUiThread(() -> {
                    if (!puedeActualizarPantalla()) {
                        return;
                    }

                    actualizarListado(resultado);
                    edtNota.setText("");
                    tvEstadoNotas.setText("Nota guardada.");
                    cambiarEstado(false);
                });

            } catch (Exception error) {
                mostrarError("No se pudo guardar la nota.");
            }
        });
    }

    private void eliminarNota(long idNota) {
        if (procesando) {
            return;
        }

        if (!sesionValida()) {
            volverAlLogin();
            return;
        }

        cambiarEstado(true);

        executor.execute(() -> {
            try {
                if (!sesionValida()) {
                    return;
                }

                boolean eliminada = databaseHelper.eliminarNota(
                        idNota,
                        idUsuario
                );

                List<DatabaseHelper.Nota> resultado =
                        databaseHelper.obtenerNotas(idUsuario);

                runOnUiThread(() -> {
                    if (!puedeActualizarPantalla()) {
                        return;
                    }

                    actualizarListado(resultado);

                    tvEstadoNotas.setText(
                            eliminada
                                    ? "Nota eliminada."
                                    : "La nota no está disponible."
                    );

                    cambiarEstado(false);
                });

            } catch (Exception error) {
                mostrarError("No se pudo eliminar la nota.");
            }
        });
    }

    private void actualizarListado(
            List<DatabaseHelper.Nota> resultado
    ) {
        notas.clear();
        notas.addAll(resultado);

        resumenes.clear();

        for (DatabaseHelper.Nota nota : notas) {
            String resumen = nota.contenido
                    .replace('\n', ' ')
                    .replace('\r', ' ');

            if (resumen.length() > 100) {
                resumen = resumen.substring(0, 100) + "…";
            }

            resumenes.add(resumen);
        }

        adapter.notifyDataSetChanged();
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> {
            if (!puedeActualizarPantalla()) {
                return;
            }

            tvEstadoNotas.setText(mensaje);
            cambiarEstado(false);
        });
    }

    private void cambiarEstado(boolean ocupado) {
        procesando = ocupado;
        btnGuardarNota.setEnabled(!ocupado);
        btnCerrarSesion.setEnabled(!ocupado);
        btnAbrirMapa.setEnabled(!ocupado);
        edtNota.setEnabled(!ocupado);
    }

    private void confirmarSalida() {
        if (procesando || saliendo) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseas cerrar tu sesión?")
                .setPositiveButton("Salir", (dialog, which) -> {
                    Sesion.cerrar();
                    volverAlLogin();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void volverAlLogin() {
        if (saliendo) {
            return;
        }

        saliendo = true;

        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            executor.execute(() -> databaseHelper.close());
        }

        executor.shutdown();
        super.onDestroy();
    }
}