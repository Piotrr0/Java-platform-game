package com.example.game.world;

import com.example.game.actors.Actor;
import com.example.game.actors.ActorManager;
import javafx.scene.layout.Pane;

import java.util.Map;

public class World {
    private final String worldName;
    private static ActorManager actorManager;

    public World(String worldName)
    {
        this.worldName = worldName;
        actorManager = new ActorManager();
    }

    public String getWorldName() { return worldName; }
    public ActorManager getActorManager() { return actorManager; }
    public Actor getActorFromId(int id) {return actorManager.getActor(id);}

    // Server method for spawning actors
    public static void spawnActor(Actor actorToSpawn) {actorManager.addActor(actorToSpawn); }

    public void update()
    {
        actorManager.updateServer();
    }
}