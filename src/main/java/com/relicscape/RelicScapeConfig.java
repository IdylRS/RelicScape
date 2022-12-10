package com.relicscape;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("relicscape")
public interface RelicScapeConfig extends Config
{
	@ConfigSection(
			name = "General",
			description = "General settings for the plugin",
			position = 0
	)
	String generalSettings = "generalSettings";

	@ConfigSection(
			name = "Environment Looks",
			description = "Settings relating to locked regions look",
			position = 1
	)
	String environmentSettings = "environmentSettings";

	@ConfigSection(
			name = "Map Settings",
			description = "Settings relating to the map overlay",
			position = 2
	)
	String mapSettings = "mapSettings";

	// General Settings

	@ConfigItem(
			keyName = "showLootbeams",
			name = "Show lootbeams",
			description = "Show Lootbeams when a relic is dropped by a monster",
			position = 1,
			section = generalSettings
	)
	default boolean showLootbeams() { return true; }

	@ConfigItem(
			keyName = "lootbeamColorT1",
			name = "Tier 1 lootbeam color",
			description = "The color of the tier 1 relic lootbeams",
			position = 2,
			section = generalSettings
	)
	default Color lootbeamColorT1() { return Color.WHITE; }

	@ConfigItem(
			keyName = "lootbeamColorT2",
			name = "Tier 2 lootbeam color",
			description = "The color of the tier 2 relic lootbeams",
			position = 2,
			section = generalSettings
	)
	default Color lootbeamColorT2() { return Color.CYAN; }

	@ConfigItem(
			keyName = "lootbeamColorT3",
			name = "Tier 3 lootbeam color",
			description = "The color of the tier 3 relic lootbeams",
			position = 2,
			section = generalSettings
	)
	default Color lootbeamColorT3() { return Color.MAGENTA; }

	@ConfigItem(
			keyName = "playRelicSound",
			name = "Play Relic Sound",
			description = "Play a sound when a relic is obtained",
			position = 3,
			section = generalSettings
	)
	default boolean playRelicSound() { return true; }

	@ConfigItem(
			keyName = "relicSoundID",
			name = "Relic sound ID",
			description = "The ID of the sound to play when a relic is obtained",
			position = 4,
			section = generalSettings
	)
	default int relicSoundID() { return 4212; }

	// Environment Looks

	@ConfigItem(
			keyName = "renderLockedRegions",
			name = "Locked chunk shader",
			description = "Adds graphical change to all chunk that are locked",
			position = 21,
			section = environmentSettings
	)
	default boolean renderLockedRegions()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			keyName = "shaderGrayColor",
			name = "Chunk shader color",
			description = "The color of the locked chunks in the shader",
			position = 22,
			section = environmentSettings
	)
	default Color shaderGrayColor()
	{
		return new Color(0, 31, 77, 204);
	}

	@Alpha
	@ConfigItem(
			keyName = "shaderGrayAmount",
			name = "Chunk shader opacity",
			description = "The amount of gray scale that is applied to a locked chunk in the shader (alpha only)",
			position = 23,
			section = environmentSettings
	)
	default Color shaderGrayAmount()
	{
		return new Color(0, 0, 0, 204);
	}

	@ConfigItem(
			keyName = "hardBorder",
			name = "Hard chunk border",
			description = "True = hard border cutoff, False = chunk border gradient",
			position = 24,
			section = environmentSettings
	)
	default boolean hardBorder()
	{
		return true;
	}

	@ConfigItem(
			keyName = "renderRegionBorders",
			name = "Draw chunk border lines",
			description = "Draw the chunk borders in the environment marked by lines",
			position = 25,
			section = environmentSettings
	)
	default boolean renderRegionBorders()
	{
		return false;
	}

	@ConfigItem(
			keyName = "regionBorderWidth",
			name = "Chunk border width",
			description = "How wide the region border will be",
			position = 26,
			section = environmentSettings
	)
	default int regionBorderWidth()
	{
		return 1;
	}

	@Alpha
	@ConfigItem(
			keyName = "regionBorderColor",
			name = "Chunk border color",
			description = "The color of the chunk borders",
			position = 27,
			section = environmentSettings
	)
	default Color regionBorderColor()
	{
		return new Color(0, 200, 83, 200);
	}

	// Map Settings

	@ConfigItem(
			keyName = "drawMapOverlay",
			name = "Draw chunks on map",
			description = "Draw a color overlay for each locked/unlocked chunk",
			position = 28,
			section = mapSettings
	)
	default boolean drawMapOverlay()
	{
		return true;
	}

	@ConfigItem(
			keyName = "invertMapOverlay",
			name = "Invert map overlay",
			description = "Switches which chunks the map will draw the color overlay for (true = locked, false = unlocked)",
			position = 29,
			section = mapSettings
	)
	default boolean invertMapOverlay()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			keyName = "mapOverlayColor",
			name = "Map overlay color",
			description = "The color the map overlay will draw the chunks in",
			position = 30,
			section = mapSettings
	)
	default Color mapOverlayColor()
	{
		return new Color(200, 16, 0, 100);
	}

	@Alpha
	@ConfigItem(
			keyName = "unlockableOverlayColor",
			name = "Unlockable overlay color",
			description = "The color the map overlay will draw the unlockable chunks in",
			position = 31,
			section = mapSettings
	)
	default Color unlockableOverlayColor()
	{
		return new Color(60, 200, 160, 100);
	}

	@Alpha
	@ConfigItem(
			keyName = "blacklistedOverlayColor",
			name = "Blacklisted overlay color",
			description = "The color the map overlay will draw the blacklisted chunks in",
			position = 32,
			section = mapSettings
	)
	default Color blacklistedOverlayColor()
	{
		return new Color(0, 0, 0, 200);
	}

	@ConfigItem(
			keyName = "drawMapGrid",
			name = "Draw map grid",
			description = "Draw the grid of chunks on the map",
			position = 33,
			section = mapSettings
	)
	default boolean drawMapGrid()
	{
		return true;
	}

	@ConfigItem(
			keyName = "drawRegionId",
			name = "Draw region IDs",
			description = "Draw the chunk ID for each chunk on the map",
			position = 34,
			section = mapSettings
	)
	default boolean drawRegionId()
	{
		return true;
	}

	@ConfigItem(
			keyName = "tierFilter",
			name = "Tier Filter",
			description = "Filter by tier in the plugin panel",
			hidden = true
	)
	default String tierFilter() { return "1,2,3,4,5"; }

	@ConfigItem(
			keyName = "regionFilter",
			name = "Region Filter",
			description = "Filter by region in the plugin panel",
			hidden = true
	)
	default String regionFilter() { return "Misthalin,Desert,Kandarin,Asgarnia,Fremennik,Tirannwn,Karamja,Morytania,Wilderness,Lower Zeah,Upper Zeah,Kebos"; }

	@ConfigItem(
			keyName = "showCompletedTasks",
			name = "Show Completed Tasks",
			description = "Show completed tasks in the plugin panel",
			hidden = true
	)
	default boolean showCompletedTasks() { return true; }
}