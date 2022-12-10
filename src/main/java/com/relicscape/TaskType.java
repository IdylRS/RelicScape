package com.relicscape;

import java.util.List;

public enum TaskType {
    EQUIP,
    LOOT,
    ENTER,
    KILL,
    CREATE,
    MAGIC,
    LEVEL;

    private List<LockedTask> tasks;
}
