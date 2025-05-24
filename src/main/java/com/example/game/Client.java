package com.example.game;

import com.example.game.messages.ServerMessages;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.*;

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

    public Client(String serverIPAddress, int serverPort, Controller controller) throws IOException {
        this.clientSocket = new Socket(serverIPAddress, serverPort);
        this.controller = controller;
        this.out = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
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

        void handleSetScene(String msg) {
            String sceneName = msg.substring(ServerMessages.SET_GAME_SCENE.length());
            Platform.runLater(() -> controller.setupGameScene(sceneName));
        }

        void handleActorUpdate(String msg) {
            String dataPart = msg.substring(ServerMessages.UPDATE_ACTOR.length());
            String[] parts = dataPart.split(":", 3);
            if (parts.length == 3) {
                try {
                    final int actorId = Integer.parseInt(parts[0]);
                    final String actorType = parts[1];
                    final String serializedState = parts[2];

                    Platform.runLater(() -> controller.updateActorStateFromServer(actorId, actorType, serializedState));
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        void handleAddActor(String msg) {
            String dataPart = msg.substring(ServerMessages.ADD_ACTOR.length());
            String[] parts = dataPart.split(":", 3);
            if (parts.length == 3) {
                try {
                    final int actorId = Integer.parseInt(parts[0]);
                    final String actorType = parts[1];
                    final String serializedState = parts[2];
                    Platform.runLater(() -> controller.addActorFromServer(actorId, actorType, serializedState));
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        void handleRemoveActor(String msg) {
            String idStr = msg.substring(ServerMessages.REMOVE_ACTOR.length());
            try {
                final int actorId = Integer.parseInt(idStr);
                Platform.runLater(() -> controller.removeActor(actorId));
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }

        void handlePlayerID(String msg) {
            String idStr = msg.substring(ServerMessages.PLAYER_ID.length());
            try {
                final int receivedPlayerId = Integer.parseInt(idStr);
                playerId = receivedPlayerId;
                Platform.runLater(() -> {
                    controller.setLocalPlayerId(receivedPlayerId);
                    System.out.println("CLIENT: Received player ID: " + receivedPlayerId);
                });
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }

        void handleGameHasChanged(String msg) {
            if (!pendingCommands.isEmpty()) {
                Set<String> commandsToSend = new HashSet<>(pendingCommands);
                pendingCommands.removeAll(commandsToSend);

                for (String command : commandsToSend) {
                    try {
                        sendDataToServer(command);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        //We just decide to refresh a score
        private void handleRefreshScore(String msg) {
            int scoreNumber = Integer.parseInt(msg.substring(msg.indexOf(":") + 1));
            controller.refreshScoreText("Score: "+scoreNumber);
        }

        /**
         * This function is called by other function and it is used to handle a message from request
         *
         * @param msg content of the message inside the request
         */
        private void handleSocketMessage(String msg) {
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
            else if(msg.startsWith(ServerMessages.REFRESH_SCORE)){
                handleRefreshScore(msg);
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