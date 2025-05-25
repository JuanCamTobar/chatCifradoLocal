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

public class ControladorCliente implements Initializable {

    @FXML
    private TextField nombre;


    @FXML
    private ChoiceBox<String> peer;

    @FXML
    protected void onConectarseButtonClick() {
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
        chat.setNombreUser(nombre.getText()); // Set local username

        try {
            KeyPair keyPair = conexion.diffieHellman();
            PublicKey llavePublica = keyPair.getPublic();
            PrivateKey llavePrivada = keyPair.getPrivate();

            if (peer.getValue().equals("Peer A")) {
                // Servidor: espera la clave del cliente primero, luego envía la suya
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

                        // Send and receive username
                        chat.enviarNombreUsuario(nombre.getText());
                        chat.recibirNombreUsuario(nombreRemoto -> chat.setNombreRemoto(nombreRemoto));

                        Platform.runLater(() -> {
                            ControladorAplicacion.hideWindow(stage);
                            ControladorAplicacion.showWindow("chat", stage);
                        });
                    } catch (Exception e) {
                        showError("Error en el intercambio de claves o nombres: " + e.getMessage());
                    }
                });
            } else {
                // Cliente: envía su clave primero, luego recibe la del servidor
                conexion.setClientSocket("127.0.0.1");
                conexion.enviarClavePublica(llavePublica);
                System.out.println("Clave pública enviada por el cliente: " + llavePublica);
                conexion.recibirClavePublica((publicKeyRecibida) -> {
                    System.out.println("Clave pública recibida: " + publicKeyRecibida);
                    try {
                        byte[] claveCompartida = conexion.generarClaveCompartida(llavePrivada, publicKeyRecibida);
                        System.out.println("Clave compartida generada (cliente): " + java.util.Arrays.toString(claveCompartida));
                        conexion.setClaveCompartida(claveCompartida);

                        // Send and receive username
                        chat.enviarNombreUsuario(nombre.getText());
                        chat.recibirNombreUsuario(nombreRemoto -> chat.setNombreRemoto(nombreRemoto));

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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nombre.setText("Coloca tu nombre :b");
        ObservableList<String> opciones = FXCollections.observableArrayList("Peer A", "Peer B");
        peer.setItems(opciones);
    }
}