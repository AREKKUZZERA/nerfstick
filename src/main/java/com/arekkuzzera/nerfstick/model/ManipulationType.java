package com.arekkuzzera.nerfstick.model;

import java.util.Locale;

public enum ManipulationType {
    DIRECTION("direction"),
    OPENABLE("openable"),
    POWER("power"),
    WATER("water"),
    SHAPE("shape"),
    LEVEL("level"),
    MULTI_FACE("multi_face"),
    MISC("misc");

    private final String permissionKey;

    ManipulationType(String permissionKey) {
        this.permissionKey = permissionKey;
    }

    public String permissionKey() {
        return permissionKey;
    }

    public String displayName() {
        return permissionKey.replace('_', ' ').toLowerCase(Locale.ROOT);
    }
}
