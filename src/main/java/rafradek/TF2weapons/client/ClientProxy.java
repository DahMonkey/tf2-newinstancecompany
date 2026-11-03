package rafradek.TF2weapons.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
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
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.*;
import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.IParticleFactory;
import rafradek.TF2weapons.client.compat.LegacyRender;
import rafradek.TF2weapons.client.compat.ModelBase;
import rafradek.TF2weapons.client.compat.ModelBiped;
import rafradek.TF2weapons.client.compat.ModelLoader;
import rafradek.TF2weapons.client.compat.RenderBiped;
import rafradek.TF2weapons.client.compat.RenderLivingBase;
import rafradek.TF2weapons.client.gui.GuiConfirm;
import rafradek.TF2weapons.client.gui.GuiDisguiseKit;
import rafradek.TF2weapons.client.gui.inventory.*;
import rafradek.TF2weapons.client.model.ModelRocket;
import rafradek.TF2weapons.client.particle.*;
import rafradek.TF2weapons.client.renderer.entity.*;
import rafradek.TF2weapons.common.CommonProxy;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.entity.mercenary.EntitySaxtonHale;
import rafradek.TF2weapons.entity.mercenary.InvasionEvent;
import rafradek.TF2weapons.entity.projectile.EntityFuryFireball;
import rafradek.TF2weapons.entity.projectile.EntityRocket;
import rafradek.TF2weapons.inventory.*;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.message.MessageContext;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.WeaponData;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Client side of the mod: renderers, item models, key bindings, colours, screens, sounds and particles. */
public class ClientProxy extends CommonProxy {

	public static HashMap<String, ModelBase> entityModel = new HashMap<>();
	public static HashMap<String, ResourceLocation> textureDisguise = new HashMap<>();

