package com.example.game;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

import static com.example.game.Controller.mainStage;


public class Client implements Runnable{
    private String serverHostname;
    private int serverPort;
    private Socket clientSocket;

    public Client(String serverHostname,int serverPort) throws IOException {
        this.clientSocket = new Socket(serverHostname,serverPort);
    }



    public void loadMap() throws IOException {
        Controller.loadMap();
    }

    @Override
    public void run() {
        System.out.println("Client has started working");
        
    }
}
