package com.example.game.actors;

public class Prop extends Actor{
    //This variable stores the name of prop e.g Chest or Ball or a Coin. It is useful when we want to decide what to do based on fact what it is.
    private String propType="";
    public Prop(int id, double x, double y, double width, double height,String propType) {
        super(id, x, y, width, height);
        this.type="Prop";
        this.propType = propType;
    }

    public String getPropType(){
        return this.propType;
    }


}
