package com.map.definition;

import lombok.Getter;

/**
 * Contains data that defines a game region on the menu.
 * This information is loaded directly from a JSON file.
 * @author Antipixel
 */
@Getter
public class RegionDefinition
{
	private int id;
	private String name;
	private IconDefinition icon;
	private boolean unlocked;
	private int indexSprite;
	private int mapSprite;
	private int cost;

	public boolean setUnlocked(boolean unlocked) { return this.unlocked = unlocked; }
}
