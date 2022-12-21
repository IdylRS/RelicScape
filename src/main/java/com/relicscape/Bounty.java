package com.relicscape;

import lombok.Getter;

import java.util.List;

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

    public Bounty(int id, String npcName, int minCombatLevel, List<Integer> npcIDs, int tier) {
        this.id = id;
        this.npcName = npcName;
        this.npcIDs = npcIDs;
        this.minCombatLevel = minCombatLevel;
        this.tier = tier;
    }

    public boolean isBountyComplete(int killedNpcID) {
        return npcIDs.contains(killedNpcID);
    }

    public String getBountyDesc() {
        return "Kill a "+npcName+".";
    }
}
