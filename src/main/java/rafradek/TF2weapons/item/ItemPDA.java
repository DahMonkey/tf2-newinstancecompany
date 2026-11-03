package rafradek.TF2weapons.item;

import net.minecraft.world.item.context.UseOnContext;
import rafradek.TF2weapons.client.compat.ScaledResolution;
import rafradek.TF2weapons.client.compat.LegacyGui;
import net.minecraft.network.chat.Component;
import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;

import rafradek.TF2weapons.client.compat.GlStateManager;

import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.util.PlayerPersistStorage;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nullable;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemPDA extends ItemFromData implements IItemSlotNumber, IItemOverlay, IItemNoSwitch {

	@SuppressWarnings("unchecked")
	private static final EntityDataAccessor<CompoundTag>[] VIEWS = new EntityDataAccessor[] { TF2PlayerCapability.SENTRY_VIEW,
			TF2PlayerCapability.DISPENSER_VIEW, TF2PlayerCapability.TELEPORTERA_VIEW,
			TF2PlayerCapability.TELEPORTERB_VIEW };
	private static final String[] GUI_BUILD_NAMES = new String[] { "gui.build.sentry", "gui.build.dispenser",
			"gui.build.entrance", "gui.build.exit", "gui.build.disposable" };

	public ItemPDA() {
		this.setMaxStackSize(1);
		this.addPropertyOverride(new ResourceLocation("building"), new ItemPropertyFunction() {
			@Override
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) {
				if (!stack.hasTag() || (stack.getTag().getByte("Building") == 0)) {
					return 0f;
				} else {
					int building = stack.getTag().getByte("Building");
					if (building == 1 || building == 5) {
						return 0.33f;
					} else if (building == 2) {
						return 0.66f;
					} else {
						return 1f;
					}
				}
			}
		});
	}

	@Override
	public boolean catchSlotHotkey(ItemStack stack, Player player) {
		return ItemToken.allowUse(player, TF2Class.ENGINEER) && !stack.hasTag()
				|| stack.getTag().getByte("Building") == 0;
	}

	@Override
	public void onSlotSelection(ItemStack stack, Player player, int slot) {
		if (!player.level.isClientSide && TF2PlayerCapability.get(player).carrying == null && slot < 5) {
			if (slot == 4 && TF2PlayerCapability.get(player).calculateMaxSentries() <= 0)
				return;
			if (!PlayerPersistStorage.get(player).hasBuilding(slot)) {
				int metal = EntityBuilding.getCost(slot, TF2Util.getFirstItem(player.getInventory(),
						stackL -> (TF2Attribute.getModifier("Teleporter Cost", stackL, 1, player) != 1)));

				if (WeaponsCapability.get(player).hasMetal(metal)) {
					if (!stack.hasTag())
						stack.setTag(new CompoundTag());
					stack.getTag().putByte("Building", (byte) (slot + 1));
				}
			} else {
				PlayerPersistStorage.get(player).buildings[slot] = null;
			}
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entityIn, int itemSlot, boolean isSelected) {
		this.forceItemSlot(stack, world, entityIn, itemSlot, isSelected);
		if (!world.isClientSide) {

			if (!stack.hasTag())
				stack.setTag(new CompoundTag());
			PlayerPersistStorage storage = PlayerPersistStorage.get(((Player) entityIn));
			stack.getTag().putBoolean("IsC", TF2PlayerCapability.get((Player) entityIn).carrying != null);
			if (TF2PlayerCapability.get((Player) entityIn).carrying != null) {
				stack.getTag().putByte("Building",
						(byte) ((byte) TF2PlayerCapability.get((Player) entityIn).carryingType + 1));
			} else if (stack.getTag().getByte("Building") > 0) {
				int metal = EntityBuilding.getCost(stack.getTag().getByte("Building") - 1, TF2Util.getFirstItem(
						((Player) entityIn).inventory, stackL -> stackL.getItem() instanceof ItemWrench));
				if (!WeaponsCapability.get(entityIn).hasMetal(metal)
						|| storage.hasBuilding(stack.getTag().getByte("Building") - 1))
					stack.getTag().putByte("Building", (byte) 0);
			}

		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || oldStack.getItem() != newStack.getItem();

	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		InteractionHand hand = context.getHand();
		Direction facing = context.getClickedFace();
		float hitX = (float) (context.getClickLocation().x - pos.getX());
		float hitY = (float) (context.getClickLocation().y - pos.getY());
		float hitZ = (float) (context.getClickLocation().z - pos.getZ());

		ItemStack stack = player.getItemInHand(hand);
		if (world.isClientSide) {
			if ((!stack.hasTag() || stack.getTag().getByte("Building") == 0))
				return InteractionResult.PASS;
			else
				return InteractionResult.SUCCESS;
		} else if (!player.canPlayerEdit(pos.relative(facing), facing, stack) || !stack.hasTag()
				|| stack.getTag().getByte("Building") == 0)
			return InteractionResult.PASS;
		else {
			BlockState iblockstate = world.getBlockState(pos);

			pos = pos.relative(facing);
			double d0 = 0.0D;

			if (facing == Direction.UP && iblockstate.getBlock() instanceof FenceBlock)
				d0 = 0.5D;

			boolean disposable = stack.getTag().getByte("Building") == 5;

			int id = 16 + stack.getTag().getByte("Building") * 2;
			if (stack.getTag().getByte("Building") == 4)
				id -= 2;
			else if (disposable)
				id = 18;

			EntityBuilding entity = (EntityBuilding) ItemMonsterPlacerPlus.spawnCreature(player, world, id,
					pos.getX() + 0.5D, pos.getY() + d0, pos.getZ() + 0.5D, TF2PlayerCapability.get(player).carrying);

			if (entity != null) {

				entity.setEntTeam(TF2Util.getTeamForDisplay(player));
				entity.setOwner(player);
				if (entity instanceof EntitySentry) {
					((EntitySentry) entity).attackRateMult = TF2Attribute.getModifier("Sentry Fire Rate", stack, 1,
							player);
					TF2Util.addModifierSafe(entity, Attributes.FOLLOW_RANGE, new AttributeModifier(
							"upgraderange", TF2Attribute.getModifier("Sentry Range", stack, 1f, entity) - 1f, AttributeModifier.Operation.fromValue(2)), true);
					((EntitySentry) entity).setHeat((int) TF2Attribute.getModifier("Piercing", stack, 0, player));
					if (disposable || !TF2Util
							.getFirstItem(player.getInventory(),
									stackL -> stackL.getItem() instanceof ItemWrench
											&& TF2Attribute.getModifier("Weapon Mode", stackL, 0, player) == 2)
							.isEmpty()) {
						((EntitySentry) entity).setMini(true);
						if (entity.getLevel() > 1)
							entity.onDeath(DamageSource.GENERIC);
					}
				}
				if (TF2PlayerCapability.get(player).carrying != null) {
					entity.setConstructing(true);
					entity.redeploy = true;
				}
				TF2Util.addModifierSafe(entity, Attributes.MAX_HEALTH,
						new AttributeModifier(EntityBuilding.UPGRADE_HEALTH_UUID, "upgradehealth",
								TF2Attribute.getModifier("Building Health", stack, 1f, entity) - 1f, AttributeModifier.Operation.fromValue(2)),
						true);
				if (entity instanceof EntityDispenser) {
					((EntityDispenser) entity).setRange(TF2Attribute.getModifier("Dispenser Range", stack, 1, entity));
				}

				entity.setYRot((player.yHeadRot));
				entity.yBodyRot = player.yHeadRot;
				entity.yHeadRot = player.yHeadRot;
				entity.fromPDA = true;
				if (stack.getTag().getByte("Building") == 5 && entity.getDisposableID() == -1)
					entity.setDisposableID(PlayerPersistStorage.get(player).disposableBuildings.size());

				if (entity instanceof EntityTeleporter) {
					((EntityTeleporter) entity).setID(127);
					((EntityTeleporter) entity).setExit(stack.getTag().getByte("Building") == 4);
				}
				PlayerPersistStorage.get(player).setBuilding(entity,
						TF2PlayerCapability.get(player).calculateMaxSentries());
				TF2PlayerCapability.get(player).carrying = null;
				if (!player.getAbilities().instabuild && TF2PlayerCapability.get(player).carrying == null)
					WeaponsCapability.get(player).consumeMetal(EntityBuilding.getCost(
							stack.getTag().getByte("Building") - 1,
							TF2Util.getFirstItem(player.getInventory(), stackL -> stackL.getItem() instanceof ItemWrench)),
							false);
			}

			stack.getTag().putByte("Building", (byte) 0);
			return InteractionResult.SUCCESS;
		}
	}

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
	public InteractionResultHolder<ItemStack> use(Level world, Player living, InteractionHand hand) {
		if (living.isShiftKeyDown()) {
			living.getItemInHand(hand).getTag().putBoolean("ShowHud",
					!living.getItemInHand(hand).getTag().getBoolean("ShowHud"));
			System.out.println(living.getItemInHand(hand).getTag().getBoolean("ShowHud"));
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, living.getItemInHand(hand));
		}
		return new InteractionResultHolder<>(InteractionResult.FAIL, living.getItemInHand(hand));
	}

	@Override
	public boolean canSwitchTo(ItemStack stack) {
		return true;
	}

	@Override
	public void drawOverlay(ItemStack stack, Player player, Tessellator tessellator, BufferBuilder buffer,
			ScaledResolution resolution) {
		if (!stack.hasTag() || (stack.getTag().getByte("Building") == 0)) {
			TF2PlayerCapability plcap = TF2PlayerCapability.get(player);
			RenderSystem.setShaderTexture(0, ClientProxy.blueprintTexture);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
			GuiComponent gui = Minecraft.getInstance().gui;
			boolean hasTag = stack.hasTag();
			int buildCount = TF2Attribute.getModifier("Extra Sentry", stack, 0, player) > 0 ? 5 : 4;
			for (int i = 0; i < buildCount; i++) {
				int cost = EntityBuilding.getCost(i,
						TF2Util.getFirstItem(player.getInventory(), stackL -> stackL.getItem() instanceof ItemWrench));
				if (hasTag && i < 4 && plcap.entityData.get(VIEWS[i]).getSize() != 0) {
					LegacyGui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 140 + i * 72,
							resolution.getScaledHeight() / 2, 0, 64, 64, 64);
					LegacyGui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 132 + i * 72,
							resolution.getScaledHeight() / 2 + 12, 208, 64 + i * 48, 48, 48);
				} else if (WeaponsCapability.get(player).getMetal() >= cost) {
					// LegacyGui.drawString(Minecraft.getInstance().font,
					// Minecraft.getInstance().font.getStringWidth(Integer.toString(cost));
					LegacyGui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 140 + i * 72,
							resolution.getScaledHeight() / 2, i == 4 ? 0 : i * 64, 0, 64, 64);
				}
				/*
				 * else LegacyGui.drawTexturedModalRect(resolution.getScaledWidth()/2-140 + i * 72,
				 * resolution.getScaledHeight()/2, 0, 0, 64, 64);
				 */

			}
			for (int i = 0; i < buildCount; i++) {
				int cost = EntityBuilding.getCost(i,
						TF2Util.getFirstItem(player.getInventory(), stackL -> stackL.getItem() instanceof ItemWrench));
				LegacyGui.drawString(Minecraft.getInstance().font, Integer.toString(cost),
						resolution.getScaledWidth() / 2 - 72
								- Minecraft.getInstance().font.width(Integer.toString(cost)) + i * 72,
						resolution.getScaledHeight() / 2 - 8, 0xFFFFFFFF);
				LegacyGui.drawCenteredString(Minecraft.getInstance().font, "[" + (i + 1) + "]",
						resolution.getScaledWidth() / 2 - 108 + i * 72, resolution.getScaledHeight() / 2 + 72,
						0xFFFFFFFF);
				LegacyGui.drawString(Minecraft.getInstance().font, I18n.get(GUI_BUILD_NAMES[i]),
						resolution.getScaledWidth() / 2 - 140 + i * 72, resolution.getScaledHeight() / 2 - 18,
						0xFFFFFFFF);
				if (WeaponsCapability.get(player).getMetal() < cost
						&& !(hasTag && i < 4 && plcap.entityData.get(VIEWS[i]).getSize() != 0)) {
					Minecraft.getInstance().font.drawSplitString(I18n.get("gui.build.nometal"),
							resolution.getScaledWidth() / 2 - 140 + i * 72, resolution.getScaledHeight() / 2 + 20, 80,
							0xFFF00F0F);
				}

			}
			/*
			 * LegacyGui.drawTexturedModalRect(resolution.getScaledWidth()/2-68,
			 * resolution.getScaledHeight()/2-32, 64, 0, 64, 64);
			 * LegacyGui.drawTexturedModalRect(resolution.getScaledWidth()/2+4,
			 * resolution.getScaledHeight()/2-32, 128, 0, 64, 64);
			 * LegacyGui.drawTexturedModalRect(resolution.getScaledWidth()/2+72,
			 * resolution.getScaledHeight()/2-32, 192, 0, 64, 64);
			 * 
			 * LegacyGui.drawCenteredString(Minecraft.getInstance().font, "[2]",
			 * resolution.getScaledWidth()/2-36, resolution.getScaledHeight()/2+40,
			 * 0xFFFFFFFF); LegacyGui.drawCenteredString(Minecraft.getInstance().font, "[3]",
			 * resolution.getScaledWidth()/2+36, resolution.getScaledHeight()/2+40,
			 * 0xFFFFFFFF); LegacyGui.drawCenteredString(Minecraft.getInstance().font, "[4]",
			 * resolution.getScaledWidth()/2+108, resolution.getScaledHeight()/2+40,
			 * 0xFFFFFFFF); LegacyGui.drawCenteredString(Minecraft.getInstance().font,
			 * I18n.format("gui.build.sentry"), resolution.getScaledWidth()/2-108,
			 * resolution.getScaledHeight()/2-40, 0xFFFFFFFF);
			 * LegacyGui.drawCenteredString(Minecraft.getInstance().font,
			 * I18n.format("gui.build.dispenser"), resolution.getScaledWidth()/2-36,
			 * resolution.getScaledHeight()/2-40, 0xFFFFFFFF);
			 * LegacyGui.drawCenteredString(Minecraft.getInstance().font,
			 * I18n.format("gui.build.entrance"), resolution.getScaledWidth()/2+36,
			 * resolution.getScaledHeight()/2-40, 0xFFFFFFFF);
			 * LegacyGui.drawCenteredString(Minecraft.getInstance().font, I18n.format("gui.build.exit"),
			 * resolution.getScaledWidth()/2+108, resolution.getScaledHeight()/2-40,
			 * 0xFFFFFFFF);
			 */
			LegacyGui.drawCenteredString(Minecraft.getInstance().font, I18n.get("gui.build"), resolution.getScaledWidth() / 2,
					resolution.getScaledHeight() / 2 - 40, 0xFFFFFFFF);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}

	}

	@Override
	public boolean stopSlotSwitch(ItemStack stack, LivingEntity living) {
		return stack.getTag().getBoolean("IsC");
	}

}
