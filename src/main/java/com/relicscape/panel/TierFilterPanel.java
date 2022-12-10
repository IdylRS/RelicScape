package com.relicscape.panel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import com.relicscape.RelicScapePlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

@Slf4j
public class TierFilterPanel extends FilterButtonPanel
{

    public TierFilterPanel(RelicScapePlugin plugin, SpriteManager spriteManager)
    {
        super(plugin);
        this.configKey = "tierFilter";
        this.tooltipPrefix = "Tier";

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

        buttonPanel.setLayout(new GridLayout(2, 3));

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

        for (int i=0;i<6;i++)
        {
            String tierIcon = "tier"+(i+1)+".png";
            tierImage = ImageUtil.loadImageResource(RelicScapePlugin.class, tierIcon);

            images.put((i+1)+"", tierImage);
        }

        return images;
    }
}
