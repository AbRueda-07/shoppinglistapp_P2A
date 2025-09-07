package com.example.lista_compras;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ComprasActivity extends AppCompatActivity {
    private EditText etNombre;
    private Spinner spEstado;
    private Button btnAgregar;
    private Button btnVolver;
    private DBHelper db;
    private int editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compras);

        etNombre = findViewById(R.id.etNombre);
        spEstado = findViewById(R.id.spEstado); // Añadido - faltaba esta línea
        btnAgregar = findViewById(R.id.btnAgregar);
        btnVolver = findViewById(R.id.btnVolver);

        db = new DBHelper(this);

        // Configurar spinner con los estados
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.estados_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spEstado != null) {
            spEstado.setAdapter(adapter);
        }

        // Verificar si es edición
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("edit_id")) {
            editId = intent.getIntExtra("edit_id", -1);
            if (editId != -1) {
                ArrayList<Product> all = db.getProducts("", "");
                Product found = null;
                for (Product prod : all) {
                    if (prod.getId() == editId) {
                        found = prod;
                        break;
                    }
                }
                if (found != null && spEstado != null) {
                    etNombre.setText(found.getName());
                    for (int idx = 0; idx < spEstado.getCount(); idx++) {
                        if (spEstado.getItemAtPosition(idx).toString().equals(found.getStatus())) {
                            spEstado.setSelection(idx);
                            break;
                        }
                    }
                    if (btnAgregar != null) {
                        btnAgregar.setText("Actualizar");
                    }
                }
            }
        }

        // Listener para el botón AGREGAR
        if (btnAgregar != null) {
            btnAgregar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    agregarProducto();
                }
            });
        }

        // Listener para el botón VOLVER
        if (btnVolver != null) {
            btnVolver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private void agregarProducto() {
        try {
            // Validar campos
            if (etNombre == null) {
                Toast.makeText(this, "Error en el formulario", Toast.LENGTH_SHORT).show();
                return;
            }

            String nombre = etNombre.getText().toString().trim();
            String estado = "Pendiente de compra"; // Valor por defecto

            // Obtener estado del spinner si existe
            if (spEstado != null && spEstado.getSelectedItem() != null) {
                estado = spEstado.getSelectedItem().toString();
            }

            // Validar nombre
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Ingrese nombre del producto", Toast.LENGTH_SHORT).show();
                return;
            }

            if (editId == -1) {
                // Agregar nuevo producto
                String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                Product p = new Product(0, nombre, estado, fecha);
                long result = db.addProduct(p);

                if (result != -1) {
                    Toast.makeText(this, "Producto agregado correctamente", Toast.LENGTH_SHORT).show();
                    etNombre.setText(""); // Limpiar campo para agregar más productos
                    // NO LLAMAR finish() - mantener la actividad abierta
                } else {
                    Toast.makeText(this, "Error al agregar producto", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Actualizar producto existente
                int rows = db.updateProduct(editId, nombre, estado);
                if (rows > 0) {
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                    finish(); // Solo cerrar si es actualización
                } else {
                    Toast.makeText(this, "Error al actualizar producto", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}