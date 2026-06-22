package com.arekkuzzera.nerfstick.service;

import com.arekkuzzera.nerfstick.model.BlockProperty;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockStateService {

    private static final Map<String, BlockFace> FACE_BY_PROPERTY = Map.of(
            "north", BlockFace.NORTH,
            "east", BlockFace.EAST,
            "south", BlockFace.SOUTH,
            "west", BlockFace.WEST,
            "up", BlockFace.UP,
            "down", BlockFace.DOWN
    );

    private final Map<String, PropertyAccessor> accessorCache = new ConcurrentHashMap<>();
    private final ManipulationRegistry manipulationRegistry;

    public BlockStateService(ManipulationRegistry manipulationRegistry) {
        this.manipulationRegistry = manipulationRegistry;
    }

    public List<BlockProperty> properties(BlockData data) {
        List<BlockProperty> properties = new ArrayList<>();

        for (String propertyName : propertyNames(data)) {
            Class<?> valueType = valueType(data, propertyName);
            properties.add(new BlockProperty(propertyName, manipulationRegistry.typeFor(propertyName, valueType)));
        }

        return properties;
    }

    public BlockData cycle(BlockData data, String property, boolean reverse) {
        BlockData clone = data.clone();

        BlockFace face = FACE_BY_PROPERTY.get(property);
        if (face != null && clone instanceof MultipleFacing multipleFacing) {
            if (!multipleFacing.getAllowedFaces().contains(face)) {
                return null;
            }

            multipleFacing.setFace(face, !multipleFacing.hasFace(face));
            return clone;
        }

        PropertyAccessor accessor = resolveAccessor(clone, property);
        if (accessor == null) {
            return null;
        }

        try {
            Object current = accessor.getter().invoke(clone);

            if (accessor.enumConstants() != null) {
                Object[] values = accessor.enumConstants();
                int index = indexOf(values, current);
                int next = Math.floorMod((index == -1 ? 0 : index) + (reverse ? -1 : 1), values.length);
                accessor.setter().invoke(clone, values[next]);
                return clone;
            }

            if (current instanceof Boolean bool) {
                accessor.setter().invoke(clone, !bool);
                return clone;
            }

            if (current instanceof Integer intValue) {
                int min = invokeIntBoundOrDefault(clone, "getMinimum" + capitalize(property), 0);
                int max = invokeIntBoundOrDefault(clone, "getMaximum" + capitalize(property), Integer.MAX_VALUE);
                int next = reverse ? intValue - 1 : intValue + 1;
                int span = max - min + 1;

                if (span > 0 && span < Integer.MAX_VALUE) {
                    next = min + Math.floorMod(next - min, span);
                } else {
                    next = Math.max(min, Math.min(max, next));
                }

                accessor.setter().invoke(clone, next);
                return clone;
            }
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            return null;
        }

        return null;
    }

    public String currentValue(BlockData data, String property) {
        String serialized = data.getAsString();
        int open = serialized.indexOf('[');
        int close = serialized.lastIndexOf(']');
        if (open == -1 || close == -1) {
            return null;
        }

        for (String entry : serialized.substring(open + 1, close).split(",")) {
            int equals = entry.indexOf('=');
            if (equals <= 0) {
                continue;
            }

            String key = entry.substring(0, equals).trim();
            if (key.equals(property)) {
                return entry.substring(equals + 1).trim();
            }
        }

        return null;
    }

    private List<String> propertyNames(BlockData data) {
        List<String> names = new ArrayList<>();
        String serialized = data.getAsString();
        int open = serialized.indexOf('[');
        int close = serialized.lastIndexOf(']');

        if (open == -1 || close == -1 || close < open) {
            return names;
        }

        String statePart = serialized.substring(open + 1, close);
        if (statePart.isEmpty()) {
            return names;
        }

        for (String entry : statePart.split(",")) {
            int equals = entry.indexOf('=');
            if (equals > 0) {
                names.add(entry.substring(0, equals).trim());
            }
        }

        return names;
    }

    private Class<?> valueType(BlockData data, String property) {
        BlockFace face = FACE_BY_PROPERTY.get(property);
        if (face != null && data instanceof MultipleFacing) {
            return Boolean.class;
        }

        PropertyAccessor accessor = resolveAccessor(data, property);
        return accessor == null ? String.class : accessor.getter().getReturnType();
    }

    private PropertyAccessor resolveAccessor(BlockData data, String property) {
        String cacheKey = data.getClass().getName() + "#" + property;
        PropertyAccessor cached = accessorCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String capitalized = capitalize(property);
        Class<?> clazz = data.getClass();
        PropertyAccessor accessor = tryGetterSetter(clazz, "get" + capitalized, "set" + capitalized);

        if (accessor == null) {
            accessor = tryGetterSetter(clazz, "is" + capitalized, "set" + capitalized);
        }

        if (accessor != null) {
            accessorCache.put(cacheKey, accessor);
        }

        return accessor;
    }

    private PropertyAccessor tryGetterSetter(Class<?> clazz, String getterName, String setterName) {
        Method getter = findNoArgMethod(clazz, getterName);
        if (getter == null) {
            return null;
        }

        Method setter = findSingleArgMethod(clazz, setterName, getter.getReturnType());
        if (setter == null) {
            return null;
        }

        Object[] enumConstants = getter.getReturnType().isEnum()
                ? getter.getReturnType().getEnumConstants()
                : null;
        return new PropertyAccessor(getter, setter, enumConstants);
    }

    private Method findNoArgMethod(Class<?> clazz, String name) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 0) {
                return method;
            }
        }

        return null;
    }

    private Method findSingleArgMethod(Class<?> clazz, String name, Class<?> paramType) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(name)
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].isAssignableFrom(paramType)) {
                return method;
            }
        }

        return null;
    }

    private int invokeIntBoundOrDefault(BlockData data, String methodName, int fallback) {
        try {
            Method method = data.getClass().getMethod(methodName);
            Object result = method.invoke(data);
            return result instanceof Integer value ? value : fallback;
        } catch (ReflectiveOperationException ignored) {
            return fallback;
        }
    }

    private int indexOf(Object[] array, Object value) {
        for (int index = 0; index < array.length; index++) {
            if (array[index].equals(value)) {
                return index;
            }
        }

        return -1;
    }

    private String capitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }

        StringBuilder builder = new StringBuilder(value.length());
        boolean upperNext = true;

        for (char character : value.toCharArray()) {
            if (character == '_') {
                upperNext = true;
                continue;
            }

            builder.append(upperNext ? Character.toUpperCase(character) : character);
            upperNext = false;
        }

        return builder.toString();
    }

    private record PropertyAccessor(Method getter, Method setter, Object[] enumConstants) {
    }
}
