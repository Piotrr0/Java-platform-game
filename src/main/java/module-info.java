module com.example.game {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.xml.dom;
    requires java.desktop;


    opens com.example.game to javafx.fxml;
    exports com.example.game;
}