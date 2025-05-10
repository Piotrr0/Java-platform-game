package com.example.game;
import com.example.game.messages.ServerMessages;
import com.example.game.actors.ActorManager;
import com.example.game.actors.Actor;
import com.example.game.actors.Player;

import com.example.game.world.World;
import com.example.game.world.WorldFactory;
import com.example.game.world.WorldManager;


import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements Runnable{
    private ServerSocket socket;
    private List<Socket> connectedClients;
    private volatile boolean gameHasStarted = false;
    private final int port;
    private final String hostname;

    private WorldManager worldManager;

    private int nextPlayerId = 0;

    private ConcurrentLinkedQueue<ClientCommand> commandQueue = new ConcurrentLinkedQueue<>();

    //This variable defines how often server asks clients, it defines the ping/latency, its expressed in miliseconds
    private final int polingInterval  = 50;

    public int getPort(){
        return this.port;
    }
    public String getHostname(){
        return this.hostname;
    }

    public Server(String hostname,int port) throws IOException, ClassNotFoundException {
        System.out.println("SERVER: I AM THE SERVER!");
        this.port = port;
        this.hostname = hostname;
        this.socket = new ServerSocket(port, 50, InetAddress.ofLiteral(hostname));
        this.connectedClients = new CopyOnWriteArrayList<>();

        this.worldManager = new WorldManager();
        worldManager.initializeDefaultWorlds();
    }

    public void startGame()
    {
        System.out.println("SERVER: Game starting...");
        this.gameHasStarted = true;

        worldManager.setActiveWorld("Level2");
    }

    private void broadcastActorStates() throws IOException {
        World activeWorld = worldManager.getActiveWorld();
        if (activeWorld == null) return;

        ActorManager actorManager = activeWorld.getActorManager();
        List<Actor> actorSnapshot = new ArrayList<>(actorManager.actorsById.values());

        if (actorSnapshot.isEmpty()) return;

        List<String> updateMessages = new ArrayList<>();
        for (Actor actor : actorSnapshot) {
            String msg = String.format(java.util.Locale.US, ServerMessages.UPDATE_ACTOR + "%d:%s:%.2f:%.2f:%.2f:%.2f",
                    actor.getId(), actor.getType(), actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight());
            updateMessages.add(msg);
        }

        for (String msg : updateMessages) {
            broadcastMessage(msg);
        }
    }

    private void broadcastAddActor(Actor actor) throws IOException {
        String msg = String.format(java.util.Locale.US, ServerMessages.ADD_ACTOR + "%d:%s:%.2f:%.2f:%.2f:%.2f",
                actor.getId(), actor.getType(), actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight());
        broadcastMessage(msg);
    }

    private void broadcastRemoveActor(int actorId) throws IOException {
        String msg = ServerMessages.REMOVE_ACTOR + actorId;
        broadcastMessage(msg);
    }

    /**
     * It makes serverSocket stop listening for new joining clients, however it will still be able to communicate
     * with previously connected sockets.
     * */
    public void stopListeningForIncomingConnections() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
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
            out.flush();
        }
    }

    public void broadcastMessageToClient(String msg, Socket clientSocket) {
        if (clientSocket == null || clientSocket.isClosed()) {
            removeClient(clientSocket);
            return;
        }
        try {
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            removeClient(clientSocket);
        }
    }

    private void removeClient(Socket clientSocket) {
        if (connectedClients.remove(clientSocket)) {
            System.out.println("SERVER: Client disconnected: " + clientSocket.getRemoteSocketAddress());
        }
        try { clientSocket.close(); } catch (IOException e) { /* ignore */ }
    }

    @Override
    public void run() {
        while (!gameHasStarted) {
            try {
                Socket clientSocket = socket.accept();
                System.out.println("SERVER: Client connected from " + clientSocket.getRemoteSocketAddress());

                int playerId = nextPlayerId++;
                connectedClients.add(clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket, playerId, commandQueue);
                clientHandler.start();

                broadcastMessageToClient(ServerMessages.PLAYER_ID + playerId, clientSocket);
            }
            // TODO: I DO NOT KNOW BUT WITHOUT THIS IT DOESN'T WORK
            catch (SocketTimeoutException e) {
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    System.err.println("SERVER: Error accepting client connection: " + e.getMessage());
                } else {
                    System.out.println("SERVER: Server socket closed, stopping accept loop.");
                }
            } catch (ClassNotFoundException e) {
                System.err.println("SERVER: Error creating ClientHandler: " + e.getMessage());
            }
        }

        try {
            if (gameHasStarted) {
                broadcastMessage(ServerMessages.SET_GAME_SCENE + worldManager.getActiveWorld().getWorldName());
                setupPlayersInActiveWorld();

                broadcastActorStates();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long lastTickTime = System.currentTimeMillis();

        while (gameHasStarted) {
            long now = System.currentTimeMillis();
            long elapsedTime = now - lastTickTime;

            if (elapsedTime >= polingInterval) {
                lastTickTime = now;

                processClientCommands();
                worldManager.update();

                try {
                    broadcastActorStates();
                    broadcastMessage(ServerMessages.HAS_GAME_CHANGED);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    long sleepTime = polingInterval - elapsedTime;
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        for (Socket clientSocket : new ArrayList<>(connectedClients)) {
            try {
                clientSocket.close();
            } catch (IOException e) { /* ignore */ }
        }
        connectedClients.clear();
        commandQueue.clear();

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupPlayersInActiveWorld() {
        World activeWorld = worldManager.getActiveWorld();
        if (activeWorld == null) return;

        int playerCount = 0;
        for (int playerId = 0; playerId < nextPlayerId; playerId++) {
            double x = 100.0 + (playerCount * 60);
            double y = 100.0;
            worldManager.createPlayerInActiveWorld(playerId, x, y);
            playerCount++;
        }
    }

    public void changeWorld(String worldName) throws IOException {
        if (worldManager.setActiveWorld(worldName)) {
            World world = worldManager.getActiveWorld();

            broadcastMessage(ServerMessages.SET_GAME_SCENE + worldName);
            setupPlayersInActiveWorld();

            broadcastActorStates();
        }
    }

    private void processClientCommands() {
        ClientCommand command;
        World activeWorld = worldManager.getActiveWorld();
        if (activeWorld == null) return;

        ActorManager actorManager = activeWorld.getActorManager();

        while ((command = commandQueue.poll()) != null) {
            Player player = actorManager.getPlayer(command.playerId);
            if (player != null) {
                player.move(command.commandString);
            }
        }
    }

    private static class ClientCommand {
        int playerId;
        String commandString;

        ClientCommand(int playerId, String commandString) {
            this.playerId = playerId;
            this.commandString = commandString;
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private int playerId;
        private DataInputStream in;
        private ConcurrentLinkedQueue<ClientCommand> sharedCommandQueue;


        public ClientHandler(Socket socket, int playerId, ConcurrentLinkedQueue<ClientCommand> commandQueue) throws IOException, ClassNotFoundException {
            this.socket = socket;
            this.playerId = playerId;
            this.sharedCommandQueue = commandQueue;
            this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                while (socket.isConnected()) {
                    String message = in.readUTF();
                    System.out.println("SERVER: Received from player " + playerId + ": " + message);

                    sharedCommandQueue.offer(new ClientCommand(playerId, message));
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            finally {
                try { if (in != null) in.close(); } catch (IOException e) {}
                try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException e) {}
                connectedClients.remove(socket);
            }
        }
    }
}