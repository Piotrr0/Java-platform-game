package com.example.game.world;

import com.example.game.actors.Player;

import java.util.HashMap;
import java.util.Map;

// TODO: Check if spawn world works! and make swap world possible
public class WorldManager
{
    private final Map<String, World> worlds;
    private World activeWorld;

    public WorldManager() {
        this.worlds = new HashMap<>();
    }

    public void registerWorld(World world) {
        worlds.put(world.getWorldName(), world);
    }

    public boolean setActiveWorld(String worldName) {
        World world = worlds.get(worldName);
        if (world != null) {
            activeWorld = world;
            return true;
        }
        return false;
    }

    public void update() {
        if (activeWorld != null) {
            activeWorld.update();
        }
    }

    public void initializeDefaultWorlds()
    {
        World level1 = WorldFactory.createWorld("Level1");
        registerWorld(level1);

        World level2 = WorldFactory.createWorld("Level2");
        registerWorld(level2);
    }

    public Player createPlayerInActiveWorld(int playerId, double x, double y) {
        if (activeWorld != null) {
            return activeWorld.getActorManager().createPlayer(playerId, x, y);
        }
        return null;
    }

    public World getActiveWorld() { return activeWorld; }
}
