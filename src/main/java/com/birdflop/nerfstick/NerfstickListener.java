package com.birdflop.nerfstick;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NerfstickListener implements Listener {

    /**
     * Stores the currently-selected property per player+block-type, e.g.
     * key = "<playerUUID>:minecraft:oak_stairs" -> "facing".
     */
    private final Map<String, String> selectedProperty = new ConcurrentHashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.DEBUG_STICK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;

        // Vanilla only mutates block state on RIGHT_CLICK and only cycles the
        // selected property on LEFT_CLICK; cancel both so Bukkit/Paper never
        // applies its own default debug-stick logic on top of ours.
        event.setCancelled(true);

        Player player = event.getPlayer();

        String denyReason = Permission.getBlockProtection(player, block.getLocation(), block.getType());
        if (denyReason != null && !denyReason.isEmpty()) {
            player.sendActionBar(
                    Component.text("Interaction denied! Reason: ", TextColor.color(0xFFAA00))
                            .append(Component.text(denyReason, TextColor.color(0xFF5555)))
            );
            return;
        }

        BlockData data = block.getBlockData();
        String blockId = block.getType().getKey().toString();

        List<String> properties = BlockStateUtil.extractProperties(data)
                .stream()
                .filter(prop -> Permission.allowBlockState(player, blockId, prop))
                .toList();

        if (properties.isEmpty()) {
            player.sendActionBar(
                    Component.text("No editable properties available for this block!", TextColor.color(0xFFAA00))
            );
            return;
        }

        String key = player.getUniqueId() + ":" + blockId;
        String current = selectedProperty.getOrDefault(key, properties.get(0));

        if (!properties.contains(current)) {
            current = properties.get(0);
        }

        boolean inverse = player.isSneaking();

        if (action == Action.LEFT_CLICK_BLOCK) {

            String next = getRelative(properties, current, inverse);
            selectedProperty.put(key, next);

            player.sendActionBar(
                    Component.text("Selected: ", TextColor.color(0xFFAA00))
                            .append(Component.text(next, TextColor.color(0x55FF55)))
                            .append(Component.text(" (" + block.getType() + ")", TextColor.color(0xFFAA00)))
            );

        } else {

            BlockData updated = BlockStateUtil.cycleProperty(data, current, inverse);

            if (updated == null) {
                player.sendActionBar(
                        Component.text("Could not cycle property: ", TextColor.color(0xFFAA00))
                                .append(Component.text(current, TextColor.color(0xFF5555)))
                );
                return;
            }

            block.setBlockData(updated, true);

            String newValue = BlockStateUtil.getCurrentValue(updated, current);

            player.sendActionBar(
                    Component.text("Applied: ", TextColor.color(0xFFAA00))
                            .append(Component.text(current + "=" + newValue, TextColor.color(0x55FF55)))
                            .append(Component.text(" → ", TextColor.color(0xFFAA00)))
                            .append(Component.text(block.getType().toString(), TextColor.color(0x55FF55)))
            );
        }
    }

    // -----------------------------
    // LIST CYCLING (which property is selected)
    // -----------------------------

    private String getRelative(List<String> list, String current, boolean inverse) {

        if (list.isEmpty()) return current;

        int index = list.indexOf(current);

        if (index == -1) return list.get(0);

        if (inverse) {
            return (index > 0)
                    ? list.get(index - 1)
                    : list.get(list.size() - 1);
        } else {
            return (index < list.size() - 1)
                    ? list.get(index + 1)
                    : list.get(0);
        }
    }
}
