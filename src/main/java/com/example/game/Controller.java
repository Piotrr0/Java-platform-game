package com.example.game;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.w3c.dom.css.RGBColor;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller {

    @FXML
    Button hostMenuButton, goBackToMenuButton, joinMenuButton= new Button(), joinLobbyButton, startButton;
    @FXML
    TextField hostnameField, portField,lobbyInformation;

    @FXML
    GridPane gridMap;
    @FXML
    Button buttonTest;

    /**
     * <code>GridMap</code> contains rectangle in each Cell. This is the list that stores them.
     * It's one dimension since weird behavior of JavaFX GridPane. Let's say that you have 10 x 10 gridPane:
     * 1st element has index 0
     * 10th element has index 9
     * element in right left cornet has index of 99
     * So there is a pattern how to calculate index based on its column and row.
     * */
    private ArrayList<Rectangle> rectangleList;





    /*
        IT stores a server object, if you wonder why the fuck this is static it is because
        Controller changes each time we load new .fxml file so we want to have it static, is there better solution? Probably YES
    */
    private static Server server = null;

    /*
        Same idea as above — we store the main window statically so we don’t have to fetch it all the time
    */
    static Stage mainStage;

    public Controller(){
        this.rectangleList = new ArrayList<>();
    }

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public void handleHostMenuButton(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        Parent root = FXMLLoader.load(getClass().getResource("hostMenu.fxml"));
        mainStage.setScene(new Scene(root));

        int port = 50000; // or any free port number you like
        String hostname = "10.10.10.124";
        server = new Server(hostname,port);
        Thread serverThread = new Thread(server);
        serverThread.start(); // starts the run() method in a new thread
    }

    public void handleJoinMenuButton(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        Parent root = FXMLLoader.load(getClass().getResource("joinMenu.fxml"));
        mainStage.setScene(new Scene(root));
    }

    public void joinLobby(ActionEvent actionEvent) throws IOException {
        String hostname = hostnameField.getText();


        int port = Integer.parseInt(portField.getText());

        Main.currentClient = new Client(hostname, port,this);

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
            Main.currentClient = new Client(server.getHostname(), server.getPort(),this);
            server.stopListeningForIncomingConnections(); // This should close serverSocket
            server.startGame(); // This changes server's state



        } else {
            System.out.println("server is null :/");
        }
    }




    public void loadMap() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("client.fxml"));
        mainStage.setScene(new Scene(root));
        this.gridMap = (GridPane) mainStage.getScene().lookup("#gridMap");

        /**
         * We already have created gridPane inside .fxml file but we also want to fill it with rectangles because rectangles are editable
         * */
        for(int col =0;col<gridMap.getColumnCount();col++){

            for(int row =0;row<gridMap.getRowCount();row++){
                Pane cellContainer = new Pane();
                Rectangle rectangle = new Rectangle();
                rectangle.setFill(Color.GOLD);

                // Make rectangle match container size
                rectangle.widthProperty().bind(cellContainer.widthProperty());
                rectangle.heightProperty().bind(cellContainer.heightProperty());

                cellContainer.getChildren().add(rectangle);
                gridMap.add(cellContainer, col, row);
                rectangleList.add(rectangle);
            }

        }




    }



    /**
     * It changes the color of the rectangle inside <code>gridMap</code>
     * @param r red color for rgb
     * @param g green color for rgb
     * @param b blue color for rgb
     * @param id id of a rectangle
     * */
    public void changeRectangleColor(int r, int g, int b, int id) {
        rectangleList.get(id).setFill(Color.rgb(r,g,b));
    }
}
