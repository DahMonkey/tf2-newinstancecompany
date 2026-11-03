package rafradek.TF2weapons.client;

import rafradek.TF2weapons.client.compat.GL11;
import rafradek.TF2weapons.client.compat.DefaultVertexFormats;
import rafradek.TF2weapons.client.compat.BufferBuilder;
import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.ScaledResolution;
import rafradek.TF2weapons.client.compat.LegacyRender;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.entity.EntityType;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import rafradek.TF2weapons.client.compat.LegacyGui;
import rafradek.TF2weapons.client.compat.RenderLivingBase;


import rafradek.TF2weapons.client.compat.ModelBiped;
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
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TF2EventsClient {
	private static boolean alreadypressed;
	private static boolean alreadypressedalt;
	private static boolean alreadypressedreload;
	public static boolean moveEntities;
	public static float tickTime = 0;
	public static TextureAtlasSprite[] pelletIcon;
	public static int ticksPressedReload;
	public static TextureAtlasSprite skinIcon;
	public static TextureAtlasSprite bisonIcon;
	public static Vec3 armLpos;
	public static Vec3 armRpos;
	public static Vec3 armLang;
	public static Vec3 armRang;

	public static void registerIcons(TextureStitchEvent.Pre event) {
		if (!event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS))
			return;
		for (int i = 0; i < 16; i++) {
			if (Minecraft.getInstance().getResourceManager().getResource(
					new ResourceLocation(TF2weapons.MOD_ID, "textures/items/tracer" + (i + 1) + ".png")).isPresent())
				event.addSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/tracer" + (i + 1)));
		}
		for (String name : new String[] { "items/bison", "misc/skin", "items/ammo_belt_empty", "items/refill_empty",
				"items/weapon_empty_0", "items/weapon_empty_1", "items/weapon_empty_2", "items/token_empty",
				"items/robot_part_1_1_empty", "items/robot_part_2_1_empty", "items/robot_part_3_1_empty",
				"items/robot_part_1_2_empty", "items/robot_part_2_2_empty", "items/robot_part_3_2_empty",
				"items/robot_part_1_3_empty" })
			event.addSprite(new ResourceLocation(TF2weapons.MOD_ID, name));
		for (int i = 0; i < ContainerWearables.CURRENCY_EMPTY.length; i++)
			event.addSprite(new ResourceLocation(ContainerWearables.CURRENCY_EMPTY[i]));
	}

	public static void iconsStitched(TextureStitchEvent.Post event) {
		if (!event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS))
			return;
		pelletIcon = new TextureAtlasSprite[16];
		for (int i = 0; i < 16; i++) {
			TextureAtlasSprite sprite = event.getAtlas()
					.getSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/tracer" + (i + 1)));
			if (!sprite.getName().equals(MissingTextureAtlasSprite.getLocation()))
				pelletIcon[i] = sprite;
		}
		bisonIcon = event.getAtlas().getSprite(new ResourceLocation(TF2weapons.MOD_ID, "items/bison"));
		skinIcon = event.getAtlas().getSprite(new ResourceLocation(TF2weapons.MOD_ID, "misc/skin"));
	}

	@SubscribeEvent
	public void keyJumpPress(InputEvent.Key event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen == null) {
			ItemStack stack = minecraft.player.getMainHandItem();
			if (minecraft.options.keyJump.consumeClick()) {
				ItemStack chest = ItemBackpack.getBackpack(minecraft.player);
				if (!minecraft.player.isOnGround() && WeaponsCapability.get(minecraft.player).airJumps < WeaponsCapability
						.get(minecraft.player).getMaxAirJumps()) {
					minecraft.player.jumpFromGround();
					float speedmult = minecraft.player.getSpeed() * (minecraft.player.isSprinting() ? 2 : 1);
					Vec3 moveDir = TF2Util.getMovementVector(minecraft.player);
					minecraft.player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).airJumps += 1;
					minecraft.player.setDeltaMovement((moveDir.x * speedmult), minecraft.player.getDeltaMovement().y, minecraft.player.getDeltaMovement().z);
					minecraft.player.setDeltaMovement(minecraft.player.getDeltaMovement().x, minecraft.player.getDeltaMovement().y, (moveDir.y * speedmult));
					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(23));
				} else if (WeaponsCapability.get(minecraft.player).isGrappled()) {
					minecraft.player.jumpFromGround();
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
					minecraft.player.connection.send(new ServerboundSetCarriedItemPacket(8));
					minecraft.player.getInventory().selected = 8;
					if (!TF2PlayerCapability.get(minecraft.player).isBackpackItemEquipped()) {
						TF2PlayerCapability.get(minecraft.player).setEquipBackpackItem(true);
						TF2weapons.network.sendToServer(new TF2Message.ActionMessage(26));
					}
				}
			}
			if (TF2PlayerCapability.get(minecraft.player).isBackpackItemEquipped()) {
				int sel = this.getPressedHotbarKey(minecraft.options.keyHotbarSlots, false);
				// KeyBinding.setKeyBindState(Keyboard.getEventKey() == 0 ?
				// Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey(), false);
				if (sel != -1) {
					TF2PlayerCapability.get(minecraft.player).setEquipBackpackItem(false);
					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(27));
				}
			}

			if (stack.getItem() instanceof IItemSlotNumber
					&& ((IItemSlotNumber) stack.getItem()).catchSlotHotkey(stack, minecraft.player)) {
				int sel = this.getPressedHotbarKey(minecraft.options.keyHotbarSlots, true);
				// KeyBinding.setKeyBindState(Keyboard.getEventKey() == 0 ?
				// Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey(), false);
				if (sel != -1) {
					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(sel + 100));
					((IItemSlotNumber) stack.getItem()).onSlotSelection(stack, minecraft.player, sel);
				}
			} else if (ClientProxy.reload.isDown()) {
				int sel = this.getPressedHotbarKey(minecraft.options.keyHotbarSlots, true);
				// KeyBinding.setKeyBindState(Keyboard.getEventKey() == 0 ?
				// Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey(), false);
				if (sel != -1)
					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(sel + 110));
			} else if (stack.getItem() instanceof IItemNoSwitch
					&& ((IItemNoSwitch) stack.getItem()).stopSlotSwitch(stack, minecraft.player))
				this.getPressedHotbarKey(minecraft.options.keyHotbarSlots, true);

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
					keyPressUpdate(minecraft.options.keyAttack.isDown(),
							minecraft.options.keyUse.isDown());
				}
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
	public void mouseScroll(InputEvent.MouseScrollingEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (event.getScrollDelta() == 0 || minecraft.screen != null || minecraft.player == null)
			return;
		ItemStack stack = minecraft.player.getMainHandItem();
		ItemStack backpack = ItemBackpack.getBackpack(minecraft.player);
		if (stack.getItem() instanceof IItemNoSwitch
				&& ((IItemNoSwitch) stack.getItem()).stopSlotSwitch(stack, minecraft.player))
			event.setCanceled(true);
		else if (!backpack.isEmpty()
				&& !((ItemBackpack) backpack.getItem()).getBackpackItemToUse(backpack, minecraft.player).isEmpty()) {
			if (TF2ConfigVars.mouseBackpackItemSwitch
					&& !TF2PlayerCapability.get(minecraft.player).isBackpackItemEquipped()
					&& ((minecraft.player.getInventory().selected == 0 && event.getScrollDelta() > 0)
							|| (minecraft.player.getInventory().selected == 8 && event.getScrollDelta() < 0))) {
				event.setCanceled(true);
				TF2PlayerCapability.get(minecraft.player).setEquipBackpackItem(true);
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(26));
			} else if (TF2PlayerCapability.get(minecraft.player).isBackpackItemEquipped()) {
				TF2PlayerCapability.get(minecraft.player).setEquipBackpackItem(false);
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(27));
			}
		}
	}


	public void keyPressUpdate(boolean attackKeyDown, boolean altAttackKeyDown) {
		keyPressUpdate(attackKeyDown, altAttackKeyDown, false);
	}

	public static void keyPressUpdate() {
		keyPressUpdate(Minecraft.getInstance().options.keyAttack.isDown(),
				Minecraft.getInstance().options.keyUse.isDown(), true);
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
			BlockPos pos = ((BlockHitResult) Minecraft.getInstance().hitResult).getBlockPos();
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
	public void getFov(ComputeFovModifierEvent event) {
		Player player = event.getPlayer();
		if (player.getMainHandItem().getItem() instanceof ItemUsable)
			if (player.getMainHandItem().getItem() instanceof ItemSniperRifle
					&& WeaponsCapability.get(player).isCharging()) {
				event.setNewFovModifier(event.getFovModifier() * 0.55f);
			} else if (player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ItemMinigun.slowdownUUID) != null) {
				event.setNewFovModifier(event.getNewFovModifier() * 1.4f);
			}
		int token = WeaponsCapability.get(player).getUsedToken();
		if (token >= 0) {
			event.setNewFovModifier(event.getNewFovModifier() - (ItemToken.SPEED_VALUES[token] / 2f));
		}
	}

	@SubscribeEvent
	public void blockDeathGui(ScreenEvent.Opening event) {
		if (event.getNewScreen() instanceof DeathScreen && Minecraft.getInstance().player != null
				&& WeaponsCapability.get(Minecraft.getInstance().player).isFeign()
				&& Minecraft.getInstance().player.getHealth() > 0f) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void guiPostInit(ScreenEvent.Init.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;
		Screen screen = event.getScreen();
		if ((screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen
				|| screen instanceof GuiWearables)
				&& !mc.player.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).isEmpty()) {
			event.addListener(new ImageButton(screen.width / 2 - 10, screen.height / 2 + 95, 18, 18, 238, 220, 18,
					GuiWearables.WEARABLES_TEXTURE, button -> {
						if (screen instanceof GuiWearables)
							mc.setScreen(new InventoryScreen(mc.player));
						else
							TF2weapons.network.sendToServer(new TF2Message.ShowGuiMessage(0));
					}));
		}

		if (screen instanceof MerchantScreen && screen.getTitle().getString().equals(I18n.get("entity.hale.name"))) {
			event.addListener(new Button(screen.width / 2 - 100, screen.height / 2 - 110, 100, 20,
					Component.literal("Leave Team"), button -> {
						TF2weapons.network.sendToServer(new TF2Message.ActionMessage(29));
						mc.player.closeContainer();
					}));
			event.addListener(new Button(screen.width / 2, screen.height / 2 - 110, 100, 20,
					Component.literal("Recover Lost Items"),
					button -> TF2weapons.network.sendToServer(new TF2Message.ActionMessage(18))));
		}
	}


	@SubscribeEvent
	public void applyRecoil(ViewportEvent.ComputeCameraAngles event) {
		Entity entity = event.getCamera().getEntity();
		if (entity != null && entity.getCapability(TF2weapons.WEAPONS_CAP).isPresent()) {
			WeaponsCapability cap = entity.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
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
	public void renderOverlay(RenderGuiOverlayEvent.Pre event) {
		LegacyRender.begin(event.getPoseStack(), null, LightTexture.FULL_BRIGHT);
		try {
			renderOverlay(event, new ScaledResolution());
		} finally {
			LegacyRender.end();
		}
	}

	private void renderOverlay(RenderGuiOverlayEvent.Pre event, ScaledResolution resolution) {
		Player player = Minecraft.getInstance().player;
		if (player == null)
			return;
		WeaponsCapability cap = player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
		ItemWeapon.inHand = event.getOverlay() != VanillaGuiOverlay.HOTBAR.type();
		ClientProxy.renderCritGlow = 0;
		GuiComponent gui = Minecraft.getInstance().gui;
		ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
		int width = resolution.getScaledWidth();
		int height = resolution.getScaledHeight();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder renderer = tessellator.getBuffer();
		Entity mouseTarget = Minecraft.getInstance().hitResult instanceof EntityHitResult
				? ((EntityHitResult) Minecraft.getInstance().hitResult).getEntity()
				: null;
		if (event.getOverlay() == VanillaGuiOverlay.HELMET.type()) {
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
				LegacyGui.drawStringNoShadow(Minecraft.getInstance().font, line1,
						resolution.getScaledWidth() - Minecraft.getInstance().font.width(line1), 50,
						0xFFFFFF);
				LegacyGui.drawStringNoShadow(Minecraft.getInstance().font, line2,
						resolution.getScaledWidth() - Minecraft.getInstance().font.width(line2), 65,
						0xFFFFFF);
			}
			if (ClientProxy.reload.isDown()) {

				int commands = 5;
				for (int i = 0; i < commands; i++) {
					String line = I18n.get("gui.command." + (i + 1));
					LegacyGui.drawStringNoShadow(Minecraft.getInstance().font, line, 5,
							(resolution.getScaledHeight() - commands * 15) / 2 + i * 15, 0xFFFFFF);
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
				GlStateManager.bindTexture(ClientProxy.VIGNETTE);
				BufferBuilder BufferBuilder = tessellator.getBuffer();
				BufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
				BufferBuilder.pos(0.0D, resolution.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
				BufferBuilder
						.pos(resolution.getScaledWidth(), resolution.getScaledHeight(), -90.0D)
						.tex(1.0D, 1.0D).endVertex();
				BufferBuilder.pos(resolution.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
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
		if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
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

				GlStateManager.bindTexture(ClientProxy.WIDGETS_TEXTURE_MC);
				int mid = width / 2;

				LegacyGui.drawTexturedModalRect(mid + 91 + 29, height - 23, 24, 22, 29, 24);

				RenderHelper.enableGUIStandardItemLighting();

				LegacyGui.renderItemAndEffectIntoGUI(player, toUse, mid + 91 + 32,
						height - 16 - 3);

				LegacyGui.renderItemOverlays(Minecraft.getInstance().font,
						toUse, mid + 91 + 32, height - 16 - 3);

				RenderHelper.disableStandardItemLighting();
				GlStateManager.enableBlend();
			}
			ItemStack pda = TF2Util.getFirstItem(Minecraft.getInstance().player.getInventory(),
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

				if (plcap.getSentryView().size() > 0)
					TF2EventsCommon.sentryView.readAdditionalSaveData(plcap.getSentryView());
				else
					TF2EventsCommon.sentryView.setHealth(0);

				GlStateManager.translate(4, 4, 0);
				GlStateManager.bindTexture(ClientProxy.buildingTexture);
				TF2EventsCommon.sentryView.renderGUI(renderer, tessellator, player, width, height,
						Minecraft.getInstance().gui);

				if (plcap.getDispenserView().size() > 0)
					TF2EventsCommon.dispenserView.readAdditionalSaveData(plcap.getDispenserView());
				else {
					TF2EventsCommon.dispenserView.setHealth(0);
				}

				GlStateManager.translate(0, 68, 0);
				GlStateManager.bindTexture(ClientProxy.buildingTexture);
				TF2EventsCommon.dispenserView.renderGUI(renderer, tessellator, player, width, height,
						Minecraft.getInstance().gui);

				if (plcap.getTeleporterAView().size() > 0)
					TF2EventsCommon.teleporterAView.readAdditionalSaveData(plcap.getTeleporterAView());
				else
					TF2EventsCommon.teleporterAView.setHealth(0);

				GlStateManager.translate(0, 52, 0);
				GlStateManager.bindTexture(ClientProxy.buildingTexture);
				TF2EventsCommon.teleporterAView.renderGUI(renderer, tessellator, player, width, height,
						Minecraft.getInstance().gui);

				if (plcap.getTeleporterBView().size() > 0)
					TF2EventsCommon.teleporterBView.readAdditionalSaveData(plcap.getTeleporterBView());
				else
					TF2EventsCommon.teleporterBView.setHealth(0);

				GlStateManager.translate(0, 52, 0);
				GlStateManager.bindTexture(ClientProxy.buildingTexture);
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
					GlStateManager.bindTexture(ClientProxy.healingTexture);

					// GL11.glDisable(GL11.GL_DEPTH_TEST);
					// GL11.glDepthMask(false);
					// OpenGlHelper.glBlendFunc(770, 771, 1, 0);
					// GL11.glDisable(GL11.GL_ALPHA_TEST);

					ClientProxy.setColor(TF2Util.getTeamColor(player), 0.7f, 0, 0.25f, 0.8f);

					renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
					renderer.pos(resolution.getScaledWidth() - 74,
							resolution.getScaledHeight() - 20, 0.0D).tex(0.0D, 1D).endVertex();
					renderer.pos(resolution.getScaledWidth() - 14,
							resolution.getScaledHeight() - 20, 0.0D).tex(0.01D, 1D).endVertex();
					renderer.pos(resolution.getScaledWidth() - 14,
							resolution.getScaledHeight() - 50, 0.0D).tex(0.01D, 0.99D).endVertex();
					renderer.pos(resolution.getScaledWidth() - 74,
							resolution.getScaledHeight() - 50, 0.0D).tex(0.0D, 0.99D).endVertex();
					tessellator.draw();

					GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
					renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
					renderer.pos(resolution.getScaledWidth() - 76,
							resolution.getScaledHeight() - 18, 0.0D).tex(0.5D, 0.265625D).endVertex();
					renderer.pos(resolution.getScaledWidth() - 12,
							resolution.getScaledHeight() - 18, 0.0D).tex(1.0D, 0.265625D).endVertex();
					renderer.pos(resolution.getScaledWidth() - 12,
							resolution.getScaledHeight() - 52, 0.0D).tex(1.0D, 0.53125D).endVertex();
					renderer.pos(resolution.getScaledWidth() - 76,
							resolution.getScaledHeight() - 52, 0.0D).tex(0.5D, 0.53125D).endVertex();
					tessellator.draw();
					String[] text = ((IItemOverlay) held.getItem()).getInfoBoxLines(held, player);
					LegacyGui.drawString(Minecraft.getInstance().font, text[0], resolution.getScaledWidth() - 66,
							resolution.getScaledHeight() - 48, 16777215);
					LegacyGui.drawString(Minecraft.getInstance().font, text[1], resolution.getScaledWidth() - 66,
							resolution.getScaledHeight() - 30, 16777215);
					// GL11.glDepthMask(true);
					// GL11.glEnable(GL11.GL_DEPTH_TEST);
					// GL11.glEnable(GL11.GL_ALPHA_TEST);
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				}
				((IItemOverlay) held.getItem()).drawOverlay(held, player, Tessellator.getInstance(),
						Tessellator.getInstance().getBuffer(), resolution);
			}
			if (player.getEffect(TF2weapons.it) != null) {
				// GL11.glDisable(GL11.GL_DEPTH_TEST);
				// GL11.glDepthMask(false);
				// OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				// GL11.glDisable(GL11.GL_ALPHA_TEST);
				LegacyGui.drawCenteredString(Minecraft.getInstance().font, I18n.get("gui.markedit"),
						resolution.getScaledWidth() / 2, resolution.getScaledHeight() / 4,
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
				LegacyGui.drawCenteredString(Minecraft.getInstance().font, I18n.get("gui.bombmrs"),
						resolution.getScaledWidth() / 2, resolution.getScaledHeight() / 4,
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

				GlStateManager.bindTexture(ClientProxy.healingTexture);
				// GL11.glDisable(GL11.GL_DEPTH_TEST);
				// GL11.glDepthMask(false);
				// OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				// GL11.glDisable(GL11.GL_ALPHA_TEST);

				GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
				// LegacyGui.drawTexturedModalRect(resolution.getScaledWidth()/2-64,
				// resolution.getScaledHeight()/2+35, 0, 0, 128, 40);
				setColorTeam(player, 0.7f);

				renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
				renderer.pos(resolution.getScaledWidth() / 2 - 62,
						resolution.getScaledHeight() / 2 + 70, 0.0D).tex(0.0D, 1D).endVertex();
				renderer.pos(resolution.getScaledWidth() / 2 + 62,
						resolution.getScaledHeight() / 2 + 70, 0.0D).tex(0.01D, 1D).endVertex();
				renderer.pos(resolution.getScaledWidth() / 2 + 62,
						resolution.getScaledHeight() / 2 + 40, 0.0D).tex(0.01D, 0.99D).endVertex();
				renderer.pos(resolution.getScaledWidth() / 2 - 62,
						resolution.getScaledHeight() / 2 + 40, 0.0D).tex(0.0D, 0.99D).endVertex();
				tessellator.draw();

				GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
				renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
				renderer.pos(resolution.getScaledWidth() / 2 - 64,
						resolution.getScaledHeight() / 2 + 72, 0.0D).tex(0.0D, 0.265625D).endVertex();
				renderer.pos(resolution.getScaledWidth() / 2 + 64,
						resolution.getScaledHeight() / 2 + 72, 0.0D).tex(1.0D, 0.265625D).endVertex();
				renderer.pos(resolution.getScaledWidth() / 2 + 64,
						resolution.getScaledHeight() / 2 + 38, 0.0D).tex(1.0D, 0.0D).endVertex();
				renderer.pos(resolution.getScaledWidth() / 2 - 64,
						resolution.getScaledHeight() / 2 + 38, 0.0D).tex(0.0D, 0.0D).endVertex();
				tessellator.draw();
				float overheal = 1f + living.getAbsorptionAmount() / living.getMaxHealth();
				if (overheal > 1f) {
					GlStateManager.color(1.0F, 1.0F, 1.0F, 0.4F);
					renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
					renderer.pos(resolution.getScaledWidth() / 2 - 47 - 10 * overheal,
							resolution.getScaledHeight() / 2 + 55 + 10 * overheal, 0.0D).tex(0.0D, 0.59375D)
							.endVertex();
					renderer.pos(resolution.getScaledWidth() / 2 - 47 + 10 * overheal,
							resolution.getScaledHeight() / 2 + 55 + 10 * overheal, 0.0D)
							.tex(0.28125D, 0.59375D).endVertex();
					renderer.pos(resolution.getScaledWidth() / 2 - 47 + 10 * overheal,
							resolution.getScaledHeight() / 2 + 55 - 10 * overheal, 0.0D)
							.tex(0.28125D, 0.3125D).endVertex();
					renderer.pos(resolution.getScaledWidth() / 2 - 47 - 10 * overheal,
							resolution.getScaledHeight() / 2 + 55 - 10 * overheal, 0.0D).tex(0.0D, 0.3125D)
							.endVertex();
					tessellator.draw();
				}
				GlStateManager.color(0.12F, 0.12F, 0.12F, 1F);
				renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
				renderer.pos(resolution.getScaledWidth() / 2 - 58.3,
						resolution.getScaledHeight() / 2 + 66.4, 0.0D).tex(0.0D, 0.59375D).endVertex();
				renderer.pos(resolution.getScaledWidth() / 2 - 35.7,
						resolution.getScaledHeight() / 2 + 66.4, 0.0D).tex(0.28125D, 0.59375D).endVertex();
				renderer.pos(resolution.getScaledWidth() / 2 - 35.7,
						resolution.getScaledHeight() / 2 + 43.6, 0.0D).tex(0.28125D, 0.3125D).endVertex();
				renderer.pos(resolution.getScaledWidth() / 2 - 58.3,
						resolution.getScaledHeight() / 2 + 43.6, 0.0D).tex(0.0D, 0.3125D).endVertex();
				tessellator.draw();
				float health = living.getHealth() / living.getMaxHealth();
				if (health > 0.33f) {
					GlStateManager.color(0.9F, 0.9F, 0.9F, 1F);
				} else {
					GlStateManager.color(0.85F, 0.0F, 0.0F, 1F);
				}
				int tf2health = Math.round((living.getHealth() / TF2ConfigVars.damageMultiplier) * overheal * 10);

				renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
				renderer.pos(resolution.getScaledWidth() / 2 - 57,
						resolution.getScaledHeight() / 2 + 65, 0.0D).tex(0.0D, 0.59375D).endVertex();
				renderer.pos(resolution.getScaledWidth() / 2 - 37,
						resolution.getScaledHeight() / 2 + 65, 0.0D).tex(0.28125D, 0.59375D).endVertex();
				renderer.pos(resolution.getScaledWidth() / 2 - 37,
						resolution.getScaledHeight() / 2 + 65 - health * 20, 0.0D)
						.tex(0.28125D, 0.59375D - 0.28125D * health).endVertex();
				renderer.pos(resolution.getScaledWidth() / 2 - 57,
						resolution.getScaledHeight() / 2 + 65 - health * 20, 0.0D)
						.tex(0.0D, 0.59375D - 0.28125D * health).endVertex();
				tessellator.draw();

				LegacyGui.drawStringNoShadow(Minecraft.getInstance().font, Integer.toString(tf2health),
						resolution.getScaledWidth() / 2 - 47
								- Minecraft.getInstance().font.width(Integer.toString(tf2health)) / 2,
						resolution.getScaledHeight() / 2 + 52, 0x101010);
				String text = "";
				if (player.level.getEntity(cap.getHealTarget()) != null) {
					text = "Healing:";
				} else if (living.getMainHandItem().getItem() instanceof ItemMedigun
						&& WeaponsCapability.get(living) != null) {
					text = "Ubercharge: " + Math.round(WeaponsCapability.get(living).getUberView() * 100f) + "%";
				}
				LegacyGui.drawString(Minecraft.getInstance().font, text, resolution.getScaledWidth() / 2 - 28,
						resolution.getScaledHeight() / 2 + 42, 16777215);
				LegacyGui.drawString(Minecraft.getInstance().font, living.getDisplayName().getString(),
						resolution.getScaledWidth() / 2 - 28,
						resolution.getScaledHeight() / 2 + 54, 16777215);

				// GL11.glEnable(GL11.GL_TEXTURE_2D);
				// GL11.glDepthMask(true);
				// GL11.glEnable(GL11.GL_DEPTH_TEST);
				// GL11.glEnable(GL11.GL_ALPHA_TEST);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}

			if (mouseTarget != null && mouseTarget instanceof EntityBuilding
					&& TF2Util.isAlliedTo(player, mouseTarget)) {
				GlStateManager.bindTexture(ClientProxy.buildingTexture);
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
	public void renderOverlayPost(RenderGuiOverlayEvent.Post event) {
		if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
			ItemWeapon.inHand = true;
		}
	}

	@SubscribeEvent
	public void renderPlayer(RenderPlayerEvent.Pre event) {
		Player player = event.getEntity();
		LegacyRender.begin(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
		try {
			if (player.getEffect(TF2weapons.bombmrs) != null) {
				ModelBomb modelBomb = new ModelBomb();
				GlStateManager.pushMatrix();
				GL11.glTranslatef(0, player.getEyeHeight(), 0);
				GL11.glRotatef(-(player.yRotO + (player.getYRot() - player.yRotO) * event.getPartialTick()), 0.0F,
						1.0F, 0.0F);
				GL11.glRotatef(player.xRotO + (player.getXRot() - player.xRotO) * event.getPartialTick(), 1.0F, 0.0F,
						0.0F);
				GL11.glScalef(1.1f, -1.1f, -1.1f);
				GlStateManager.bindTexture(RenderGrenade.TEXTURE_BOMB);
				modelBomb.render(player, 0F, 0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				GlStateManager.popMatrix();
			}
			if (player != Minecraft.getInstance().player) {
				renderBeam(player, event.getPartialTick(), 0.04f, Mth.lerp(event.getPartialTick(), player.xo, player.getX()),
						Mth.lerp(event.getPartialTick(), player.yo, player.getY()),
						Mth.lerp(event.getPartialTick(), player.zo, player.getZ()));
			}
		} finally {
			LegacyRender.end();
		}
	}

	@SubscribeEvent
	public void renderGui(ScreenEvent.Render.Pre event) {
		ClientProxy.renderCritGlow = 0;
		ItemWeapon.inHand = false;
	}

	@SubscribeEvent
	public void renderGui(ScreenEvent.Render.Post event) {
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
		if (WeaponsCapability.get(player).isInvisible() || (WeaponsCapability.get(player).isCharging()
				&& player.getMainHandItem().getItem() instanceof ItemSniperRifle)) {
			event.setCanceled(true);
		}
		if (event.getItemStack().getItem() instanceof ItemCloak && !WeaponsCapability.get(player).isFeign()
				&& ((ItemCloak) event.getItemStack().getItem()).isFeignDeath(event.getItemStack(), player)) {
			event.setCanceled(true);
		}
	}


	/**
	 * Draws the medigun beam from ent to its heal target. The current legacy pose must be at the camera
	 * (or entity) position given by originX/Y/Z.
	 */
	public static void renderBeam(LivingEntity ent, float partialTicks, float size, double originX, double originY,
			double originZ) {
		if (!ent.getCapability(TF2weapons.WEAPONS_CAP).isPresent())
			return;
		Entity healTarget = ent.level.getEntity(WeaponsCapability.get(ent).getHealTarget());
		if (healTarget != null) {
			double xPos1 = Mth.lerp(partialTicks, ent.xo, ent.getX());
			double yPos1 = Mth.lerp(partialTicks, ent.yo, ent.getY());
			double zPos1 = Mth.lerp(partialTicks, ent.zo, ent.getZ());
			double xPos2 = Mth.lerp(partialTicks, healTarget.xo, healTarget.getX());
			double yPos2 = Mth.lerp(partialTicks, healTarget.yo, healTarget.getY());
			double zPos2 = Mth.lerp(partialTicks, healTarget.zo, healTarget.getZ());
			double xDist = xPos2 - xPos1;
			double yDist = (yPos2 + (healTarget.getBoundingBox().maxY - healTarget.getBoundingBox().minY) / 2 + 0.1)
					- (yPos1 + ent.getEyeHeight() - 0.1);
			double zDist = zPos2 - zPos1;
			float f = (float) Math.sqrt(xDist * xDist + zDist * zDist);
			float fullDist = (float) Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder renderer = tessellator.getBuffer();
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) (xPos1 - originX), (float) (yPos1 + ent.getEyeHeight() - 0.1 - originY),
					(float) (zPos1 - originZ));
			GL11.glRotatef((float) (Math.atan2(xDist, zDist) * 180.0D / Math.PI), 0.0F, 1.0F, 0.0F);
			GL11.glRotatef((float) (Math.atan2(yDist, f) * 180.0D / Math.PI) * -1, 1.0F, 0.0F, 0.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.disableCull();
			ClientProxy.setColor(TF2Util.getTeamColor(ent), 0.23f, 0, 0f, 1f);
			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(-size, -size, 0).endVertex();
			renderer.pos(size, size, 0).endVertex();
			renderer.pos(size, size, fullDist).endVertex();
			renderer.pos(-size, -size, fullDist).endVertex();
			tessellator.draw();
			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(size, -size, 0).endVertex();
			renderer.pos(-size, size, 0).endVertex();
			renderer.pos(-size, size, fullDist).endVertex();
			renderer.pos(size, -size, fullDist).endVertex();
			tessellator.draw();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			GlStateManager.enableTexture2D();
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
					&& minecraft.player.getMainHandItem().getItem() instanceof ItemUsable) {
				minecraft.player.yHeadRot = minecraft.player.getYRot();
				moveEntities = true;
				keyPressUpdate(minecraft.options.keyAttack.isDown(), minecraft.options.keyUse.isDown());
				moveEntities = false;
			}
	}

	@SubscribeEvent
	public void renderWorld(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || Minecraft.getInstance().player == null)
			return;
		Vec3 cam = event.getCamera().getPosition();
		LegacyRender.begin(event.getPoseStack(), null, LightTexture.FULL_BRIGHT);
		try {
			GlStateManager.pushMatrix();
			GlStateManager.translate(-cam.x, -cam.y, -cam.z);
			float attackdir = TF2PlayerCapability.get(Minecraft.getInstance().player).getInvasionDir();
			if (attackdir != Float.MIN_VALUE) {
				ClientRender.renderAttackDirection(attackdir);
				ClientRender.renderAttackDirection(attackdir - 0.5f);
				ClientRender.renderAttackDirection(attackdir + 0.5f);
			}
			GlStateManager.popMatrix();
			renderBeam(Minecraft.getInstance().player, event.getPartialTick(), 0.04f, cam.x, cam.y, cam.z);
		} finally {
			LegacyRender.end();
		}
	}

	@SubscribeEvent
	public void playerName(PlayerEvent.NameFormat event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.level != null && WeaponsCapability.get(event.getEntity()).isDisguised()) {
			WeaponsCapability cap = WeaponsCapability.get(event.getEntity());
			String username = cap.getDisguiseType().substring(2);
			if (TF2Util.isAlliedTo(mc.player, event.getEntity())) {
				event.setDisplayname(event.getDisplayname().copy().append(" [" + username + "]"));
			} else if (cap.getDisguiseType().startsWith("M:")) {
				if (cap.entityDisguise != null)
					event.setDisplayname(cap.entityDisguise.getName());
				else
					event.setDisplayname(Component.translatable("entity." + username.replace(':', '.')));
			} else if (cap.getDisguiseType().startsWith("P:")) {
				event.setDisplayname(PlayerTeam.formatNameForTeam(mc.level.getScoreboard().getPlayersTeam(username),
						Component.literal(username)));
			} else {
				Player player = mc.level.players().stream().filter(p -> !TF2Util.isAlliedTo(mc.player, p)).findFirst()
						.orElse(null);
				if (player != null)
					event.setDisplayname(PlayerTeam.formatNameForTeam(player.getTeam(), player.getName()));
			}
		}
	}

	@SubscribeEvent
	public void renderLivingEntity(RenderLivingEvent.Pre<?, ?> event) {
		LivingEntity entity = event.getEntity();
		if (!entity.isAlive())
			return;

		ClientProxy.renderCritGlow = 0;

		if (!(entity instanceof Player || entity instanceof EntityTF2Character))
			return;

		if (!entity.getMainHandItem().isEmpty()) {
			ClientProxy.renderCritGlow = TF2Util.calculateCritPre(entity.getMainHandItem(), entity) * 16
					+ TF2Util.getTeamColorNumber(entity);
		}

		if (entity.getCapability(TF2weapons.INVENTORY_CAP).isPresent()) {
			InventoryWearables cap = entity.getCapability(TF2weapons.INVENTORY_CAP).orElse(null);
			for (int i = 0; i < cap.getContainerSize(); i++) {
				ItemStack hat = cap.getStackInSlot(i);
				if (hat.getItem() instanceof ItemFromData
						&& (((ItemFromData) hat.getItem()).getVisibilityFlags(hat, entity) & 1) == 1) {
					cap.origHead = entity.getItemBySlot(EquipmentSlot.HEAD);
					if (entity instanceof Player)
						((Player) entity).getInventory().armor.set(3, ItemStack.EMPTY);
					else
						entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
					break;
				}
			}
		}
		int visTick = WeaponsCapability.get(entity).invisTicks;
		if (visTick > 0) {
			if (Minecraft.getInstance().player != null && TF2Util.isAlliedTo(entity, Minecraft.getInstance().player))
				visTick = 8;
			// 1.19's entity renderers have no global alpha; cloaked spies vanish once mostly invisible.
			if (visTick >= 10) {
				event.setCanceled(true);
				restoreHead(entity);
				return;
			}
		}

		LegacyRender.begin(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
		try {
			if (entity.getEffect(TF2weapons.buffbanner) != null || entity.getEffect(TF2weapons.backup) != null
					|| entity.getEffect(TF2weapons.conch) != null || entity.getEffect(TF2weapons.quickFix) != null) {
				GlStateManager.enableBlend();
				GlStateManager.disableCull();
				GlStateManager.bindTexture(ClientProxy.circleTexture);
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				float size = (entity.tickCount % 20) / 20f;
				ClientProxy.setColor(TF2Util.getTeamColor(entity), size, 0.2f, 0f, 1f);
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
				bufferbuilder.pos(-0.7 * size, 0.1, 0.7 * size).tex(0, 1).endVertex();
				bufferbuilder.pos(0.7 * size, 0.1, 0.7 * size).tex(1, 1).endVertex();
				bufferbuilder.pos(0.7 * size, 0.1, -0.7 * size).tex(1, 0).endVertex();
				bufferbuilder.pos(-0.7 * size, 0.1, -0.7 * size).tex(0, 0).endVertex();
				tessellator.draw();
				GlStateManager.color(1f, 1f, 1f, 1f);
				GlStateManager.enableCull();
				GlStateManager.disableBlend();
			}
		} finally {
			LegacyRender.end();
		}

		boolean seeDisguise = WeaponsCapability.get(entity).isDisguised()
				&& (!TF2Util.isAlliedTo(entity, Minecraft.getInstance().player)
						|| Minecraft.getInstance().player == entity);
		if (event.getRenderer() == ClientProxy.disguiseRenderPlayer
				|| event.getRenderer() == ClientProxy.disguiseRenderPlayerSmall
				|| event.getRenderer() == ClientProxy.forceTextureRenderPlayer
				|| !(seeDisguise || usesForceTexture(entity)))
			return;

		float partialTicks = event.getPartialTick();
		String disguiseType = WeaponsCapability.get(entity).getDisguiseType();
		if (seeDisguise && disguiseType.startsWith("M:")) {
			String mobType = disguiseType.substring(2);
			WeaponsCapability cap = WeaponsCapability.get(entity);
			LivingEntity entToRender = cap.entityDisguise;
			if (entToRender == null || !EntityType.getKey(entToRender.getType()).equals(new ResourceLocation(mobType))) {
				Entity created = EntityType.byString(mobType).map(type -> type.create(entity.level)).orElse(null);
				entToRender = cap.entityDisguise = created instanceof LivingEntity ? (LivingEntity) created : null;
				if (entToRender instanceof EntityTF2Character)
					((EntityTF2Character) entToRender).setEntTeam(1 - TF2Util.getTeamForDisplay(entity));
				if (entToRender instanceof EntityBuilding)
					((EntityBuilding) entToRender).setEntTeam(1 - TF2Util.getTeamForDisplay(entity));
				if (entToRender instanceof EntitySpy)
					WeaponsCapability.get(entToRender).invisTicks = 0;
			}
			if (entToRender != null) {
				entToRender.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
				entToRender.xo = entity.xo;
				entToRender.yo = entity.yo;
				entToRender.zo = entity.zo;
				entToRender.yBodyRotO = entity.yBodyRotO;
				entToRender.xRotO = entity.xRotO;
				entToRender.yRotO = entity.yRotO;
				entToRender.yHeadRot = entity.yHeadRot;
				entToRender.yHeadRotO = entity.yHeadRotO;
				entToRender.yBodyRot = entity.yBodyRot;
				entToRender.animationPosition = entity.animationPosition;
				entToRender.animationSpeed = entity.animationSpeed;
				entToRender.animationSpeedOld = entity.animationSpeedOld;
				entToRender.tickCount = entity.tickCount;
				entToRender.hurtTime = entity.hurtTime;
				restoreHead(entity);
				Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entToRender).render(entToRender,
						Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()), partialTicks, event.getPoseStack(),
						event.getMultiBufferSource(), event.getPackedLight());
				event.setCanceled(true);
			}
		} else if (entity instanceof AbstractClientPlayer) {
			AbstractClientPlayer player = (AbstractClientPlayer) entity;
			net.minecraft.client.renderer.entity.player.PlayerRenderer render = null;
			ItemStack mainhanditem = null;
			if (seeDisguise && disguiseType.startsWith("P:")) {
				render = "slim".equals(WeaponsCapability.get(entity).skinType) ? ClientProxy.disguiseRenderPlayerSmall
						: ClientProxy.disguiseRenderPlayer;
			} else if (seeDisguise && disguiseType.startsWith("T:")) {
				mainhanditem = player.getMainHandItem();
				player.getInventory().setItem(player.getInventory().selected,
						ItemFromData.getRandomWeaponOfSlotMob(TF2Class.getClass(disguiseType.substring(2)),
								player.getInventory().selected % 3, entity.getRandom(), false, 1f, true));
				render = ClientProxy.disguiseRenderPlayer;
			} else if (usesForceTexture(entity)) {
				render = ClientProxy.forceTextureRenderPlayer;
			}
			if (render != null) {
				restoreHead(entity);
				render.render(player, Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()), partialTicks,
						event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
				event.setCanceled(true);
				if (mainhanditem != null)
					player.getInventory().setItem(player.getInventory().selected, mainhanditem);
			}
		}
	}

	/** Puts back a helmet hidden for hat rendering when the Post event will not run. */
	private static void restoreHead(LivingEntity entity) {
		entity.getCapability(TF2weapons.INVENTORY_CAP).ifPresent(cap -> {
			if (!cap.origHead.isEmpty()) {
				if (entity instanceof Player)
					((Player) entity).getInventory().armor.set(3, cap.origHead);
				else
					entity.setItemSlot(EquipmentSlot.HEAD, cap.origHead);
				cap.origHead = ItemStack.EMPTY;
			}
		});
	}

	public static boolean usesForceTexture(LivingEntity living) {
		return living instanceof Player && TF2PlayerCapability.get((Player) living).isForceClassTexture()
				&& WeaponsCapability.get(living).getUsedToken() >= 0;
	}

	@SubscribeEvent
	public void renderLivingPostEntity(RenderLivingEvent.Post<?, ?> event) {
		LivingEntity entity = event.getEntity();
		if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() && !entity.isInvisible()
				&& !Minecraft.getInstance().showOnlyReducedInfo()) {
			AABB head = TF2Util.getHead(entity).move(-entity.getX(), -entity.getY(), -entity.getZ());
			LevelRenderer.renderLineBox(event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.lines()),
					head, 1.0F, 0.0F, 1.0F, 1.0F);
		}
		if (!(entity instanceof Player || entity instanceof EntityTF2Character))
			return;

		restoreHead(entity);
		MobEffectInstance bullet = entity.getEffect(TF2weapons.shieldBullet);
		MobEffectInstance explosive = entity.getEffect(TF2weapons.shieldExplosive);
		MobEffectInstance fire = entity.getEffect(TF2weapons.shieldFire);
		if (bullet != null || explosive != null || fire != null) {
			LegacyRender.begin(event.getPoseStack(), event.getMultiBufferSource(), LightTexture.FULL_BRIGHT);
			try {
				GlStateManager.enableBlend();
				float offset = 0f;
				if (TF2Util.getTeamForDisplay(entity) != 0)
					offset = 0.375f;
				boolean hasShield = (bullet != null && bullet.getAmplifier() > 0)
						|| (explosive != null && explosive.getAmplifier() > 0)
						|| (fire != null && fire.getAmplifier() > 0);
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, entity.getBbHeight() + 0.5, 0);
				GlStateManager.bindTexture(ClientRender.VAC_EFFECT);
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
					GlStateManager.bindTexture(ClientRender.VAC_SHIELD);
					ClientProxy.setColor(TF2Util.getTeamColor(entity), 1f, 0.2f, 0f, 1f);
					double width = entity.getBbWidth() * 0.5 + 0.25;
					ClientRender.renderAABBTexture(
							new AABB(-width, 0, -width, width, entity.getBbHeight() + 0.25, width), 0, 0, 1, 1);
					GlStateManager.color(1f, 1f, 1f, 1f);
				}
				GlStateManager.disableBlend();
			} finally {
				LegacyRender.end();
			}
		}
		ClientProxy.renderCritGlow = 0;
	}

	@SubscribeEvent
	public void addTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (stack.getItem() == TF2weapons.itemTF2 || event.getToolTip().isEmpty())
			return;
		if (stack.hasTag() && stack.getTag().getBoolean("Australium") && !(stack.getItem() instanceof ItemFromData)
				&& !stack.hasCustomHoverName()) {
			event.getToolTip().set(0, Component.literal("Australium ").append(event.getToolTip().get(0)));
		}
		if (stack.hasTag() && stack.getTag().getBoolean("Strange")) {
			if (!(stack.getItem() instanceof ItemFromData) && !stack.hasCustomHoverName()) {
				event.getToolTip().set(0,
						Component.literal(TF2EventsCommon.STRANGE_TITLES[stack.getTag().getInt("StrangeLevel")] + " ")
								.append(event.getToolTip().get(0)));
			}
			event.getToolTip().add(Component.empty());
			if (stack.getItem() instanceof ItemMedigun) {
				event.getToolTip().add(Component.literal("Ubercharges: " + stack.getTag().getInt("Ubercharges")));
			} else if (stack.getItem() instanceof ItemCloak) {
				event.getToolTip().add(Component.literal("Seconds cloaked: " + stack.getTag().getInt("CloakTicks") / 20));
			} else {
				event.getToolTip().add(Component.literal("Mob kills: " + stack.getTag().getInt("Kills")));
				event.getToolTip().add(Component.literal("Player kills: " + stack.getTag().getShort("PlayerKills")));
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
