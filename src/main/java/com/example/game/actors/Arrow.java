package com.example.game.actors;

import javafx.scene.paint.Color;

public class Arrow extends Actor{
    public Arrow(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.affectedByGravity = true;
        this.type="Arrow";
    }
}
