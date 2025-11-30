/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ejercicioparcial;

/**
 *
 * @author mabel
 */
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class VentasController implements Initializable {

    private static final String FILE_PATH = "productos.dat";
    private static final String TICKET_PATH = "ticket.txt";

    @FXML private TableView<Producto> tvProductos;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TextField tfCantidad;
    @FXML private ListView<CarritoItem> lvCarrito;
    @FXML private Label lblTotal;
    @FXML private Label lblEstado;
    @FXML private Button btnFinalizar;

    private List<Producto> listaProductos;
    private List<CarritoItem> listaCarrito;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        
        listaCarrito = new ArrayList<>();
        lvCarrito.getItems().setAll(listaCarrito);

        cargarProductos();

        tfCantidad.setTextFormatter(new TextFormatter<>(change -> 
            change.getText().matches("\\d*") ? change : null));
    }
    
    private void cargarProductos() {
        GenerarArchivoDat.generarArchivoInicial(FILE_PATH); 
        

        try {
            
            listaProductos = Archivo.leerProductos(FILE_PATH);
            tvProductos.getItems().setAll(listaProductos);
            mostrarMensaje("Productos cargados con éxito.", true);
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Carga", 
                "No se pudo cargar el archivo " + FILE_PATH + ".\nDetalle: " + e.getMessage());
            listaProductos = new ArrayList<>();
            tvProductos.getItems().setAll(listaProductos);
            mostrarMensaje("ERROR al cargar productos. " + e.getMessage(), false);
        }
    }

    @FXML
    private void AgregarAlCarrito() {
        Producto productoSeleccionado = tvProductos.getSelectionModel().getSelectedItem();
        int cantidad;

        if (productoSeleccionado == null) {
            mostrarMensaje("Debe seleccionar un producto de la tabla.", false);
            return;
        }

        try {
            cantidad = Integer.parseInt(tfCantidad.getText());
        } catch (NumberFormatException e) {
            mostrarMensaje("La cantidad debe ser un número entero válido.", false);
            return;
        }

        if (cantidad <= 0) {
            mostrarMensaje("La cantidad debe ser mayor a 0.", false);
            return;
        }

        int cantidadEnCarrito = listaCarrito.stream()
            .filter(item -> item.getProducto().equals(productoSeleccionado))
            .mapToInt(CarritoItem::getCantidad)
            .sum();
            
        if (cantidad > productoSeleccionado.getStock() - cantidadEnCarrito) {
            mostrarMensaje("Stock insuficiente. Solo quedan " + (productoSeleccionado.getStock() - cantidadEnCarrito) + " unidades de " + productoSeleccionado.getNombre() + ".", false);
            return;
        }

        Optional<CarritoItem> itemExistente = listaCarrito.stream()
            .filter(item -> item.getProducto().equals(productoSeleccionado))
            .findFirst();

        if (itemExistente.isPresent()) {
            itemExistente.get().incrementarCantidad(cantidad);
        } else {
            listaCarrito.add(new CarritoItem(productoSeleccionado, cantidad));
        }

        lvCarrito.getItems().setAll(listaCarrito); 
        lvCarrito.refresh(); 
        calcularTotal();
        mostrarMensaje(cantidad + " unidades de " + productoSeleccionado.getNombre() + " agregadas al carrito.", true);
    }

    @FXML
    private void FinalizarCompra() {

        if (listaCarrito.isEmpty()) {
            mostrarMensaje("El carrito de compras está vacío. Agregue productos para finalizar.", false);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmar Compra");
        confirmation.setHeaderText("Confirmar la compra por un total de " + lblTotal.getText());
        confirmation.setContentText("¿Está seguro de que desea finalizar la compra?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            
            String ticketContent = null;
            try {
                Double total = totalCompra();
                ticketContent = Archivo.generarTicket(listaCarrito, total, TICKET_PATH);

                for (CarritoItem item : listaCarrito) {
                    Producto p = item.getProducto();
                    listaProductos.stream()
                        .filter(prod -> prod.getNombre().equals(p.getNombre()))
                        .findFirst()
                        .ifPresent(prod -> prod.setStock(prod.getStock() - item.getCantidad()));
                }

                Archivo.guardarProductos(new ArrayList<>(listaProductos), FILE_PATH);

                if (ticketContent != null) {
                    mostrarTicket(ticketContent);
                }
                
                listaCarrito.clear();
                tvProductos.getItems().setAll(listaProductos); 
                tvProductos.refresh(); 
                lvCarrito.getItems().clear(); 
                calcularTotal(); 
                mostrarMensaje("¡Compra finalizada y stock actualizado! Ticket generado y guardado.", true);                

            } catch (IOException e) {
                 mostrarAlerta(Alert.AlertType.ERROR, "Error de Persistencia", 
                    "Error al guardar el stock actualizado o generar el ticket: " + e.getMessage());
            }
        }
    }

    private void mostrarTicket(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ticket de Compra");
        alert.setHeaderText("Ticket generado y guardado en " + TICKET_PATH);

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);
        
        alert.showAndWait();
    }

    private void calcularTotal() {
        double total = totalCompra();
        lblTotal.setText(String.format("TOTAL: $%.2f", total));
    }

    private Double totalCompra() {
        return listaCarrito.stream()
            .mapToDouble(CarritoItem::getSubtotal)
            .sum();
    }

    private void mostrarMensaje(String mensaje, boolean exito) {
        lblEstado.setText("Estado: " + mensaje);
        String color = exito ? "green" : "red";
        lblEstado.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    private static class Archivo {
        
        public static List<Producto> leerProductos(String filePath) {
            ObjectInputStream ois = null;
            try{
                ois = new ObjectInputStream(new FileInputStream(filePath));
                @SuppressWarnings("unchecked")
                List<Producto> productos = (List<Producto>) ois.readObject();
                return productos;
            } catch (FileNotFoundException e) {
                return new ArrayList<>();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally{
                if (ois!= null){
                    try{
                        ois.close();
                    } catch (IOException ex){
                        System.err.println("Error al cerrar ObjectInputStream: " + ex.getMessage());
                    }
                }
            }
        }

        public static void guardarProductos(List<Producto> productos, String filePath) throws IOException {
            ObjectOutputStream oos = null;
            try{
                oos = new ObjectOutputStream(new FileOutputStream(filePath));
                oos.writeObject(productos);
            } finally {
                if (oos != null) {
                    try{
                        oos.close();
                    } catch (IOException ex){
                        System.err.println("Error al cerrar ObjectInputStream: " + ex.getMessage());
                    }
                }
            }
        }
        
        public static String generarTicket(List<CarritoItem> carritoItems, Double total, String filePath) throws IOException {
            Path ticketPath = Path.of(filePath);
            StringBuilder contentBuilder = new StringBuilder();

            contentBuilder.append("========================================\n");
            contentBuilder.append("       TICKET DE COMPRA    \n");
            contentBuilder.append("========================================\n");
            
            for (CarritoItem item : carritoItems) {
                Producto p = item.getProducto();
                String linea = String.format("%-25s x %3d  $%.2f = $%.2f", 
                    p.getNombre(), 
                    item.getCantidad(), 
                    p.getPrecio(), 
                    item.getSubtotal());
                contentBuilder.append(linea).append("\n");
            }
            
            contentBuilder.append("----------------------------------------\n");
            contentBuilder.append(String.format("TOTAL PAGADO: $%.2f\n ", total));
            contentBuilder.append("----------------------------------------\n");
            
            Files.writeString(ticketPath, contentBuilder.toString());
            
            return contentBuilder.toString();
        }
    }

    private static class GenerarArchivoDat {
        public static void generarArchivoInicial(String filePath) {
            File file = new File(filePath);
            if (!file.exists()) {
                List<Producto> productosIniciales = new ArrayList<>();
                productosIniciales.add(new Producto("Mouse Logitech", 20.50, 20));
                productosIniciales.add(new Producto("Teclado Redragon", 55.75, 14));
                productosIniciales.add(new Producto("Monitor Samsung 24", 250.33, 8));
                productosIniciales.add(new Producto("Auriculares Sony", 89.90, 19));
                productosIniciales.add(new Producto("Notebook Lenovo", 950.80, 5));
                productosIniciales.add(new Producto("GPU RTX 4060", 490.00, 3));
                productosIniciales.add(new Producto("Impresora HP", 155.80, 18));
                productosIniciales.add(new Producto("Disco SSD 1TB", 75.30, 25));
                productosIniciales.add(new Producto("Memoria RAM 16GB", 60.60, 22));
                productosIniciales.add(new Producto("Parlantes Genius", 45.45, 30));
                
                try {
                    Archivo.guardarProductos(productosIniciales, filePath);
                } catch (IOException e) {
                    System.err.println("Error al crear el archivo inicial de productos: " + e.getMessage());
                }
            }
        }
    }
}