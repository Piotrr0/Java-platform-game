package com.example.game.world;

import com.example.game.actors.ActorManager;

import java.util.Map;

public class World {
    private final String worldName;
    private final ActorManager actorManager; // TODO: Consider making

    public World(String worldName, ActorManager actorManager)
    {
        this.worldName = worldName;
        this.actorManager = actorManager;
    }

    public String getWorldName() { return worldName; }
    public ActorManager getActorManager() { return actorManager; }

    public void update()
    {
        actorManager.updateServer();
    }
}