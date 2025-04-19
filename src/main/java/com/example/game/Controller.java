package com.example.game;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller {

    @FXML
    Button hostMenuButton, goBackToMenuButton, joinMenuButton= new Button(), joinLobbyButton, startButton;
    @FXML
    TextField hostnameField, portField,lobbyInformation;



    /*
        IT stores a server object, if you wonder why the fuck this is static it is because
        Controller changes each time we load new .fxml file so we want to have it static, is there better solution? Probably YES
    */
    private static Server server = null;

    /*
        Same idea as above — we store the main window statically so we don’t have to fetch it all the time
    */
    static Stage mainStage;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public void handleHostMenuButton(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        Parent root = FXMLLoader.load(getClass().getResource("hostMenu.fxml"));
        mainStage.setScene(new Scene(root));

        int port = 50000; // or any free port number you like
        server = new Server(port);
        Thread serverThread = new Thread(server);
        serverThread.start(); // starts the run() method in a new thread
    }

    public void handleJoinMenuButton(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        Parent root = FXMLLoader.load(getClass().getResource("joinMenu.fxml"));
        mainStage.setScene(new Scene(root));


    }

    public void joinLobby(ActionEvent actionEvent) throws IOException, ClassNotFoundException {




        String hostname = hostnameField.getText();
        int port = Integer.parseInt(portField.getText());

        // This should probably become a full Client class later
        Client client = new Client(hostname,port);
        Thread clientThread = new Thread(client);
        clientThread.start(); // starts the run() method in a new thread


        joinLobbyButton.setVisible(false);
        lobbyInformation.setVisible(true);

    }

    public void handleGoBackToMenuButton(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        mainStage.setScene(new Scene(root));
    }

    public void handleStartButton(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        System.out.println("Host wants to start a game");
        if (server != null) {
            server.stopListeningForIncomingConnections(); // This should close serverSocket
            server.startGame(); // This changes server's state




        } else {
            System.out.println("server is null :/");
        }
    }

    public static void loadMap() throws IOException {
        Parent root = FXMLLoader.load(Controller.class.getResource("client.fxml"));

        mainStage.setScene(new Scene(root));
    }



}
