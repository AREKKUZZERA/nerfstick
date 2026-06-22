package com.arekkuzzera.nerfstick.service;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class ProtectionService {

    public String denyReason(Player player, Location location, Material material) {
        PluginManager pluginManager = Bukkit.getServer().getPluginManager();

        String griefPreventionReason = checkGriefPrevention(pluginManager, player, location, material);
        if (griefPreventionReason != null) {
            return griefPreventionReason;
        }

        return checkWorldGuard(pluginManager, player, location);
    }

    private String checkGriefPrevention(PluginManager pluginManager, Player player, Location location, Material material) {
        Plugin plugin = pluginManager.getPlugin("GriefPrevention");
        if (plugin == null || !plugin.isEnabled()) {
            return null;
        }

        return GriefPrevention.instance.allowBuild(player, location, material);
    }

    private String checkWorldGuard(PluginManager pluginManager, Player player, Location location) {
        Plugin plugin = pluginManager.getPlugin("WorldGuard");
        if (plugin == null || !plugin.isEnabled()) {
            return null;
        }

        WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        if (platform.getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return null;
        }

        RegionContainer container = platform.getRegionContainer();
        RegionQuery query = container.createQuery();
        com.sk89q.worldedit.util.Location worldEditLocation = BukkitAdapter.adapt(location);

        return query.testBuild(worldEditLocation, localPlayer)
                ? null
                : "You do not have permission to build here!";
    }
}
