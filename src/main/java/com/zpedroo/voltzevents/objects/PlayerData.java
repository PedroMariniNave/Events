package com.zpedroo.voltzevents.objects;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.UUID;

@Getter @Setter
public class PlayerData {

    private final UUID uniqueId;
    private int winsAmount;
    private int participationsAmount;
    private final Map<SpecialItem, Boolean> specialItemsStatus;
    private boolean update = false;

    public PlayerData(UUID uniqueId, int winsAmount, int participationsAmount, Map<SpecialItem, Boolean> specialItemsStatus) {
        this.uniqueId = uniqueId;
        this.winsAmount = winsAmount;
        this.participationsAmount = participationsAmount;
        this.specialItemsStatus = specialItemsStatus;
    }

    public double getWinRate() {
        return participationsAmount == 0 ? winsAmount : (double) (winsAmount / participationsAmount) * 100;
    }

    public boolean getSpecialItemStatus(SpecialItem specialItem) {
        return specialItemsStatus.getOrDefault(specialItem, true);
    }

    public void addWins(int winsAmount) {
        this.setWinsAmount(this.winsAmount + winsAmount);
    }

    public void setWinsAmount(int winsAmount) {
        this.winsAmount = winsAmount;
        this.update = true;
    }

    public void addParticipation(int participationsAmount) {
        this.setParticipationsAmount(this.participationsAmount + participationsAmount);
    }

    public void setParticipationsAmount(int participationsAmount) {
        this.participationsAmount = participationsAmount;
        this.update = true;
    }

    public void setSpecialItemsStatus(SpecialItem specialItem, boolean status) {
        this.specialItemsStatus.put(specialItem, status);
        this.update = true;
    }

    public boolean isViewingParticipants() {
        for (Map.Entry<SpecialItem, Boolean> entry : specialItemsStatus.entrySet()) {
            SpecialItem specialItem = entry.getKey();
            if (!StringUtils.equalsIgnoreCase(specialItem.getAction(), "SWITCH_VISIBILITY")) continue;

            return entry.getValue();
        }

        return true;
    }
}