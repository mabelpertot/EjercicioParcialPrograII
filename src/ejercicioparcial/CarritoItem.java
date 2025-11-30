/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ejercicioparcial;

/**
 *
 * @author mabel
 */
public class CarritoItem {
    private final Producto producto;
    private int cantidad;
    private double subtotal; 


    public CarritoItem(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        actualizarSubtotal();
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    
    public void incrementarCantidad(int cantidad) {
        this.cantidad += cantidad;
        actualizarSubtotal();
    }
    
    private void actualizarSubtotal() {
        this.subtotal = this.producto.getPrecio() * this.cantidad;
    }


    public Double getSubtotal() {
        return producto.getPrecio() * cantidad;
    }

    @Override
    public String toString() {
        return producto.getNombre() + " (x" + cantidad + ") - Subtotal: $" + this.subtotal;
    }
}