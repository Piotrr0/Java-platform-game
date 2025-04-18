package com.example.game;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {


    
    public static void main(String[] args) {
        launch(args);

    }



    @Override
    public void start(Stage primaryStage) throws IOException {


        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        
        primaryStage.setTitle("Platform game");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


}
