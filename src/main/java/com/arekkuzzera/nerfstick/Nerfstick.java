package com.arekkuzzera.nerfstick;

import com.birdflop.nerfstick.NerfstickListener;
import com.birdflop.nerfstick.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

public final class Nerfstick extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new NerfstickListener(), this);

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
