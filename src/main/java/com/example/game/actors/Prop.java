package com.example.game.actors;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.net.URISyntaxException;

public class Prop extends Actor {

    //Server-side prop initialization
    public Prop(double x, double y, double width, double height, String propType) {
        super(x, y, width, height);
        this.type = propType;
    }

    // Constructor for client-side actors, used when creating/updating based on server state
    public  Prop(double x, double y, double width, double height, Color color, String textureFileName,String proptype) {
        this(x, y, width, height, proptype);
        this.color = color;
        initializeGraphics(textureFileName);
    }

    // Client-side methods for graphics
    protected void initializeGraphics(String textureFileName) {
        System.out.println("Prop powinien miec teksture: " + textureFileName);

        Rectangle rectangle = new Rectangle(width, height);
        rectangle.setX(x);
        rectangle.setY(y);
        try {
            Image img = new Image(getClass().getResource("/assets/" + textureFileName).toURI().toString());
            rectangle.setFill(new ImagePattern(img));
        } catch (IllegalArgumentException e) {
            System.out.println("Nie mozna zaladowac tekstury " + textureFileName);
        } catch (URISyntaxException e) {
            System.out.println("Nie mozna zaladowac tekstury " + textureFileName);
        } catch (NullPointerException e) {
            System.out.println("Nie mozna zaladowac tekstury");
        }

        this.graphicalRepresentation = rectangle;
    }
}
