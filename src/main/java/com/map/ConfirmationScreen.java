package com.map;

import com.map.ui.UIButton;
import com.map.ui.UIGraphic;
import com.map.ui.UILabel;
import com.map.ui.UIPage;
import lombok.Getter;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

import java.awt.*;

import static com.relicscape.RelicScapePlugin.*;

public class ConfirmationScreen extends UIPage {
    @Getter
    private Widget window;

    @Getter
    private UILabel confirmAfford;
    @Getter
    private UILabel confirmLabel;
    @Getter
    private UILabel confirmTitle;
    @Getter
    private UIGraphic confirmIcon;
    @Getter
    private UIButton confirmButton;

    public ConfirmationScreen(Widget window) {
        this.window = window;

        int labelHeight = 100;
        int titleHeight = 50;
        final int POS_X = getCenterX(window, REGION_MAP_SPRITE_WIDTH);
        final int POS_Y = getCenterY(window, labelHeight);

        Widget label = window.createChild(-1, WidgetType.TEXT);
        label.setTextColor(Color.WHITE.getRGB());
        label.setTextShadowed(true);
        label.setName("Confirm Label");

        Widget title = window.createChild(-1, WidgetType.TEXT);
        label.setTextColor(Color.WHITE.getRGB());
        label.setTextShadowed(true);
        label.setName("Confirm Title");

        Widget afford = window.createChild(-1, WidgetType.TEXT);
        this.confirmAfford = new UILabel(afford);
        this.confirmAfford.setFont(495);
        this.confirmAfford.setSize(REGION_MAP_SPRITE_WIDTH, titleHeight);
        this.confirmAfford.setPosition(getCenterX(window, REGION_MAP_SPRITE_WIDTH), getCenterY(window, titleHeight)+40);

        Widget buttonWidget = window.createChild(-1, WidgetType.GRAPHIC);
        this.confirmButton = new UIButton(buttonWidget);
        this.confirmButton.setSize(140, 30);
        this.confirmButton.setPosition(getCenterX(window, 140), getCenterY(window, 30) + 75);

        this.confirmLabel = new UILabel(label);
        this.confirmLabel.setFont(495);
        this.confirmLabel.setPosition(POS_X, POS_Y);
        this.confirmLabel.setSize(Math.min(REGION_MAP_SPRITE_WIDTH, window.getWidth()), labelHeight);

        Widget icon = window.createChild(-1, WidgetType.GRAPHIC);
        this.confirmIcon = new UIGraphic(icon);
        this.confirmIcon.setSize(MAP_ICON_WIDTH, MAP_ICON_HEIGHT);
        this.confirmIcon.setPosition(getCenterX(window, MAP_ICON_WIDTH), getCenterY(window, MAP_ICON_HEIGHT)-50);

        this.confirmTitle = new UILabel(title);
        this.confirmTitle.setFont(496);
        this.confirmTitle.setSize(REGION_MAP_SPRITE_WIDTH, titleHeight);
        this.confirmTitle.setPosition(getCenterX(window, REGION_MAP_SPRITE_WIDTH), getCenterY(window, titleHeight)-80);

        this.add(this.confirmTitle);
        this.add(this.confirmLabel);
        this.add(this.confirmButton);
        this.add(this.confirmAfford);
        this.add(this.confirmIcon);
    }

    public void setDescription(String desc) {
        this.confirmLabel.setText(desc);
    }

    public void setAffordText(String desc, Color color) {
        this.confirmAfford.setText(desc);
        this.confirmAfford.setColour(color.getRGB());
    }

    public void setButtonSprite(int spriteID) {
        this.confirmButton.setSprites(spriteID);
    }

    public void setTitle(String title) {
        this.confirmTitle.setText(title);
    }

    public void setIcon(int spriteID) { this.confirmIcon.setSprite(spriteID); }
}
