package com.example.game;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements Runnable{
    private ServerSocket socket;
    private List<Socket> connectedClients;
    private boolean gameHasStarted = false;
    private int port;
    private String hostname;

    private Map<Integer, double[]> playerPositions;
    private int nextPlayerId = 0;

    //This variable defines how often server asks clients, it defines the ping/latency, its expressed in miliseconds
    private int polingInterval = 50;

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
        this.connectedClients = new CopyOnWriteArrayList<>();
        this.playerPositions = new ConcurrentHashMap<>();
    }

    public void startGame()
    {
        this.gameHasStarted = true;
    }

    private void broadcastPositions() throws IOException
    {
        if (playerPositions.isEmpty()) return;

        for (Map.Entry<Integer, double[]> entry : playerPositions.entrySet())
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
        for (Socket connectedClient : connectedClients)
        {
            DataOutputStream out = new DataOutputStream(connectedClient.getOutputStream());
            out.writeUTF(msg);
        }
    }

    @Override
    public void run() {
        //It's listening for incoming connection, it stops running when gameHasStarted
        while(!gameHasStarted &&!socket.isClosed())
        {
            if(gameHasStarted)  break;

            Socket clientSocket = null;
            try {
                clientSocket = socket.accept();
                System.out.println("SERVER: Client connected from " + clientSocket.getRemoteSocketAddress());

                int playerId = nextPlayerId++;
                connectedClients.add(clientSocket);
                playerPositions.put(playerId, new double[]{100.0 + (playerId * 60), 100.0});

                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                out.writeUTF("PLAYER_ID:" + playerId);

                ClientHandler clientHandler = new ClientHandler(clientSocket, playerId);
                clientHandler.start();

            }
            catch (IOException | ClassNotFoundException e) {
                System.out.println("It throws an exception,however it might be due to the fact that serverSocket was closed");
            }
        }


        if(gameHasStarted)
        {
            try {
                Thread.sleep(100);
                broadcastMessage("LOAD_MAP");
            } catch (Exception e) {
                System.out.println("Problem with loading a map!");
            }
        }

        while (gameHasStarted)
        {
            try
            {
                Thread.sleep(polingInterval);
                broadcastPositions();
                broadcastMessage("HAS_GAME_CHANGED");
            }
            catch (InterruptedException | IOException e)
            {
                gameHasStarted = false;
                Thread.currentThread().interrupt();
            }
        }

        for (Socket clientSocket : connectedClients) {
            try {
                clientSocket.close();
            } catch (IOException e) { /* ignore */ }
        }

        connectedClients.clear();
        playerPositions.clear();
    }

    private class ClientHandler extends Thread{
        private Socket socket;
        private int playerId;

        public ClientHandler(Socket socket, int playerId) throws IOException, ClassNotFoundException {
            this.socket = socket;
            this.playerId = playerId;
            System.out.println("SERVER: New user has connected with ID: " + playerId);
        }
        @Override
        public void run()
        {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
                while(socket.isConnected()){
                    String message = in.readUTF();

                    double[] pos = playerPositions.get(playerId);
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
                        default:
                            break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}