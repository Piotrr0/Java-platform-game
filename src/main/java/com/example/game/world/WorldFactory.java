package com.example.game.world;

import com.example.game.actors.Actor;
import com.example.game.actors.ActorManager;

public class WorldFactory
{
    public static World createWorld(String worldName) {
        World world = new World(worldName);

        switch (worldName) {
            case "Level1":
                setupLevel1(world);
                break;
            case "Level2":
                setupLevel2(world);
                break;
        }

        return world;
    }

    private static void setupLevel1(World world) {
        ActorManager actorManager = world.getActorManager();

        Actor obstacle1 = new Actor(1000, 200, 200, 50, 50);
        actorManager.addActor(obstacle1);
    }

    private static void setupLevel2(World world) {
        ActorManager actorManager = world.getActorManager();

        Actor obstacle1 = new Actor(1001, 150, 150, 50, 50);
        actorManager.addActor(obstacle1);

        Actor obstacle2 = new Actor(1002, 300, 300, 125, 75);
        actorManager.addActor(obstacle2);
    }
}
