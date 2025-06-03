package com.example.game.world;

import com.example.game.actors.Actor;
import com.example.game.actors.ActorManager;
import javafx.scene.layout.Pane;

import java.util.Map;

public class World {
    private final String worldName;
    private ActorManager actorManager;

    public int coinThreshold = 2;
    private int collectedCoins = 0;
    public String nextLevelName;
    public World(String worldName, String nextWorld)
    {
        this.worldName = worldName;
        actorManager = new ActorManager();
        nextLevelName = nextWorld;
    }

    public String getWorldName() { return worldName; }
    public ActorManager getActorManager() { return actorManager; }
    public Actor getActorFromId(int id) {return actorManager.getActor(id);}

    public int getCollectedCoins() {
        collectedCoins++;
        return collectedCoins; }
    public boolean checkCoinCount() {
        return collectedCoins >= coinThreshold;
    }

    public String getNextLevelName() { return nextLevelName; }

    public void update()
    {
        actorManager.updateServer();
    }
    void setCoinThreshold(int coinThreshold) { this.coinThreshold = coinThreshold; }
    void setNextLevelName(String nextLevelName) { this.nextLevelName = nextLevelName; }
}