package rafradek.TF2weapons.item;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class ItemParachute extends ItemBackpack implements ISpecialArmor {

	public ItemParachute() {
		this.setMaxDamage(1000);
		this.addPropertyOverride(new ResourceLocation("active"), (ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) -> {
			if (entityIn != null && stack.getTag().getBoolean("Deployed"))
				return 1;
			return 0;
		});
	}

	@Override
	public ArmorProperties getProperties(LivingEntity player, ItemStack armor, DamageSource source, double damage,
			int slot) {
		return new ArmorProperties(0, 0, Integer.MAX_VALUE);
	}

	@Override
	public int getArmorDisplay(Player player, ItemStack armor, int slot) {
		return 0;
	}

	@Override
	public void damageArmor(LivingEntity entity, ItemStack stack, DamageSource source, int damage, int slot) {
		stack.hurtAndBreak(damage * (stack.getTag().getBoolean("Deployed") ? 8 : 1), entity, broken -> {});
	}

	@Override
	public void onArmorTickAny(Level world, final LivingEntity player, ItemStack itemStack) {
		if (itemStack.getTag().getBoolean("Deployed")) {
			player.setDeltaMovement(player.getDeltaMovement().x, (Math.max(-0.1f, player.getDeltaMovement().y)), player.getDeltaMovement().z);
			player.fallDistance = 0f;
			/*
			 * if (player.ticksExisted % 30 == 0) { itemStack.damageItem(1, player); }
			 */
			if (player.isOnGround() || player.isEyeInFluid(Material.WATER))
				itemStack.getTag().putBoolean("Deployed", false);
		}
	}
}
