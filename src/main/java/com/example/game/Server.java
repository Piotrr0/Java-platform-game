package com.example.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    private ServerSocket socket;
    public Server(int port) throws IOException, ClassNotFoundException {
        this.socket = new ServerSocket(port, 0, InetAddress.getByName("localhost"));

        System.out.println("The server has been set up");
    }

    @Override
    public void run() {
        //It's listening for incoming connection using main thread
        while(!socket.isClosed()){
            System.out.println("It is checking for incoming connections all the time");
            //It blocks because accepting is a while loop until a new user connects
            Socket clientSocket = null;
            try {
                clientSocket = socket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }


            //It happens after new user has been connected with our server

        }
    }

    private class ClientHandler extends Thread{
        private Socket socket;
        private int clientPort;
        public ClientHandler(Socket socket) throws IOException, ClassNotFoundException {
            this.socket = socket;
            this.clientPort = socket.getPort();
            System.out.println("SERVER: New user has connected");
        }
        @Override
        public void run() {
            while(this.socket.isConnected()){
                System.out.println("SERVER: It listens for user "+clientPort);
            }
        }
    }

    public static void main(String [ ] args) throws IOException, ClassNotFoundException {


    }
}
