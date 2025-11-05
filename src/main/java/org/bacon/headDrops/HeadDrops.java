/*
 * HeadDrops Plugin
 * Created by @BaconCat1
 * Copyright (C) 2025 BaconCat1
 * Licensed under the GNU General Public License v3.0
 */

package org.bacon.headDrops;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import org.bacon.headDrops.listeners.BlockBreakListener;
import org.bacon.headDrops.listeners.BlockPlaceListener;
import org.bacon.headDrops.listeners.DeathListener;
import org.bacon.headDrops.serial.SerialNumberService;
import org.bacon.headDrops.serial.SerialNumberService.SerialMode;
import org.bacon.headDrops.utils.TimeUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class HeadDrops extends JavaPlugin {

    private static final String DEFAULT_TITLE_TEMPLATE = "&6%player%'s Head";
    private static final String DEFAULT_TIMESTAMP_TEMPLATE = "&7Killed on: &f%timestamp%";
    private static final String DEFAULT_KILLER_TEMPLATE = "&7Killer: &f%killer%";
    private static final String DEFAULT_KILLER_UNKNOWN = "Unknown";
    private static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_SERIAL_TEMPLATE = "&7Serial: &f%serial%";
    private static final String DEFAULT_SERIAL_UNKNOWN = "N/A";

    private NamespacedKey markerKey;
    private NamespacedKey ownerIdKey;
    private NamespacedKey ownerNameKey;
    private NamespacedKey timestampKey;
    private NamespacedKey killerKey;
    private NamespacedKey serialKey;

    private boolean requirePlayerKill;
    private double dropChance;
    private ZoneId timestampZone;
    private DateTimeFormatter timestampFormatter;
    private String titleTemplate;
    private String timestampTemplate;
    private String killerTemplate;
    private String killerUnknown;
    private String serialTemplate;
    private String serialUnknown;
    private boolean serialEnabled;
    private boolean serialVisible;
    private SerialMode serialMode;
    private SerialNumberService serialService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initialiseKeys();
        serialService = new SerialNumberService(this);
        serialService.load();
        applySettings(getConfig());

        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new DeathListener(this), this);
        pluginManager.registerEvents(new BlockPlaceListener(this), this);
        pluginManager.registerEvents(new BlockBreakListener(this), this);
    }

    @Override
    public void onDisable() {
        if (serialService != null) {
            serialService.save();
        }
    }

    public void reloadPluginSettings() {
        reloadConfig();
        if (serialService != null) {
            serialService.load();
        }
        applySettings(getConfig());
    }

    private void initialiseKeys() {
        markerKey = new NamespacedKey(this, "head_marker");
        ownerIdKey = new NamespacedKey(this, "head_owner_id");
        ownerNameKey = new NamespacedKey(this, "head_owner_name");
        timestampKey = new NamespacedKey(this, "head_timestamp");
        killerKey = new NamespacedKey(this, "head_killer");
        serialKey = new NamespacedKey(this, "head_serial");
    }

    private void applySettings(FileConfiguration config) {
        requirePlayerKill = config.getBoolean("drop.require-player-kill", true);
        dropChance = clampPercentage(config.getDouble("drop.drop-chance", 100.0));

        var logger = getLogger();
        String requestedZone = config.getString("lore.timezone");
        timestampZone = TimeUtils.resolveZoneId(requestedZone, logger);
        timestampFormatter = TimeUtils.buildFormatter(
                config.getString("lore.timestamp-format"),
                timestampZone,
                DEFAULT_TIMESTAMP_PATTERN,
                logger
        );

        titleTemplate = nonNullOrDefault(config.getString("lore.title"), DEFAULT_TITLE_TEMPLATE);
        timestampTemplate = nonNullOrDefault(config.getString("lore.timestamp-line"), DEFAULT_TIMESTAMP_TEMPLATE);
        killerTemplate = nonNullOrDefault(config.getString("lore.killer-line"), DEFAULT_KILLER_TEMPLATE);
        killerUnknown = nonNullOrDefault(config.getString("lore.killer-unknown"), DEFAULT_KILLER_UNKNOWN);

        serialUnknown = nonNullOrDefault(config.getString("lore.serial-unknown"), DEFAULT_SERIAL_UNKNOWN);
        serialVisible = config.getBoolean("serial.visible", true);
        serialTemplate = serialVisible
                ? nonNullOrDefault(config.getString("lore.serial-line"), DEFAULT_SERIAL_TEMPLATE)
                : "";

        serialEnabled = config.getBoolean("serial.enabled", true);
        String modeName = config.getString("serial.mode", "random");
        serialMode = SerialMode.fromConfig(modeName).orElse(SerialMode.RANDOM);

        if (serialService != null) {
            serialService.configure(serialEnabled, serialMode);
        }
    }

    private String nonNullOrDefault(String value, String fallback) {
        return Objects.requireNonNullElse(value, fallback);
    }

    private double clampPercentage(double value) {
        if (Double.isNaN(value)) {
            return 100.0;
        }
        return Math.max(0.0, Math.min(100.0, value));
    }

    public NamespacedKey markerKey() {
        return markerKey;
    }

    public NamespacedKey ownerIdKey() {
        return ownerIdKey;
    }

    public NamespacedKey ownerNameKey() {
        return ownerNameKey;
    }

    public NamespacedKey timestampKey() {
        return timestampKey;
    }

    public NamespacedKey killerKey() {
        return killerKey;
    }

    public NamespacedKey serialKey() {
        return serialKey;
    }

    public boolean requirePlayerKill() {
        return requirePlayerKill;
    }

    public double dropChance() {
        return dropChance;
    }

    public String titleTemplate() {
        return titleTemplate;
    }

    public String timestampTemplate() {
        return timestampTemplate;
    }

    public String killerTemplate() {
        return killerTemplate;
    }

    public String killerUnknown() {
        return killerUnknown;
    }

    public String serialTemplate() {
        return serialTemplate;
    }

    public String serialUnknown() {
        return serialUnknown;
    }

    public boolean serialEnabled() {
        return serialEnabled;
    }

    public boolean serialVisible() {
        return serialVisible;
    }

    public Optional<String> nextSerial() {
        if (serialService == null) {
            return Optional.empty();
        }
        return serialService.nextSerial();
    }

    public String formatTimestamp(Instant instant) {
        return timestampFormatter.format(instant);
    }

    public ZoneId timestampZone() {
        return timestampZone;
    }

    public DateTimeFormatter timestampFormatter() {
        return timestampFormatter;
    }

    public Logger pluginLogger() {
        return getLogger();
    }
}
