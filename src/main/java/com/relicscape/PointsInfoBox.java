package com.relicscape;

import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PointsInfoBox extends InfoBox {

    RelicScapePlugin plugin;

    PointsInfoBox(RelicScapePlugin plugin, BufferedImage img) {
        super(img, plugin);
        this.plugin = plugin;
    }

    @Override
    public String getText() {
        return plugin.getUnlockData().getPoints()+"";
    }

    @Override
    public Color getTextColor() {
        int points = plugin.getUnlockData().getPoints();

        if(points == 0) {
            return Color.RED;
        }
        if(points < 100) {
            return Color.YELLOW;
        }
        else {
            return Color.GREEN;
        }
    }
}
