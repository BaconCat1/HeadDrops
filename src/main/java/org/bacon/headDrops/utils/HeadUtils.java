/*
 * HeadDrops Plugin
 * Created by @BaconCat1
 * Copyright (C) 2025 BaconCat1
 * Licensed under the GNU General Public License v3.0
 */

package org.bacon.headDrops.utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.bacon.headDrops.HeadDrops;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.profile.PlayerProfile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class HeadUtils {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private HeadUtils() {
    }

    public static ItemStack createHeadItem(HeadDrops plugin, HeadMetadata metadata, PlayerProfile profile) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(metadata, "metadata");

        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta baseMeta = item.getItemMeta();
        if (!(baseMeta instanceof SkullMeta skullMeta)) {
            return item;
        }

        PlayerProfile appliedProfile = profile;
        if (appliedProfile == null) {
            appliedProfile = Bukkit.createProfile(metadata.ownerUuid(), metadata.ownerName());
        }
        skullMeta.setPlayerProfile(appliedProfile);

        applyDisplayData(plugin, skullMeta, metadata);
        writeHeadData(plugin, skullMeta.getPersistentDataContainer(), metadata);

        item.setItemMeta(skullMeta);
        return item;
    }

    public static boolean isPluginHead(HeadDrops plugin, ItemStack item) {
        if (plugin == null || item == null) {
            return false;
        }
        if (item.getType() != Material.PLAYER_HEAD) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(plugin.markerKey(), PersistentDataType.BYTE);
    }

    public static Optional<HeadMetadata> readHeadData(HeadDrops plugin, ItemStack item) {
        if (plugin == null || item == null) {
            return Optional.empty();
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        return readHeadData(plugin, meta.getPersistentDataContainer());
    }

    public static Optional<HeadMetadata> readHeadData(HeadDrops plugin, PersistentDataContainer container) {
        if (plugin == null || container == null) {
            return Optional.empty();
        }
        if (!container.has(plugin.markerKey(), PersistentDataType.BYTE)) {
            return Optional.empty();
        }

        String ownerIdRaw = container.get(plugin.ownerIdKey(), PersistentDataType.STRING);
        String ownerName = container.get(plugin.ownerNameKey(), PersistentDataType.STRING);
        Long timestampRaw = container.get(plugin.timestampKey(), PersistentDataType.LONG);

        if (ownerIdRaw == null || ownerName == null || timestampRaw == null) {
            return Optional.empty();
        }

        UUID ownerId;
        try {
            ownerId = UUID.fromString(ownerIdRaw);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

    Instant timestamp = Instant.ofEpochMilli(timestampRaw);
    String killerName = container.get(plugin.killerKey(), PersistentDataType.STRING);
    String serial = container.get(plugin.serialKey(), PersistentDataType.STRING);

    return Optional.of(new HeadMetadata(ownerId, ownerName, timestamp, killerName, serial));
    }

    public static void writeHeadData(HeadDrops plugin, PersistentDataContainer container, HeadMetadata metadata) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(container, "container");
        Objects.requireNonNull(metadata, "metadata");

        container.set(plugin.markerKey(), PersistentDataType.BYTE, (byte) 1);
        container.set(plugin.ownerIdKey(), PersistentDataType.STRING, metadata.ownerUuid().toString());
        container.set(plugin.ownerNameKey(), PersistentDataType.STRING, metadata.ownerName());
        container.set(plugin.timestampKey(), PersistentDataType.LONG, metadata.timestamp().toEpochMilli());

        if (metadata.killerName() != null && !metadata.killerName().isBlank()) {
            container.set(plugin.killerKey(), PersistentDataType.STRING, metadata.killerName());
        } else {
            container.remove(plugin.killerKey());
        }

        if (metadata.serial() != null && !metadata.serial().isBlank()) {
            container.set(plugin.serialKey(), PersistentDataType.STRING, metadata.serial());
        } else {
            container.remove(plugin.serialKey());
        }
    }

    public static void clearHeadData(HeadDrops plugin, PersistentDataContainer container) {
        if (plugin == null || container == null) {
            return;
        }
        container.remove(plugin.markerKey());
        container.remove(plugin.ownerIdKey());
        container.remove(plugin.ownerNameKey());
        container.remove(plugin.timestampKey());
        container.remove(plugin.killerKey());
    container.remove(plugin.serialKey());
    }

    private static void applyDisplayData(HeadDrops plugin, SkullMeta meta, HeadMetadata metadata) {
        String title = applyPlaceholders(plugin.titleTemplate(), metadata, plugin);
        if (!title.isBlank()) {
            meta.displayName(LEGACY_SERIALIZER.deserialize(title));
        }

        List<Component> lore = new ArrayList<>();

        String timestampLine = applyPlaceholders(plugin.timestampTemplate(), metadata, plugin);
        if (!timestampLine.isBlank()) {
            lore.add(LEGACY_SERIALIZER.deserialize(timestampLine));
        }

        String killerLine = applyPlaceholders(plugin.killerTemplate(), metadata, plugin);
        if (!killerLine.isBlank()) {
            lore.add(LEGACY_SERIALIZER.deserialize(killerLine));
        }

        if (plugin.serialEnabled() && plugin.serialVisible()) {
            String serialLine = applyPlaceholders(plugin.serialTemplate(), metadata, plugin);
            if (!serialLine.isBlank()) {
                lore.add(LEGACY_SERIALIZER.deserialize(serialLine));
            }
        }

        meta.lore(lore);
    }

    private static String applyPlaceholders(String template, HeadMetadata metadata, HeadDrops plugin) {
        String killerName = metadata.killerName();
        if (killerName == null || killerName.isBlank()) {
            killerName = plugin.killerUnknown();
        }

        String serial = metadata.serial();
        if (serial == null || serial.isBlank()) {
            serial = plugin.serialUnknown();
        }

        String result = template
                .replace("%player%", metadata.ownerName())
                .replace("%timestamp%", plugin.formatTimestamp(metadata.timestamp()))
                .replace("%killer%", killerName)
                .replace("%serial%", serial);

        return result;
    }
    
    public record HeadMetadata(UUID ownerUuid, String ownerName, Instant timestamp, String killerName, String serial) {

        public HeadMetadata {
            Objects.requireNonNull(ownerUuid, "ownerUuid");
            Objects.requireNonNull(ownerName, "ownerName");
            Objects.requireNonNull(timestamp, "timestamp");
            if (killerName != null && killerName.isBlank()) {
                killerName = null;
            }
            if (serial != null && serial.isBlank()) {
                serial = null;
            }
        }

        public HeadMetadata(UUID ownerUuid, String ownerName, Instant timestamp, String killerName) {
            this(ownerUuid, ownerName, timestamp, killerName, null);
        }
    }
}
