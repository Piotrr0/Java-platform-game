package com.example.game;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Controller {

    @FXML
    Button hostMenuButton, goBackToMenuButton, joinMenuButton = new Button(), joinLobbyButton, startButton;
    @FXML
    TextField hostnameField, portField, lobbyInformation;

    @FXML
    Button movingButton;

    private Pane gamePane;
    private Map<Integer, Rectangle> playerRectangles = new HashMap<>();
    private int localPlayerId = 0;

    /*
        IT stores a server object, if you wonder why the fuck this is static it is because
        Controller changes each time we load new .fxml file so we want to have it static, is there better solution? Probably YES
    */
    private static Server server = null;

    /*
        Same idea as above — we store the main window statically so we don't have to fetch it all the time
    */
    static Stage mainStage;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public void handleHostMenuButton(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        Parent root = FXMLLoader.load(getClass().getResource("hostMenu.fxml"));
        mainStage.setScene(new Scene(root));

        int port = 50000; // or any free port number you like
        String hostname = InetAddress.getLocalHost().getHostAddress();
        server = new Server(hostname, port);
        Thread serverThread = new Thread(server);
        serverThread.start(); // starts the run() method in a new thread
    }

    public void handleJoinMenuButton(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("joinMenu.fxml"));
        mainStage.setScene(new Scene(root));
    }

    public void joinLobby(ActionEvent actionEvent) throws IOException {
        String hostname = hostnameField.getText();
        int port = Integer.parseInt(portField.getText());

        Main.currentClient = new Client(hostname, port, this);

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
            //WHEN WE START we also want
            Main.currentClient = new Client(server.getHostname(), server.getPort(), this);
            server.stopListeningForIncomingConnections(); // This should close serverSocket
            server.startGame(); // This changes server's state

        } else {
            System.out.println("server is null :/");
        }
    }

    public Controller loadMap() throws IOException
    {
        gamePane = new Pane();
        Scene scene = new Scene(gamePane, 500, 500);

        scene.setOnKeyPressed(this::handleKeyEvent);

        mainStage.setScene(scene);
        mainStage.show();

        gamePane.requestFocus();
        return this;
    }

    @FXML
    private void handleKeyEvent(KeyEvent event) {
        try {
            String msg = null;
            switch (event.getCode()) {
                case UP:
                    msg = "MOVE_UP";
                    break;
                case DOWN:
                    msg = "MOVE_DOWN";
                    break;
                case LEFT:
                    msg = "MOVE_LEFT";
                    break;
                case RIGHT:
                    msg = "MOVE_RIGHT";
                    break;
            }

            if (msg != null) {
                Main.currentClient.addPendingCommand(msg);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setLocalPlayerId(int id) {
        localPlayerId = id;
    }

    public int getLocalPlayerId() {
        return localPlayerId;
    }

    private Color GetPlayerColor(int playerId)
    {
        if(playerId == localPlayerId)
        {
            return Color.BLUE;
        }
        return Color.RED;
    }

    public void updatePlayerPosition(int playerId, double x, double y)
    {
        Rectangle playerRect = playerRectangles.get(playerId);

        if (playerRect == null)
        {
            Color playerColor = GetPlayerColor(playerId);
            playerRect = new Rectangle(50, 50, playerColor);

            playerRectangles.put(playerId, playerRect);
            gamePane.getChildren().add(playerRect);
        }

        playerRect.setX(x);
        playerRect.setY(y);
    }
}