package com.arekkuzzera.nerfstick;

import org.bukkit.plugin.java.JavaPlugin;

import com.birdflop.nerfstick.NerfstickListener;

public final class Nerfstick extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new NerfstickListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
