package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal que extiende de Application para iniciar la interfaz gráfica de la aplicación cliente.
 */
public class ControladorAplicacion extends Application {

    /**
     * Método que se ejecuta al iniciar la aplicación.
     * Carga el archivo FXML principal y muestra la ventana.
     *
     * @param stage el escenario principal de JavaFX.
     * @throws IOException si ocurre un error al cargar el archivo FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Carga el archivo FXML de la interfaz principal
        FXMLLoader fxmlLoader = new FXMLLoader(ControladorAplicacion.class.getResource("controlador-cliente.fxml"));
        // Crea la escena con dimensiones específicas
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        // Configura y muestra la ventana principal
        stage.setTitle("Cliente");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Método principal que lanza la aplicación JavaFX.
     *
     * @param args argumentos de línea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Oculta la ventana especificada.
     *
     * @param stage la ventana (Stage) que se desea ocultar.
     */
    public static void hideWindow(Stage stage){
        stage.hide();
    }

    /**
     * Muestra una nueva ventana cargando una interfaz desde un archivo FXML.
     *
     * @param fxml  nombre del archivo FXML (sin extensión) a cargar desde la carpeta /ui/.
     * @param stage escenario donde se mostrará la interfaz; si es null, se crea uno nuevo.
     */
    public static void showWindow(String fxml, Stage stage) {
        // Carga el archivo FXML especificado
        FXMLLoader fxmlLoader = new FXMLLoader(ControladorAplicacion.class.getResource("/ui/" + fxml + ".fxml"));
        Scene scene;
        try {
            // Crea la escena con tamaño fijo
            scene = new Scene(fxmlLoader.load(), 600, 400); // Tamaño ajustado
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Si no se proporcionó un Stage, se crea uno nuevo
        if (stage == null) stage = new Stage();
        // Configura y muestra la nueva ventana
        stage.setScene(scene);
        stage.setTitle("Chat Seguro");
        stage.show();
    }
}
