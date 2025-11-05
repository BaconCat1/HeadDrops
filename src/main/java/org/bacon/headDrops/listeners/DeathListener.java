/*
 * HeadDrops Plugin
 * Created by @BaconCat1
 * Copyright (C) 2025 BaconCat1
 * Licensed under the GNU General Public License v3.0
 */

package org.bacon.headDrops.listeners;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bacon.headDrops.HeadDrops;
import org.bacon.headDrops.utils.HeadUtils;
import org.bacon.headDrops.utils.HeadUtils.HeadMetadata;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class DeathListener implements Listener {

    private final HeadDrops plugin;

    public DeathListener(HeadDrops plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (plugin.requirePlayerKill() && killer == null) {
            return;
        }

        double chance = plugin.dropChance();
        if (chance <= 0.0) {
            return;
        }
        if (chance < 100.0) {
            double roll = ThreadLocalRandom.current().nextDouble(100.0);
            if (roll >= chance) {
                return;
            }
        }

        Instant deathTime = Instant.now();
        String killerName = determineKillerName(event).orElse(null);
        String serial = plugin.nextSerial().orElse(null);

        HeadMetadata metadata = new HeadMetadata(
                victim.getUniqueId(),
                victim.getName(),
                deathTime,
                killerName,
                serial
        );

        event.getDrops().add(HeadUtils.createHeadItem(plugin, metadata, victim.getPlayerProfile()));
    }

    private Optional<String> determineKillerName(PlayerDeathEvent event) {
        Player directKiller = event.getEntity().getKiller();
        if (directKiller != null) {
            return Optional.of(directKiller.getName());
        }

        EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();
        if (lastDamage == null) {
            return Optional.empty();
        }

        if (lastDamage instanceof EntityDamageByEntityEvent entityDamage) {
            Entity damager = resolveDamager(entityDamage.getDamager());
            if (damager instanceof Player player) {
                return Optional.of(player.getName());
            }
            return Optional.ofNullable(prettyName(damager.getName()));
        }

        return Optional.ofNullable(prettyEnumName(lastDamage.getCause()));
    }

    private Entity resolveDamager(Entity damager) {
        if (damager instanceof Projectile projectile) {
            Object shooter = projectile.getShooter();
            if (shooter instanceof Entity entity) {
                return entity;
            }
        }
        return damager;
    }

    private String prettyName(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Arrays.stream(raw.split(" "))
                .filter(part -> !part.isBlank())
                .map(this::capitalise)
                .collect(Collectors.joining(" "));
    }

    private String prettyEnumName(Enum<?> value) {
        if (value == null) {
            return null;
        }
        String spaced = value.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Arrays.stream(spaced.split(" "))
                .filter(part -> !part.isBlank())
                .map(this::capitalise)
                .collect(Collectors.joining(" "));
    }

    private String capitalise(String part) {
        if (part.isEmpty()) {
            return part;
        }
        return Character.toUpperCase(part.charAt(0)) + part.substring(1).toLowerCase(Locale.ROOT);
    }
}
