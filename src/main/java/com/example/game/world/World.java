package com.example.game.world;

import com.example.game.actors.Actor;
import com.example.game.actors.ActorManager;
import javafx.scene.layout.Pane;

import java.util.Map;

public class World {
    private final String worldName;
    private final ActorManager actorManager;

    public World(String worldName)
    {
        this.worldName = worldName;
        this.actorManager = new ActorManager();
    }

    public World (String worldName, Pane gamePane)
    {
        this.worldName = worldName;
        this.actorManager = new ActorManager(gamePane);
    }

    public String getWorldName() { return worldName; }
    public ActorManager getActorManager() { return actorManager; }
    public Actor getActorFromId(int id) {return actorManager.getActor(id);}

    public void update()
    {
        actorManager.updateServer();
    }
}