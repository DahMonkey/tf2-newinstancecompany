package rafradek.TF2weapons.client;

import rafradek.TF2weapons.client.compat.RenderLivingBase;

import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;
import rafradek.TF2weapons.client.compat.DefaultVertexFormats;

import rafradek.TF2weapons.client.compat.ModelBiped;
import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.GL11;
import rafradek.TF2weapons.client.compat.OpenGlHelper;
import rafradek.TF2weapons.client.compat.RenderHelper;

import org.apache.commons.io.IOUtils;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2EventsCommon;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.gui.GuiContracts;
import rafradek.TF2weapons.client.gui.inventory.GuiWearables;
import rafradek.TF2weapons.client.model.ModelBomb;
import rafradek.TF2weapons.client.renderer.entity.RenderGrenade;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.mercenary.EntitySpy;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.inventory.ContainerWearables;
import rafradek.TF2weapons.inventory.InventoryWearables;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.lightsource.MuzzleFlashLightSource;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TF2EventsClient {
	private static boolean alreadypressed;
	private static boolean alreadypressedalt;
	private static boolean alreadypressedreload;
	public static boolean moveEntities;
	public static float tickTime = 0;
	public static ArrayList<MuzzleFlashLightSource> muzzleFlashes = new ArrayList<>();
	public static TextureAtlasSprite[] pelletIcon;
	public static int ticksPressedReload;
	private static FloatBuffer brightnessBuffer = GLAllocation.createDirectFloatBuffer(4);
	private static final DynamicTexture TEXTURE_BRIGHTNESS = new DynamicTexture(16, 16);
	public static TextureAtlasSprite skinIcon;
	public static TextureAtlasSprite bisonIcon;
	private long profileStartTime = -1;
	private int profileStartTick;
	public static Vec3 armLpos;
	public static Vec3 armRpos;
	public static Vec3 armLang;
	public static Vec3 armRang;

	@SubscribeEvent
	public void registerIcons(TextureStitchEvent.Pre event) {
		// if(event.getMap().getGlTextureId()==1){
		// System.out.println("Registered icon:
		// "+event.getMap().getGlTextureId());
		pelletIcon = new TextureAtlasSprite[16];
		for (int i = 0; i < 16; i++) {
			ResourceLocation location = new ResourceLocation(TF2weapons.MOD_ID, "items/tracer" + (i + 1));
			try {
				Minecraft.getInstance().getResourceManager().getResource(
						new ResourceLocation(TF2weapons.MOD_ID, "textures/items/tracer" + (i + 1) + ".png"));
				pelletIcon[i] = event.getMap().registerSprite(location);
			} catch (IOException e) {

			}

		}
		bisonIcon = event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/bison"));

		skinIcon = event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "misc/skin"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/ammo_belt_empty"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/refill_empty"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/weapon_empty_0"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/weapon_empty_1"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/weapon_empty_2"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/token_empty"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/robot_part_1_1_empty"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/robot_part_2_1_empty"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/robot_part_3_1_empty"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/robot_part_1_2_empty"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/robot_part_2_2_empty"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/robot_part_3_2_empty"));
		event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/robot_part_1_3_empty"));

		for (int i = 0; i < ContainerWearables.CURRENCY_EMPTY.length; i++)
			event.getMap().registerSprite(new ResourceLocation(ContainerWearables.CURRENCY_EMPTY[i]));
		// }
	}

	@SubscribeEvent
	public void keyJumpPress(InputEvent.KeyInputEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen == null) {
			ItemStack stack = minecraft.player.getMainHandItem();
			if (minecraft.options.keyBindJump.consumeClick()) {
				ItemStack chest = ItemBackpack.getBackpack(minecraft.player);
				if (!minecraft.player.onGround && WeaponsCapability.get(minecraft.player).airJumps < WeaponsCapability
						.get(minecraft.player).getMaxAirJumps()) {
					minecraft.player.jump();
					float speedmult = minecraft.player.getAIMoveSpeed() * (minecraft.player.isSprinting() ? 2 : 1);
					Vec3 moveDir = TF2Util.getMovementVector(minecraft.player);
					minecraft.player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).airJumps += 1;
					minecraft.player.setDeltaMovement((moveDir.x * speedmult), minecraft.player.getDeltaMovement().y, minecraft.player.getDeltaMovement().z);
					minecraft.player.setDeltaMovement(minecraft.player.getDeltaMovement().x, minecraft.player.getDeltaMovement().y, (moveDir.y * speedmult));
					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(23));
				} else if (WeaponsCapability.get(minecraft.player).isGrappled()) {
					minecraft.player.jump();
					if (minecraft.player.getMainHandItem().getItem() instanceof ItemGrapplingHook) {
						WeaponsCapability.get(minecraft.player).setPrimaryCooldown(InteractionHand.MAIN_HAND,
								((ItemGrapplingHook) minecraft.player.getMainHandItem().getItem())
										.getFiringSpeed(minecraft.player.getMainHandItem(), minecraft.player));

					}

					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(23));
				} else if (!minecraft.player.getAbilities().instabuild && !minecraft.player.isInWater()
						&& chest.getItem() instanceof ItemJetpack
						&& TF2Attribute.getModifier("Jetpack Item", chest, 0f, minecraft.player) != 0f
						&& ((ItemJetpack) chest.getItem()).canActivate(chest, minecraft.player)) {
					// ((ItemJetpack)chest.getItem()).activateJetpack(chest, minecraft.player,
					// true);
					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(30));
				} else if (ItemToken.allowUse(minecraft.player, TF2Class.SOLDIER)
						&& chest.getItem() instanceof ItemParachute) {
					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(25));
				}
			}

			if (ClientProxy.backpackitem.consumeClick()) {
				ItemStack backpack = ItemBackpack.getBackpack(minecraft.player);
				if (!backpack.isEmpty() && !((ItemBackpack) backpack.getItem())
						.getBackpackItemToUse(backpack, minecraft.player).isEmpty()) {
					minecraft.player.connection.sendPacket(new CPacketHeldItemChange(8));
					minecraft.player.getInventory().selected = 8;
					if (!TF2PlayerCapability.get(minecraft.player).isBackpackItemEquipped()) {
						TF2PlayerCapability.get(minecraft.player).setEquipBackpackItem(true);
						TF2weapons.network.sendToServer(new TF2Message.ActionMessage(26));
					}
				}
			}
			if (TF2PlayerCapability.get(minecraft.player).isBackpackItemEquipped()) {
				int sel = this.getPressedHotbarKey(minecraft.options.keyBindsHotbar, false);
				// KeyBinding.setKeyBindState(Keyboard.getEventKey() == 0 ?
				// Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey(), false);
				if (sel != -1) {
					TF2PlayerCapability.get(minecraft.player).setEquipBackpackItem(false);
					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(27));
				}
			}

			if (stack.getItem() instanceof IItemSlotNumber
					&& ((IItemSlotNumber) stack.getItem()).catchSlotHotkey(stack, minecraft.player)) {
				int sel = this.getPressedHotbarKey(minecraft.options.keyBindsHotbar, true);
				// KeyBinding.setKeyBindState(Keyboard.getEventKey() == 0 ?
				// Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey(), false);
				if (sel != -1) {
					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(sel + 100));
					((IItemSlotNumber) stack.getItem()).onSlotSelection(stack, minecraft.player, sel);
				}
			} else if (ClientProxy.reload.isDown()) {
				int sel = this.getPressedHotbarKey(minecraft.options.keyBindsHotbar, true);
				// KeyBinding.setKeyBindState(Keyboard.getEventKey() == 0 ?
				// Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey(), false);
				if (sel != -1)
					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(sel + 110));
			} else if (stack.getItem() instanceof IItemNoSwitch
					&& ((IItemNoSwitch) stack.getItem()).stopSlotSwitch(stack, minecraft.player))
				this.getPressedHotbarKey(minecraft.options.keyBindsHotbar, true);

			/*
			 * if (minecraft.currentScreen == null &&
			 * minecraft.gameSettings.keyBindPickBlock.isKeyDown() &&
			 * minecraft.player.getHeldItemMainhand().getItem() instanceof ItemWeapon &&
			 * TF2Attribute.getModifier("Knockback Rage",
			 * minecraft.player.getHeldItemMainhand(), 0, null) != 0) {
			 * TF2weapons.network.sendToServer(new TF2Message.ActionMessage(26)); }
			 */
			if (minecraft.player != null && !minecraft.player.getMainHandItem().isEmpty())
				if (minecraft.player.getMainHandItem().getItem() instanceof ItemUsable) {
					keyPressUpdate(minecraft.options.keyBindAttack.isDown(),
							minecraft.options.keyBindUseItem.isDown());
				}
		}
		if (minecraft.options.showDebugProfilerChart && this.profileStartTime == -1) {
			this.profileStartTime = MinecraftServer.getCurrentTimeMillis();
			this.profileStartTick = (int) Minecraft.getInstance().level.getGameTime();
		} else if (this.profileStartTime != -1) {
			long i = MinecraftServer.getCurrentTimeMillis();
			int j = (int) Minecraft.getInstance().level.getGameTime();
			long k = i - this.profileStartTime;
			int l = j - this.profileStartTick;
			this.saveProfilerResults(k, l);
			this.profileStartTime = -1;
		}
	}

	private void saveProfilerResults(long timeSpan, int tickSpan) {
		File file1 = new File("./debug/profile-results-client-"
				+ (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + ".txt");
		file1.getParentFile().mkdirs();
		Writer writer = null;

		try {
			writer = new OutputStreamWriter(new FileOutputStream(file1), StandardCharsets.UTF_8);
			writer.write(this.getProfilerResults(timeSpan, tickSpan, Minecraft.getInstance().mcProfiler));
		} catch (Throwable throwable) {
			TF2weapons.LOGGER.error("Could not save profiler results to {}", file1, throwable);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	private String getProfilerResults(long timeSpan, int tickSpan, ProfilerFiller server) {
		StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.append("---- Minecraft Profiler Results ----\n");
		stringbuilder.append("// ");
		stringbuilder.append(getWittyComment());
		stringbuilder.append("\n\n");
		stringbuilder.append("Time span: ").append(timeSpan).append(" ms\n");
		stringbuilder.append("Tick span: ").append(tickSpan).append(" ticks\n");
		stringbuilder.append("// This is approximately ").append(String.format("%.2f", tickSpan / (timeSpan / 1000.0F)))
				.append(" ticks per second. It should be ").append(20).append(" ticks per second\n\n");
		stringbuilder.append("--- BEGIN PROFILE DUMP ---\n\n");
		appendProfilerResults(0, "root", stringbuilder, server);
		stringbuilder.append("--- END PROFILE DUMP ---\n\n");
		return stringbuilder.toString();
	}

	private void appendProfilerResults(int depth, String sectionName, StringBuilder builder, ProfilerFiller server) {
		List<ProfilerFiller.Result> list = server.getProfilingData(sectionName);

		if (list == null) return;
		if (list.size() < 3) return;
		for (int i = 1; i < list.size(); ++i) {
			ProfilerFiller.Result profiler$result = list.get(i);
			builder.append(String.format("[%02d] ", depth));
			for (int j = 0; j < depth; ++j) builder.append("|   ");
			builder.append(profiler$result.profilerName).append(" - ")
					.append(String.format("%.2f", profiler$result.usePercentage)).append("%/")
					.append(String.format("%.2f", profiler$result.totalUsePercentage)).append("%\n");
			if (!"unspecified".equals(profiler$result.profilerName)) {
				try {
					this.appendProfilerResults(depth + 1, sectionName + "." + profiler$result.profilerName, builder,
							server);
				} catch (Exception exception) {
					builder.append("[[ EXCEPTION ").append(exception).append(" ]]");
				}
			}
		}
	}

	/**
	 * Get a random witty comment
	 */
	private static String getWittyComment() {
		String[] astring = new String[] { "Shiny numbers!", "Am I not running fast enough? :(",
				"I'm working as hard as I can!", "Will I ever be good enough for you? :(", "Speedy. Zoooooom!",
				"Hello world", "40% better than a crash report.", "Now with extra numbers", "Now with less numbers",
				"Now with the same numbers", "You should add flames to things, it makes them go faster!",
				"Do you feel the need for... optimization?", "*cracks redstone whip*",
				"Maybe if you treated it better then it'll have more motivation to work faster! Poor server." };

		try {
			return astring[(int) (System.nanoTime() % astring.length)];
		} catch (Throwable var2) {
			return "Witty comment unavailable :(";
		}
	}

	public int getPressedHotbarKey(KeyMapping[] keys, boolean press) {
		int sel = -1;
		for (int i = 0; i < keys.length; i++) if (keys[i].isDown()) {
			sel = i;
			if (press) keys[i].consumeClick();
			break;
		}
		return sel;
	}

	@SubscribeEvent
	public void mousePress(MouseEvent event) {
		if (event.getButton() != -1) {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.screen == null && minecraft.player != null
					&& !minecraft.player.getMainHandItem().isEmpty())
				if (minecraft.player.getMainHandItem().getItem() instanceof ItemUsable) {
					KeyMapping.setKeyBindState(event.getButton() - 100, event.isButtonstate());
					keyPressUpdate(minecraft.options.keyBindAttack.isDown(),
							minecraft.options.keyBindUseItem.isDown());
				}
		}
		if (event.getDwheel() != 0) {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.screen == null && minecraft.player != null) {
				ItemStack stack = minecraft.player.getMainHandItem();
				ItemStack backpack = ItemBackpack.getBackpack(minecraft.player);
				if (stack.getItem() instanceof IItemNoSwitch
						&& ((IItemNoSwitch) stack.getItem()).stopSlotSwitch(stack, minecraft.player))
					minecraft.player.getInventory().changeCurrentItem(-event.getDwheel());
				else if (!backpack.isEmpty() && !((ItemBackpack) backpack.getItem())
						.getBackpackItemToUse(backpack, minecraft.player).isEmpty()) {

					if (TF2ConfigVars.mouseBackpackItemSwitch
							&& !TF2PlayerCapability.get(minecraft.player).isBackpackItemEquipped()
							&& ((minecraft.player.getInventory().selected == 0 && event.getDwheel() > 0)
									|| (minecraft.player.getInventory().selected == 8 && event.getDwheel() < 0))) {
						minecraft.player.getInventory().changeCurrentItem(-event.getDwheel());
						TF2PlayerCapability.get(minecraft.player).setEquipBackpackItem(true);
						TF2weapons.network.sendToServer(new TF2Message.ActionMessage(26));
					} else if (TF2PlayerCapability.get(minecraft.player).isBackpackItemEquipped()) {
						TF2PlayerCapability.get(minecraft.player).setEquipBackpackItem(false);
						TF2weapons.network.sendToServer(new TF2Message.ActionMessage(27));
					}
				}

			}

		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		// TF2weapons.syncConfig();
		if (eventArgs.getModID().equals("rafradek_tf2_weapons")) {
			TF2ConfigVars.createConfig(true);
			if (Minecraft.getInstance().player != null)
				TF2weapons.network.sendToServer(new TF2Message.InitClientMessage(TF2weapons.conf));
		}
	}

	public void keyPressUpdate(boolean attackKeyDown, boolean altAttackKeyDown) {
		keyPressUpdate(attackKeyDown, altAttackKeyDown, false);
	}

	public static void keyPressUpdate() {
		keyPressUpdate(Minecraft.getInstance().options.keyBindAttack.isDown(),
				Minecraft.getInstance().options.keyBindUseItem.isDown(), true);
	}

	public static void keyPressUpdate(boolean attackKeyDown, boolean altAttackKeyDown, boolean force) {
		Minecraft minecraft = Minecraft.getInstance();

		boolean changed = force;
		ItemStack item = minecraft.player.getMainHandItem();
		// System.out.println("Gui: "+(minecraft.currentScreen!=null));
		if (attackKeyDown && !alreadypressed) {
			changed = true;
			alreadypressed = true;
		}
		if (!attackKeyDown && alreadypressed) {
			changed = true;
			alreadypressed = false;
		}
		if (altAttackKeyDown && !alreadypressedalt) {
			changed = true;
			alreadypressedalt = true;
		}
		if (!altAttackKeyDown && alreadypressedalt) {
			changed = true;
			alreadypressedalt = false;
		}
		if (reloadPressed(minecraft) && !alreadypressedreload) {
			changed = true;
			alreadypressedreload = true;
		}
		if (alreadypressedreload && !reloadPressed(minecraft)) {
			changed = true;
			alreadypressedreload = false;
		}
		if (changed && minecraft.screen == null) {
			LivingEntity player = minecraft.player;
			WeaponsCapability cap = minecraft.player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
			int oldState = cap.state & 3;
			int plus = cap.state & 8;
			int state = getActionType(attackKeyDown, altAttackKeyDown) + plus;
			cap.state = state;
			int stateOverride = ((ItemUsable) item.getItem()).getStateOverride(item, player, state);
			if (item != null && item.getItem() instanceof ItemUsable
					&& oldState != (getActionType(attackKeyDown, altAttackKeyDown) & 3)
					&& item.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active == 2) {
				if ((oldState & 2) < (state & 2)) {
					// cap.setSecondaryCooldown(EnumHand.OFF_HAND, ((ItemUsable)
					// item.getItem()).getAltFiringSpeed(item, player) / 2);
					cap.stateDo(player, item, InteractionHand.MAIN_HAND, stateOverride);
					((ItemUsable) item.getItem()).startUse(item, player, player.level, oldState, state & 3);
				} else if ((oldState & 2) > (state & 2)) {
					((ItemUsable) item.getItem()).endUse(item, player, player.level, oldState, state & 3);
				}
				if ((oldState & 1) < (state & 1)) {
					// cap.setPrimaryCooldown(EnumHand.OFF_HAND, ((ItemUsable)
					// item.getItem()).getFiringSpeed(item, player) / 2);
					cap.stateDo(player, item, InteractionHand.MAIN_HAND, stateOverride);
					((ItemUsable) item.getItem()).startUse(item, player, player.level, oldState, state & 3);
				} else if ((oldState & 1) > (state & 1)) {
					((ItemUsable) item.getItem()).endUse(item, player, player.level, oldState, state & 3);
				}
			}
			TF2weapons.network
					.sendToServer(new TF2Message.ActionMessage(getActionType(attackKeyDown, altAttackKeyDown)));
		}
	}

	@SubscribeEvent
	public void clientTickEnd(TickEvent.ClientTickEvent event) {

		Minecraft minecraft = Minecraft.getInstance();

		if (event.phase == TickEvent.Phase.END) {

			if (minecraft.player != null && minecraft.screen != null) {
				WeaponsCapability.get(minecraft.player).setPrimaryCooldown(
						Math.max(WeaponsCapability.get(minecraft.player).getPrimaryCooldown(), 250));
			}

			Iterator<LivingEntity> soundIterator = ClientProxy.soundsToStart.keySet().iterator();
			while (soundIterator.hasNext()) {
				LivingEntity living = soundIterator.next();

				soundIterator.remove();
			}
			if (TF2ConfigVars.dynamicLights) {
				removeSource();
			}
			// ItemUsable.tick(true);
		}
	}

	/*
	 * @SubscribeEvent public void entityConstructing(final
	 * EntityEvent.EntityConstructing event) {
	 *
	 * if (event.getEntity() instanceof EntityPlayerSP) { int[][] tex = new
	 * int[1][skinIcon.getIconWidth()*skinIcon.getIconHeight()];
	 *
	 * for (int x = 0; x < skinIcon.getIconWidth(); x++) { for (int y = 0; y <
	 * skinIcon.getIconHeight(); y++) { tex[0][x+skinIcon.getIconWidth()*y] =
	 * ((EntityLivingBase) event.getEntity()).getRNG().nextInt(); } }
	 *
	 * TextureUtil.uploadTextureMipmap(tex, skinIcon.getIconWidth(),
	 * skinIcon.getIconHeight(), skinIcon.getOriginX(), skinIcon.getOriginY(),
	 * false, false); } }
	 */

	@Optional.Method(modid = "dynamiclights")
	public static void removeSource() {
		Iterator<MuzzleFlashLightSource> iterator = muzzleFlashes.iterator();
		while (iterator.hasNext()) {
			MuzzleFlashLightSource light = iterator.next();
			light.update();
			if (light.over) {
				DynamicLights.removeLightSource(light);
				iterator.remove();
			}
		}

	}

	public static boolean reloadPressed(Minecraft minecraft) {
		ItemStack stack = minecraft.player.getMainHandItem();
		return ClientProxy.reload.isDown() || (minecraft.player != null && TF2ConfigVars.autoReload
				&& stack.getItem() instanceof ItemWeapon && ((ItemWeapon) stack.getItem()).hasClip(stack)
				&& stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active == 2 && ((ItemWeapon) stack.getItem())
						.getClip(stack) != ((ItemWeapon) stack.getItem()).getWeaponClipSize(stack, minecraft.player)
				&& !(stack.getItem() instanceof ItemSniperRifle));
	}

	public static int getActionType(boolean attackKeyDown, boolean altAttackKeyDown) {
		int value = 0;
		ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
		boolean swap = TF2ConfigVars.swapAttackButton && !(stack.getItem() instanceof ItemMeleeWeapon);
		boolean allow = true;
		if (Minecraft.getInstance().hitResult != null
				&& Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = Minecraft.getInstance().hitResult.getBlockPos();
			if (ClientProxy.interactingBlocks
					.contains(Minecraft.getInstance().level.getBlockState(pos).getBlock().getClass()))
				allow = false;
		}

		if ((!swap && attackKeyDown) || (swap && altAttackKeyDown && allow)) {
			value++;
		}
		if ((swap && attackKeyDown) || (!swap && altAttackKeyDown && allow)) {
			value += 2;
		}
		if (reloadPressed(Minecraft.getInstance())) {
			value += 4;
		}
		return value;
	}

	@SubscribeEvent
	public void getFov(FOVUpdateEvent event) {
		if (event.getEntity().getItemInHand(InteractionHand.MAIN_HAND) != null
				&& event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemUsable)
			if (event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemSniperRifle
					&& event.getEntity().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).isCharging()) {
				event.setNewfov(event.getFov() * 0.55f);
			} else if (event.getEntity().getAttribute(Attributes.MOVEMENT_SPEED)
					.getModifier(ItemMinigun.slowdownUUID) != null) {
				event.setNewfov(event.getNewfov() * 1.4f);
			}
		int token = WeaponsCapability.get(event.getEntity()).getUsedToken();
		if (token >= 0) {
			event.setNewfov(event.getNewfov() - (ItemToken.SPEED_VALUES[token] / 2f));
		}
	}

	@SubscribeEvent
	public void blockDeathGui(GuiOpenEvent event) {
		if (event.getGui() instanceof GuiGameOver && WeaponsCapability.get(Minecraft.getInstance().player).isFeign()
				&& Minecraft.getInstance().player.getHealth() > 0f) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
		if (Minecraft.getInstance().player != null) {
			if ((event.getGui() instanceof InventoryScreen || event.getGui() instanceof CreativeModeInventoryScreen
					|| event.getGui() instanceof GuiWearables)
					&& !Minecraft.getInstance().player.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).isEmpty()) {
				// GuiContainer gui = (GuiContainer) event.getGui();
				event.getButtonList().add(new GuiButtonImage(97535627, event.getGui().getBbWidth() / 2 - 10,
						event.getGui().getBbHeight() / 2 + 95, 18, 18, 238, 220, 18, GuiWearables.WEARABLES_TEXTURE));
			}

			if (event.getGui() instanceof MerchantScreen)
				if (((MerchantScreen) event.getGui()).getMerchant().getHoverName().getString()
						.equals(I18n.get("entity.hale.name"))) {
					event.getButtonList().add(new Button(7578, event.getGui().getBbWidth() / 2 - 100,
							event.getGui().getBbHeight() / 2 - 110, 100, 20, "Leave Team"));
					event.getButtonList().add(new Button(7579, event.getGui().getBbWidth() / 2,
							event.getGui().getBbHeight() / 2 - 110, 100, 20, "Recover Lost Items"));
				}

			if (event.getGui() instanceof GuiIngameMenu)
				event.getButtonList().add(new Button(125362351, event.getGui().getBbWidth() / 2 - 40,
						event.getGui().getBbHeight() - 40, 80, 20, "Contracts"));
			ItemStack stack = Minecraft.getInstance().player.getMainHandItem();

		}
	}

	@SubscribeEvent
	public void guiPostAction(GuiScreenEvent.ActionPerformedEvent.Post event) {

		if (event.getGui() instanceof InventoryScreen || event.getGui() instanceof CreativeModeInventoryScreen)
			if (event.getButton().id == 97535627) {
				// Minecraft.getMinecraft().displayGuiScreen(null);
				TF2weapons.network.sendToServer(new TF2Message.ShowGuiMessage(0));
			}

		if (event.getGui() instanceof GuiWearables)
			if (event.getButton().id == 97535627) {
				event.getGui().mc.setScreen(new InventoryScreen(event.getGui().mc.player));
			}
		// PacketHandler.INSTANCE.sendToServer(new
		// PacketOpenNormalInventory(event.getGui().mc.player));
		if (event.getGui() instanceof MerchantScreen && event.getButton().id == 7578) {
			TF2weapons.network.sendToServer(new TF2Message.ActionMessage(29));
			Minecraft.getInstance().player.closeScreen();
		} else if (event.getGui() instanceof MerchantScreen && event.getButton().id == 7579) {
			TF2weapons.network.sendToServer(new TF2Message.ActionMessage(18));
		}

		if (event.getGui() instanceof GuiIngameMenu)
			if (event.getButton().id == 125362351) {
				Minecraft.getInstance().setScreen(new GuiContracts());
			}

	}

	@SubscribeEvent
	public void applyRecoil(EntityViewRenderEvent.CameraSetup event) {
		if (event.getEntity().getCapability(TF2weapons.WEAPONS_CAP).isPresent()) {
			WeaponsCapability cap = event.getEntity().getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
			event.setPitch(event.getPitch() - cap.recoil);
			if (cap.recoil > 0) {
				cap.recoil = Math.max((cap.recoil * 0.8f) - 0.06f, 0);
			}
		}
	}

	public static void setColorTeam(Entity ent, float alpha) {
		ClientProxy.setColor(TF2Util.getTeamColor(ent), 0.7f, 0, 0.25f, 0.8f);
	}

	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent.Pre event) {
		Player player = Minecraft.getInstance().player;
		if (player == null)
			return;
		WeaponsCapability cap = player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
		ItemWeapon.inHand = event.getType() != ElementType.HOTBAR;
		ClientProxy.renderCritGlow = 0;
		GuiComponent gui = Minecraft.getInstance().gui;
		ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
		int width = event.getResolution().getScaledWidth();
		int height = event.getResolution().getScaledHeight();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder renderer = tessellator.getBuffer();
		Entity mouseTarget = Minecraft.getInstance().hitResult != null
				? Minecraft.getInstance().hitResult.getEntity()
				: null;
		if (event.getType() == ElementType.HELMET) {
			GlStateManager.enableBlend();
			if (player.getUseItem().getItem() instanceof ItemWrench && player.getUseItemRemainingTicks() < 770) {

			}
			if ((player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).newContracts
					|| player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).newRewards)) {
				String line1;
				String line2;
				if (player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).newRewards) {
					line1 = "You can claim your contract reward";
					line2 = "Contracts can be accessed in the pause menu";
				} else {
					line1 = "You have a new contract";
					line2 = "Contracts can be accessed in the pause menu";
				}
				gui.getFontRenderer().drawString(line1,
						event.getResolution().getScaledWidth() - gui.getFontRenderer().width(line1), 50,
						0xFFFFFF);
				gui.getFontRenderer().drawString(line2,
						event.getResolution().getScaledWidth() - gui.getFontRenderer().width(line2), 65,
						0xFFFFFF);
			}
			if (ClientProxy.reload.isDown()) {

				int commands = 5;
				for (int i = 0; i < commands; i++) {
					String line = I18n.get("gui.command." + (i + 1));
					gui.getFontRenderer().drawString(line, 5,
							(event.getResolution().getScaledHeight() - commands * 15) / 2 + i * 15, 0xFFFFFF);
				}
			}
			boolean shield = (player.hasEffect(TF2weapons.shieldBullet)
					&& player.getEffect(TF2weapons.shieldBullet).getAmplifier() > 0)
					|| (player.hasEffect(TF2weapons.shieldExplosive)
							&& player.getEffect(TF2weapons.shieldExplosive).getAmplifier() > 0)
					|| (player.hasEffect(TF2weapons.shieldFire)
							&& player.getEffect(TF2weapons.shieldFire).getAmplifier() > 0);
			if (player.getEffect(TF2weapons.uber) != null || shield) {
				GlStateManager.disableDepth();
				GlStateManager.depthMask(false);
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ZERO,
						GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE,
						GlStateManager.DestFactor.ZERO);
				ClientProxy.setColor(TF2Util.getTeamColor(player), 1f, 0, 0f, 1f);
				Minecraft.getInstance().RenderSystem.setShaderTexture(0, ClientProxy.VIGNETTE);
				BufferBuilder BufferBuilder = tessellator.getBuffer();
				BufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
				BufferBuilder.pos(0.0D, event.getResolution().getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
				BufferBuilder
						.pos(event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight(), -90.0D)
						.tex(1.0D, 1.0D).endVertex();
				BufferBuilder.pos(event.getResolution().getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
				BufferBuilder.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
				tessellator.draw();
				GlStateManager.depthMask(true);
				GlStateManager.enableDepth();
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
						GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
						GlStateManager.DestFactor.ZERO);
			}
		}
		if (event.getType() == ElementType.HOTBAR) {
			GlStateManager.enableBlend();
			ItemStack backpack = ItemBackpack.getBackpack(player);
			if (!backpack.isEmpty()
					&& !((ItemBackpack) backpack.getItem()).getBackpackItemToUse(backpack, player).isEmpty()) {
				ItemStack toUse;
				if (!TF2PlayerCapability.get(player).getBackpackItemHold().isEmpty())
					toUse = TF2PlayerCapability.get(player).getBackpackItemHold();
				else {
					toUse = ((ItemBackpack) backpack.getItem()).getBackpackItemToUse(backpack, player);
				}

				Minecraft.getInstance().RenderSystem.setShaderTexture(0, ClientProxy.WIDGETS_TEXTURE_MC);
				int mid = width / 2;

				Minecraft.getInstance().gui.drawTexturedModalRect(mid + 91 + 29, height - 23, 24, 22, 29, 24);

				RenderHelper.enableGUIStandardItemLighting();

				Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(player, toUse, mid + 91 + 32,
						height - 16 - 3);

				Minecraft.getInstance().getItemRenderer().renderItemOverlays(Minecraft.getInstance().font,
						toUse, mid + 91 + 32, height - 16 - 3);

				RenderHelper.disableStandardItemLighting();
				GlStateManager.enableBlend();
			}
			ItemStack pda = TF2Util.getFirstItem(Minecraft.getInstance().player.inventory,
					stackL -> stackL.getItem() instanceof ItemPDA);
			if (TF2EventsCommon.sentryView != null && !pda.isEmpty() && pda.hasTag()
					&& pda.getTag().getBoolean("ShowHud")) {
				// GL11.glDisable(GL11.GL_DEPTH_TEST);
				// GL11.glDepthMask(false);
				// OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				// GL11.glDisable(GL11.GL_ALPHA_TEST);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
				TF2PlayerCapability plcap = TF2PlayerCapability.get(player);
				GlStateManager.pushMatrix();
				GlStateManager.scale(0.75f, 0.75f, 1f);

				if (plcap.getSentryView().getSize() > 0)
					TF2EventsCommon.sentryView.readAdditionalSaveData(plcap.getSentryView());
				else
					TF2EventsCommon.sentryView.setHealth(0);

				GlStateManager.translate(4, 4, 0);
				Minecraft.getInstance().RenderSystem.setShaderTexture(0, ClientProxy.buildingTexture);
				TF2EventsCommon.sentryView.renderGUI(renderer, tessellator, player, width, height,
						Minecraft.getInstance().gui);

				if (plcap.getDispenserView().getSize() > 0)
					TF2EventsCommon.dispenserView.readAdditionalSaveData(plcap.getDispenserView());
				else {
					TF2EventsCommon.dispenserView.setHealth(0);
				}

				GlStateManager.translate(0, 68, 0);
				Minecraft.getInstance().RenderSystem.setShaderTexture(0, ClientProxy.buildingTexture);
				TF2EventsCommon.dispenserView.renderGUI(renderer, tessellator, player, width, height,
						Minecraft.getInstance().gui);

				if (plcap.getTeleporterAView().getSize() > 0)
					TF2EventsCommon.teleporterAView.readAdditionalSaveData(plcap.getTeleporterAView());
				else
					TF2EventsCommon.teleporterAView.setHealth(0);

				GlStateManager.translate(0, 52, 0);
				Minecraft.getInstance().RenderSystem.setShaderTexture(0, ClientProxy.buildingTexture);
				TF2EventsCommon.teleporterAView.renderGUI(renderer, tessellator, player, width, height,
						Minecraft.getInstance().gui);

				if (plcap.getTeleporterBView().getSize() > 0)
					TF2EventsCommon.teleporterBView.readAdditionalSaveData(plcap.getTeleporterBView());
				else
					TF2EventsCommon.teleporterBView.setHealth(0);

				GlStateManager.translate(0, 52, 0);
				Minecraft.getInstance().RenderSystem.setShaderTexture(0, ClientProxy.buildingTexture);
				TF2EventsCommon.teleporterBView.renderGUI(renderer, tessellator, player, width, height,
						Minecraft.getInstance().gui);
				GlStateManager.popMatrix();
				// GL11.glEnable(GL11.GL_TEXTURE_2D);
				// GL11.glDepthMask(true);
				// GL11.glEnable(GL11.GL_DEPTH_TEST);
				// GL11.glEnable(GL11.GL_ALPHA_TEST);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}
			if (!held.isEmpty() && held.getItem() instanceof IItemOverlay) {
				if (((IItemOverlay) held.getItem()).showInfoBox(held, player)) {
					Minecraft.getInstance().RenderSystem.setShaderTexture(0, ClientProxy.healingTexture);

					// GL11.glDisable(GL11.GL_DEPTH_TEST);
					// GL11.glDepthMask(false);
					// OpenGlHelper.glBlendFunc(770, 771, 1, 0);
					// GL11.glDisable(GL11.GL_ALPHA_TEST);

					ClientProxy.setColor(TF2Util.getTeamColor(player), 0.7f, 0, 0.25f, 0.8f);

					renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
					renderer.pos(event.getResolution().getScaledWidth() - 74,
							event.getResolution().getScaledHeight() - 20, 0.0D).tex(0.0D, 1D).endVertex();
					renderer.pos(event.getResolution().getScaledWidth() - 14,
							event.getResolution().getScaledHeight() - 20, 0.0D).tex(0.01D, 1D).endVertex();
					renderer.pos(event.getResolution().getScaledWidth() - 14,
							event.getResolution().getScaledHeight() - 50, 0.0D).tex(0.01D, 0.99D).endVertex();
					renderer.pos(event.getResolution().getScaledWidth() - 74,
							event.getResolution().getScaledHeight() - 50, 0.0D).tex(0.0D, 0.99D).endVertex();
					tessellator.draw();

					GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
					renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
					renderer.pos(event.getResolution().getScaledWidth() - 76,
							event.getResolution().getScaledHeight() - 18, 0.0D).tex(0.5D, 0.265625D).endVertex();
					renderer.pos(event.getResolution().getScaledWidth() - 12,
							event.getResolution().getScaledHeight() - 18, 0.0D).tex(1.0D, 0.265625D).endVertex();
					renderer.pos(event.getResolution().getScaledWidth() - 12,
							event.getResolution().getScaledHeight() - 52, 0.0D).tex(1.0D, 0.53125D).endVertex();
					renderer.pos(event.getResolution().getScaledWidth() - 76,
							event.getResolution().getScaledHeight() - 52, 0.0D).tex(0.5D, 0.53125D).endVertex();
					tessellator.draw();
					String[] text = ((IItemOverlay) held.getItem()).getInfoBoxLines(held, player);
					gui.drawString(gui.getFontRenderer(), text[0], event.getResolution().getScaledWidth() - 66,
							event.getResolution().getScaledHeight() - 48, 16777215);
					gui.drawString(gui.getFontRenderer(), text[1], event.getResolution().getScaledWidth() - 66,
							event.getResolution().getScaledHeight() - 30, 16777215);
					// GL11.glDepthMask(true);
					// GL11.glEnable(GL11.GL_DEPTH_TEST);
					// GL11.glEnable(GL11.GL_ALPHA_TEST);
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				}
				((IItemOverlay) held.getItem()).drawOverlay(held, player, Tessellator.getInstance(),
						Tessellator.getInstance().getBuffer(), event.getResolution());
			}
			if (player.getEffect(TF2weapons.it) != null) {
				// GL11.glDisable(GL11.GL_DEPTH_TEST);
				// GL11.glDepthMask(false);
				// OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				// GL11.glDisable(GL11.GL_ALPHA_TEST);
				gui.drawCenteredString(gui.getFontRenderer(), I18n.get("gui.markedit"),
						event.getResolution().getScaledWidth() / 2, event.getResolution().getScaledHeight() / 4,
						16777215);
				// GL11.glDepthMask(true);
				// GL11.glEnable(GL11.GL_DEPTH_TEST);
				// GL11.glEnable(GL11.GL_ALPHA_TEST);
			}
			if (player.getEffect(TF2weapons.bombmrs) != null) {
				// GL11.glDisable(GL11.GL_DEPTH_TEST);
				// GL11.glDepthMask(false);
				// OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				// GL11.glDisable(GL11.GL_ALPHA_TEST);
				gui.drawCenteredString(gui.getFontRenderer(), I18n.get("gui.bombmrs"),
						event.getResolution().getScaledWidth() / 2, event.getResolution().getScaledHeight() / 4,
						16777215);
				// GL11.glDepthMask(true);
				// GL11.glEnable(GL11.GL_DEPTH_TEST);
				// GL11.glEnable(GL11.GL_ALPHA_TEST);
			}
			Entity healTarget = player.level.getEntity(cap.getHealTarget());

			if (healTarget == null && TF2Util.isAlliedTo(mouseTarget, player)
					&& !(mouseTarget instanceof EntityBuilding))
				healTarget = mouseTarget;

			if (healTarget != null && healTarget instanceof LivingEntity) {
				LivingEntity living = (LivingEntity) healTarget;

				Minecraft.getInstance().RenderSystem.setShaderTexture(0, ClientProxy.healingTexture);
				// GL11.glDisable(GL11.GL_DEPTH_TEST);
				// GL11.glDepthMask(false);
				// OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				// GL11.glDisable(GL11.GL_ALPHA_TEST);

				GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
				// gui.drawTexturedModalRect(event.getResolution().getScaledWidth()/2-64,
				// event.getResolution().getScaledHeight()/2+35, 0, 0, 128, 40);
				setColorTeam(player, 0.7f);

				renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
				renderer.pos(event.getResolution().getScaledWidth() / 2 - 62,
						event.getResolution().getScaledHeight() / 2 + 70, 0.0D).tex(0.0D, 1D).endVertex();
				renderer.pos(event.getResolution().getScaledWidth() / 2 + 62,
						event.getResolution().getScaledHeight() / 2 + 70, 0.0D).tex(0.01D, 1D).endVertex();
				renderer.pos(event.getResolution().getScaledWidth() / 2 + 62,
						event.getResolution().getScaledHeight() / 2 + 40, 0.0D).tex(0.01D, 0.99D).endVertex();
				renderer.pos(event.getResolution().getScaledWidth() / 2 - 62,
						event.getResolution().getScaledHeight() / 2 + 40, 0.0D).tex(0.0D, 0.99D).endVertex();
				tessellator.draw();

				GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
				renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
				renderer.pos(event.getResolution().getScaledWidth() / 2 - 64,
						event.getResolution().getScaledHeight() / 2 + 72, 0.0D).tex(0.0D, 0.265625D).endVertex();
				renderer.pos(event.getResolution().getScaledWidth() / 2 + 64,
						event.getResolution().getScaledHeight() / 2 + 72, 0.0D).tex(1.0D, 0.265625D).endVertex();
				renderer.pos(event.getResolution().getScaledWidth() / 2 + 64,
						event.getResolution().getScaledHeight() / 2 + 38, 0.0D).tex(1.0D, 0.0D).endVertex();
				renderer.pos(event.getResolution().getScaledWidth() / 2 - 64,
						event.getResolution().getScaledHeight() / 2 + 38, 0.0D).tex(0.0D, 0.0D).endVertex();
				tessellator.draw();
				float overheal = 1f + living.getAbsorptionAmount() / living.getMaxHealth();
				if (overheal > 1f) {
					GlStateManager.color(1.0F, 1.0F, 1.0F, 0.4F);
					renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
					renderer.pos(event.getResolution().getScaledWidth() / 2 - 47 - 10 * overheal,
							event.getResolution().getScaledHeight() / 2 + 55 + 10 * overheal, 0.0D).tex(0.0D, 0.59375D)
							.endVertex();
					renderer.pos(event.getResolution().getScaledWidth() / 2 - 47 + 10 * overheal,
							event.getResolution().getScaledHeight() / 2 + 55 + 10 * overheal, 0.0D)
							.tex(0.28125D, 0.59375D).endVertex();
					renderer.pos(event.getResolution().getScaledWidth() / 2 - 47 + 10 * overheal,
							event.getResolution().getScaledHeight() / 2 + 55 - 10 * overheal, 0.0D)
							.tex(0.28125D, 0.3125D).endVertex();
					renderer.pos(event.getResolution().getScaledWidth() / 2 - 47 - 10 * overheal,
							event.getResolution().getScaledHeight() / 2 + 55 - 10 * overheal, 0.0D).tex(0.0D, 0.3125D)
							.endVertex();
					tessellator.draw();
				}
				GlStateManager.color(0.12F, 0.12F, 0.12F, 1F);
				renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
				renderer.pos(event.getResolution().getScaledWidth() / 2 - 58.3,
						event.getResolution().getScaledHeight() / 2 + 66.4, 0.0D).tex(0.0D, 0.59375D).endVertex();
				renderer.pos(event.getResolution().getScaledWidth() / 2 - 35.7,
						event.getResolution().getScaledHeight() / 2 + 66.4, 0.0D).tex(0.28125D, 0.59375D).endVertex();
				renderer.pos(event.getResolution().getScaledWidth() / 2 - 35.7,
						event.getResolution().getScaledHeight() / 2 + 43.6, 0.0D).tex(0.28125D, 0.3125D).endVertex();
				renderer.pos(event.getResolution().getScaledWidth() / 2 - 58.3,
						event.getResolution().getScaledHeight() / 2 + 43.6, 0.0D).tex(0.0D, 0.3125D).endVertex();
				tessellator.draw();
				float health = living.getHealth() / living.getMaxHealth();
				if (health > 0.33f) {
					GlStateManager.color(0.9F, 0.9F, 0.9F, 1F);
				} else {
					GlStateManager.color(0.85F, 0.0F, 0.0F, 1F);
				}
				int tf2health = Math.round((living.getHealth() / TF2ConfigVars.damageMultiplier) * overheal * 10);

				renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
				renderer.pos(event.getResolution().getScaledWidth() / 2 - 57,
						event.getResolution().getScaledHeight() / 2 + 65, 0.0D).tex(0.0D, 0.59375D).endVertex();
				renderer.pos(event.getResolution().getScaledWidth() / 2 - 37,
						event.getResolution().getScaledHeight() / 2 + 65, 0.0D).tex(0.28125D, 0.59375D).endVertex();
				renderer.pos(event.getResolution().getScaledWidth() / 2 - 37,
						event.getResolution().getScaledHeight() / 2 + 65 - health * 20, 0.0D)
						.tex(0.28125D, 0.59375D - 0.28125D * health).endVertex();
				renderer.pos(event.getResolution().getScaledWidth() / 2 - 57,
						event.getResolution().getScaledHeight() / 2 + 65 - health * 20, 0.0D)
						.tex(0.0D, 0.59375D - 0.28125D * health).endVertex();
				tessellator.draw();

				gui.getFontRenderer().drawString(Integer.toString(tf2health),
						event.getResolution().getScaledWidth() / 2 - 47
								- gui.getFontRenderer().width(Integer.toString(tf2health)) / 2,
						event.getResolution().getScaledHeight() / 2 + 52, 0x101010);
				String text = "";
				if (player.level.getEntity(cap.getHealTarget()) != null) {
					text = "Healing:";
				} else if (living.getMainHandItem().getItem() instanceof ItemMedigun
						&& WeaponsCapability.get(living) != null) {
					text = "Ubercharge: " + Math.round(WeaponsCapability.get(living).getUberView() * 100f) + "%";
				}
				gui.drawString(gui.getFontRenderer(), text, event.getResolution().getScaledWidth() / 2 - 28,
						event.getResolution().getScaledHeight() / 2 + 42, 16777215);
				gui.drawString(gui.getFontRenderer(), living.getHoverName().getString(),
						event.getResolution().getScaledWidth() / 2 - 28,
						event.getResolution().getScaledHeight() / 2 + 54, 16777215);

				// GL11.glEnable(GL11.GL_TEXTURE_2D);
				// GL11.glDepthMask(true);
				// GL11.glEnable(GL11.GL_DEPTH_TEST);
				// GL11.glEnable(GL11.GL_ALPHA_TEST);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}

			if (mouseTarget != null && mouseTarget instanceof EntityBuilding
					&& TF2Util.isAlliedTo(player, mouseTarget)) {
				Minecraft.getInstance().RenderSystem.setShaderTexture(0, ClientProxy.buildingTexture);
				// GL11.glDisable(GL11.GL_DEPTH_TEST);
				// GL11.glDepthMask(false);
				// OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				// GL11.glDisable(GL11.GL_ALPHA_TEST);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
				EntityBuilding building = (EntityBuilding) mouseTarget;
				GlStateManager.translate(width / 2 - 72, height / 2 + 52 - building.getGuiHeight() / 2, 0);
				building.renderGUI(renderer, tessellator, player, width, height, gui);
				GlStateManager.translate(-width / 2 + 72, -height / 2 - 52 + building.getGuiHeight() / 2, 0);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				// GL11.glDepthMask(true);
				// GL11.glEnable(GL11.GL_DEPTH_TEST);
				// GL11.glEnable(GL11.GL_ALPHA_TEST);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}
	}

	@SubscribeEvent
	public void renderOverlayPost(RenderGameOverlayEvent.Post event) {

		if (event.getType() == ElementType.HOTBAR) {
			ItemWeapon.inHand = true;
		}
	}

	@SubscribeEvent
	public void renderPlayer(RenderPlayerEvent.Pre event) {
		if (event.getEntity().getEffect(TF2weapons.bombmrs) != null) {
			ModelBomb modelBomb = new ModelBomb();
			GlStateManager.pushMatrix();
			GL11.glTranslatef(0, event.getEntity().eyeHeight, 0);
			GL11.glRotatef(event.getEntity().yRotO
					+ (event.getEntity().getYRot() - event.getEntity().yRotO)
							* event.getPartialRenderTick(),
					0.0F, 1.0F, 0.0F);
			GL11.glRotatef(event.getEntity().xRotO
					+ (event.getEntity().getXRot() - event.getEntity().xRotO)
							* event.getPartialRenderTick(),
					1.0F, 0.0F, 0.0F);
			GL11.glScalef(1.1f, 1.1f, 1.1f);
			Minecraft.getInstance().RenderSystem.setShaderTexture(0, RenderGrenade.TEXTURE_BOMB);
			modelBomb.render(event.getEntity(), 0F, 0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			GlStateManager.popMatrix();
		}
		if (event.getEntity() != Minecraft.getInstance().player) {
			renderBeam(event.getEntity(), event.getPartialRenderTick(), 0.04f);
		}
	}

	@SubscribeEvent
	public void renderGui(GuiScreenEvent.DrawScreenEvent.Pre event) {

		ClientProxy.renderCritGlow = 0;
		ItemWeapon.inHand = false;
	}

	@SubscribeEvent
	public void renderGui(GuiScreenEvent.DrawScreenEvent.Post event) {
		ItemWeapon.inHand = true;
	}

	@SubscribeEvent
	public void renderHand(RenderHandEvent event) {

		Player player = Minecraft.getInstance().player;

		if (!player.getMainHandItem().isEmpty()) {
			ClientProxy.renderCritGlow = TF2Util.calculateCritPre(player.getMainHandItem(), player) * 16
					+ TF2Util.getTeamColorNumber(player);
		} else {
			ClientProxy.renderCritGlow = 0;
		}
		if (WeaponsCapability.get(player).isInvisible()
				|| (player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).isCharging()
						&& player.getMainHandItem().getItem() instanceof ItemSniperRifle)) {

			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void renderSpecificHand(RenderSpecificHandEvent event) {
		Player player = Minecraft.getInstance().player;
		if ((event.getItem().getItem() instanceof ItemCloak && !WeaponsCapability.get(player).isFeign()
				&& ((ItemCloak) event.getItem().getItem()).isFeignDeath(event.getItem(), player))) {
			event.setCanceled(true);
		}
	}

	public static void renderBeam(LivingEntity ent, float partialTicks, float size) {
		if (!ent.getCapability(TF2weapons.WEAPONS_CAP).isPresent())
			return;
		// System.out.println("Drawing");
		Entity healTarget = ent.level.getEntity(ent.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getHealTarget());
		if (healTarget != null) {
			Entity camera = Minecraft.getInstance().getCameraEntity();
			double cameraX = camera.xo + (camera.getX() - camera.xo) * partialTicks;
			double cameraY = camera.yo + (camera.getY() - camera.yo) * partialTicks;
			double cameraZ = camera.zo + (camera.getZ() - camera.zo) * partialTicks;
			// System.out.println("rendering");
			double xPos1 = ent.xo + (ent.getX() - ent.xo) * partialTicks;
			double yPos1 = ent.yo + (ent.getY() - ent.yo) * partialTicks;
			double zPos1 = ent.zo + (ent.getZ() - ent.zo) * partialTicks;
			double xPos2 = healTarget.xo + (healTarget.getX() - healTarget.xo) * partialTicks;
			double yPos2 = healTarget.yo + (healTarget.getY() - healTarget.yo) * partialTicks;
			double zPos2 = healTarget.zo + (healTarget.getZ() - healTarget.zo) * partialTicks;
			double xDist = xPos2 - xPos1;
			double yDist = (yPos2
					+ (healTarget.getBoundingBox().maxY - healTarget.getBoundingBox().minY) / 2 + 0.1)
					- (yPos1 + ent.getEyeHeight() - 0.1);
			double zDist = zPos2 - zPos1;
			float f = Mth.sqrt((float)xDist * xDist + zDist * zDist);
			float fullDist = Mth.sqrt((float)xDist * xDist + yDist * yDist + zDist * zDist);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder renderer = tessellator.getBuffer();
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) xPos1 - cameraX, (float) (yPos1 + ent.getEyeHeight() - 0.1) - cameraY,
					(float) zPos1 - cameraZ);
			GL11.glRotatef((float) (Math.atan2(xDist, zDist) * 180.0D / Math.PI), 0.0F, 1.0F, 0.0F);
			GL11.glRotatef((float) (Math.atan2(yDist, f) * 180.0D / Math.PI) * -1, 1.0F, 0.0F, 0.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			ClientProxy.setColor(TF2Util.getTeamColor(ent), 0.23f, 0, 0f, 1f);
			/*
			 * if (TF2Util.getTeamForDisplay(ent) == 0) { GlStateManager.color(1.0F, 0.0F,
			 * 0.0F, 0.23F); } else { GlStateManager.color(0.0F, 0.0F, 1.0F, 0.23F); }
			 */
			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(-size, -size, 0).endVertex();
			renderer.pos(size, size, 0).endVertex();
			renderer.pos(size, size, fullDist).endVertex();
			renderer.pos(-size, -size, fullDist).endVertex();
			tessellator.draw();
			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(-size, -size, fullDist).endVertex();
			renderer.pos(size, size, fullDist).endVertex();
			renderer.pos(size, size, 0).endVertex();
			renderer.pos(-size, -size, 0).endVertex();
			tessellator.draw();
			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(size, -size, 0).endVertex();
			renderer.pos(-size, size, 0).endVertex();
			renderer.pos(-size, size, fullDist).endVertex();
			renderer.pos(size, -size, fullDist).endVertex();
			tessellator.draw();
			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(size, -size, fullDist).endVertex();
			renderer.pos(-size, size, fullDist).endVertex();
			renderer.pos(-size, size, 0).endVertex();
			renderer.pos(size, -size, 0).endVertex();
			tessellator.draw();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_BLEND);
			GlStateManager.enableTexture2D();
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}

	public static float interpolateRotation(float par1, float par2, float par3) {
		float f;

		for (f = par2 - par1; f < -180.0F; f += 360.0F) {
			;
		}

		while (f >= 180.0F) {
			f -= 360.0F;
		}

		return par1 + par3 * f;
	}

	@SubscribeEvent
	public void renderTick(TickEvent.RenderTickEvent event) {
		tickTime = event.renderTickTime;
		Minecraft minecraft = Minecraft.getInstance();
		if (event.phase == Phase.END)
			if (minecraft.screen == null && minecraft.player != null
					&& minecraft.player.getMainHandItem() != null)
				if (minecraft.player.getMainHandItem().getItem() instanceof ItemUsable) {
					Mouse.poll();
					minecraft.player.yHeadRot = minecraft.player.getYRot();
					moveEntities = true;
					keyPressUpdate(Mouse.isButtonDown(minecraft.options.keyBindAttack.getKeyCode() + 100),
							Mouse.isButtonDown(minecraft.options.keyBindUseItem.getKeyCode() + 100));
					moveEntities = false;
				}
	}

	@SubscribeEvent
	public void renderWorld(RenderWorldLastEvent event) {
		if (Minecraft.getInstance().player != null) {
			float attackdir = TF2PlayerCapability.get(Minecraft.getInstance().player).getInvasionDir();
			if (attackdir != Float.MIN_VALUE) {
				ClientRender.renderAttackDirection(attackdir);
				ClientRender.renderAttackDirection(attackdir - 0.5f);
				ClientRender.renderAttackDirection(attackdir + 0.5f);
			}
			renderBeam(Minecraft.getInstance().player, event.getPartialTicks(), 0.04f);
		}
	}

	@SubscribeEvent
	public void playerName(PlayerEvent.NameFormat event) {
		if (Minecraft.getInstance().player != null && WeaponsCapability.get(event.getEntity()).isDisguised()) {

			String username = WeaponsCapability.get(event.getEntity()).getDisguiseType().substring(2);
			if (TF2Util.isAlliedTo(Minecraft.getInstance().player, event.getEntity())) {
				event.setDisplayname(event.getDisplayname() + " [" + username + "]");
			} else {
				if (WeaponsCapability.get(event.getEntity()).getDisguiseType().startsWith("M:")) {
					if (event.getEntity().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).entityDisguise != null) {
						event.setDisplayname(ChatFormatting.RESET
								+ event.getEntity().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).entityDisguise
										.getHoverName().getString());
					} else
						event.setDisplayname(ChatFormatting.RESET + I18n.get("entity." + username + ".name"));

				} else if (WeaponsCapability.get(event.getEntity()).getDisguiseType().startsWith("P:")) {
					event.setDisplayname(PlayerTeam.formatPlayerName(
							Minecraft.getInstance().level.getScoreboard().getPlayersTeam(username), username));
				} else {
					Player player = Minecraft.getInstance().level.getPlayers(Player.class,
							playerl -> !TF2Util.isAlliedTo(Minecraft.getInstance().player, playerl)).get(0);
					if (player != null)
						event.setDisplayname(PlayerTeam.formatPlayerName(
								Minecraft.getInstance().level.getScoreboard().getPlayersTeam(username),
								player.getName()));
				}
			}
		}
	}

	@SubscribeEvent
	@SuppressWarnings("unchecked")
	public void renderLivingEntity(RenderLivingEvent.Pre<LivingEntity> event) {
		if (!event.getEntity().isAlive())
			return;

		ClientProxy.renderCritGlow = 0;

		if (!(event.getEntity() instanceof Player || event.getEntity() instanceof EntityTF2Character))
			return;

		if (!event.getEntity().getMainHandItem().isEmpty()) {
			ClientProxy.renderCritGlow = TF2Util.calculateCritPre(event.getEntity().getMainHandItem(),
					event.getEntity()) * 16 + TF2Util.getTeamColorNumber(event.getEntity());
		} else {
			ClientProxy.renderCritGlow = 0;
		}

		if (event.getEntity().getCapability(TF2weapons.INVENTORY_CAP).isPresent()) {
			InventoryWearables cap = event.getEntity().getCapability(TF2weapons.INVENTORY_CAP).orElse(null);
			for (int i = 0; i < cap.getContainerSize(); i++) {
				ItemStack hat = cap.getStackInSlot(i);
				if (hat.getItem() instanceof ItemFromData
						&& (((ItemFromData) hat.getItem()).getVisibilityFlags(hat, event.getEntity()) & 1) == 1) {
					cap.origHead = event.getEntity().getItemBySlot(EquipmentSlot.HEAD);
					if (event.getEntity() instanceof Player)
						((Player) event.getEntity()).getInventory().armor.set(3, ItemStack.EMPTY);
					else
						event.getEntity().setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
					break;
				}
			}
		}
		int visTick = event.getEntity().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks;

		if (visTick > 0) {
			if (Minecraft.getInstance().player != null
					&& TF2Util.isAlliedTo(event.getEntity(), Minecraft.getInstance().player))
				visTick = 8;
			if (visTick >= 20) {
				event.setCanceled(true);
				return;
			} else {
				// System.out.println("VisTicksRender
				// "+event.getEntity().getEntityData().getInteger("VisTicks"));
				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				// Team team = event.getEntity().getTeam();
				// if (team == event.getEntity().world.getScoreboard().getTeam("RED")) {
				ClientProxy.setColor(TF2Util.getTeamColor(event.getEntity()), 0.7f * (1 - (float) visTick / 20), 0f,
						0.17f, 1f);
				// } else if (team == event.getEntity().world.getScoreboard().getTeam("BLU")) {
				// GlStateManager.color(0.17F, 0.17F, 1.0F, 0.7f * (1 - (float) visTick / 20));
				// } else {
				// GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7f * (1 - (float) visTick / 20));
				// }
			}
		} else if (event.getEntity() instanceof Player
				&& event.getEntity().getItemInHand(InteractionHand.MAIN_HAND) != null
				&& event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemUsable
				&& !(event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemMeleeWeapon)
				&& event.getRenderer().getMainModel() instanceof ModelBiped) {
			((ModelBiped) event.getRenderer()
					.getMainModel()).rightArmPose = ((event.getEntity().getCapability(TF2weapons.WEAPONS_CAP,
							null).state & 3) > 0)
							|| event.getEntity().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).isCharging()
									? ModelBiped.ArmPose.BOW_AND_ARROW
									: ModelBiped.ArmPose.ITEM;
		}
		if (event.getEntity().getEffect(TF2weapons.buffbanner) != null
				|| event.getEntity().getEffect(TF2weapons.backup) != null
				|| event.getEntity().getEffect(TF2weapons.conch) != null
				|| event.getEntity().getEffect(TF2weapons.quickFix) != null) {
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableBlend();
			GlStateManager.disableLighting();
			Minecraft.getInstance().RenderSystem.setShaderTexture(0, ClientProxy.circleTexture);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			float size = (event.getEntity().tickCount % 20) / 20f;
			ClientProxy.setColor(TF2Util.getTeamColor(event.getEntity()), size, 0.2f, 0f, 1f);
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			bufferbuilder.pos(event.getX() - 0.7 * size, event.getY() + 0.1, event.getZ() + 0.7 * size).tex(0, 1)
					.endVertex();
			bufferbuilder.pos(event.getX() + 0.7 * size, event.getY() + 0.1, event.getZ() + 0.7 * size).tex(1, 1)
					.endVertex();
			bufferbuilder.pos(event.getX() + 0.7 * size, event.getY() + 0.1, event.getZ() - 0.7 * size).tex(1, 0)
					.endVertex();
			bufferbuilder.pos(event.getX() - 0.7 * size, event.getY() + 0.1, event.getZ() - 0.7 * size).tex(0, 0)
					.endVertex();
			tessellator.draw();
			GlStateManager.color(1f, 1f, 1f, 1f);
			GlStateManager.enableLighting();
			GlStateManager.disableBlend();

		}

		if (event.getEntity().getEffect(TF2weapons.uber) != null) {
			// GlStateManager.disableLighting();
			int i = TF2Util.getTeamColor(event.getEntity());
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			GlStateManager.enableTexture2D();
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GlStateManager.enableTexture2D();
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND2_RGB, 770);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			brightnessBuffer.position(0);
			// float f1 = (float)(i >> 24 & 255) / 255.0F;
			float f2 = (i >> 16 & 255) / 255.0F;
			float f3 = (i >> 8 & 255) / 255.0F;
			float f4 = (i & 255) / 255.0F;
			brightnessBuffer.put(f2);
			brightnessBuffer.put(f3);
			brightnessBuffer.put(f4);
			brightnessBuffer.put(0.5f);
			brightnessBuffer.flip();
			GlStateManager.glTexEnv(8960, 8705, brightnessBuffer);
			GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
			GlStateManager.enableTexture2D();
			GlStateManager.bindTexture(TEXTURE_BRIGHTNESS.getGlTextureId());
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

			// ClientProxy.setColor(TF2Util.getTeamColor(event.getEntity()), 1f, 0f, 0.33f,
			// 1f);
		}
		boolean seeDisguise = WeaponsCapability.get(event.getEntity()).isDisguised()
				&& (!TF2Util.isAlliedTo(event.getEntity(), Minecraft.getInstance().player)
						|| Minecraft.getInstance().player == event.getEntity());
		if (event.getRenderer() != ClientProxy.disguiseRender && event.getRenderer() != ClientProxy.disguiseRenderPlayer
				&& event.getRenderer() != ClientProxy.disguiseRenderPlayerSmall
				&& event.getRenderer() != ClientProxy.forceTextureRenderPlayer
				&& (seeDisguise || usesForceTexture(event.getEntity()))) {
			float partialTicks = tickTime;
			String disguiseType = WeaponsCapability.get(event.getEntity()).getDisguiseType();
			RenderLivingBase<LivingEntity> render = null;
			Inventory origStack = null;
			ItemStack mainhanditem = null;
			if (seeDisguise && disguiseType.startsWith("M:")) {
				String mobType = disguiseType.substring(2);
				LivingEntity entToRender = event.getEntity().getCapability(TF2weapons.WEAPONS_CAP,
						null).entityDisguise;
				if (entToRender == null || !EntityList.getKey(entToRender).equals(new ResourceLocation(mobType))) {
					entToRender = event.getEntity().getCapability(TF2weapons.WEAPONS_CAP,
							null).entityDisguise = (LivingEntity) EntityList
									.createEntityByIDFromName(new ResourceLocation(mobType), event.getEntity().level);
					if (entToRender instanceof EntityTF2Character) {
						((EntityTF2Character) entToRender).setEntTeam(1 - TF2Util.getTeamForDisplay(event.getEntity()));
					}
					if (entToRender instanceof EntityBuilding)
						((EntityBuilding) entToRender).setEntTeam(1 - TF2Util.getTeamForDisplay(event.getEntity()));
					if (entToRender instanceof EntitySpy)
						entToRender.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks = 0;
				}
				if (entToRender != null) {
					entToRender.setPositionAndRotationDirect(event.getEntity().getX(), event.getEntity().getY(),
							event.getEntity().getZ(), event.getEntity().getYRot(), event.getEntity().getXRot(), 0,
							true);
					entToRender.yBodyRotO = event.getEntity().yBodyRotO;
					entToRender.setXRot((event.getEntity().getXRot()));
					entToRender.xRotO = event.getEntity().xRotO;
					entToRender.yRotO = event.getEntity().yRotO;
					entToRender.yHeadRot = event.getEntity().yHeadRot;
					entToRender.yHeadRotO = event.getEntity().yHeadRotO;
					entToRender.yBodyRot = event.getEntity().yBodyRot;
					entToRender.limbSwing = event.getEntity().limbSwing;
					entToRender.animationSpeed = event.getEntity().animationSpeed;
					entToRender.animationSpeedOld = event.getEntity().animationSpeedOld;
					entToRender.tickCount = event.getEntity().tickCount;
					entToRender.hurtTime = event.getEntity().hurtTime;

					Minecraft.getInstance().getEntityRenderDispatcher().renderEntity(entToRender, event.getX(), event.getY(),
							event.getZ(), event.getEntity().getYRot(), partialTicks, false);
					event.setCanceled(true);
				}
			} else if (event.getEntity() instanceof AbstractClientPlayer && (disguiseType.startsWith("P:")
					|| disguiseType.startsWith("T:") || usesForceTexture(event.getEntity()))) {
				AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
				if (seeDisguise && disguiseType.startsWith("P:")) {
					Player player2 = event.getEntity().level.getPlayerEntityByName(disguiseType.substring(2));

					if (player2 != null) {

						origStack = player.inventory;
						player.inventory = player2.inventory;
						/*
						 * for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
						 * origStack.put(slot,event.getEntity().getItemStackFromSlot(slot));
						 * event.getEntity().setItemStackToSlot(slot,
						 * player2.getItemStackFromSlot(slot)); }
						 */
					}

					if ("slim".equals(event.getEntity().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).skinType)) {
						render = ClientProxy.disguiseRenderPlayerSmall;
					} else {
						render = ClientProxy.disguiseRenderPlayer;
					}
				} else {
					if (seeDisguise && disguiseType.startsWith("T:")) {
						mainhanditem = player.getMainHandItem();
						player.getInventory().setInventorySlotContents(player.getInventory().selected,
								ItemFromData.getRandomWeaponOfSlotMob(TF2Class.getClass(disguiseType.substring(2)),
										player.getInventory().selected % 3, event.getEntity().getRandom(), false, 1f, true));
						render = ClientProxy.disguiseRenderPlayer;
					} else {
						render = ClientProxy.forceTextureRenderPlayer;
					}
				}
			}
			if (render != null) {
				render.doRender(event.getEntity(), event.getX(), event.getY(), event.getZ(),
						event.getEntity().getYRot(), partialTicks);
				event.setCanceled(true);

				if (origStack != null) {
					((Player) event.getEntity()).inventory = origStack;
				}
				if (mainhanditem != null)
					((Player) event.getEntity()).getInventory().setInventorySlotContents(
							((Player) event.getEntity()).getInventory().selected, mainhanditem);
			}
		}
	}

	public static boolean usesForceTexture(LivingEntity living) {
		return living instanceof Player && TF2PlayerCapability.get((Player) living).isForceClassTexture()
				&& WeaponsCapability.get(living).getUsedToken() >= 0;
	}

	@SubscribeEvent
	public void renderLivingPostEntity(RenderLivingEvent.Post<LivingEntity> event) {
		if (event.getRenderer().getEntityRenderDispatcher().isDebugBoundingBox() && !event.getEntity().isInvisible()
				&& !Minecraft.getInstance().isReducedDebug()) {
			GlStateManager.depthMask(false);
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.disableCull();
			GlStateManager.disableBlend();
			AABB head = TF2Util.getHead(event.getEntity()).move(-event.getEntity().getX(),
					-event.getEntity().getY(), -event.getEntity().getZ());
			RenderGlobal.drawBoundingBox(head.minX + event.getX(), head.minY + event.getY(), head.minZ + event.getZ(),
					head.maxX + event.getX(), head.maxY + event.getY(), head.maxZ + event.getZ(), 1.0F, 0.0F, 1.0F,
					1.0F);
			GlStateManager.enableTexture2D();
			GlStateManager.enableLighting();
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			GlStateManager.depthMask(true);
		}
		if (!(event.getEntity() instanceof Player || event.getEntity() instanceof EntityTF2Character))
			return;

		if (event.getEntity().getCapability(TF2weapons.INVENTORY_CAP).isPresent()) {
			InventoryWearables cap = event.getEntity().getCapability(TF2weapons.INVENTORY_CAP).orElse(null);
			if (!cap.origHead.isEmpty()) {
				if (event.getEntity() instanceof Player)
					((Player) event.getEntity()).getInventory().armor.set(3, cap.origHead);
				else
					event.getEntity().setItemSlot(EquipmentSlot.HEAD, cap.origHead);
				cap.origHead = ItemStack.EMPTY;
			}
		}
		MobEffectInstance bullet = event.getEntity().getEffect(TF2weapons.shieldBullet);
		MobEffectInstance explosive = event.getEntity().getEffect(TF2weapons.shieldExplosive);
		MobEffectInstance fire = event.getEntity().getEffect(TF2weapons.shieldFire);
		if (bullet != null || explosive != null || fire != null) {

			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableBlend();
			GlStateManager.disableLighting();

			float size = (event.getEntity().tickCount % 20) / 20f;
			float offset = 0f;
			if (TF2Util.getTeamForDisplay(event.getEntity()) != 0)
				offset = 0.375f;
			boolean hasShield = (bullet != null && bullet.getAmplifier() > 0)
					|| (explosive != null && explosive.getAmplifier() > 0) || (fire != null && fire.getAmplifier() > 0);
			GlStateManager.pushMatrix();
			GlStateManager.translate(event.getX(), event.getY() + event.getEntity().getBbHeight() + 0.5, event.getZ());
			Minecraft.getInstance().RenderSystem.setShaderTexture(0, ClientRender.VAC_EFFECT);
			if (bullet != null && (!hasShield || bullet.getAmplifier() > 0)) {
				ClientRender.renderIconWorld(0f + offset, 0f, 0.125f + offset, 1f, 0.6f, 0.6f);
			}
			if (explosive != null && (!hasShield || explosive.getAmplifier() > 0)) {
				ClientRender.renderIconWorld(0.125f + offset, 0f, 0.250f + offset, 1f, 0.6f, 0.6f);
			}
			if (fire != null && (!hasShield || fire.getAmplifier() > 0)) {
				ClientRender.renderIconWorld(0.25f + offset, 0f, 0.375f + offset, 1f, 0.6f, 0.6f);
			}
			GlStateManager.popMatrix();
			if (hasShield) {
				GlStateManager.pushMatrix();
				Minecraft.getInstance().RenderSystem.setShaderTexture(0, ClientRender.VAC_SHIELD);
				GlStateManager.translate(event.getX(), event.getY(), event.getZ());
				ClientProxy.setColor(TF2Util.getTeamColor(event.getEntity()), 1f, 0.2f, 0f, 1f);
				double width = event.getEntity().getBbWidth() * 0.5 + 0.25;
				ClientRender.renderAABBTexture(
						new AABB(-width, 0, -width, width, event.getEntity().getBbHeight() + 0.25, width), 0, 0, 1,
						1);
				GlStateManager.color(1f, 1f, 1f, 1f);
				GlStateManager.popMatrix();
			}
			GlStateManager.enableLighting();
			GlStateManager.disableBlend();
		}
		ClientProxy.renderCritGlow = 0;
		if (event.getEntity().getEffect(TF2weapons.uber) != null) {
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			GlStateManager.enableTexture2D();
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_ALPHA, OpenGlHelper.GL_PRIMARY_COLOR);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_ALPHA, 770);
			GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
			GlStateManager.disableTexture2D();
			GlStateManager.bindTexture(0);
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			// GlStateManager.color(1.0F, 1F, 1.0F, 1F);
		}
		// GlStateManager.enableLighting();
		if (event.getEntity().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks > 0) {
			GL11.glDisable(GL11.GL_BLEND);
			GlStateManager.color(1.0F, 1F, 1.0F, 1F);
		}
	}

	@SubscribeEvent
	public void addTooltip(ItemTooltipEvent event) {
		if (event.getItem().getItem() == TF2weapons.itemTF2) return;
		if (event.getItem().hasTag() && event.getItem().getTag().getBoolean("Australium")
				&& !(event.getItem().getItem() instanceof ItemFromData)
				&& !event.getItem().hasCustomHoverName()) {
			event.getToolTip().set(0, "Australium " + event.getToolTip().get(0));
		}
		if (event.getItem().hasTag() && event.getItem().getTag().getBoolean("Strange")) {
			if (!(event.getItem().getItem() instanceof ItemFromData) && !event.getItem().hasCustomHoverName()) {
				event.getToolTip().set(0,
						TF2EventsCommon.STRANGE_TITLES[event.getItem().getTag().getInt("StrangeLevel")]
								+ " " + event.getToolTip().get(0));
			}

			event.getToolTip().add("");
			if (event.getItem().getItem() instanceof ItemMedigun) {
				event.getToolTip()
						.add("Ubercharges: " + event.getItem().getTag().getInt("Ubercharges"));
			} else if (event.getItem().getItem() instanceof ItemCloak) {
				event.getToolTip()
						.add("Seconds cloaked: " + event.getItem().getTag().getInt("CloakTicks") / 20);
			} else {
				event.getToolTip().add("Mob kills: " + event.getItem().getTag().getInt("Kills"));
				event.getToolTip()
						.add("Player kills: " + event.getItem().getTag().getShort("PlayerKills"));
			}
		}

	}

	/*
	 * protected static boolean setBrightness(float partialTicks, boolean
	 * combineTextures) { float f = entitylivingbaseIn.getBrightness(); int i =
	 * this.getColorMultiplier(entitylivingbaseIn, f, partialTicks); boolean flag =
	 * (i >> 24 & 255) > 0; boolean flag1 = entitylivingbaseIn.hurtTime > 0 ||
	 * entitylivingbaseIn.deathTime > 0;
	 *
	 * if (!flag && !flag1) { return false; } else if (!flag && !combineTextures) {
	 * return false; } else {
	 * GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	 * GlStateManager.enableTexture2D(); GlStateManager.glTexEnvi(8960, 8704,
	 * OpenGlHelper.GL_COMBINE); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_COMBINE_RGB, 8448); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB,
	 * OpenGlHelper.GL_PRIMARY_COLOR); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_OPERAND0_RGB, 768); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_OPERAND1_RGB, 768); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_COMBINE_ALPHA, 7681); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
	 * GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	 * GlStateManager.enableTexture2D(); GlStateManager.glTexEnvi(8960, 8704,
	 * OpenGlHelper.GL_COMBINE); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB,
	 * OpenGlHelper.GL_CONSTANT); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE2_RGB,
	 * OpenGlHelper.GL_CONSTANT); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_OPERAND0_RGB, 768); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_OPERAND1_RGB, 768); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_OPERAND2_RGB, 770); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_COMBINE_ALPHA, 7681); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
	 * brightnessBuffer.position(0);
	 *
	 * if (flag1) { brightnessBuffer.put(1.0F); brightnessBuffer.put(0.0F);
	 * brightnessBuffer.put(0.0F); brightnessBuffer.put(0.5F); } else { float f1 =
	 * (float)(i >> 24 & 255) / 255.0F; float f2 = (float)(i >> 16 & 255) / 255.0F;
	 * float f3 = (float)(i >> 8 & 255) / 255.0F; float f4 = (float)(i & 255) /
	 * 255.0F; brightnessBuffer.put(f2); brightnessBuffer.put(f3);
	 * brightnessBuffer.put(f4); brightnessBuffer.put(1.0F - f1); }
	 *
	 * brightnessBuffer.flip(); GlStateManager.glTexEnv(8960, 8705,
	 * brightnessBuffer); GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
	 * GlStateManager.enableTexture2D();
	 * GlStateManager.bindTexture(TEXTURE_BRIGHTNESS.getGlTextureId());
	 * GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB,
	 * OpenGlHelper.GL_PREVIOUS); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA,
	 * OpenGlHelper.GL_PREVIOUS); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_OPERAND0_ALPHA, 770);
	 * GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit); return true; }
	 * }
	 *
	 * protected void unsetBrightness() {
	 * GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	 * GlStateManager.enableTexture2D(); GlStateManager.glTexEnvi(8960, 8704,
	 * OpenGlHelper.GL_COMBINE); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_COMBINE_RGB, 8448); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB,
	 * OpenGlHelper.GL_PRIMARY_COLOR); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_OPERAND0_RGB, 768); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_OPERAND1_RGB, 768); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_COMBINE_ALPHA, 8448); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_ALPHA,
	 * OpenGlHelper.GL_PRIMARY_COLOR); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_OPERAND0_ALPHA, 770); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_OPERAND1_ALPHA, 770);
	 * GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	 * GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB,
	 * OpenGlHelper.GL_PREVIOUS); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_COMBINE_ALPHA, 8448); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_OPERAND0_ALPHA, 770); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_SOURCE0_ALPHA, 5890); GlStateManager.color(1.0F, 1.0F, 1.0F,
	 * 1.0F); GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
	 * GlStateManager.disableTexture2D(); GlStateManager.bindTexture(0);
	 * GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
	 * GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB,
	 * OpenGlHelper.GL_PREVIOUS); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_COMBINE_ALPHA, 8448); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_OPERAND0_ALPHA, 770); GlStateManager.glTexEnvi(8960,
	 * OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
	 * GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit); }
	 */

}
