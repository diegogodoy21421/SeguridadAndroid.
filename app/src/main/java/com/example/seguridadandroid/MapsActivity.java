package com.example.seguridadandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.Locale;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mapa;
    private Button btnMiUbicacion;
    private TextView txtEstadoUbicacion;

    private FusedLocationProviderClient clienteUbicacion;
    private CancellationTokenSource solicitudUbicacion;
    private Marker marcadorUbicacion;

    private long idUsuario = -1;

    private final ActivityResultLauncher<String[]> permisosUbicacion =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    resultado -> {
                        if (!sesionValida()) {
                            return;
                        }

                        if (tienePermisoUbicacion()) {
                            obtenerUbicacion();
                        } else {
                            txtEstadoUbicacion.setText(
                                    "Permiso denegado. Puedes seguir explorando el mapa."
                            );
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        idUsuario = Sesion.obtenerId();

        if (idUsuario <= 0) {
            volverAlLogin();
            return;
        }

        setContentView(R.layout.activity_maps);

        btnMiUbicacion = findViewById(R.id.btnMiUbicacion);
        txtEstadoUbicacion = findViewById(R.id.txtEstadoUbicacion);

        clienteUbicacion =
                LocationServices.getFusedLocationProviderClient(this);

        btnMiUbicacion.setEnabled(false);

        btnMiUbicacion.setOnClickListener(v -> {
            if (!sesionValida()) {
                volverAlLogin();
                return;
            }

            if (tienePermisoUbicacion()) {
                obtenerUbicacion();
            } else {
                permisosUbicacion.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });

        SupportMapFragment fragmento =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapa);

        if (fragmento != null) {
            fragmento.getMapAsync(this);
        } else {
            txtEstadoUbicacion.setText(
                    "No se pudo iniciar la pantalla del mapa."
            );
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (!sesionValida()) {
            return;
        }

        mapa = googleMap;
        mapa.getUiSettings().setZoomControlsEnabled(true);

        // Vista inicial de Chile; no representa tu ubicación.
        LatLng chile = new LatLng(-30.0, -71.0);

        mapa.moveCamera(
                CameraUpdateFactory.newLatLngZoom(chile, 5f)
        );

        btnMiUbicacion.setEnabled(true);
    }

    private boolean tienePermisoUbicacion() {
        boolean precisa = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean aproximada = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        return precisa || aproximada;
    }

    private void obtenerUbicacion() {
        if (mapa == null || !sesionValida()) {
            return;
        }

        if (!tienePermisoUbicacion()) {
            txtEstadoUbicacion.setText(
                    "Necesitas permitir la ubicación para utilizar este botón."
            );
            return;
        }

        if (solicitudUbicacion != null) {
            solicitudUbicacion.cancel();
        }

        solicitudUbicacion = new CancellationTokenSource();

        btnMiUbicacion.setEnabled(false);
        txtEstadoUbicacion.setText("Buscando tu ubicación…");

        try {
            clienteUbicacion.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    solicitudUbicacion.getToken()
            ).addOnSuccessListener(this, ubicacion -> {
                if (!sesionValida()) {
                    return;
                }

                btnMiUbicacion.setEnabled(true);

                if (ubicacion == null) {
                    txtEstadoUbicacion.setText(
                            "No se pudo obtener la ubicación. "
                                    + "Activa la ubicación del dispositivo "
                                    + "e inténtalo otra vez."
                    );
                    return;
                }

                LatLng posicion = new LatLng(
                        ubicacion.getLatitude(),
                        ubicacion.getLongitude()
                );

                if (marcadorUbicacion != null) {
                    marcadorUbicacion.remove();
                }

                marcadorUbicacion = mapa.addMarker(
                        new MarkerOptions()
                                .position(posicion)
                                .title("Mi ubicación")
                );

                mapa.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(posicion, 15f)
                );

                txtEstadoUbicacion.setText(
                        String.format(
                                Locale.getDefault(),
                                "Latitud: %.5f | Longitud: %.5f",
                                posicion.latitude,
                                posicion.longitude
                        )
                );
            }).addOnFailureListener(this, error -> {
                if (!sesionValida()) {
                    return;
                }

                btnMiUbicacion.setEnabled(true);
                txtEstadoUbicacion.setText(
                        "No se pudo consultar la ubicación. Inténtalo otra vez."
                );
            });
        } catch (SecurityException error) {
            btnMiUbicacion.setEnabled(true);
            txtEstadoUbicacion.setText(
                    "El permiso de ubicación no está disponible."
            );
        }
    }

    private boolean sesionValida() {
        return !isFinishing()
                && !isDestroyed()
                && idUsuario > 0
                && Sesion.obtenerId() == idUsuario;
    }

    private void volverAlLogin() {
        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isFinishing()
                && (idUsuario <= 0 || Sesion.obtenerId() != idUsuario)) {
            volverAlLogin();
        }
    }

    @Override
    protected void onStop() {
        if (solicitudUbicacion != null) {
            solicitudUbicacion.cancel();
        }

        if (btnMiUbicacion != null) {
            btnMiUbicacion.setEnabled(mapa != null);
        }

        super.onStop();
    }
}