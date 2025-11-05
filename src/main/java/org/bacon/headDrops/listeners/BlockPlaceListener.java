/*
 * HeadDrops Plugin
 * Created by @BaconCat1
 * Copyright (C) 2025 BaconCat1
 * Licensed under the GNU General Public License v3.0
 */

package org.bacon.headDrops.listeners;

import java.util.Optional;

import org.bacon.headDrops.HeadDrops;
import org.bacon.headDrops.utils.HeadUtils;
import org.bacon.headDrops.utils.HeadUtils.HeadMetadata;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public final class BlockPlaceListener implements Listener {

    private final HeadDrops plugin;

    public BlockPlaceListener(HeadDrops plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!HeadUtils.isPluginHead(plugin, item)) {
            return;
        }

        Optional<HeadMetadata> metadata = HeadUtils.readHeadData(plugin, item);
        if (metadata.isEmpty()) {
            return;
        }

        BlockState state = event.getBlockPlaced().getState();
        if (state instanceof Skull skull) {
            HeadUtils.writeHeadData(plugin, skull.getPersistentDataContainer(), metadata.get());
            skull.update(true, false);
        }
    }
}
