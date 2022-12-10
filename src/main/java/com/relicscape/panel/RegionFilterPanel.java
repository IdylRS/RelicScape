package com.relicscape.panel;

import com.map.sprites.SpriteDefinition;
import com.relicscape.RelicScapePlugin;
import com.relicscape.TrailblazerRegion;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;

@Slf4j
public class RegionFilterPanel extends FilterButtonPanel
{

    public RegionFilterPanel(RelicScapePlugin plugin, SpriteManager spriteManager)
    {
        super(plugin);
        this.configKey = "regionFilter";

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        redraw();
    }

    @Override
    protected JPanel makeButtonPanel()
    {
        // Panel that holds skill icons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        LinkedHashMap<String, BufferedImage> skillImages = getIconImages();

        buttonPanel.setLayout(new GridLayout(4, 3));

        // For each skill on the in-game skill panel, create a button and add it to the UI
        skillImages.forEach((name, image) -> {
            JToggleButton button = makeButton(name, image);
            buttons.put(name, button);
            buttonPanel.add(button);
        });

        return buttonPanel;
    }

    @Override
    protected LinkedHashMap<String, BufferedImage> getIconImages()
    {
        LinkedHashMap<String, BufferedImage> images = new LinkedHashMap<>();
        BufferedImage tierImage;

        for (TrailblazerRegion region : TrailblazerRegion.values())
        {
            String name = region.name.replace(" ","_");
            String regionIcon = "Icon_"+name+"_Hover.png";
            tierImage = ImageUtil.loadImageResource(SpriteDefinition.class, regionIcon);

            images.put(region.name, tierImage);
        }

        return images;
    }
}
