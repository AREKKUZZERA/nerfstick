package com.arekkuzzera.nerfstick;

import com.arekkuzzera.nerfstick.listener.DebugStickListener;
import com.arekkuzzera.nerfstick.service.BlockStateService;
import com.arekkuzzera.nerfstick.service.DebugStickService;
import com.arekkuzzera.nerfstick.service.ManipulationRegistry;
import com.arekkuzzera.nerfstick.service.PermissionService;
import com.arekkuzzera.nerfstick.service.ProtectionService;
import com.arekkuzzera.nerfstick.service.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

public final class Nerfstick extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ManipulationRegistry manipulationRegistry = new ManipulationRegistry();
        PermissionService permissionService = new PermissionService();
        ProtectionService protectionService = new ProtectionService();
        BlockStateService blockStateService = new BlockStateService(manipulationRegistry);
        DebugStickService debugStickService = new DebugStickService(
                permissionService,
                protectionService,
                blockStateService
        );

        getServer().getPluginManager().registerEvents(new DebugStickListener(debugStickService), this);

        if (getConfig().getBoolean("check-for-updates", true)) {
            UpdateChecker updateChecker = new UpdateChecker(this);
            getServer().getPluginManager().registerEvents(updateChecker, this);
            updateChecker.checkAsync();
        }

        getLogger().info("Nerfstick enabled (v" + getPluginMeta().getVersion() + ").");
    }

    @Override
    public void onDisable() {
        getLogger().info("Nerfstick disabled.");
    }
}
