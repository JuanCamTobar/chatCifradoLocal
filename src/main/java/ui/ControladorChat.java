package ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import util.Conexion;

import java.net.URL;
import java.util.ResourceBundle;

public class ControladorChat implements Initializable {

    @FXML private Label statusLabel;
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    private Chat chat = Chat.getInstance();
    private String nombreUsuario = chat.getNombreUser();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Iniciar recepción de mensajes
        chat.recibirMensaje(mensaje -> {
            String nombreRemoto = chat.getNombreRemoto();
            Platform.runLater(() ->
                    chatArea.appendText(nombreRemoto != null ? nombreRemoto + ": " + mensaje + "\n" : "Otro: " + mensaje + "\n")
            );
        });
    }

    @FXML
    private void onSendButtonClick() {
        String mensaje = messageField.getText().trim();
        if (!mensaje.isEmpty()) {
            try {
                chat.enviarMensaje(mensaje);
                chatArea.appendText(nombreUsuario + ": " + mensaje + "\n");
                messageField.clear();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    chatArea.appendText("Error al enviar mensaje: " + e.getMessage() + "\n");
                });
            }
        }
    }
}