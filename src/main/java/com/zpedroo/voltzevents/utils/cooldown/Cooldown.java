package com.zpedroo.voltzevents.utils.cooldown;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.zpedroo.voltzevents.utils.formatter.TimeFormatter;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Cooldown {

    private static Cooldown instance;
    public static Cooldown get() { return instance; }

    private final Table<UUID, Object, Long> cooldowns = HashBasedTable.create();

    public Cooldown() {
        instance = this;
    }

    public void addCooldown(Player player, Object object, int cooldownInSeconds) {
        addCooldown(player, object, TimeUnit.SECONDS.toMillis(cooldownInSeconds));
    }

    public void addCooldown(Player player, Object object, long cooldownInMillis) {
        if (cooldownInMillis <= 0) return;

        cooldowns.put(player.getUniqueId(), object, System.currentTimeMillis() + cooldownInMillis);
    }

    public void removeCooldown(Player player, Object object) {
        removeCooldown(player.getUniqueId(), object);
    }

    public void removeCooldown(UUID uuid, Object object) {
        cooldowns.remove(uuid, object);
    }

    public boolean isInCooldown(Player player, Object object) {
        int timeLeftInSeconds = getTimeLeftInSeconds(player, object);
        if (timeLeftInSeconds <= 0 && cooldowns.contains(player.getUniqueId(), object)) {
            cooldowns.row(player.getUniqueId()).remove(object);
            return false;
        }

        return timeLeftInSeconds > 0;
    }

    public long getTimeLeftInMillis(Player player, Object object) {
        if (!cooldowns.contains(player.getUniqueId(), object)) return 0L;

        return cooldowns.row(player.getUniqueId()).get(object) - System.currentTimeMillis();
    }

    public int getTimeLeftInSeconds(Player player, Object object) {
        long timeInMillis = getTimeLeftInMillis(player, object);

        return (int) TimeUnit.MILLISECONDS.toSeconds(timeInMillis);
    }

    public String getTimeLeftFormatted(Player player, Object object) {
        long timeInMillis = getTimeLeftInMillis(player, object);

        return TimeFormatter.millisToFormattedTime(timeInMillis);
    }
}