package com.example.game.actors;

import com.example.game.network.Replicated;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.geometry.Rectangle2D;

import java.net.URISyntaxException;
import java.util.List;

public class Actor
{
    @Replicated
    protected int id = -1;

    @Replicated
    protected String type = "Actor";

    @Replicated
    protected double x;
    @Replicated
    protected double y;
    @Replicated
    protected double width;
    @Replicated
    protected double height;
    @Replicated
    //Flag that determines if actor should be removed with next tickrate
    protected boolean toBeDeleted = false;

    protected double velocityX = 0;

    @Replicated(Using = "onRep_velocityY")
    protected double velocityY = 0;
    protected void onRep_velocityY(double oldVelocityY) {}

    protected static final double GRAVITY = 0.5;
    protected boolean affectedByGravity = false;
    protected boolean collidable = true;

    protected Pane parentPane;
    protected Rectangle graphicalRepresentation;

    protected Color color;

    public boolean isAlive = true;

    // Constructor for server-side actors (no graphics needed initially)
    public Actor(double x, double y, double width, double height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Constructor for client-side actors, used when creating/updating based on server state
    public Actor(double x, double y, double width, double height, Color color)
    {
        this.color = color;
        initializeGraphics(color);
    }

    // Client-side methods for graphics
    protected void initializeGraphics(Color color) {
        Rectangle rectangle = new Rectangle(width, height, color);
        rectangle.setX(x);
        rectangle.setY(y);
        this.color = color;
        this.graphicalRepresentation = rectangle;
    }

    // Client-side methods for graphics, instead of color we generate a texture
    protected void initializeGraphics(String path) {
        Rectangle rectangle = new Rectangle(width, height);
        rectangle.setX(x);
        rectangle.setY(y);
        try {
            Image img = new Image(getClass().getResource("/assets/" + path).toURI().toString());
            rectangle.setFill(new ImagePattern(img));
        } catch (IllegalArgumentException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        this.graphicalRepresentation = rectangle;
    }

    public void addToPane(Pane pane) {
        this.parentPane = pane;
        pane.getChildren().add(graphicalRepresentation);
    }

    public void removeFromPane() {
        if (parentPane != null) {
            parentPane.getChildren().remove(graphicalRepresentation);
            parentPane = null;
        }
    }

    protected void updateGraphicalRepresentation() {
        if (graphicalRepresentation != null) {
            graphicalRepresentation.setX(x);
            graphicalRepresentation.setY(y);
            graphicalRepresentation.setWidth(width);
            graphicalRepresentation.setHeight(height);
            if (this.color != null && graphicalRepresentation.getFill() != this.color && !this.getType().equals("Crate") &&!this.getType().equals("Coin")) {
                graphicalRepresentation.setFill(this.color);
            }
        }
    }

    // Client-side update method, primarily for graphical state based on current x/y
    public void updateClient()
    {
        updateGraphicalRepresentation();
    }

    // Server-side method to set position (and client-side uses this to apply server state)
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setScale(double width, double height) {
        this.width = width;
        this.height = height;
    }

    // Server-side collision check
    public boolean collidesWith(Actor other) {
        if (!this.collidable || !other.collidable) {
            return false;
        }
        return getBounds().intersects(other.getBounds());
    }

    // Server-side
    public void handleCollision(Actor other) {
        //If one of the colliding objects was arrow, we want to flag the arrow to be deleted.
        if(this.getType()=="Arrow"){
            this.toBeDeleted = true;
            if(other.getType()=="Crate"){
                other.toBeDeleted = true;
            }
            else if(other.getType().equals("Enemy")){
                //Make enemy die
                other.isAlive  = false;
                other.toBeDeleted = true;
            }
        }

        else if(this.getType()=="Player"){
            if(other.getType()=="Coin"){
                other.toBeDeleted = true;
            }
            else if(other.getType().equals("Enemy")){
                System.out.println("Enemy should kill a client");
                toBeDeleted = true;
                isAlive = false;
            }
        }

    };

    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, width, height);
    }

    public void addVelocity(double vx, double vy) {
        this.velocityX += vx;
        this.velocityY += vy;
    }

    public void setVelocity(double vx, double vy) {
        this.velocityX = vx;
        this.velocityY = vy;
    }

    public void setVelocityX(double vx) {
        this.velocityX = vx;
    }

    public void setVelocityY(double vy) {
        this.velocityY = vy;
    }

    protected void applyGravity(){
        if (affectedByGravity) {
            velocityY += GRAVITY;
        }
    }

    protected void handleVerticalCollision(List<Actor> actorsToCheck)
    {
        double proposedY = y + velocityY;

        if (velocityY != 0) {
            Rectangle2D verticalProposedBounds = new Rectangle2D(x, proposedY, width, height);
            if (actorsToCheck != null) {
                for (Actor otherActor : actorsToCheck) {
                    if (otherActor.getId() == this.getId() || !otherActor.isCollidable()) {
                        continue;
                    }

                    if (verticalProposedBounds.intersects(otherActor.getBounds())) {
                        handleCollision(otherActor);
                        proposedY = computeVerticalCollisionY(otherActor.getBounds());
                        velocityY = 0;
                        break;
                    }
                }
            }
            this.y = proposedY;
        }
    }

    protected void handleHorizontalCollision(List<Actor> actorsToCheck)
    {
        double proposedX = x + velocityX;

        if (velocityX != 0) {
            Rectangle2D horizontalProposedBounds = new Rectangle2D(proposedX, y, width, height); // Use potentially adjusted y

            if (actorsToCheck != null) {
                for (Actor otherActor : actorsToCheck) {
                    if (otherActor.getId() == this.getId() || !otherActor.isCollidable()) {
                        continue;
                    }

                    if (horizontalProposedBounds.intersects(otherActor.getBounds())) {
                        handleCollision(otherActor);
                        proposedX = computeHorizontalCollisionX(otherActor.getBounds());
                        velocityX = 0;
                        break;
                    }
                }
            }
            this.x = proposedX;
        }
    }

    public void updateServer(ActorManager manager) {
        applyGravity();

        List<Actor> allActors = manager.getAllActorsServer();

        handleVerticalCollision(allActors);
        handleHorizontalCollision(allActors);
    }

    protected double computeVerticalCollisionY(Rectangle2D obstacle) {
        if (velocityY > 0) {
            // landing on top
            return obstacle.getMinY() - height;
        } else if (velocityY < 0) {
            // hitting head
            return obstacle.getMaxY();
        }
        return y;
    }

    protected double computeHorizontalCollisionX(Rectangle2D obstacle) {
        if (velocityX > 0) {
            // hitting rightward obstacle
            return obstacle.getMinX() - width;
        } else if (velocityX < 0) {
            // hitting leftward obstacle
            return obstacle.getMaxX();
        }
        return x;
    }

    public int getId() { return id; }
    public String getType() { return type; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isCollidable() { return collidable; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public boolean isToBeDeleted(){return toBeDeleted;}

    public void setColor(Color color) {this.color = color;}
    public Color getColor() { return this.color; }




}