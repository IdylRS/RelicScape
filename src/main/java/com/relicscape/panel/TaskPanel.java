/*
 * Copyright (c) 2018, Psikoi <https://github.com/Psikoi>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.relicscape.panel;

import java.awt.image.BufferedImage;
import java.util.List;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.map.sprites.SpriteDefinition;
import com.relicscape.LockedTask;
import com.relicscape.RelicScapePlugin;
import com.relicscape.Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Constants;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;
import net.runelite.client.util.ImageUtil;

@Slf4j
class TaskPanel extends JPanel
{
    private RelicScapePlugin plugin;
    private ClientThread clientThread;
    private SpriteManager spriteManager;

    private final JPanel container = new JPanel(new BorderLayout());
    private final JPanel body = new JPanel(new BorderLayout());
    private final JShadowedLabel name = new JShadowedLabel();
    private final JLabel description = new JLabel();
    private final JLabel icon = new JLabel();

    private boolean isComplete = false;

    @Getter
    private final LockedTask task;

    TaskPanel(SpriteManager spriteManager, LockedTask task, ClientThread clientThread)
    {
        super(new BorderLayout());
        this.task = task;
        this.clientThread = clientThread;
        this.spriteManager = spriteManager;
        createPanel();
        ToolTipManager.sharedInstance().registerComponent(this);
        refresh();
    }

    TaskPanel(SpriteManager spriteManager, LockedTask task, List<String> completedTasks, ClientThread clientThread)
    {
        super(new BorderLayout());
        this.task = task;
        this.clientThread = clientThread;
        this.spriteManager = spriteManager;
        this.isComplete = completedTasks.contains(task.getId());
        createPanel();
        ToolTipManager.sharedInstance().registerComponent(this);
        refresh();
    }

    public void createPanel()
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 7, 0));

        container.setBorder(new EmptyBorder(7, 7, 6, 0));

        // Body

        name.setFont(FontManager.getRunescapeSmallFont());
        name.setForeground(Color.WHITE);
        body.add(name, BorderLayout.NORTH);

        description.setFont(FontManager.getRunescapeSmallFont());
        description.setForeground(Color.GRAY);
        body.add(description, BorderLayout.CENTER);

        // Full
        container.add(icon, BorderLayout.WEST);
        container.add(body, BorderLayout.CENTER);

        BufferedImage img = ImageUtil.loadImageResource(SpriteDefinition.class, "Icon_"+task.getRegion().name+"_Hover.png");
        clientThread.invoke(() -> {
            if (img != null)
            {
                icon.setMinimumSize(new Dimension(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT));
                icon.setIcon(new ImageIcon(img));
                icon.setBorder(new EmptyBorder(0, 0, 0, 5));
            }
            else
            {
                icon.setBorder(new EmptyBorder(0, 0, 0, 0));
            }
        });


        add(container, BorderLayout.NORTH);
    }

    public Color getTaskBackgroundColor(LockedTask task)
    {
        return this.isComplete ? new Color(0, 50, 0) : ColorScheme.DARKER_GRAY_COLOR;
    }

    public void refresh()
    {
        description.setText(Util.wrapWithHtml("Tier "+task.getTier()));
        name.setText(Util.wrapWithHtml(task.getDescription()));
        setBackgroundColor(getTaskBackgroundColor(task));
        setVisible(true);
        revalidate();
    }

    private void setBackgroundColor(Color color)
    {
        container.setBackground(color);
        body.setBackground(color);
    }
}