package com.example.game;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.game.Controller.mainStage;

/**
 * For now a client has a special thread that listents to incoming messages, if it receives one it is sent to a message that should handle it.
 * */

public class Client{
    private String serverIPAdress;
    private int serverPort;
    private Socket clientSocket;

    //We should edit this when we want to change the map
    private Controller controller;


    public Client(String serverIPAdress,int serverPort,Controller controller) throws IOException {
        this.clientSocket = new Socket(serverIPAdress,serverPort);
        this.controller = controller;
        ClientReceiver clientReceiver = new ClientReceiver();
        Thread clientReceiverThread = new Thread(clientReceiver);
        clientReceiverThread.start();
    }

    public void sendDataToserver(String msg) throws IOException {
        DataOutputStream out = new DataOutputStream(this.clientSocket.getOutputStream());
        out.writeUTF(msg);
    }

    private class ClientReceiver implements Runnable{


    /**
     * This function is called by other function and it is used to handle a message from request
     * @param msg content of the message inside the request
     * */
    private void handleSocketMessage(String msg) throws IOException {


        if (Objects.equals(msg, "LOAD_MAP")){

            Platform.runLater(()->{
                try {
                    controller = controller.loadMap();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
            return;



        }


        /**Pattern for a request to change color of rectangle is
         * RectangleChangeColor;R;G;B;ID e.g RectangleChangeColor;40;50;60;5 means that it should change color to RGB(40,50,60) for a rectangle with ID of 5
         * */
        Pattern rectangleColorPattern = Pattern.compile("^RectangleChangeColor;(\\d+);(\\d+);(\\d+);(\\d+)$");
        Matcher rectangleMatcher = rectangleColorPattern.matcher(msg);
        if (rectangleMatcher.matches()) {
            int r = Integer.parseInt(rectangleMatcher.group(1));
            int g = Integer.parseInt(rectangleMatcher.group(2));
            int b = Integer.parseInt(rectangleMatcher.group(3));
            int id = Integer.parseInt(rectangleMatcher.group(4));

            if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
                controller.changeRectangleColor(r, g, b, id); // Hypothetical method
            } else {
                System.out.println("Invalid RGB values: " + msg);
            }
            return;
        }

        if(Objects.equals(msg, "HAS_GAME_CHANGED")){
            //Sprawdzamy jaki przycisk zostal nacisniety
            Event pressedKey = controller.returnPressedKey();
            if(pressedKey == null){
                //System.out.println("Uzytkownik nic nie wcisnal, powiedzmy serwerowi ze ten klient nie chce sie poruszac");
            }
            else{
                switch (((KeyEvent)pressedKey).getCode()){
                    case UP:
                        sendDataToserver("MOVE_UP");
                        break;
                    case DOWN:
                        sendDataToserver("MOVE_DOWN");
                        break;
                    case RIGHT:
                        sendDataToserver("MOVE_RIGHT");
                        break;
                    case LEFT:
                        sendDataToserver("MOVE_LEFT");
                        break;
                }


            }
            return;
        }

        System.out.println("Nieznana komenda! Odebrano: "+msg);

    }

    @Override
    public void run() {
        System.out.println("Client has started working");
        while(true){
            try {
                DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                handleSocketMessage(in.readUTF());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }



    }





}

