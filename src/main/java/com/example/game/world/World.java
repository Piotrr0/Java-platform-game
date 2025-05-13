package com.example.game.world;

import com.example.game.actors.ActorManager;

import java.util.Map;

public class World {
    private final String worldName;
    private final ActorManager actorManager;

    public World(String worldName)
    {
        this.worldName = worldName;
        this.actorManager = new ActorManager();
    }

    public String getWorldName() { return worldName; }
    public ActorManager getActorManager() { return actorManager; }

    public void update()
    {
        actorManager.updateServer();
    }
}