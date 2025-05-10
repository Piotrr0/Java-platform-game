package com.example.game;
import com.example.game.actors.ActorManager;
import com.example.game.messages.ServerMessages;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * For now a client has a special thread that listents to incoming messages, if it receives one it is sent to a message that should handle it.
 * */

public class Client{
    private Socket clientSocket;

    //We should edit this when we want to change the map
    private Controller controller;
    private int playerId = -1;

    private Set<String> pendingCommands = new HashSet<>();

    private DataOutputStream out;
    private DataInputStream in;

    public void addPendingCommand(String command) {
        pendingCommands.add(command);
    }

    public Client(String serverIPAddress, int serverPort, Controller controller) throws IOException
    {
        this.clientSocket = new Socket(serverIPAddress,serverPort);
        this.controller = controller;
        this.out = new DataOutputStream(clientSocket.getOutputStream());
        this.in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

        ClientReceiver clientReceiver = new ClientReceiver();
        Thread clientReceiverThread = new Thread(clientReceiver);
        clientReceiverThread.start();
    }

    public void sendDataToServer(String msg) throws IOException {
        if (out == null) return;

        out.writeUTF(msg);
        out.flush();
    }

    private class ClientReceiver implements Runnable {

        void handleSetScene(String msg)
        {
            String[] parts = msg.split(":");
            if(parts.length >= 2)
            {
                Platform.runLater(() -> {
                    controller.setupGameScene();
                });
            }
        }

        void handleActorUpdate(String msg)
        {
            String[] parts = msg.split(":");
            if (parts.length >= 5) {
                try {
                    final int actorId = Integer.parseInt(parts[1]);
                    final String actorType = parts[2];
                    final double x = Double.parseDouble(parts[3]);
                    final double y = Double.parseDouble(parts[4]);

                    Platform.runLater(() -> {
                        controller.updateActorState(actorId, actorType, x, y);
                    });
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        void handleAddActor(String msg)
        {
            String[] parts = msg.split(":");
            if (parts.length >= 5) {
                try {
                    final int actorId = Integer.parseInt(parts[1]);
                    final String actorType = parts[2];
                    final double x = Double.parseDouble(parts[3]);
                    final double y = Double.parseDouble(parts[4]);

                    Platform.runLater(() -> {
                        controller.addActorState(actorId, actorType, x, y);
                    });

                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        void handleRemoveActor(String msg) {
            String[] parts = msg.split(":");
            if (parts.length == 2) {
                try {
                    final int actorId = Integer.parseInt(parts[1]);

                    Platform.runLater(() -> {
                        controller.removeActor(actorId);
                    });

                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        void handlePlayerID(String msg)
        {
            String[] parts = msg.split(":");
            if (parts.length == 2) {
                try {
                    playerId = Integer.parseInt(parts[1]);
                    Platform.runLater(() -> {
                        controller.setLocalPlayerId(playerId);
                        System.out.println("CLIENT: received player ID: " + playerId);
                    });
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        void handleGameHasChanged(String msg)
        {
            if (msg.equals(ServerMessages.HAS_GAME_CHANGED)) {
                if (!pendingCommands.isEmpty())
                {
                    for (String command : new HashSet<>(pendingCommands))
                    {
                        try {
                            sendDataToServer(command);
                            pendingCommands.remove(command);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        /**
         * This function is called by other function and it is used to handle a message from request
         *
         * @param msg content of the message inside the request
         */
        private void handleSocketMessage(String msg) throws IOException {
            if (msg.startsWith(ServerMessages.SET_GAME_SCENE)) {
                handleSetScene(msg);
            } else if (msg.startsWith(ServerMessages.PLAYER_ID)) {
                handlePlayerID(msg);
            } else if (msg.startsWith(ServerMessages.ADD_ACTOR)) {
                handleAddActor(msg);
            } else if (msg.startsWith(ServerMessages.REMOVE_ACTOR)) {
                handleRemoveActor(msg);
            } else if (msg.startsWith(ServerMessages.UPDATE_ACTOR)) {
                handleActorUpdate(msg);
            } else if (msg.equals(ServerMessages.HAS_GAME_CHANGED)) {
                handleGameHasChanged(msg);
            }
        }

        @Override
        public void run() {
            try {
                while (!clientSocket.isClosed()) {
                    String msg = in.readUTF();
                    handleSocketMessage(msg);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try { if (in != null) in.close(); } catch (IOException e) {}
                try { if (out != null) out.close(); } catch (IOException e) {}
                try { if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close(); } catch (IOException e) {}
            }
        }
    }
}