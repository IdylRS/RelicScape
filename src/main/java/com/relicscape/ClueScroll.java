package com.relicscape;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum ClueScroll {
    BEGINNER("beginner", 50, Arrays.asList(100,0,0, 0)),
    EASY("easy", 50, Arrays.asList(100, 30, 0, 0)),
    MEDIUM("medium", 50, Arrays.asList(100, 69, 1, 0)),
    HARD("hard", 60, Arrays.asList(0, 100, 10, 0)),
    ELITE("elite", 70, Arrays.asList(0, 100, 26, 1)),
    MASTER("master", 80, Arrays.asList(0, 100, 60, 10));

    private String tier;
    private List<Integer> relicTierChances;
    private int relicChance;

    ClueScroll(String tier, int relicChance, List<Integer> relicTierChances) {
        this.tier = tier;
        this.relicChance = relicChance;
        this.relicTierChances = relicTierChances;
    }
}
