package com.example.game;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

import static com.example.game.Controller.mainStage;


public class Client{
    private String serverHostname;
    private int serverPort;
    private Socket clientSocket;

    public Client(String serverHostname,int serverPort) throws IOException {
        this.clientSocket = new Socket(serverHostname,serverPort);

        ClientSender clientSender = new ClientSender();
        ClientReceiver clientReceiver = new ClientReceiver();


        Thread clientSenderThread = new Thread(clientSender);
        clientSenderThread.start();

        Thread clientReceiverThread = new Thread(clientReceiver);
        clientReceiverThread.start();


    }

    private class ClientSender implements Runnable{

        @Override
        public void run() {

        }
    }

    private class ClientReceiver implements Runnable{

    public void handleSocketMessage(String msg) throws IOException {
        if (Objects.equals(msg, "LOAD_MAP")){


            Platform.runLater(()->{
                try {
                    loadMap();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });


            System.out.println("I should load map right now");
        }
        else{
            System.out.println("Nieznana komenda! Odebrano: "+msg);
        }
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


    public void loadMap() throws IOException {
        Controller.loadMap();
    }


}

