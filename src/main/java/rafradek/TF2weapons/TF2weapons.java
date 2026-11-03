package rafradek.TF2weapons;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rafradek.TF2weapons.arena.GameArena;
import rafradek.TF2weapons.block.*;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.command.TF2Commands;
import rafradek.TF2weapons.common.*;
import rafradek.TF2weapons.entity.*;
import rafradek.TF2weapons.entity.boss.EntityHHH;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import rafradek.TF2weapons.entity.boss.EntityMonoculus;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.entity.mercenary.*;
import rafradek.TF2weapons.entity.projectile.*;
import rafradek.TF2weapons.inventory.InventoryAmmoBelt;
import rafradek.TF2weapons.inventory.InventoryWearables;
import rafradek.TF2weapons.inventory.TF2GuiHandler;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.item.crafting.OpenCrateRecipe;
import rafradek.TF2weapons.loot.*;
import rafradek.TF2weapons.message.*;
import rafradek.TF2weapons.potion.PotionTF2;
import rafradek.TF2weapons.potion.PotionTF2Item;
import rafradek.TF2weapons.tileentity.*;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

/**
 * Main mod class. In 1.12 this did all registration in pre-init; in 1.19 every registry is filled
 * from {@link RegisterEvent}, while weapon data files are parsed in the constructor because they do
 * not depend on registries.
 */
@Mod(TF2weapons.MOD_ID)
public class TF2weapons {

	public static final Logger LOGGER = LogManager.getLogger("TF2 Stuff Mod");
	public static final String MOD_ID = "rafradek_tf2_weapons";

	public static final Capability<WeaponsCapability> WEAPONS_CAP = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<InventoryWearables> INVENTORY_CAP = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<InventoryAmmoBelt> INVENTORY_BELT_CAP = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<TF2EventsCommon.TF2WorldStorage> WORLD_CAP = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<WeaponData.WeaponDataCapability> WEAPONS_DATA_CAP = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<TF2PlayerCapability> PLAYER_CAP = CapabilityManager.get(new CapabilityToken<>() {});

	public static CreativeModeTab tabutilitytf2;
	public static CreativeModeTab tabweapontf2;
	public static CreativeModeTab tabsurvivaltf2;
	public static CreativeModeTab tabspawnertf2;
	public static CreativeModeTab tabrareweapontf2;
	public static CreativeModeTab tabarenatf2;

	public static TF2NetworkWrapper network;
	public static Item itemPlacer;
	public static Item mobHeldItem;

	public static TF2weapons instance;
	public static CommonProxy proxy;

	public File weaponDir;

	public static Block blockCabinet;
	public static Block blockCopperOre;
	public static Block blockProp;
	public static Block blockLeadOre;
	public static Block blockAustraliumOre;
	public static Block blockAustralium;
	public static Block blockUpgradeStation;
	public static Block blockAmmoFurnace;
	public static Block blockOverheadDoor;
	public static Block blockRobotDeploy;
	public static Block blockResupplyCabinet;
	public static Block blockCapturePoint;
	public static Block blockConfigure;

	public static MobEffect bonk;
	public static MobEffect stun;
	public static MobEffect crit;
	public static MobEffect buffbanner;
	public static MobEffect backup;
	public static MobEffect conch;
	public static MobEffect markDeath;
	public static MobEffect jarate;
	public static MobEffect madmilk;
	public static MobEffect critBoost;
	public static MobEffect charging;
	public static MobEffect uber;
	public static MobEffect it;
	public static MobEffect bombmrs;
	public static MobEffect bleeding;
	public static MobEffect noKnockback;
	public static MobEffect sapped;
	public static MobEffect regen;
	public static MobEffect viralfire;
	public static MobEffect shieldExplosive;
	public static MobEffect shieldBullet;
	public static MobEffect shieldFire;
	public static MobEffect gas;
	public static MobEffect quickFix;

	public static Item itemDisguiseKit;
	public static Item itemBuildingBox;
	public static Item itemSandvich;
	public static Item itemChocolate;
	public static Item itemAmmo;
	public static Item itemAmmoFire;
	public static Item itemAmmoPistol;
	public static Item itemAmmoMinigun;
	public static Item itemAmmoSMG;
	public static Item itemAmmoSyringe;
	public static Item itemAmmoPackage;
	public static Item itemAmmoMedigun;
	public static Item itemAmmoBelt;
	public static Item itemScoutBoots;
	public static Item itemMantreads;
	public static Item itemTF2;
	public static Item itemHorn;
	public static Item itemStatue;
	public static Item itemToken;
	public static Item itemTarget;
	public static Item itemPDA;
	public static Item itemKillstreak;
	public static Item itemGunboats;
	public static Item itemStrangifier;
	public static Item itemKillstreakFabricator;
	public static Item itemEventMaker;
	public static Item itemRobotPart;
	public static Item itemBossSpawn;
	public static Item itemDoorController;
	public static Item itemMoney;
	public static Item itemConfigurator;
	public static Item itemPickup;

	/* Entity types */
	public static EntityType<EntityHeavy> HEAVY;
	public static EntityType<EntityScout> SCOUT;
	public static EntityType<EntitySniper> SNIPER;
	public static EntityType<EntitySoldier> SOLDIER;
	public static EntityType<EntityPyro> PYRO;
	public static EntityType<EntityDemoman> DEMOMAN;
	public static EntityType<EntityMedic> MEDIC;
	public static EntityType<EntitySpy> SPY;
	public static EntityType<EntityEngineer> ENGINEER;
	public static EntityType<EntityRocket> ROCKET;
	public static EntityType<EntityFlame> FLAME;
	public static EntityType<EntityGrenade> GRENADE;
	public static EntityType<EntityStickybomb> STICKYBOMB;
	public static EntityType<EntityProjectileSimple> PROJECTILE_SIMPLE;
	public static EntityType<EntityStickProjectile> SYRINGE;
	public static EntityType<EntityFlare> FLARE;
	public static EntityType<EntityJar> JAR;
	public static EntityType<EntityBall> BALL;
	public static EntityType<EntitySentry> SENTRY;
	public static EntityType<EntityDispenser> DISPENSER;
	public static EntityType<EntityTeleporter> TELEPORTER;
	public static EntityType<EntitySaxtonHale> HALE;
	public static EntityType<EntityMonoculus> MONOCULUS;
	public static EntityType<EntityHHH> HHH;
	public static EntityType<EntityMerasmus> MERASMUS;
	public static EntityType<EntityCleaver> CLEAVER;
	public static EntityType<EntityTarget> TARGET;
	public static EntityType<EntityStatue> STATUE;
	public static EntityType<EntityProjectileEnergy> PROJECTILE_ENERGY;
	public static EntityType<EntityOnyx> ONYX;
	public static EntityType<EntityFuryFireball> FURY_FIREBALL;
	public static EntityType<EntityPickup> PICKUP;
	public static EntityType<EntityGrapplingHook> GRAPPLING_HOOK;
	public static EntityType<EntityProjectileShortCircuitOrb> SHORT_CIRCUIT_ORB;
	public static EntityType<EntityDummy> DUMMY;

