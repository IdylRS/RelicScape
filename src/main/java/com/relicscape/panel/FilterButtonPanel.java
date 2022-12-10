package com.relicscape.panel;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.relicscape.RelicScapePlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

public abstract class FilterButtonPanel extends FixedWidthPanel
{
    protected final RelicScapePlugin plugin;

    protected final Map<String, JToggleButton> buttons = new HashMap<>();
    protected String configKey;
    protected String tooltipPrefix = "";


    public FilterButtonPanel(RelicScapePlugin plugin)
    {
        this.plugin = plugin;

    }

    protected abstract LinkedHashMap<String, BufferedImage> getIconImages();

    protected abstract JPanel makeButtonPanel();

    protected JToggleButton makeButton(String name, BufferedImage image)
    {
        JToggleButton button = new JToggleButton();
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setBorder(new EmptyBorder(2, 0, 2, 0));

        if(image != null) {
            ImageIcon selectedIcon = new ImageIcon(image);
            ImageIcon deselectedIcon = new ImageIcon(ImageUtil.alphaOffset(image, -180));

            button.setIcon(deselectedIcon);
            button.setSelectedIcon(selectedIcon);
        }

        String prefix = tooltipPrefix.length() > 0 ? tooltipPrefix+" " : "";
        button.setToolTipText(prefix + name);

        button.addActionListener(e -> {
            updateFilterText();
            plugin.redrawPanel();
        });

        button.setSelected(true);

        return button;
    }

    protected JPanel allOrNoneButtons()
    {
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.X_AXIS));
        buttonWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        buttonWrapper.setAlignmentX(JPanel.CENTER_ALIGNMENT);

        JButton all = new JButton("all");
        SwingUtil.removeButtonDecorations(all);
        all.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        all.setFont(FontManager.getRunescapeSmallFont());
        all.addActionListener(e -> {
            setAllSelected(true);
            updateFilterText();
            plugin.redrawPanel();
        });

        JButton none = new JButton("none");
        SwingUtil.removeButtonDecorations(none);
        none.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        none.setFont(FontManager.getRunescapeSmallFont());
        none.addActionListener(e -> {
            setAllSelected(false);
            updateFilterText();
            plugin.redrawPanel();
        });

        JLabel separator = new JLabel("|");
        separator.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);

        buttonWrapper.add(all);
        buttonWrapper.add(separator);
        buttonWrapper.add(none);

        return buttonWrapper;
    }

    protected void updateFilterText()
    {
        String filterText = buttons.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(e -> e.getKey())
                .collect(Collectors.joining(","));

        plugin.getConfigManager().setConfiguration(RelicScapePlugin.CONFIG_KEY, configKey, filterText);
    }

    protected void setAllSelected(boolean state)
    {
        buttons.values().forEach(button -> button.setSelected(state));
    }

    public void redraw()
    {
        assert SwingUtilities.isEventDispatchThread();

        buttons.clear();
        removeAll();

        add(makeButtonPanel(), BorderLayout.CENTER);
        add(allOrNoneButtons(), BorderLayout.SOUTH);
        updateFilterText();

        validate();
        repaint();
    }
}
