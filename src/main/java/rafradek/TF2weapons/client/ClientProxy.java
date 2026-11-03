package rafradek.TF2weapons.client;

import rafradek.TF2weapons.client.compat.RenderLivingBase;
import rafradek.TF2weapons.client.compat.RenderBiped;

import rafradek.TF2weapons.client.compat.ModelBase;
import rafradek.TF2weapons.client.compat.ModelBiped;
import rafradek.TF2weapons.client.compat.GlStateManager;

import rafradek.TF2weapons.message.MessageContext;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.*;
import rafradek.TF2weapons.client.gui.GuiConfirm;
import rafradek.TF2weapons.client.gui.GuiDisguiseKit;
import rafradek.TF2weapons.client.gui.inventory.GuiSentry;
import rafradek.TF2weapons.client.gui.inventory.GuiTeleporter;
import rafradek.TF2weapons.client.model.ModelRocket;
import rafradek.TF2weapons.client.particle.*;
import rafradek.TF2weapons.client.renderer.LayerWearables;
import rafradek.TF2weapons.client.renderer.entity.*;
import rafradek.TF2weapons.client.renderer.tileentity.RenderDoor;
import rafradek.TF2weapons.client.renderer.tileentity.RenderRobotDeploy;
import rafradek.TF2weapons.common.CommonProxy;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.entity.EntityPickup;
import rafradek.TF2weapons.entity.EntityStatue;
import rafradek.TF2weapons.entity.boss.EntityHHH;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import rafradek.TF2weapons.entity.boss.EntityMonoculus;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.entity.mercenary.EntitySaxtonHale;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.mercenary.InvasionEvent;
import rafradek.TF2weapons.entity.projectile.*;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.tileentity.TileEntityOverheadDoor;
import rafradek.TF2weapons.tileentity.TileEntityRobotDeploy;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("rawtypes")
public class ClientProxy extends CommonProxy {

	public static HashMap<String, ModelBase> entityModel = new HashMap<>();
	public static HashMap<String, ResourceLocation> textureDisguise = new HashMap<>();

	public static RenderCustomModel disguiseRender;
	public static RenderLivingBase disguiseRenderPlayer;
	public static RenderLivingBase disguiseRenderPlayerSmall;
	public static RenderLivingBase forceTextureRenderPlayer;
	public static TextureAtlas particleMap;
	public static KeyMapping reload = new KeyMapping("key.reload", Keyboard.KEY_R, "TF2");
	public static KeyMapping backpackitem = new KeyMapping("key.activatebackpack", Keyboard.KEY_0, "TF2");
	public static ResourceLocation scopeTexture = new ResourceLocation(TF2weapons.MOD_ID, "textures/misc/scope.png");
	// public static Map<MinigunLoopSound, EntityLivingBase > spinSounds;
	public static BiMap<LivingEntity, WeaponSound> fireSounds;
	public static Map<LivingEntity, ReloadSound> reloadSounds;
	public static Map<String, ModelResourceLocation> nameToModel;
	public static ConcurrentMap<LivingEntity, ItemStack> soundsToStart;
	public static ResourceLocation blackTexture = new ResourceLocation(TF2weapons.MOD_ID, "textures/misc/black.png");
	public static ResourceLocation circleTexture = new ResourceLocation(TF2weapons.MOD_ID, "textures/misc/circle.png");
	public static ResourceLocation healingTexture = new ResourceLocation(TF2weapons.MOD_ID, "textures/gui/healing.png");
	public static ResourceLocation buildingTexture = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/buildings.png");
	public static ResourceLocation blueprintTexture = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/blueprints.png");
	public static ResourceLocation chargeTexture = new ResourceLocation(TF2weapons.MOD_ID, "textures/misc/charge.png");
	public static final ResourceLocation WIDGETS_TEXTURE_MC = new ResourceLocation("textures/gui/widgets.png");
	public static final ResourceLocation VIGNETTE = new ResourceLocation("textures/misc/vignette.png");
	public static List<WeaponSound> weaponSoundsToStart;
	public static int renderCritGlow;
	public static boolean inRenderHand;
	public static boolean inRenderHandTicked;
	public static boolean buildingsUseEnergy;

