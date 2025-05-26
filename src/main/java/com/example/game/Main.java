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
 * 1. Server should request if any changes happened on client side and if so, what were they? e.g server will ask if particular player wanted to move, if so it will proceed to ask where does it want to move?
 *
 *2. Client will reply to the request and if this information is worth sending to server it will be sent from client to server
 *
 *3. After server processes all inputs from all clients it will broadcast message to all clients with current state of the game
 * */

public class Main extends Application {

    public static Client currentClient; // static reference to client

    @Override
    public void start(Stage primaryStage) throws IOException {
        //System.out.println("Hi I am a client!");
        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        Controller.setMainStage(primaryStage);
        primaryStage.setTitle("Platform game");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
