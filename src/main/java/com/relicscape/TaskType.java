package com.relicscape;

import java.util.List;

public enum TaskType {
    EQUIP,
    LOOT,
    ENTER,
    KILL,
    CREATE,
    MAGIC,
    LEVEL,
    QUEST;

    private List<LockedTask> tasks;
}