	public static EnumMap<EnumTF2Particles, IParticleFactory> particleFactories = new EnumMap<>(EnumTF2Particles.class);
	public static final Logger LOGGER = (Logger) LogManager.getLogger();
	public static Set<Class<? extends Block>> interactingBlocks;

	@Override
	public void registerItemBlock(BlockItem item) {
		for (int i = 0; i < 16; i++)
			ModelLoader.setCustomModelResourceLocation(item, i,
					new ModelResourceLocation(item.getRegistryName(), "inventory"));

	}

	public static void RegisterWeaponData(WeaponData weapon) {

		Item item = MapList.weaponClasses.get(weapon.getString(PropertyType.CLASS));

		if (item instanceof ItemFromData)
			((ItemFromData) item).registerModels(weapon);

	}

	@Override
	@SuppressWarnings("deprecation")
	public void registerRenderInformation() {

		Minecraft.getInstance().getItemColors().registerItemColorHandler((stack, tintIndex) -> {
			if (stack.getDamageValue() < 25) {
				return stack.getDamageValue() < 9 ? 16711680 : 255;
			} else if (stack.getDamageValue() / 2 == 13)
				return 0xFFD400;
			else if (stack.getDamageValue() == 28)
				return 0x743501;
			else if (stack.getDamageValue() == 29)
				return 0x1B013A;
			else if (stack.getDamageValue() == 30)
				return 0x20582B;
			else if (stack.getDamageValue() < 45)
				return 0x2AAAFF;
			return stack.getDamageValue() / 2 == 13 ? 0xFFFFFF : (stack.getDamageValue() % 2 == 0 ? 16711680 : 255);
		}, TF2weapons.itemPlacer);
		Collection<Item> items = new ArrayList<>(ForgeRegistries.ITEMS.getValues());
		items.removeIf(item -> !(item instanceof ItemFromData || item instanceof DiggerItem || item instanceof SwordItem
				|| item instanceof BowItem || item == TF2weapons.itemTF2));

		Minecraft.getInstance().getItemColors().registerItemColorHandler((ItemColor) (stack, tintIndex) -> {
			if (ItemFromData.getData(stack).hasProperty(PropertyType.COLOR))
				return ItemFromData.getData(stack).getInt(PropertyType.COLOR);
			if (renderCritGlow > 15) {
				int color = TF2Util.colorCode[renderCritGlow % 16];
				if (renderCritGlow < 32)
					color |= 0x7F7F7F;
				if (renderCritGlow == 28)
					color = 0xFFB060;
				else if (renderCritGlow == 25)
					color = 0x60B0FF;
				else if (renderCritGlow == 44)
					color = 0xFF5050;
				else if (renderCritGlow == 41)
					color = 0x5050FF;
				return color;
			} else if (stack.hasTag() && stack.getTag().getBoolean("Australium"))
				return 0xFFD400;
			if (stack.getItem() instanceof ItemUsable && tintIndex == 1)
				return TF2Util.colorCode[renderCritGlow % 16];
			else
				return 0xFFFFFF;
		}, items.toArray(new Item[items.size()]));

		for (PlayerRenderer render : Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().values()) {
			render.addLayer(new LayerWearables(render));
		}
		reloadSounds = new HashMap<>();
		soundsToStart = new ConcurrentHashMap<>();
		weaponSoundsToStart = new ArrayList<>();
		fireSounds = HashBiMap.create();
		ClientRegistry.registerKeyBinding(ClientProxy.reload);
		ClientRegistry.registerKeyBinding(ClientProxy.backpackitem);
		// disguiseRender = new
		// RenderCustomModel(Minecraft.getMinecraft().getRenderManager(), new
		// ModelBiped(), 0);
		disguiseRenderPlayer = new RenderPlayerDisguised(Minecraft.getInstance().getEntityRenderDispatcher(), false);
		disguiseRenderPlayerSmall = new RenderPlayerDisguised(Minecraft.getInstance().getEntityRenderDispatcher(), true);
		forceTextureRenderPlayer = new RenderPlayerForceTexture(Minecraft.getInstance().getEntityRenderDispatcher(), false);
		interactingBlocks = new HashSet<>();
		Method usemethod = null;
		for (Method method : Block.class.getMethods()) {
			if (method.getName().equals("onBlockActivated") || method.getName().equals("func_180639_a")) {
				usemethod = method;
			}
		}
		for (Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
			try {
				if (!interactingBlocks.contains(block.getClass()))
					for (Method method : block.getClass().getMethods()) {
						if ((method.getName().equals("onBlockActivated") || method.getName().equals("func_180639_a"))
								&& !method.equals(usemethod)) {
							interactingBlocks.add(block.getClass());
							break;
						}
					}
			} catch (NoClassDefFoundError err) {

			}
		}
		try {
			// System.out.println("Is Class: "+logger.getClass().getCanonicalName());
			/*
			 * Filter filter= new AbstractFilter() {
			 *
			 * @Override public Result filter(final LogEvent event) {
			 * if(event.getLoggerName().equals(
			 * "net.minecraft.client.multiplayer.GuiConnecting")) {
			 * //System.out.println(event.getMessage().getParameters()[0]);
			 * //System.out.println(event.getLoggerName()); TF2UdpClient.addressToUse =
			 * (String) event.getMessage().getParameters()[0]; } return Result.NEUTRAL; } };
			 * filter.start(); LOGGER.get().addFilter(filter);
			 */
			// logger.get().addAppender(app, org.apache.logging.log4j.Level.ALL, null);
		} catch (Exception e) {

		}
		particleFactories.put(EnumTF2Particles.EXPLOSION, new ParticleExplosion.Factory());
		particleFactories.put(EnumTF2Particles.BULLET_TRACER, new EntityBulletTracer.Factory());
	}

