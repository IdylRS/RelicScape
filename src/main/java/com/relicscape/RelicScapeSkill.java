package com.relicscape;

import lombok.Getter;

public enum RelicScapeSkill {
    ATTACK("Attack", 20971521, 100),
    STRENGTH("Strength", 20971522, 300),
    DEFENCE("Defence", 20971523, 100),
    RANGED("Ranged", 20971524, 300),
    PRAYER("Prayer", 20971525, 300),
    MAGIC("Magic", 20971526, 300),
    RUNECRAFT("Runecraft", 20971527, 200),
    CONSTRUCTION("Construction", 20971528, 300),
    HITPOINTS("Hitpoints", 20971529, 100),
    AGILITY("Agility", 20971530, 250),
    HERBLORE("Herblore", 20971531, 250),
    THIEVING("Thieving", 20971532, 250),
    CRAFTING("Crafting", 20971533, 200),
    FLETCHING("Fletching", 20971534, 150),
    SLAYER("Slayer", 20971535, 300),
    HUNTER("Hunter", 20971536, 300),
    MINING("Mining", 20971537, 200),
    SMITHING("Smithing", 20971538, 200),
    FISHING("Fishing", 20971539, 250),
    COOKING("Cooking", 20971540, 250),
    FIREMAKING("Firemaking", 20971541, 150),
    WOODCUTTING("Woodcutting", 20971542, 250),
    FARMING("Farming", 20971543, 300);

    @Getter
    private String name;
    @Getter
    private int widgetID;
    @Getter
    private int unlockCost;

    RelicScapeSkill(String name, int id, int unlockCost) {
        this.name = name;
        this.widgetID = id;
        this.unlockCost = unlockCost;
    }
}
