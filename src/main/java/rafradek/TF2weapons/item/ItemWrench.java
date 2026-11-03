package rafradek.TF2weapons.item;

import rafradek.TF2weapons.client.compat.ScaledResolution;
import rafradek.TF2weapons.client.compat.LegacyGui;
import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;

import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.GL11;
import rafradek.TF2weapons.client.compat.OpenGlHelper;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.entity.building.EntityTeleporter.TeleporterData;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class ItemWrench extends ItemMeleeWeapon implements IItemSlotNumber {

	/*
	 * @Override public void addInformation(ItemStack par1ItemStack, EntityPlayer
	 * par2EntityPlayer, List<String> par2List, boolean par4) {
	 * super.addInformation(par1ItemStack, par2EntityPlayer, par2List, par4);
	 * par2List.add("Metal: " + Integer.toString(200 -
	 * par1ItemStack.getItemDamage()) + "/200"); }
	 */

	@Override
	public boolean onHit(ItemStack stack, LivingEntity attacker, Entity target, float damage, int critical,
			boolean simulate) {
		// attacker.swingArm(EnumHand.MAIN_HAND);
		if (target instanceof EntityBuilding && TF2Util.isAlliedTo(target, attacker)) {
			EntityBuilding building = (EntityBuilding) target;

			if (building.isSapped())
				building.removeSapper();
			else if (building.isConstructing()) {
				building.wrenchBonusTime = 25;
				building.wrenchBonusMult = TF2Attribute.getModifier("Construction Rate", stack, 1, attacker);
				building.playSound(ItemFromData.getSound(stack, PropertyType.BUILD_HIT_SUCCESS_SOUND), 1.7f, 1f);
			} else {
				boolean useIgnot = false;
				int metalLeft = attacker.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getMetal();
				float metalMult = TF2Attribute.getModifier("Metal Used", stack, 1f, attacker);
				if ((attacker instanceof Player && ((Player) attacker).getAbilities().instabuild))
					metalMult = 10;

				ItemStack ingot = new ItemStack(Items.IRON_INGOT);
				if (metalLeft == 0 && attacker instanceof Player
						&& ((Player) attacker).getInventory().contains(ingot)) {
					metalLeft = 50;
					attacker.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setMetal(50);
					useIgnot = true;
				}
				int metalUse = 0;

				if (building.getHealth() < building.getMaxHealth()) {
					metalUse = (int) Math
							.min((Math.min((building.getMaxHealth() - building.getHealth()) * 3.333333f, 33 * metalMult)
									+ 1), metalLeft);
					building.heal(metalUse * 0.3f);
					metalLeft -= metalUse;
				}

				if (building instanceof EntitySentry) {
					metalUse = Math
							.min(Math.min(((EntitySentry) building).getMaxAmmo() - ((EntitySentry) building).getAmmo(),
									(int) (40 * metalMult)), metalLeft);
					((EntitySentry) building).setAmmo(Math.min(((EntitySentry) building).getMaxAmmo(),
							((EntitySentry) building).getAmmo() + metalUse));
					metalLeft -= metalUse;
					if (building.getLevel() == 3) {
						metalUse = Math.min(
								Math.min(20 - ((EntitySentry) building).getRocketAmmo(), (int) (8 * metalMult)),
								metalLeft / 2);
						((EntitySentry) building)
								.setRocketAmmo(Math.min(20, ((EntitySentry) building).getRocketAmmo() + metalUse));
						metalLeft -= metalUse * 2;
					}
				}
				if (building.getLevel() < building.getMaxLevel()) {
					metalUse = Math.min(
							Math.min(200 - building.getProgress(),
									(int) TF2Attribute.getModifier("Upgrade Rate", stack, 25 * metalMult, attacker)),
							metalLeft);
					float teleUpgradeRate = building instanceof EntityTeleporter
							? TF2Attribute.getModifier("Teleporter Cost", stack, 1f, attacker)
							: 1;
					building.setProgress(Math.min(building.getProgress() + (int) (metalUse * teleUpgradeRate), 200));
					metalLeft -= metalUse;
					if (building.getProgress() >= 200)
						building.upgrade();
				}

				building.playSound(ItemFromData.getSound(stack,
						metalLeft != attacker.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getMetal()
								? PropertyType.BUILD_HIT_SUCCESS_SOUND
								: PropertyType.BUILD_HIT_FAIL_SOUND),
						1.7f, 1f);
				// System.out.println("metal: "+TF2weapons.getMetal(attacker)+"
				// used: "+metalLeft);
				if (useIgnot && metalLeft != 50)
					((Player) attacker).getInventory().clearMatchingItems(Items.IRON_INGOT, 0, 1, null);

				if (!(attacker instanceof Player && ((Player) attacker).getAbilities().instabuild))
					attacker.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setMetal(metalLeft);
			}

			return false;
		}
		if (target instanceof EntityTF2Character && TF2Util.isAlliedTo(target, attacker)
				&& ((EntityTF2Character) target).isRobot()) {
			EntityTF2Character robot = (EntityTF2Character) target;
			if (robot.getEffect(TF2weapons.sapped) != null)
				robot.removeActivePotionEffect(TF2weapons.sapped);
			else {
				boolean useIgnot = false;
				int metalLeft = attacker.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getMetal();
				float metalMult = TF2Attribute.getModifier("Metal Used", stack, 1f, attacker);
				if ((attacker instanceof Player && ((Player) attacker).getAbilities().instabuild))
					metalMult = 10;

				ItemStack ingot = new ItemStack(Items.IRON_INGOT);
				if (metalLeft == 0 && attacker instanceof Player
						&& ((Player) attacker).getInventory().contains(ingot)) {
					metalLeft = 50;
					attacker.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setMetal(50);
					useIgnot = true;
				}
				int metalUse = 0;

				if (robot.getHealth() < robot.getMaxHealth()) {
					metalUse = (int) Math.min(
							(Math.min((robot.getMaxHealth() - robot.getHealth()) * 3.333333f, 33 * metalMult) + 1),
							metalLeft);
					robot.heal(metalUse * 0.3f);
					metalLeft -= metalUse;
				}

				if (useIgnot && metalLeft != 50)
					((Player) attacker).getInventory().clearMatchingItems(Items.IRON_INGOT, 0, 1, null);

				robot.playSound(ItemFromData.getSound(stack,
						metalLeft != attacker.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getMetal()
								? PropertyType.BUILD_HIT_SUCCESS_SOUND
								: PropertyType.BUILD_HIT_FAIL_SOUND),
						1.7f, 1f);

				if (!(attacker instanceof Player && ((Player) attacker).getAbilities().instabuild))
					attacker.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setMetal(metalLeft);
			}
			return false;
		}
		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player living, InteractionHand hand) {
		ItemStack stack = living.getItemInHand(hand);
		if (living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getMetal() >= 20
				&& TF2Attribute.getModifier("Weapon Mode", stack, 0, living) == 1) {
			living.startUsingItem(hand);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		}
		return super.use(world, living, hand);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 800;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	/*
	 * public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack
	 * newStack, boolean slotChanged) { return
	 * super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged); }
	 */
	@Override
	public boolean showInfoBox(ItemStack stack, Player player) {
		return true;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, Player player) {
		return new String[] { "METAL",
				Integer.toString(player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getMetal()) };
	}

	@Override
	public boolean catchSlotHotkey(ItemStack stack, Player player) {
		return ItemToken.allowUse(player, TF2Class.ENGINEER) && !player.getUseItem().isEmpty()
				&& player.getUseItemRemainingTicks() < 770;
	}

	@Override
	public void onSlotSelection(ItemStack stack, Player player, int slot) {
		if (!player.level.isClientSide && player.getUseItem().getItem() instanceof ItemWrench) {
			int dimension = 0;
			BlockPos pos = null;
			if (slot == 8) {
				dimension = player.dimension;
				pos = player.getBedLocation(player.dimension);
				if (pos == null) {
					pos = player.getBedLocation(0);
					dimension = 0;
				}
				if (pos != null)
					pos = Player.getBedSpawnLocation(TF2weapons.server.getWorld(dimension), pos,
							player.isSpawnForced(dimension));
				else
					pos = TF2weapons.server.getWorld(0).provider.getRandomizedSpawnPoint();
			} else if (EntityTeleporter.teleporters.containsKey(player.getUUID())) {
				TeleporterData[] data = EntityTeleporter.teleporters.get(player.getUUID());
				if (data[slot] != null) {
					dimension = data[slot].dimension;
					pos = data[slot];
				}
			}
			if (pos != null) {
				if (dimension != player.dimension)
					player.level.getServer().getPlayerList().transferPlayerToDimension((ServerPlayer) player,
							dimension, new EntityTeleporter.TeleporterDim((ServerLevel) player.level, pos));
				player.teleportTo(pos.getX() + 0.5, pos.getY() + 0.23, pos.getZ() + 0.5);
				player.getCooldowns().addCooldown(MapList.weaponClasses.get("wrench"), 200);
				TF2Util.playSound(player, TF2Sounds.MOB_TELEPORTER_SEND, 1.0F, 1.0F);
				player.stopUsingItem();
				player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).consumeMetal(20, false);
			}
		}
	}

	@Override
	public void drawOverlay(ItemStack stack, Player player, Tessellator tessellator, BufferBuilder renderer,
			ScaledResolution resolution) {
		if (player.getUseItem().getItem() instanceof ItemWrench && player.getUseItemRemainingTicks() < 770) {
			RenderSystem.setShaderTexture(0, ClientProxy.buildingTexture);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
			GuiComponent gui = Minecraft.getInstance().gui;
			Minecraft.getInstance().LegacyGui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 80,
					resolution.getScaledHeight() / 2 - 32, 64, 192, 64, 64);
			Minecraft.getInstance().LegacyGui.drawTexturedModalRect(resolution.getScaledWidth() / 2 + 16,
					resolution.getScaledHeight() / 2 - 32, 0, 192, 64, 64);

			LegacyGui.drawCenteredString(Minecraft.getInstance().font, "(1-8)", resolution.getScaledWidth() / 2 - 48,
					resolution.getScaledHeight() / 2 + 40, 0xFFFFFFFF);
			LegacyGui.drawCenteredString(Minecraft.getInstance().font, I18n.get("gui.selectlocation"),
					resolution.getScaledWidth() / 2, resolution.getScaledHeight() / 2 - 50, 0xFFFFFFFF);
			LegacyGui.drawCenteredString(Minecraft.getInstance().font, "(9)", resolution.getScaledWidth() / 2 + 48,
					resolution.getScaledHeight() / 2 + 40, 0xFFFFFFFF);

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}