	@Override
	public Player getPlayerForSide(MessageContext ctx) {
		return ctx.side == Dist.SERVER ? ctx.getServerHandler().player : Minecraft.getInstance().player;
	}

	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(new TF2EventsClient());
		MinecraftForge.EVENT_BUS.register(PyrolandRenderer.INSTANCE);
		OBJLoader.INSTANCE.addDomain(TF2weapons.MOD_ID.toLowerCase());

		for (int i = 1; i < ItemAmmo.AMMO_TYPES.length; i++) {

			if (i != 10 && i != 12)
				ModelLoader.setCustomModelResourceLocation(TF2weapons.itemAmmo, i,
						new ModelResourceLocation(TF2weapons.MOD_ID + ":ammo_" + ItemAmmo.AMMO_TYPES[i], "inventory"));
		}

		// ModelLoader.registerItemVariants(TF2weapons.itemTF2, new
		// ModelResourceLocation(TF2weapons.MOD_ID+":copper_ingot",
		// "inventory"),new
		// ModelResourceLocation(TF2weapons.MOD_ID+":lead_ingot", "inventory"));
		nameToModel = new HashMap<>();
		for (WeaponData weapon : MapList.nameToData.values())
			// System.out.println("Execut "+weapon.getName());
			RegisterWeaponData(weapon);

		for (Item item : MapList.weaponClasses.values()) {
			ModelLoader.setCustomMeshDefinition(item, ((ItemFromData) item).getMeshDefinition());
		}

		ModelResourceLocation spawnEgg = new ModelResourceLocation("spawn_egg", "inventory");

