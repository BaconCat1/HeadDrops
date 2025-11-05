/*
 * HeadDrops Plugin
 * Created by @BaconCat1
 * Copyright (C) 2025 BaconCat1
 * Licensed under the GNU General Public License v3.0
 */

package org.bacon.headDrops.serial;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.bacon.headDrops.HeadDrops;
import org.bukkit.configuration.file.YamlConfiguration;

public final class SerialNumberService {

    public enum SerialMode {
        RANDOM,
        INCREMENTAL;

        public static Optional<SerialMode> fromConfig(String value) {
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }
            try {
                return Optional.of(SerialMode.valueOf(value.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
        }
    }

    private final HeadDrops plugin;
    private final AtomicLong counter = new AtomicLong();
    private final File dataFile;

    private SerialMode mode = SerialMode.RANDOM;
    private boolean enabled = true;

    public SerialNumberService(HeadDrops plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "serial-data.yml");
    }

    public void load() {
        ensureParentExists();
        if (!dataFile.exists()) {
            counter.set(0L);
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        long stored = yaml.getLong("last-value", 0L);
        counter.set(stored);
    }

    public void save() {
        persistValue(counter.get());
    }

    public void configure(boolean enabled, SerialMode mode) {
        this.enabled = enabled;
        if (mode != null) {
            this.mode = mode;
        }
    }

    public Optional<String> nextSerial() {
        if (!enabled) {
            return Optional.empty();
        }

        return Optional.of(mode == SerialMode.INCREMENTAL ? nextIncremental() : nextRandom());
    }

    private String nextIncremental() {
        long value = counter.incrementAndGet();
        persistValue(value);
        return Long.toString(value);
    }

    private String nextRandom() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    private void persistValue(long value) {
        if (!ensureParentExists()) {
            return;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("last-value", value);
        try {
            yaml.save(dataFile);
        } catch (IOException ex) {
            Logger logger = plugin.getLogger();
            logger.warning("Failed to save serial counter: " + ex.getMessage());
        }
    }

    private boolean ensureParentExists() {
        File parent = dataFile.getParentFile();
        if (parent == null) {
            return false;
        }
        if (parent.exists()) {
            return true;
        }
        return parent.mkdirs();
    }
}
