package com.relicscape;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;

import com.map.ConfirmationScreen;
import com.map.definition.IconDefinition;
import com.map.definition.RegionDefinition;
import com.map.sprites.SpriteDefinition;
import com.map.ui.*;
import com.relicscape.regionlocker.RegionBorderOverlay;
import com.relicscape.regionlocker.RegionLocker;
import com.relicscape.regionlocker.RegionLockerOverlay;
import com.relicscape.panel.RelicScapePluginPanel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.MenuAction;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.*;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.api.widgets.WidgetInfo.TO_GROUP;
import static net.runelite.http.api.RuneLiteAPI.GSON;

@Slf4j
@PluginDescriptor(
		name = "RelicScape",
		description = "RelicScape game mode",
		tags = {"leagues", "locked", "relic"},
		conflicts = {"Region Locker"}
)
public class RelicScapePlugin extends Plugin {
	public static final String CONFIG_KEY = "relicscape";
	private static final String CFG_KEY_STATE = "prevState";
	public static final String DATA_FOLDER_NAME = "relicscape";

	/* Packed Widget IDs */
	private static final int WORLD_MAP_VIEW = 38993928;
	private static final int WORLD_MAP_LOCATION = 38993929;
	private static final int WORLD_MAP_BAR = 38993942;
	private static final int WORLD_MAP_BORDER = 38993950;
	private static final int WORLD_MAP_CONTAINER = 38993925;
	private static final int PRAYER_TAB = 35454980;
	private static final int PRAYER_ORB = 10485777;
	private static final int QUICK_PRAYER = 10485779;


	/* Widget dimensions and positions */
	public static final int REGION_MAP_SPRITE_WIDTH = 478;
	public static final int REGION_MAP_SPRITE_HEIGHT = 272;
	public static final int MAP_ICON_WIDTH = 50;
	public static final int MAP_ICON_HEIGHT = 41;

	/* Script, Sprite IDs */
	private static final int REGION_MAP_MAIN = 2721;
	private static final int SOUND_EFFECT_TWINKLE = 98;
	private static final int CONFIRM_BUTTON_SPRITE = -19120;
	private static final int BACK_BUTTON_SPRITE = -19127;
	private static final int LOCK_BUTTON_SPRITE = -19129;

	/* Menu actions */
	private static final String ACTION_TEXT_SELECT = "Select";
	private static final String ACTION_TEXT_CONFIRM = "Confirm";
	private static final String ACTION_TEXT_BACK = "Back";
	private static final String NAME_TEXT_TOGGLE = "Map Mode";

	/* Definition JSON files */
	private static final String DEF_FILE_REGIONS = "RegionDef.json";
	private static final String DEF_FILE_SPRITES = "SpriteDef.json";
	private static final String DEF_FILE_TASKS = "tasks.json";

	/* Sound Effects */
	private static final int SOUND_EFFECT_FAIL = 2277;

	private final int BASE_RELIC_CHANCE = 1000;
	private final Skill[] BLACKLIST = {Skill.SLAYER, Skill.ATTACK, Skill.STRENGTH, Skill.HITPOINTS, Skill.MAGIC, Skill.RANGED, Skill.DEFENCE};

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	@Getter
	private RelicScapeConfig config;

	@Inject
	private RegionLockerOverlay regionLockerOverlay;

	@Inject
	private RegionBorderOverlay regionBorderOverlay;

	@Inject
	@Getter
	private ConfigManager configManager;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Gson gson;

	@Setter(AccessLevel.PACKAGE)
	@Getter
	private int hoveredRegion = -1;

	private RegionLocker regionLocker;

	private RelicScapePluginPanel pluginPanel;

	private RegionDefinition[] regionDefinitions;
	private SpriteDefinition[] spriteDefinitions;

	private Map<String, List<Widget>> skillOverlays;

	@Getter
	private boolean mapEnabled;
	private final List<WorldMapPoint> mapIcons = new ArrayList<>();

	/* Widgets */
	private List<Integer> hiddenWidgetIDs;
	private UIGraphic[] indexRegionGraphics;
	private UILabel regionCount;
	private UILabel prayerLocked;
	private UIButton quickPrayer;

	private UIPage indexPage;
	private ConfirmationScreen confirmPage;

	@Getter
	private UnlockData unlockData;
	private HashMap<Skill, Integer> skillXP;
	private HashMap<Skill, Integer> skillLevels;

	// Player files
	private File playerFile;

	private PointsInfoBox pointsInfoBox;

	private final HashMap<WorldPoint, Lootbeam> lootbeams = new HashMap<>();
	private final HashMap<WorldPoint, Relic> groundItems = new HashMap<>();

	private WorldPoint lastPlayerLoc;

	private List<Item> lastInventoryState;

