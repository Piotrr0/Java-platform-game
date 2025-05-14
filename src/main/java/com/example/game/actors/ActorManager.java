package com.example.game.actors;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ActorManager {
    private List<Actor> actors;
    public Map<Integer, Actor> actorsById;
    private Map<Integer, Player> players;
    private Pane gamePane;

    private boolean isServer = false;

    // Server-side constructor
    public ActorManager() {
        this.isServer = true;
        this.gamePane = null;
        this.actors = new ArrayList<>();
        this.actorsById = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
    }

    // Client-side constructor
    public ActorManager(Pane gamePane) {
        this.isServer = false;
        this.gamePane = gamePane;
        this.actors = new ArrayList<>();
        this.actorsById = new HashMap<>();
        this.players = new HashMap<>();
    }

    public void addActor(Actor actor) {
        if (!isServer) {
            return;
        }

        actors.add(actor);
        actorsById.put(actor.getId(), actor);
        if (actor instanceof Player) {
            players.put(((Player) actor).getPlayerId(), (Player) actor);
        }
    }

    public Actor getActor(int id) {
        return actorsById.get(id);
    }

    // Server-side method to remove actor
    public void removeActor(int actorId) {
        if (!isServer) {
            return;
        }

        Actor actor = actorsById.remove(actorId);
        if (actor != null) {
            actors.remove(actor);
            if (actor instanceof Player) {
                players.remove(((Player) actor).getPlayerId());
            }
        }
    }

    // Client-side method to remove actor (based on server instruction)
    public void removeActor(int actorId, Pane pane) {
        if (isServer) {
            return;
        }

        Actor actor = actorsById.remove(actorId);
        if (actor != null) {
            actors.remove(actor);
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
    public void updateServer()
    {
        if (!isServer) {
            return;
        }

        for (Actor actor : actors) {
            actor.updateServer(this);
        }
    }

    public List<Actor> getAllActorsServer() {
        if (!isServer) {
            return null;
        }
        return new ArrayList<>(actors);
    }

    public void updateClient()
    {
        if (isServer)
        {
            return;
        }

        for (Actor actor : actors)
        {
            actor.updateClient();
        }
    }

    // Server-side method to create a player
    public Player createPlayer(int playerId, double x, double y)
    {
        if (!isServer) {
            return null;
        }
        Player player = new Player(playerId, x, y);
        addActor(player);
        return player;
    }

    public Actor createOrUpdateActor(int id, String type, double x, double y, double width, double height, boolean isLocalPlayer, Pane pane)
    {
        if (isServer) {
            return null;
        }

        Actor actor = actorsById.get(id);
        if (actor == null)
        {
            Color color = null;
            if ("Player".equals(type)) {
                actor = new Player(id, x, y, isLocalPlayer);
                players.put(id, (Player)actor);
            } else {
                color = Color.GRAY;
                actor = new Actor(id, x, y, width, height, color);
            }

            actor.addToPane(pane);
            actors.add(actor);
            actorsById.put(id, actor);

        } else {
            actor.setPosition(x, y);
            actor.setScale(width, height);
        }
        return actor;
    }

    public List<Actor> getAllActorsClient()
    {
        if (isServer) {
            return null;
        }
        return new ArrayList<>(actors);
    }
}
