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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public final class BlockBreakListener implements Listener {

    private final HeadDrops plugin;

    public BlockBreakListener(HeadDrops plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        BlockState state = event.getBlock().getState();
        if (!(state instanceof Skull skull)) {
            return;
        }

        Optional<HeadMetadata> metadata = HeadUtils.readHeadData(plugin, skull.getPersistentDataContainer());
        if (metadata.isEmpty()) {
            return;
        }

        event.setDropItems(false);
        ItemStack drop = HeadUtils.createHeadItem(plugin, metadata.get(), skull.getPlayerProfile());

        World world = skull.getWorld();
        Location dropLocation = skull.getBlock().getLocation().add(0.5, 0.25, 0.5);
        world.dropItemNaturally(dropLocation, drop);

        HeadUtils.clearHeadData(plugin, skull.getPersistentDataContainer());
        skull.update(true, false);
    }
}