	public static RenderCustomModel disguiseRender;
	public static net.minecraft.client.renderer.entity.player.PlayerRenderer disguiseRenderPlayer;
	public static net.minecraft.client.renderer.entity.player.PlayerRenderer disguiseRenderPlayerSmall;
	public static net.minecraft.client.renderer.entity.player.PlayerRenderer forceTextureRenderPlayer;
	public static KeyMapping reload = new KeyMapping("key.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "TF2");
	public static KeyMapping backpackitem = new KeyMapping("key.activatebackpack", InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_0, "TF2");
	public static ResourceLocation scopeTexture = new ResourceLocation(TF2weapons.MOD_ID, "textures/misc/scope.png");
	public static BiMap<LivingEntity, WeaponSound> fireSounds = HashBiMap.create();
	public static Map<LivingEntity, ReloadSound> reloadSounds = new HashMap<>();
	public static Map<String, ModelResourceLocation> nameToModel = new HashMap<>();
	public static ConcurrentMap<LivingEntity, ItemStack> soundsToStart = new ConcurrentHashMap<>();
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
	public static List<WeaponSound> weaponSoundsToStart = new ArrayList<>();
	public static int renderCritGlow;
	public static boolean inRenderHand;
	public static boolean inRenderHandTicked;
	public static boolean buildingsUseEnergy;

	public static EnumMap<EnumTF2Particles, IParticleFactory> particleFactories = new EnumMap<>(EnumTF2Particles.class);
	public static final Logger LOGGER = LogManager.getLogger();
	public static Set<Class<? extends Block>> interactingBlocks = new HashSet<>();

	private static double mouseSensitivityBeforeCharge = -1;
	private static double mouseSensitivityBeforeScope = -1;

	@Override
	public void construct(IEventBus modBus) {
		modBus.addListener(this::clientSetup);
		modBus.addListener(this::registerRenderers);
		modBus.addListener(this::registerKeys);
		modBus.addListener(this::registerItemColors);
		modBus.addListener((ModelEvent.RegisterAdditional event) -> {
			registerItemModels();
			ModelLoader.onRegisterAdditional(event);
		});
		modBus.addListener(ModelLoader::onBakingCompleted);
		modBus.addListener((net.minecraftforge.client.event.EntityRenderersEvent.AddLayers event) -> {
			Minecraft mc = Minecraft.getInstance();
			net.minecraft.client.renderer.entity.EntityRendererProvider.Context context = new net.minecraft.client.renderer.entity.EntityRendererProvider.Context(
					mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderer(),
					mc.getEntityRenderDispatcher().getItemInHandRenderer(), mc.getResourceManager(),
					event.getEntityModels(), mc.font);
			disguiseRenderPlayer = new rafradek.TF2weapons.client.renderer.entity.RenderPlayerDisguised(
					context, false);
			disguiseRenderPlayerSmall = new rafradek.TF2weapons.client.renderer.entity.RenderPlayerDisguised(
					context, true);
			forceTextureRenderPlayer = new rafradek.TF2weapons.client.renderer.entity.RenderPlayerForceTexture(
					context, false);
		});
		modBus.addListener(TF2EventsClient::registerIcons);
		modBus.addListener(TF2EventsClient::iconsStitched);
		MinecraftForge.EVENT_BUS.register(new TF2EventsClient());
		MinecraftForge.EVENT_BUS.register(PyrolandRenderer.INSTANCE);
	}

	private void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			registerScreens();
			net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(TF2weapons.blockUpgradeStation,
					net.minecraft.client.renderer.RenderType.cutout());
			for (LegacyItem.PropertyOverride override : LegacyItem.PROPERTY_OVERRIDES)
				ItemProperties.register(override.item(), override.name(),
						(stack, level, entity, seed) -> override.getter().get(stack, level, entity));
			findInteractingBlocks();
		});
		particleFactories.put(EnumTF2Particles.EXPLOSION, new ParticleExplosion.Factory());
		particleFactories.put(EnumTF2Particles.BULLET_TRACER, new EntityBulletTracer.Factory());
	}

	@SuppressWarnings("unchecked")
	private static <M extends AbstractContainerMenu> MenuType<M> menu(int id) {
		return (MenuType<M>) TF2weapons.MENU_TYPES[id];
	}

	private interface ScreenFactory {
		Screen create(AbstractContainerMenu menu, net.minecraft.world.entity.player.Inventory inv, Component title);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void screen(int id, ScreenFactory factory) {
		MenuScreens.register((MenuType) TF2weapons.MENU_TYPES[id],
				(MenuScreens.ScreenConstructor) (m, inv, t) -> factory.create((AbstractContainerMenu) m, inv, t));
	}

	private static void registerScreens() {
		screen(0, (m, inv, t) -> new GuiWearables((ContainerWearables) m));
		screen(1, (m, inv, t) -> new GuiTF2Crafting((ContainerTF2Workbench) m));
		screen(2, (m, inv, t) -> new GuiUpgradeStation((ContainerUpgrades) m));
		screen(3, (m, inv, t) -> new GuiAmmoFurnace((ContainerAmmoFurnace) m));
		screen(4, (m, inv, t) -> new rafradek.TF2weapons.client.gui.inventory.GuiMercenary(
				(rafradek.TF2weapons.inventory.ContainerMercenary) m, inv, t));
		screen(5, (m, inv, t) -> GuiBuildingFactory.create(m));
	}

	/** Remembers which block classes react to right-click, as the 1.12 code did with reflection. */
	private static void findInteractingBlocks() {
		Method useMethod = null;
		for (Method method : Block.class.getMethods())
			if (method.getName().equals("use") || method.getName().equals("m_6227_"))
				useMethod = method;
		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			try {
				if (!interactingBlocks.contains(block.getClass()))
					for (Method method : block.getClass().getMethods())
						if ((method.getName().equals("use") || method.getName().equals("m_6227_"))
								&& !method.equals(useMethod)
								&& method.getDeclaringClass() != net.minecraft.world.level.block.state.BlockBehaviour.class) {
							interactingBlocks.add(block.getClass());
							break;
						}
			} catch (NoClassDefFoundError err) {
			}
		}
	}

	private void registerKeys(RegisterKeyMappingsEvent event) {
		event.register(reload);
		event.register(backpackitem);
	}

	private void registerItemColors(RegisterColorHandlersEvent.Item event) {
		event.register((stack, tintIndex) -> {
			int meta = stack.getDamageValue();
			if (meta < 25)
				return meta < 9 ? 16711680 : 255;
			else if (meta / 2 == 13)
				return 0xFFD400;
			else if (meta == 28)
				return 0x743501;
			else if (meta == 29)
				return 0x1B013A;
			else if (meta == 30)
				return 0x20582B;
			else if (meta < 45)
				return 0x2AAAFF;
			return meta % 2 == 0 ? 16711680 : 255;
		}, TF2weapons.itemPlacer);

		List<Item> items = new ArrayList<>();
		for (Item item : ForgeRegistries.ITEMS.getValues())
			if (item instanceof ItemFromData || item instanceof DiggerItem || item instanceof SwordItem
					|| item instanceof BowItem || item == TF2weapons.itemTF2)
				items.add(item);
		event.register((stack, tintIndex) -> {
			if (ItemFromData.getData(stack).hasProperty(PropertyType.COLOR))
				return ItemFromData.getData(stack).getInt(PropertyType.COLOR);
			if (renderCritGlow > 15) {
				int color = rafradek.TF2weapons.util.TF2Util.colorCode[renderCritGlow % 16];
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
				return rafradek.TF2weapons.util.TF2Util.colorCode[renderCritGlow % 16];
			return 0xFFFFFF;
		}, items.toArray(new Item[0]));
	}

	private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(TF2weapons.HEAVY, RenderTF2Character::new);
		event.registerEntityRenderer(TF2weapons.SCOUT, RenderTF2Character::new);
		event.registerEntityRenderer(TF2weapons.SNIPER, RenderTF2Character::new);
		event.registerEntityRenderer(TF2weapons.SOLDIER, RenderTF2Character::new);
		event.registerEntityRenderer(TF2weapons.PYRO, RenderTF2Character::new);
		event.registerEntityRenderer(TF2weapons.DEMOMAN, RenderTF2Character::new);
		event.registerEntityRenderer(TF2weapons.MEDIC, RenderTF2Character::new);
		event.registerEntityRenderer(TF2weapons.SPY, RenderTF2Character::new);
		event.registerEntityRenderer(TF2weapons.ENGINEER, RenderTF2Character::new);
		event.registerEntityRenderer(TF2weapons.ROCKET, RenderRocket::new);
		event.registerEntityRenderer(TF2weapons.FLAME, NoopRenderer::new);
		event.registerEntityRenderer(TF2weapons.GRENADE, RenderGrenade::new);
		event.registerEntityRenderer(TF2weapons.STICKYBOMB, RenderStickybomb::new);
		event.registerEntityRenderer(TF2weapons.PROJECTILE_SIMPLE, RenderProjectileSimple::new);
		event.registerEntityRenderer(TF2weapons.SYRINGE, RenderProjectileSimple::new);
		event.registerEntityRenderer(TF2weapons.CLEAVER, RenderProjectileSimple::new);
		event.registerEntityRenderer(TF2weapons.PROJECTILE_ENERGY, RenderProjectileSimple::new);
		event.registerEntityRenderer(TF2weapons.SHORT_CIRCUIT_ORB, RenderProjectileSimple::new);
		event.registerEntityRenderer(TF2weapons.GRAPPLING_HOOK, RenderProjectileSimple::new);
		event.registerEntityRenderer(TF2weapons.BALL, RenderBall::new);
		event.registerEntityRenderer(TF2weapons.FLARE, RenderFlare::new);
		event.registerEntityRenderer(TF2weapons.JAR, RenderJar::new);
		event.registerEntityRenderer(TF2weapons.ONYX, context -> {
			ResourceLocation texture = new ResourceLocation(TF2weapons.MOD_ID, "textures/entity/projectile/onyx.png");
			return new RenderProjectile(new ModelRocket(), texture, texture, context);
		});
		event.registerEntityRenderer(TF2weapons.SENTRY, RenderSentry::new);
		event.registerEntityRenderer(TF2weapons.DISPENSER, RenderDispenser::new);
		event.registerEntityRenderer(TF2weapons.TELEPORTER, RenderTeleporter::new);
		event.registerEntityRenderer(TF2weapons.STATUE, RenderStatue::new);
		event.registerEntityRenderer(TF2weapons.HHH, RenderHHH::new);
		event.registerEntityRenderer(TF2weapons.MERASMUS, RenderMerasmus::new);
		event.registerEntityRenderer(TF2weapons.MONOCULUS, RenderMonoculus::new);
		event.registerEntityRenderer(TF2weapons.PICKUP, RenderPickup::new);
		event.registerEntityRenderer(TF2weapons.TARGET, ArmorStandRenderer::new);
		event.registerEntityRenderer(TF2weapons.DUMMY, NoopRenderer::new);
		event.registerEntityRenderer(TF2weapons.FURY_FIREBALL,
				context -> new RenderSprite<EntityFuryFireball>(context, 1f, null) {
					@Override
					protected TextureAtlasSprite getSprite(EntityFuryFireball entity) {
						return Minecraft.getInstance().getItemRenderer().getItemModelShaper()
								.getItemModel(Items.FIRE_CHARGE).getParticleIcon();
					}
				});
		event.registerEntityRenderer(TF2weapons.HALE,
				context -> new RenderBiped<EntitySaxtonHale>(context, new ModelBiped(0.1f), 0.5F) {
					private final ResourceLocation TEXTURE = new ResourceLocation(TF2weapons.MOD_ID,
							"textures/entity/tf2/saxtonhale.png");

					@Override
					protected ResourceLocation getEntityTexture(EntitySaxtonHale entity) {
						return TEXTURE;
					}
				});
	}

	public static void RegisterWeaponData(WeaponData weapon) {
		Item item = MapList.weaponClasses.get(weapon.getString(PropertyType.CLASS));
		if (item instanceof ItemFromData)
			((ItemFromData) item).registerModels(weapon);
	}

	private static ModelResourceLocation model(String name) {
		return new ModelResourceLocation(TF2weapons.MOD_ID + ":" + name, "inventory");
	}

	/** Item models; the same registrations as the 1.12 preInit. */
	private static void registerItemModels() {
		for (int i = 1; i < ItemAmmo.AMMO_TYPES.length; i++)
			if (i != 10 && i != 12)
				ModelLoader.setCustomModelResourceLocation(TF2weapons.itemAmmo, i, model("ammo_" + ItemAmmo.AMMO_TYPES[i]));

		nameToModel.clear();
		for (WeaponData weapon : MapList.nameToData.values())
			RegisterWeaponData(weapon);
		for (Item item : MapList.weaponClasses.values())
			ModelLoader.setCustomMeshDefinition(item, ((ItemFromData) item).getMeshDefinition());

		ModelResourceLocation spawnEgg = new ModelResourceLocation("minecraft:template_spawn_egg", "inventory");
		ModelLoader.registerItemVariants(TF2weapons.itemPlacer, spawnEgg);
		ModelLoader.setCustomMeshDefinition(TF2weapons.itemPlacer, stack -> spawnEgg);

		ModelLoader.registerItemVariants(TF2weapons.itemBuildingBox, model("sentryred"), model("sentryblu"),
				model("dispenserred"), model("dispenserblu"), model("teleporterred"), model("teleporterblu"));
		ModelLoader.setCustomMeshDefinition(TF2weapons.itemBuildingBox, stack -> switch (stack.getDamageValue()) {
		case 18 -> model("sentryred");
		case 19 -> model("sentryblu");
		case 20 -> model("dispenserred");
		case 21 -> model("dispenserblu");
		case 22 -> model("teleporterred");
		default -> model("teleporterblu");
		});
		ModelLoader.registerItemVariants(TF2weapons.itemKillstreak, model("killstreak_kit"),
				model("killstreak_kit_specialized"), model("killstreak_kit_professional"));
		ModelLoader.setCustomMeshDefinition(TF2weapons.itemKillstreak,
				stack -> switch (((ItemKillstreakKit) stack.getItem()).getLevel(stack)) {
				case 2 -> model("killstreak_kit_specialized");
				case 3 -> model("killstreak_kit_professional");
				default -> model("killstreak_kit");
				});
		ModelLoader.registerItemVariants(TF2weapons.itemAmmoPackage, model("ammo_box"), model("ammo_box_small"));
		ModelLoader.setCustomMeshDefinition(TF2weapons.itemAmmoPackage,
				stack -> stack.getDamageValue() / 16 == 0 ? model("ammo_box_small") : model("ammo_box"));
		ModelLoader.registerItemVariants(TF2weapons.itemKillstreakFabricator, model("killstreak_fabricator"),
				model("killstreak_fabricator_spec"), model("killstreak_fabricator_pro"));
		ModelLoader.setCustomMeshDefinition(TF2weapons.itemKillstreakFabricator,
				stack -> switch (((ItemKillstreakFabricator) stack.getItem()).getLevel(stack)) {
				case 2 -> model("killstreak_fabricator_spec");
				case 3 -> model("killstreak_fabricator_pro");
				default -> model("killstreak_fabricator");
				});
		for (int i = 0; i < ItemRobotPart.LEVEL.length; i++)
			ModelLoader.setCustomModelResourceLocation(TF2weapons.itemRobotPart, i, model("robot_part_" + i));
		for (int i = 0; i < InvasionEvent.DIFFICULTY.length; i++)
			ModelLoader.setCustomModelResourceLocation(TF2weapons.itemEventMaker, i, model("tour_ticket"));

		String[] tf2Items = { "copper_ingot", "lead_ingot", "australium_ingot", "scrap_metal", "reclaimed_metal",
				"refined_metal", "australium_nugget", "key", "crate", "random_weapon", "random_hat", "logic_board",
				"robot_part_pack", "robot_part_pack" };
		for (int i = 0; i < tf2Items.length; i++)
			ModelLoader.setCustomModelResourceLocation(TF2weapons.itemTF2, i, model(tf2Items[i]));
		String[] tokens = { "scout", "soldier", "pyro", "demoman", "heavy", "engineer", "medic", "sniper", "spy" };
		for (int i = 0; i < tokens.length; i++)
			ModelLoader.setCustomModelResourceLocation(TF2weapons.itemToken, i, model("token_" + tokens[i]));
		String[] bosses = { "hhh", "monoculus", "merasmus" };
		for (int i = 0; i < bosses.length; i++)
			ModelLoader.setCustomModelResourceLocation(TF2weapons.itemBossSpawn, i, model("boss_" + bosses[i]));
		String[] money = { "small", "normal", "big" };
		for (int i = 0; i < money.length; i++)
			ModelLoader.setCustomModelResourceLocation(TF2weapons.itemMoney, i, model("money_" + money[i]));

		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemAmmoPistol, 0, model("pistol_mag"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemAmmoSMG, 0, model("smg_mag"));
		ModelLoader.setCustomModelResourceLocation(TF2weapons.itemKillstreak, 0, model("killstreak_kit"));
	}

	/* ------------------------------------------------------------------ */
	/* Proxy methods */
	/* ------------------------------------------------------------------ */

	@Override
	public Player getPlayerForSide(MessageContext ctx) {
		return ctx != null && ctx.getSender() != null ? ctx.getSender() : Minecraft.getInstance().player;
	}

	@Override
	public void sendMovementInput(Player player) {
		if (player == Minecraft.getInstance().player) {
			net.minecraft.client.player.LocalPlayer local = (net.minecraft.client.player.LocalPlayer) player;
			local.connection.send(new net.minecraft.network.protocol.game.ServerboundPlayerInputPacket(local.xxa,
					local.zza, local.input.jumping, local.input.shiftKeyDown));
		}
	}

	@Override
	public void updateSniperMouseSlow(WeaponsCapability cap) {
		if (cap.owner != Minecraft.getInstance().player)
			return;
		var sensitivity = Minecraft.getInstance().options.sensitivity();
		boolean scoped = cap.owner.getMainHandItem().getItem() instanceof ItemSniperRifle && cap.isCharging();
		if (scoped && !cap.appliedMouseSlow) {
			mouseSensitivityBeforeScope = sensitivity.get();
			sensitivity.set(sensitivity.get() * 0.4);
			cap.appliedMouseSlow = true;
		} else if (!cap.isCharging() && cap.appliedMouseSlow) {
			sensitivity.set(mouseSensitivityBeforeScope >= 0 ? mouseSensitivityBeforeScope : sensitivity.get() * 2.5);
			cap.appliedMouseSlow = false;
		}
	}

	@Override
	public void loadDisguiseSkin(WeaponsCapability cap, String playerName) {
		cap.skinType = net.minecraft.client.resources.DefaultPlayerSkin.getSkinModelName(cap.owner.getUUID());
		net.minecraft.world.level.block.entity.SkullBlockEntity.updateGameprofile(
				new com.mojang.authlib.GameProfile(null, playerName), profile -> {
					if (profile.getId() != null) {
						cap.skinType = net.minecraft.client.resources.DefaultPlayerSkin.getSkinModelName(profile.getId());
						cap.skinDisguise = net.minecraft.client.resources.DefaultPlayerSkin.getDefaultSkin(profile.getId());
					}
					Minecraft.getInstance().getSkinManager().registerSkins(profile, (type, location, texture) -> {
						if (type == com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN) {
							cap.skinDisguise = location;
							String model = texture.getMetadata("model");
							cap.skinType = model == null ? "default" : model;
						}
					}, false);
				});
	}

	@Override
	public void playReloadSound(LivingEntity player, ItemStack stack) {
		if (!Minecraft.getInstance().isSameThread() || !(stack.getItem() instanceof ItemUsable))
			return;
		SoundEvent event = ItemFromData.getSound(stack, PropertyType.RELOAD_SOUND);
		if (event != null) {
			ReloadSound sound = new ReloadSound(event, player);
			if (ClientProxy.reloadSounds.get(player) != null)
				ClientProxy.reloadSounds.get(player).done = true;
			ClientProxy.reloadSounds.put(player, sound);
			Minecraft.getInstance().getSoundManager().play(sound);
		}
	}

	@Override
	public void displayCorruptedFileError() {
		LOGGER.error("TF2 Stuff Mod: failed to copy weapon files. Restart the game and try again");
	}

	/* ------------------------------------------------------------------ */
	/* Client helpers used across the mod */
	/* ------------------------------------------------------------------ */

	public static void playBuildingSound(BuildingSound sound) {
		Minecraft.getInstance().getSoundManager().play(sound);
	}

	public static void spawnFlameParticle(Level world, LivingEntity ent, float step, boolean heater) {
		spawnParticle(world, EntityFlameEffect.createNewEffect(world, ent, step, heater));
	}

	public static void spawnBubbleParticle(Level world, Vec3 pos, Vec3 direction) {
		spawnParticle(world, EntityBubbleEffect.createNewEffect(world, pos, direction));
	}

	public static void spawnBulletParticle(Level world, LivingEntity living, double startX, double startY,
			double startZ, double endX, double endY, double endZ, int j, int crits, int type, float length) {
		spawnParticle(world, new EntityBulletTracer(world, startX, startY, startZ, endX, endY, endZ, j, crits, living,
				type, length));
	}

	public static void spawnCritParticle(Level world, double pX, double pY, double pZ, int teamForDisplay) {
		spawnParticle(world, new EntityCritEffect(world, pX, pY, pZ, teamForDisplay));
	}

	public static void spawnBisonParticle(Level world, double pX, double pY, double pZ, int teamForDisplay) {
		spawnParticle(world, new EntityBisonEffect(world, pX, pY, pZ, teamForDisplay));
	}

	public static void spawnGasSmokeParticle(Level world, double pX, double pY, double pZ, int teamForDisplay) {
		spawnParticle(world, new ParticleGasSmoke(world, pX, pY, pZ, teamForDisplay));
	}

	public static void spawnBulletHoleParticle(Level world, HitResult origin) {
		spawnParticle(world, new ParticleBulletHole(world, origin));
	}

	public static void spawnFlashParticle(Level world, LivingEntity ent, InteractionHand hand) {
		spawnParticle(world, new EntityMuzzleFlash(world, ent, hand));
	}

	public static void spawnRocketParticle(Level world, EntityRocket rocket) {
		spawnParticle(world, new rafradek.TF2weapons.entity.projectile.EntityRocketEffect(world, rocket));
	}

	public static void spawnParticle(Level world, Particle particle) {
		Minecraft mc = Minecraft.getInstance();
		if (particle == null || mc.getCameraEntity() == null || mc.particleEngine == null)
			return;
		ParticleStatus status = mc.options.particles().get();
		if (status == ParticleStatus.DECREASED && world.random.nextInt(3) == 0)
			status = ParticleStatus.MINIMAL;
		if (status == ParticleStatus.MINIMAL) {
			particle.remove();
			return;
		}
		mc.particleEngine.add(particle);
	}

	public static WeaponSound playWeaponSound(LivingEntity living, SoundEvent playSound, boolean loop, int type,
			ItemStack stack) {
		if (playSound == null)
			return null;
		WeaponSound sound;
		if (loop)
			sound = new WeaponLoopSound(playSound, living, type < 2, ItemFromData.getData(stack), type == 1, type);
		else
			sound = new WeaponSound(playSound, living, type, ItemFromData.getData(stack));
		if (fireSounds.get(living) != null)
			fireSounds.get(living).setDone();
		Minecraft.getInstance().getSoundManager().play(sound);
		fireSounds.put(living, sound);
		return sound;
	}

	public static void removeReloadSound(LivingEntity entity) {
		if (reloadSounds.get(entity) != null)
			reloadSounds.remove(entity).done = true;
	}

	public static void playOnFireSound(Entity target, SoundEvent playSound) {
		if (!Minecraft.getInstance().isSameThread() || playSound == null)
			return;
		Minecraft.getInstance().getSoundManager().play(new OnFireSound(playSound, target));
	}

	/** The client world, typed as Level so common code never names ClientLevel. */
	public static net.minecraft.world.level.Level getClientLevel() {
		return Minecraft.getInstance().level;
	}

	public static Player getLocalPlayer() {
		return Minecraft.getInstance().player;
	}

	public static void displayScreenConfirm(String str1, String str2) {
		Minecraft.getInstance().setScreen(new GuiConfirm(str1, str2));
	}

	public static void displayScreenJoinTeam() {
		final Screen prevScreen = Minecraft.getInstance().screen;
		Minecraft.getInstance().setScreen(new ConfirmScreen(result -> {
			TF2weapons.network.sendToServer(new TF2Message.ActionMessage(result ? 16 : 17));
			Minecraft.getInstance().setScreen(prevScreen);
		}, Component.literal("Choose your team"),
				Component.literal("Before using the store, you need to join a team"), Component.literal("RED"),
				Component.literal("BLU")));
	}

	public static void doChargeTick(LivingEntity player) {
		Minecraft mc = Minecraft.getInstance();
		if (player != mc.player)
			return;
		rafradek.TF2weapons.TF2PlayerCapability cap = rafradek.TF2weapons.TF2PlayerCapability.get(mc.player);
		var sensitivity = mc.options.sensitivity();
		if (player.getEffect(TF2weapons.charging) != null && !(mc.player.input instanceof MovementInputCharging)) {
			cap.lastMovementInput = mc.player.input;
			mc.player.input = new MovementInputCharging();
			KeyMapping.set(mc.options.keySprint.getKey(), true);
			mouseSensitivityBeforeCharge = sensitivity.get();
			sensitivity.set(sensitivity.get() * 0.1);
		} else if (player.getEffect(TF2weapons.charging) == null && cap.lastMovementInput != null) {
			mc.player.input = cap.lastMovementInput;
			cap.lastMovementInput = null;
			sensitivity.set(mouseSensitivityBeforeCharge >= 0 ? mouseSensitivityBeforeCharge : sensitivity.get() * 10);
		}
	}

	/** Movement input used while charging with a shield: always forward. */
	public static class MovementInputCharging extends Input {
		public MovementInputCharging() {
			this.leftImpulse = 0.0F;
			this.forwardImpulse = 1.0F;
		}

		@Override
		public void tick(boolean slow, float slowFactor) {
			this.leftImpulse = 0.0F;
			this.forwardImpulse = 1.0F;
		}
	}

	public static void showGuiDisguise() {
		Minecraft.getInstance().setScreen(new GuiDisguiseKit());
	}

	public static void removeSprint() {
		KeyMapping.set(Minecraft.getInstance().options.keySprint.getKey(), false);
	}

	public static void setColor(int color, float alpha, float darken, float min, float max) {
		GlStateManager.color(Mth.clamp((color >> 16) / 255f + darken, min, max),
				Mth.clamp((color >> 8 & 255) / 255f + darken, min, max), Mth.clamp((color & 255) / 255f + darken, min, max),
				alpha);
	}

	/** Renders an arbitrary legacy model with a texture (used for disguises). */
	public static class RenderCustomModel extends RenderLivingBase<LivingEntity> {
		private ResourceLocation texture;

		public RenderCustomModel(EntityRendererProvider.Context context, ModelBase model, float shadowSize) {
			super(context, model, shadowSize);
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
		protected boolean shouldShowName(LivingEntity entity) {
			return false;
		}
	}
}
