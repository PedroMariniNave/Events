package com.zpedroo.voltzevents.events.hotpotato.tasks;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent;
import com.zpedroo.voltzevents.utils.actionbar.ActionBarAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class HotPotatoTask extends BukkitRunnable {

    private final Plugin plugin = VoltzEvents.get();
    private final HotPotatoEvent event;

    public HotPotatoTask(HotPotatoEvent event) {
        this.event = event;
    }

    @Override
    public void run() {
        if (!event.isHappening()) {
            this.cancel();
            return;
        }

        if (event.getRoundTimer() <= 0) { // waiting new round
            int newRoundTimer = event.getNewRoundTimer();
            event.setNewRoundTimer(--newRoundTimer);
            event.setAllParticipantsLevel(newRoundTimer);

            if (newRoundTimer <= 3) {
                event.playSoundToAllParticipants(Sound.NOTE_STICKS, 0.75f, 1f);
            }

            if (newRoundTimer < 0) {
                event.setEventPhase(EventPhase.STARTED);
                event.setRoundTimer(HotPotatoEvent.Settings.ROUND_DURATION);
                event.setNewRoundTimer(HotPotatoEvent.Settings.ROUND_DELAY);
                event.selectHotPotatoesAndAnnounce();
            }
        } else {
            int roundTimer = event.getRoundTimer();
            event.setRoundTimer(--roundTimer);
            event.setAllParticipantsLevel(roundTimer);

            if (roundTimer <= 3) {
                event.playSoundToAllParticipants(Sound.NOTE_STICKS, 0.75f, 1f);
            }

            if (roundTimer > 0 || event.getEventPhase() != EventPhase.STARTED) return;

            event.setEventPhase(EventPhase.WARMUP);
            event.explodeAllHotPotatoes();
            event.playSoundToAllParticipants(Sound.EXPLODE, 0.5f, 2f);

            final int hotPotatoesAmount = event.getHotPotatoesAmount();
            int roundDelayInSeconds = HotPotatoEvent.Settings.ROUND_DELAY;

            event.sendMessageToAllParticipants(HotPotatoEvent.Messages.ROUND_FINISHED, new String[]{
                    "{amount}",
                    "{round_delay}"
            }, new String[]{
                    String.valueOf(hotPotatoesAmount),
                    String.valueOf(roundDelayInSeconds)
            });
        }

        updateAllParticipantsActionBar();
        updateAllParticipantsPotion();
    }

    private void updateAllParticipantsActionBar() {
        event.getPlayersParticipating().forEach(player -> {
            String actionBar = event.getEventPhase() == EventPhase.WARMUP ?
                    StringUtils.replace(HotPotatoEvent.ActionBars.WARMUP, "{timer}", String.valueOf(event.getNewRoundTimer())) :
                    event.isHotPotato(player) ? HotPotatoEvent.ActionBars.TAGGED : HotPotatoEvent.ActionBars.UNTAGGED;
            ActionBarAPI.sendActionBar(player, actionBar);
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

    public void startTask() {
        this.runTaskTimer(plugin, 0L, 20L);
    }
}