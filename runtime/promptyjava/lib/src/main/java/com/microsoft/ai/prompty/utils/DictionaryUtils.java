package com.microsoft.ai.prompty.utils;

import com.fasterxml.jackson.databind.JsonNode;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DictionaryUtils {
    private static Map<String, Object> expand(Map<?, ?> dictionary) {
        Map<String, Object> dict = new HashMap<>();
        for (Map.Entry<?, ?> entry : dictionary.entrySet()) {
            if (entry.getValue() != null) {
                dict.put(entry.getKey().toString(), getValue(entry.getValue()));
            }
        }
        return dict;
    }

    private static Object getValue(Object o) {
        if (o == null) return null;
        
        if (o instanceof Map) {
            return expand((Map<?, ?>) o);
        } else if (o instanceof List) {
            List<?> list = (List<?>) o;
            return list.stream()
                .filter(Objects::nonNull)
                .map(DictionaryUtils::toParamMap)
                .collect(Collectors.toList());
        } else if (o.getClass().isPrimitive() || o instanceof String || o instanceof Number || o instanceof Boolean) {
            return o;
        } else {
            return toParamMap(o);
        }
    }

    public static Map<String, Object> toParamMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }

        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            return map;
        }

        Map<String, Object> dict = new HashMap<>();
        Arrays.stream(obj.getClass().getMethods())
            .filter(method -> method.getName().startsWith("get") && method.getParameterCount() == 0)
            .forEach(method -> {
                try {
                    String name = method.getName().substring(3);
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                    Object value = method.invoke(obj);
                    if (value != null) {
                        dict.put(name, getValue(value));
                    }
                } catch (Exception e) {
                    // Skip properties that can't be accessed
                }
            });

        return dict;
    }

    public static Map<String, Object> convertJsonNodeToMap(JsonNode node) {
        return JsonConverter.convertJsonNodeToMap(node);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(Map<String, Object> dict, String key, Class<T> type) {
        if (dict.containsKey(key) && type.isInstance(dict.get(key))) {
            return (T) dict.get(key);
        }
        return null;
    }

    public static <T> List<T> getList(Map<String, Object> dict, String key, Class<T> type) {
        if (dict.containsKey(key) && dict.get(key) instanceof List) {
            List<?> list = (List<?>) dict.get(key);
            return list.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static <S, T> List<T> getList(Map<String, Object> dict, String key, Function<S, T> transform) {
        if (dict.containsKey(key) && dict.get(key) instanceof List) {
            List<?> list = (List<?>) dict.get(key);
            return list.stream()
                .map(item -> transform.apply((S) item))
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static <T> List<T> getConfigList(Map<String, Object> dict, String key, 
            Function<Map<String, Object>, T> transform) {
        return getList(dict, key, transform);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getConfig(Map<String, Object> dict, String key) {
        Map<String, Object> sub = getValue(dict, key, Map.class);
        return sub != null && !sub.isEmpty() ? sub : null;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getAndRemoveConfig(Map<String, Object> dict, String key) {
        Map<String, Object> sub = getAndRemove(dict, key, Map.class);
        return sub != null && !sub.isEmpty() ? sub : null;
    }

    public static <T> T getConfig(Map<String, Object> dict, String key, 
            Function<Map<String, Object>, T> transform) {
        Map<String, Object> item = getConfig(dict, key);
        return item != null ? transform.apply(item) : null;
    }

    public static Map<String, Object> toConfig(Map<Object, Object> dict) {
        return dict.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().toString(),
                Map.Entry::getValue
            ));
    }

    public static <T> T getAndRemove(Map<String, Object> dict, String key, Class<T> type) {
        if (dict.containsKey(key) && type.isInstance(dict.get(key))) {
            T value = type.cast(dict.get(key));
            dict.remove(key);
            return value;
        }
        return null;
    }

    public static Map<String, Object> paramHoisting(Map<String, Object> top, 
            Map<String, Object> bottom, String key) {
        Map<String, Object> dict;
        if (key != null && !key.isEmpty()) {
            dict = top != null ? getConfig(top, key) : new HashMap<>();
            if (dict == null) dict = new HashMap<>();
        } else {
            dict = new HashMap<>(top != null ? top : new HashMap<>());
        }

        bottom.forEach((k, v) -> {
            if (!dict.containsKey(k)) {
                dict.put(k, v);
            }
        });

        return dict;
    }
}