package com.example.lista_compras;

public class Product {
    private int id;
    private String name;
    private String status;
    private String date;

    public Product(int id, String name, String status, String date) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.date = date;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}

