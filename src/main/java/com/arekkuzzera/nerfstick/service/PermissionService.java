package com.arekkuzzera.nerfstick.service;

import com.arekkuzzera.nerfstick.model.BlockProperty;
import org.bukkit.entity.Player;

public final class PermissionService {

    private static final String VANILLA_DEBUG_STICK_BYPASS = "minecraft.debugstick.always";
    private static final String USE_PERMISSION = "nerfstick.use";
    private static final String MANIPULATION_PREFIX = "nerfstick.manipulation.";

    public boolean canUse(Player player) {
        return player.hasPermission(VANILLA_DEBUG_STICK_BYPASS)
                || player.hasPermission(USE_PERMISSION);
    }

    public boolean canManipulate(Player player, BlockProperty property) {
        return player.hasPermission(VANILLA_DEBUG_STICK_BYPASS)
                || player.hasPermission(MANIPULATION_PREFIX + "*")
                || player.hasPermission(MANIPULATION_PREFIX + property.type().permissionKey());
    }
}
