package com.example.game;

import javafx.application.Platform;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * For now a client has a special thread that listents to incoming messages, if it receives one it is sent to a message that should handle it.
 * */

public class Client{
    private Socket clientSocket;

    //We should edit this when we want to change the map
    private Controller controller;
    private int playerId = -1;
    private Set<String> pendingCommands = Collections.synchronizedSet(new HashSet<>());

    public void addPendingCommand(String command) {
        pendingCommands.add(command);
    }

    public Client(String serverIPAddress, int serverPort, Controller controller) throws IOException
    {
        this.clientSocket = new Socket(serverIPAddress,serverPort);
        this.controller = controller;

        ClientReceiver clientReceiver = new ClientReceiver();
        Thread clientReceiverThread = new Thread(clientReceiver);
        clientReceiverThread.start();
    }

    public void sendDataToServer(String msg) throws IOException {
        DataOutputStream out = new DataOutputStream(this.clientSocket.getOutputStream());
        out.writeUTF(msg);
    }

    private class ClientReceiver implements Runnable {

        /**
         * This function is called by other function and it is used to handle a message from request
         *
         * @param msg content of the message inside the request
         */
        private void handleSocketMessage(String msg) throws IOException {
            if (Objects.equals(msg, "LOAD_MAP")) {
                Platform.runLater(() -> {
                    try {
                        controller = controller.loadMap();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                return;
            }

            if (msg.startsWith("POSITION_UPDATE:")) {
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
                        e.printStackTrace();
                    }
                }
                return;
            }

            if (Objects.equals(msg, "HAS_GAME_CHANGED"))
            {
                if (!pendingCommands.isEmpty())
                {
                    for (String command : pendingCommands)
                    {
                        sendDataToServer(command);
                    }
                    pendingCommands.clear();
                }
                return;
            }

            if (msg.startsWith("PLAYER_ID:")) {
                String[] parts = msg.split(":");
                if (parts.length == 2) {
                    try {
                        playerId = Integer.parseInt(parts[1]);
                        System.out.println("Assigned player ID: " + playerId);

                        Platform.runLater(() -> {
                            controller.setLocalPlayerId(playerId);
                        });
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid player ID format: " + msg);
                    }
                }
            }
        }

        @Override
        public void run() {
            System.out.println("Client has started working");
            while (!clientSocket.isClosed()) {
                try {
                    DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                    handleSocketMessage(in.readUTF());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}