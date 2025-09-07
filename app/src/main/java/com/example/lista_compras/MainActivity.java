package com.example.lista_compras;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnNueva = findViewById(R.id.btnNuevaCompra);
        Button btnLista = findViewById(R.id.btnListaCompras);
        Button btnReporte = findViewById(R.id.btnReporte);

        btnNueva.setOnClickListener(v -> startActivity(new Intent(this, ComprasActivity.class)));
        btnLista.setOnClickListener(v -> startActivity(new Intent(this, ListaComprasActivity.class)));
        btnReporte.setOnClickListener(v -> startActivity(new Intent(this, ReporteActivity.class)));
    }
}