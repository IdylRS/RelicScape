package com.relicscape.panel;

import com.relicscape.LockedTask;
import com.relicscape.RelicScapePlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.awt.*;
import java.util.ArrayList;

@Slf4j
public class RelicScapePluginPanel extends PluginPanel {
    private final RelicScapePlugin plugin;
    private final ClientThread clientThread;
    private final SpriteManager spriteManager;

    public final ArrayList<TaskPanel> taskPanels = new ArrayList<>();
    private final JScrollPane listContainer = new JScrollPane();
    private final JLabel emptyTasks = new JLabel();
    private final JPanel titlePanel = new JPanel();
    private final TaskListListPanel taskList;
    private final JToggleButton collapseBtn = new JToggleButton();
    private SubFilterPanel subFilterPanel;

    private final BufferedImage collapseImg = ImageUtil.loadImageResource(RelicScapePlugin.class, "filter_menu_collapsed.png");
    private final Icon MENU_COLLAPSED_ICON = new ImageIcon(ImageUtil.alphaOffset(collapseImg, -180));
    private final Icon MENU_ICON_HOVER = new ImageIcon(collapseImg);
    private final Icon MENU_EXPANDED_ICON = new ImageIcon(ImageUtil.loadImageResource(RelicScapePlugin.class, "filter_menu_expanded.png"));

    public RelicScapePluginPanel(RelicScapePlugin plugin, ClientThread clientThread, SpriteManager spriteManager)
    {
        super(false);
        this.plugin = plugin;
        this.clientThread = clientThread;
        this.spriteManager = spriteManager;

        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        taskList = new TaskListListPanel();

        listContainer.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listContainer.setViewportView(taskList);

        add(getNorthPanel(), BorderLayout.NORTH);
        add(listContainer, BorderLayout.CENTER);
    }

    public void redraw() {
        taskList.redraw();
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(PANEL_WIDTH + SCROLLBAR_WIDTH, super.getPreferredSize().height);
    }

    private class TaskListListPanel extends FixedWidthPanel
    {
        public TaskListListPanel()
        {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(0, 10, 10, 10));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            emptyTasks.setBorder(new EmptyBorder(10,0,10,0));
            emptyTasks.setText("<html><center>" + "No tasks to display." + "</center></html>");
            emptyTasks.setFont(FontManager.getRunescapeSmallFont());
            emptyTasks.setHorizontalAlignment(JLabel.CENTER);
            emptyTasks.setVerticalAlignment(JLabel.CENTER);
            add(emptyTasks);
            emptyTasks.setVisible(false);
        }

