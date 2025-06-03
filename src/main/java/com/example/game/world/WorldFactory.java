package com.example.game.world;

import com.example.game.actors.Actor;
import com.example.game.actors.ActorManager;
import com.example.game.actors.Enemy;
import com.example.game.actors.Prop;
import javafx.scene.paint.Color;

public class WorldFactory
{
    public static World createWorld(String worldName, String nextWorld) {
        World world = new World(worldName, nextWorld);

        switch (worldName) {
            case "Level1":
                setupLevel1(world);
                world.coinThreshold = 2;
                break;
            case "Level2":
                setupLevel2(world);
                world.coinThreshold = 5;
                break;
            case "Level3":
                setupLevel3(world);
                world.coinThreshold = 3;
        }

        return world;
    }
    private static void setupLevel1(World world) {
        ActorManager actorManager = world.getActorManager();

        Actor ground = new Actor(-1000, 500, 3000, 100);
        actorManager.addActor(ground);

        Actor coin3_1 = new Prop(200, 440, 50, 50, "Coin");
        actorManager.addActor(coin3_1);


        Enemy enemy1 = new Enemy(400, 530);
        enemy1.setMoveSpeed(1.0);
        actorManager.addActor(enemy1);

        Actor crate3_1 = new Prop(550, 450, 50, 50, "Crate");
        actorManager.addActor(crate3_1);
    }

    private static void setupLevel2(World world) {
        ActorManager actorManager = world.getActorManager();

        //base
        Actor obstacle1 = new Actor(-1000, 500, 3000, 75);
        actorManager.addActor(obstacle1);

        Actor coin1_3 = new Prop(20, 440, 50, 50,"Coin");
        actorManager.addActor(coin1_3);

        Actor coin1_4 = new Prop(630, 440, 50, 50,"Coin");
        actorManager.addActor(coin1_4);

        //first row of platforms
        Actor obstacle2 = new Actor(0, 380, 200, 20);
        actorManager.addActor(obstacle2);

        Actor obstacle3 = new Actor(500, 380, 200, 20);
        actorManager.addActor(obstacle3);

        //second row
        Actor obstacle4 = new Actor(220, 290, 260, 20);
        actorManager.addActor(obstacle4);

        //coin for testing purposes
        Actor coin1_1 = new Prop(220, 230, 50, 50,"Coin");
        actorManager.addActor(coin1_1);

        //third row
        Actor obstacle5 = new Actor(0, 200, 100, 20);
        actorManager.addActor(obstacle5);

        Actor coin1_2 = new Prop(10, 140, 50, 50, "Coin");
        actorManager.addActor(coin1_2);

        Actor obstacle6 = new Actor(600, 200, 100, 20);
        actorManager.addActor(obstacle6);

        Actor chest = new Prop(600, 150, 50, 50,"Crate");
        actorManager.addActor(chest);

        Enemy enemyLevel1 = new Enemy(300, 240);
        enemyLevel1.setMoveSpeed(1.2);
        actorManager.addActor(enemyLevel1);

        Enemy enemyLevel1_2 = new Enemy(350, 440);
        actorManager.addActor(enemyLevel1_2);
    }

    private static void setupLevel3(World world) {
        ActorManager actorManager = world.getActorManager();

        Actor ground = new Actor(-1000, 500, 3000, 100);
        actorManager.addActor(ground);

        Actor platform1 = new Actor(0, 400, 300, 20);
        actorManager.addActor(platform1);
        Actor platform2 = new Actor(500, 400, 300, 20);
        actorManager.addActor(platform2);

        Actor coin2_1 = new Prop(550, 330, 50, 50, "Coin");
        actorManager.addActor(coin2_1);
        Actor crate2_1 = new Prop(450, 170, 50, 50, "Crate");
        actorManager.addActor(crate2_1);
        Actor coin2_2 = new Prop(150, 170, 50,  50, "Coin");
        actorManager.addActor(coin2_2);


        Actor platform3 = new Actor(250, 300, 200, 20);
        actorManager.addActor(platform3);


        actorManager.addActor(new Actor(100, 220, 100, 15));
        actorManager.addActor(new Actor(400, 220, 100, 15));

        Enemy enemy2 = new Enemy(100, 530);
        enemy2.setMoveSpeed(1.0);
        actorManager.addActor(enemy2);
        Enemy enemy2_2 = new Enemy(275, 240);
        enemy2_2.setMoveSpeed(2.0);
        actorManager.addActor(enemy2_2);

    }


}