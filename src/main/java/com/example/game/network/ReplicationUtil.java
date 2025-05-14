package com.example.game.network;

import com.example.game.actors.Actor;
import javafx.scene.paint.Color;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReplicationUtil {

    private static final String NULL_VALUE_MARKER = "%%NULL%%";

    public static Map<String, Object> getReplicatedState(Actor actor) {
        Map<String, Object> state = new HashMap<>();
        Class<?> clazz = actor.getClass();

        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Replicated.class)) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(actor);
                        state.put(field.getName(), value);
                    } catch (IllegalAccessException e) {
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return state;
    }

    public static void applyReplicatedState(Actor actor, Map<String, String> stateData) {
        Class<?> clazz = actor.getClass();

        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Replicated.class) && stateData.containsKey(field.getName())) {
                    try {
                        field.setAccessible(true);
                        Object oldValue = field.get(actor);
                        String valueStr = stateData.get(field.getName());
                        Object newValue = convertStringToFieldType(valueStr, field.getType(), field.getName());
                        field.set(actor, newValue);

                        Replicated replicatedAnnotation = field.getAnnotation(Replicated.class);
                        String onRepMethodName = replicatedAnnotation.Using();

                        if (onRepMethodName != null && !onRepMethodName.isEmpty()) {
                            try {
                                Method onRepMethod = null;
                                Class<?> paramType = field.getType();

                                try {
                                    onRepMethod = clazz.getDeclaredMethod(onRepMethodName, paramType);
                                } catch (NoSuchMethodException e1) {
                                    if (paramType.isPrimitive()) {
                                        Class<?> wrapperType = getWrapperType(paramType);
                                        if (wrapperType != null) {
                                            try {
                                                onRepMethod = clazz.getDeclaredMethod(onRepMethodName, wrapperType);
                                            } catch (NoSuchMethodException e3) {
                                            }
                                        }
                                    }
                                }

                                if (onRepMethod != null) {
                                    onRepMethod.setAccessible(true);
                                    onRepMethod.invoke(actor, oldValue);
                                    continue;
                                }

                                try {
                                    onRepMethod = clazz.getDeclaredMethod(onRepMethodName);
                                    onRepMethod.setAccessible(true);
                                    onRepMethod.invoke(actor);
                                    continue;
                                } catch (NoSuchMethodException e2) {
                                }

                            } catch (Exception e) {
                            }
                        }

                    } catch (IllegalAccessException | NumberFormatException e) {
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static Class<?> getWrapperType(Class<?> primitiveType) {
        if (primitiveType == int.class) return Integer.class;
        if (primitiveType == double.class) return Double.class;
        if (primitiveType == float.class) return Float.class;
        if (primitiveType == long.class) return Long.class;
        if (primitiveType == boolean.class) return Boolean.class;
        if (primitiveType == byte.class) return Byte.class;
        if (primitiveType == short.class) return Short.class;
        if (primitiveType == char.class) return Character.class;
        return null;
    }

    private static Object convertStringToFieldType(String valueStr, Class<?> fieldType, String fieldName) {
        if (valueStr == null || valueStr.equals(NULL_VALUE_MARKER)) {
            if (fieldType.isPrimitive()) {
                if (fieldType == double.class) return 0.0d;
                if (fieldType == int.class) return 0;
                if (fieldType == float.class) return 0.0f;
                if (fieldType == long.class) return 0L;
                if (fieldType == boolean.class) return false;
                if (fieldType == byte.class) return (byte) 0;
                if (fieldType == short.class) return (short) 0;
                if (fieldType == char.class) return '\u0000';
                return null;
            }
            return null;
        }

        try {
            if (fieldType == double.class || fieldType == Double.class) return Double.parseDouble(valueStr);
            if (fieldType == int.class || fieldType == Integer.class) return Integer.parseInt(valueStr);
            if (fieldType == float.class || fieldType == Float.class) return Float.parseFloat(valueStr);
            if (fieldType == long.class || fieldType == Long.class) return Long.parseLong(valueStr);
            if (fieldType == boolean.class || fieldType == Boolean.class) return Boolean.parseBoolean(valueStr);
            if (fieldType == String.class) return valueStr;
            if (fieldType == Color.class) return Color.valueOf(valueStr);
        } catch (IllegalArgumentException e) {
            if (fieldType == Color.class) return Color.WHITE;
            if (fieldType.isPrimitive()) {
                if (fieldType == double.class) return 0.0d;
                if (fieldType == int.class) return 0;
            }
            return null;
        }

        return null;
    }

    public static String serializeStateMap(Map<String, Object> stateMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : stateMap.entrySet()) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            String key = entry.getKey();
            String valueString;
            Object entryValue = entry.getValue();
            if (entryValue == null) {
                valueString = NULL_VALUE_MARKER;
            } else {
                if (entryValue instanceof Color) {
                    valueString = ((Color) entryValue).toString();
                } else {
                    valueString = entryValue.toString();
                }
            }
            if (!valueString.equals(NULL_VALUE_MARKER)) {
                valueString = valueString.replace(";", "%3B").replace("=", "%3D");
            }
            sb.append(key).append("=").append(valueString);
        }
        return sb.toString();
    }

    public static Map<String, String> deserializeStateMap(String stateString) {
        Map<String, String> stateMap = new HashMap<>();
        if (stateString == null || stateString.isEmpty()) {
            return stateMap;
        }

        String[] pairs = stateString.split(";", -1);
        for (String pair : pairs) {
            if (pair.isEmpty()) continue;
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                if (!value.equals(NULL_VALUE_MARKER)) {
                    value = value.replace("%3B", ";").replace("%3D", "=");
                }
                stateMap.put(key, value);
            }
        }
        return stateMap;
    }
}
