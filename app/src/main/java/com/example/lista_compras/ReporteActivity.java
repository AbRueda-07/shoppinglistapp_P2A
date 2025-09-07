package com.example.lista_compras;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReporteActivity extends AppCompatActivity {

    private Spinner spEstado;
    private DatePicker datePicker;
    private Button btnGenerar;
    private Button btnVolver;
    private Button btnGuardar;
    private TextView tvPendientes, tvComprados, tvTotal;
    private DBHelper dbHelper;
    private static final String TAG = "ReporteActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);

        initViews();
        if (initDatabase()) {
            setupSpinner();
            setupListeners();
            generarReporte(); // Generar reporte inicial
        } else {
            showToast("Error inicializando base de datos");
            finish();
        }
    }

    private void initViews() {
        try {
            btnVolver = findViewById(R.id.btnVolverReporte);
            spEstado = findViewById(R.id.spEstadoReporte);
            datePicker = findViewById(R.id.datePickerReporte);
            btnGenerar = findViewById(R.id.btnGenerarReporte);
            btnGuardar = findViewById(R.id.btnGuardarReporte);
            tvPendientes = findViewById(R.id.tvPendientes);
            tvComprados = findViewById(R.id.tvComprados);
            tvTotal = findViewById(R.id.tvTotal);
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando vistas: " + e.getMessage());
        }
    }

    private boolean initDatabase() {
        try {
            dbHelper = new DBHelper(this);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando base de datos: " + e.getMessage());
            return false;
        }
    }

    private void setupSpinner() {
        try {
            if (spEstado != null) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                        R.array.estados_array, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spEstado.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error configurando spinner: " + e.getMessage());
            showToast("Error cargando opciones");
        }
    }

    private void setupListeners() {
        if (btnGenerar != null) {
            btnGenerar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    generarReporte();
                }
            });
        }

        if (btnVolver != null) {
            btnVolver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guardarReporte();
                }
            });
        }
    }

    private void generarReporte() {
        try {
            // Validar que los elementos existan
            if (spEstado == null || dbHelper == null) {
                Log.w(TAG, "Elementos necesarios son null");
                return;
            }

            String estado = "Todo";
            if (spEstado.getSelectedItem() != null) {
                estado = spEstado.getSelectedItem().toString();
            }

            String fecha = "";
            if (datePicker != null) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;
                int year = datePicker.getYear();
                fecha = String.format("%04d-%02d-%02d", year, month, day);
                Log.d(TAG, "Fecha seleccionada: " + fecha);
            }

            int pendientes = 0;
            int comprados = 0;
            int total = 0;

            Log.d(TAG, "Generando reporte para estado: " + estado);

            if (estado.equals("Todo")) {
                pendientes = dbHelper.countByStatus("Pendiente de compra", fecha);
                comprados = dbHelper.countByStatus("Comprado", fecha);
                total = pendientes + comprados; // Más lógico que usar countTotal
                Log.d(TAG, "Todo - Pendientes: " + pendientes + ", Comprados: " + comprados);
            } else if (estado.equals("Pendiente de compra")) {
                pendientes = dbHelper.countByStatus("Pendiente de compra", fecha);
                comprados = 0;
                total = pendientes;
                Log.d(TAG, "Pendientes - " + pendientes);
            } else if (estado.equals("Comprado")) {
                comprados = dbHelper.countByStatus("Comprado", fecha);
                pendientes = 0;
                total = comprados;
                Log.d(TAG, "Comprados - " + comprados);
            } else {
                // Caso por defecto
                pendientes = dbHelper.countByStatus("Pendiente de compra", fecha);
                comprados = dbHelper.countByStatus("Comprado", fecha);
                total = pendientes + comprados;
            }

            updateUI(pendientes, comprados, total);

        } catch (Exception e) {
            Log.e(TAG, "Error generando reporte: " + e.getMessage(), e);
            showToast("Error generando reporte");
        }
    }

    private void updateUI(int pendientes, int comprados, int total) {
        try {
            if (tvPendientes != null) {
                tvPendientes.setText("Pendientes: " + pendientes);
            }
            if (tvComprados != null) {
                tvComprados.setText("Comprados: " + comprados);
            }
            if (tvTotal != null) {
                tvTotal.setText("Total: " + total);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error actualizando UI: " + e.getMessage());
        }
    }

    // ------------------- NUEVA FUNCIONALIDAD: GUARDAR REPORTE -------------------
    private void guardarReporte() {
        // Verificar permisos de almacenamiento
        if (checkPermission()) {
            saveReportToFile();
        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveReportToFile();
                showToast("Permiso concedido. Guardando reporte...");
            } else {
                showToast("Permiso denegado. No se puede guardar el reporte.");
            }
        }
    }

    private void saveReportToFile() {
        try {
            // Obtener datos actuales del reporte
            String estado = "Todo";
            if (spEstado != null && spEstado.getSelectedItem() != null) {
                estado = spEstado.getSelectedItem().toString();
            }

            String fecha = "";
            if (datePicker != null) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;
                int year = datePicker.getYear();
                fecha = String.format("%04d-%02d-%02d", year, month, day);
            }

            // Obtener valores actuales de la UI
            String pendientesText = tvPendientes != null ? tvPendientes.getText().toString() : "Pendientes: 0";
            String compradosText = tvComprados != null ? tvComprados.getText().toString() : "Comprados: 0";
            String totalText = tvTotal != null ? tvTotal.getText().toString() : "Total: 0";

            // Crear contenido del reporte
            StringBuilder reportContent = new StringBuilder();
            reportContent.append("=== REPORTE DE LISTA DE COMPRAS ===\n");
            reportContent.append("Fecha de generación: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date())).append("\n");
            reportContent.append("Filtro de estado: ").append(estado).append("\n");
            reportContent.append("Filtro de fecha: ").append(fecha.isEmpty() ? "Todas las fechas" : fecha).append("\n");
            reportContent.append("===================================\n\n");
            reportContent.append(pendientesText).append("\n");
            reportContent.append(compradosText).append("\n");
            reportContent.append(totalText).append("\n");
            reportContent.append("===================================\n");

            // Guardar archivo
            String fileName = "reporte_compras_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".txt";
            boolean saved = saveToFile(reportContent.toString(), fileName);

            if (saved) {
                showToast("Reporte guardado: " + fileName);
            } else {
                showToast("Error al guardar el reporte");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error guardando reporte: " + e.getMessage(), e);
            showToast("Error al guardar el reporte");
        }
    }

    private boolean saveToFile(String content, String fileName) {
        try {
            // Crear directorio para la app
            File directory = new File(getExternalFilesDir(null), "Reportes");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Crear archivo
            File file = new File(directory, fileName);

            // Escribir contenido
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();

            Log.d(TAG, "Reporte guardado en: " + file.getAbsolutePath());
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error escribiendo archivo: " + e.getMessage());
            return false;
        }
    }

    private void showToast(String message) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error mostrando toast: " + e.getMessage());
        }
    }
}