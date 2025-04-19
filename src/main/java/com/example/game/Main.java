package com.example.game;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * That's the way I'm planning to do network architecture, I will see in practice if its even possible
 *
 * 1.Have clients send their inputs to server at fixed intervals (without being asked)
 *
 *2.Server validates and broadcasts to other clients
 *
 *3.This is how most FPS games handle input
 * */

public class Main extends Application {


    
    public static void main(String[] args) {
        launch(args);

    }



    @Override
    public void start(Stage primaryStage) throws IOException {


        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        Controller.setMainStage(primaryStage);

        primaryStage.setTitle("Platform game");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


}
