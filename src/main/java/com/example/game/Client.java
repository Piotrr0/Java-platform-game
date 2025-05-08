package com.example.game;
import com.example.game.messages.ServerMessages;

import javafx.application.Platform;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

        void handleLoadMap(String msg)
        {
            Platform.runLater(() -> {
                try {
                    controller = controller.loadMap();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        void handlePositionUpdate(String msg)
        {
            String[] parts = msg.split(":");
            if (parts.length == 4) {
                try {
                    final int pid = Integer.parseInt(parts[1]);

                    String xStr = parts[2];
                    String yStr = parts[3];
                    final double x = Double.parseDouble(xStr);
                    final double y = Double.parseDouble(yStr);

                    Platform.runLater(() -> {
                        controller.updatePlayerPosition(pid, x, y);
                    });
                } catch (NumberFormatException e) {
                    System.err.println("Invalid position update format: " + msg);
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
                    });
                } catch (NumberFormatException e) {
                    System.err.println("Invalid player ID format: " + msg);
                }
            }
        }

        void handleGameHasChanged(String msg)
        {
            if (!pendingCommands.isEmpty())
            {
                for (String command : pendingCommands)
                {
                    try {
                        sendDataToServer(command);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                pendingCommands.clear();
            }
        }

        /**
         * This function is called by other function and it is used to handle a message from request
         *
         * @param msg content of the message inside the request
         */
        private void handleSocketMessage(String msg) throws IOException {
            if (Objects.equals(msg, ServerMessages.LOAD_MAP)) {
                handleLoadMap(msg);
            }

            if (msg.startsWith(ServerMessages.POSITION_UPDATE)) {
                handlePositionUpdate(msg);
            }

            if (msg.startsWith(ServerMessages.PLAYER_ID)) {
                handlePlayerID(msg);
            }

            if (Objects.equals(msg, ServerMessages.HAS_GAME_CHANGED)) {
                handleGameHasChanged(msg);
            }
        }

        @Override
        public void run() {
            System.out.println("Client has started working");
            while (!clientSocket.isClosed()) {
                try {
                    String msg = in.readUTF();
                    handleSocketMessage(msg);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}