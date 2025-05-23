package com.example.game.actors;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class Player extends Actor {
    private int playerId;
    private boolean isLocalPlayer;
    private final double moveSpeed = 5.0;
    private final double jumpForce = -10.0;
    private List<Arrow> arrows = new ArrayList<Arrow>();

    // For demonstration purposes
    protected void onRep_velocityY(double oldVelocityY)
    {
        //System.out.println("old:" + oldVelocityY + "new:" + this.velocityY);
        if(this.velocityY > 0)
        {
            this.color = Color.MAGENTA;
        }
        else
        {
            this.color = Color.GREEN;
        }
    }

    // Server-side constructor
    public Player(int playerId, double x, double y)
    {
        super(x, y, 50, 50);
        this.type = "Player";
        this.playerId = playerId;
        this.affectedByGravity = true;
    }

    // Client-side constructor
    public Player(int playerId, double x, double y, boolean isLocalPlayer)
    {
        super(x, y, 50, 50, isLocalPlayer ? Color.GREEN : Color.RED);
        this.type = "Player";
        this.playerId = playerId;
        this.affectedByGravity = true;
    }

    public Integer getPlayerId() { return  playerId; }

    @Override
    public void handleCollision(Actor other) {
        super.handleCollision(other);

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

    public double getJumpForce() {
        return jumpForce;
    }
}