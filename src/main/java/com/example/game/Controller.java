package com.example.game;

import com.example.game.actors.*;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
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
    boolean firstPlayer = true; //first player added is mario, second is luigi, mitigates problem above

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

    Text topLeftText,gameOverText,youWonGameText;

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
        try {
            Main.currentClient = new Client(hostname, port, this);
            joinLobbyButton.setVisible(false);
            lobbyInformation.setVisible(true);
        } catch (SocketTimeoutException e) {
            System.out.println("Połączenie nie powiodło się: timeout");
        }
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

    public void refreshScoreText(String msg){
        topLeftText.setText(msg);
        System.out.println("score updated: " + msg);
    }

    public void showGameOver(){
        gameOverText.setVisible(true); // Hide initially
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

        // Create and style the text
        topLeftText = new Text(10, 20, "Score: 0"); // X=10, Y=20 (top-left)
        topLeftText.setFill(Color.WHITE); // Set text color
        topLeftText.setFont(Font.font("Arial", 16)); // Set font and size
        gamePane.getChildren().add(topLeftText);

        // Create and style the text
        gameOverText = new Text(50, 50, "GAME OVER!"); // X=10, Y=20 (top-left)
        gameOverText.setFill(Color.WHITE); // Set text color
        gameOverText.setFont(Font.font("Arial", 50)); // Set font and size
        gameOverText.setVisible(false); // Hide initially
        gamePane.getChildren().add(gameOverText);

        double textWidth = gameOverText.getLayoutBounds().getWidth();
        double textHeight = gameOverText.getLayoutBounds().getHeight();
        double paneWidth = gamePane.getPrefWidth() > 0 ? gamePane.getPrefWidth() : 700;
        double paneHeight = gamePane.getPrefHeight() > 0 ? gamePane.getPrefHeight() : 550;

        // Center the text
        gameOverText.setX((paneWidth - textWidth) / 2);
        gameOverText.setY(50);

        // Create and style the text
        youWonGameText = new Text(50, 50, "YOU WON!"); // X=50, Y=50 - tymczasowe, zostanie nadpisane
        youWonGameText.setFill(Color.LIME); // Zielony kolor zwycięstwa
        youWonGameText.setFont(Font.font("Arial", 50)); // Set font and size
        youWonGameText.setVisible(false); // Hide initially
        gamePane.getChildren().add(youWonGameText);

// Get dimensions
        double textYouWonWidth = youWonGameText.getLayoutBounds().getWidth();
        double textYouWonHeight = youWonGameText.getLayoutBounds().getHeight();


// Center the text
        youWonGameText.setX((paneWidth - textYouWonWidth) / 2);
        youWonGameText.setY(50);


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
        //this doesn't work... (ALWAYS returns false)
        //actors always get 8 or 9 - shown in console when launching many games
        //something has to be broken in serialization and naming id I guess
        //localplayerid is always 1 or 2
        boolean isLocal = (actorId == localPlayerId);

        //for some reason same assets are loaded multiple times (seen in console)
        if ("Player".equals(actorType)) {
            System.out.println(firstPlayer);
            if(firstPlayer) {
                actor = new Player(actorId, 0, 0, true);
                firstPlayer = false;
            }
            else {
                actor = new Player(actorId, 0, 0, false);
                firstPlayer = true;
            }
            System.out.println("actor id: " + actorId + " lokal: " + localPlayerId);
        }
        else if("Crate".equals(actorType)){
            actor = new Prop(0, 0, 0, 0,Color.ORANGE,"Crate.png","Crate");
        }
        else if("Coin".equals(actorType)){
            actor = new Prop(0, 0, 0, 0,Color.BLUE,"coin_spin.gif","Coin");
        }
        else if("Enemy".equals(actorType)){
            actor = new Enemy(0, 0,"tutel.png");
        }
        else{
            actor = new Actor(0, 0, 0, 0,Color.BROWN);
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

    public void showYouWon() {
        System.out.println("It should show text: YOU WON");
        youWonGameText.setVisible(true); // Hide initially

    }
}