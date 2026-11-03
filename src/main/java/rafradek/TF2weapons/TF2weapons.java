package rafradek.TF2weapons;

import com.google.common.io.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rafradek.TF2weapons.arena.GameArena;
import rafradek.TF2weapons.block.*;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.gui.inventory.*;
import rafradek.TF2weapons.command.*;
import rafradek.TF2weapons.common.*;
import rafradek.TF2weapons.entity.*;
import rafradek.TF2weapons.entity.boss.EntityHHH;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import rafradek.TF2weapons.entity.boss.EntityMonoculus;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.entity.building.EntityTeleporter.TeleporterData;
import rafradek.TF2weapons.entity.mercenary.*;
import rafradek.TF2weapons.entity.projectile.*;
import rafradek.TF2weapons.inventory.*;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.item.crafting.OpenCrateRecipe;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import rafradek.TF2weapons.loot.*;
import rafradek.TF2weapons.message.*;
import rafradek.TF2weapons.message.udp.TF2UdpClient;
import rafradek.TF2weapons.message.udp.TF2UdpServer;
import rafradek.TF2weapons.potion.PotionTF2;
import rafradek.TF2weapons.potion.PotionTF2Item;
import rafradek.TF2weapons.tileentity.*;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.ReflectionAccess;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;
import rafradek.TF2weapons.world.gen.structure.MannCoBuilding;
import rafradek.TF2weapons.world.gen.structure.ScatteredFeatureTF2Base;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(modid = TF2weapons.MOD_ID, name = "TF2 Stuff", version = "1.6.2a", guiFactory = "rafradek.TF2weapons.client.gui.TF2GuiFactory", acceptedMinecraftVersions = "[1.12, 1.13)", dependencies = "after:dynamiclights;after:thermalexpansion"/*, updateJSON = "https://rafradek.github.io/tf2stuffmod.json"*/)
public class TF2weapons {

	public static final Logger LOGGER = LogManager.getLogger("TF2 Stuff Mod");
	public static final String MOD_ID = "rafradek_tf2_weapons";
	@Metadata(MOD_ID)
	public static ModMetadata metadata;

	@CapabilityInject(WeaponsCapability.class)
	public static final Capability<WeaponsCapability> WEAPONS_CAP = null;

	@CapabilityInject(InventoryWearables.class)
	public static final Capability<InventoryWearables> INVENTORY_CAP = null;

	@CapabilityInject(InventoryAmmoBelt.class)
	public static final Capability<InventoryAmmoBelt> INVENTORY_BELT_CAP = null;

	@CapabilityInject(TF2EventsCommon.TF2WorldStorage.class)
	public static final Capability<TF2EventsCommon.TF2WorldStorage> WORLD_CAP = null;

	@CapabilityInject(WeaponData.WeaponDataCapability.class)
	public static final Capability<WeaponData.WeaponDataCapability> WEAPONS_DATA_CAP = null;

	@CapabilityInject(TF2PlayerCapability.class)
	public static final Capability<TF2PlayerCapability> PLAYER_CAP = null;

	public static TF2UdpServer udpServer;

	public int[] itemid = new int[9];
	public static Configuration conf;

	public static CreativeModeTab tabutilitytf2;
	public static CreativeModeTab tabweapontf2;
	public static CreativeModeTab tabsurvivaltf2;
	public static CreativeModeTab tabspawnertf2;
	public static CreativeModeTab tabrareweapontf2;
	public static CreativeModeTab tabarenatf2;
	// public static final ArmorMaterial OPARMOR =
	// EnumHelper.addArmorMaterial("OPARMOR", "", 1000, new int[] {24,0,0,0},
	// 100);
	public static TF2NetworkWrapper network;
	public static Item itemPlacer;
	public static Item mobHeldItem;

	static int weaponVersion;

	@Instance(value = MOD_ID)
	public static TF2weapons instance;

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

	public static boolean generateCopper;
	public static boolean generateLead;
	public static boolean generateAustralium;

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

	public static ResourceLocation lootTF2Character;
	public static ResourceLocation lootScout;
	public static ResourceLocation lootSpy;
	public static ResourceLocation lootHeavy;
	public static ResourceLocation lootEngineer;
	public static ResourceLocation lootMedic;
	public static ResourceLocation lootPyro;
	public static ResourceLocation lootSoldier;
	public static ResourceLocation lootDemoman;
	public static ResourceLocation lootSniper;
	public static ResourceLocation lootHale;
	public static ResourceLocation lootTF2Base;

	public static byte[] itemDataCompressed;
	public static GZIPOutputStream out;

	public static StatBase cratesOpened;
	public static StatBase robotsKilled;
	public static MinecraftServer server;
	public static EntityDummy dummyEnt;

	public static BannerPattern redPattern;
	public static BannerPattern bluPattern;
	public static BannerPattern neutralPattern;
	public static BannerPattern fastSpawn;
	public static ArrayList<ResourceLocation> animalsDisguise;

	public static Fluid refinedFuel;
	public static boolean squakeLoaded;
	public static boolean corrupted;

	public static Map<String, GameArena> gameArenas;

	@SidedProxy(clientSide = "rafradek.TF2weapons.client.ClientProxy", serverSide = "rafradek.TF2weapons.common.CommonProxy")
	public static CommonProxy proxy;

	public static int getCurrentWeaponVersion() {
		return 50;
	}

	@SuppressWarnings("deprecation")
	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		LOGGER.info("Initializing config files");
		this.weaponDir = new File(event.getModConfigurationDirectory(), "TF2WeaponsLists");
		if (!this.weaponDir.exists())
			this.weaponDir.mkdirs();
		metadata.autogenerated = false;

		conf = new Configuration(event.getSuggestedConfigurationFile());
		TF2ConfigVars.conf = conf;
		boolean shouldCopy = false;
		if (!conf.contains("internal", "Weapon Config Version"))
			shouldCopy = true;
		TF2ConfigVars.createConfig(true);
		File file = event.getSourceFile();
		File squadFile = new File(event.getModConfigurationDirectory(), "TF2RobotSquad.json");
		// System.out.println("LOLOLOLOLOLOL "+file.getAbsolutePath());
		// System.out.println("LOLOLOLOLOLOL2
		// "+event.getModConfigurationDirectory());
		// System.out.println("Istnieje? "+outputFile.exists());

		if (weaponVersion < getCurrentWeaponVersion() || !file.isFile())
			shouldCopy = true;
		if (!new File(this.weaponDir, "Weapons.json").exists() || shouldCopy) {
			conf.get("internal", "Weapon Config Version", getCurrentWeaponVersion()).set(getCurrentWeaponVersion());
			conf.save();
			TF2Util.extractData("weapons/weapons.json", new File(this.weaponDir, "Weapons.json"), file);
			TF2Util.extractData("weapons/cosmetics.json", new File(this.weaponDir, "Cosmetics.json"), file);
			TF2Util.extractData("weapons/crates.json", new File(this.weaponDir, "Crates.json"), file);
			TF2Util.extractData("robots/robot_squad.json", squadFile, file);
		}

		bluPattern = EnumHelper.addEnum(BannerPattern.class, "BLU_PATTERN",
				new Class<?>[] { String.class, String.class }, "blu_base", "bb");
		redPattern = EnumHelper.addEnum(BannerPattern.class, "RED_PATTERN",
				new Class<?>[] { String.class, String.class }, "red_base", "rb");
		neutralPattern = EnumHelper.addEnum(BannerPattern.class, "NEUTRAL_PATTERN",
				new Class<?>[] { String.class, String.class }, "neutral_base", "nb");
		fastSpawn = EnumHelper.addEnum(BannerPattern.class, "FAST_SPAWN", new Class<?>[] { String.class, String.class },
				"fast_spawn", "fs");

