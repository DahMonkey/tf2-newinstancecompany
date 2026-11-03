package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ItemJetpack extends ItemBackpack {

	public static ItemStack trigger = ItemStack.EMPTY;

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return stack.getTag().getShort("Charge") > 0;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return (double) (stack.getTag().getShort("Charge")) / this.getCooldown(stack, null);
	}

	@Override
	public ItemStack getBackpackItemToUse(ItemStack stack, LivingEntity player) {
		if (trigger.isEmpty())
			trigger = ItemFromData.getNewStack("trigger");
		return trigger;
	}

	@Override
	public void onArmorTickAny(Level world, LivingEntity player, ItemStack itemStack) {
		super.onArmorTickAny(world, player, itemStack);
		if (!world.isClientSide) {
			if (player.tickCount % 4 == 0 && itemStack.getTag().getShort("Charge") > 0
					&& this.getAmmoAmount(player, itemStack) >= this.getActualAmmoUse(itemStack, player, 1)) {
				itemStack.getTag().putShort("Charge",
						(short) (itemStack.getTag().getInt("Charge") - 4));
				if (player.tickCount % (4 * Math.round(this.getCooldown(itemStack, player) / 75f)) == 0)
					this.consumeAmmoGlobal(player, itemStack, this.getActualAmmoUse(itemStack, player, 1));
				if (itemStack.getTag().getShort("Charge") <= 0) {
					itemStack.getTag().putShort("Charges",
							(short) (itemStack.getTag().getShort("Charges") + 1));
				}
			}
			if (itemStack.getTag().getBoolean("Active")) {
				if (player.onGround) {
					TF2Util.playSound(player, getSound(itemStack, PropertyType.FIRE_STOP_SOUND), 1f, 1f);
					itemStack.getTag().putBoolean("Active", false);
				}
				TF2Util.stomp(player);
			}
			if (itemStack.getTag().getByte("Load") > 0) {
				itemStack.getTag().putByte("Load", (byte) (itemStack.getTag().getByte("Load") - 1));
				if (itemStack.getTag().getByte("Load") == 0) {
					itemStack.getTag().putByte("Charges",
							(byte) (itemStack.getTag().getByte("Charges") - 1));
					Vec3 vel = player.getViewVector(1);
					vel = vel.scale(TF2Attribute.getModifier("Self Push Force", itemStack, 1, player));
					WeaponsCapability.get(player).setExpJump(true);
					player.setDeltaMovement((vel.x), player.getDeltaMovement().y, player.getDeltaMovement().z);
					player.setDeltaMovement(player.getDeltaMovement().x, (vel.y + (player instanceof Player ? 0.1 : 0.25)), player.getDeltaMovement().z);
					player.setDeltaMovement(player.getDeltaMovement().x, player.getDeltaMovement().y, (vel.z));
					TF2Util.playSound(player, getSound(itemStack, PropertyType.FIRE_SOUND), 1f, 1f);
					itemStack.getTag().putBoolean("Active", true);
					if (player instanceof ServerPlayer)
						((ServerPlayer) player).connection.sendPacket(new ClientboundSetEntityMotionPacket(player));
				}
			}
			if (itemStack.getTag().getShort("Charge") == 0
					&& itemStack.getTag().getByte("Charges") < this.getMaxCharges(itemStack, player)) {
				itemStack.getTag().putShort("Charge", (short) this.getCooldown(itemStack, player));
			}
		} else if (itemStack.getTag().getBoolean("Active") || itemStack.getTag().getByte("Load") > 0) {
			Vec3 vec = TF2Util.getRotationVector(0, player.yBodyRot);
			Vec3 vec2 = TF2Util.getRotationVector(0, player.yBodyRot + 90);
			world.addParticle(ParticleTypes.SMOKE_LARGE, player.getX() - vec.x * 0.4 + vec2.x * 0.3,
					player.getY() + 1, player.getZ() - vec.z * 0.4 + vec2.z * 0.3, 0, 0, 0);
			world.addParticle(ParticleTypes.SMOKE_LARGE, player.getX() - vec.x * 0.4 - vec2.x * 0.3,
					player.getY() + 1, player.getZ() - vec.z * 0.4 - vec2.z * 0.3, 0, 0, 0);
		}
	}

	public int getCooldown(ItemStack stack, LivingEntity living) {
		return (int) (getData(stack).getInt(PropertyType.COOLDOWN)
				* (TF2ConfigVars.fastItemCooldown ? 1 : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))
				/ (TF2Attribute.getModifier("Charge", stack, 1, living)
						+ TF2Attribute.getModifier("Charges", stack, 0, living) * 0.12f));
	}

	@Override
	public int getAmmoType(ItemStack stack) {
		return TF2ConfigVars.freeUseItems ? 0 : super.getAmmoType(stack);
	}

	public boolean canActivate(ItemStack stack, LivingEntity player) {
		return ItemToken.allowUse(player, TF2Class.PYRO)
				&& (!stack.getTag().getBoolean("Active")
						|| TF2Attribute.getModifier("Jetpack", stack, 0f, player) > 0f)
				&& stack.getTag().getByte("Load") <= 0 && stack.getTag().getByte("Charges") > 0;
	}

	public int getMaxCharges(ItemStack stack, LivingEntity player) {
		return (int) TF2Attribute.getModifier("Charges", stack, 2, player);
	}

	public void activateJetpack(ItemStack stack, LivingEntity player, boolean setTimer) {
		player.setDeltaMovement(player.getDeltaMovement().x, (Math.max(player.getDeltaMovement().y, 0) + 0.5), player.getDeltaMovement().z);
		player.hasImpulse = true;
		player.fallDistance = 0;
		if (!player.level.isClientSide) {
			TF2Util.playSound(player, getSound(stack, PropertyType.CHARGE_SOUND), 1f, 1f);
			WeaponsCapability.get(player).setExpJump(true);
			stack.getTag().putByte("Load", (byte) 12);
			if (player instanceof ServerPlayer)
				TF2weapons.network.sendTo(new TF2Message.ActionMessage(30, player), (ServerPlayer) player);
		} else {
			for (int i = 0; i < 50; i++)
				ClientProxy.spawnFlameParticle(player.level, player, 0, true);
		}
		if (setTimer && !(player.getMainHandItem().getItem() instanceof ItemJetpackTrigger)) {
			WeaponsCapability.get(player).setPrimaryCooldown(1500);
			WeaponsCapability.get(player).setSecondaryCooldown(1500);
		}
	}
}
