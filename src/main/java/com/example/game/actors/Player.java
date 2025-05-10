package com.example.game.actors;

import javafx.scene.paint.Color;

public class Player extends Actor {
    private int playerId;
    private boolean isLocalPlayer;
    private final double moveAmount = 10.0;

    // Server-side constructor
    public Player(int playerId, double x, double y)
    {
        super(playerId, x, y, 50, 50); // Use server-side Actor constructor
        this.type = "Player";
        this.playerId = playerId;
    }

    // Client-side constructor
    public Player(int playerId, double x, double y, boolean isLocalPlayer)
    {
        super(playerId, x, y, 50, 50, isLocalPlayer ? Color.GREEN : Color.RED);
        this.type = "Player";
        this.playerId = playerId;
    }

    public Integer getPlayerId() { return  playerId; }
    public boolean isLocalPlayer(int localPlayerId) {
        return this.playerId == localPlayerId;
    }

    // Server-side method to apply movement based on command
    public void move(String command) {
        switch (command) {
            case "MOVE_UP":
                this.y -= moveAmount;
                break;
            case "MOVE_DOWN":
                this.y += moveAmount;
                break;
            case "MOVE_RIGHT":
                this.x += moveAmount;
                break;
            case "MOVE_LEFT":
                this.x -= moveAmount;
                break;
            default:
                break;
        }
    }
}