package com.zpedroo.voltzevents.hooks;

import org.bukkit.entity.Player;
import yclans.api.yClansAPI;
import yclans.model.Clan;
import yclans.model.ClanPlayer;

import java.util.HashMap;
import java.util.Map;

public class yClansHook {

    private static yClansAPI clansAPI = null;
    private static final Map<Player, PvPStatus> oldClanPvPStatus = new HashMap<>(16);

    public static void setClanPvP(Player player, boolean status) {
        if (clansAPI == null) return;

        ClanPlayer clanPlayer = clansAPI.getPlayer(player);
        if (!clanPlayer.hasClan()) return;

        Clan clan = clanPlayer.getClan();
        PvPStatus oldStatus = new PvPStatus(clan.isFriendlyFireAlly(), clan.isFriendlyFireMember());
        oldClanPvPStatus.put(player, oldStatus);

        clan.setFriendlyFireAlly(status);
        clan.setFriendlyFireMember(status);
    }

    public static void resetClanPvP(Player player) {
        if (clansAPI == null) return;

        PvPStatus status = oldClanPvPStatus.remove(player);
        if (status == null) return;

        ClanPlayer clanPlayer = clansAPI.getPlayer(player);
        Clan clan = clanPlayer.getClan();

        clan.setFriendlyFireAlly(status.isFriendlyFireAlly());
        clan.setFriendlyFireMember(status.isFriendlyFireMember());
    }

    public static void hook() {
        yClansHook.clansAPI = yClansAPI.yclansapi;
    }

    private static class PvPStatus {

        private final boolean friendlyFireAlly;
        private final boolean friendlyFireMember;

        public PvPStatus(boolean friendlyFireAlly, boolean friendlyFireMember) {
            this.friendlyFireAlly = friendlyFireAlly;
            this.friendlyFireMember = friendlyFireMember;
        }

        public boolean isFriendlyFireAlly() {
            return friendlyFireAlly;
        }

        public boolean isFriendlyFireMember() {
            return friendlyFireMember;
        }
    }
}