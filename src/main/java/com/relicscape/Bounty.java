package com.relicscape;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Bounty {
    @Getter
    private String npcName;
    @Getter
    private int minCombatLevel;
    @Getter
    private List<Integer> npcIDs;
    @Getter
    private int tier;
    @Getter
    private int id;

    @Getter
    private int ticksRemaining;

    public Bounty(int id, String npcName, int minCombatLevel, List<Integer> npcIDs, int tier) {
        this.id = id;
        this.npcName = npcName;
        this.npcIDs = npcIDs;
        this.minCombatLevel = minCombatLevel;
        this.tier = tier;
    }

    public void initCounter() {
        ticksRemaining = 500;
    }

    public boolean isBountyComplete(int killedNpcID) {
        return npcIDs.contains(killedNpcID);
    }

    public String getBountyDesc() {
        return "Kill a "+npcName+".";
    }

    public String getTimeRemaining() {
        if(ticksRemaining == 0) return "0:00";

        int seconds = (int) Math.floor(ticksRemaining * .6);
        int minutes = (int) Math.floor(seconds/60);
        if(minutes == 0) return seconds%60+"s";
        return (minutes+1)+"m";
    }

    public void decrementTicks() {
        if(ticksRemaining == 0) return;

        ticksRemaining--;
    }
}
