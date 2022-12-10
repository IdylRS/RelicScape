package com.relicscape;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
public class UnlockData {
    private int points;
    private List<String> skills;
    private List<TrailblazerRegion> areas;
    private List<String> tasks;

    UnlockData() {
        points = 0;
        skills = new ArrayList<>();
        areas = new ArrayList<>();
        tasks = new ArrayList<>();
    }

    public void addArea(TrailblazerRegion area) {
        areas.add(area);
    }

    public void removeArea(TrailblazerRegion area) {
        this.areas.remove(area);
    }

    public void addSkill(String skill) {
        skills.add(skill);
    }

    public void removeSkill(RelicScapeSkill skill) {
        skills.remove(skill.getName());
    }

    public void addTask(LockedTask task) { tasks.add(task.getId()); }

    public void subtractPoints(int amount) { this.points -= amount; }

    public void addPoints(int amount) { this.points += amount; }

    public void clearAll() {
        points = 0;
        skills.clear();
        areas.clear();
    }

    public boolean isAreaUnlocked(String name) {
        TrailblazerRegion area = trailblazerify(name);
        return areas.contains(area);
    }

    private TrailblazerRegion trailblazerify(String name) {
        return TrailblazerRegion.valueOf(name.toUpperCase().replace(" ", "_"));
    }
}
