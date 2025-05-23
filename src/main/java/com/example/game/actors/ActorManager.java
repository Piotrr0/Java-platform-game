package com.example.game.actors;

import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ActorManager {
    private final AtomicInteger nextId = new AtomicInteger(0);

    public Map<Integer, Actor> actorsById;
    private Map<Integer, Player> players;
    private Pane gamePane;
    private boolean isServer = false;

    // Server-side constructor
    public ActorManager() {
        this.isServer = true;
        this.gamePane = null;
        this.actorsById = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
    }

    // Client-side constructor
    public ActorManager(Pane gamePane) {
        this.isServer = false;
        this.gamePane = gamePane;
        this.actorsById = new HashMap<>();
        this.players = new HashMap<>();
    }

    // Server-side: Add actor
    private int generateId() {
        return nextId.getAndIncrement();
    }

    public void addActor(Actor actor) {
        if (!isServer) {
            return;
        }

        int id = generateId();
        actor.id = id;
        actorsById.put(id, actor);
        if (actor instanceof Player) {
            players.put(((Player) actor).getPlayerId(), (Player) actor);
        }
    }

    // Client-side: Add an actor
    public void addActorClientSide(Actor actor, Pane targetPane) {
        if (isServer) {
            return;
        }
        if (actorsById.containsKey(actor.getId())) {
            return;
        }

        actorsById.put(actor.getId(), actor);
        if (actor instanceof Player) {
            players.put(((Player) actor).getPlayerId(), (Player) actor);
        }
        actor.addToPane(targetPane);
    }

    public Actor getActor(int id) {
        return actorsById.get(id);
    }

    // Server-side method to remove actor
    public void removeActor(int actorId) {
        if (!isServer) return;

        Actor actor = actorsById.remove(actorId);
        if (actor != null) {
            if (actor instanceof Player) {
                players.remove(((Player) actor).getPlayerId());
            }
        }
    }

    // Client-side method to remove actor (based on server instruction)
    public void removeActorClientSide(int actorId) {
        if (isServer) return;

        Actor actor = actorsById.remove(actorId);
        if (actor != null) {
            if (actor instanceof Player) {
                players.remove(((Player) actor).getPlayerId());
            }
            actor.removeFromPane();
        }
    }

    public Player getPlayer(int playerId) {
        return players.get(playerId);
    }

    // Server-side update loop
    public void updateServer() {
        if (!isServer) return;
        for (Actor actor : actorsById.values()) {
            actor.updateServer(this);
        }
    }

    public List<Actor> getAllActorsServer() {
        if (!isServer) return new ArrayList<>();
        return new ArrayList<>(actorsById.values());
    }

    // Client-side update loop (actors update their graphics)
    public void updateClient() {
        if (isServer) return;
        for (Actor actor : actorsById.values()) {
            actor.updateClient();
        }
    }

    // Server-side method to create a player
    public Player createPlayer(int playerId, double x, double y) {
        if (!isServer) return null;
        Player player = new Player(playerId, x, y);
        addActor(player);
        return player;
    }

    public List<Actor> getAllActorsClient() {
        if (isServer) return new ArrayList<>();
        return new ArrayList<>(actorsById.values());
    }
}