	/* Block entity types */
	public static BlockEntityType<TileEntityUpgrades> TILE_UPGRADES;
	public static BlockEntityType<TileEntityAmmoFurnace> TILE_AMMO_FURNACE;
	public static BlockEntityType<TileEntityOverheadDoor> TILE_OVERHEAD_DOOR;
	public static BlockEntityType<TileEntityRobotDeploy> TILE_ROBOT_DEPLOY;
	public static BlockEntityType<TileEntityResupplyCabinet> TILE_RESUPPLY_CABINET;
	public static BlockEntityType<TileEntityCapturePoint> TILE_CAPTURE_POINT;
	public static BlockEntityType<TileEntityGameConfigure> TILE_GAME_CONFIGURE;

	/** Menus, indexed by the 1.12 GUI id (see {@link TF2GuiHandler}). */
	public static final MenuType<?>[] MENU_TYPES = new MenuType<?>[8];

	public static RecipeSerializer<OpenCrateRecipe> OPEN_CRATE_RECIPE;

	public static LootItemFunctionType SET_META;
	public static LootItemFunctionType RANDOM_META;
	public static LootItemFunctionType SET_WEAPON_CLASS;
	public static LootItemFunctionType SET_WEAPON_UPGRADED;
	public static LootItemFunctionType RANDOM_WEAPON_ITEM;
	public static LootItemConditionType KILLED_BY_PLAYER_TEAM;
	public static LootItemConditionType NBT_TEST;
	public static LootItemConditionType OR;

	public static final ResourceLocation lootTF2Character = id("entities/tf2character");
	public static final ResourceLocation lootScout = id("entities/scout");
	public static final ResourceLocation lootSpy = id("entities/spy");
	public static final ResourceLocation lootHeavy = id("entities/heavy");
	public static final ResourceLocation lootEngineer = id("entities/engineer");
	public static final ResourceLocation lootMedic = id("entities/medic");
	public static final ResourceLocation lootPyro = id("entities/pyro");
	public static final ResourceLocation lootSoldier = id("entities/soldier");
	public static final ResourceLocation lootDemoman = id("entities/demoman");
	public static final ResourceLocation lootSniper = id("entities/sniper");
	public static final ResourceLocation lootHale = id("entities/hale");
	public static final ResourceLocation lootTF2Base = id("chests/tf2_base");

	public static byte[] itemDataCompressed;

	public static final ResourceLocation cratesOpened = id("crates_opened");
	public static final ResourceLocation robotsKilled = id("robots_killed");
	public static MinecraftServer server;
	public static EntityDummy dummyEnt;

	public static ArrayList<ResourceLocation> animalsDisguise = new ArrayList<>();

	public static boolean squakeLoaded;
	public static boolean corrupted;

	public static Map<String, GameArena> gameArenas = new HashMap<>();

	public static int getCurrentWeaponVersion() {
		return 50;
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	public TF2weapons() {
		instance = this;
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TF2ConfigVars.SPEC);
		modBus.addListener((ModConfigEvent.Loading e) -> TF2ConfigVars.bake());
		modBus.addListener((ModConfigEvent.Reloading e) -> TF2ConfigVars.bake());
		modBus.addListener(this::register);
		modBus.addListener(this::registerCapabilities);
		modBus.addListener(this::commonSetup);
		modBus.addListener(this::entityAttributes);

		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.register(new TF2EventsCommon());
		forgeBus.addListener(this::serverStarting);
		forgeBus.addListener(this::serverStopping);
		forgeBus.addListener(this::registerCommands);

		proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		proxy.construct(modBus);

		createTabs();
		loadWeaponData();
	}

	/* ------------------------------------------------------------------ */
	/* Weapon data */
	/* ------------------------------------------------------------------ */

	private void loadWeaponData() {
		LOGGER.info("Initializing attributes");
		MapList.initMaps();
		TF2Attribute.initAttributes();
		PropertyType.init();
		ItemKillstreakFabricator.initKillstreaks();

		File configDir = FMLPaths.CONFIGDIR.get().toFile();
		this.weaponDir = new File(configDir, "TF2WeaponsLists");
		if (!this.weaponDir.exists())
			this.weaponDir.mkdirs();
		File versionFile = new File(this.weaponDir, "version.txt");
		int version = 0;
		try {
			if (versionFile.exists())
				version = Integer.parseInt(Files.readString(versionFile.toPath()).trim());
		} catch (Exception e) {
			version = 0;
		}
		File squadFile = new File(configDir, "TF2RobotSquad.json");
		if (version < getCurrentWeaponVersion() || !new File(this.weaponDir, "Weapons.json").exists()) {
			extractData("weapons/weapons.json", new File(this.weaponDir, "Weapons.json"));
			extractData("weapons/cosmetics.json", new File(this.weaponDir, "Cosmetics.json"));
			extractData("weapons/crates.json", new File(this.weaponDir, "Crates.json"));
			extractData("robots/robot_squad.json", squadFile);
			try {
				Files.writeString(versionFile.toPath(), String.valueOf(getCurrentWeaponVersion()));
			} catch (IOException e) {
				LOGGER.error("Cannot write weapon config version", e);
			}
		}
		if (!squadFile.exists())
			extractData("robots/robot_squad.json", squadFile);

		LOGGER.info("Parsing weapon files");
		loadWeapons();
		try {
			InvasionEvent.squads = Squad.parseFile(squadFile);
		} catch (Exception e) {
			LOGGER.error("Cannot read robot squads", e);
		}
	}

	public static void extractData(String input, File output) {
		try (InputStream in = TF2weapons.class.getResourceAsStream("/assets/" + MOD_ID + "/" + input)) {
			if (in == null) {
				LOGGER.error("Missing built-in data file {}", input);
				return;
			}
			Files.write(output.toPath(), in.readAllBytes());
		} catch (IOException e) {
			LOGGER.error("Cannot extract " + input, e);
		}
	}