        public void redraw()
        {
            removeAll();
            taskPanels.clear();
            add(emptyTasks);
            emptyTasks.setVisible(false);

            log.debug(" Creating panels...");
            List<LockedTask> tasks = LockedTask.getAllTasks();
            tasks.sort((t1, t2) -> {
                String region1 = t1.getRegion().name;
                String region2 = t2.getRegion().name;
                int comparison = region1.compareTo(region2);
                int tierCompare = Integer.compare(t1.getTier(), t2.getTier());
                return comparison > 0 ?
                        1 : comparison == 0 ?
                        tierCompare == 0 ?
                        t1.getDescription().compareTo(t2.getDescription()) : tierCompare : -1;
            });
            if (tasks == null || tasks.size() == 0)
            {
                emptyTasks.setVisible(true);
                return;
            }
            for (LockedTask task : tasks)
            {
                if(isTaskFiltered(task)) continue;
                TaskPanel taskPanel;
                if(plugin.getUnlockData() != null) {
                    taskPanel = new TaskPanel(spriteManager, task, plugin.getUnlockData().getTasks(), clientThread);
                }
                else {
                    taskPanel = new TaskPanel(spriteManager, task, clientThread);
                }
                add(taskPanel);
                taskPanels.add(taskPanel);
            }
            log.debug("Validated and repaint...");
            validate();
            repaint();
        }
    }

    private JPanel getNorthPanel()
    {
        JPanel northPanel = new JPanel();
        BoxLayout layout = new BoxLayout(northPanel, BoxLayout.Y_AXIS);
        northPanel.setLayout(layout);
        northPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Toggle completed tasks
        JCheckBox toggle = new JCheckBox("Hide Completed Tasks");
        toggle.setSelected(!plugin.getConfig().showCompletedTasks());
        toggle.addActionListener(e -> {
            plugin.getConfigManager().setConfiguration(RelicScapePlugin.CONFIG_KEY, "showCompletedTasks", !toggle.isSelected());
            plugin.redrawPanel();
        });


        // Wrapper for collapsible sub-filter menu
        JPanel subFilterWrapper = new JPanel();
        subFilterWrapper.setLayout(new BorderLayout());
        subFilterWrapper.setBorder(new MatteBorder(1, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR));
        subFilterWrapper.setAlignmentX(LEFT_ALIGNMENT);
        subFilterWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // collapse button
        SwingUtil.removeButtonDecorations(collapseBtn);
        collapseBtn.setIcon(MENU_COLLAPSED_ICON);
        collapseBtn.setSelectedIcon(MENU_EXPANDED_ICON);
        collapseBtn.setRolloverIcon(MENU_ICON_HOVER);
        SwingUtil.addModalTooltip(collapseBtn, "Collapse filters", "Expand filters");
        collapseBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        collapseBtn.setAlignmentX(LEFT_ALIGNMENT);
        collapseBtn.setUI(new BasicButtonUI()); // substance breaks the layout
        collapseBtn.addActionListener(ev -> subFilterPanel.setVisible(!subFilterPanel.isVisible()));
        collapseBtn.setHorizontalTextPosition(JButton.CENTER);
        collapseBtn.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        collapseBtn.setFont(FontManager.getRunescapeSmallFont());

        // filter button
        SwingUtil.removeButtonDecorations(collapseBtn);
        collapseBtn.setIcon(MENU_COLLAPSED_ICON);
        collapseBtn.setSelectedIcon(MENU_EXPANDED_ICON);

        // panel to hold sub-filters
        subFilterPanel = new SubFilterPanel(plugin, spriteManager);

        subFilterWrapper.add(collapseBtn, BorderLayout.NORTH);
        subFilterWrapper.add(subFilterPanel, BorderLayout.CENTER);

        northPanel.add(getTitleAndButtonPanel());
        northPanel.add(Box.createVerticalStrut(10));
        northPanel.add(Box.createVerticalStrut(2));
        northPanel.add(Box.createVerticalStrut(5));
        northPanel.add(toggle);
        northPanel.add(subFilterWrapper);

        return northPanel;
    }

    private JPanel getTitleAndButtonPanel()
    {
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        titlePanel.setPreferredSize(new Dimension(0, 30));
        titlePanel.setBorder(new EmptyBorder(0, 5, 5, 10));

        JLabel title = new JShadowedLabel("RelicScape Tasks");
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setHorizontalAlignment(SwingConstants.LEFT);
        title.setForeground(Color.WHITE);

        // Filter button bar
        final JPanel viewControls = new JPanel();
        viewControls.setLayout(new BoxLayout(viewControls, BoxLayout.X_AXIS));
        viewControls.setBackground(ColorScheme.DARK_GRAY_COLOR);

        titlePanel.add(viewControls, BorderLayout.EAST);
        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.setAlignmentX(LEFT_ALIGNMENT);

        return titlePanel;
    }

    private boolean isTaskFiltered(LockedTask task) {
        List<String> tiers = Arrays.asList(plugin.getConfig().tierFilter().split(","));
        List<String> regions = Arrays.asList(plugin.getConfig().regionFilter().split(","));
        boolean hideCompleted = false;
        if(plugin.getUnlockData() != null) {
            hideCompleted = !plugin.getConfig().showCompletedTasks() && plugin.getUnlockData().getTasks().contains(task.getId());
        }

        return !tiers.contains(task.getTier()+"") || !regions.contains(task.getRegion().name) || hideCompleted;
    }
}


