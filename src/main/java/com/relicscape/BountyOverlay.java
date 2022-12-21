package com.relicscape;

import net.runelite.api.MenuAction;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class BountyOverlay extends OverlayPanel {
    public static OverlayMenuEntry SKIP_ENTRY = new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, "Skip", "Bounty");

    private RelicScapePlugin plugin;
    private RelicScapeConfig config;

    private LineComponent bountyComponent;

    @Inject
    private BountyOverlay(RelicScapePlugin plugin, RelicScapeConfig config) {
        this.plugin = plugin;
        this.config = config;
        getMenuEntries().add(SKIP_ENTRY);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.BOTTOM_LEFT);

        TitleComponent title = TitleComponent.builder().text("Current Bounty").build();
        bountyComponent = LineComponent.builder().build();
        panelComponent.getChildren().add(title);
        panelComponent.getChildren().add(bountyComponent);

        setClearChildren(false);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if(plugin.getActiveBounty() == null) return null;

        graphics.setFont(FontManager.getRunescapeFont());
        bountyComponent.setRightColor(Color.GREEN);
        bountyComponent.setLeftColor(Color.WHITE);
        bountyComponent.setRight(plugin.getActiveBounty().getTimeRemaining());
        bountyComponent.setLeft(plugin.getActiveBounty().getNpcName()+" (T"+plugin.getActiveBounty().getTier()+")");
        return super.render(graphics);
    }
}
