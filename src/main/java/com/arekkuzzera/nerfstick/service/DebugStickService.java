package com.arekkuzzera.nerfstick.service;

import com.arekkuzzera.nerfstick.model.BlockProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DebugStickService {

    private final Map<String, String> selectedProperties = new ConcurrentHashMap<>();
    private final PermissionService permissionService;
    private final ProtectionService protectionService;
    private final BlockStateService blockStateService;

    public DebugStickService(
            PermissionService permissionService,
            ProtectionService protectionService,
            BlockStateService blockStateService
    ) {
        this.permissionService = permissionService;
        this.protectionService = protectionService;
        this.blockStateService = blockStateService;
    }

    public void handle(Player player, Block block, Action action) {
        if (!permissionService.canUse(player)) {
            player.sendActionBar(Component.text("You cannot use the debug stick.", TextColor.color(0xFF5555)));
            return;
        }

        String denyReason = protectionService.denyReason(player, block.getLocation(), block.getType());
        if (denyReason != null && !denyReason.isEmpty()) {
            player.sendActionBar(
                    Component.text("Interaction denied: ", TextColor.color(0xFFAA00))
                            .append(Component.text(denyReason, TextColor.color(0xFF5555)))
            );
            return;
        }

        BlockData data = block.getBlockData();
        List<BlockProperty> properties = blockStateService.properties(data)
                .stream()
                .filter(property -> permissionService.canManipulate(player, property))
                .toList();

        if (properties.isEmpty()) {
            player.sendActionBar(Component.text("No permitted debug-stick properties.", TextColor.color(0xFFAA00)));
            return;
        }

        String selected = selectedProperty(player, block, properties);
        boolean reverse = player.isSneaking();

        if (action == Action.LEFT_CLICK_BLOCK) {
            BlockProperty next = relativeProperty(properties, selected, reverse);
            selectedProperties.put(selectionKey(player, block), next.name());
            sendSelected(player, block, next);
            return;
        }

        BlockData updated = blockStateService.cycle(data, selected, reverse);
        if (updated == null) {
            player.sendActionBar(
                    Component.text("Could not change property: ", TextColor.color(0xFFAA00))
                            .append(Component.text(selected, TextColor.color(0xFF5555)))
            );
            return;
        }

        block.setBlockData(updated, true);
        sendApplied(player, selected, blockStateService.currentValue(updated, selected), block);
    }

    private String selectedProperty(Player player, Block block, List<BlockProperty> properties) {
        String key = selectionKey(player, block);
        String selected = selectedProperties.getOrDefault(key, properties.get(0).name());

        boolean stillAllowed = false;
        for (BlockProperty property : properties) {
            if (property.name().equals(selected)) {
                stillAllowed = true;
                break;
            }
        }
        if (!stillAllowed) {
            selected = properties.get(0).name();
            selectedProperties.put(key, selected);
        }

        return selected;
    }

    private BlockProperty relativeProperty(List<BlockProperty> properties, String selected, boolean reverse) {
        int currentIndex = -1;
        for (int index = 0; index < properties.size(); index++) {
            if (properties.get(index).name().equals(selected)) {
                currentIndex = index;
                break;
            }
        }

        if (currentIndex == -1) {
            return properties.get(0);
        }

        int nextIndex = Math.floorMod(currentIndex + (reverse ? -1 : 1), properties.size());
        return properties.get(nextIndex);
    }

    private String selectionKey(Player player, Block block) {
        return player.getUniqueId() + ":" + block.getType().getKey();
    }

    private void sendSelected(Player player, Block block, BlockProperty property) {
        player.sendActionBar(
                Component.text("Selected: ", TextColor.color(0xFFAA00))
                        .append(Component.text(property.name(), TextColor.color(0x55FF55)))
                        .append(Component.text(" [" + property.type().displayName() + "]", TextColor.color(0xAAAAAA)))
                        .append(Component.text(" (" + block.getType().getKey() + ")", TextColor.color(0xFFAA00)))
        );
    }

    private void sendApplied(Player player, String property, String value, Block block) {
        player.sendActionBar(
                Component.text("Applied: ", TextColor.color(0xFFAA00))
                        .append(Component.text(property + "=" + value, TextColor.color(0x55FF55)))
                        .append(Component.text(" -> ", TextColor.color(0xFFAA00)))
                        .append(Component.text(block.getType().getKey().toString(), TextColor.color(0x55FF55)))
        );
    }
}
