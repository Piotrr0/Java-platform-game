package com.example.game.messages;

public class ServerMessages
{
    // Format: UPDATE_ACTOR:LevelName
    public static final String  SET_GAME_SCENE = "SET_GAME_SCENE:";

    public static final String UPDATE_ACTOR = "UPDATE_ACTOR:";

    // Format: PLAYER_ID:playerId
    public static final String PLAYER_ID = "PLAYER_ID:";

    public static final String HAS_GAME_CHANGED = "HAS_GAME_CHANGED";

    // Format: ADD_ACTOR:actorId:type:x:y:width:height[:prop1:value1...]
    public static final String ADD_ACTOR = "ADD_ACTOR:";

    // Format: REMOVE_ACTOR:actorId
    public static final String REMOVE_ACTOR = "REMOVE_ACTOR:";

    //Format: REFRESH_SCORE:newScore
    public static final String REFRESH_SCORE = "REFRESH_SCORE:";
}