package com.example.game.network;

import com.example.game.Server;
import com.example.game.actors.Actor;
import com.example.game.actors.ActorManager;
import com.example.game.messages.ServerMessages;
import javafx.scene.paint.Color;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RPCUtils {

    private static Server serverInstance;

    public static void initializeServer(Server server) {
        serverInstance = server;
    }

    public static void initializeClient() {}

    public static String serializeParameters(Object... args) {
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg instanceof Color) {
                        return "Color:" + ((Color) arg).toString();
                    } else if (arg instanceof String) {
                        return "String:" + arg;
                    } else if (arg instanceof Integer) {
                        return "Integer:" + arg;
                    } else if (arg instanceof Double) {
                        return "Double:" + arg;
                    } else if (arg instanceof Boolean) {
                        return "Boolean:" + arg;
                    }
                    // Add more types as needed
                    return "String:" + String.valueOf(arg);
                })
                .collect(Collectors.joining(";"));
    }

    public static Object[] deserializeParameters(String paramsString, Class<?>[] parameterTypes) {
        if (paramsString == null || paramsString.isEmpty()) {
            return new Object[0];
        }
        String[] paramStrings = paramsString.split(";");
        if (paramStrings.length != parameterTypes.length) {
            return null;
        }

        Object[] deserializedArgs = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            String paramPart = paramStrings[i];
            String[] typeAndValue = paramPart.split(":", 2);
            if (typeAndValue.length < 2) {
                if (parameterTypes[i] == String.class && typeAndValue[0].equals("String")) {
                    deserializedArgs[i] = "";
                    continue;
                }
                return null;
            }

            String type = typeAndValue[0];
            String value = typeAndValue[1];

            try {
                if (type.equals("Color") && parameterTypes[i] == Color.class) {
                    deserializedArgs[i] = Color.valueOf(value);
                } else if (type.equals("String") && parameterTypes[i] == String.class) {
                    deserializedArgs[i] = value;
                } else if (type.equals("Integer") && (parameterTypes[i] == int.class || parameterTypes[i] == Integer.class)) {
                    deserializedArgs[i] = Integer.parseInt(value);
                } else if (type.equals("Double") && (parameterTypes[i] == double.class || parameterTypes[i] == Double.class)) {
                    deserializedArgs[i] = Double.parseDouble(value);
                } else if (type.equals("Boolean") && (parameterTypes[i] == boolean.class || parameterTypes[i] == Boolean.class)) {
                    deserializedArgs[i] = Boolean.parseBoolean(value);
                } else {
                    throw new IllegalArgumentException();
                }
            } catch (Exception e) {
                return null;
            }
        }
        return deserializedArgs;
    }

    public static void callServerRPC(Actor actorContext, String methodName, Object... args) {
        String params = serializeParameters(args);
        String message = ServerMessages.RPC_CALL_PREFIX +
                actorContext.getId() + ":" +
                methodName + ":" +
                params;

        try {
            serverInstance.broadcastMessage(message);
        } catch (Exception e) {
        }
    }

    public static void sendRpcToClientOwner(int ownerPlayerId, int actorId, String methodName, Object... args) {
        if (serverInstance == null) {
            return;
        }
        String params = serializeParameters(args);
        String message = ServerMessages.RPC_CALL_PREFIX +
                actorId + ":" +
                methodName + ":" +
                params;
        serverInstance.sendMessageToPlayer(ownerPlayerId, message);
    }

    public static void sendRpcToAllClients(int actorId, String methodName, Object... args) {
        if (serverInstance == null) {
            return;
        }
        String params = serializeParameters(args);
        String message = ServerMessages.RPC_CALL_PREFIX +
                actorId + ":" +
                methodName + ":" +
                params;
        serverInstance.broadcastMessage(message);
    }


    public static void processIncomingRPC(String rpcMessage, ActorManager actorManager, boolean isServerContext) {
        String[] parts = rpcMessage.substring(ServerMessages.RPC_CALL_PREFIX.length()).split(":", 3);
        if (parts.length < 2) {
            return;
        }

        try {
            int actorId = Integer.parseInt(parts[0]);
            String methodName = parts[1];
            String paramsString = (parts.length > 2) ? parts[2] : "";

            Actor targetActor = actorManager.getActor(actorId);
            if (targetActor == null) {
                return;
            }

            Method methodToCall = findRpcMethod(targetActor.getClass(), methodName, isServerContext);

            if (methodToCall == null) {
                return;
            }

            Class<?>[] paramTypes = methodToCall.getParameterTypes();
            Object[] deserializedArgs = deserializeParameters(paramsString, paramTypes);

            if (deserializedArgs == null && paramTypes.length > 0) {
                return;
            }
            if (deserializedArgs != null && deserializedArgs.length != paramTypes.length) {
                return;
            }

            methodToCall.invoke(targetActor, deserializedArgs);

        } catch (NumberFormatException e) {
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private static Method findRpcMethod(Class<?> actorClass, String methodName, boolean isServerContext) {
        for (Method method : actorClass.getMethods()) {
            if (method.getName().equals(methodName) && method.isAnnotationPresent(RPC.class)) {
                RPC rpcAnnotation = method.getAnnotation(RPC.class);
                ExecutionTarget target = rpcAnnotation.target();

                if (isServerContext && target == ExecutionTarget.SERVER) {
                    return method;
                } else if (!isServerContext && (target == ExecutionTarget.CLIENT || target == ExecutionTarget.MULTICAST)) {
                    return method;
                }
            }
        }
        return null;
    }

    // Helper for Actors to call their own RPCs that target clients
    public static void invokeClientRpcOnOwner(Actor actor, String methodName, Object... args) {
        if (serverInstance == null || !actor.hasAuthority()) {
            return;
        }
        int ownerPlayerId = actor.getOwnerPlayerId();
        if (ownerPlayerId != -1) {
            sendRpcToClientOwner(ownerPlayerId, actor.getId(), methodName, args);
        }
    }

    // Helper for Actors to call their own RPCs that target all clients
    public static void invokeMulticastRpc(Actor actor, String methodName, Object... args) {
        if (serverInstance == null || !actor.hasAuthority()) {
            return;
        }
        sendRpcToAllClients(actor.getId(), methodName, args);
    }
}