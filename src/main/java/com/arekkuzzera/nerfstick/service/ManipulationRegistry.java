package com.arekkuzzera.nerfstick.service;

import com.arekkuzzera.nerfstick.model.ManipulationType;

import java.util.Map;
import java.util.Set;

public final class ManipulationRegistry {

    private static final Set<String> MULTI_FACE_PROPERTIES = Set.of(
            "north", "east", "south", "west", "up", "down"
    );

    private static final Map<String, ManipulationType> TYPES_BY_PROPERTY = Map.ofEntries(
            Map.entry("facing", ManipulationType.DIRECTION),
            Map.entry("rotation", ManipulationType.DIRECTION),
            Map.entry("axis", ManipulationType.DIRECTION),
            Map.entry("orientation", ManipulationType.DIRECTION),
            Map.entry("vertical_direction", ManipulationType.DIRECTION),

            Map.entry("open", ManipulationType.OPENABLE),
            Map.entry("hinge", ManipulationType.OPENABLE),
            Map.entry("half", ManipulationType.OPENABLE),
            Map.entry("in_wall", ManipulationType.OPENABLE),

            Map.entry("powered", ManipulationType.POWER),
            Map.entry("lit", ManipulationType.POWER),
            Map.entry("enabled", ManipulationType.POWER),
            Map.entry("triggered", ManipulationType.POWER),
            Map.entry("conditional", ManipulationType.POWER),
            Map.entry("mode", ManipulationType.POWER),
            Map.entry("power", ManipulationType.POWER),

            Map.entry("waterlogged", ManipulationType.WATER),

            Map.entry("shape", ManipulationType.SHAPE),
            Map.entry("type", ManipulationType.SHAPE),
            Map.entry("thickness", ManipulationType.SHAPE),
            Map.entry("attachment", ManipulationType.SHAPE),
            Map.entry("face", ManipulationType.SHAPE),
            Map.entry("tilt", ManipulationType.SHAPE),
            Map.entry("sculk_sensor_phase", ManipulationType.SHAPE),

            Map.entry("age", ManipulationType.LEVEL),
            Map.entry("level", ManipulationType.LEVEL),
            Map.entry("delay", ManipulationType.LEVEL),
            Map.entry("distance", ManipulationType.LEVEL),
            Map.entry("bites", ManipulationType.LEVEL),
            Map.entry("candles", ManipulationType.LEVEL),
            Map.entry("eggs", ManipulationType.LEVEL),
            Map.entry("layers", ManipulationType.LEVEL),
            Map.entry("pickles", ManipulationType.LEVEL),
            Map.entry("charges", ManipulationType.LEVEL),
            Map.entry("note", ManipulationType.LEVEL),
            Map.entry("moisture", ManipulationType.LEVEL),
            Map.entry("honey_level", ManipulationType.LEVEL),
            Map.entry("hatch", ManipulationType.LEVEL),
            Map.entry("stage", ManipulationType.LEVEL)
    );

    public ManipulationType typeFor(String propertyName, Class<?> valueType) {
        if (MULTI_FACE_PROPERTIES.contains(propertyName)) {
            return ManipulationType.MULTI_FACE;
        }

        ManipulationType explicit = TYPES_BY_PROPERTY.get(propertyName);
        if (explicit != null) {
            return explicit;
        }

        if (Integer.class.equals(valueType) || int.class.equals(valueType)) {
            return ManipulationType.LEVEL;
        }

        return ManipulationType.MISC;
    }
}
