package com.arekkuzzera.nerfstick.listener;

import com.arekkuzzera.nerfstick.service.DebugStickService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class DebugStickListener implements Listener {

    private final DebugStickService debugStickService;

    public DebugStickListener(DebugStickService debugStickService) {
        this.debugStickService = debugStickService;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.DEBUG_STICK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);
        debugStickService.handle(event.getPlayer(), block, action);
    }
}
