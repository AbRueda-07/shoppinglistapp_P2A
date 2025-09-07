package com.example.lista_compras;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private ArrayList<Product> products;
    private OnStatusChangeListener statusListener;
    private OnEditListener editListener;
    private OnDeleteListener deleteListener;

    public interface OnStatusChangeListener {
        void onStatusChange(Product product);
    }

    public interface OnEditListener {
        void onEdit(Product product);
    }

    public interface OnDeleteListener {
        void onDelete(Product product);
    }

    public ProductAdapter(ArrayList<Product> products,
                          OnStatusChangeListener statusListener,
                          OnEditListener editListener,
                          OnDeleteListener deleteListener) {
        this.products = products;
        this.statusListener = statusListener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // USAR EL NOMBRE CORRECTO: item_product
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = products.get(position);

        // Limpiar listeners anteriores para evitar acumulación
        holder.btnComprar.setOnClickListener(null);
        holder.btnEditar.setOnClickListener(null);
        holder.btnEliminar.setOnClickListener(null);

        // Asignar datos
        holder.name.setText(p.getName());
        holder.status.setText("Estado: " + p.getStatus());
        holder.date.setText("Fecha: " + p.getDate());

        // Configurar visibilidad y listeners
        if ("Comprado".equals(p.getStatus())) {
            holder.btnComprar.setVisibility(View.GONE);
        } else {
            holder.btnComprar.setVisibility(View.VISIBLE);
            holder.btnComprar.setOnClickListener(v -> {
                if (statusListener != null) statusListener.onStatusChange(p);
            });
        }

        holder.btnEditar.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(p);
        });

        holder.btnEliminar.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(p);
        });
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, status, date;
        Button btnComprar, btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // USAR LOS IDs CORRECTOS
            name = itemView.findViewById(R.id.tvName);
            status = itemView.findViewById(R.id.tvStatus);
            date = itemView.findViewById(R.id.tvDate);
            btnComprar = itemView.findViewById(R.id.btnComprar);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}