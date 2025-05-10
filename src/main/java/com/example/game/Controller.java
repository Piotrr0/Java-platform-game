package com.example.game;

import com.example.game.actors.ActorManager;
import com.example.game.actors.Player;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;

public class Controller {

    @FXML
    Button hostMenuButton, goBackToMenuButton, joinMenuButton = new Button(), joinLobbyButton, startButton;
    @FXML
    TextField hostnameField, portField, lobbyInformation;

    @FXML
    Button movingButton;

    private Pane gamePane;
    private int localPlayerId = -1;

    private ActorManager actorManager;
    private String currentLevelName;

    /*
        IT stores a server object, if you wonder why the fuck this is static it is because
        Controller changes each time we load new .fxml file so we want to have it static, is there better solution? Probably YES
    */
    private static Server server = null;

    /*
        Same idea as above — we store the main window statically so we don't have to fetch it all the time
    */
    static Stage mainStage;

    private AnimationTimer gameLoop;

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

    private void startGameLoop() {
        if (gameLoop == null) {
            gameLoop = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (actorManager != null) {
                        actorManager.updateClient();
                    }
                }
            };
            gameLoop.start();
        }
    }

    private void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }

    public void setupGameScene(String sceneName) {
        currentLevelName = sceneName;

        gamePane = new Pane();
        Scene scene = new Scene(gamePane, 500, 500);

        scene.setOnKeyPressed(this::handleKeyEvent);

        actorManager = new ActorManager(gamePane);

        mainStage.setScene(scene);
        mainStage.show();

        gamePane.requestFocus();

        startGameLoop();

        System.out.println("Scene setup complete for: " + sceneName);
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
                default:
                    return;
            }

            if (msg != null && Main.currentClient != null) {
                Main.currentClient.addPendingCommand(msg);
            }
        } catch (Exception e) {
            System.err.println("Error handling key event: " + e.getMessage());
        }
    }

    public void setLocalPlayerId(int id) {
        localPlayerId = id;
    }

    public void updateActorState(int actorId, String type, double x, double y, double width, double height)
    {
        if (actorManager == null) {
            return;
        }
        actorManager.createOrUpdateActor(actorId, type, x, y, width, height, (actorId == localPlayerId), gamePane);
    }

    public void addActorState(int actorId, String type, double x, double y, double width, double height) {
        if (gamePane == null || actorManager == null) {
            return;
        }

        actorManager.createOrUpdateActor(actorId, type, x, y, width, height,(actorId == localPlayerId), gamePane);
    }

    public void removeActor(int actorId) {
        if (actorManager == null || gamePane == null) {
            return;
        }
        actorManager.removeActor(actorId, gamePane);
    }

    public Pane getGamePane() {
        return gamePane;
    }
}