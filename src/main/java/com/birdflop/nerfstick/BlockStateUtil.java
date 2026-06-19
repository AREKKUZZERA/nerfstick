package com.birdflop.nerfstick;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads and cycles vanilla block-state properties (facing, axis, half, shape,
 * instrument, age, level, ...) using the typed Bukkit {@link BlockData} interfaces
 * (Directional, Orientable, Bisected, Ageable, Levelled, Powerable, ...) via reflection.
 * <p>
 * This avoids NMS entirely: every vanilla block-state property exposed in the
 * string form returned by {@link BlockData#getAsString()} (e.g. {@code facing=north})
 * has a matching getter/setter pair on one of the BlockData sub-interfaces
 * (e.g. {@code getFacing()} / {@code setFacing(BlockFace)}), and enum-typed getters
 * report every legal value via {@code getDeclaringClass().getEnumConstants()}.
 */
public final class BlockStateUtil {

    private BlockStateUtil() {
    }

    /**
     * Cache of (BlockData impl class + property name) -> resolved accessor, so we only
     * pay the reflection-scan cost once per (block type, property) combination instead
     * of on every single interaction.
     */
    private static final Map<String, PropertyAccessor> ACCESSOR_CACHE = new ConcurrentHashMap<>();

    /** Maps the textual blockstate property name to the BlockFace used by MultipleFacing. */
    private static final Map<String, BlockFace> FACE_BY_PROPERTY = Map.of(
            "north", BlockFace.NORTH,
            "east", BlockFace.EAST,
            "south", BlockFace.SOUTH,
            "west", BlockFace.WEST,
            "up", BlockFace.UP,
            "down", BlockFace.DOWN
    );

    private static final class PropertyAccessor {
        final Method getter;
        final Method setter;
        /** Enum constants in declaration order, or null for non-enum (boolean/int) properties. */
        final Object[] enumConstants;

        PropertyAccessor(Method getter, Method setter, Object[] enumConstants) {
            this.getter = getter;
            this.setter = setter;
            this.enumConstants = enumConstants;
        }
    }

    /**
     * Extracts the ordered list of mutable property names from a block's state string,
     * e.g. {@code minecraft:oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]}
     * -> [facing, half, shape, waterlogged].
     */
    public static List<String> extractProperties(BlockData data) {
        List<String> props = new ArrayList<>();
        String serialized = data.getAsString();

        int open = serialized.indexOf('[');
        if (open == -1) return props;
        int close = serialized.lastIndexOf(']');
        if (close == -1 || close < open) return props;

        String statePart = serialized.substring(open + 1, close);
        if (statePart.isEmpty()) return props;

        for (String entry : statePart.split(",")) {
            int eq = entry.indexOf('=');
            if (eq <= 0) continue;
            props.add(entry.substring(0, eq).trim());
        }

        return props;
    }

    /**
     * Returns the current value of a property as a string, in the same textual form
     * used by {@link BlockData#getAsString()} (lowercase enum names, "true"/"false", or
     * an integer).
     */
    public static String getCurrentValue(BlockData data, String property) {
        String serialized = data.getAsString();
        int open = serialized.indexOf('[');
        int close = serialized.lastIndexOf(']');
        if (open == -1 || close == -1) return null;

        for (String entry : serialized.substring(open + 1, close).split(",")) {
            int eq = entry.indexOf('=');
            if (eq <= 0) continue;
            String key = entry.substring(0, eq).trim();
            if (key.equals(property)) {
                return entry.substring(eq + 1).trim();
            }
        }
        return null;
    }

    /**
     * Cycles {@code property} on a cloned copy of {@code data} to the next (or, if
     * {@code inverse}, previous) legal value and returns the new BlockData, or
     * {@code null} if the property could not be resolved/cycled.
     */
    public static BlockData cycleProperty(BlockData data, String property, boolean inverse) {
        BlockData clone = data.clone();

        // Special case: fences, glass panes, glow lichen, etc. expose the
        // north/east/south/west/up/down faces as boolean flags through
        // MultipleFacing#hasFace/setFace rather than a getX()/setX() pair,
        // so they need their own toggle path.
        BlockFace face = FACE_BY_PROPERTY.get(property);
        if (face != null && clone instanceof MultipleFacing multipleFacing) {
            if (!multipleFacing.getAllowedFaces().contains(face)) return null;
            multipleFacing.setFace(face, !multipleFacing.hasFace(face));
            return clone;
        }

        PropertyAccessor accessor = resolveAccessor(clone, property);
        if (accessor == null) return null;

        try {
            Object current = accessor.getter.invoke(clone);

            if (accessor.enumConstants != null) {
                // Enum-valued property (facing, axis, half, shape, instrument, hinge, ...)
                int index = indexOf(accessor.enumConstants, current);
                if (index == -1) index = 0;
                int size = accessor.enumConstants.length;
                int next = inverse ? Math.floorMod(index - 1, size) : Math.floorMod(index + 1, size);
                accessor.setter.invoke(clone, accessor.enumConstants[next]);
                return clone;
            }

            if (current instanceof Boolean bool) {
                accessor.setter.invoke(clone, !bool);
                return clone;
            }

            if (current instanceof Integer intVal) {
                int min = invokeIntBoundOrDefault(clone, "getMinimum" + capitalize(property), 0);
                int max = invokeIntBoundOrDefault(clone, "getMaximum" + capitalize(property), Integer.MAX_VALUE);
                int span = max - min + 1;
                int next = inverse ? intVal - 1 : intVal + 1;
                if (span > 0 && span < Integer.MAX_VALUE) {
                    // wrap within the property's legal numeric range
                    next = min + Math.floorMod(next - min, span);
                } else {
                    next = Math.max(min, Math.min(max, next));
                }
                accessor.setter.invoke(clone, next);
                return clone;
            }
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            return null;
        }

        return null;
    }

    private static int indexOf(Object[] array, Object value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) return i;
        }
        return -1;
    }

    private static int invokeIntBoundOrDefault(BlockData data, String methodName, int fallback) {
        try {
            Method m = data.getClass().getMethod(methodName);
            Object result = m.invoke(data);
            if (result instanceof Integer i) return i;
        } catch (ReflectiveOperationException ignored) {
            // no explicit bound exposed by this interface; caller falls back to a sane default
        }
        return fallback;
    }

    /**
     * Resolves the getter/setter pair (and, for enums, the legal value set) for a
     * given vanilla block-state property name by matching it against the public
     * Bukkit BlockData interfaces implemented by {@code data} (Directional, Orientable,
     * Bisected, Ageable, Levelled, Powerable, Openable, Waterlogged, Rotatable, ...).
     */
    private static PropertyAccessor resolveAccessor(BlockData data, String property) {
        String cacheKey = data.getClass().getName() + "#" + property;
        PropertyAccessor cached = ACCESSOR_CACHE.get(cacheKey);
        if (cached != null) return cached;

        String capitalized = capitalize(property);
        Class<?> clazz = data.getClass();

        // 1) Standard "getX()/setX(...)" pair, the common case for almost every
        //    vanilla property (facing, axis, half, shape, hinge, instrument, face,
        //    attachment, orientation, mode, thickness, tilt, sculk sensor phase, ...).
        PropertyAccessor accessor = tryGetterSetter(clazz, "get" + capitalized, "set" + capitalized);

        // 2) Boolean properties are commonly exposed as "isX()/setX(boolean)"
        //    (open, waterlogged, lit, powered, persistent, snowy, triggered, ...).
        if (accessor == null) {
            accessor = tryGetterSetter(clazz, "is" + capitalized, "set" + capitalized);
        }

        if (accessor != null) {
            ACCESSOR_CACHE.put(cacheKey, accessor);
        }

        return accessor;
    }

    private static PropertyAccessor tryGetterSetter(Class<?> clazz, String getterName, String setterName) {
        Method getter = findNoArgMethod(clazz, getterName);
        if (getter == null) return null;

        Class<?> returnType = getter.getReturnType();
        Method setter = findSingleArgMethod(clazz, setterName, returnType);
        if (setter == null) return null;

        Object[] enumConstants = returnType.isEnum() ? returnType.getEnumConstants() : null;
        return new PropertyAccessor(getter, setter, enumConstants);
    }

    private static Method findNoArgMethod(Class<?> clazz, String name) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 0) {
                return m;
            }
        }
        return null;
    }

    private static Method findSingleArgMethod(Class<?> clazz, String name, Class<?> paramType) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 1
                    && m.getParameterTypes()[0].isAssignableFrom(paramType)) {
                return m;
            }
        }
        return null;
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperNext = true;
        for (char c : s.toCharArray()) {
            if (c == '_') {
                upperNext = true;
                continue;
            }
            sb.append(upperNext ? Character.toUpperCase(c) : c);
            upperNext = false;
        }
        return sb.toString();
    }

    /** Simple cache reset hook, exposed for tests/reloads. */
    public static void clearCache() {
        ACCESSOR_CACHE.clear();
    }
}
