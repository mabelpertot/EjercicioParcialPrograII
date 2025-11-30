/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

/*
Una tienda necesita un sistema de ventas para administrar sus productos, permitir
realizar compras y generar tickets.
El sistema deberá trabajar con una lista de productos provista por la tienda en un
archivo .dat, junto con la clase Producto ubicada en el paquete modelo.
Los alumnos deben usar la clase y el archivo provistos sin modificarlos, para poder
deserializar correctamente los datos.
Al iniciar el sistema la tienda quiere, deserializar el archivo de productos (provisto) y
cargarlo en una lista de productos, la cual se debe mostrar por completo dentro de un
TableView. Si ocurre un problema al leer el archivo, debe mostrarse un mensaje de
error.
El usuario podrá seleccionar un producto del TableView, indicar la cantidad y agregarlo
a un carrito de compras (el cual es un ListView) donde se visualiza el nombre del
producto y la cantidad.
Antes de agregar el producto al carrito el sistema debe validar que tenga un producto
seleccionado, que la cantidad sea mayor a 0 y que tenga stock suficiente. De lo contrario
informarlo en la pantalla.
Una vez finalizada la compra se debe validar que tenga productos en el carrito, calcular
el total de la compra y generar el ticket de compra, que se guardara en un archivo txt
denominado ticket, con el detalle de la compra y el valor total:
Una vez confirmada la compra debe actualizarse el stock real de cada producto afectado
sobrescribirse el archivo .dat original con la nueva lista serializada y limpiar el carrito.
 */

package ejercicioparcial;


/*
 *
 * @author mabel
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EjercicioParcial extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Ventas.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            
            primaryStage.setTitle("Sistema de Ventas");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}