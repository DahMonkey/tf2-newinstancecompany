package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import rafradek.TF2weapons.util.PropertyType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemMeleeWeapon extends ItemBulletWeapon {

	@Override
	public float getMaxRange(ItemStack stack) {
		return TF2Attribute.getModifier("Range", stack, 2.4f, null);
	}

	public float getBulletSize() {
		return 0.35f;
	}

	@Override
	public boolean showTracer(ItemStack stack) {
		return false;
	}

	@Override
	public short getAltFiringSpeed(ItemStack item, LivingEntity player) {
		if (TF2Attribute.getModifier("Ball Release", item, 0, player) > 0) {
			if (player instanceof EntityTF2Character) {
				return (short) this.getFiringSpeed(getNewStack("sandmanball"), player);
			} else
				return 2000;
		}
		return super.getAltFiringSpeed(item, player);
	}

	@Override
	public boolean canAltFire(Level worldObj, LivingEntity player, ItemStack item) {
		return super.canAltFire(worldObj, player, item)
				&& !(player instanceof Player && ((Player) player).getCooldowns().isOnCooldown(this));
	}

	@Override
	public void altUse(ItemStack stack, LivingEntity living, Level world) {
		if (TF2Attribute.getModifier("Ball Release", stack, 0, living) > 0) {
			ItemStack ballStack = getNewStack("sandmanball");
			if (!this.searchForAmmo(living, ballStack).isEmpty()) {
				int cooldown = this.getFiringSpeed(ballStack, living) / 50;
				if (!TF2ConfigVars.fastItemCooldown)
					cooldown *= getData(ballStack).getFloat(PropertyType.COOLDOWN_LONG);
				if (living instanceof Player)
					((Player) living).getCooldowns().setCooldown(this, cooldown);
				ItemStack oldHeldItem = living.getMainHandItem();
				living.setItemInHand(InteractionHand.MAIN_HAND, ballStack);
				((ItemProjectileWeapon) ballStack.getItem()).use(ballStack, living, world, InteractionHand.MAIN_HAND, null);
				living.setItemInHand(InteractionHand.MAIN_HAND, oldHeldItem);
			}
		}
	}

	@Override
	public void draw(WeaponsCapability weaponsCapability, ItemStack stack, LivingEntity living, Level world) {
		super.draw(weaponsCapability, stack, living, world);
		if (living instanceof ServerPlayer)
			TF2weapons.network.sendTo(
					new TF2Message.UseMessage(this.getClip(stack), false,
							this.getAmmoAmount(living, getNewStack("sandmanball")), InteractionHand.MAIN_HAND),
					(ServerPlayer) living);
	}

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		ItemWeapon.shouldSwing = true;
		living.swing(hand);
		ItemWeapon.shouldSwing = false;
		return super.use(stack, living, world, hand, message);
	}

	@Override
	public float critChance(ItemStack stack, Entity entity) {
		float chance = 0.15f;
		if (entity instanceof Player)
			chance += TF2PlayerCapability.get((Player) entity).getTotalLastDamage() / 177f;
		return Math.min(chance, 0.6f);
	}

	@Override
	public boolean doMuzzleFlash(ItemStack stack, LivingEntity attacker, InteractionHand hand) {
		return false;
	}

	@Override
	public boolean showInfoBox(ItemStack stack, Player player) {
		return TF2Attribute.getModifier("Ball Release", stack, 0, player) > 0
				|| TF2Attribute.getModifier("Kill Count", stack, 0, player) > 0;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, Player player) {
		String[] result = new String[2];
		if (TF2Attribute.getModifier("Kill Count", stack, 0, player) > 0) {
			result[0] = "HEADS";
			result[1] = Integer.toString(player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getHeads());
		} else {
			result[0] = "BALLS";
			// ItemStack ballStack = getNewStack("sandmanball");
			int ammoLeft = player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).cachedAmmoCount[14];
			result[1] = Integer.toString(ammoLeft);
		}

		return result;
	}
}
