module ui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;


    exports ui;
    opens ui to javafx.fxml;
    exports util;
    opens util to javafx.fxml;
}