		ModelBakery.registerItemVariants(TF2weapons.itemPlacer, spawnEgg);
		ModelLoader.setCustomMeshDefinition(TF2weapons.itemPlacer, stack -> spawnEgg);
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemDisguiseKit, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":disguise_kit", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemSandvich, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":sandvich", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemAmmoBelt, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":ammo_belt", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemStatue, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":statue", "inventory"));
		// Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ItemBlock.getItemFromBlock(TF2weapons.blockCabinet),
		// 0, new
		// ModelResourceLocation(TF2weapons.MOD_ID+":tf2workbench","inventory"));

		final ModelResourceLocation sentryRed = new ModelResourceLocation(TF2weapons.MOD_ID + ":sentryred",
				"inventory");
		final ModelResourceLocation sentryBlu = new ModelResourceLocation(TF2weapons.MOD_ID + ":sentryblu",
				"inventory");
		final ModelResourceLocation dispenserRed = new ModelResourceLocation(TF2weapons.MOD_ID + ":dispenserred",
				"inventory");
		final ModelResourceLocation dispenserBlu = new ModelResourceLocation(TF2weapons.MOD_ID + ":dispenserblu",
				"inventory");
		final ModelResourceLocation teleporterRed = new ModelResourceLocation(TF2weapons.MOD_ID + ":teleporterred",
				"inventory");
		final ModelResourceLocation teleporterBlu = new ModelResourceLocation(TF2weapons.MOD_ID + ":teleporterblu",
				"inventory");
		final ModelResourceLocation killstreak = new ModelResourceLocation(TF2weapons.MOD_ID + ":killstreak_kit",
				"inventory");
		final ModelResourceLocation killstreakSpec = new ModelResourceLocation(
				TF2weapons.MOD_ID + ":killstreak_kit_specialized", "inventory");
		final ModelResourceLocation killstreakPro = new ModelResourceLocation(
				TF2weapons.MOD_ID + ":killstreak_kit_professional", "inventory");
		final ModelResourceLocation ammoBox = new ModelResourceLocation(TF2weapons.MOD_ID + ":ammo_box", "inventory");
		final ModelResourceLocation ammoBoxSmall = new ModelResourceLocation(TF2weapons.MOD_ID + ":ammo_box_small",
				"inventory");

		ModelBakery.registerItemVariants(TF2weapons.itemBuildingBox, sentryRed, sentryBlu, dispenserRed, dispenserBlu,
				teleporterRed, teleporterBlu);
		ModelLoader.setCustomMeshDefinition(TF2weapons.itemBuildingBox, stack -> {
			if (stack.getDamageValue() == 18)
				return sentryRed;
			else if (stack.getDamageValue() == 19)
				return sentryBlu;
			else if (stack.getDamageValue() == 20)
				return dispenserRed;
			else if (stack.getDamageValue() == 21)
				return dispenserBlu;
			else if (stack.getDamageValue() == 22)
				return teleporterRed;
			else
				return teleporterBlu;
		});
		ModelBakery.registerItemVariants(TF2weapons.itemKillstreak, killstreak, killstreakSpec, killstreakPro);
		ModelLoader.setCustomMeshDefinition(TF2weapons.itemKillstreak, stack -> {
			switch (((ItemKillstreakKit) stack.getItem()).getLevel(stack)) {
			case 1:
				return killstreak;
			case 2:
				return killstreakSpec;
			case 3:
				return killstreakPro;
			default:
				return killstreak;
			}
		});

		ModelBakery.registerItemVariants(TF2weapons.itemAmmoPackage, ammoBox, ammoBoxSmall);
		ModelLoader.setCustomMeshDefinition(TF2weapons.itemAmmoPackage,
				stack -> stack.getDamageValue() / 16 == 0 ? ammoBoxSmall : ammoBox);

		final ModelResourceLocation killstreakFab = new ModelResourceLocation(
				TF2weapons.MOD_ID + ":killstreak_fabricator", "inventory");
		final ModelResourceLocation killstreakFabSpec = new ModelResourceLocation(
				TF2weapons.MOD_ID + ":killstreak_fabricator_spec", "inventory");
		final ModelResourceLocation killstreakFabPro = new ModelResourceLocation(
				TF2weapons.MOD_ID + ":killstreak_fabricator_pro", "inventory");
		ModelBakery.registerItemVariants(TF2weapons.itemKillstreakFabricator, killstreakFab, killstreakFabSpec,
				killstreakFabPro);
		ModelLoader.setCustomMeshDefinition(TF2weapons.itemKillstreakFabricator, stack -> {
			switch (((ItemKillstreakFabricator) stack.getItem()).getLevel(stack)) {
			case 1:
				return killstreakFab;
			case 2:
				return killstreakFabSpec;
			case 3:
				return killstreakFabPro;
			default:
				return killstreakFab;
			}
		});
		for (int i = 0; i < ItemRobotPart.LEVEL.length; i++)
			ModelLoader.setCustomModelResourceLocation(TF2weapons.itemRobotPart, i,
					new ModelResourceLocation(TF2weapons.MOD_ID + ":robot_part_" + i, "inventory"));

		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemAmmoFire, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":ammo_fire", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemAmmoPistol, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":pistol_mag", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemAmmoSMG, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":smg_mag", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemAmmoSyringe, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":ammo_syringe", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemAmmoMinigun, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":ammo_minigun", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemStrangifier, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":strangifier", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemChocolate, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":chocolate", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTarget, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":target", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemHorn, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":horn", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemMantreads, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":mantreads", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemScoutBoots, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":scout_shoes", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemGunboats, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":gunboats", "inventory"));
		for (int i = 0; i < InvasionEvent.DIFFICULTY.length; i++)
			ModelLoader.setCustomModelResourceLocation(TF2weapons.itemEventMaker, i,
					new ModelResourceLocation(TF2weapons.MOD_ID + ":tour_ticket", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemAmmoMedigun, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":ammo_medigun", "inventory"));

		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":copper_ingot", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 1,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":lead_ingot", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 2,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":australium_ingot", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 3,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":scrap_metal", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 4,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":reclaimed_metal", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 5,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":refined_metal", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 6,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":australium_nugget", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 7,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":key", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 8,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":crate", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 9,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":random_weapon", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 10,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":random_hat", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 11,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":logic_board", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 12,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":robot_part_pack", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, 13,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":robot_part_pack", "inventory"));

		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemToken, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":token_scout", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemToken, 1,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":token_soldier", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemToken, 2,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":token_pyro", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemToken, 3,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":token_demoman", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemToken, 4,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":token_heavy", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemToken, 5,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":token_engineer", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemToken, 6,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":token_medic", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemToken, 7,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":token_sniper", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemToken, 8,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":token_spy", "inventory"));

		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemDoorController, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":door_controller_player", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemDoorController, 1,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":door_controller_entity", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemDoorController, 2,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":door_controller_red", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemDoorController, 3,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":door_controller_blu", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemBossSpawn, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":boss_hhh", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemBossSpawn, 1,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":boss_monoculus", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemBossSpawn, 2,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":boss_merasmus", "inventory"));

		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemMoney, 0,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":money_small", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemMoney, 1,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":money_normal", "inventory"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemMoney, 2,
				new ModelResourceLocation(TF2weapons.MOD_ID + ":money_big", "inventory"));

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOverheadDoor.class, new RenderDoor());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRobotDeploy.class, new RenderRobotDeploy());

		RenderingRegistry.registerEntityRenderingHandler(EntityTF2Character.class, RenderTF2Character::new);
		/*
		 * RenderingRegistry.registerEntityRenderingHandler(EntityProjectileBase .class,
		 * new IRenderFactory<Entity>(){
		 *
		 * @Override public Render<Entity> createRenderFor(RenderManager manager) { //
		 * return new RenderEntity(manager); } });
		 */
		RenderingRegistry.registerEntityRenderingHandler(EntityRocket.class, RenderRocket::new);
		/*
		 * RenderingRegistry.registerEntityRenderingHandler(EntityFlame.class, new
		 * IRenderFactory<EntityFlame>(){
		 *
		 * @Override public Render<EntityFlame> createRenderFor(RenderManager manager) {
		 * return (Render<EntityFlame>) new RenderEntity(); } });
		 */
		RenderingRegistry.registerEntityRenderingHandler(EntityGrenade.class, RenderGrenade::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityStickybomb.class, RenderStickybomb::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityProjectileSimple.class, RenderProjectileSimple::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBall.class, RenderBall::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityFlare.class, RenderFlare::new);

		RenderingRegistry.registerEntityRenderingHandler(EntityJar.class, RenderJar::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityOnyx.class, manager -> {
			ResourceLocation texture = new ResourceLocation(TF2weapons.MOD_ID, "textures/entity/projectile/onyx.png");
			return new RenderProjectile(new ModelRocket(), texture, texture, manager);
		});

		RenderingRegistry.registerEntityRenderingHandler(EntitySentry.class, RenderSentry::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityDispenser.class, RenderDispenser::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTeleporter.class, RenderTeleporter::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityStatue.class, RenderStatue::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityHHH.class, RenderHHH::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMerasmus.class, RenderMerasmus::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMonoculus.class, RenderMonoculus::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityPickup.class, RenderPickup::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityFuryFireball.class,
				manager -> new RenderSprite<EntityFuryFireball>(manager, 1f, null) {
					@Override
					protected TextureAtlasSprite getSprite(EntityFuryFireball entity) {
						return Minecraft.getInstance().getItemRenderer().getItemModelMesher()
								.getParticleIcon(Items.FIRE_CHARGE);
					}
				});
		RenderingRegistry.registerEntityRenderingHandler(EntitySaxtonHale.class,
				manager -> new RenderBiped<EntitySaxtonHale>(manager, new ModelBiped(0.1f), 0.5F) {
					private final ResourceLocation TEXTURE = new ResourceLocation(TF2weapons.MOD_ID,
							"textures/entity/tf2/SaxtonHale.png");

					@Override
					protected ResourceLocation getEntityTexture(EntitySaxtonHale entity) {
						return TEXTURE;
					}
				});
	}

	public static void playBuildingSound(BuildingSound sound) {
		Minecraft.getInstance().getSoundManager().playSound(sound);
	}

	@Override
	public void registerTicks() {}

	public static void spawnFlameParticle(Level world, LivingEntity ent, float step, boolean heater) {
		Particle entity = EntityFlameEffect.createNewEffect(world, ent, step, heater);
		spawnParticle(world, entity);
	}

	public static void spawnBubbleParticle(Level world, Vec3 pos, Vec3 direction) {
		Particle entity = EntityBubbleEffect.createNewEffect(world, pos, direction);
		spawnParticle(world, entity);
	}

	public static void spawnBulletParticle(Level world, LivingEntity living, double startX, double startY,
			double startZ, double endX, double endY, double endZ, int j, int crits, int type, float length) {
		Particle entity = new EntityBulletTracer(world, startX, startY, startZ, endX, endY, endZ, j, crits, living,
				type, length);
		spawnParticle(world, entity);
	}

	public static void spawnCritParticle(Level world, double pX, double pY, double pZ, int teamForDisplay) {
		Particle entity = new EntityCritEffect(world, pX, pY, pZ, teamForDisplay);
		spawnParticle(world, entity);
	}

	public static void spawnBisonParticle(Level world, double pX, double pY, double pZ, int teamForDisplay) {
		Particle entity = new EntityBisonEffect(world, pX, pY, pZ, teamForDisplay);
		spawnParticle(world, entity);
	}

	public static void spawnGasSmokeParticle(Level world, double pX, double pY, double pZ, int teamForDisplay) {
		Particle entity = new ParticleGasSmoke(world, pX, pY, pZ, teamForDisplay);
		spawnParticle(world, entity);
	}

	public static void spawnBulletHoleParticle(Level world, HitResult origin) {
		Particle entity = new ParticleBulletHole(world, origin);
		spawnParticle(world, entity);
	}

	public static void spawnFlashParticle(Level world, LivingEntity ent, InteractionHand hand) {
		Particle entity = new EntityMuzzleFlash(world, ent, hand);
		spawnParticle(world, entity);
	}

	public static void spawnParticle(Level world, Particle entity) {
		if (Minecraft.getInstance() != null && Minecraft.getInstance().getCameraEntity() != null
				&& Minecraft.getInstance().effectRenderer != null) {
			int i = Minecraft.getInstance().options.particleSetting;

			if (i == 1 && world.random.nextInt(3) == 0)
				i = 2;
			if (i > 1) {
				entity.setExpired();
				return;
			}
			Minecraft.getInstance().effectRenderer.addEffect(entity);
		}
	}

	@Override
	public void playReloadSound(LivingEntity player, ItemStack stack) {
		if (!Thread.currentThread().getName().equals("Client thread") || !(stack.getItem() instanceof ItemUsable))
			return;
		// ResourceLocation soundName=new
		// ResourceLocation(ItemUsable.getData(stack).get("Reload
		// Sound").getString());
		SoundEvent event = ItemFromData.getSound(stack, PropertyType.RELOAD_SOUND);
		if (event != null) {
			ReloadSound sound = new ReloadSound(event, player);
			if (ClientProxy.reloadSounds.get(player) != null)
				ClientProxy.reloadSounds.get(player).done = true;
			ClientProxy.reloadSounds.put(player, sound);
			Minecraft.getInstance().getSoundManager().playSound(sound);
		}
	}

	public static WeaponSound playWeaponSound(LivingEntity living, SoundEvent playSound, boolean loop, int type,
			ItemStack stack) {
		// System.out.println(sound.type);\
		if (playSound == null)
			return null;

		WeaponSound sound;
		if (loop) {
			sound = new WeaponLoopSound(playSound, living, type < 2, ItemFromData.getData(stack), type == 1, type);
		} else
			sound = new WeaponSound(playSound, living, type, ItemFromData.getData(stack));
		if (fireSounds.get(living) != null)
			// Minecraft.getMinecraft().getSoundHandler().stopSound(fireSounds.get(living));
			fireSounds.get(living).setDone();
		/*
		 * if(Thread.currentThread().getName().equals("Client thread")){
		 * Minecraft.getMinecraft().getSoundHandler().playSound(sound); } else{
		 */
		Minecraft.getInstance().getSoundManager().playSound(sound);
		// }
		fireSounds.put(living, sound);
		return sound;
	}

	public static void removeReloadSound(LivingEntity entity) {
		if (reloadSounds.get(entity) != null)
			reloadSounds.remove(entity).done = true;
	}

	public static void playOnFireSound(Entity target, SoundEvent playSound) {
		if (!Thread.currentThread().getName().equals("Client thread") || playSound == null)
			return;
		Minecraft.getInstance().getSoundManager().playSound(new OnFireSound(playSound, target));
	}

	public static void spawnRocketParticle(Level world, EntityRocket rocket) {
		spawnParticle(world, new EntityRocketEffect(world, rocket));
	}

	public static class RenderCustomModel extends RenderLivingBase<LivingEntity> {

		private ResourceLocation texture;

		public RenderCustomModel(EntityRenderDispatcher renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
			super(renderManagerIn, modelBaseIn, shadowSizeIn);
		}

		@Override
		protected ResourceLocation getEntityTexture(LivingEntity entity) {
			return texture;
		}

		public void setRenderOptions(ModelBase model, ResourceLocation texture) {
			this.mainModel = model;
			this.texture = texture;
		}

		@Override
		protected boolean canRenderName(LivingEntity entity) {
			return false;
		}
	}

	public static Player getLocalPlayer() {
		return Minecraft.getInstance().player;
	}

	public static void showGuiTeleporter(EntityTeleporter entityTeleporter) {
		Minecraft.getInstance().setScreen(new GuiTeleporter(entityTeleporter));
	}

	public static void displayScreenConfirm(String str1, String str2) {
		Minecraft.getInstance().setScreen(new GuiConfirm(str1, str2));
	}

	public static void displayScreenJoinTeam() {
		final Screen prevScreen = Minecraft.getInstance().screen;
		Minecraft.getInstance().setScreen(new GuiYesNo((result, id) -> {
			if (result)
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(16));
			else
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(17));
			Minecraft.getInstance().setScreen(prevScreen);
		}, "Choose your team", "Before using the store, you need to join a team", "RED", "BLU", 0));
	}

	public static void doChargeTick(LivingEntity player) {
		if (player == Minecraft.getInstance().player) {
			if (player.getEffect(TF2weapons.charging) != null
					&& !(Minecraft.getInstance().player.movementInput instanceof MovementInputCharging)) {
				player.getCapability(TF2weapons.PLAYER_CAP,
						null).lastMovementInput = Minecraft.getInstance().player.movementInput;
				Minecraft.getInstance().player.movementInput = new MovementInputCharging();
				KeyMapping.setKeyBindState(Minecraft.getInstance().options.keyBindSprint.getKeyCode(), true);
				Minecraft.getInstance().options.mouseSensitivity *= 0.1f;

			} else if (player.getEffect(TF2weapons.charging) == null
					&& player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).lastMovementInput != null) {
				Minecraft.getInstance().player.movementInput = player.getCapability(TF2weapons.PLAYER_CAP,
						null).lastMovementInput;
				player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).lastMovementInput = null;
				Minecraft.getInstance().options.mouseSensitivity *= 10f;
			}
			// Minecraft.getMinecraft().player.movementInput.moveForward = 1f;
		}
	}

	public static class MovementInputCharging extends Input {
		public MovementInputCharging() {
			this.moveStrafe = 0.0F;
			this.zza = 1.0F;
		}

	}

	public static void showGuiSentry(EntitySentry entitySentry) {
		Minecraft.getInstance().setScreen(new GuiSentry(entitySentry));
	}

	public static void showGuiDisguise() {
		Minecraft.getInstance().setScreen(new GuiDisguiseKit());
	}

	public static void removeSprint() {
		KeyMapping.setKeyBindState(Minecraft.getInstance().options.keyBindSprint.getKeyCode(), false);
	}

	public static void setColor(int color, float alpha, float darken, float min, float max) {
		GlStateManager.color(Mth.clamp((color >> 16) / 255f + darken, min, max),
				Mth.clamp((color >> 8 & 255) / 255f + darken, min, max),
				Mth.clamp((color & 255) / 255f + darken, min, max), alpha);
	}

	@Override
	public void displayCorruptedFileError() {
		throw new CorruptedFileException();
	}

	public static class CorruptedFileException extends CustomModLoadingErrorDisplayException {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void initGui(GuiErrorScreen errorScreen, Font fontRenderer) {}

		@Override
		public void drawScreen(GuiErrorScreen errorScreen, Font fontRenderer, int mouseRelX, int mouseRelY,
				float tickTime) {
			errorScreen.drawCenteredString(fontRenderer, "TF2 Stuff Mod", errorScreen.getBbWidth() / 2, 70, 16777215);
			errorScreen.drawCenteredString(fontRenderer, "Failed to copy weapon files. Restart the game and try again",
					errorScreen.getBbWidth() / 2, 90, 16777215);

		}

	}
}
