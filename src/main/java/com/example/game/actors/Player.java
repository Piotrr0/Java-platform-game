package com.example.game.actors;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Player extends Actor {
    private int playerId;
    private boolean isLocalPlayer;
    private  double moveSpeed = 5.0;
    private final double jumpForce = -10.0;
    private List<Arrow> arrows = new ArrayList<Arrow>();

    // Server-side constructor
    public Player(int playerId, double x, double y,String type)
    {
        super(x, y, 50, 50);
        this.type = type;
        this.playerId = playerId;
        this.affectedByGravity = true;
    }

    // Client-side constructor
    public Player(int playerId, double x, double y, boolean firstPlayer)
    {
        //local player has to have his color declared or texture given, or it doesn't work
        super(x, y, 50, 50); //if no texture then color here (both don't work at once)
        this.type = "Player"; //I don't think this does anything - client doesn't care about type i think
        this.playerId = playerId;
        this.affectedByGravity = true;
        if(firstPlayer) { //mario
            initializeGraphics("mario.png");
        }
        else //luigi
        {
            initializeGraphics("luigi.png");
        }
    }

    public Integer getPlayerId() { return  playerId; }

    @Override
    public void handleCollision(Actor other) {
        super.handleCollision(other);

        if(getType().equals("Enemy")){
            if(other.getType().equals("Player")){
                System.out.println("Enemy should kill");
                other.isAlive = false;
                other.toBeDeleted=true;
            }
        }

        // For demonstration how it works, I do not find it it useful
        if (other instanceof Player)
        {
            if (this.x < other.getX()) {
                other.x += 1;
            }
            else {
                other.x -= 1;
            }
        }
    }

    // Server-side method to apply movement based on command
    public void move(String command)
    {
        switch (command) {
            case "MOVE_UP":
                if (Math.abs(velocityY) < 0.1) {
                    setVelocityY(jumpForce); // For testing do not work as intended
                }
                break;
            case "MOVE_DOWN":
                // Not needed right now
                break;
            case "MOVE_RIGHT":
                setVelocityX(moveSpeed);
                break;
            case "MOVE_LEFT":
                setVelocityX(-moveSpeed);
                break;
            case "STOP_LEFT", "STOP_RIGHT":
                setVelocityX(0);
                break;
            default:
                break;
        }
    }

    public Arrow Shoot()
    {
        Arrow arrow = new Arrow(x,y + 70,30,30);
        arrows.add(arrow);
        return arrow;
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(double moveSpeed){
        this.moveSpeed = moveSpeed;
    }

    public double getJumpForce() {
        return jumpForce;
    }
}