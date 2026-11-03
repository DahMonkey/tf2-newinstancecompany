package rafradek.TF2weapons;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mod configuration. The 1.12 Configuration file is replaced by a Forge config spec
 * (config/rafradek_tf2_weapons-common.toml). Option names, defaults and ranges are unchanged; the
 * static fields are refreshed by {@link #bake()} whenever the file loads or changes.
 */
public class TF2ConfigVars {

	public static final ForgeConfigSpec SPEC;

	public static int destTerrain;
	public static boolean medigunLock;
	public static boolean swapAttackButton;
	public static boolean fastMetalProduction;
	public static boolean shootAttract;
	public static boolean disableSpawn;
	public static boolean disableBossSpawn;
	public static boolean disableBossSpawnItems;
	public static float invasionChance;
	public static int invasionMinDay;
	public static int invasionMaxWaves;
	public static boolean disableInvasionItems;
	public static boolean disableLoot;
	public static int bossReappear;
	public static boolean disableContracts;
	public static boolean disableGeneration;
	public static int mannCoChance;
	public static int baseChance;
	public static int randomCrits;
	public static boolean overworldOnly;
	public static String spawnOres;
	public static String naturalCheck;
	public static String biomeCheck;
	public static int batchDamage;
	public static float damageMultiplier;
	public static float healthScale;
	public static boolean dynamicLights;
	public static boolean dynamicLightsProj;
	public static boolean deadRingerTrigger;
	public static boolean thermalExpansion;
	public static float medicChance;
	public static int sentryTargets;
	public static float dispenserRepair;
	public static boolean dispenserPlayers;
	public static boolean teleporterPlayers;
	public static boolean teleporterEntities;
	public static boolean enableUdp;
	public static boolean targetSentries;
	public static float dropAmmo;
	public static float speedMult;
	public static float armorMult;
	public static float costMult;
	public static float xpMult;
	public static int xpCap;
	public static float mercenaryVolume;
	public static float bossVolume;
	public static float gunVolume;
	public static boolean enchantedExplosion;
	public static float maxEnergy;
	public static boolean buildingsUseEnergy;
	public static int sentryUseEnergy;
	public static int dispenserUseEnergy;
	public static int teleporterUseEnergy;
	public static boolean fastItemCooldown;
	public static boolean freeUseItems;
	public static int maxMetalEngineer;
	public static boolean fastBuildEngineer;
	public static boolean longDurationBanner;
	public static boolean headCountDrop;
	public static boolean killstreakDrop;
	public static boolean disableBuildingGui;
	public static boolean limitedDisguise;

	public static boolean oldDispenser;
	public static boolean scaleAttributes;
	public static boolean mustReload;
	public static boolean autoReload;
	public static float accurracyMult;
	public static int updateDelay = 3;
	public static float worldScale;
	public static float explosionKnockback;
	public static float explosiveJumpGravity;
	public static float hatMercenaryMult;
	public static int allowTrimp;
	public static boolean australiumStatue;
	public static boolean doorBlockLight;
	public static boolean neutralAttack;
	public static boolean attackMobs;
	public static boolean canJoin;
	public static boolean mouseBackpackItemSwitch;

	public static boolean generateCopper;
	public static boolean generateLead;
	public static boolean generateAustralium;

	/** Default targeting options for new buildings, keyed by their 1.12 option name. */
	public static final Map<String, Boolean> defaultBuildingTargets = new LinkedHashMap<>();

	/** Spawn weight per mercenary class name (scout, pyro, ...). */
	public static final Map<String, Integer> spawnRate = new LinkedHashMap<>();

	public static ArrayList<ResourceLocation> repairBlacklist = new ArrayList<>();
	public static ArrayList<ResourceLocation> hostileBlacklist = new ArrayList<>();

	/**
	 * Options the server sends to clients on login, so client-side prediction uses the server's
	 * values. Keyed by option path ("category.Option name").
	 */
	public static final Map<String, ForgeConfigSpec.ConfigValue<?>> NETWORKED = new LinkedHashMap<>();

	private static final Map<String, ForgeConfigSpec.ConfigValue<?>> VALUES = new LinkedHashMap<>();

	private static ForgeConfigSpec.Builder builder;
	private static String category;

	private static final Map<String, Object> OVERRIDES = new LinkedHashMap<>();

	static {
		builder = new ForgeConfigSpec.Builder();

		push("gameplay");
		string("Destructible terrain", "Upgrade only", "Explosions can destroy blocks. Always, Upgrade only, Never");
		bool("Medigun lock target", false, "Left Click selects healing target");
		bool("Swap mouse buttons", false, "Swaps attack and use buttons on weapons");
		bool("Disable contracts", false, "Stop new contracts from appearing");
		net(bool("Reload in creative", true, "Weapons have to be reloaded in creative mode"));
		bool("Auto reload", true, "Always reload weapons automatically");
		bool("Shooting attracts mobs", true, "Gunfire made by players attracts mobs");
		string("Random critical hits", "Players only",
				"Enables randomly appearing critical hits that deal 3x more damage. Always, Players Only, Never");
		bool("Feign death events", true,
				"Does feign death trigger death events, set to false in case of mod conflicts");
		bool("Enable australium statues", true, "Disable in case of mod conflicts");
		integer("Boss respawn cooldown", 30000, 1100, Integer.MAX_VALUE,
				"Maximum boss reappear time in ticks. Bosses always spawn in full moon");
		list("Hostile entity blacklist", List.of("minecraft:enderman", "minecraft:zombified_piglin"),
				"Entity IDs that should not be considered hostile");
		string("Attack invulnerability period", "Ignore",
				"Ignore, respect, or batch damage dealt during mob invulnerability. Ignore, Batch, Respect");
		real("Weapon cost multiplier", 1, 0, 100, "Weapon cost multiplier in Mann Co. store");
		net(real("Upgrade cost multiplier", 1, 0, 100, "Upgrade cost multiplier"));
		net(integer("Max upgrade limit", Integer.MAX_VALUE, 0, Integer.MAX_VALUE,
				"Maximum upgrade limit, applied after other limits"));
		bool("Enchanted blast jumping", true, "Strafing, no air resistance and reduced gravity when blast jumping");
		real("Ammo drop chance", 0.15, 0, 1, "Chance of dropping ammo from non-TF2 hostile creature");
		bool("Allow team joining", true, "Allow players to join teams");
		bool("Backpack activation in hotbar", true, "Activate backpack by switching slot in hotbar");
		pop();

		push("adaption");
		bool("Fast metal production", false, "Dispensers produce metal every 5 seconds");
		net(integer("TF2 - Minecraft health translation", 200, -10000, 10000,
				"How much 10 minecraft hearts are worth in TF2 health"));
		bool("Fast item cooldown", true, "Sandman balls and cleavers recover faster than normal");
		bool("Free use items", false, "Throwable and consumable items are free to use");
		bool("Long duration banner", true, "Banner buffs build longer and last longer");
		real("World scale", 0.9, 0, Float.MAX_VALUE, "How many meters long minecraft block is");
		real("Explosion knockback", 0.14, 0, Float.MAX_VALUE,
				"Velocity added for each point of explosive damage. 0.115 is TF2 original");
		real("Blast jump gravity", 0.046, 0, Float.MAX_VALUE,
				"Gravity applied when rocket jumping. 0.0381 is original TF2 gravity");
		string("Simulate ramps", "Horizontal only",
				"Simulate ramps when rocket jumping or charging. Always, Horizontal only, Never");
		bool("Head count drop", true, "If enabled, head count on eyelander will drop over time");
		bool("Killstreak count drop", true,
				"If enabled, killstreak count will drop over time. When disabled, bonuses from killstreak weapons are no longer applied");
		bool("Disable building gui", false, "When disabled, right click will always pick up the building");
		net(bool("Limited disguise", false, "Disguise is limited to class selection"));
		pop();

		push("spawn rate");
		bool("Disable mob spawning", false, "Disable mod-specific mobs spawning");
		bool("Spawn only in overworld", false, "Disable spawning in custom dimensions");
		string("Spawn in biomes", "Default",
				"Default - biomes that spawn vanilla monsters. All, Default, Vanilla only");
		bool("Disable boss spawn", false, "Disable random tf2 boss spawn");
		bool("Disable boss spawn items", false, "Disable boss spawning items");
		real("Invasion chance", 0.06, 0, 1, "Chance of robot invasion occuring at the beginning of each day.");
		integer("Invasion minimum delay", 10, 0, Integer.MAX_VALUE, "Minimal delay between invasions, in days");
		integer("Invasion max waves", 6, 1, Integer.MAX_VALUE, "Maximum amount of waves");
		bool("Disable invasion event items", false, "Disable invasion items");
		integer("Scout", 12, 0, 1000, "");
		integer("Pyro", 12, 0, 1000, "");
		integer("Soldier", 12, 0, 1000, "");
		integer("Heavy", 12, 0, 1000, "");
		integer("Demoman", 12, 0, 1000, "");
		integer("Spy", 9, 0, 1000, "");
		integer("Engineer", 9, 0, 1000, "");
		integer("Sniper", 9, 0, 1000, "");
		real("Medic spawn chance", 1, 0, 1000, "Medic spawn chance multiplier");
		pop();

		push("world gen");
		bool("Disable structures", false, "Disable structures generation, such as Mann Co. building");
		integer("Mann Co. building chance", 32, 1, Integer.MAX_VALUE, "Median chunk distance between Mann Co. buildings");
		integer("Base building chance", 32, 1, Integer.MAX_VALUE, "Median chunk distance between base buildings");
		bool("Disable chest loot", false, "Disable chest loot generated by this mod");
		string("Generate copper", "Never", "Always, Default, Never. Vanilla 1.19 has its own copper, so this defaults to Never");
		string("Generate lead", "Default", "Always, Default, Never");
		string("Generate australium", "Always", "Always, Never");
		pop();

		push("mercenary");
		string("Natural mob detection", "Always", "Always, Fast, Never");
		real("Mercenary speed multiplier", 0.8, 0, 2,
				"Speed multiplier of mercenaries. Does not apply to owned mercenaries");
		real("Armored mercenary chance", 0.06, 0, 10,
				"Base chance of armored mercenaries. Altered by difficulty level");
		real("Hatted mercenary chance", 1, 0, 10, "Base chance of hatted mercenaries. Altered by difficulty level");
		integer("Engineer max metal", 500, 0, Integer.MAX_VALUE, "Starting metal for engineers");
		bool("Engineer fast build", true, "Should engineers build at extra speed");
		bool("Reduce vs player damage", true, "Reduce weapon effectiveness versus players");
		real("Accurracy multiplier", 1, 0, 10, "Mercenary accurracy multiplier");
		integer("Update delay", 3, 1, 20, "Delay in sending position updates. Only for dedicated server");
		bool("Attack neutral players", false, "Should mercenaries attack neutral players");
		bool("Attack mobs", true, "Should mercenaries attack mobs");
		pop();

		push("building");
		bool("Old dispenser behavior", false, "When enabled, no material is being used to repair");
		bool("Buildings use energy", false, "");
		integer("Sentry energy use", 100, 0, 40000, "Energy use on attack");
		integer("Dispenser energy use", 15, 0, 40000, "Energy use on repairs and heals");
		integer("Teleport energy use", 20000, 0, 40000, "Energy use on teleport");
		real("Dispenser repair rate", 3, 0, 10000, "Repair per 1 metal. Reduced by enchants");
		list("Repair blacklist", List.of(), "Item IDs that should not be repairable by dispensers");
		bool("Mobs target sentries", true, "Mobs will attack sentries that dont shoot");
		pop();

		push("default building targets");
		bool("Attack on hurt", true, "");
		bool("Attack other players", false, "");
		bool("Attack hostile mobs", true, "");
		bool("Attack friendly creatures", false, "");
		bool("Dispensers heal neutral players", true, "");
		bool("Neutral players can teleport", true, "");
		bool("Entities can teleport", true, "");
		pop();

		push("sound volume");
		real("Boss volume (radius)", 4, 0, 10, "Values above 1 increase sound radius only");
		real("Mercenary volume (radius)", 0.6, 0, 10, "Values above 1 increase sound radius only");
		real("Gun volume (radius)", 2, 0, 10, "Applies only to players, values above 1 increase sound radius only");
		pop();

		push("miscellaneous");
		bool("Overhead door blocks light", true, "Should overhead doors block light");
		pop();

		SPEC = builder.build();
		builder = null;
		bake();
	}

	/** Reads every option into the static fields. Called on load, reload and when the server syncs values. */
	public static void bake() {
		String destr = s("gameplay.Destructible terrain");
		destTerrain = destr.equalsIgnoreCase("Always") ? 2 : destr.equalsIgnoreCase("Upgrade only") ? 1 : 0;
		medigunLock = b("gameplay.Medigun lock target");
		swapAttackButton = b("gameplay.Swap mouse buttons");
		disableContracts = b("gameplay.Disable contracts");
		mustReload = b("gameplay.Reload in creative");
		autoReload = b("gameplay.Auto reload");
		shootAttract = b("gameplay.Shooting attracts mobs");
		randomCrits = index(s("gameplay.Random critical hits"), 1, "Always", "Players Only", "Never");
		deadRingerTrigger = b("gameplay.Feign death events");
		australiumStatue = b("gameplay.Enable australium statues");
		bossReappear = i("gameplay.Boss respawn cooldown");
		hostileBlacklist = locations("gameplay.Hostile entity blacklist");
		enableUdp = false;
		batchDamage = index(s("gameplay.Attack invulnerability period"), 0, "Ignore", "Batch", "Respect");
		costMult = f("gameplay.Weapon cost multiplier");
		xpMult = f("gameplay.Upgrade cost multiplier");
		xpCap = i("gameplay.Max upgrade limit");
		enchantedExplosion = b("gameplay.Enchanted blast jumping");
		dropAmmo = f("gameplay.Ammo drop chance");
		canJoin = b("gameplay.Allow team joining");
		mouseBackpackItemSwitch = b("gameplay.Backpack activation in hotbar");

		fastMetalProduction = b("adaption.Fast metal production");
		healthScale = i("adaption.TF2 - Minecraft health translation");
		damageMultiplier = 200f / healthScale;
		fastItemCooldown = b("adaption.Fast item cooldown");
		freeUseItems = b("adaption.Free use items");
		longDurationBanner = b("adaption.Long duration banner");
		worldScale = f("adaption.World scale");
		explosionKnockback = f("adaption.Explosion knockback");
		explosiveJumpGravity = f("adaption.Blast jump gravity");
		allowTrimp = index(s("adaption.Simulate ramps"), 0, "Always", "Horizontal only", "Never");
		headCountDrop = b("adaption.Head count drop");
		killstreakDrop = b("adaption.Killstreak count drop");
		disableBuildingGui = b("adaption.Disable building gui");
		limitedDisguise = b("adaption.Limited disguise");

		disableSpawn = b("spawn rate.Disable mob spawning");
		overworldOnly = b("spawn rate.Spawn only in overworld");
		biomeCheck = s("spawn rate.Spawn in biomes");
		disableBossSpawn = b("spawn rate.Disable boss spawn");
		disableBossSpawnItems = b("spawn rate.Disable boss spawn items");
		invasionChance = f("spawn rate.Invasion chance");
		invasionMinDay = i("spawn rate.Invasion minimum delay");
		invasionMaxWaves = i("spawn rate.Invasion max waves");
		disableInvasionItems = b("spawn rate.Disable invasion event items");
		for (String clazz : new String[] { "Scout", "Pyro", "Soldier", "Heavy", "Demoman", "Spy", "Engineer",
				"Sniper" })
			spawnRate.put(clazz.toLowerCase(), i("spawn rate." + clazz));
		medicChance = f("spawn rate.Medic spawn chance");

		disableGeneration = b("world gen.Disable structures");
		mannCoChance = i("world gen.Mann Co. building chance");
		baseChance = i("world gen.Base building chance");
		disableLoot = b("world gen.Disable chest loot");
		generateCopper = s("world gen.Generate copper").equalsIgnoreCase("Always");
		generateLead = !s("world gen.Generate lead").equalsIgnoreCase("Never");
		generateAustralium = s("world gen.Generate australium").equalsIgnoreCase("Always");

		naturalCheck = s("mercenary.Natural mob detection");
		speedMult = f("mercenary.Mercenary speed multiplier");
		armorMult = f("mercenary.Armored mercenary chance");
		hatMercenaryMult = f("mercenary.Hatted mercenary chance");
		maxMetalEngineer = i("mercenary.Engineer max metal");
		fastBuildEngineer = b("mercenary.Engineer fast build");
		scaleAttributes = b("mercenary.Reduce vs player damage");
		accurracyMult = f("mercenary.Accurracy multiplier");
		updateDelay = i("mercenary.Update delay");
		neutralAttack = b("mercenary.Attack neutral players");
		attackMobs = b("mercenary.Attack mobs");

		oldDispenser = b("building.Old dispenser behavior");
		buildingsUseEnergy = b("building.Buildings use energy");
		sentryUseEnergy = buildingsUseEnergy ? i("building.Sentry energy use") : 0;
		dispenserUseEnergy = buildingsUseEnergy ? i("building.Dispenser energy use") : 0;
		teleporterUseEnergy = buildingsUseEnergy ? i("building.Teleport energy use") : 0;
		dispenserRepair = f("building.Dispenser repair rate");
		repairBlacklist = locations("building.Repair blacklist");
		targetSentries = b("building.Mobs target sentries");

		for (String key : VALUES.keySet())
			if (key.startsWith("default building targets."))
				defaultBuildingTargets.put(key.substring("default building targets.".length()), b(key));

		bossVolume = f("sound volume.Boss volume (radius)");
		mercenaryVolume = f("sound volume.Mercenary volume (radius)");
		gunVolume = f("sound volume.Gun volume (radius)");

		doorBlockLight = b("miscellaneous.Overhead door blocks light");

		dynamicLights = false;
		dynamicLightsProj = false;
		thermalExpansion = false;
	}

	public static boolean getDefaultBuildingTarget(String name) {
		return defaultBuildingTargets.getOrDefault(name, false);
	}

	public static boolean isModLoaded(String id) {
		return ModList.get() != null && ModList.get().isLoaded(id);
	}

	/* ---------------- value access ---------------- */

	public static ForgeConfigSpec.ConfigValue<?> getValue(String path) {
		return VALUES.get(path);
	}

	/** Applies a value the server sent for a networked option (client side only, not saved). */
	@SuppressWarnings("unchecked")
	public static void applyNetworked(String path, String value) {
		ForgeConfigSpec.ConfigValue<?> cv = NETWORKED.get(path);
		if (cv == null)
			return;
		Object current = cv.get();
		Object parsed;
		if (current instanceof Boolean)
			parsed = Boolean.parseBoolean(value);
		else if (current instanceof Integer)
			parsed = Integer.parseInt(value);
		else if (current instanceof Double)
			parsed = Double.parseDouble(value);
		else
			parsed = value;
		OVERRIDES.put(path, parsed);
		bake();
	}

	public static void clearNetworked() {
		OVERRIDES.clear();
		bake();
	}

	private static Object raw(String path) {
		if (OVERRIDES.containsKey(path))
			return OVERRIDES.get(path);
		ForgeConfigSpec.ConfigValue<?> v = VALUES.get(path);
		try {
			return v.get();
		} catch (IllegalStateException e) {
			// Config not loaded yet: use the default.
			return v.getDefault();
		}
	}

	private static boolean b(String path) {
		return (Boolean) raw(path);
	}

	private static int i(String path) {
		return (Integer) raw(path);
	}

	private static float f(String path) {
		return ((Number) raw(path)).floatValue();
	}

	private static String s(String path) {
		return String.valueOf(raw(path));
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<ResourceLocation> locations(String path) {
		ArrayList<ResourceLocation> list = new ArrayList<>();
		for (Object id : (List<Object>) raw(path))
			list.add(new ResourceLocation(String.valueOf(id)));
		return list;
	}

	private static int index(String value, int def, String... options) {
		for (int j = 0; j < options.length; j++)
			if (options[j].equalsIgnoreCase(value))
				return j;
		return def;
	}

	/* ---------------- spec building ---------------- */

	private static void push(String name) {
		category = name;
		builder.push(name);
	}

	private static void pop() {
		builder.pop();
		category = null;
	}

	private static String key(String name) {
		return category + "." + name;
	}

	private static String path(String name) {
		// TOML keys cannot contain dots; keep the readable name otherwise.
		return name.replace(".", "");
	}

	private static String bool(String name, boolean def, String comment) {
		VALUES.put(key(name), comment(comment).define(path(name), def));
		return key(name);
	}

	private static String integer(String name, int def, int min, int max, String comment) {
		VALUES.put(key(name), comment(comment).defineInRange(path(name), def, min, max));
		return key(name);
	}

	private static String real(String name, double def, double min, double max, String comment) {
		VALUES.put(key(name), comment(comment).defineInRange(path(name), def, min, max));
		return key(name);
	}

	private static String string(String name, String def, String comment) {
		VALUES.put(key(name), comment(comment).define(path(name), def));
		return key(name);
	}

	private static String list(String name, List<String> def, String comment) {
		VALUES.put(key(name), comment(comment).defineListAllowEmpty(List.of(path(name)), () -> def,
				o -> o instanceof String));
		return key(name);
	}

	private static ForgeConfigSpec.Builder comment(String comment) {
		return comment.isEmpty() ? builder : builder.comment(comment);
	}

	private static void net(String key) {
		NETWORKED.put(key, VALUES.get(key));
	}
}
