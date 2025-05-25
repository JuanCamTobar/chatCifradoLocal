package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ControladorAplicacion extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ControladorAplicacion.class.getResource("controlador-cliente.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Cliente");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void hideWindow(Stage stage){
        stage.hide();
    }

    public static void showWindow(String fxml, Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(ControladorAplicacion.class.getResource("/ui/" + fxml + ".fxml"));
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load(), 600, 400); // Tamaño ajustado
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (stage == null) stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Chat Seguro");
        stage.show();
    }
}