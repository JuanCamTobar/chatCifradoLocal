package ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import util.Conexion;
import javafx.fxml.Initializable;

import java.net.URL;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.util.ResourceBundle;

/**
 * Controlador para la vista principal del cliente.
 * Permite al usuario ingresar su nombre y seleccionar si actuará como Peer A o Peer B.
 * Establece la conexión segura utilizando Diffie-Hellman y transfiere los nombres de usuario.
 */
public class ControladorCliente implements Initializable {

    @FXML
    private TextField nombre; // Campo para ingresar el nombre del usuario

    @FXML
    private ChoiceBox<String> peer; // ChoiceBox para seleccionar el tipo de peer (A o B)

    /**
     * Se ejecuta al hacer clic en el botón de conectarse.
     * Realiza validaciones, establece la conexión segura, intercambia claves y muestra la ventana de chat.
     */
    @FXML
    protected void onConectarseButtonClick() {
        // Validaciones del formulario
        if (nombre.getText().isEmpty() || nombre.getText().equals("Coloca tu nombre :b")) {
            showAlert(Alert.AlertType.WARNING, "Nombre requerido", "Debes ingresar un nombre de usuario válido.");
            return;
        }
        if (peer.getValue() == null || peer.getValue().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Selección requerida", "Debes seleccionar un peer.");
            return;
        }

        Stage stage = (Stage) nombre.getScene().getWindow();
        Conexion conexion = Conexion.getInstance();
        Chat chat = Chat.getInstance();
        chat.setNombreUser(nombre.getText()); // Asignar el nombre local del usuario

        try {
            // Generar par de llaves públicas/privadas con Diffie-Hellman
            KeyPair keyPair = conexion.diffieHellman();
            PublicKey llavePublica = keyPair.getPublic();
            PrivateKey llavePrivada = keyPair.getPrivate();

            if (peer.getValue().equals("Peer A")) {
                // Peer A actúa como servidor: recibe primero la clave y luego responde
                conexion.setServerSocket();
                System.out.println("Servidor iniciado, esperando clave pública...");
                conexion.recibirClavePublica((publicKeyRecibida) -> {
                    System.out.println("Clave pública recibida: " + publicKeyRecibida);
                    try {
                        conexion.enviarClavePublica(llavePublica);
                        System.out.println("Clave pública enviada por el servidor: " + llavePublica);
                        byte[] claveCompartida = conexion.generarClaveCompartida(llavePrivada, publicKeyRecibida);
                        System.out.println("Clave compartida generada (servidor): " + java.util.Arrays.toString(claveCompartida));
                        conexion.setClaveCompartida(claveCompartida);

                        // Intercambio de nombres de usuario
                        chat.enviarNombreUsuario(nombre.getText());
                        chat.recibirNombreUsuario(nombreRemoto -> chat.setNombreRemoto(nombreRemoto));

                        // Cambiar a la ventana de chat
                        Platform.runLater(() -> {
                            ControladorAplicacion.hideWindow(stage);
                            ControladorAplicacion.showWindow("chat", stage);
                        });
                    } catch (Exception e) {
                        showError("Error en el intercambio de claves o nombres: " + e.getMessage());
                    }
                });
            } else {
                // Peer B actúa como cliente: envía la clave primero y luego espera respuesta
                conexion.setClientSocket("127.0.0.1");
                conexion.enviarClavePublica(llavePublica);
                System.out.println("Clave pública enviada por el cliente: " + llavePublica);
                conexion.recibirClavePublica((publicKeyRecibida) -> {
                    System.out.println("Clave pública recibida: " + publicKeyRecibida);
                    try {
                        byte[] claveCompartida = conexion.generarClaveCompartida(llavePrivada, publicKeyRecibida);
                        System.out.println("Clave compartida generada (cliente): " + java.util.Arrays.toString(claveCompartida));
                        conexion.setClaveCompartida(claveCompartida);

                        // Intercambio de nombres de usuario
                        chat.enviarNombreUsuario(nombre.getText());
                        chat.recibirNombreUsuario(nombreRemoto -> chat.setNombreRemoto(nombreRemoto));

                        // Cambiar a la ventana de chat
                        Platform.runLater(() -> {
                            ControladorAplicacion.hideWindow(stage);
                            ControladorAplicacion.showWindow("chat", stage);
                        });
                    } catch (Exception e) {
                        showError("Error al generar clave compartida o nombres: " + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            showError("Error al establecer conexión: " + e.getMessage());
        }
    }

    /**
     * Muestra una alerta genérica.
     *
     * @param type Tipo de alerta (WARNING, ERROR, etc.)
     * @param title Título de la alerta.
     * @param message Mensaje que se desea mostrar.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta de error con el mensaje recibido.
     *
     * @param message Mensaje de error.
     */
    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }

    /**
     * Método de inicialización de la interfaz.
     * Establece el texto inicial del campo de nombre y llena las opciones del ChoiceBox.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nombre.setText("Coloca tu nombre :b");
        ObservableList<String> opciones = FXCollections.observableArrayList("Peer A", "Peer B");
        peer.setItems(opciones);
    }
}
