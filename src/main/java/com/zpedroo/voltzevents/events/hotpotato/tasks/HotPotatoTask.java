package com.zpedroo.voltzevents.events.hotpotato.tasks;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent;
import com.zpedroo.voltzevents.utils.actionbar.ActionBarAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import static com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent.Settings.ROUND_DELAY;
import static com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent.Settings.ROUND_DURATION;

public class HotPotatoTask extends BukkitRunnable {

    private final Plugin plugin = VoltzEvents.get();
    private final HotPotatoEvent event;
    private int round = 1;
    private int burnTimer = ROUND_DURATION;
    private int newRoundTimer = ROUND_DELAY;

    public HotPotatoTask(HotPotatoEvent event) {
        this.event = event;
    }

    @Override
    public void run() {
        if (!event.isHappening()) {
            this.cancel();
            return;
        }

        if (burnTimer <= 0) { // waiting new round
            --newRoundTimer;
            event.setAllParticipantsLevel(newRoundTimer);

            if (newRoundTimer <= 3) {
                event.playSoundToAllParticipants(Sound.NOTE_STICKS, 0.75f, 1f);
            }

            if (newRoundTimer <= 0) {
                event.setEventPhase(EventPhase.STARTED);
                burnTimer = ROUND_DURATION;
                newRoundTimer = ROUND_DELAY;
                event.selectHotPotatoesAndAnnounce();
            }
        } else {
            --burnTimer;
            event.setAllParticipantsLevel(burnTimer);

            if (burnTimer <= 3) {
                event.playSoundToAllParticipants(Sound.NOTE_STICKS, 0.75f, 1f);
            }

            if (burnTimer <= 0 && event.getEventPhase() == EventPhase.STARTED) {
                event.setEventPhase(EventPhase.WARMUP);
                event.explodeAllHotPotatoes();
                event.playSoundToAllParticipants(Sound.EXPLODE, 0.5f, 2f);

                final int hotPotatoesAmount = event.getHotPotatoesAmount();

                event.sendMessageToAllParticipants(HotPotatoEvent.Messages.ROUND_FINISHED, new String[]{
                        "{amount}",
                        "{round}",
                        "{new_round}",
                        "{round_delay}"
                }, new String[]{
                        String.valueOf(hotPotatoesAmount),
                        String.valueOf(round),
                        String.valueOf(++round),
                        String.valueOf(ROUND_DELAY)
                });
            }
        }

        updateAllParticipantsActionBar();
        updateAllParticipantsPotion();
    }

    private void updateAllParticipantsActionBar() {
        event.getPlayersParticipating().forEach(player -> {
            String text = event.getEventPhase() == EventPhase.WARMUP ? StringUtils.replace(HotPotatoEvent.ActionBars.WARMUP, "{timer}", String.valueOf(newRoundTimer)) :
                    event.isHotPotato(player) ? HotPotatoEvent.ActionBars.TAGGED : HotPotatoEvent.ActionBars.UNTAGGED;
            ActionBarAPI.sendActionBar(player, text);
        });
    }

    private void updateAllParticipantsPotion() {
        event.getPlayersParticipating().forEach(player -> {
            boolean isHotPotato = event.isHotPotato(player);
            boolean hasPotion = isHotPotato ? HotPotatoEvent.Potions.IS_TAGGED_POTION_ENABLED : HotPotatoEvent.Potions.IS_UNTAGGED_POTION_ENABLED;
            if (!hasPotion) {
                for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                    player.removePotionEffect(potionEffect.getType());
                }
                return;
            }

            PotionEffectType potionEffectType = isHotPotato ? HotPotatoEvent.Potions.TAGGED_POTION_TYPE : HotPotatoEvent.Potions.UNTAGGED_POTION_TYPE;
            int level = isHotPotato ? HotPotatoEvent.Potions.TAGGED_POTION_LEVEL : HotPotatoEvent.Potions.UNTAGGED_POTION_LEVEL;

            player.removePotionEffect(potionEffectType);
            player.addPotionEffect(new PotionEffect(potionEffectType, 999*999, level-1));
        });
    }

    public int getRound() {
        return round;
    }

    public int getBurnTimer() {
        return burnTimer;
    }

    public int getNewRoundTimer() {
        return newRoundTimer;
    }

    public void startTask() {
        this.runTaskTimer(plugin, 0L, 20L);
    }
}