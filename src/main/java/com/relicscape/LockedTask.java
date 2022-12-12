package com.relicscape;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirement;
import net.runelite.client.plugins.cluescrolls.clues.item.SingleItemRequirement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class LockedTask {
    private static final List<Skill> creationSkills = Arrays.asList(
            Skill.COOKING, Skill.SMITHING, Skill.FLETCHING,
            Skill.FISHING, Skill.THIEVING, Skill.RUNECRAFT,
            Skill.WOODCUTTING, Skill.MINING, Skill.HUNTER,
            Skill.HERBLORE, Skill.FARMING, Skill.FIREMAKING,
            Skill.CONSTRUCTION
    );

    private static final List<Integer> specialItemContainers = Arrays.asList(
            InventoryID.CHAMBERS_OF_XERIC_CHEST.getId(), InventoryID.TOA_REWARD_CHEST.getId(),
            InventoryID.THEATRE_OF_BLOOD_CHEST.getId(), InventoryID.BARROWS_REWARD.getId()
    );

    public static List<LockedTask> lootTaskList = new ArrayList<>();
    public static List<LockedTask> equipTaskList = new ArrayList<>();
    public static List<LockedTask> locationTaskList = new ArrayList<>();
    public static List<LockedTask> killTaskList = new ArrayList<>();
    public static List<LockedTask> creationTaskList = new ArrayList<>();
    public static List<LockedTask> magicTaskList = new ArrayList<>();
    public static List<LockedTask> levelTaskList = new ArrayList<>();
    public static List<LockedTask> questTaskList = new ArrayList<>();

    private String id;
    private int tier;
    private TaskType type;
    private String description;
    private TrailblazerRegion region;
    private List<Integer> completionIDs;
    private List<WorldPoint> locations;
    private Skill skill;
    private Quest quest;
    private double gainedXP;
    private boolean useRegionID;

    public LockedTask(
            String id,
            int tier,
            TaskType type,
            String description,
            TrailblazerRegion region,
            List<Integer> completionIDs,
            List<WorldPoint> locations,
            double gainedXP,
            Skill skill,
            boolean useRegionID,
            Quest quest

    ) {
        this.id = id;
        this.tier = tier;
        this.type = type;
        this.description = description;
        this.region = region;
        this.locations = locations;
        this.completionIDs = completionIDs;
        this.gainedXP = gainedXP;
        this.useRegionID = useRegionID;
        this.skill = skill;
        this.quest = quest;
    }

    public static void createTasks(LockedTask[] tasks) {
        Arrays.asList(tasks).forEach(task -> {
            switch(task.getType()) {
                case KILL:
                    killTaskList.add(task);
                    break;
                case LOOT:
                    lootTaskList.add(task);
                    break;
                case CREATE:
                    creationTaskList.add(task);
                    break;
                case ENTER:
                    locationTaskList.add(task);
                    break;
                case EQUIP:
                    equipTaskList.add(task);
                    break;
                case MAGIC:
                    magicTaskList.add(task);
                    break;
                case LEVEL:
                    levelTaskList.add(task);
                    break;
                case QUEST:
                    questTaskList.add(task);
                    log.info("Creating quest task "+task.getDescription());
                default:
                    break;
            }
        });
    }

    public static List<LockedTask> getAllTasks() {
        List<LockedTask> taskList = new ArrayList<>();

        taskList.addAll(killTaskList);
        taskList.addAll(lootTaskList);
        taskList.addAll(locationTaskList);
        taskList.addAll(creationTaskList);
        taskList.addAll(equipTaskList);
        taskList.addAll(magicTaskList);
        taskList.addAll(levelTaskList);
        taskList.addAll(questTaskList);

        return taskList;
    }

    public static List<LockedTask> getTasksByType(TaskType type) {
        switch(type) {
            case KILL:
                return killTaskList;
            case LOOT:
                return lootTaskList;
            case CREATE:
                return creationTaskList;
            case ENTER:
                return locationTaskList;
            case EQUIP:
                return equipTaskList;
            case MAGIC:
                return magicTaskList;
            case LEVEL:
                return levelTaskList;
            case QUEST:
                return questTaskList;
            default:
                return null;
        }
    }

    public static List<LockedTask> checkForLocationCompletion(WorldPoint playerLoc, List<String> completedTasks) {
        return locationTaskList.stream().filter(t -> isLocationTaskComplete(t, playerLoc, completedTasks)).collect(Collectors.toList());
    }

    public static boolean isLocationTaskComplete(LockedTask task, WorldPoint playerLoc, List<String> completedTasks) {
        if(completedTasks.contains(task.getId())) return false;

        List<WorldPoint> points = task.locations.stream().filter(p -> isLocationValid(playerLoc, p, task.useRegionID)).collect(Collectors.toList());

        return points.size() > 0;
    }

    public static List<LockedTask> checkForLootCompletion(List<ItemStack> loot, List<String> completedTasks) {
        List<LockedTask> completed = new ArrayList<>();

        LockedTask.lootTaskList.forEach(task -> {
            if(task.getCompletionIDs().size() == 0) return;

            List<ItemStack> stacks = loot.stream().filter(stack ->
                task.getCompletionIDs().contains(stack.getId()) && !completedTasks.contains(task.getId())
            ).collect(Collectors.toList());

            if(stacks.size() > 0) completed.add(task);
        });

        return completed;
    }

    public static List<LockedTask> checkForEquipCompletion(List<Item> equipment, WorldPoint playerLoc, List<String> completedTasks) {
        List<Integer> equipmentIDs = equipment.stream().map(i -> i.getId()).collect(Collectors.toList());

        return equipTaskList.stream().filter(t -> {
            if (t.getCompletionIDs().size() == 0) return false;
            boolean locationValid = true;
            if(t.getLocations().size() > 0) {
                locationValid = t.getLocations().stream().filter(p -> isLocationValid(playerLoc, p, t.useRegionID)).collect(Collectors.toList()).size() > 0;
            }

            boolean hasItem = t.getCompletionIDs().stream()
                    .filter(id -> equipmentIDs.contains(id))
                    .collect(Collectors.toList())
                    .size() > 0;
            return locationValid && hasItem && !completedTasks.contains(t.getId());
        }).collect(Collectors.toList());
    }

    public static List<LockedTask> checkForKillCompletion(NPC npc, List<String> completedTasks) {
        return killTaskList.stream().filter(t -> !completedTasks.contains(t.getId()) && killedCorrectMonster(t, npc)).collect(Collectors.toList());
    }

    public static boolean killedCorrectMonster(LockedTask task, NPC npc) {
        if(task.locations == null || task.locations.size() <= 0) return task.getCompletionIDs().contains(npc.getId());

        List<Integer> validRegionIDs = task.locations.stream().map(p -> p.getRegionID()).collect(Collectors.toList());
        boolean correctLoc = validRegionIDs.contains(npc.getWorldLocation().getRegionID());
        return correctLoc && task.getCompletionIDs().contains(npc.getId());
    }

    public static List<LockedTask> checkForCreateCompletion(Client client, Skill skill, int xpGained, List<String> completedTasks) {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();

        if(xpGained == 0) return Arrays.asList();

        return creationTaskList.stream().filter(t -> {
            if (completedTasks.contains(t.getId())) return false;

            if(t.getSkill() != skill) return false;

            if (creationSkills.contains(skill)) {
                return checkForCreatedItem(t, inventory, playerLoc, t.getCompletionIDs(), xpGained, t.getGainedXP(), t.getLocations());
            }
            else if(skill == Skill.AGILITY) {
                if(t.getLocations().size() <= 0) return false;
                return checkForLapCompletion(playerLoc, xpGained, t.getGainedXP(), t.getLocations().get(0));
            }
            else return false;
        }).collect(Collectors.toList());
    }

    private static boolean checkForCreatedItem(LockedTask task, ItemContainer inventory, WorldPoint playerLoc, List<Integer> itemIDs, int xpGained, double xpTarget, List<WorldPoint> locations) {
        int itemID = -1;
        if(itemIDs.size() >= 0) itemID = itemIDs.get(0);
        ItemRequirement req = new SingleItemRequirement(itemID);
        double xpDifference = Math.abs(xpGained - xpTarget);
        if(xpDifference <= 0.5 || xpTarget < 0) {
            boolean hasItem = itemID <= 0 || req.fulfilledBy(inventory.getItems());
            log.info("item valid for task "+ task.getDescription());
            if(locations == null && task.useRegionID == false) return hasItem;
            else return playerAtLocation(playerLoc, task, task.useRegionID) && hasItem;
        }

        return false;
    }

    private static boolean playerAtLocation(WorldPoint playerLoc, LockedTask task, boolean checkForRegion) {
        List<WorldPoint> locations = task.getLocations();
        if(locations == null) {
            List<String> regionIDs = Arrays.asList(task.getRegion().getRegions());
            return regionIDs.contains(playerLoc.getRegionID());
        }
        return locations.stream().filter(p -> isLocationValid(playerLoc, p, checkForRegion)).collect(Collectors.toList()).size() > 0;
    }

    private static boolean checkForLapCompletion(WorldPoint playerLoc, int xpGained, double xpTarget, WorldPoint targetLoc) {
        double xpDifference = Math.abs(xpGained - xpTarget);
        return isLocationValid(playerLoc, targetLoc, true) && xpDifference <= 0.5;
    }

    public static List<LockedTask> checkForMagicCompletion(int param1, int magicLevel, List<String> completedTasks) {
        return magicTaskList.stream()
                .filter(t -> !completedTasks.contains(t.getId()) && isSpellCast(t, param1, magicLevel))
                .collect(Collectors.toList());
    }

    private static boolean isSpellCast(LockedTask task, int param1, int magicLevel) {
        return task.getCompletionIDs().contains(param1) && magicLevel >= task.getGainedXP();
    }

    public static List<LockedTask> checkForNonNpcLoot(ItemContainer container, WorldPoint playerLoc, List<Item> newItems, List<String> completedTasks) {
        if(specialItemContainers.contains(container.getId())) return checkForChestLoot(container, completedTasks);

        if(newItems.size() <= 0) return null;
        List<Integer> newItemIDs = newItems.stream().map(i -> i.getId()).collect(Collectors.toList());

        return lootTaskList.stream().filter(t -> {
            if(completedTasks.contains(t.getId())) return false;

            boolean hasItem = t.completionIDs.stream().filter(id -> newItemIDs.contains(id)).collect(Collectors.toList()).size() > 0;

            if(t.locations == null) return hasItem;
            boolean locationValid = t.getLocations().stream().filter(p -> isLocationValid(playerLoc, p, t.useRegionID)).collect(Collectors.toList()).size() > 0;
            return locationValid && hasItem;
        }).collect(Collectors.toList());
    }

    private static List<LockedTask> checkForChestLoot(ItemContainer itemContainer, List<String> completedTasks) {
        List<Item> containerItems = Arrays.asList(itemContainer.getItems());
        List<Integer> containerItemIDs = containerItems.stream().map(i -> i.getId()).collect(Collectors.toList());
        log.info(containerItems.toString());

        return lootTaskList.stream().filter(t -> {
            if(completedTasks.contains(t.getId())) return false;

            return t.getCompletionIDs().stream().anyMatch(id -> containerItemIDs.contains(id));
        }).collect(Collectors.toList());
    }

    public static List<LockedTask> checkForLevelCompletion(Skill skill, int skillLevel, List<String> completedTasks) {
        if(skill == Skill.HITPOINTS && skillLevel <= 10) return Arrays.asList();

        return levelTaskList.stream().filter(t -> {
           if(completedTasks.contains(t.getId())) return false;
           if(t.getDescription().contains("total level") && skillLevel < 100) return false;

           if(t.skill == Skill.OVERALL || skill == t.getSkill()) {
               return skillLevel >= t.gainedXP;
           }

           return false;
        }).collect(Collectors.toList());
    }

    public static List<LockedTask> checkForQuestCompletion(Client client, List<String> completedTasks) {
        return questTaskList.stream().filter(t -> {
            if(completedTasks.contains(t.getId()) || t.getQuest() == null) return false;

            return t.getQuest().getState(client) == QuestState.FINISHED;
        }).collect(Collectors.toList());
    }

    private static boolean isLocationValid(WorldPoint playerLoc, WorldPoint targetLoc, boolean useRegionID) {
        if(useRegionID) return playerLoc.getRegionID() == targetLoc.getRegionID();
        else return playerLoc.distanceTo(targetLoc) <= 0;
    }
}