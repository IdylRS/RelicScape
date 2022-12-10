package com.relicscape;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;

public class Relic {
    private static final int RELIC_GROUND_ID = 25511;

    private Client client;
    private LocalPoint location;
    @Getter
    private int tier;

    private RuneLiteObject obj;

    public Relic(int tier) {
        this.tier = tier;
    }

    public Relic(Client client, LocalPoint location, int tier) {
        this.location = location;
        this.tier = tier;

        this.obj = client.createRuneLiteObject();
        Model model = client.loadModel(RELIC_GROUND_ID);
        obj.setModel(model);
        obj.setLocation(location, client.getPlane());
        obj.setActive(true);
    }

    public void setActive(boolean active) {
        this.obj.setActive(active);
    }

    public int getValue() {
        switch(this.tier) {
            case 1:
                return 10;
            case 2:
                return 25;
            case 3:
                return 50;
            case 4:
                return 100;
            case 5:
                return 250;
            case 6:
                return 500;
            default:
                return 1;
        }
    }
}