		tabweapontf2 = new CreativeModeTab("tf2weapons") {
			@Override
			public ItemStack getTabIconItem() {
				return ItemFromData.getNewStack("minigun");
			}

		};
		tabspawnertf2 = new CreativeModeTab("tf2spawner") {
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(itemBuildingBox, 1, 18);
			}

		};
		tabrareweapontf2 = new CreativeModeTab("tf2rareweapons") {
			@Override
			public ItemStack getTabIconItem() {
				return ItemFromData.getNewStack("rocketshotgun");
			}

		};

		tabutilitytf2 = new CreativeModeTab("tf2util") {
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(itemDisguiseKit);
			}

			@Override
			@OnlyIn(Dist.CLIENT)
			public void displayAllRelevantItems(NonNullList<ItemStack> list) {
				super.displayAllRelevantItems(list);
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 2; j++) {
						if (i == 2 && j == 1)
							continue;
						ItemStack banner = new ItemStack(Items.BANNER, 1,
								(i == 0 ? DyeColor.RED : (i == 1 ? DyeColor.BLUE : DyeColor.GRAY))
								.getDyeDamage());
						ListTag patterns = new ListTag();
						banner.getOrCreateTagElement("BlockEntityTag").put("Patterns", patterns);
						CompoundTag pattern = new CompoundTag();
						pattern.putInt("Color", 15);
						if (i == 0) {
							pattern.putString("Pattern", "rb");
						} else if (i == 1) {
							pattern.putString("Pattern", "bb");
						} else if (i == 2) {
							pattern.putString("Pattern", "nb");
						}

						patterns.add(pattern);
						if (j == 1) {
							CompoundTag patternfast = new CompoundTag();
							patternfast.putString("Pattern", "fs");
							patternfast.putInt("Color", 15);
							patterns.add(patternfast);
						}

						list.add(banner);
					}
				}
			}
		};
		tabsurvivaltf2 = new CreativeModeTab("tf2misc") {
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(Item.byBlock(blockCabinet));
			}
		};
		tabarenatf2 = new CreativeModeTab("tf2arena") {
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(itemPickup, 1, 2);
			}

		};

		LOGGER.info("Initializing attributes");
		MapList.initMaps();
		TF2Attribute.initAttributes();

		/*
		 * File refFile=new File(event.getModConfigurationDirectory(), "TF2References");
		 * if(!refFile.exists()) refFile.mkdirs(); try { BufferedWriter attributes=new
		 * BufferedWriter(new FileWriter(new File(refFile,"attributes.txt")));
		 * attributes.write("ID - Name - Effect - State - Type - Default\n"); for(int
		 * i=0;i<TF2Attribute.attributes.length;i++){ TF2Attribute
		 * attr=TF2Attribute.attributes[i]; if(attr != null){
		 * attributes.write(attr.id+" - "+attr.name+" - "+attr.effect+" - "+attr.state.
		 * toString()+" - "+attr.typeOfValue.toString()+" - "+attr.defaultValue+"\n"); }
		 * } attributes.close(); } catch (IOException e) { e.printStackTrace(); }
		 */
		LOGGER.info("Registering");

		// EntityRegistry.registerModEntity(EntityBullet.class, "bullet", 1,
		// this, 256, 100, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "heavy"), EntityHeavy.class, "heavy", 2, this, 80,
				TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "scout"), EntityScout.class, "scout", 3, this, 80,
				TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "sniper"), EntitySniper.class, "sniper", 4, this,
				80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "soldier"), EntitySoldier.class, "soldier", 5,
				this, 80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "pyro"), EntityPyro.class, "pyro", 6, this, 80,
				TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "demoman"), EntityDemoman.class, "demoman", 7,
				this, 80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "medic"), EntityMedic.class, "medic", 8, this, 80,
				TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "spy"), EntitySpy.class, "spy", 9, this, 80,
				TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "engineer"), EntityEngineer.class, "engineer", 10,
				this, 80, TF2ConfigVars.updateDelay, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "rocket"), EntityRocket.class, "rocket", 11, this,
				64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "flame"), EntityFlame.class, "flame", 12, this, 0,
				20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "grenade"), EntityGrenade.class, "grenade", 13,
				this, 64, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "sticky"), EntityStickybomb.class, "sticky", 14,
				this, 64, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "simple"), EntityProjectileSimple.class, "simple",
				26, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "syringe"), EntityStickProjectile.class,
				"syringe", 15, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "flare"), EntityFlare.class, "flare", 20, this,
				64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "jar"), EntityJar.class, "jar", 21, this, 64, 20,
				false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "ball"), EntityBall.class, "ball", 22, this, 64,
				10, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "sentry"), EntitySentry.class, "sentry", 16, this,
				80, 2, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "dispenser"), EntityDispenser.class, "dispenser",
				17, this, 80, 40, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "teleporter"), EntityTeleporter.class,
				"teleporter", 18, this, 80, 40, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "hale"), EntitySaxtonHale.class, "hale", 19, this,
				80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "monoculus"), EntityMonoculus.class, "monoculus",
				23, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "hhh"), EntityHHH.class, "hhh", 24, this, 80, 3,
				true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "merasmus"), EntityMerasmus.class, "merasmus", 25,
				this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "cleaver"), EntityCleaver.class, "cleaver", 27,
				this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "target"), EntityTarget.class, "target", 28, this,
				80, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "statue"), EntityStatue.class, "statue", 29, this,
				80, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "projenergy"), EntityProjectileEnergy.class,
				"projenergy", 30, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "onyx"), EntityOnyx.class, "onyx", 31, this, 64,
				20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "fireball"), EntityFuryFireball.class, "fireball",
				32, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "light"), EntityLightDynamic.class, "light", 33,
				this, 256, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "pickup"), EntityPickup.class, "pickup", 34, this,
				80, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "grapplinghook"), EntityGrapplingHook.class,
				"grapplinghook", 35, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID, "shortcircuitorb"),
				EntityProjectileShortCircuitOrb.class, "shortcircuitorb", 36, this, 64, 20, false);
		// GameRegistry.registerItem(new ItemArmor(TF2weapons.OPARMOR, 3,
		// 0).setUnlocalizedName(TF2weapons.MOD_ID +
		// ".oparmor").setTextureName("diamond_helmet").setCreativeTab(tabtf2),"oparmor");
		ForgeRegistries.ITEMS.register(itemPlacer = new ItemMonsterPlacerPlus().setUnlocalizedName("monsterPlacer")
				.setRegistryName(TF2weapons.MOD_ID + ":placer"));
		ForgeRegistries.ITEMS
		.register(itemDisguiseKit = new ItemDisguiseKit().setUnlocalizedName(TF2weapons.MOD_ID + ".disguiseKit")
		.setRegistryName(TF2weapons.MOD_ID + ":disguise_kit"));
		ForgeRegistries.ITEMS
		.register(itemBuildingBox = new ItemBuildingBox().setUnlocalizedName(TF2weapons.MOD_ID + ".buildingBox")
		.setRegistryName(TF2weapons.MOD_ID + ":building_box"));
		ForgeRegistries.ITEMS.register(itemSandvich = new ItemFoodThrowable(14, 2F, false, 600)
				.setPotionEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 2), 1f)
				.setUnlocalizedName(TF2weapons.MOD_ID + ".sandvich").setCreativeTab(tabutilitytf2)
				.setRegistryName(TF2weapons.MOD_ID + ":sandvich"));
		ForgeRegistries.ITEMS.register(itemChocolate = new ItemFoodThrowable(7, 1.5F, false, 200)
				.setPotionEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0), 1f)
				.setUnlocalizedName(TF2weapons.MOD_ID + ".chocolate").setCreativeTab(tabutilitytf2)
				.setRegistryName(TF2weapons.MOD_ID + ":chocolate"));
		ForgeRegistries.ITEMS.register(itemHorn = new ItemHorn().setUnlocalizedName(TF2weapons.MOD_ID + ".horn")
				.setRegistryName(TF2weapons.MOD_ID + ":horn"));
		ForgeRegistries.ITEMS.register(itemAmmo = new ItemAmmo().setUnlocalizedName(TF2weapons.MOD_ID + ".tf2ammo")
				.setRegistryName(TF2weapons.MOD_ID + ":ammo"));
		ForgeRegistries.ITEMS
		.register(itemAmmoPackage = new ItemAmmoPackage().setUnlocalizedName(TF2weapons.MOD_ID + ".tf2ammobox")
		.setRegistryName(TF2weapons.MOD_ID + ":ammo_box"));
		ForgeRegistries.ITEMS.register(itemAmmoFire = new ItemFireAmmo(10, 350, 1)
				.setUnlocalizedName(TF2weapons.MOD_ID + ".tf2ammo").setRegistryName(TF2weapons.MOD_ID + ":ammo_fire"));
		ForgeRegistries.ITEMS.register(
				itemAmmoMedigun = new ItemFireAmmo(12, 11200, 1).setUnlocalizedName(TF2weapons.MOD_ID + ".tf2ammo")
				.setRegistryName(TF2weapons.MOD_ID + ":ammo_medigun"));
		ForgeRegistries.ITEMS.register(
				itemAmmoPistol = new ItemFireAmmo(3, 12, 12).setUnlocalizedName(TF2weapons.MOD_ID + ".tf2ammo")
				.setRegistryName(TF2weapons.MOD_ID + ":ammo_pistol"));
		ForgeRegistries.ITEMS.register(
				itemAmmoMinigun = new ItemFireAmmo(2, 100, 2).setUnlocalizedName(TF2weapons.MOD_ID + ".tf2ammo")
				.setRegistryName(TF2weapons.MOD_ID + ":ammo_minigun"));
		ForgeRegistries.ITEMS.register(itemAmmoSMG = new ItemFireAmmo(5, 25, 8)
				.setUnlocalizedName(TF2weapons.MOD_ID + ".tf2ammo").setRegistryName(TF2weapons.MOD_ID + ":ammo_smg"));
		ForgeRegistries.ITEMS.register(
				itemAmmoSyringe = new ItemFireAmmo(9, 40, 6).setUnlocalizedName(TF2weapons.MOD_ID + ".tf2ammo")
				.setRegistryName(TF2weapons.MOD_ID + ":ammo_syringe"));
		ForgeRegistries.ITEMS
		.register(itemAmmoBelt = new ItemAmmoBelt().setUnlocalizedName(TF2weapons.MOD_ID + ".ammoBelt")
		.setRegistryName(TF2weapons.MOD_ID + ":ammo_belt").setCreativeTab(tabsurvivaltf2));
		ForgeRegistries.ITEMS
		.register(itemScoutBoots = new ItemArmorTF2(ArmorMaterial.LEATHER, 0, EquipmentSlot.FEET,
				"Allows double jumping", 0f).setUnlocalizedName(TF2weapons.MOD_ID + ".scoutBoots")
		.setRegistryName(TF2weapons.MOD_ID + ":scout_shoes").setCreativeTab(tabutilitytf2));
		ForgeRegistries.ITEMS.register(itemMantreads = new ItemArmorTF2(ArmorMaterial.IRON, 0, EquipmentSlot.FEET,
				"Deals 1.8x falling damage to the player you land on", 0.75f)
				.setUnlocalizedName(TF2weapons.MOD_ID + ".mantreads")
				.setRegistryName(TF2weapons.MOD_ID + ":mantreads").setCreativeTab(tabutilitytf2));
		ForgeRegistries.ITEMS.register(itemGunboats = new ItemArmorTF2(ArmorMaterial.IRON, 0, EquipmentSlot.FEET,
				"Reduces blast jumping damage by 60%", 0f).setUnlocalizedName(TF2weapons.MOD_ID + ".gunboats")
				.setRegistryName(TF2weapons.MOD_ID + ":gunboats").setCreativeTab(tabutilitytf2));
		ForgeRegistries.ITEMS.register(itemStatue = new ItemStatue().setRegistryName(TF2weapons.MOD_ID + ":statue"));
		ForgeRegistries.ITEMS.register(itemToken = new ItemToken().setRegistryName(TF2weapons.MOD_ID, "token"));
		// GameRegistry.register(itemCopperIngot=new
		// Item().setUnlocalizedName(TF2weapons.MOD_ID +
		// ".ingotCopper").setCreativeTab(tabtf2).setRegistryName(TF2weapons.MOD_ID+":ingotCopper"));
		// GameRegistry.register(itemLeadIngot=new
		// Item().setUnlocalizedName(TF2weapons.MOD_ID +
		// ".ingotLead").setCreativeTab(tabtf2).setRegistryName(TF2weapons.MOD_ID+":ingotLead"));
		// GameRegistry.register(itemAustraliumIngot=new
		// Item().setUnlocalizedName(TF2weapons.MOD_ID +
		// ".ingotAustralium").setCreativeTab(tabtf2).setRegistryName(TF2weapons.MOD_ID+":ingotAustralium"));
		ForgeRegistries.ITEMS.register(itemTF2 = new ItemTF2().setRegistryName(TF2weapons.MOD_ID + ":itemTF2"));
		ForgeRegistries.ITEMS
		.register(itemRobotPart = new ItemRobotPart().setRegistryName(TF2weapons.MOD_ID + ":robotPart"));
		// ForgeRegistries.ITEMS.register(itemPDA = new
		// ItemPDA().setRegistryName(TF2weapons.MOD_ID +
		// ":pda").setUnlocalizedName(TF2weapons.MOD_ID +
		// ".pda").setCreativeTab(tabutilitytf2));
		ForgeRegistries.ITEMS.register(itemTarget = new ItemTarget().setRegistryName(TF2weapons.MOD_ID + ":target")
				.setUnlocalizedName(TF2weapons.MOD_ID + ".attackTarget"));
		ForgeRegistries.ITEMS.register(
				itemKillstreak = new ItemKillstreakKit().setRegistryName(TF2weapons.MOD_ID + ":killstreakkit"));
		ForgeRegistries.ITEMS.register(itemStrangifier = new ItemStrangifier()
				.setRegistryName(TF2weapons.MOD_ID + ":strangifier")
				.setUnlocalizedName(TF2weapons.MOD_ID + ".strangifier").setCreativeTab(TF2weapons.tabutilitytf2));
		ForgeRegistries.ITEMS.register(itemKillstreakFabricator = new ItemKillstreakFabricator()
				.setRegistryName(TF2weapons.MOD_ID + ":killstreakfabricator"));
		ForgeRegistries.ITEMS.register(itemEventMaker = new ItemEventMaker().setCreativeTab(tabutilitytf2)
				.setUnlocalizedName(TF2weapons.MOD_ID + ".eventmaker")
				.setRegistryName(TF2weapons.MOD_ID + ":eventmaker"));
		ForgeRegistries.ITEMS.register(itemBossSpawn = new ItemBossSpawner().setCreativeTab(tabspawnertf2)
				.setUnlocalizedName(TF2weapons.MOD_ID + ".boss").setRegistryName(TF2weapons.MOD_ID + ":boss_spawner"));
		ForgeRegistries.ITEMS.register(itemDoorController = new ItemDoorController().setCreativeTab(tabutilitytf2)
				.setUnlocalizedName(TF2weapons.MOD_ID + ".doorcontroller")
				.setRegistryName(TF2weapons.MOD_ID + ":door_controller"));
		ForgeRegistries.ITEMS.register(itemMoney = new ItemMoney().setRegistryName(TF2weapons.MOD_ID + ":money"));
		ForgeRegistries.ITEMS.register(itemConfigurator = new ItemConfigure()
				.setRegistryName(TF2weapons.MOD_ID + ":configurator").setCreativeTab(tabarenatf2));
		ForgeRegistries.ITEMS.register(itemPickup = new ItemPickup().setRegistryName(TF2weapons.MOD_ID + ":pickup")
				.setCreativeTab(tabarenatf2));

		Iterator<String> iterator = MapList.weaponClasses.keySet().iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			ForgeRegistries.ITEMS.register(MapList.weaponClasses.get(name)
					.setRegistryName(new ResourceLocation(MOD_ID, "" + name.toLowerCase())));
		}

		GameRegistry.registerTileEntity(TileEntityUpgrades.class, "UpgradeStation");
		GameRegistry.registerTileEntity(TileEntityAmmoFurnace.class, "AmmoFurnace");
		GameRegistry.registerTileEntity(TileEntityOverheadDoor.class,
				new ResourceLocation(TF2weapons.MOD_ID, "overhead_door"));
		GameRegistry.registerTileEntity(TileEntityRobotDeploy.class,
				new ResourceLocation(TF2weapons.MOD_ID, "robot_deploy"));
		GameRegistry.registerTileEntity(TileEntityResupplyCabinet.class,
				new ResourceLocation(TF2weapons.MOD_ID, "resupply_cabinet"));
		GameRegistry.registerTileEntity(TileEntityCapturePoint.class,
				new ResourceLocation(TF2weapons.MOD_ID, "capture_point"));
		GameRegistry.registerTileEntity(TileEntityGameConfigure.class,
				new ResourceLocation(TF2weapons.MOD_ID, "game_configure"));

		registerBlock(blockCabinet = new BlockCabinet().setHardness(5.0F).setResistance(10.0F)
				.setUnlocalizedName(TF2weapons.MOD_ID + ".cabinet"), TF2weapons.MOD_ID + ":tf2workbench");
		registerBlock(blockAmmoFurnace = new BlockAmmoFurnace(false).setHardness(5.0F).setResistance(10.0F)
				.setUnlocalizedName(TF2weapons.MOD_ID + ".ammoFurnace"), TF2weapons.MOD_ID + ":ammo_furnace");
		registerBlock(
				blockUpgradeStation = new BlockUpgradeStation().setHardness(5f).setResistance(10.0F)
				.setUnlocalizedName(TF2weapons.MOD_ID + ".upgradeStation"),
				TF2weapons.MOD_ID + ":upgrade_station");
		registerBlock(blockCopperOre = new DropExperienceBlock().setCreativeTab(tabsurvivaltf2).setHardness(3.0F)
				.setResistance(5.0F).setUnlocalizedName(TF2weapons.MOD_ID + ".oreCopper"),
				TF2weapons.MOD_ID + ":copper_ore");
		registerBlock(blockLeadOre = new DropExperienceBlock().setCreativeTab(tabsurvivaltf2).setHardness(3.0F).setResistance(5.0F)
				.setUnlocalizedName(TF2weapons.MOD_ID + ".oreLead"), TF2weapons.MOD_ID + ":lead_ore");
		registerBlock(
				blockAustraliumOre = new DropExperienceBlock().setCreativeTab(tabsurvivaltf2).setHardness(7.0F)
				.setResistance(10.0F).setUnlocalizedName(TF2weapons.MOD_ID + ".oreAustralium"),
				TF2weapons.MOD_ID + ":australium_ore");
		registerBlock(blockAustralium = new Block(Material.IRON, MaterialColor.GOLD).setCreativeTab(tabsurvivaltf2)
				.setHardness(9.0F).setResistance(20.0F).setUnlocalizedName(TF2weapons.MOD_ID + ".blockAustralium"),
				TF2weapons.MOD_ID + ":australium_block");
		registerBlock(
				blockOverheadDoor = new BlockOverheadDoor().setCreativeTab(tabsurvivaltf2).setHardness(3.0F)
				.setResistance(5.0F).setUnlocalizedName(TF2weapons.MOD_ID + ".blockOverhead"),
				TF2weapons.MOD_ID + ":overhead_door");
		registerBlock(
				blockRobotDeploy = new BlockRobotDeploy().setCreativeTab(tabsurvivaltf2).setHardness(3.0F)
				.setResistance(5.0F).setUnlocalizedName(TF2weapons.MOD_ID + ".blockRobotDeploy"),
				TF2weapons.MOD_ID + ":robot_deploy");
		registerBlock(
				blockResupplyCabinet = new BlockResupplyCabinet().setCreativeTab(tabarenatf2).setBlockUnbreakable()
				.setResistance(6000000F).setUnlocalizedName(TF2weapons.MOD_ID + ".blockResupplyCabinet"),
				TF2weapons.MOD_ID + ":resupply_cabinet");
		registerBlock(
				blockCapturePoint = new BlockCapturePoint().setCreativeTab(tabarenatf2).setBlockUnbreakable()
				.setResistance(6000000F).setUnlocalizedName(TF2weapons.MOD_ID + ".blockCapturePoint"),
				TF2weapons.MOD_ID + ":capture_point");
		registerBlock(
				blockConfigure = new BlockGameConfigure().setCreativeTab(tabarenatf2).setBlockUnbreakable()
				.setResistance(6000000F).setUnlocalizedName(TF2weapons.MOD_ID + ".blockGameConfigure"),
				TF2weapons.MOD_ID + ":game_configure");
		ForgeRegistries.BLOCKS.register(blockProp = new BlockProp(Material.WOOD, MaterialColor.GOLD).setHardness(0.75F)
				.setResistance(2.0F).setUnlocalizedName(TF2weapons.MOD_ID + ".blockProp")
				.setRegistryName(TF2weapons.MOD_ID + ":prop_block"));

		OreDictionary.registerOre("oreCopper", blockCopperOre);
		OreDictionary.registerOre("oreLead", blockLeadOre);
		OreDictionary.registerOre("oreAustralium", blockAustraliumOre);
		OreDictionary.registerOre("blockAustralium", blockAustralium);
		OreDictionary.registerOre("ingotCopper", new ItemStack(itemTF2, 1, 0));
		OreDictionary.registerOre("ingotLead", new ItemStack(itemTF2, 1, 1));
		OreDictionary.registerOre("ingotAustralium", new ItemStack(itemTF2, 1, 2));
		OreDictionary.registerOre("nuggetAustralium", new ItemStack(itemTF2, 1, 9));

		blockCopperOre.setHarvestLevel("pickaxe", 1);
		blockLeadOre.setHarvestLevel("pickaxe", 1);
		blockAustraliumOre.setHarvestLevel("pickaxe", 2);

		ItemAmmo.STACK_FILL = new ItemStack(itemAmmo);

		CapabilityManager.INSTANCE.register(TF2PlayerCapability.class, new NullStorage<TF2PlayerCapability>(),
				() -> null);
		CapabilityManager.INSTANCE.register(WeaponsCapability.class, new NullStorage<WeaponsCapability>(), () -> null);
		CapabilityManager.INSTANCE.register(InventoryWearables.class, new NullStorage<InventoryWearables>(),
				() -> null);
		CapabilityManager.INSTANCE.register(WeaponData.WeaponDataCapability.class,
				new NullStorage<WeaponData.WeaponDataCapability>(), () -> null);
		CapabilityManager.INSTANCE.register(TF2EventsCommon.TF2WorldStorage.class,
				new NullStorage<TF2EventsCommon.TF2WorldStorage>(), () -> null);
		CapabilityManager.INSTANCE.register(ItemCrate.CrateContent.class, new NullStorage<ItemCrate.CrateContent>(),
				() -> null);
		CapabilityManager.INSTANCE.register(ItemFromData.AttributeProvider.class,
				new NullStorage<ItemFromData.AttributeProvider>(), () -> null);

		ForgeRegistries.POTIONS.register(bonk = new PotionTF2Item(false, 0x696969,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/items/bonk.png")).setPotionName("effect.bonk")
				.setRegistryName(TF2weapons.MOD_ID + ":bonkEff"));
		ForgeRegistries.POTIONS.register(stun = new PotionTF2(true, 0, 3, 1).setPotionName("effect.stun")
				.setRegistryName(TF2weapons.MOD_ID + ":stunEff").registerPotionAttributeModifier(
						Attributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B354F0D8BA", -0.5D, 2));
		ForgeRegistries.POTIONS.register(crit = new PotionTF2Item(false, 0,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/items/critacola.png")).setPotionName("effect.crit")
				.setRegistryName(TF2weapons.MOD_ID + ":critEff")
				.registerPotionAttributeModifier(Attributes.MOVEMENT_SPEED,
						"7107DE5E-7CE8-4030-940E-14B354E56B59", 0.35D, 1));
		ForgeRegistries.POTIONS.register(buffbanner = new PotionTF2Item(false, 0,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/items/buffbanner.png")).setPotionName("effect.banner")
				.setRegistryName(TF2weapons.MOD_ID + ":bannerEff").setBeneficial());
		ForgeRegistries.POTIONS.register(backup = new PotionTF2Item(false, 0,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/items/backup.png")).setPotionName("effect.backup")
				.setRegistryName(TF2weapons.MOD_ID + ":backupEff").setBeneficial());
		ForgeRegistries.POTIONS.register(
				conch = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/conch.png"))
				.setPotionName("effect.conch").setRegistryName(TF2weapons.MOD_ID + ":conchEff").setBeneficial()
				.registerPotionAttributeModifier(Attributes.MOVEMENT_SPEED,
						"7107DE5E-7CE8-4030-940E-14B35B5565E2", 0.35D, 1));
		ForgeRegistries.POTIONS.register(markDeath = new PotionTF2(true, 0, 1, 2).setPotionName("effect.markDeath")
				.setRegistryName(TF2weapons.MOD_ID + ":markDeathEff"));
		ForgeRegistries.POTIONS.register(critBoost = new PotionTF2(false, 0, 4, 0).setPotionName("effect.critBoost")
				.setRegistryName(TF2weapons.MOD_ID + ":critBoostEff").setBeneficial());
		ForgeRegistries.POTIONS.register(quickFix = new PotionTF2Item(false, 0,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/items/medigun_im.png"))
				.setPotionName("effect.quickfix").setRegistryName(TF2weapons.MOD_ID + ":quickFixEff")
				.setBeneficial().registerPotionAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,
						"0d09b7c3-ac5e-4f56-9b65-fe2d09b99dc1", 10, 0));
		ForgeRegistries.POTIONS.register(jarate = new PotionTF2Item(true, 0xFFD500,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/items/jarate.png")).setPotionName("effect.jarate")
				.setRegistryName(TF2weapons.MOD_ID + ":jarateEff"));
		ForgeRegistries.POTIONS.register(madmilk = new PotionTF2Item(true, 0xF1F1F1,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/items/madmilk.png")).setPotionName("effect.madmilk")
				.setRegistryName(TF2weapons.MOD_ID + ":madmilkEff"));
		ForgeRegistries.POTIONS.register(charging = new PotionTF2Item(false, 0,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/items/charging_targe.png")) {

			@Override
			public void applyAttributesModifiersToEntity(LivingEntity entityLivingBaseIn,
					AttributeMap attributeMapIn, int amplifier) {
				super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
				entityLivingBaseIn.stepHeight += amplifier;
				if (entityLivingBaseIn instanceof EntityTF2Character) {
					((EntityTF2Character) entityLivingBaseIn).rotation *= 0.1f;
				}
			}

			@Override
			public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn,
					AttributeMap attributeMapIn, int amplifier) {
				super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
				entityLivingBaseIn.stepHeight -= amplifier;
				if (entityLivingBaseIn.getCapability(TF2weapons.WEAPONS_CAP).isPresent()) {
					WeaponsCapability.get(entityLivingBaseIn).setExpJump(true);
				}
				if (entityLivingBaseIn instanceof EntityTF2Character) {
					((EntityTF2Character) entityLivingBaseIn).rotation *= 10f;
				}
			}

		}.setPotionName("effect.charging").setRegistryName(TF2weapons.MOD_ID + ":chargingEff")
				.registerPotionAttributeModifier(Attributes.MOVEMENT_SPEED,
						"7107DE5E-7CE8-4030-940E-14B35B5565E6", 1.5D, 2)
				.setBeneficial());
		ForgeRegistries.POTIONS.register(uber = new PotionTF2Item(false, 0,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/items/medigun_im.png")).setPotionName("effect.uber")
				.setRegistryName(TF2weapons.MOD_ID + ":uberEff").setBeneficial());
		ForgeRegistries.POTIONS.register(it = new PotionTF2(true, 0xFFFFFFFF, 1, 2).setPotionName("effect.it")
				.setRegistryName(TF2weapons.MOD_ID + ":itEff"));
		ForgeRegistries.POTIONS.register(bombmrs = new PotionTF2(false, 0xFFFFFFFF, 1, 2)
				.setPotionName("effect.bombmrs").setRegistryName(TF2weapons.MOD_ID + ":bombEff")
				.registerPotionAttributeModifier(Attributes.MOVEMENT_SPEED,
						"7107DE6F-7CE8-4030-940E-14B354F0D8BA", 1.25D, 2));
		ForgeRegistries.POTIONS.register(bleeding = new PotionTF2(true, 0, 4, 0).setPotionName("effect.bleeding")
				.setRegistryName(TF2weapons.MOD_ID + ":bleedingEff"));
		ForgeRegistries.POTIONS.register(noKnockback = new PotionTF2(true, 0, 0, 0).setPotionName("effect.noKnockback")
				.setRegistryName(TF2weapons.MOD_ID + ":noKnockbackEff").registerPotionAttributeModifier(
						Attributes.KNOCKBACK_RESISTANCE, "0d09b7c3-ac5e-4f56-9b65-fe2d09b99dc3", 10, 0));
		ForgeRegistries.POTIONS.register(sapped = new PotionTF2(true, 0, 0, 0) {
			@Override
			public void applyAttributesModifiersToEntity(LivingEntity entityLivingBaseIn,
					AttributeMap attributeMapIn, int amplifier) {
				super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
				if (entityLivingBaseIn instanceof EntityTF2Character) {
					if (((EntityTF2Character) entityLivingBaseIn).isGiant())
						((EntityTF2Character) entityLivingBaseIn).rotation *= 0.4f;
					else
						((EntityTF2Character) entityLivingBaseIn).rotation *= 0.1f;
				}
			}

			@Override
			public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn,
					AttributeMap attributeMapIn, int amplifier) {
				super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
				if (entityLivingBaseIn instanceof EntityTF2Character) {
					if (((EntityTF2Character) entityLivingBaseIn).isGiant())
						((EntityTF2Character) entityLivingBaseIn).rotation *= 2.5f;
					else
						((EntityTF2Character) entityLivingBaseIn).rotation *= 10f;
				}
			}
		}.setPotionName("effect.sapped").setRegistryName(TF2weapons.MOD_ID + ":sapped").registerPotionAttributeModifier(
				Attributes.MOVEMENT_SPEED, "7107DE6F-7CE8-4030-940E-14B354F0D4AB", -0.5D, 2));

		ForgeRegistries.POTIONS.register(regen = new PotionTF2(false, 0, 0, 0) {

			@Override
			public boolean isReady(int duration, int amplifier) {
				return duration % 5 == 0;
			}

			@Override
			public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
				entityLivingBaseIn.heal((amplifier + 1) * 0.5f
						* Math.min(1, TF2Util.getReducedHealing(entityLivingBaseIn, entityLivingBaseIn, 1.5f)));
			}

		}.setPotionName("effect.regenHit").setRegistryName(TF2weapons.MOD_ID + ":regenHit"));

		ForgeRegistries.POTIONS.register(viralfire = new PotionTF2(true, 0, 0, 0) {

			@Override
			public boolean isReady(int duration, int amplifier) {
				return amplifier != 0 && duration % 2 == 0;
			}

			@Override
			public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
				float range = 0.75f + 0.75f * amplifier;
				for (LivingEntity target : entityLivingBaseIn.level.getEntitiesOfClass(LivingEntity.class,
						entityLivingBaseIn.getBoundingBox().inflate(range, range, range))) {
					if (!(TF2Util.getOwnerIfOwnable(target) instanceof Player
							&& TF2Util.isEnemy(entityLivingBaseIn, target))
							&& target.getEffect(viralfire) == null) {
						try {
							int firetime = ReflectionAccess.entityFire.getInt(entityLivingBaseIn);
							target.setSecondsOnFire(firetime / 20 + 1);
							target.hurt(DamageSource.ON_FIRE, amplifier * 2);
							target.addEffect(new MobEffectInstance(viralfire, firetime, amplifier));
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}
			}

			@Override
			public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn,
					AttributeMap attributeMapIn, int amplifier) {
				super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
				if (amplifier > 0 && entityLivingBaseIn.getEffect(viralfire) == null) {
					entityLivingBaseIn.addEffect(new MobEffectInstance(viralfire, 40, 0));
				}
			}

		}.setPotionName("effect.viralFire").setRegistryName(TF2weapons.MOD_ID + ":viralFire"));
		ForgeRegistries.POTIONS.register(shieldBullet = new PotionTF2Item(false, 0,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/misc/vac_bullet_red.png"))
				.setPotionName("effect.shieldBullet").setRegistryName(TF2weapons.MOD_ID + ":shieldBullet"));
		ForgeRegistries.POTIONS.register(shieldExplosive = new PotionTF2Item(false, 0,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/misc/vac_explosive_red.png"))
				.setPotionName("effect.shieldExplosive")
				.setRegistryName(TF2weapons.MOD_ID + ":shieldExplosive"));
		ForgeRegistries.POTIONS.register(shieldFire = new PotionTF2Item(false, 0,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/misc/vac_fire_red.png"))
				.setPotionName("effect.shieldFire").setRegistryName(TF2weapons.MOD_ID + ":shieldFire"));
		ForgeRegistries.POTIONS.register(gas = new PotionTF2Item(true, 0x2B3E00,
				new ResourceLocation(TF2weapons.MOD_ID, "textures/misc/vac_fire_red.png")).setPotionName("effect.gas")
				.setRegistryName(TF2weapons.MOD_ID + ":gaspasserEff"));
		// conf.save();
		ItemKillstreakFabricator.initKillstreaks();
		PropertyType.init();
		if (!TF2ConfigVars.disableGeneration) {
			MapGenStructureIO.registerStructureComponent(MannCoBuilding.class, "ViMC");
			MapGenStructureIO.registerStructure(MannCoBuilding.Start.class, "MCBuild");
			MapGenStructureIO.registerStructureComponent(ScatteredFeatureTF2Base.class, "TF2B");
			MapGenStructureIO.registerStructure(ScatteredFeatureTF2Base.Start.class, "TF2Base");
			// GameRegistry.registerWorldGenerator(generator, modGenerationWeight);
		}

		if (TF2ConfigVars.thermalExpansion) {
			ThermalExpansionModule.init();
		}

		LOGGER.info("Parsing weapon files");
		loadWeapons();
		InvasionEvent.squads = Squad.parseFile(squadFile);
		// System.out.println(MapList.nameToData.get("rocketlauncher"));
		cratesOpened = (new StatBasic("stat.cratesOpened",
				Component.translatable("stat.cratesOpened", new Object[0]))).registerStat();
		robotsKilled = (new StatBasic("stat.robotsKilled",
				Component.translatable("stat.cratesKilled", new Object[0]))).registerStat();
		LOGGER.info("Registering models");
		proxy.preInit();
		MinecraftForge.EVENT_BUS.register(new TF2EventsCommon());
		MinecraftForge.ORE_GEN_BUS.register(new TF2EventsCommon());
		LOGGER.info("Pre init done");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		/*
		 * if (event.getSide() == Side.CLIENT) {
		 * AchievementPage.registerAchievementPage(new TF2Achievements()); }
		 */

		TF2Achievements.init();
		GameRegistry.addSmelting(new ItemStack(blockCopperOre), new ItemStack(itemTF2, 1, 0), 0.5f);
		GameRegistry.addSmelting(new ItemStack(blockLeadOre), new ItemStack(itemTF2, 1, 1), 0.55f);
		GameRegistry.addSmelting(new ItemStack(blockAustraliumOre), new ItemStack(itemTF2, 1, 2), 2f);

		/*
		 * GameRegistry.(new IFuelHandler() {
		 *
		 * @Override public int getBurnTime(ItemStack fuel) { return fuel.getItem()
		 * instanceof ItemCrate ? 300 : 0; }
		 *
		 * });
		 */
		ForgeRegistries.RECIPES.register(
				new ShapedOreRecipe(null, new ItemStack(blockCabinet), "SCS", "SIS", 'S', new ItemStack(itemTF2, 1, 3),
						'C', "workbench", 'I', "blockIron").setRegistryName(TF2weapons.MOD_ID, "recipe1"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(itemTF2, 1, 2), "AAA", "AAA", "AAA",
				'A', new ItemStack(itemTF2, 1, 6)).setRegistryName(TF2weapons.MOD_ID, "recipe2"));
		ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(null, new ItemStack(itemTF2, 9, 6), "ingotAustralium")
				.setRegistryName(TF2weapons.MOD_ID, "recipe3"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(blockAustralium), "AAA", "AAA", "AAA",
				'A', new ItemStack(itemTF2, 1, 2)).setRegistryName(TF2weapons.MOD_ID, "recipe4"));
		ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(null, new ItemStack(itemTF2, 9, 2), "blockAustralium")
				.setRegistryName(TF2weapons.MOD_ID, "recipe5"));
		ForgeRegistries.RECIPES.register(new OpenCrateRecipe().setRegistryName(TF2weapons.MOD_ID, "opencrate"));

		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(TF2weapons.blockOverheadDoor),
				new Object[] { "BI", "B ", "B ", 'B', Blocks.IRON_BARS, 'I', "ingotIron" })
				.setRegistryName(TF2weapons.MOD_ID, "blockoverhead"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemDoorController, 1, 0),
				new Object[] { "RIR", "IOI", "RIR", 'R', "dustRedstone", 'I', "ingotIron", 'O', Blocks.OBSERVER })
				.setRegistryName(TF2weapons.MOD_ID, "doorcontroller1"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemDoorController, 1, 1),
				new Object[] { "SIS", "IOI", "SIS", 'S', "gunpowder", 'I', "ingotIron", 'O', Blocks.OBSERVER })
				.setRegistryName(TF2weapons.MOD_ID, "doorcontroller2"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemDoorController, 1, 2),
				new Object[] { "RIR", "IOI", "RIR", 'R', "dyeRed", 'I', "ingotIron", 'O', Blocks.OBSERVER })
				.setRegistryName(TF2weapons.MOD_ID, "doorcontroller3"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemDoorController, 1, 3),
				new Object[] { "RIR", "IOI", "RIR", 'R', "dyeBlue", 'I', "ingotIron", 'O', Blocks.OBSERVER })
				.setRegistryName(TF2weapons.MOD_ID, "doorcontroller4"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemMoney, 1, 1),
				new Object[] { "AAA", "AAA", "AAA", 'A', new ItemStack(TF2weapons.itemMoney, 1, 0) })
				.setRegistryName(TF2weapons.MOD_ID, "money1"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemMoney, 1, 2),
				new Object[] { "AAA", "AAA", "AAA", 'A', new ItemStack(TF2weapons.itemMoney, 1, 1) })
				.setRegistryName(TF2weapons.MOD_ID, "money2"));
		ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(null, new ItemStack(TF2weapons.itemMoney, 9, 0),
				new ItemStack(TF2weapons.itemMoney, 1, 1)).setRegistryName(TF2weapons.MOD_ID, "money3"));
		ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(null, new ItemStack(TF2weapons.itemMoney, 9, 1),
				new ItemStack(TF2weapons.itemMoney, 1, 2)).setRegistryName(TF2weapons.MOD_ID, "money4"));

		LootFunctionManager.registerFunction(new EntityBuildingFunction.Serializer());
		LootFunctionManager.registerFunction(new EntityOfClassFunction.Serializer());
		LootFunctionManager.registerFunction(new RandomWeaponFunction.Serializer());
		LootConditionManager.registerCondition(new KilledByTeam.Serializer());
		LootConditionManager.registerCondition(new EntityNBTCondition.Serializer());
		LootConditionManager.registerCondition(new EntityOrCondition.Serializer());

		BuiltInLootTables.register(new ResourceLocation(MOD_ID, "chests/simple_dungeon"));
		BuiltInLootTables.register(new ResourceLocation(MOD_ID, "chests/nether_bridge"));
		BuiltInLootTables.register(new ResourceLocation(MOD_ID, "chests/stronghold_corridor"));
		BuiltInLootTables.register(new ResourceLocation(MOD_ID, "chests/end_city_treasure"));
		BuiltInLootTables.register(new ResourceLocation(MOD_ID, "chests/abandoned_mineshaft"));

		lootTF2Character = BuiltInLootTables.register(new ResourceLocation(MOD_ID, "entities/tf2character"));
		lootScout = BuiltInLootTables.register(new ResourceLocation(MOD_ID, "entities/scout"));
		lootHeavy = BuiltInLootTables.register(new ResourceLocation(MOD_ID, "entities/heavy"));
		lootSniper = BuiltInLootTables.register(new ResourceLocation(MOD_ID, "entities/sniper"));
		lootSpy = BuiltInLootTables.register(new ResourceLocation(MOD_ID, "entities/spy"));
		lootDemoman = BuiltInLootTables.register(new ResourceLocation(MOD_ID, "entities/demoman"));
		lootEngineer = BuiltInLootTables.register(new ResourceLocation(MOD_ID, "entities/engineer"));
		lootSoldier = BuiltInLootTables.register(new ResourceLocation(MOD_ID, "entities/soldier"));
		lootMedic = BuiltInLootTables.register(new ResourceLocation(MOD_ID, "entities/medic"));
		lootPyro = BuiltInLootTables.register(new ResourceLocation(MOD_ID, "entities/pyro"));
		lootHale = BuiltInLootTables.register(new ResourceLocation(MOD_ID, "entities/hale"));
		lootTF2Base = BuiltInLootTables.register(new ResourceLocation(MOD_ID, "chests/tf2_base"));

		// new
		// Item(2498).setUnlocalizedName(TF2weapons.MOD_ID +
		// ".FakeItem").setTextureName(TF2weapons.MOD_ID+":saw").setCreativeTab(CreativeTabs.tabBlock);
		DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemPlacer, new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack dispenseStack(BlockSource p_82487_1_, ItemStack p_82487_2_) {
				Direction enumfacing = p_82487_1_.getBlockState().getValue(DispenserBlock.FACING);
				double d0 = p_82487_1_.getX() + enumfacing.getStepX();
				double d1 = p_82487_1_.getY() + 0.2F;
				double d2 = p_82487_1_.getZ() + enumfacing.getStepZ();
				Entity entity = ItemMonsterPlacerPlus.spawnCreature(null, p_82487_1_.getWorld(),
						p_82487_2_.getDamageValue(), d0, d1, d2,
						p_82487_2_.getTag() != null && p_82487_2_.getTag().contains("SavedEntity")
						? p_82487_2_.getTag().getCompound("SavedEntity")
								: null);

				if (entity instanceof LivingEntity && p_82487_2_.hasCustomHoverName())
					((Mob) entity).setCustomNameTag(p_82487_2_.getHoverName());

				p_82487_2_.splitStack(1);
				return p_82487_2_;
			}
		});
		DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemBuildingBox, new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack dispenseStack(BlockSource p_82487_1_, ItemStack p_82487_2_) {
				Direction enumfacing = p_82487_1_.getBlockState().getValue(DispenserBlock.FACING);
				double d0 = p_82487_1_.getX() + enumfacing.getStepX();
				double d1 = p_82487_1_.getY() + 0.2F;
				double d2 = p_82487_1_.getZ() + enumfacing.getStepZ();
				Entity entity = ItemMonsterPlacerPlus.spawnCreature(null, p_82487_1_.getWorld(),
						p_82487_2_.getDamageValue(), d0, d1, d2,
						p_82487_2_.getTag() != null && p_82487_2_.getTag().contains("SavedEntity")
						? p_82487_2_.getTag().getCompound("SavedEntity")
								: null);

				if (entity instanceof LivingEntity && p_82487_2_.hasCustomHoverName())
					((Mob) entity).setCustomNameTag(p_82487_2_.getHoverName());

				p_82487_2_.splitStack(1);
				return p_82487_2_;
			}
		});
		class BehaviorDispenseAmmo extends DefaultDispenseItemBehavior {
			@Override
			public ItemStack dispenseStack(BlockSource source, ItemStack stack) {
				ItemStack weapon;
				switch (((ItemAmmo) stack.getItem()).getTypeInt(stack)) {
				case 1:
					weapon = ItemFromData.getNewStack("shotgun");
					break;
				case 2:
					weapon = ItemFromData.getNewStack("minigun");
					break;
				case 3:
					weapon = ItemFromData.getNewStack("pistol");
					break;
				case 4:
					weapon = ItemFromData.getNewStack("revolver");
					break;
				case 5:
					weapon = ItemFromData.getNewStack("smg");
					break;
				case 6:
					weapon = ItemFromData.getNewStack("sniperrifle");
					TF2Attribute.setAttribute(weapon, TF2Attribute.attributes[60], 2);
					break;
				case 7:
					weapon = ItemFromData.getNewStack("rocketlauncher");
					break;
				case 8:
					weapon = ItemFromData.getNewStack("grenadelauncher");
					break;
				case 9:
					weapon = ItemFromData.getNewStack("syringegun");
					break;
				case 13:
					weapon = ItemFromData.getNewStack("flaregun");
					break;
				case 14:
					weapon = ItemFromData.getNewStack("sandmanball");
					break;
				default:
					return stack;
				}
				if (weapon.isEmpty())
					return stack;
				Level world = source.getWorld();
				Position iposition = DispenserBlock.getDispensePosition(source);
				Direction enumfacing = source.getBlockState().getValue(DispenserBlock.FACING);
				dummyEnt.setPos(iposition.getX(), iposition.getY() + 0.1, iposition.getZ());
				Vec2 rot = TF2Util.getAngleFromFacing(enumfacing);
				dummyEnt.rotationYawHead = rot.x;
				dummyEnt.rotationPitch = rot.y;
				dummyEnt.level = world;
				dummyEnt.setItemInHand(InteractionHand.MAIN_HAND, weapon);
				((ItemUsable) weapon.getItem()).use(weapon, dummyEnt, world, InteractionHand.MAIN_HAND, null);
				// IProjectile iprojectile = this.getProjectileEntity(world, iposition, stack);
				// iprojectile.shoot((double)enumfacing.getFrontOffsetX(),
				// (double)((float)enumfacing.getFrontOffsetY() + 0.1F),
				// (double)enumfacing.getFrontOffsetZ(), this.getProjectileVelocity(),
				// this.getProjectileInaccuracy());
				// world.spawnEntity((Entity)iprojectile);
				((ItemAmmo) stack.getItem()).consumeAmmo(dummyEnt, stack, 1);
				return stack;
			}

			/**
			 * Play the dispense sound from the specified block.
			 */
			@Override
			protected void playDispenseSound(BlockSource source) {
				source.getWorld().playEvent(1002, source.getBlockPos(), 0);
			}
		}
		;
		class BehaviorDispenseSelf extends DefaultDispenseItemBehavior {
			@Override
			public ItemStack dispenseStack(BlockSource source, ItemStack stack) {
				Level world = source.getWorld();
				Position iposition = DispenserBlock.getDispensePosition(source);
				Direction enumfacing = source.getBlockState().getValue(DispenserBlock.FACING);
				dummyEnt.setPos(iposition.getX(), iposition.getY(), iposition.getZ());
				Vec2 rot = TF2Util.getAngleFromFacing(enumfacing);
				dummyEnt.rotationYawHead = rot.x;
				dummyEnt.rotationPitch = rot.y;
				dummyEnt.level = world;
				dummyEnt.setItemInHand(InteractionHand.MAIN_HAND, stack);
				((ItemUsable) stack.getItem()).use(stack, dummyEnt, world, InteractionHand.MAIN_HAND, null);
				// IProjectile iprojectile = this.getProjectileEntity(world, iposition, stack);
				// iprojectile.shoot((double)enumfacing.getFrontOffsetX(),
				// (double)((float)enumfacing.getFrontOffsetY() + 0.1F),
				// (double)enumfacing.getFrontOffsetZ(), this.getProjectileVelocity(),
				// this.getProjectileInaccuracy());
				// world.spawnEntity((Entity)iprojectile);
				stack.shrink(1);
				return stack;
			}

			/**
			 * Play the dispense sound from the specified block.
			 */
			@Override
			protected void playDispenseSound(BlockSource source) {
				source.getWorld().playEvent(1002, source.getBlockPos(), 0);
			}
		}
		;
		DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemAmmo, new BehaviorDispenseAmmo());
		DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemAmmoMinigun, new BehaviorDispenseAmmo());
		DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemAmmoPistol, new BehaviorDispenseAmmo());
		DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemAmmoSMG, new BehaviorDispenseAmmo());
		DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemAmmoSyringe, new BehaviorDispenseAmmo());
		DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.putObject(MapList.weaponClasses.get("jar"),
				new BehaviorDispenseSelf());
		DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.putObject(MapList.weaponClasses.get("cleaver"),
				new BehaviorDispenseSelf());
		network = new TF2NetworkWrapper(MOD_ID);
		network.registerMessage(TF2ActionHandler.class, TF2Message.ActionMessage.class, 0, Dist.SERVER, true);
		network.registerMessage(TF2PropertyHandler.class, TF2Message.PropertyMessage.class, 2, Dist.SERVER, false);
		network.registerMessage(TF2BulletHandler.class, TF2Message.BulletMessage.class, 3, Dist.SERVER, true);
		network.registerMessage(TF2ProjectileHandler.class, TF2Message.PredictionMessage.class, 4, Dist.SERVER, true);
		network.registerMessage(TF2BuildingConfigHandler.class, TF2Message.BuildingConfigMessage.class, 5, Dist.SERVER,
				false);
		network.registerMessage(TF2CapabilityHandler.class, TF2Message.CapabilityMessage.class, 7, Dist.SERVER, false);
		network.registerMessage(TF2DisguiseHandler.class, TF2Message.DisguiseMessage.class, 11, Dist.SERVER, false);
		network.registerMessage(TF2ActionHandler.class, TF2Message.ActionMessage.class, 18, Dist.CLIENT, true);
		network.registerMessage(TF2UseHandler.class, TF2Message.UseMessage.class, 1, Dist.CLIENT, true);
		network.registerMessage(TF2PropertyHandler.class, TF2Message.PropertyMessage.class, 19, Dist.CLIENT, false);
		network.registerMessage(TF2CapabilityHandler.class, TF2Message.CapabilityMessage.class, 6, Dist.CLIENT, false);
		network.registerMessage(TF2WeaponDataHandler.class, TF2Message.WeaponDataMessage.class, 8, Dist.CLIENT, false);
		network.registerMessage(TF2WearableChangeHandler.class, TF2Message.WearableChangeMessage.class, 10, Dist.CLIENT,
				false);
		network.registerMessage(TF2WeaponDropHandler.class, TF2Message.WeaponDroppedMessage.class, 12, Dist.CLIENT,
				true);
		network.registerMessage(TF2ContractHandler.class, TF2Message.ContractMessage.class, 13, Dist.CLIENT, false);
		network.registerMessage(TF2VelocityAddHandler.class, TF2Message.VelocityAddMessage.class, 14, Dist.CLIENT,
				true);
		network.registerMessage(TF2AttackSyncHandler.class, TF2Message.AttackSyncMessage.class, 15, Dist.SERVER, true);
		network.registerMessage(TF2InitHandler.class, TF2Message.InitMessage.class, 16, Dist.CLIENT, false);
		network.registerMessage(TF2InitClientHandler.class, TF2Message.InitClientMessage.class, 17, Dist.SERVER, false);
		network.registerMessage(TF2EffectCooldownHandler.class, TF2Message.EffectCooldownMessage.class, 20, Dist.CLIENT,
				false);
		network.registerMessage(TF2ParticleSpawnHandler.class, TF2Message.ParticleSpawnMessage.class, 21, Dist.CLIENT,
				true);
		network.registerMessage(TF2PlayerCapabilityHandler.class, TF2Message.PlayerCapabilityMessage.class, 22,
				Dist.CLIENT, false);
		network.registerMessage(TF2GuiConfigHandler.class, TF2Message.GuiConfigMessage.class, 23, Dist.CLIENT, false);
		network.registerMessage(TF2GuiConfigHandler.class, TF2Message.GuiConfigMessage.class, 24, Dist.SERVER, false);
		network.registerMessage(TF2ShowGuiHandler.class, TF2Message.ShowGuiMessage.class, 25, Dist.CLIENT, false);
		network.registerMessage(TF2ShowGuiHandler.class, TF2Message.ShowGuiMessage.class, 26, Dist.SERVER, false);
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new IGuiHandler() {

			@Override
			public Object getServerGuiElement(int ID, Player player, Level world, int x, int y, int z) {
				BlockPos pos = new BlockPos(x, y, z);
				if (ID == 0) {
					ContainerWearables container = new ContainerWearables(player.inventory,
							player.getCapability(INVENTORY_CAP).orElse(null), false, player);
					container.addListener(new TF2EventsCommon.TF2ContainerListener((ServerPlayer) player));
					return container;
				} else if (ID == 1 && world.getBlockState(pos).getBlock() instanceof BlockCabinet)
					return new ContainerTF2Workbench(player, player.inventory,
							/* (TileEntityCabinet) world.getTileEntity(pos), */ world, pos);
				else if (ID == 2 && world.getBlockEntity(pos) != null
						&& world.getBlockEntity(pos) instanceof TileEntityUpgrades) {
					if (((TileEntityUpgrades) world.getBlockEntity(pos)).attributes.size() == 0) {
						((TileEntityUpgrades) world.getBlockEntity(pos)).generateUpgrades(player.getRandom());
					}
					if (player instanceof ServerPlayer)
						((ServerPlayer) player).connection.sendPacket(world.getBlockEntity(pos).getUpdatePacket());
					return new ContainerUpgrades(player, player.inventory,
							(TileEntityUpgrades) world.getBlockEntity(pos), world, pos);
				} else if (ID == 3 && world.getBlockEntity(pos) != null
						&& world.getBlockEntity(pos) instanceof TileEntityAmmoFurnace)
					return new ContainerAmmoFurnace(player.inventory, (TileEntityAmmoFurnace) world.getBlockEntity(pos));
				else if (ID == 4 && world.getEntity(x) != null
						&& world.getEntity(x) instanceof EntityTF2Character)
					return new ContainerMercenary(player, (EntityTF2Character) world.getEntity(x), world);
				else if (ID == 5 && world.getEntity(x) != null
						&& world.getEntity(x) instanceof EntityBuilding) {
					if (world.getEntity(x) instanceof EntityDispenser)
						return new ContainerDispenser((EntityDispenser) world.getEntity(x), player.inventory);
					else
						return new ContainerEnergy((EntityBuilding) world.getEntity(x), player.inventory);
				} else if (ID == 6 && world.getBlockEntity(pos) != null
						&& world.getBlockEntity(pos) instanceof TileEntityRobotDeploy)
					return new ContainerRobotDeploy(player.inventory, (TileEntityRobotDeploy) world.getBlockEntity(pos));
				else if (ID == 7 && world.getBlockEntity(pos) != null
						&& world.getBlockEntity(pos) instanceof IEntityConfigurable)
					return new ContainerConfigurable(player, player.inventory,
							(IEntityConfigurable) world.getBlockEntity(pos), world, pos);
				return null;
			}

			@Override
			public Object getClientGuiElement(int ID, Player player, Level world, int x, int y, int z) {
				BlockPos pos = new BlockPos(x, y, z);
				if (ID == 0)
					return new GuiWearables(new ContainerWearables(player.inventory,
							player.getCapability(INVENTORY_CAP).orElse(null), true, player));
				else if (ID == 1 && world.getBlockState(pos).getBlock() instanceof BlockCabinet)
					return new GuiTF2Crafting(player.inventory,
							/* (TileEntityCabinet) world.getTileEntity(pos), */world, pos);
				else if (ID == 2 && world.getBlockEntity(pos) != null
						&& world.getBlockEntity(pos) instanceof TileEntityUpgrades) {
					return new GuiUpgradeStation(player.inventory, (TileEntityUpgrades) world.getBlockEntity(pos), world,
							pos);
				} else if (ID == 3 && world.getBlockEntity(pos) != null
						&& world.getBlockEntity(pos) instanceof TileEntityAmmoFurnace)
					return new GuiAmmoFurnace(player.inventory, (TileEntityAmmoFurnace) world.getBlockEntity(pos));
				else if (ID == 4 && world.getEntity(x) != null
						&& world.getEntity(x) instanceof EntityTF2Character)
					return new GuiMercenary(player.inventory, (EntityTF2Character) world.getEntity(x), world);
				else if (ID == 5 && world.getEntity(x) != null
						&& world.getEntity(x) instanceof EntityBuilding) {
					if (world.getEntity(x) instanceof EntitySentry)
						return new GuiSentry((EntitySentry) world.getEntity(x));
					else if (world.getEntity(x) instanceof EntityDispenser)
						return new GuiDispenser((EntityDispenser) world.getEntity(x));
					else if (world.getEntity(x) instanceof EntityTeleporter)
						return new GuiTeleporter((EntityTeleporter) world.getEntity(x));
				} else if (ID == 6 && world.getBlockEntity(pos) != null
						&& world.getBlockEntity(pos) instanceof TileEntityRobotDeploy)
					return new GuiRobotDeploy(player.inventory, (TileEntityRobotDeploy) world.getBlockEntity(pos));
				else if (ID == 7 && world.getBlockEntity(pos) != null
						&& world.getBlockEntity(pos) instanceof IEntityConfigurable)
					return new GuiConfigurable(player.inventory, (IEntityConfigurable) world.getBlockEntity(pos), world,
							pos);
				return null;
			}

		});

		refinedFuel = FluidRegistry.getFluid("refined_fuel");
		// TickRegistry.registerTickHandler(new CommonTickHandler(),
		// Side.SERVER);

		// FMLCommonHandler.instance().bus().register(new
		// TF2EventBusListener());
		proxy.registerRenderInformation();

	}

	@Mod.EventHandler
	public void postinit(FMLPostInitializationEvent event) {

		updateMobSpawning();
		updateOreGenStatus();
		squakeLoaded = Loader.isModLoaded("squake");

		animalsDisguise = new ArrayList<>();
		animalsDisguise.add(new ResourceLocation("pig"));
		animalsDisguise.add(new ResourceLocation("sheep"));
		animalsDisguise.add(new ResourceLocation("cow"));
		animalsDisguise.add(new ResourceLocation("chicken"));
		animalsDisguise.add(new ResourceLocation("horse"));
	}

	@Mod.EventHandler
	public void modMessage(IMCEvent event) {
		for (IMCMessage message : event.getMessages()) {
			if ("addcraftingrecipe".equals(message.key)) {
				TF2CraftingManager.INSTANCE
				.addRecipe(ForgeRegistries.RECIPES.getValue(message.getResourceLocationValue()));
				((IForgeRegistryModifiable<Recipe>) ForgeRegistries.RECIPES)
				.remove(message.getResourceLocationValue());
			} else if ("removecraftingrecipeoutput".equals(message.key)) {
				for (Iterator<Recipe> it = TF2CraftingManager.INSTANCE.getRecipeList().iterator(); it.hasNext();) {
					Recipe recipe = it.next();
					if (recipe.getRecipeOutput().sameItem(message.getItemStackValue())) {
						it.remove();
					}
				}
			} else if ("removecraftingrecipeid".equals(message.key)) {
				TF2CraftingManager.INSTANCE.getRecipeList().remove(Integer.parseInt(message.getStringValue()));
			} else if ("addsaxtonhalerecipe".equals(message.key)) {
				MerchantOffer recipe = new MerchantOffer(message.getNBTValue());
				EntitySaxtonHale.addRecipes.add(recipe);
			} else if ("removesaxtonhalerecipe".equals(message.key)) {
				MerchantOffer recipe = new MerchantOffer(message.getNBTValue());
				EntitySaxtonHale.removeRecipes.add(recipe);
			}
		}
	}

	public static void updateOreGenStatus() {

		// System.out.println("Generowane: " +
		// OreDictionary.getOres("oreCopper").size());
		String copper = conf.get("world gen", "Generate copper", "Default")
				.setValidValues(new String[] { "Always", "Default", "Never" }).getString();
		String lead = conf.get("world gen", "Generate lead", "Default")
				.setValidValues(new String[] { "Always", "Default", "Never" }).getString();
		String australium = conf.get("world gen", "Generate australium", "Always")
				.setValidValues(new String[] { "Always", "Never" }).getString();
		generateCopper = copper.equals("Always");
		generateLead = lead.equals("Always");
		generateAustralium = australium.equals("Always");

		if (copper.equals("Default") && OreDictionary.getOres("oreCopper").size() == 1)
			generateCopper = true;
		if (lead.equals("Default") && OreDictionary.getOres("oreLead").size() == 1)
			generateLead = true;
	}

	public static void registerBlock(Block block, String name) {
		ForgeRegistries.BLOCKS.register(block.setRegistryName(name));
		BlockItem item = new BlockItem(block);
		item.setRegistryName(block.getRegistryName());
		ForgeRegistries.ITEMS.register(item);
		proxy.registerItemBlock(item);
	}

	public static void loadWeapons() {

		MapList.nameToData.clear();
		MapList.buildInAttributes.clear();
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(output)));

			loadConfig(new File(instance.weaponDir, "Weapons.json"), stream);
			File[] files = instance.weaponDir.listFiles((FilenameFilter) (arg0, arg1) -> arg1.endsWith(".json") && !arg1.equalsIgnoreCase("Weapons.json"));
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
		loadConfig(Files.toString(file, StandardCharsets.UTF_8), file.getName(), output);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void loadConfig(String fileData, String fileName, DataOutput output) {
		/*
		 * Configuration weaponsFile= new Configuration(file); weaponsFile.load();
		 */
		ArrayList<WeaponData> list = WeaponData.parseFile(fileData, fileName);
		for (WeaponData data : list) {
			String weaponEntry = data.getName();
			// Class<?> weaponClass =
			// MapList.weaponClasses.get(weaponData.get("Class").getString());
			try {
				// System.out.println("attach "+weaponEntry);
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

			} catch (Exception var4) {
				var4.printStackTrace();
			}
		}
	}

	public static void loadWeapon(String name, WeaponData weapon) {

		/*
		 * else{ weaponList[Integer.parseInt(weaponEntry)] =(ItemUsable)
		 * weaponClass.getConstructor(new Class[] {ConfigCategory.class,
		 * ConfigCategory.class}).newInstance(new Object[] {weaponData, null}); }
		 */
		// GameRegistry.registerItem(weaponList[Integer.parseInt(weaponEntry)],
		// "weapon"+Integer.parseInt(weaponEntry));
		MapList.nameToData.put(name, weapon);
		// System.out.println("Weapon read: "+name);
		/*
		 * for(Entry<PropertyType, WeaponData.Property>
		 * entry:weapon.properties.entrySet()){
		 * System.out.println("Property: "+entry.getKey().name+" Value: "+entry.
		 * getValue().intValue+" "+entry.getValue().stringValue); }
		 */
		// LanguageRegistry.instance().addStringLocalization(weaponData.get("Name").getString()+".name",
		// weaponData.get("Name").getString());
		CompoundTag tag = new CompoundTag();
		tag.putString("Type", name);
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
		// System.out.println("merged: "+additional.getName()+" "+key);

		// new ConfigCategory(null, additional);
		return additional;
	}

	@Mod.EventHandler
	public void serverPreInit(FMLServerAboutToStartEvent event) {
		// System.out.println("Starting server");

		if (!event.getServer().isDedicatedServer())
			for (WeaponData weapon : MapList.nameToData.values())
				ClientProxy.RegisterWeaponData(weapon);
		/*
		 * if(event.getSide()==Side.SERVER) AchievementPage.registerAchievementPage(new
		 * TF2Achievements());
		 */
	}

	@Mod.EventHandler
	public void serverInit(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandGiveWeapon());
		event.registerServerCommand(new CommandResetWeapons());
		event.registerServerCommand(new CommandResetStat());
		event.registerServerCommand(new CommandForceClass());
		event.registerServerCommand(new CommandGenerateReferences());
		event.registerServerCommand(new CommandChangeConfig());
		event.registerServerCommand(new CommandInvasion());
		event.registerServerCommand(new CommandGameArena());
		if (event.getServer().isSinglePlayer())
			TF2UdpClient.addressToUse = "127.0.0.1";
		try {
			server = event.getServer();
			File input = new File(((AnvilSaveConverter) server.getActiveAnvilConverter()).savesDirectory,
					server.getFolderName() + "/teleports.dat");
			CompoundTag tagRoot = NbtIo.readCompressed(new FileInputStream(input));
			CompoundTag tag = tagRoot.getCompound("Teleporters");
			for (String keys : tag.getAllKeys()) {
				TeleporterData[] blockArray = new TeleporterData[EntityTeleporter.TP_PER_PLAYER];
				EntityTeleporter.teleporters.put(UUID.fromString(keys), blockArray);
				CompoundTag exitTag = tag.getCompound(keys);
				for (int i = 0; i < EntityTeleporter.TP_PER_PLAYER; i++)
					if (exitTag.contains(Integer.toString(i))) {
						int[] array = exitTag.getIntArray(Integer.toString(i));
						blockArray[i] = new TeleporterData(new BlockPos(array[0], array[1], array[2]), array[3],
								array[4]);
					}
			}
			EntityTeleporter.tpCount = tagRoot.getInt("TPCount");

		} catch (IOException e) {
			System.err.println("Reading teleporter data skipped");
		}

		if (TF2ConfigVars.enableUdp) {
			try {
				udpServer = new TF2UdpServer(
						event.getServer().isSinglePlayer() ? 12454 : event.getServer().getServerPort());
				udpServer.start();
				TF2weapons.network.useUdp = true;
				// new TF2UdpClient(SocketUtils.socketAddress("127.0.0.1", 12454));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppingEvent event) {
		// MapList.nameToData.clear();
		// MapList.buildInAttributes.clear();

		File output = new File(((AnvilSaveConverter) server.getActiveAnvilConverter()).savesDirectory,
				server.getFolderName() + "/teleports.dat");
		CompoundTag tagRoot = new CompoundTag();
		CompoundTag tag = new CompoundTag();
		tagRoot.put("Teleporters", tag);

		for (Entry<UUID, TeleporterData[]> entry : EntityTeleporter.teleporters.entrySet()) {
			CompoundTag exitTag = new CompoundTag();
			for (int i = 0; i < EntityTeleporter.TP_PER_PLAYER; i++) {
				TeleporterData blockPos = entry.getValue()[i];
				if (blockPos != null)
					exitTag.putIntArray(Integer.toString(i), new int[] { blockPos.getX(), blockPos.getY(),
							blockPos.getZ(), blockPos.id, blockPos.dimension });
			}
			tag.put(entry.getKey().toString(), exitTag);
		}
		tagRoot.putInt("TPCount", EntityTeleporter.tpCount);

		try {
			NbtIo.writeCompressed(tagRoot, new FileOutputStream(output));
		} catch (IOException e) {
			e.printStackTrace();
		}

		EntityTeleporter.teleporters.clear();
		EntityTeleporter.tpCount = 0;
		if (udpServer != null) {
			udpServer.stopServer();
			udpServer = null;
		}
	}

	/*
	 * public static void openWearableGUI(EntityPlayerMP player){ InventoryBasic
	 * inventory=new InventoryBasic("Wearables", false, 3); NBTTagList nbttaglist =
	 * player.getEntityData().getTagList("Wearables", 10); for (int i = 0; i <
	 * nbttaglist.tagCount(); ++i) { NBTTagCompound nbttagcompound =
	 * nbttaglist.getCompoundTagAt(i); int j = nbttagcompound.getByte("Slot") & 255;
	 * inventory.setInventorySlotContents(j,
	 * ItemStack.loadItemStackFromNBT(nbttagcompound)); } if (player.openContainer
	 * != player.inventoryContainer) { player.closeScreen(); }
	 *
	 * player.getNextWindowId(); player.connection.sendPacket(new
	 * SPacketOpenWindow(player.currentWindowId, "rafradek_wearables",
	 * inventory.getDisplayName(), inventory.getSizeInventory(),
	 * player.getEntityId())); player.openContainer = new
	 * ContainerHorseInventory(player.inventory, inventory, horse, this);
	 * player.openContainer.windowId = player.currentWindowId;
	 * player.openContainer.addListener(this); }
	 */

	public static void updateMobSpawning() {
		ArrayList<Biome> biomesList = new ArrayList<>();
		for (Biome biome : GameRegistry.findRegistry(Biome.class)) {
			biome.getSpawnableList(EnumCreatureType.MONSTER)
			.removeIf(entry -> TF2ConfigVars.spawnRate.containsKey(entry.entityClass));
			if (TF2ConfigVars.biomeCheck.equalsIgnoreCase("Default")) {
				boolean zombie = false;
				boolean spider = false;
				boolean creeper = false;
				for (SpawnListEntry entry : biome.getSpawnableList(EnumCreatureType.MONSTER)) {
					if (entry.entityClass == Zombie.class)
						zombie = true;
					else if (entry.entityClass == Spider.class)
						spider = true;
					else if (entry.entityClass == Creeper.class)
						creeper = true;
					if (zombie && spider && creeper) {
						biomesList.add(biome);
						break;
					}
				}
			} else if (TF2ConfigVars.biomeCheck.equalsIgnoreCase("Vanilla")
					&& biome.delegate.name().getResourceDomain().equals("minecraft") && biome != Biomes.HELL
					&& biome != Biomes.SKY) {
				biomesList.add(biome);
			} else if (TF2ConfigVars.biomeCheck.equalsIgnoreCase("All"))
				biomesList.add(biome);
		}
		if (!TF2ConfigVars.disableSpawn) {
			Biome[] biomes = biomesList.toArray(new Biome[biomesList.size()]);
			for (Entry<Class<? extends Mob>, Integer> entry : TF2ConfigVars.spawnRate.entrySet()) {
				EntityRegistry.addSpawn(entry.getKey(), entry.getValue().intValue(), 1, 3, EnumCreatureType.MONSTER,
						biomes);
			}
		}
	}

	public static class NullStorage<T> implements IStorage<T> {

		@Override
		public Tag writeNBT(Capability<T> capability, T instance, Direction side) {
			return null;
		}

		@Override
		public void readNBT(Capability<T> capability, T instance, Direction side, Tag nbt) {}

	}

	public static class NullCallable<T> implements Callable<T> {

		@Override
		public T call() throws Exception {
			return null;
		}

	}
}
