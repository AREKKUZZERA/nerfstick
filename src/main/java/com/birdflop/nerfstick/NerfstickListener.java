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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NerfstickListener implements Listener {

    /**
     * Храним выбранный "property" для игрока отдельно.
     * key = playerUUID + blockType
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
        List<String> properties = extractProperties(data);

        if (properties.isEmpty()) {
            player.sendActionBar(
                    Component.text("Block has no editable properties!", TextColor.color(0xFFAA00))
            );
            return;
        }

        String key = player.getUniqueId() + ":" + block.getType();
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

            applyProperty(block, data, current, inverse);

            player.sendActionBar(
                    Component.text("Applied: ", TextColor.color(0xFFAA00))
                            .append(Component.text(current, TextColor.color(0x55FF55)))
                            .append(Component.text(" → ", TextColor.color(0xFFAA00)))
                            .append(Component.text(block.getType().toString(), TextColor.color(0x55FF55)))
            );
        }
    }

    // -----------------------------
    // PROPERTY EXTRACTION
    // -----------------------------

    @SuppressWarnings("unused")
    private List<String> extractProperties(BlockData data) {
        List<String> props = new ArrayList<>();

        for (String key : data.getAsString().split("\\[")) {
            // not used, fallback below
        }

        // Paper API way: only few block types expose mutability
        // We'll support generic "blockdata state cycling" via string form
        String[] parts = data.getAsString().split("\\[");
        if (parts.length < 2) return props;

        String statePart = parts[1].replace("]", "");
        String[] entries = statePart.split(",");

        for (String entry : entries) {
            if (entry.contains("=")) {
                props.add(entry.split("=")[0].trim());
            }
        }

        return props;
    }

    // -----------------------------
    // APPLY PROPERTY CHANGE
    // -----------------------------

    private void applyProperty(Block block, BlockData data, String property, boolean inverse) {

        String serialized = data.getAsString();

        String[] split = serialized.split("\\[");
        if (split.length < 2) return;

        String base = split[0];
        String state = split[1].replace("]", "");

        String[] entries = state.split(",");

        List<String> updated = new ArrayList<>();

        for (String e : entries) {
            if (!e.contains("=")) continue;

            String[] kv = e.split("=");
            String key = kv[0].trim();
            String value = kv[1].trim();

            if (key.equals(property)) {
                // toggle / cycle for booleans or enums (simple heuristic)
                value = cycleValue(value, inverse);
            }

            updated.add(key + "=" + value);
        }

        String newState = String.join(",", updated);
        String finalData = base + "[" + newState + "]";

        BlockData newData = org.bukkit.Bukkit.createBlockData(finalData);
        block.setBlockData(newData, false);
    }

    // -----------------------------
    // VALUE CYCLING (GENERIC)
    // -----------------------------

    private String cycleValue(String value, boolean inverse) {

        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.toString(!Boolean.parseBoolean(value));
        }

        try {
            int v = Integer.parseInt(value);
            return Integer.toString(inverse ? v - 1 : v + 1);
        } catch (NumberFormatException ignored) {
            // enum fallback
        }

        return value;
    }

    // -----------------------------
    // LIST CYCLING
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