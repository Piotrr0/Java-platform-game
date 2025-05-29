package com.example.game.actors;

public class Enemy extends Actor {
    private double moveSpeed = 1.0;

    // Server-side constructor
    public Enemy(double x, double y) {
        super(x, y, 50, 50);
        this.type = "Enemy";
        this.affectedByGravity = true;
        this.collidable = true;
    }

    // Client-side constructor with specific texture (optional, if needed)
    public Enemy(double x, double y, String textureFileName) {
        super(x, y, 50, 50);
        this.type = "Enemy";
        this.affectedByGravity = true;
        this.collidable = true;
        if (textureFileName != null && !textureFileName.isEmpty()) {
            initializeGraphics(textureFileName);
        } else {
            initializeGraphics("tutel.png");
        }
    }


    @Override
    public void handleCollision(Actor other) {
        super.handleCollision(other);

        if (other.getType().equals("Player")) {
            other.isAlive = false;
            other.toBeDeleted = true;
        } else if (other.getType().equals("Arrow")) {
            this.isAlive = false;
            this.toBeDeleted = true;
        }
    }

    public void move(String direction) {
        switch (direction) {
            case "MOVE_LEFT":
                setVelocityX(-moveSpeed);
                break;
            case "MOVE_RIGHT":
                setVelocityX(moveSpeed);
                break;
            case "STOP":
                setVelocityX(0);
                break;
            default:
                break;
        }
    }

    public void startAI()
    {
        final int defaultTimeToGoLeft = 1500;
        final int defaultTimeToGoRight = 1500;

        new Thread(() -> {
            try {
                while (this.isAlive) {
                    this.move("MOVE_RIGHT");
                    Thread.sleep(defaultTimeToGoRight);

                    if (!this.isAlive) break;

                    this.move("MOVE_LEFT");
                    Thread.sleep(defaultTimeToGoLeft);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(double moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    @Override
    public void updateServer(ActorManager manager) {
        super.updateServer(manager);
    }
}