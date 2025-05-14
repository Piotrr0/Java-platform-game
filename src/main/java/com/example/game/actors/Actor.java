package com.example.game.actors;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.geometry.Rectangle2D;

import java.util.List;

public class Actor
{
    //TODO: NOW ID IS MANUAL FOR STATIC OBJECT AND AUTOMATIC FOR PLAYER. MAKE SURE IT IS ALSO AUTMATIC FOR ACTORS
    protected int id = -1;
    protected String type = "Actor";

    protected double x;
    protected double y;
    protected double width;
    protected double height;

    protected double dx = 0;
    protected double dy = 0;

    protected Pane parentPane;
    protected Rectangle graphicalRepresentation;
    protected Color color;

    protected boolean collidable = true;

    // Constructor for server-side actors (no graphics needed initially)
    public Actor(int id, double x, double y, double width, double height)
    {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Constructor for client-side actors, used when creating/updating based on server state
    public Actor(int id, double x, double y, double width, double height, Color color) // Client-side constructor
    {
        this(id, x, y, width, height);
        this.color = color;
        initializeGraphics(color);
    }

    // Client-side methods for graphics
    protected void initializeGraphics(Color color) {
        Rectangle rectangle = new Rectangle(width, height, color);
        rectangle.setX(x);
        rectangle.setY(y);
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
    public void handleCollision(Actor other) { System.out.println("Collision occurred: " + other.id); };

    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, width, height);
    }

    public Rectangle2D getProposedBounds() {
        return new Rectangle2D(x + dx, y + dy, width, height);
    }

    public void setMovement(double deltaX, double deltaY) {
        this.dx = deltaX;
        this.dy = deltaY;
    }

    public void updateServer(ActorManager manager) {
        if (dx == 0 && dy == 0) {
            return;
        }

        double proposedX = x + dx;
        double proposedY = y + dy;
        Rectangle2D proposedBounds = new Rectangle2D(proposedX, proposedY, width, height);

        boolean collisionDetected = false;
        List<Actor> allActors = manager.getAllActorsServer();

        if (allActors != null) {
            for (Actor otherActor : allActors) {
                if (otherActor.getId() == this.getId() || !otherActor.isCollidable()) {
                    continue;
                }

                if (proposedBounds.intersects(otherActor.getBounds())) {
                    collisionDetected = true;
                    handleCollision(otherActor);
                    break;
                }
            }
        }

        if (!collisionDetected) {
            this.x = proposedX;
            this.y = proposedY;
        }

        dx = 0;
        dy = 0;
    }

    public int getId() { return id; }
    public String getType() { return type; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isCollidable() { return collidable; }
    public double getDx() { return dx; }
    public double getDy() { return dy; }
}