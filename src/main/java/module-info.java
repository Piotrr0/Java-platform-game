module com.example.game {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.xml.dom;


    opens com.example.game to javafx.fxml;
    exports com.example.game;
}