	public static void loadWeapons() {
		MapList.nameToData.clear();
		MapList.buildInAttributes.clear();
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(output)));

			loadConfig(new File(instance.weaponDir, "Weapons.json"), stream);
			File[] files = instance.weaponDir.listFiles(
					(FilenameFilter) (dir, name) -> name.endsWith(".json") && !name.equalsIgnoreCase("Weapons.json"));
			if (files != null)
				for (File file : files)
					loadConfig(file, stream);
			stream.flush();
			stream.close();
			itemDataCompressed = output.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadConfig(File file, DataOutput output) throws IOException {
		loadConfig(Files.readString(file.toPath(), StandardCharsets.UTF_8), file.getName(), output);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void loadConfig(String fileData, String fileName, DataOutput output) {
		ArrayList<WeaponData> list = WeaponData.parseFile(fileData, fileName);
		for (WeaponData data : list) {
			String weaponEntry = data.getName();
			try {
				if (PropertyType.BASED_ON.contains(data)
						&& MapList.nameToData.containsKey(data.getString(PropertyType.BASED_ON)))
					data = attach(MapList.nameToData.get(data.getString(PropertyType.BASED_ON)), data);
				loadWeapon(weaponEntry, data);

				output.writeUTF(data.getName());
				output.writeByte(data.properties.size());
				for (PropertyType type : data.properties.keySet()) {
					output.writeByte(type.id);
					type.serialize(output, data, data.get(type));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void loadWeapon(String name, WeaponData weapon) {
		MapList.nameToData.put(name, weapon);
		CompoundTag tag2 = new CompoundTag();
		if (weapon.hasProperty(PropertyType.ATTRIBUTES))
			for (Entry<TF2Attribute, Float> entry : weapon.get(PropertyType.ATTRIBUTES).attributes.entrySet())
				tag2.putFloat(String.valueOf(entry.getKey().id), entry.getValue());
		MapList.buildInAttributes.put(name, tag2);
	}

	public static WeaponData attach(WeaponData base, WeaponData additional) {
		for (PropertyType<?> prop : base.properties.keySet())
			if (!additional.properties.containsKey(prop))
				additional.properties.put(prop, base.properties.get(prop));
		MapList.buildInAttributes.put(additional.getName(), MapList.buildInAttributes.get(base.getName()));
		return additional;
	}

	/* ------------------------------------------------------------------ */
	/* Creative tabs */
	/* ------------------------------------------------------------------ */

	private static void createTabs() {
		tabweapontf2 = new CreativeModeTab("tf2weapons") {
			@Override
			public ItemStack makeIcon() {
				return ItemFromData.getNewStack("minigun");
			}
		};
		tabspawnertf2 = new CreativeModeTab("tf2spawner") {
			@Override
			public ItemStack makeIcon() {
				return TF2Util.metaStack(itemBuildingBox, 1, 18);
			}
		};
		tabrareweapontf2 = new CreativeModeTab("tf2rareweapons") {
			@Override
			public ItemStack makeIcon() {
				return ItemFromData.getNewStack("rocketshotgun");
			}
		};
		tabutilitytf2 = new CreativeModeTab("tf2util") {
			@Override
			public ItemStack makeIcon() {
				return new ItemStack(itemDisguiseKit);
			}

			@Override
			public void fillItemList(NonNullList<ItemStack> list) {
				super.fillItemList(list);
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 2; j++) {
						if (i == 2 && j == 1)
							continue;
						ItemStack banner = new ItemStack(
								i == 0 ? Items.RED_BANNER : (i == 1 ? Items.BLUE_BANNER : Items.GRAY_BANNER));
						ListTag patterns = new ListTag();
						banner.getOrCreateTagElement("BlockEntityTag").put("Patterns", patterns);
						CompoundTag pattern = new CompoundTag();
						pattern.putInt("Color", DyeColor.BLACK.getId());
						pattern.putString("Pattern", i == 0 ? "rb" : (i == 1 ? "bb" : "nb"));
						patterns.add(pattern);
						if (j == 1) {
							CompoundTag patternfast = new CompoundTag();
							patternfast.putString("Pattern", "fs");
							patternfast.putInt("Color", DyeColor.BLACK.getId());
							patterns.add(patternfast);
						}
						list.add(banner);
					}
				}
			}
		};
		tabsurvivaltf2 = new CreativeModeTab("tf2misc") {
			@Override
			public ItemStack makeIcon() {
				return new ItemStack(blockCabinet);
			}
		};
		tabarenatf2 = new CreativeModeTab("tf2arena") {
			@Override
			public ItemStack makeIcon() {
				return TF2Util.metaStack(itemPickup, 1, 2);
			}
		};
	}

	/* ------------------------------------------------------------------ */
	/* Registration */
	/* ------------------------------------------------------------------ */

	private void register(RegisterEvent event) {
		event.register(ForgeRegistries.Keys.BLOCKS, TF2weapons::registerBlocks);
		event.register(ForgeRegistries.Keys.ITEMS, TF2weapons::registerItems);
		event.register(ForgeRegistries.Keys.MOB_EFFECTS, TF2weapons::registerEffects);
		event.register(ForgeRegistries.Keys.ENTITY_TYPES, TF2weapons::registerEntities);
		event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, TF2weapons::registerTileEntities);
		event.register(ForgeRegistries.Keys.SOUND_EVENTS, helper -> {
			TF2Sounds.registerWeaponSounds();
			TF2Sounds.SOUND_EVENTS.forEach(helper::register);
		});
		event.register(ForgeRegistries.Keys.MENU_TYPES, helper -> {
			for (int i = 0; i < MENU_TYPES.length; i++) {
				final int guiId = i;
				MenuType<?> type = IForgeMenuType
						.create((windowId, inv, buf) -> TF2GuiHandler.createClientMenu(guiId, windowId, inv, buf));
				MENU_TYPES[i] = type;
				helper.register(id("gui_" + i), type);
			}
		});
		event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS, helper -> {
			OPEN_CRATE_RECIPE = new SimpleRecipeSerializer<>(OpenCrateRecipe::new);
			helper.register(id("open_crate"), OPEN_CRATE_RECIPE);
		});
		event.register(Registry.LOOT_FUNCTION_REGISTRY, helper -> {
			helper.register(id("set_meta"), SET_META = new LootItemFunctionType(new SetMetaFunction.Serializer()));
			helper.register(id("random_meta"),
					RANDOM_META = new LootItemFunctionType(new EntityBuildingFunction.Serializer()));
			helper.register(id("set_weapon_class"),
					SET_WEAPON_CLASS = new LootItemFunctionType(new EntityOfClassFunction.Serializer()));
			helper.register(id("set_weapon_upgraded"),
					SET_WEAPON_UPGRADED = new LootItemFunctionType(new RandomWeaponFunction.Serializer()));
			helper.register(id("random_weapon_item"),
					RANDOM_WEAPON_ITEM = new LootItemFunctionType(new RandomWeaponItemFunction.Serializer()));
		});
		event.register(Registry.LOOT_ITEM_REGISTRY, helper -> {
			helper.register(id("killed_by_player_team"),
					KILLED_BY_PLAYER_TEAM = new LootItemConditionType(new KilledByTeam.Serializer()));
			helper.register(id("nbt_test"), NBT_TEST = new LootItemConditionType(new EntityNBTCondition.Serializer()));
			helper.register(id("or"), OR = new LootItemConditionType(new EntityOrCondition.Serializer()));
		});
		event.register(Registry.CUSTOM_STAT_REGISTRY, helper -> {
			helper.register(cratesOpened, cratesOpened);
			helper.register(robotsKilled, robotsKilled);
		});
		event.register(Registry.BANNER_PATTERN_REGISTRY, helper -> {
			helper.register(id("red_base"), new BannerPattern("rb"));
			helper.register(id("blu_base"), new BannerPattern("bb"));
			helper.register(id("neutral_base"), new BannerPattern("nb"));
			helper.register(id("fast_spawn"), new BannerPattern("fs"));
		});
	}

	private static BlockBehaviour.Properties blockProps(Material material, MaterialColor color, float hardness,
			float resistance) {
		return BlockBehaviour.Properties.of(material, color).strength(hardness, resistance);
	}

	private static void registerBlocks(RegisterEvent.RegisterHelper<Block> helper) {
		helper.register(id("tf2workbench"), blockCabinet = new BlockCabinet(
				blockProps(Material.METAL, MaterialColor.METAL, 5f, 10f).requiresCorrectToolForDrops()));
		helper.register(id("ammo_furnace"), blockAmmoFurnace = new BlockAmmoFurnace(false,
				blockProps(Material.METAL, MaterialColor.METAL, 5f, 10f).requiresCorrectToolForDrops()));
		helper.register(id("upgrade_station"), blockUpgradeStation = new BlockUpgradeStation(
				blockProps(Material.METAL, MaterialColor.METAL, 5f, 10f).requiresCorrectToolForDrops()));
		helper.register(id("copper_ore"), blockCopperOre = new DropExperienceBlock(
				blockProps(Material.STONE, MaterialColor.STONE, 3f, 5f).requiresCorrectToolForDrops()));
		helper.register(id("lead_ore"), blockLeadOre = new DropExperienceBlock(
				blockProps(Material.STONE, MaterialColor.STONE, 3f, 5f).requiresCorrectToolForDrops()));
		helper.register(id("australium_ore"), blockAustraliumOre = new DropExperienceBlock(
				blockProps(Material.STONE, MaterialColor.STONE, 7f, 10f).requiresCorrectToolForDrops()));
		helper.register(id("australium_block"), blockAustralium = new Block(
				blockProps(Material.METAL, MaterialColor.GOLD, 9f, 20f).requiresCorrectToolForDrops()
						.sound(SoundType.METAL)));
		helper.register(id("overhead_door"), blockOverheadDoor = new BlockOverheadDoor(
				blockProps(Material.METAL, MaterialColor.METAL, 3f, 5f).noOcclusion()));
		helper.register(id("robot_deploy"), blockRobotDeploy = new BlockRobotDeploy(
				blockProps(Material.METAL, MaterialColor.METAL, 3f, 5f)));
		helper.register(id("resupply_cabinet"), blockResupplyCabinet = new BlockResupplyCabinet(
				blockProps(Material.METAL, MaterialColor.METAL, -1f, 6000000f).noLootTable()));
		helper.register(id("capture_point"), blockCapturePoint = new BlockCapturePoint(
				blockProps(Material.METAL, MaterialColor.METAL, -1f, 6000000f).noLootTable()));
		helper.register(id("game_configure"), blockConfigure = new BlockGameConfigure(
				blockProps(Material.METAL, MaterialColor.METAL, -1f, 6000000f).noLootTable()));
		helper.register(id("prop_block"), blockProp = new BlockProp(
				blockProps(Material.WOOD, MaterialColor.GOLD, 0.75f, 2f).noLootTable()));
	}

	private static Item.Properties props(CreativeModeTab tab) {
		return new Item.Properties().tab(tab);
	}

	private static void registerItems(RegisterEvent.RegisterHelper<Item> helper) {
		helper.register(id("placer"), itemPlacer = new ItemMonsterPlacerPlus());
		helper.register(id("disguise_kit"), itemDisguiseKit = new ItemDisguiseKit());
		helper.register(id("building_box"), itemBuildingBox = new ItemBuildingBox());
		helper.register(id("sandvich"), itemSandvich = new ItemFoodThrowable(14, 2F, false, 600,
				() -> new MobEffectInstance(MobEffects.REGENERATION, 120, 2), props(tabutilitytf2)));
		helper.register(id("chocolate"), itemChocolate = new ItemFoodThrowable(7, 1.5F, false, 200,
				() -> new MobEffectInstance(MobEffects.ABSORPTION, 600, 0), props(tabutilitytf2)));
		helper.register(id("horn"), itemHorn = new ItemHorn());
		helper.register(id("ammo"), itemAmmo = new ItemAmmo());
		helper.register(id("ammo_box"), itemAmmoPackage = new ItemAmmoPackage());
		helper.register(id("ammo_fire"), itemAmmoFire = new ItemFireAmmo(10, 350, 1));
		helper.register(id("ammo_medigun"), itemAmmoMedigun = new ItemFireAmmo(12, 11200, 1));
		helper.register(id("ammo_pistol"), itemAmmoPistol = new ItemFireAmmo(3, 12, 12));
		helper.register(id("ammo_minigun"), itemAmmoMinigun = new ItemFireAmmo(2, 100, 2));
		helper.register(id("ammo_smg"), itemAmmoSMG = new ItemFireAmmo(5, 25, 8));
		helper.register(id("ammo_syringe"), itemAmmoSyringe = new ItemFireAmmo(9, 40, 6));
		helper.register(id("ammo_belt"), itemAmmoBelt = new ItemAmmoBelt());
		helper.register(id("scout_shoes"),
				itemScoutBoots = new ItemArmorTF2(ArmorMaterials.LEATHER, "Allows double jumping", 0f));
		helper.register(id("mantreads"), itemMantreads = new ItemArmorTF2(ArmorMaterials.IRON,
				"Deals 1.8x falling damage to the player you land on", 0.75f));
		helper.register(id("gunboats"),
				itemGunboats = new ItemArmorTF2(ArmorMaterials.IRON, "Reduces blast jumping damage by 60%", 0f));
		helper.register(id("statue"), itemStatue = new ItemStatue());
		helper.register(id("token"), itemToken = new ItemToken());
		helper.register(id("item_tf2"), itemTF2 = new ItemTF2());
		helper.register(id("robot_part"), itemRobotPart = new ItemRobotPart());
		helper.register(id("target"), itemTarget = new ItemTarget());
		helper.register(id("killstreakkit"), itemKillstreak = new ItemKillstreakKit());
		helper.register(id("strangifier"), itemStrangifier = new ItemStrangifier());
		helper.register(id("killstreakfabricator"), itemKillstreakFabricator = new ItemKillstreakFabricator());
		helper.register(id("eventmaker"), itemEventMaker = new ItemEventMaker());
		helper.register(id("boss_spawner"), itemBossSpawn = new ItemBossSpawner());
		helper.register(id("door_controller"), itemDoorController = new ItemDoorController());
		helper.register(id("money"), itemMoney = new ItemMoney());
		helper.register(id("configurator"), itemConfigurator = new ItemConfigure());
		helper.register(id("pickup"), itemPickup = new ItemPickup());

		// Weapon items, one per weapon class ("bullet", "minigun", ...). Several classes can share
		// one item instance, which must only be registered once.
		MapList.initWeaponItems();
		Set<Item> done = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Entry<String, Item> entry : MapList.weaponClasses.entrySet())
			if (done.add(entry.getValue()))
				helper.register(id(entry.getKey().toLowerCase(Locale.ROOT)), entry.getValue());

		for (Block block : new Block[] { blockCabinet, blockAmmoFurnace, blockUpgradeStation, blockCopperOre,
				blockLeadOre, blockAustraliumOre, blockAustralium, blockOverheadDoor, blockRobotDeploy,
				blockResupplyCabinet, blockCapturePoint, blockConfigure }) {
			CreativeModeTab tab = block == blockResupplyCabinet || block == blockCapturePoint
					|| block == blockConfigure ? tabarenatf2 : tabsurvivaltf2;
			helper.register(ForgeRegistries.BLOCKS.getKey(block), new BlockItem(block, props(tab)));
		}
	}

	private static void registerEffects(RegisterEvent.RegisterHelper<MobEffect> helper) {
		helper.register(id("bonk_eff"), bonk = new PotionTF2Item(false, 0x696969, icon("items/bonk"), "effect.bonk"));
		helper.register(id("stun_eff"), stun = new PotionTF2(true, 0, 3, 1, "effect.stun").addAttributeModifier(
				Attributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B354F0D8BA", -0.5D, Operation.MULTIPLY_TOTAL));
		helper.register(id("crit_eff"), crit = new PotionTF2Item(false, 0, icon("items/critacola"), "effect.crit")
				.addAttributeModifier(Attributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B354E56B59", 0.35D,
						Operation.MULTIPLY_BASE));
		helper.register(id("banner_eff"),
				buffbanner = new PotionTF2Item(false, 0, icon("items/buffbanner"), "effect.banner"));
		helper.register(id("backup_eff"), backup = new PotionTF2Item(false, 0, icon("items/backup"), "effect.backup"));
		helper.register(id("conch_eff"), conch = new PotionTF2Item(false, 0, icon("items/conch"), "effect.conch")
				.addAttributeModifier(Attributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B35B5565E2", 0.35D,
						Operation.MULTIPLY_BASE));
		helper.register(id("mark_death_eff"), markDeath = new PotionTF2(true, 0, 1, 2, "effect.markDeath"));
		helper.register(id("crit_boost_eff"), critBoost = new PotionTF2(false, 0, 4, 0, "effect.critBoost"));
		helper.register(id("quick_fix_eff"),
				quickFix = new PotionTF2Item(false, 0, icon("items/medigun_im"), "effect.quickfix")
						.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "0d09b7c3-ac5e-4f56-9b65-fe2d09b99dc1",
								10, Operation.ADDITION));
		helper.register(id("jarate_eff"),
				jarate = new PotionTF2Item(true, 0xFFD500, icon("items/jarate"), "effect.jarate"));
		helper.register(id("madmilk_eff"),
				madmilk = new PotionTF2Item(true, 0xF1F1F1, icon("items/madmilk"), "effect.madmilk"));
		helper.register(id("charging_eff"),
				charging = new PotionTF2Item(false, 0, icon("items/charging_targe"), "effect.charging") {
					@Override
					public void addAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
						super.addAttributeModifiers(entity, map, amplifier);
						entity.maxUpStep += amplifier;
						if (entity instanceof EntityTF2Character)
							((EntityTF2Character) entity).rotation *= 0.1f;
					}

					@Override
					public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
						super.removeAttributeModifiers(entity, map, amplifier);
						entity.maxUpStep -= amplifier;
						if (entity.getCapability(TF2weapons.WEAPONS_CAP).isPresent())
							WeaponsCapability.get(entity).setExpJump(true);
						if (entity instanceof EntityTF2Character)
							((EntityTF2Character) entity).rotation *= 10f;
					}
				}.addAttributeModifier(Attributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B35B5565E6", 1.5D,
						Operation.MULTIPLY_TOTAL));
		helper.register(id("uber_eff"), uber = new PotionTF2Item(false, 0, icon("items/medigun_im"), "effect.uber"));
		helper.register(id("it_eff"), it = new PotionTF2(true, 0xFFFFFFFF, 1, 2, "effect.it"));
		helper.register(id("bomb_eff"), bombmrs = new PotionTF2(false, 0xFFFFFFFF, 1, 2, "effect.bombmrs")
				.addAttributeModifier(Attributes.MOVEMENT_SPEED, "7107DE6F-7CE8-4030-940E-14B354F0D8BA", 1.25D,
						Operation.MULTIPLY_TOTAL));
		helper.register(id("bleeding_eff"), bleeding = new PotionTF2(true, 0, 4, 0, "effect.bleeding"));
		helper.register(id("no_knockback_eff"), noKnockback = new PotionTF2(true, 0, 0, 0, "effect.noKnockback")
				.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "0d09b7c3-ac5e-4f56-9b65-fe2d09b99dc3", 10,
						Operation.ADDITION));
		helper.register(id("sapped"), sapped = new PotionTF2(true, 0, 0, 0, "effect.sapped") {
			@Override
			public void addAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
				super.addAttributeModifiers(entity, map, amplifier);
				if (entity instanceof EntityTF2Character)
					((EntityTF2Character) entity).rotation *= ((EntityTF2Character) entity).isGiant() ? 0.4f : 0.1f;
			}

			@Override
			public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
				super.removeAttributeModifiers(entity, map, amplifier);
				if (entity instanceof EntityTF2Character)
					((EntityTF2Character) entity).rotation *= ((EntityTF2Character) entity).isGiant() ? 2.5f : 10f;
			}
		}.addAttributeModifier(Attributes.MOVEMENT_SPEED, "7107DE6F-7CE8-4030-940E-14B354F0D4AB", -0.5D,
				Operation.MULTIPLY_TOTAL));
		helper.register(id("regen_hit"), regen = new PotionTF2(false, 0, 0, 0, "effect.regenHit") {
			@Override
			public boolean isDurationEffectTick(int duration, int amplifier) {
				return duration % 5 == 0;
			}

			@Override
			public void applyEffectTick(LivingEntity entity, int amplifier) {
				entity.heal((amplifier + 1) * 0.5f * Math.min(1, TF2Util.getReducedHealing(entity, entity, 1.5f)));
			}
		});
		helper.register(id("viral_fire"), viralfire = new PotionTF2(true, 0, 0, 0, "effect.viralFire") {
			@Override
			public boolean isDurationEffectTick(int duration, int amplifier) {
				return amplifier != 0 && duration % 2 == 0;
			}

			@Override
			public void applyEffectTick(LivingEntity entity, int amplifier) {
				float range = 0.75f + 0.75f * amplifier;
				for (LivingEntity target : entity.level.getEntitiesOfClass(LivingEntity.class,
						entity.getBoundingBox().inflate(range, range, range))) {
					if (!(TF2Util.getOwnerIfOwnable(target) instanceof Player && TF2Util.isEnemy(entity, target))
							&& target.getEffect(viralfire) == null) {
						int firetime = entity.getRemainingFireTicks();
						target.setSecondsOnFire(firetime / 20 + 1);
						target.hurt(DamageSource.ON_FIRE, amplifier * 2);
						target.addEffect(new MobEffectInstance(viralfire, firetime, amplifier));
					}
				}
			}

			@Override
			public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
				super.removeAttributeModifiers(entity, map, amplifier);
				if (amplifier > 0 && entity.getEffect(viralfire) == null)
					entity.addEffect(new MobEffectInstance(viralfire, 40, 0));
			}
		});
		helper.register(id("shield_bullet"),
				shieldBullet = new PotionTF2Item(false, 0, icon("misc/vac_bullet_red"), "effect.shieldBullet"));
		helper.register(id("shield_explosive"), shieldExplosive = new PotionTF2Item(false, 0,
				icon("misc/vac_explosive_red"), "effect.shieldExplosive"));
		helper.register(id("shield_fire"),
				shieldFire = new PotionTF2Item(false, 0, icon("misc/vac_fire_red"), "effect.shieldFire"));
		helper.register(id("gaspasser_eff"),
				gas = new PotionTF2Item(true, 0x2B3E00, icon("misc/vac_fire_red"), "effect.gas"));
	}

	private static ResourceLocation icon(String path) {
		return id("textures/" + path + ".png");
	}

	private static <T extends Entity> EntityType<T> entity(RegisterEvent.RegisterHelper<EntityType<?>> helper,
			String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height,
			int trackingRange, int updateInterval, boolean velocityUpdates) {
		EntityType<T> type = EntityType.Builder.of(factory, category).sized(width, height)
				.clientTrackingRange(Math.max(1, trackingRange / 16)).updateInterval(updateInterval)
				.setShouldReceiveVelocityUpdates(velocityUpdates).build(MOD_ID + ":" + name);
		helper.register(id(name), type);
		return type;
	}

	private static void registerEntities(RegisterEvent.RegisterHelper<EntityType<?>> helper) {
		int merc = TF2ConfigVars.updateDelay;
		MobCategory mon = MobCategory.MONSTER;
		MobCategory misc = MobCategory.MISC;
		HEAVY = entity(helper, "heavy", EntityHeavy::new, mon, 0.6f, 1.99f, 80, merc, true);
		SCOUT = entity(helper, "scout", EntityScout::new, mon, 0.6f, 1.8f, 80, merc, true);
		SNIPER = entity(helper, "sniper", EntitySniper::new, mon, 0.6f, 1.8f, 80, merc, true);
		SOLDIER = entity(helper, "soldier", EntitySoldier::new, mon, 0.6f, 1.8f, 80, merc, true);
		PYRO = entity(helper, "pyro", EntityPyro::new, mon, 0.6f, 1.8f, 80, merc, true);
		DEMOMAN = entity(helper, "demoman", EntityDemoman::new, mon, 0.6f, 1.8f, 80, merc, true);
		MEDIC = entity(helper, "medic", EntityMedic::new, mon, 0.6f, 1.8f, 80, merc, true);
		SPY = entity(helper, "spy", EntitySpy::new, mon, 0.6f, 1.8f, 80, merc, true);
		ENGINEER = entity(helper, "engineer", EntityEngineer::new, mon, 0.6f, 1.8f, 80, merc, true);
		ROCKET = entity(helper, "rocket", EntityRocket::new, misc, 0.5f, 0.5f, 64, 20, false);
		FLAME = entity(helper, "flame", EntityFlame::new, misc, 0.5f, 0.5f, 16, 20, false);
		GRENADE = entity(helper, "grenade", EntityGrenade::new, misc, 0.3f, 0.3f, 64, 5, true);
		STICKYBOMB = entity(helper, "sticky", EntityStickybomb::new, misc, 0.3f, 0.3f, 64, 5, true);
		PROJECTILE_SIMPLE = entity(helper, "simple", EntityProjectileSimple::new, misc, 0.3f, 0.3f, 64, 20, false);
		SYRINGE = entity(helper, "syringe", EntityStickProjectile::new, misc, 0.3f, 0.3f, 64, 20, false);
		FLARE = entity(helper, "flare", EntityFlare::new, misc, 0.3f, 0.3f, 64, 20, false);
		JAR = entity(helper, "jar", EntityJar::new, misc, 0.5f, 0.5f, 64, 20, false);
		BALL = entity(helper, "ball", EntityBall::new, misc, 0.3f, 0.3f, 64, 10, true);
		SENTRY = entity(helper, "sentry", EntitySentry::new, mon, 0.8f, 0.8f, 80, 2, true);
		DISPENSER = entity(helper, "dispenser", EntityDispenser::new, mon, 1f, 1.1f, 80, 40, true);
		TELEPORTER = entity(helper, "teleporter", EntityTeleporter::new, mon, 1f, 0.2f, 80, 40, true);
		HALE = entity(helper, "hale", EntitySaxtonHale::new, MobCategory.CREATURE, 0.6f, 1.99f, 80, 3, true);
		MONOCULUS = entity(helper, "monoculus", EntityMonoculus::new, mon, 4f, 4f, 80, 3, true);
		HHH = entity(helper, "hhh", EntityHHH::new, mon, 0.9f, 2.85f, 80, 3, true);
		MERASMUS = entity(helper, "merasmus", EntityMerasmus::new, mon, 1.15f, 3.5f, 80, 3, true);
		CLEAVER = entity(helper, "cleaver", EntityCleaver::new, misc, 0.3f, 0.3f, 64, 20, false);
		TARGET = entity(helper, "target", EntityTarget::new, misc, 0.6f, 1.8625f, 80, 20, false);
		STATUE = entity(helper, "statue", EntityStatue::new, misc, 0.6f, 1.8f, 80, 20, false);
		PROJECTILE_ENERGY = entity(helper, "projenergy", EntityProjectileEnergy::new, misc, 0.3f, 0.3f, 64, 20, false);
		ONYX = entity(helper, "onyx", EntityOnyx::new, misc, 0.5f, 0.5f, 64, 20, false);
		FURY_FIREBALL = entity(helper, "fireball", EntityFuryFireball::new, misc, 0.3f, 0.3f, 64, 20, false);
		PICKUP = entity(helper, "pickup", EntityPickup::new, misc, 0.25f, 0.25f, 80, 20, false);
		GRAPPLING_HOOK = entity(helper, "grapplinghook", EntityGrapplingHook::new, misc, 0.3f, 0.3f, 64, 20, false);
		SHORT_CIRCUIT_ORB = entity(helper, "shortcircuitorb", EntityProjectileShortCircuitOrb::new, misc, 0.3f, 0.3f,
				64, 20, false);
		DUMMY = EntityType.Builder.<EntityDummy>of(EntityDummy::new, misc).sized(0.01f, 0.01f).noSave().noSummon()
				.build(MOD_ID + ":dummy");
		helper.register(id("dummy"), DUMMY);
	}

	private void entityAttributes(EntityAttributeCreationEvent event) {
		for (EntityType<? extends EntityTF2Character> type : List.of(HEAVY, SCOUT, SNIPER, SOLDIER, PYRO, DEMOMAN,
				MEDIC, SPY, ENGINEER))
			event.put(type, EntityTF2Character.createAttributes().build());
		event.put(SENTRY, EntitySentry.createAttributes().build());
		event.put(DISPENSER, EntityDispenser.createAttributes().build());
		event.put(TELEPORTER, EntityTeleporter.createAttributes().build());
		event.put(HALE, EntitySaxtonHale.createAttributes().build());
		event.put(MONOCULUS, EntityMonoculus.createAttributes().build());
		event.put(HHH, EntityHHH.createAttributes().build());
		event.put(MERASMUS, EntityMerasmus.createAttributes().build());
		event.put(TARGET, ArmorStand.createLivingAttributes().build());
		event.put(DUMMY, Mob.createMobAttributes().build());
	}

	private static <T extends BlockEntity> BlockEntityType<T> tile(
			RegisterEvent.RegisterHelper<BlockEntityType<?>> helper, String name,
			BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
		BlockEntityType<T> type = BlockEntityType.Builder.of(factory, blocks).build(null);
		helper.register(id(name), type);
		return type;
	}

	private static void registerTileEntities(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
		TILE_UPGRADES = tile(helper, "upgrade_station", TileEntityUpgrades::new, blockUpgradeStation);
		TILE_AMMO_FURNACE = tile(helper, "ammo_furnace", TileEntityAmmoFurnace::new, blockAmmoFurnace);
		TILE_OVERHEAD_DOOR = tile(helper, "overhead_door", TileEntityOverheadDoor::new, blockOverheadDoor);
		TILE_ROBOT_DEPLOY = tile(helper, "robot_deploy", TileEntityRobotDeploy::new, blockRobotDeploy);
		TILE_RESUPPLY_CABINET = tile(helper, "resupply_cabinet", TileEntityResupplyCabinet::new, blockResupplyCabinet);
		TILE_CAPTURE_POINT = tile(helper, "capture_point", TileEntityCapturePoint::new, blockCapturePoint);
		TILE_GAME_CONFIGURE = tile(helper, "game_configure", TileEntityGameConfigure::new, blockConfigure);
	}

	private void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(TF2PlayerCapability.class);
		event.register(WeaponsCapability.class);
		event.register(InventoryWearables.class);
		event.register(InventoryAmmoBelt.class);
		event.register(WeaponData.WeaponDataCapability.class);
		event.register(TF2EventsCommon.TF2WorldStorage.class);
		event.register(ItemCrate.CrateContent.class);
		event.register(ItemFromData.AttributeProvider.class);
	}

	/* ------------------------------------------------------------------ */
	/* Setup */
	/* ------------------------------------------------------------------ */

	private void commonSetup(FMLCommonSetupEvent event) {
		network = new TF2NetworkWrapper(MOD_ID);
		network.registerMessage(TF2ActionHandler.class, TF2Message.ActionMessage.class, 0, null, true);
		network.registerMessage(TF2UseHandler.class, TF2Message.UseMessage.class, 1, null, true);
		network.registerMessage(TF2PropertyHandler.class, TF2Message.PropertyMessage.class, 2, null, false);
		network.registerMessage(TF2BulletHandler.class, TF2Message.BulletMessage.class, 3, null, true);
		network.registerMessage(TF2ProjectileHandler.class, TF2Message.PredictionMessage.class, 4, null, true);
		network.registerMessage(TF2BuildingConfigHandler.class, TF2Message.BuildingConfigMessage.class, 5, null, false);
		network.registerMessage(TF2CapabilityHandler.class, TF2Message.CapabilityMessage.class, 6, null, false);
		network.registerMessage(TF2WeaponDataHandler.class, TF2Message.WeaponDataMessage.class, 8, null, false);
		network.registerMessage(TF2WearableChangeHandler.class, TF2Message.WearableChangeMessage.class, 10, null, false);
		network.registerMessage(TF2DisguiseHandler.class, TF2Message.DisguiseMessage.class, 11, null, false);
		network.registerMessage(TF2WeaponDropHandler.class, TF2Message.WeaponDroppedMessage.class, 12, null, true);
		network.registerMessage(TF2ContractHandler.class, TF2Message.ContractMessage.class, 13, null, false);
		network.registerMessage(TF2VelocityAddHandler.class, TF2Message.VelocityAddMessage.class, 14, null, true);
		network.registerMessage(TF2AttackSyncHandler.class, TF2Message.AttackSyncMessage.class, 15, null, true);
		network.registerMessage(TF2InitHandler.class, TF2Message.InitMessage.class, 16, null, false);
		network.registerMessage(TF2InitClientHandler.class, TF2Message.InitClientMessage.class, 17, null, false);
		network.registerMessage(TF2EffectCooldownHandler.class, TF2Message.EffectCooldownMessage.class, 20, null, false);
		network.registerMessage(TF2ParticleSpawnHandler.class, TF2Message.ParticleSpawnMessage.class, 21, null, true);
		network.registerMessage(TF2PlayerCapabilityHandler.class, TF2Message.PlayerCapabilityMessage.class, 22, null,
				false);
		network.registerMessage(TF2GuiConfigHandler.class, TF2Message.GuiConfigMessage.class, 23, null, false);
		network.registerMessage(TF2ShowGuiHandler.class, TF2Message.ShowGuiMessage.class, 25, null, false);
		network.registerMessage(TF2NetworkedSoundHandler.class, TF2Message.NetworkedSoundMessage.class, 27, null,
				false);

		event.enqueueWork(() -> {
			Stats.CUSTOM.get(cratesOpened, StatFormatter.DEFAULT);
			Stats.CUSTOM.get(robotsKilled, StatFormatter.DEFAULT);
			registerDispenserBehaviors();
		});

		squakeLoaded = TF2ConfigVars.isModLoaded("squake");
		animalsDisguise.clear();
		for (String name : new String[] { "pig", "sheep", "cow", "chicken", "horse" })
			animalsDisguise.add(new ResourceLocation(name));
	}

	private static void registerDispenserBehaviors() {
		DefaultDispenseItemBehavior spawnBehavior = new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack execute(BlockSource source, ItemStack stack) {
				Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
				double x = source.x() + facing.getStepX();
				double y = source.y() + 0.2F;
				double z = source.z() + facing.getStepZ();
				Entity entity = ItemMonsterPlacerPlus.spawnCreature(null, source.getLevel(), stack.getDamageValue(), x,
						y, z, stack.getTag() != null && stack.getTag().contains("SavedEntity")
								? stack.getTag().getCompound("SavedEntity")
								: null);
				if (entity instanceof Mob && stack.hasCustomHoverName())
					entity.setCustomName(stack.getHoverName());
				stack.shrink(1);
				return stack;
			}
		};
		DispenserBlock.registerBehavior(itemPlacer, spawnBehavior);
		DispenserBlock.registerBehavior(itemBuildingBox, spawnBehavior);

		class BehaviorDispenseAmmo extends DefaultDispenseItemBehavior {
			@Override
			public ItemStack execute(BlockSource source, ItemStack stack) {
				ItemStack weapon;
				switch (((ItemAmmo) stack.getItem()).getTypeInt(stack)) {
				case 1 -> weapon = ItemFromData.getNewStack("shotgun");
				case 2 -> weapon = ItemFromData.getNewStack("minigun");
				case 3 -> weapon = ItemFromData.getNewStack("pistol");
				case 4 -> weapon = ItemFromData.getNewStack("revolver");
				case 5 -> weapon = ItemFromData.getNewStack("smg");
				case 6 -> {
					weapon = ItemFromData.getNewStack("sniperrifle");
					TF2Attribute.setAttribute(weapon, TF2Attribute.attributes[60], 2);
				}
				case 7 -> weapon = ItemFromData.getNewStack("rocketlauncher");
				case 8 -> weapon = ItemFromData.getNewStack("grenadelauncher");
				case 9 -> weapon = ItemFromData.getNewStack("syringegun");
				case 13 -> weapon = ItemFromData.getNewStack("flaregun");
				case 14 -> weapon = ItemFromData.getNewStack("sandmanball");
				default -> {
					return stack;
				}
				}
				if (weapon.isEmpty())
					return stack;
				shootFromDispenser(source, weapon, 0.1);
				((ItemAmmo) stack.getItem()).consumeAmmo(getDummy(source.getLevel()), stack, 1);
				return stack;
			}

			@Override
			protected void playSound(BlockSource source) {
				source.getLevel().levelEvent(1002, source.getPos(), 0);
			}
		}

		class BehaviorDispenseSelf extends DefaultDispenseItemBehavior {
			@Override
			public ItemStack execute(BlockSource source, ItemStack stack) {
				shootFromDispenser(source, stack, 0);
				stack.shrink(1);
				return stack;
			}

			@Override
			protected void playSound(BlockSource source) {
				source.getLevel().levelEvent(1002, source.getPos(), 0);
			}
		}

		for (Item ammo : new Item[] { itemAmmo, itemAmmoMinigun, itemAmmoPistol, itemAmmoSMG, itemAmmoSyringe })
			DispenserBlock.registerBehavior(ammo, new BehaviorDispenseAmmo());
		DispenserBlock.registerBehavior(MapList.weaponClasses.get("jar"), new BehaviorDispenseSelf());
		DispenserBlock.registerBehavior(MapList.weaponClasses.get("cleaver"), new BehaviorDispenseSelf());
	}

	public static EntityDummy getDummy(Level level) {
		if (dummyEnt == null || dummyEnt.level != level)
			dummyEnt = new EntityDummy(level);
		return dummyEnt;
	}

	private static void shootFromDispenser(BlockSource source, ItemStack weapon, double yOffset) {
		Level world = source.getLevel();
		Position pos = DispenserBlock.getDispensePosition(source);
		Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
		EntityDummy dummy = getDummy(world);
		dummy.setPos(pos.x(), pos.y() + yOffset, pos.z());
		Vec2 rot = TF2Util.getAngleFromFacing(facing);
		dummy.yHeadRot = rot.x;
		dummy.setYRot(rot.x);
		dummy.setXRot(rot.y);
		dummy.setItemInHand(InteractionHand.MAIN_HAND, weapon);
		((ItemUsable) weapon.getItem()).use(weapon, dummy, world, InteractionHand.MAIN_HAND, null);
	}

	/* ------------------------------------------------------------------ */
	/* GUIs */
	/* ------------------------------------------------------------------ */

	/** Replacement for 1.12's player.openGui(TF2weapons.instance, id, world, x, y, z). */
	public static void openGui(Player player, int id, Level world, int x, int y, int z) {
		if (world.isClientSide || !(player instanceof ServerPlayer))
			return;
		NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider(
				(windowId, inv, p) -> TF2GuiHandler.createServerMenu(id, windowId, p, world, x, y, z),
				Component.empty()), buf -> {
					buf.writeInt(x);
					buf.writeInt(y);
					buf.writeInt(z);
				});
	}

	/* ------------------------------------------------------------------ */
	/* Server lifecycle */
	/* ------------------------------------------------------------------ */

	private void registerCommands(RegisterCommandsEvent event) {
		TF2Commands.register(event.getDispatcher());
	}

	private void serverStarting(ServerStartingEvent event) {
		server = event.getServer();
		EntityTeleporter.loadTeleporters(server);
	}

	private void serverStopping(ServerStoppingEvent event) {
		EntityTeleporter.saveTeleporters(event.getServer());
		gameArenas.clear();
		server = null;
	}
}
