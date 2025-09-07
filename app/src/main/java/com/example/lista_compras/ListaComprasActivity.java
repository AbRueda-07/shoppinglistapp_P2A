package com.example.lista_compras;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListaComprasActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private ProductAdapter adapter;
    private ArrayList<Product> products = new ArrayList<>();
    private Spinner spEstado;
    private DatePicker datePicker;
    private Button btnFiltrar;
    private Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_compras);

        dbHelper = new DBHelper(this);
        spEstado = findViewById(R.id.spEstadoFiltro);
        datePicker = findViewById(R.id.datePicker);
        btnFiltrar = findViewById(R.id.btnFiltrar);
        btnVolver = findViewById(R.id.btnVolverLista);

        ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(this,
                R.array.estados_array, android.R.layout.simple_spinner_item);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEstado.setAdapter(adapterEstado);

        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(products,
                product -> { // statusListener
                    int rows = dbHelper.updateProductStatus(product.getId(), "Comprado");
                    if (rows > 0) Toast.makeText(this, "Producto marcado como Comprado", Toast.LENGTH_SHORT).show();
                    cargarLista();
                },
                product -> { // editListener
                    Intent i = new Intent(this, ComprasActivity.class);
                    i.putExtra("edit_id", product.getId());
                    startActivity(i);
                },
                product -> { // deleteListener
                    new AlertDialog.Builder(this)
                            .setTitle("Eliminar")
                            .setMessage("¿Eliminar producto '" + product.getName() + "'?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Eliminar", (dialog, which) -> {
                                int rows = dbHelper.deleteProduct(product.getId());
                                if (rows > 0) Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                                cargarLista();
                            }).show();
                }
        );
        rv.setAdapter(adapter);

        btnFiltrar.setOnClickListener(v -> cargarLista());
        btnVolver.setOnClickListener(v -> finish());

        // cargar inicial
        cargarLista();
    }

    private void cargarLista() {
        try {
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

            // Limpiar lista y cargar nuevos datos
            products.clear();
            ArrayList<Product> nuevosProductos = dbHelper.getProducts(
                    estado.equals("Todo") ? "" : estado,
                    fecha
            );
            products.addAll(nuevosProductos);
            adapter.notifyDataSetChanged();

            Log.d("ListaComprasActivity", "Cargados " + products.size() + " productos");

        } catch (Exception e) {
            Log.e("ListaComprasActivity", "Error cargando lista", e);
            Toast.makeText(this, "Error cargando productos", Toast.LENGTH_SHORT).show();
        }
    }
}
