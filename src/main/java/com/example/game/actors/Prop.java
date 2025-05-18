package com.example.game.actors;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.net.URISyntaxException;

public class Prop extends Actor{
    //This variable stores the name of prop e.g Chest or Ball or a Coin. It is useful when we want to decide what to do based on fact what it is.
    private String propType="";
    public Prop(int id, double x, double y, double width, double height,String propType) {
        super(id, x, y, width, height);
        this.type="Prop";
        this.propType = propType;
    }

    // Constructor for client-side actors, used when creating/updating based on server state
    public Prop(int id, double x, double y, double width, double height,Color color,String textureFileName) {
        this(id,x,y,width,height,"");
        this.color = color;
        initializeGraphics(textureFileName);
    }

    // Client-side methods for graphics

    protected void initializeGraphics(String textureFileName) {
        System.out.println("Prop powinien miec teksture: "+textureFileName);




        Rectangle rectangle = new Rectangle(width, height);
        rectangle.setX(x);
        rectangle.setY(y);
        try {
            Image img = new Image(getClass().getResource("/assets/"+textureFileName).toURI().toString());
            rectangle.setFill(new ImagePattern(img));
        } catch (IllegalArgumentException e)
        {
            System.out.println("Nie mozna zaladowac tekstury "+textureFileName);
        } catch (URISyntaxException e) {
            System.out.println("Nie mozna zaladowac tekstury "+textureFileName);
        }
        catch (NullPointerException e){
            System.out.println("Nie mozna zaladowac tekstury");
        }

        this.graphicalRepresentation = rectangle;
    }


    public String getPropType(){
        return this.propType;
    }


}
