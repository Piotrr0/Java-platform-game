package com.example.game.actors;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;

public class Actor
{
    //TODO: NOW ID IS MANUAL FOR STATIC OBJECT AND AUTOMATIC FOR PLAYER. MAKE SURE IT IS ALSO AUTMATIC FOR ACTORS
    protected int id = -1;
    protected String type = "Actor";

    protected double x;
    protected double y;
    protected double width;
    protected double height;

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
        }
    }

    // Client-side update method, primarily for graphical state based on current x/y
    public void update()
    {
        updateGraphicalRepresentation();
    }

    // Server-side method to set position (and client-side uses this to apply server state)
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Server-side collision check
    public boolean collidesWith(Actor other) {
        if (!this.collidable || !other.collidable) {
            return false;
        }

        return this.x < other.x + other.width &&
                this.x + this.width > other.x &&
                this.y < other.y + other.height &&
                this.y + this.height > other.y;
    }

    // Server-side
    public void handleCollision(Actor other) { System.out.println("Collision occurred: " + other.id); };

    public int getId() { return id; }
    public String getType() { return type; }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isCollidable() { return collidable; }

}
