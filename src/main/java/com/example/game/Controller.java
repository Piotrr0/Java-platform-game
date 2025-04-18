package com.example.game;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class Controller {

    @FXML
    Button hostMenuButton,goBackToMenuButton,joinMenuButton,joinLobbyButton;
    @FXML
    TextField hostnameField,portField;



    public void handleHostMenuButton(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        Parent root = FXMLLoader.load(getClass().getResource("hostMenu.fxml"));
        Stage window = (Stage)hostMenuButton.getScene().getWindow();
        window.setScene(new Scene(root));

        int port = 50000; // or any free port number you like
        Server server = new Server(port);
        Thread serverThread = new Thread(server);
        serverThread.start(); // starts the run() method in a new thread

    }

    public void handleJoinMenuButton(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        Parent root = FXMLLoader.load(getClass().getResource("joinMenu.fxml"));
        Stage window = (Stage)hostMenuButton.getScene().getWindow();
        window.setScene(new Scene(root));
    }

    public void joinLobby(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        String hostname = hostnameField.getText();
        int port = Integer.parseInt(portField.getText());
        System.out.println(hostname);
        System.out.println(port);
        //IT SHOULD CREATE STANDALONE OBJECT OF CLIENT but it's too much for today so I create it here

        Socket socket = new Socket(hostname,port);
    }

    public void handleGoBackToMenuButton(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        Stage window = (Stage)goBackToMenuButton.getScene().getWindow();
        window.setScene(new Scene(root));
    }

}
