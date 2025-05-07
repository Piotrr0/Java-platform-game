package com.example.game;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Server implements Runnable{
    private ServerSocket socket;
    private ArrayList<Socket> connectedClients;
    private boolean gameHasStarted = false;
    private int port;
    private String hostname;

    private HashMap<Integer, double[]> playerPositions;
    private int nextPlayerId = 0;

    //This variable defines how often server asks clients, it defines the ping/latency, its expressed in miliseconds
    private int polingInterval = 100;

    public int getPort(){
        return this.port;
    }
    public String getHostname(){
        return this.hostname;
    }

    public Server(String hostname,int port) throws IOException, ClassNotFoundException {
        System.out.println("I AM THE SERVER!");
        this.port = port;
        this.hostname = hostname;
        this.socket = new ServerSocket(port, 0, InetAddress.ofLiteral(hostname));
        this.connectedClients = new ArrayList<>();
        this.playerPositions = new HashMap<>();

        playerPositions.put(0, new double[]{200, 200});
    }

    public void startGame()
    {
        this.gameHasStarted = true;

        Thread gameLoopThread = new Thread(this::gameLoop);
        gameLoopThread.start();
    }

    private void gameLoop()
    {
        while (gameHasStarted) {
            try
            {
                Thread.sleep(polingInterval);
                broadcastPositions();
            }
            catch (Exception e)
            {
                System.err.println("Error in game loop: " + e.getMessage());
            }
        }
    }

    private void broadcastPositions() throws IOException
    {
        for (HashMap.Entry<Integer, double[]> entry : playerPositions.entrySet())
        {
            int playerId = entry.getKey();
            double[] pos = entry.getValue();

            String posMsg = String.format(java.util.Locale.US, "POSITION_UPDATE:%d:%.2f:%.2f", playerId, pos[0], pos[1]);
            broadcastMessage(posMsg);
        }
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
        for (int i = 0; i < connectedClients.size(); i++)
        {
            DataOutputStream out = new DataOutputStream(connectedClients.get(i).getOutputStream());
            out.writeUTF(msg);
        }
    }

    @Override
    public void run() {
        //It's listening for incoming connection, it stops running when gameHasStarted
        while(!gameHasStarted &&!socket.isClosed())
        {
            System.out.println("It is checking for incoming connections all the time");
            //It blocks because accepting is a while loop until a new user connects
            Socket clientSocket = null;
            try {
                clientSocket = socket.accept();

                int playerId = nextPlayerId++;
                playerPositions.put(playerId, new double[]{200, 200});

                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                out.writeUTF("PLAYER_ID:" + playerId);

                ClientHandler clientHandler = new ClientHandler(clientSocket, playerId);
                clientHandler.start();
                //Add sockets to the list so server has easy access in future
                connectedClients.add(clientSocket);

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("It throws an exception,however it might be due to the fact that serverSocket was closed");
            }
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
    }

    private class ClientHandler extends Thread{
        private Socket socket;
        private int clientPort;
        private int playerId;

        public ClientHandler(Socket socket, int playerId) throws IOException, ClassNotFoundException {
            this.socket = socket;
            this.clientPort = socket.getPort();
            this.playerId = playerId;
            System.out.println("SERVER: New user has connected with ID: " + playerId);
        }
        @Override
        public void run()
        {
            while(this.socket.isConnected()){
                try
                {
                    DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    String message = in.readUTF();

                    double[] pos = playerPositions.get(playerId);
                    if (pos != null) {
                        double moveAmount = 10.0;

                        switch (message) {
                            case "MOVE_UP":
                                pos[1] -= moveAmount;
                                break;
                            case "MOVE_DOWN":
                                pos[1] += moveAmount;
                                break;
                            case "MOVE_RIGHT":
                                pos[0] += moveAmount;
                                break;
                            case "MOVE_LEFT":
                                pos[0] -= moveAmount;
                                break;
                        }

                        playerPositions.put(playerId, pos);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            playerPositions.remove(playerId);
            connectedClients.remove(socket);
        }
    }
}