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

/**
 * Controlador de la interfaz del chat.
 * Gestiona el envío y recepción de mensajes en la ventana del chat.
 */
public class ControladorChat implements Initializable {

    @FXML private Label statusLabel; // Etiqueta de estado (no usada en este código, pero declarada en la UI)
    @FXML private TextArea chatArea; // Área de texto donde se muestra la conversación
    @FXML private TextField messageField; // Campo de texto para escribir el mensaje a enviar

    private Chat chat = Chat.getInstance(); // Instancia única de la clase Chat
    private String nombreUsuario = chat.getNombreUser(); // Nombre del usuario local

    /**
     * Método que se ejecuta al inicializar la vista.
     * Inicia un hilo para recibir mensajes de manera asíncrona y los muestra en el área de chat.
     *
     * @param url no utilizado.
     * @param resourceBundle no utilizado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicia la recepción de mensajes entrantes
        chat.recibirMensaje(mensaje -> {
            String nombreRemoto = chat.getNombreRemoto();
            Platform.runLater(() ->
                    chatArea.appendText(
                            nombreRemoto != null ? nombreRemoto + ": " + mensaje + "\n" : "Otro: " + mensaje + "\n"
                    )
            );
        });
    }

    /**
     * Método llamado cuando se hace clic en el botón de enviar.
     * Envía el mensaje escrito al otro usuario y lo muestra en el área de chat.
     */
    @FXML
    private void onSendButtonClick() {
        String mensaje = messageField.getText().trim();
        if (!mensaje.isEmpty()) {
            try {
                // Enviar el mensaje a través del canal seguro
                chat.enviarMensaje(mensaje);
                // Mostrar el mensaje en el área de chat como emisor local
                chatArea.appendText(nombreUsuario + ": " + mensaje + "\n");
                // Limpiar el campo de entrada
                messageField.clear();
            } catch (Exception e) {
                // En caso de error, mostrar el mensaje de error en el chat
                Platform.runLater(() -> {
                    chatArea.appendText("Error al enviar mensaje: " + e.getMessage() + "\n");
                });
            }
        }
    }
}
