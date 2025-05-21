package com.example.game;

import com.example.game.actors.Actor;
import com.example.game.actors.ActorManager;
import com.example.game.actors.Player;
import com.example.game.actors.Prop;
import com.example.game.network.ReplicationUtil;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

public class Controller {

    @FXML
    Button hostMenuButton, goBackToMenuButton, joinMenuButton = new Button(), joinLobbyButton, startButton;
    @FXML
    TextField hostnameField, portField, lobbyInformation;

    @FXML
    Button movingButton;

    @FXML
    Label hostnameLabel, portLabel;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hostMenu.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController(); // pobieramy instancję kontrolera vy działało
        mainStage.setScene(new Scene(root));

        int port = 50000;
        String hostname = InetAddress.getLocalHost().getHostAddress();
        server = new Server(hostname, port);
        Thread serverThread = new Thread(server);
        serverThread.start();

        controller.hostnameLabel.setText("hostname: " + hostname);
        controller.portLabel.setText("port: " + port);
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

    public void handleStartButton(ActionEvent actionEvent) throws IOException, ClassNotFoundException, InterruptedException {
        System.out.println("Host wants to start a game");
        if (server != null) {
            //server.finalizeGameSetupAndStart();
            //server.stopListeningForIncomingConnections();
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
        gamePane = new Pane();
        gamePane.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(gamePane, 700, 550);

        scene.setOnKeyPressed(this::handleKeyEvent);
        scene.setOnKeyReleased(this::handleKeyEventReleased);

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
                case SPACE:
                    msg = "SHOOT";
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

    @FXML
    private void handleKeyEventReleased(KeyEvent event) {
        try {
            String msg = switch (event.getCode()) {
                case LEFT -> "STOP_LEFT";
                case RIGHT -> "STOP_RIGHT";
                default -> null;
            };

            if (msg != null && Main.currentClient != null) {
                Main.currentClient.addPendingCommand(msg);
            }
        } catch (Exception e) {
            System.err.println("Error handling key event: " + e.getMessage());
        }
    }

    public void setLocalPlayerId(int id) {
        this.localPlayerId = id;
    }

    public void addActorFromServer(int actorId, String actorType, String serializedState) {
        if (actorManager != null && actorManager.getActor(actorId) != null) {
            updateActorStateFromServer(actorId, actorType, serializedState);
            return;
        }

        Actor actor;
        boolean isLocal = (actorId == localPlayerId);
        if ("Player".equals(actorType)) {
            actor = new Player(actorId, 0, 0, isLocal);
        }
        else if("Prop".equals(actorType)){
            actor = new Prop(actorId, 0, 0, 0, 0,Color.ORANGE,"Crate.png");
        }
        else{
            actor = new Actor(actorId, 0, 0, 0, 0,Color.BROWN);
        }


        Map<String, String> stateData = ReplicationUtil.deserializeStateMap(serializedState);
        ReplicationUtil.applyReplicatedState(actor, stateData);

        if (actor != null && gamePane != null) {
            actorManager.addActorClientSide(actor, gamePane);
        }
    }

    public void updateActorStateFromServer(int actorId, String actorType, String serializedState) {

        if(actorManager != null)
        {
            Actor actor = actorManager.getActor(actorId);
            if (actor == null) {
                // This might happen if ADD_ACTOR message was missed or arrived late.
                addActorFromServer(actorId, actorType, serializedState);
                return;
            }

            Map<String, String> stateData = ReplicationUtil.deserializeStateMap(serializedState);
            ReplicationUtil.applyReplicatedState(actor, stateData);
        }
    }

    public void removeActor(int actorId) {
        if(actorManager != null)
        {
            actorManager.removeActorClientSide(actorId);
            System.out.println("CLIENT: Removed actor " + actorId);
        }
    }
}