	@Provides
	RelicScapeConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(RelicScapeConfig.class);
	}

	@Override
	protected void startUp() {
		regionLocker = new RegionLocker(client, config, configManager, this);
		overlayManager.add(regionLockerOverlay);
		overlayManager.add(regionBorderOverlay);
		startMap();

		pluginPanel = new RelicScapePluginPanel(this, this.clientThread, spriteManager);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "panel_icon.png");
		NavigationButton navButton = NavigationButton.builder()
				.tooltip("RelicScape")
				.priority(5)
				.icon(icon)
				.panel(pluginPanel)
				.build();
		clientToolbar.addNavigation(navButton);
		redrawPanel();
	}

	@Override
	protected void shutDown() {
		overlayManager.remove(regionLockerOverlay);
		overlayManager.remove(regionBorderOverlay);
		this.lootbeams.keySet().forEach(this::removeLootbeam);
		this.groundItems.keySet().forEach(this::removeGroundItem);
		skillXP = null;
		shutDownMap();
		RegionLocker.renderLockedRegions = false;
	}

	private void startMap() {
		this.loadDefinitions();
		this.createHiddenWidgetList();

		// Add the custom sprites to the sprite manager
		this.spriteManager.addSpriteOverrides(spriteDefinitions);
	}

	private void shutDownMap() {
		this.regionDefinitions = null;
		this.hiddenWidgetIDs.clear();

		// Remove the custom sprites
		this.spriteManager.removeSpriteOverrides(spriteDefinitions);
	}

	private void initXpTracker() {
		this.skillXP = new HashMap<>();
		this.skillLevels = new HashMap<>();

		Arrays.stream(Skill.values()).forEach(skill -> {
			skillXP.put(skill, -1);
		});
	}

	/**
	 * Loads the definition files
	 */
	private void loadDefinitions() {
		// Load the definitions files for the regions and sprite override
		this.regionDefinitions = loadDefinitionResource(RegionDefinition[].class, DEF_FILE_REGIONS, gson);
		this.spriteDefinitions = loadDefinitionResource(SpriteDefinition[].class, DEF_FILE_SPRITES, gson);

		LockedTask.createTasks(loadDefinitionResource(LockedTask[].class, DEF_FILE_TASKS, gson));
	}

	/**
	 * Loads a definition resource from a JSON file
	 *
	 * @param classType the class into which the data contained in the JSON file will be read into
	 * @param resource  the name of the resource (file name)
	 * @param gson      a reference to the GSON object
	 * @param <T>       the class type
	 * @return the data read from the JSON definition file
	 */
	private <T> T loadDefinitionResource(Class<T> classType, String resource, Gson gson) {
		// Load the resource as a stream and wrap it in a reader
		InputStream resourceStream = classType.getResourceAsStream(resource);
		assert resourceStream != null;
		InputStreamReader definitionReader = new InputStreamReader(resourceStream);

		// Load the objects from the JSON file
		return gson.fromJson(definitionReader, classType);
	}

	/**
	 * Sets up the playerFile variable, and makes the player file if needed.
	 */
	private void setupPlayerFile() {
		unlockData = new UnlockData();
		File playerFolder = new File(RuneLite.RUNELITE_DIR, DATA_FOLDER_NAME);
		if (!playerFolder.exists()) {
			playerFolder.mkdirs();
		}
		playerFile = new File(playerFolder, client.getAccountHash() + ".txt");
		if (!playerFile.exists()) {
			try {
				playerFile.createNewFile();
				unlockDefaults();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			loadPlayerData();
		}
	}

	private void loadPlayerData() {
		unlockData.clearAll();
		try {
			String json = new Scanner(playerFile).useDelimiter("\\Z").next();
			unlockData = GSON.fromJson(json, new TypeToken<UnlockData>() {}.getType());
			regionLocker.updateRegions();
			SwingUtilities.invokeLater(this::redrawPanel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void savePlayerData() {
		try {
			PrintWriter w = new PrintWriter(playerFile);
			String json = GSON.toJson(unlockData);
			w.println(json);
			w.close();
			regionLocker.updateRegions();
			log.debug("Saving player data");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void unlockDefaults() {
		addPoints(1500);
		savePlayerData();
	}

	private void unlockSkill(RelicScapeSkill skill) {
		if(skill.getUnlockCost() > unlockData.getPoints()) {
			sendFailMessage("You need "+skill.getUnlockCost()+" points to unlock "+skill.getName()+".");
			return;
		}
		else if(unlockData.getSkills().size() >= config.maxSkillUnlocks()) {
			sendFailMessage("You have used all "+config.maxSkillUnlocks()+" of your skill unlocks. You can re-lock a skill by right clicking it.");
			return;
		}

		unlockData.addSkill(skill.getName());
		subtractPoints(skill.getUnlockCost());
		savePlayerData();
		updateSkillOverlay(skill, true);
		this.client.playSoundEffect(SOUND_EFFECT_TWINKLE);

		int skillSlots = unlockData.getSkills().size();
		client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"You unlocked the "+skill.getName()+" skill! You have used "+skillSlots+"/"+config.maxSkillUnlocks()+" skill unlocks.",
				null
		);

		if(skill == RelicScapeSkill.PRAYER) {
			showPrayers();
		}
	}

	public boolean unlockArea(TrailblazerRegion area, boolean force) {
		int regionSlots = unlockData.getAreas().size();
		if(area.getUnlockCost() > unlockData.getPoints() && !force) {
			sendFailMessage("You need "+area.getUnlockCost()+" points to unlock "+area.getName()+".");
			return false;
		}
		else if(regionSlots >= config.maxRegionUnlocks()) {
			sendFailMessage("You have unlocked "+unlockData.getAreas().size()+"/"+config.maxRegionUnlocks()+" regions. You must re-lock a region before unlocking a new one.");
			return false;
		}

		unlockData.addArea(area);
		savePlayerData();
		subtractPoints(area.getUnlockCost());

		client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"You unlocked the "+area.getName()+" region! You have used "+regionSlots+"/"+config.maxRegionUnlocks()+" region unlocks.",
				null
		);

		this.regionDefinitions[area.regionID].setUnlocked(true);
		this.client.playSoundEffect(SOUND_EFFECT_TWINKLE);

		return true;
	}

	private void lockSkill(RelicScapeSkill skill) {
		unlockData.removeSkill(skill);
		savePlayerData();
		this.client.playSoundEffect(1228);
		updateSkillOverlay(skill, false);

		int skillSlots = unlockData.getSkills().size();
		client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"You locked the "+skill.getName()+" skill. You have used "+skillSlots+"/"+config.maxSkillUnlocks()+" skill unlocks.",
				null
		);

		if(skill == RelicScapeSkill.PRAYER) {
			hidePrayers();
		}
	}

	private void lockRegion(RegionDefinition region) {
		unlockData.removeArea(stringToTrailblazer(region.getName()));
		region.setUnlocked(false);
		this.indexRegionGraphics[region.getId()].setOpacity(0);
		savePlayerData();
		this.client.playSoundEffect(1228);


		int regionSlots = unlockData.getAreas().size();
		client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"You locked the "+region.getName()+" region. You have used "+regionSlots+"/"+config.maxRegionUnlocks()+" region unlocks.",
				null
		);
	}

	private void updateSkillOverlay(RelicScapeSkill skill, boolean hide) {
		if(this.skillOverlays == null) return;

		List<Widget> overlay = this.skillOverlays.get(skill.getName());
		if(overlay != null && overlay.size() > 0) {
			overlay.forEach(widget -> widget.setHidden(hide));
		}
	}

	private void addPoints(int amount) {
		unlockData.addPoints(amount);
	}

	private void subtractPoints(int amount) {
		unlockData.subtractPoints(amount);
		savePlayerData();
	}

	private void awardRelic(Relic relic, boolean sendMessage, boolean playSound) {
		addPoints(relic.getValue());
		if(playSound && config.playRelicSound()) client.playSoundEffect(config.relicSoundID());

		if(sendMessage) {
			final ChatMessageBuilder message = new ChatMessageBuilder()
					.append(ChatColorType.HIGHLIGHT)
					.append("You found a tier "+relic.getTier()+" relic worth "+relic.getValue()+" points!")
					.append(ChatColorType.NORMAL);

			chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.ITEM_EXAMINE)
					.runeLiteFormattedMessage(message.build())
					.build());
		}
	}

	private void hidePrayers() {
		client.getWidget(QUICK_PRAYER).setHidden(true);
		client.getWidget(PRAYER_TAB).setHidden(true);
		prayerLocked.setVisibility(true);
		quickPrayer.setVisibility(true);
	}

	private void showPrayers() {
		client.getWidget(QUICK_PRAYER).setHidden(false);
		client.getWidget(PRAYER_TAB).setHidden(false);
		prayerLocked.setVisibility(false);
		quickPrayer.setVisibility(false);
	}

	/**
	 * Creates a list of widgets that the plugin does not require
	 * in order to function, for them to be hidden and shown as required
	 */
	private void createHiddenWidgetList() {
		this.hiddenWidgetIDs = new ArrayList<>();

		this.hiddenWidgetIDs.add(WORLD_MAP_VIEW);
		this.hiddenWidgetIDs.add(WORLD_MAP_BORDER);
		this.hiddenWidgetIDs.add(WORLD_MAP_BAR);
		this.hiddenWidgetIDs.add(WORLD_MAP_LOCATION);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		// World map loaded
		if (e.getGroupId() == 595) {
			// The main window layer
			Widget window = this.client.getWidget(WORLD_MAP_CONTAINER);

			this.updateDisplayedMenu();

			// Create the page objects, onto which the UI
			// components will be placed
			this.createMenuPages(window);

			// Create the custom widgets
			assert window != null;
			this.createBackButton(window);
			this.createRegionCount(window);
			this.createIndexMenu(window);
			this.createConfirmationMenu();
			this.createToggleCheckbox(window);
			this.createBackButton(window);

			this.updateMapState();
		}
		// Skill menu loaded
		else if(e.getGroupId() == 320) {
			this.createLockedSkillOverlays();
		}
		// Prayer menu loaded
		else if(e.getGroupId() == 160 && quickPrayer == null) {
			Widget prayerOrb = client.getWidget(PRAYER_ORB);
			Widget orbWidget = prayerOrb.createChild(-1, WidgetType.GRAPHIC);

			quickPrayer = new UIButton(orbWidget);
			quickPrayer.setSize(prayerOrb.getWidth(), prayerOrb.getHeight());
			quickPrayer.addAction("Disabled", () -> client.playSoundEffect(SOUND_EFFECT_FAIL));
			quickPrayer.setVisibility(false);
		}
		else if(e.getGroupId() == 541) {
			Widget container = client.getWidget(35454976);
			Widget prayerLabel = container.createChild(-1, WidgetType.TEXT);
			prayerLocked = new UILabel(prayerLabel);
			prayerLocked.setText("You must unlock the Prayer skill to use your prayers.");
			prayerLocked.setColour(ColorScheme.BRAND_ORANGE.getRGB());
			prayerLocked.setSize(150, 75);
			prayerLocked.setPosition(getCenterX(container, 150), getCenterY(container, 75));
			prayerLocked.setVisibility(false);

			if(unlockData.getSkills().contains(Skill.PRAYER.getName())) return;
			hidePrayers();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(RelicScapePlugin.CONFIG_KEY)) {
			return;
		}

		if(event.getKey().equals("showLootbeams") || event.getKey().equals("lootbeamColor")) {
			clientThread.invokeLater(this::handleLootbeams);
		}

		regionLocker.readConfig();
	}

	@Subscribe
	public void onMenuEntryAdded(final MenuEntryAdded event)
	{
		int widgetID = event.getActionParam1();

		if(event.getOption().startsWith("Walk here")) {
			Tile tile = client.getSelectedSceneTile();
			if(tile == null) return;

			Relic relic = groundItems.get(tile.getWorldLocation());
			if(relic == null) return;

			Color color = relic.getTier() == 3 ? config.lootbeamColorT3() : relic.getTier() == 2 ? config.lootbeamColorT2() : config.lootbeamColorT1();
			String hex = String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

			client.createMenuEntry(-1)
					.setTarget("<col="+hex+">Relic (T"+relic.getTier()+")</col>")
					.setOption("Take")
					.setType(MenuAction.GROUND_ITEM_FIRST_OPTION)
					.onClick(e -> pickUpRelic(tile.getWorldLocation()));
		}

		else if (TO_GROUP(widgetID) == WidgetID.SKILLS_GROUP_ID && event.getOption().startsWith("View")) {
			// Get skill from menu option, eg. "View <col=ff981f>Attack</col> guide"
			final String skillText = event.getOption().split(" ")[1];
			final RelicScapeSkill skill = RelicScapeSkill.valueOf(Text.removeTags(skillText).toUpperCase());

			if (unlockData.getSkills().contains(skill.getName())) {
				client.createMenuEntry(-1)
						.setTarget(skillText)
						.setOption("Re-lock")
						.setType(MenuAction.RUNELITE)
						.onClick(e -> lockSkill(skill));
			}
			else {
				client.createMenuEntry(-1)
						.setTarget(skillText)
						.setOption("Unlock for " + skill.getUnlockCost())
						.setType(MenuAction.RUNELITE)
						.onClick(e -> unlockSkill(skill));
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGGED_IN) {
			setupPlayerFile();
			handleLootbeams();

			if(skillXP == null) {
				initXpTracker();
			}

			if(pointsInfoBox == null) {
				BufferedImage icon = itemManager.getImage(999);
				pointsInfoBox = new PointsInfoBox(this, icon);
				pointsInfoBox.setTooltip("RelicScape Points");
				infoBoxManager.addInfoBox(pointsInfoBox);
			}
		}
		else if(event.getGameState() == GameState.LOGIN_SCREEN) {
			skillXP = null;
			unlockData = null;
			infoBoxManager.removeInfoBox(pointsInfoBox);
			pointsInfoBox = null;
		}
	}

	@Subscribe
	public void onNpcLootReceived(NpcLootReceived event) {
		NPC npc = event.getNpc();
		ArrayList<ItemStack> loot = new ArrayList<>(event.getItems());

		if(npc == null) return;

		rollForRelicDrop(npc, loot.get(0).getLocation());

		List<LockedTask> completedTasks = LockedTask.checkForLootCompletion(loot, unlockData.getTasks());
		completedTasks.addAll(LockedTask.checkForKillCompletion(npc, unlockData.getTasks()));

		completedTasks.forEach(this::completeTask);

		if(completedTasks.size() > 0) savePlayerData();
	}

	@Subscribe
	public void onStatChanged(StatChanged event) {
		Skill skill = event.getSkill();
		int newXp = event.getXp();
		int skillLevel = client.getRealSkillLevel(skill);

		if(skillXP.get(skill) < 0) {
			skillXP.put(skill, newXp);
			skillLevels.put(skill, client.getRealSkillLevel(skill));

			List<LockedTask> completedTasks = LockedTask.checkForLevelCompletion(skill, skillLevel, client.getTotalLevel(), unlockData.getTasks());
			completedTasks.forEach(this::completeTask);

			if(completedTasks.size() > 0) savePlayerData();

			return;
		}

		int xpGained = newXp - skillXP.get(skill);
		int levelsGained = skillLevel - skillLevels.get(skill);
		skillXP.put(skill, newXp);
		skillLevels.put(event.getSkill(), skillLevel);

		if(!unlockData.getSkills().contains(skill.getName()) && xpGained > 0) {
			final ChatMessageBuilder message = new ChatMessageBuilder()
					.append(ChatColorType.HIGHLIGHT)
					.append("Illegal XP gained! You have not unlocked "+skill.getName() +".")
					.append(ChatColorType.NORMAL);

			chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.CONSOLE)
					.runeLiteFormattedMessage(message.build())
					.build());
			return;
		}

		updateSkill(skill, xpGained);
		List<LockedTask> completedTasks = LockedTask.checkForCreateCompletion(client, skill, xpGained, unlockData.getTasks());
		if(levelsGained > 0) {
			completedTasks.addAll(LockedTask.checkForLevelCompletion(skill, skillLevel, client.getTotalLevel(), unlockData.getTasks()));
		}
		completedTasks.forEach(this::completeTask);

		if(completedTasks.size() > 0) savePlayerData();
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		String menuOption = event.getMenuOption();
		String menuTarget = Text.removeTags(event.getMenuTarget());

		if(menuOption.startsWith("Open") && menuTarget.startsWith("Reward casket")) {
			String casketTier = menuTarget.split("Reward casket")[1].replaceAll("\\(|\\)", "").trim();
			Relic relic = rollForRelicCasket(casketTier);

			log.info("Opened a "+casketTier+" casket.");

			if(relic != null) {
				awardRelic(relic, true, true);
				savePlayerData();
			}
		}
		else if (menuOption.startsWith("Cast")){
			int magicLevel = client.getRealSkillLevel(Skill.MAGIC);
			List<LockedTask> completedTasks = LockedTask.checkForMagicCompletion(event.getParam1(), magicLevel, unlockData.getTasks());
			completedTasks.forEach(this::completeTask);
			if(completedTasks.size() > 0) savePlayerData();
			log.info(menuOption + " target: " + menuTarget + " param0: " + event.getParam0() + " item param1:" + event.getParam1());
		}
		else if(menuTarget.equalsIgnoreCase("Quick-prayers")) {
			if(!unlockData.getSkills().contains(Skill.PRAYER.getName())) {
				client.playSoundEffect(SOUND_EFFECT_FAIL);
				event.consume();
			}
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if(event.getActor() == client.getLocalPlayer()) {
			int points = (int) Math.floor(unlockData.getPoints()/2);
			unlockData.subtractPoints(points);
			savePlayerData();

			final ChatMessageBuilder message = new ChatMessageBuilder()
					.append(ChatColorType.HIGHLIGHT)
					.append("As a result of your death, half your points have been taken!")
					.append(ChatColorType.NORMAL);

			chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.CONSOLE)
					.runeLiteFormattedMessage(message.build())
					.build());
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		WorldPoint loc = client.getLocalPlayer().getWorldLocation();
		if(lastPlayerLoc == null) lastPlayerLoc = loc;

		if(client.getLocalPlayer().getWorldLocation() != lastPlayerLoc) {
			List<LockedTask> tasks = LockedTask.checkForLocationCompletion(loc, unlockData.getTasks());

			tasks.forEach(this::completeTask);

			if(tasks.size() > 0) savePlayerData();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged e) {
		if(lastInventoryState == null) {
			lastInventoryState = Arrays.asList(client.getItemContainer(InventoryID.INVENTORY).getItems());
			return;
		}

		WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();
		List<String> completedTasks = unlockData.getTasks();
		List<LockedTask> tasks;

		if(e.getContainerId() == InventoryID.EQUIPMENT.getId()) {
			tasks = LockedTask.checkForEquipCompletion(Arrays.asList(e.getItemContainer().getItems()), playerLoc, completedTasks);
		}
		else {
			List<Item> newItems = Arrays.asList(e.getItemContainer().getItems());
			List<Item> changedItems = newItems.stream().filter(i -> !lastInventoryState.contains(i)).collect(Collectors.toList());;

			tasks = LockedTask.checkForNonNpcLoot(
					e.getItemContainer(),
					playerLoc,
					changedItems,
					completedTasks
			);
		}

		if(tasks == null) return;

		tasks.forEach(this::completeTask);

		if(completedTasks.size() > 0) savePlayerData();
		if(e.getContainerId() == InventoryID.INVENTORY.getId()) lastInventoryState = Arrays.asList(e.getItemContainer().getItems());
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		if(client.getGameState().equals(GameState.LOGGED_IN)) {
			clientThread.invokeLater(() -> {
				List<LockedTask> tasks = LockedTask.checkForQuestCompletion(client, unlockData.getTasks());
				tasks.forEach(this::completeTask);
				if(tasks.size() > 0) savePlayerData();
			});
		}
	}

	private void completeTask(LockedTask task) {
		if(unlockData.getTasks().contains(task.getId())) return;

		unlockData.addTask(task);
		awardRelic(new Relic(task.getTier()), false, false);

		final ChatMessageBuilder message = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append("You completed a Tier "+task.getTier()+" task: "+task.getDescription())
				.append(ChatColorType.NORMAL);

		chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.ITEM_EXAMINE)
				.runeLiteFormattedMessage(message.build())
				.build());

		SwingUtilities.invokeLater(this::redrawPanel);
	}

	private void updateSkill(Skill skill, int xpGained) {
		//No xp gained
		if(xpGained == 0) return;
		rollForRelic(xpGained, skill);
	}

	private void rollForRelic(double xpGained, Skill skill) {
		// Don't grant relics for combat skills
		if(Arrays.asList(BLACKLIST).contains(skill)) return;
		double maxXp = 2500;
		double maxChance = 33;
		double chance = Math.min(xpGained, maxXp) / maxXp * maxChance;
		double roll = Math.random()*100;

		log.info(chance + "% chance of relic... roll was "+roll);

		if(roll < chance) {
			this.awardRelic(new Relic(1), false, true);
			savePlayerData();

			final ChatMessageBuilder message = new ChatMessageBuilder()
					.append(ChatColorType.HIGHLIGHT)
					.append("While training "+skill.getName()+" you found a Tier 1 relic!")
					.append(ChatColorType.NORMAL);

			chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.ITEM_EXAMINE)
					.runeLiteFormattedMessage(message.build())
					.build());
		}
	}

	private void rollForRelicDrop(NPC npc, LocalPoint point) {
		double maxCb = 350;
		double minCb = 1;
		double combatLevel = npc.getCombatLevel();

		// The chance of a relic drop is a normalized value between 0 and 15 based on CB level
		// The value is then clamped so the min % chance is 0.5% and the max is 15%
		double baseChance = (combatLevel - minCb) / (maxCb - minCb)  * 15;
		double clampedChance = Math.min(15, Math.max(baseChance, .5));
		double roll = Math.random()*100;

		log.info(clampedChance+"% chance of a relic drop... roll was "+roll);

		if(roll < clampedChance) {
			int tier = rollRelicTier(combatLevel);
			addGroundItem(point, tier);
			playRelicSound();
		}
	}

	private void pickUpRelic(WorldPoint point) {
		client.playSoundEffect(SoundEffectID.ITEM_PICKUP);
		awardRelic(this.groundItems.get(point), false, false);
		savePlayerData();
		removeGroundItem(point);
		removeLootbeam(point);
	}

	private void createLockedSkillOverlays() {
		List<RelicScapeSkill> skillWidgets = Arrays.asList(RelicScapeSkill.values());
		this.skillOverlays = new HashMap<>();

		skillWidgets.forEach(widget -> addSkillOverlay(widget.getName(), widget.getWidgetID()));
	}

	private void addSkillOverlay(String skillName, int widgetID) {
		Widget skillWidget = client.getWidget(widgetID);
		if(skillWidget == null) return;

		boolean isUnlocked = unlockData.getSkills().contains(skillName);
		List<Widget> widgets = new ArrayList<>();

		Widget icon = skillWidget.createChild(-1, WidgetType.GRAPHIC);
		Widget level = skillWidget.createChild(-1, WidgetType.GRAPHIC);
		level.setSpriteId(176);
		level.setSize(36, 36);
		level.setPos(28, -2);
		level.setOpacity(90);
		level.setHidden(isUnlocked);
		icon.setSpriteId(174);
		icon.setSize(36, 36);
		icon.setPos(-2, -2);
		icon.setOpacity(90);
		icon.setHidden(isUnlocked);

		widgets.add(icon);
		widgets.add(level);
		skillOverlays.put(skillName, widgets);
	}

	private void setDefaultWidgetVisibility(boolean visible) {
		// Iterate though each of the non essential widgets
		for (Integer packedID : this.hiddenWidgetIDs) {
			// Update their visibility
			this.client.getWidget(packedID).setHidden(!visible);
		}
	}

	private void updateDisplayedMenu() {
		// Set the initial map state from config
		this.mapEnabled = this.getInitialMapState();
	}

	private boolean getInitialMapState() {
		return false;
	}

	private void createMenuPages(Widget window) {
		this.indexPage = new UIPage();
		this.confirmPage = new ConfirmationScreen(window);
	}

	private void createConfirmationMenu() {
		this.confirmPage.getConfirmButton().setOnHoverListener((c) -> onConfirmHover());
		this.confirmPage.getConfirmButton().setOnLeaveListener((c) -> onConfirmLeave());
		this.confirmPage.setVisibility(false);
	}

	/**
	 * Creates the widgets and components required for the index menu,
	 * such as the index maps and the region icons
	 *
	 * @param window the layer on which to create the widgets
	 */
	private void createIndexMenu(Widget window) {
		// Create a graphic widget for the background image of the index page
		Widget backingWidget = window.createChild(-1, WidgetType.GRAPHIC);

		final int POS_X = getSpriteX(window);
		final int POS_Y = getSpriteY(window);

		// Wrap in a UIGraphic, set dimensions, position and sprite
		UIGraphic indexBackingGraphic = new UIGraphic(backingWidget);
		indexBackingGraphic.setPosition(POS_X, POS_Y);
		indexBackingGraphic.setSize(REGION_MAP_SPRITE_WIDTH, REGION_MAP_SPRITE_HEIGHT);
		indexBackingGraphic.setSprite(REGION_MAP_MAIN);

		// Initialise the arrays for the map graphics and icons
		this.indexRegionGraphics = new UIGraphic[regionDefinitions.length];
		UIButton[] indexRegionIcons = new UIButton[regionDefinitions.length];

		// Add the backing graphic to the index page
		this.indexPage.add(indexBackingGraphic);

		for (TrailblazerRegion region : TrailblazerRegion.values()) {
			if(region.getRegionID() == -1) continue;

			int i = region.getRegionID();
			// Get definition for the region
			RegionDefinition regionDef = this.regionDefinitions[i];
			if (unlockData != null) regionDef.setUnlocked(unlockData.isAreaUnlocked(regionDef.getName()));

			// Create a widget for the region sprite graphic
			Widget regionGraphic = window.createChild(-1, WidgetType.GRAPHIC);

			// Wrap in UIGraphic, update the size and position to match that of
			// the backing graphic. Set the sprite to that of the current region
			this.indexRegionGraphics[i] = new UIGraphic(regionGraphic);
			this.indexRegionGraphics[i].setPosition(POS_X, POS_Y);
			this.indexRegionGraphics[i].setSize(REGION_MAP_SPRITE_WIDTH, REGION_MAP_SPRITE_HEIGHT);
			this.indexRegionGraphics[i].setSprite(regionDef.getIndexSprite());

			if (!regionDef.isUnlocked()) this.indexRegionGraphics[i].setOpacity(0);

			// Add the component to the index page
			this.indexPage.add(this.indexRegionGraphics[i]);

			// Create the widget for the regions icon
			Widget regionIcon = window.createChild(-1, WidgetType.GRAPHIC);

			// Get the definition for the regions icon
			IconDefinition iconDef = regionDef.getIcon();

			// Wrap in UIBUtton, position the component. attach listeners, etc.
			indexRegionIcons[i] = new UIButton(regionIcon);
			indexRegionIcons[i].setName(regionDef.getName());
			indexRegionIcons[i].setPosition(iconDef.getX() + POS_X, iconDef.getY() + POS_Y);
			indexRegionIcons[i].setSize(MAP_ICON_WIDTH, MAP_ICON_HEIGHT);
			indexRegionIcons[i].setSprites(iconDef.getSpriteStandard(), iconDef.getSpriteHover());
			indexRegionIcons[i].setOnHoverListener((c) -> onIconHover(regionDef.getId(), window));
			indexRegionIcons[i].setOnLeaveListener((c) -> onIconLeave(regionDef.getId(), window));
			indexRegionIcons[i].addAction(ACTION_TEXT_SELECT, () -> onIconClicked(regionDef.getId()));

			// Add to the index page
			this.indexPage.add(indexRegionIcons[i]);
		}
	}

	/**
	 * Creates the back arrow, used to return to the index page
	 *
	 * @param window the layer on which to create the widget
	 */
	private void createBackButton(Widget window) {
		// Create the widget for the button
		Widget backArrowWidget = window.createChild(-1, WidgetType.GRAPHIC);

		// Wrap as a button, set the position, sprite, etc.
		UIButton backArrowButton = new UIFadeButton(backArrowWidget);
		backArrowButton.setSprites(SpriteID.GE_BACK_ARROW_BUTTON);
		backArrowButton.setPosition(10, 40);
		backArrowButton.setSize(30, 23);

		// Assign the callback for the button
		backArrowButton.addAction(ACTION_TEXT_BACK, this::onBackButtonPressed);

		this.confirmPage.add(backArrowButton);
	}

	private void createRegionCount(Widget window) {
		// Create the widget for the button
		Widget countWidget = window.createChild(-1, WidgetType.TEXT);

		// Wrap as a button, set the position, sprite, etc.
		regionCount = new UILabel(countWidget);
		regionCount.setPosition(20, REGION_MAP_SPRITE_HEIGHT+10);
		regionCount.setSize(130, 10);
		regionCount.setColour(0xFF981F);
	}

	/**
	 * Creates the checkbox for toggling the state of the map
	 *
	 * @param window the layer on which to create the widget
	 */
	private void createToggleCheckbox(Widget window) {
		// Create the graphic widget for the checkbox
		Widget toggleWidget = window.createChild(-1, WidgetType.GRAPHIC);
		Widget labelWidget = window.createChild(-1, WidgetType.TEXT);

		// Wrap in checkbox, set size, position, etc.
		UICheckBox mapToggle = new UICheckBox(toggleWidget, labelWidget);
		mapToggle.setPosition(10, 10);
		mapToggle.setName(NAME_TEXT_TOGGLE);
		mapToggle.setEnabled(this.mapEnabled);
		mapToggle.setText("Show Area Unlocks");
		labelWidget.setPos(30, 10);
		mapToggle.setToggleListener(this::onMapStateToggled);
	}

	/**
	 * Updates the state of the widgets depending on map state
	 */
	private void updateMapState() {
		// If the map is enabled, display the custom widgets
		if (this.mapEnabled) {
			// Hide the default widgets and display the map index map
			this.setDefaultWidgetVisibility(false);
			this.displayIndexPage();
		} else {
			// Hide all custom widgets and show the default widgets
			this.indexPage.setVisibility(false);
			this.confirmPage.setVisibility(false);
			this.setDefaultWidgetVisibility(true);
		}

		// Save the new map mode to the config
		this.setPreviousDisplayMode(mapEnabled);
	}

	/**
	 * Displays the index page and makes sure
	 * that each of the map pages are hidden
	 */
	private void displayIndexPage() {
		this.indexPage.setVisibility(true);
		this.confirmPage.setVisibility(false);

		this.regionCount.setText("Regions unlocked: "+unlockData.getAreas().size()+"/"+config.maxRegionUnlocks());
	}

	private void displayConfirmationPage(int regionID) {
		// Hide the index page
		this.indexPage.setVisibility(false);
		RegionDefinition regDef = this.regionDefinitions[regionID];
		TrailblazerRegion area = stringToTrailblazer(regDef.getName());
		boolean canAfford = this.unlockData.getPoints() >= area.unlockCost;

		if(regDef.isUnlocked()) {
			this.confirmPage.setAffordText("You have unlocked this region. Press \"Lock\" to re-lock it.", Color.GREEN);
			this.confirmPage.setButtonSprite(LOCK_BUTTON_SPRITE);
		}
		else if(this.unlockData.getPoints() >= area.unlockCost) {
			this.confirmPage.setAffordText("You can afford this region. Press \"Confirm\" to unlock.", Color.GREEN);
			this.confirmPage.setButtonSprite(CONFIRM_BUTTON_SPRITE);
		}
		else {
			this.confirmPage.setAffordText("You cannot afford this region. You have "+unlockData.getPoints()+" points.", Color.RED);
			this.confirmPage.setButtonSprite(BACK_BUTTON_SPRITE);
		}

		this.confirmPage.setIcon(regDef.getIcon().getSpriteHover());
		this.confirmPage.setDescription(area.description);
		this.confirmPage.setTitle(area.name+": "+area.unlockCost+" points");
		this.confirmPage.getConfirmButton().clearActions();
		this.confirmPage.getConfirmButton().addAction(ACTION_TEXT_CONFIRM, () -> onConfirmClicked(regDef, !canAfford));
		this.confirmPage.setVisibility(true);
	}

	/**
	 * Called when the map state checkbox is toggled
	 *
	 * @param src the checkbox component
	 */
	private void onMapStateToggled(UIComponent src) {
		// The checkbox component
		UICheckBox toggleCheckbox = (UICheckBox) src;

		// Update the map enabled flag
		this.mapEnabled = toggleCheckbox.isEnabled();
		this.regionCount.setVisibility(this.mapEnabled);
		if(this.mapEnabled) {
			clearMapIcons();
		}
		else {
			restoreMapIcons();
		}

		// Update the map state
		this.updateMapState();

		// *Boop*
		this.client.playSoundEffect(SoundEffectID.UI_BOOP);
	}

	/**
	 * Called when the mouse enters the icon
	 *
	 * @param regionID the ID of the region represented by the icon
	 */
	private void onIconHover(int regionID, Widget window) {
		if (!this.regionDefinitions[regionID].isUnlocked()) return;

		final int POS_Y = getSpriteY(window);

		// Move the map sprite for this region up by 2 pixels, and
		// set the opacity to 75% opaque
		this.indexRegionGraphics[regionID].setY(POS_Y - 2);
		this.indexRegionGraphics[regionID].setOpacity(.75f);
		this.indexRegionGraphics[regionID].getWidget().revalidate();
	}

	/**
	 * Called when the mouse exits the icon
	 *
	 * @param regionID the ID of the region represented by the icon
	 */
	private void onIconLeave(int regionID, Widget window) {
		if (!this.regionDefinitions[regionID].isUnlocked()) return;

		final int POS_Y = getSpriteY(window);

		// Restore the original position and set back to fully opaque
		this.indexRegionGraphics[regionID].setY(POS_Y);
		this.indexRegionGraphics[regionID].setOpacity(1.0f);
		this.indexRegionGraphics[regionID].getWidget().revalidate();
	}

	/**
	 * Called when a region icon is selected
	 *
	 * @param regionID the ID of the region represented by the icon
	 */
	private void onIconClicked(int regionID) {
		// Display the map page for the region
		this.displayConfirmationPage(regionID);

		// *Boop*
		this.client.playSoundEffect(SoundEffectID.UI_BOOP);
	}

	/**
	 * Called when the mouse enters the confirm button
	 */
	private void onConfirmHover() {
		this.confirmPage.getConfirmButton().setOpacity(.75f);
	}

	/**
	 * Called when the mouse exits the confirm button
	 */
	private void onConfirmLeave() {
		this.confirmPage.getConfirmButton().setOpacity(1.0f);
	}

	/**
	 * Called when the confirm button is selected
	 */
	private void onConfirmClicked(RegionDefinition region, boolean back) {
		if(region.isUnlocked()) {
			lockRegion(region);
		}
		else if(!back) {
			boolean success = unlockArea(stringToTrailblazer(region.getName()), false);
			if(!success) return;
		}
		else {
			this.client.playSoundEffect(SoundEffectID.UI_BOOP);
		}

		this.displayIndexPage();
	}

	private void onBackButtonPressed() {
		// Go back to the index page
		this.displayIndexPage();

		// *Boop*
		this.client.playSoundEffect(SoundEffectID.UI_BOOP);
	}

	private void setPreviousDisplayMode(boolean mode) {
		this.configManager.setConfiguration(CONFIG_KEY, CFG_KEY_STATE, mode);
	}

	private int getSpriteX(Widget window) {
		return (window.getWidth() / 2) - (REGION_MAP_SPRITE_WIDTH / 2);
	}

	private int getSpriteY(Widget window) {
		return (window.getHeight() / 2) - (REGION_MAP_SPRITE_HEIGHT / 2);
	}

	public static int getCenterX(Widget window, int width) {
		return (window.getWidth() / 2) - (width / 2);
	}

	public static int getCenterY(Widget window, int height) {
		return (window.getHeight() / 2) - (height / 2);
	}

	private void clearMapIcons() {
		this.mapIcons.clear();
		worldMapPointManager.removeIf(this::storeAndClearPoint);
	}

	private boolean storeAndClearPoint(WorldMapPoint point) {
		this.mapIcons.add(point);
		return true;
	}

	private void restoreMapIcons() {
		this.mapIcons.forEach(p -> worldMapPointManager.add(p));
	}

	private TrailblazerRegion stringToTrailblazer(String name) {
		return TrailblazerRegion.valueOf(name.toUpperCase().replace(" ", "_"));
	}

	private void addLootbeam(WorldPoint worldPoint, Color color)
	{
		Lootbeam lootbeam = lootbeams.get(worldPoint);
		if (lootbeam == null)
		{
			lootbeam = new Lootbeam(client, clientThread, worldPoint, color, Lootbeam.Style.MODERN);
			lootbeams.put(worldPoint, lootbeam);
		}
		else
		{
			lootbeam.setColor(color);
			lootbeam.setStyle(Lootbeam.Style.MODERN);
		}
	}

	private void removeLootbeam(WorldPoint worldPoint)
	{
		Lootbeam lootbeam = lootbeams.remove(worldPoint);
		if (lootbeam != null)
		{
			lootbeam.remove();
		}
	}

	private void addGroundItem(LocalPoint point, int tier) {
		WorldPoint loc = WorldPoint.fromLocal(client, point);
		Relic relic = new Relic(client, point, tier);

		groundItems.put(loc, relic);
		if(config.showLootbeams()) addLootbeam(loc,getLootbeamColor(tier));
	}

	private void removeGroundItem(WorldPoint point) {
		Relic item = groundItems.remove(point);
		if(item != null) {
			item.setActive(false);
		}
	}

	private void handleLootbeams() {
		HashMap<WorldPoint, Lootbeam> beamsToDelete = new HashMap<>(lootbeams);
		beamsToDelete.keySet().forEach(this::removeLootbeam);

		if(!config.showLootbeams()) return;

		groundItems.keySet().forEach(point -> {
			int tier = groundItems.get(point).getTier();
			this.addLootbeam(point, getLootbeamColor(tier));
		});
	}

	private void playRelicSound() {
		if(config.playRelicSound()) this.client.playSoundEffect(config.relicSoundID());
	}

	private int rollRelicTier(double combatLevel) {
		double[] minCb = {0, 50, 200};
		double[] maxCb = {49, 199, 1024};
		int maxTier = combatLevel >= minCb[2] ? 3 : combatLevel >= minCb[1] ? 2 : 1;
		if(maxTier == 1) return 1;

		double maxTierChance = (combatLevel-minCb[maxTier-1]) / (maxCb[maxTier-1]-minCb[maxTier-1]) * 100;
		double roll = Math.random()*100;

		return roll < maxTierChance ? maxTier : maxTier-1;
	}

	private Color getLootbeamColor(int tier) {
		return tier == 3 ? config.lootbeamColorT3() : tier == 2 ? config.lootbeamColorT2() : config.lootbeamColorT1();
	}

	private void sendFailMessage(String message) {
		client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				message,
				null
		);
		client.playSoundEffect(2277);
	}

	private Relic rollForRelicCasket(String casketTier) {
		ClueScroll clueScroll = ClueScroll.valueOf(ClueScroll.class, casketTier.toUpperCase());

		int relicRoll = (int) Math.floor(Math.random()*100);
		int relicChance = clueScroll.getRelicChance();

		log.info("There is a "+relicChance+"% chance of rolling a relic. The roll was "+relicRoll);

		if(relicRoll >= relicChance) return null;

		List<Integer> chances = clueScroll.getRelicTierChances();
		int tierRoll = (int) Math.floor(Math.random()*100);

		log.info("The tier chances are "+chances.toString()+". The roll was "+tierRoll);

		if(tierRoll < chances.get(3)){
			return new Relic(4);
		}
		if(tierRoll < chances.get(2)) {
			return new Relic(3);
		}
		if(tierRoll < chances.get(1)) {
			return new Relic(2);
		}
		else {
			return new Relic(1);
		}
	}

	public void redrawPanel() {
		pluginPanel.redraw();
	}
}
