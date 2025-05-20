package com.example.game;

import com.example.game.actors.*;
import com.example.game.messages.ServerMessages;
import com.example.game.network.ReplicationUtil;
import com.example.game.world.World;
import com.example.game.world.WorldManager;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements Runnable {

    private ServerSocket socket; //stay
    private final int port; //stay
    private final String hostname; //stay
    private List<Game> games;
    private List<Thread> gameThreads;



    //This variable defines how often server asks clients, it defines the ping/latency, its expressed in miliseconds
    private final int polingInterval = 30;

    public int getPort() {
        return this.port;
    }

    public String getHostname() {
        return this.hostname;
    }

    public Server(String hostname, int port) throws IOException, ClassNotFoundException {
        System.out.println("SERVER: I AM THE SERVER!");
        this.port = port;
        this.hostname = hostname;
        this.socket = new ServerSocket(port, 50, InetAddress.ofLiteral(hostname));
        this.games = new ArrayList<>();
        this.gameThreads = new ArrayList<>();

    }




    /**
     * It makes serverSocket stop listening for new joining clients, however it will still be able to communicate
     * with previously connected sockets.
     */
    public void stopListeningForIncomingConnections() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public void shutdown() {
        try {
            stopListeningForIncomingConnections();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //THIS BUT FOR ALL GAMES

        //for (ClientConnection clientConn : new ArrayList<>(connectedClients)) {
        //    clientConn.closeConnection("Server shutting down");
        //}

        //connectedClients.clear();
        //commandQueue.clear();
    }




    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                //first player
                Game game = new Game();
                Socket clientSocket = socket.accept();
                System.out.println("SERVER: Client connected from " + clientSocket.getRemoteSocketAddress());
                int newPlayerId = 0;
                game.addPlayer(clientSocket, newPlayerId);

                //second player
                clientSocket = socket.accept();
                System.out.println("SERVER: Client connected from " + clientSocket.getRemoteSocketAddress());
                newPlayerId = 1;
                game.addPlayer(clientSocket, newPlayerId);
                game.gameHasStarted=true; //start the game
                //thread has to be here to work
                //the function itself will be launched automatically when 2 players are connected but for now Game class has to be expanded
                Thread gameThread = new Thread(game);
                gameThread.start();
                gameThreads.add(gameThread);
                games.add(game);



                /*
                if (gameHasStarted) {
                    World activeWorld = worldManager.getActiveWorld();
                    if (activeWorld != null) {
                        Player player = activeWorld.getActorManager().createPlayer(newPlayerId, 100, 100);
                        if (player != null) {
                            clientConnection.sendMessage(ServerMessages.SET_GAME_SCENE + activeWorld.getWorldName());
                            broadcastAddActor(player);
                            sendFullWorldStateToPlayer(clientConnection);


                        }
                    }
                }

                 */

            } // TODO: I DO NOT KNOW BUT WITHOUT THIS IT DOESN'T WORK
            catch (SocketTimeoutException e) {
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    System.err.println("SERVER: Error accepting client connection: " + e.getMessage());
                } else {
                    System.out.println("SERVER: Server socket closed, stopping accept loop.");
                }
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

    private class ClientConnection extends Thread {
        private Socket socket;
        private int playerId;
        private DataInputStream in;
        private DataOutputStream out;
        private ConcurrentLinkedQueue<ClientCommand> sharedCommandQueue;
        private volatile boolean connectionOpen = true;

        public ClientConnection(Socket socket, int playerId,
                                ConcurrentLinkedQueue<ClientCommand> commandQueue) throws IOException {
            this.socket = socket;
            this.playerId = playerId;
            this.sharedCommandQueue = commandQueue;
            this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        }

        public int getPlayerId() {
            return playerId;
        }

        public Socket getSocket() {
            return socket;
        }

        public void sendMessage(String msg) {
            if (!connectionOpen || socket.isClosed() || out == null) return;
            try {
                out.writeUTF(msg);
                out.flush();
            } catch (IOException e) {
                closeConnection("Send error");
            }
        }

        public void closeConnection(String reason) {
            if (!connectionOpen) return;
            connectionOpen = false;
            try {
                if (in != null) in.close();
            } catch (IOException e) { /* ignore */ }
            try {
                if (out != null) out.close();
            } catch (IOException e) { /* ignore */ }
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) { /* ignore */ }

            //notify server - server finds game with this - game closes connection
            //removeClientServer(this);
        }


        @Override
        public void run() {
            try {
                while (socket.isConnected()) {
                    String message = in.readUTF();
                    //System.out.println("SERVER: Received from player " + playerId + ": " + message);

                    sharedCommandQueue.offer(new ClientCommand(playerId, message));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                closeConnection("Connection ended");
            }
        }
    }

    //BASIC CLASS FOR STARTING GAME IN NEW THREAD - gotta start somewhere with infinite games
    //end goal is having an array of Game class objects all of which are different games with different maps
    private class Game implements Runnable {

        private List<ClientConnection> connectedClients;
        private WorldManager worldManager;
        private ConcurrentLinkedQueue<ClientCommand> commandQueue; //in game
        public boolean gameHasStarted = false;

        public Game()
        {
            this.connectedClients = new CopyOnWriteArrayList<>();
            this.worldManager = new WorldManager();
            worldManager.initializeDefaultWorlds();
            commandQueue = new ConcurrentLinkedQueue<>();
        }

        public void addPlayer(Socket clientSocket, int newPlayerId)
        {
            if(connectedClients.size()<2)
            try {
                ClientConnection clientConnection = new ClientConnection(clientSocket, newPlayerId, commandQueue);
                connectedClients.add(clientConnection);
                clientConnection.start();
                clientConnection.sendMessage(ServerMessages.PLAYER_ID + (newPlayerId+1)); //+1 because player 1 & 2 but 0 & 1 in code
            }
            catch(IOException e)
            {
                System.err.println(e.getMessage());
            }
        }

        @Override
        public void run(){



            World activeWorld = worldManager.getActiveWorld();
            if (activeWorld == null) {
                if (!worldManager.setActiveWorld("Level2")) {
                    return;
                }
                activeWorld = worldManager.getActiveWorld();
            }

            for (ClientConnection clientConnection : new ArrayList<>(connectedClients)) { // Iterate a copy
                if (activeWorld.getActorManager().getPlayer(clientConnection.getPlayerId()) == null) {
                    Player player = activeWorld.getActorManager().createPlayer(
                            clientConnection.getPlayerId(),
                            100.0 + (clientConnection.getPlayerId() * 60),
                            100.0
                    );
                    if (player != null) {
                        broadcastAddActor(player);
                    }
                }
            }

            broadcastMessage(ServerMessages.SET_GAME_SCENE + activeWorld.getWorldName());
            for (ClientConnection cc : connectedClients) {
                sendFullWorldStateToPlayer(cc);
            }



            long lastTickTime = System.currentTimeMillis();
            while (gameHasStarted) {
                long now = System.currentTimeMillis();
                long elapsedTime = now - lastTickTime;

                if (elapsedTime >= polingInterval) {
                    lastTickTime = now;

                    processClientCommands();
                    if (worldManager.getActiveWorld() != null) {
                        worldManager.update();
                    }

                    deleteActors();
                    broadcastActorStates();
                    broadcastMessage(ServerMessages.HAS_GAME_CHANGED);


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
            shutdown();
        }

        /**
         * Function takes care of Actors that have a field of <code>toBeDeleted</code> set to true. It is responsible for checking which Actors should be deleted and makes sure that they
         * are deleted on both server and client.
         * */
        private void deleteActors(){
            for(int i = 0;i<worldManager.getActiveWorld().getActorManager().getAllActorsServer().size();i++){
                Actor actor = worldManager.getActiveWorld().getActorManager().getAllActorsServer().get(i);
                int idOfActor = worldManager.getActiveWorld().getActorFromId(worldManager.getActiveWorld().getActorManager().getAllActorsServer().get(i).getId()).getId();
                boolean shouldBeDeleted = worldManager.getActiveWorld().getActorFromId(actor.getId()).isToBeDeleted();
                if(shouldBeDeleted){
                    if(actor.getType()=="Prop"){
                        handlePropRemoval((Prop) actor);
                    }
                    worldManager.getActiveWorld().getActorManager().removeActor(idOfActor);
                    broadcastMessage("REMOVE_ACTOR:"+idOfActor);
                }
            }
        }

        /**
         * This function handles the prop removal, based on type of prop we can decide what to do, e.g if we destroy the chest we can increase points or something.
         * **/
        private void handlePropRemoval(Prop prop) {
            System.out.println("Im deleting a prop with a type of "+prop.getPropType());
        }

        private void broadcastActorStates() {
            World activeWorld = worldManager.getActiveWorld();
            if (activeWorld == null || activeWorld.getActorManager() == null) return;

            ActorManager actorManager = activeWorld.getActorManager();
            List<Actor> actorSnapshot = actorManager.getAllActorsServer();

            if (actorSnapshot.isEmpty()) return;

            for (Actor actor : actorSnapshot) {
                Map<String, Object> replicatedState = ReplicationUtil.getReplicatedState(actor);
                String serializedState = ReplicationUtil.serializeStateMap(replicatedState);

                // Message format: UPDATE_ACTOR:<id>:<type>:<serialized_state>
                String msg = ServerMessages.UPDATE_ACTOR +
                        actor.getId() + ":" +
                        actor.getType() + ":" +
                        serializedState;
                broadcastMessage(msg);
            }
        }

        public void broadcastAddActor(Actor actor) {
            Map<String, Object> replicatedState = ReplicationUtil.getReplicatedState(actor);
            String serializedState = ReplicationUtil.serializeStateMap(replicatedState);

            // Message format: ADD_ACTOR:<id>:<type>:<serialized_state>
            String msg = ServerMessages.ADD_ACTOR +
                    actor.getId() + ":" +
                    actor.getType() + ":" +
                    serializedState;
            broadcastMessage(msg);
        }

        public void broadcastRemoveActor(int actorId) {
            String msg = ServerMessages.REMOVE_ACTOR + actorId;
            broadcastMessage(msg);
        }

        private void removeClientGame(ClientConnection clientConnection) {
            if (clientConnection == null) return;
            connectedClients.remove(clientConnection);

            World activeWorld = worldManager.getActiveWorld();
            if (activeWorld != null && activeWorld.getActorManager() != null) {
                activeWorld.getActorManager().removeActor(clientConnection.getPlayerId());
                broadcastRemoveActor(clientConnection.getPlayerId());
            }
        }

        private void sendFullWorldStateToPlayer(ClientConnection newClientConn) {
            World activeWorld = worldManager.getActiveWorld();
            if (activeWorld == null || activeWorld.getActorManager() == null) return;

            ActorManager actorManager = activeWorld.getActorManager();
            List<Actor> allActors = actorManager.getAllActorsServer();

            for (Actor actor : allActors) {
                if (actor.getId() == newClientConn.getPlayerId() && actor instanceof Player) continue;

                Map<String, Object> replicatedState = ReplicationUtil.getReplicatedState(actor);
                String serializedState = ReplicationUtil.serializeStateMap(replicatedState);
                String msg = ServerMessages.ADD_ACTOR +
                        actor.getId() + ":" +
                        actor.getType() + ":" +
                        serializedState;
                newClientConn.sendMessage(msg);
            }
        }
        private void processClientCommands() {
            ClientCommand command;
            World activeWorld = worldManager.getActiveWorld();
            if (activeWorld == null || activeWorld.getActorManager() == null) return;

            ActorManager actorManager = activeWorld.getActorManager();
            while ((command = commandQueue.poll()) != null) {
                Player player = actorManager.getPlayer(command.playerId);
                if (player != null) {
                    if(Objects.equals(command.commandString, "SHOOT")){
                        Actor arrow = new Arrow(300,player.getX(),player.getY()+70,30,30);
                        actorManager.addActor(arrow);
                        System.out.println(player.getPlayerId()+"wants to shoot");
                    }
                    else{
                        player.move(command.commandString);

                    }
                }
            }
        }

        public void changeWorld(String worldName) throws IOException {
            if (worldManager.setActiveWorld(worldName)) {
                World world = worldManager.getActiveWorld();
                if (world == null) {
                    return;
                }

                List<Integer> currentPlayerIds = new ArrayList<>();
                for (ClientConnection cc : connectedClients) {
                    currentPlayerIds.add(cc.getPlayerId());
                }

                for (int pId : currentPlayerIds) {
                    world.getActorManager().createPlayer(pId, 50.0, 50.0);
                }

                broadcastMessage(ServerMessages.SET_GAME_SCENE + worldName);

                for (ClientConnection cc : connectedClients) {
                    sendFullWorldStateToPlayer(cc);
                }
            }
        }

        /**
         * It broadcasts message to everyone, basically saying it sends message to everyone in the list of clients
         *
         * @param msg content of message to be sent
         */
        public void broadcastMessage(String msg) {
            for (ClientConnection client : new ArrayList<>(connectedClients)) {
                client.sendMessage(msg);
            }
        }

        public void sendMessageToPlayer(int playerId, String msg) {
            for (ClientConnection clientConn : connectedClients) {
                if (clientConn.getPlayerId() == playerId) {
                    clientConn.sendMessage(msg);
                    return;
                }
            }
        }
        private void removeClientServer(ClientConnection clientConnection){
            if (clientConnection == null) return;
            connectedClients.remove(clientConnection);


        }
    }


}