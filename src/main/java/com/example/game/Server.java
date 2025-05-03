package com.example.game;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Server implements Runnable{
    private ServerSocket socket;
    /**
     * A list of integers that stores clients' sockets.
     * */
    private ArrayList<Socket> connectedClients;
    private boolean gameHasStarted = false;
    private int port;
    private String hostname;
    private Random random;
    private int numberOfRows,numberOfColumns;




    public int getPort(){
        return this.port;
    }
    public String getHostname(){
        return this.hostname;
    }


    public Server(String hostname,int port) throws IOException, ClassNotFoundException {
        System.out.println("The server has been set up with "+numberOfRows+" rows and "+numberOfColumns+" columns");
        this.port = port;
        this.hostname = hostname;
        this.numberOfColumns = 0;
        this.numberOfRows = 0;
        this.socket = new ServerSocket(port, 0, InetAddress.ofLiteral(hostname));
        this.connectedClients = new ArrayList<>();
        //It's useful only for testing blinking rectangles, you can remove if if you want
        this.random = new Random();
    }

    /**
     * It changes the state of <code>gameHasStarted</code> to True.
     * */
    public void startGame(){
        this.gameHasStarted = true;
    }

    /**
     * It makes serverSocket stop listening for new joining clients, however it will still be able to communicate
     * with previously connected sockets.
     * */
    public void stopListeningForIncomingConnections() throws IOException {
        socket.close();
    }

    /**
     * It broadcasts message to everyone, basically saying it sends message to everyone in the list of clients
     * @param msg content of message to be sent
     * */
    public void broadcastMessage(String msg) throws IOException {
        for(int i =0;i<connectedClients.size();i++){
            DataOutputStream out = new DataOutputStream(connectedClients.get(i).getOutputStream());
            out.writeUTF(msg);
        }
    }

    /**
     * It sets the current number of rows and columns of the GridMap, it is useful to track the state of game
     * */
    public void setNumberOfRowsAndColumns(int numberOfRows,int numberOfColumns){
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
    }




    @Override
    public void run() {
        //It's listening for incoming connection, it stops running when gameHasStarted
        while(!socket.isClosed()){
            if(gameHasStarted)
                break;
            System.out.println("It is checking for incoming connections all the time");
            //It blocks because accepting is a while loop until a new user connects
            Socket clientSocket = null;
            try {
                clientSocket = socket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
                //Add sockets to the list so server has easy access in future
                connectedClients.add(clientSocket);

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("It throws an exception,however it might be due to the fact that serverSocket was closed");
            }


            //It happens after new user has been connected with our server

        }


        //Once the game has started we tell all users to load a map
        if(gameHasStarted){
            try {
                broadcastMessage("LOAD_MAP");
                //Let's wait with handling the game logic for a moment because we should give clients some time to process loading a map
                Thread.sleep(2000);
            } catch (Exception e) {
                System.out.println("Problem with loading a map!");
            }
        }


        while(gameHasStarted){
            try {
                Thread.sleep(1000);
                int r = this.random.nextInt(256); // 0–255
                int g = this.random.nextInt(256);
                int b = this.random.nextInt(256);
                int id = this.random.nextInt(110); // 0 to rows*columns - 1
                broadcastMessage("RectangleChangeColor;"+r+";"+g+";"+b+";"+1);
                broadcastMessage("HAS_GAME_CHANGED");



            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
                try {
                    DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                    System.out.println("Otrzymalem wiadomosc ze : "+in.readUTF());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
