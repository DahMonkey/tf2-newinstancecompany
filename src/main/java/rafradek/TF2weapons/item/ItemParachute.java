package rafradek.TF2weapons.item;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class ItemParachute extends ItemBackpack {

	public ItemParachute() {
		this.setMaxDamage(1000);
		this.addPropertyOverride(new ResourceLocation("active"), (ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) -> {
			if (entityIn != null && stack.getTag().getBoolean("Deployed"))
				return 1;
			return 0;
		});
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity,
			java.util.function.Consumer<T> onBroken) {
		return amount * (stack.hasTag() && stack.getTag().getBoolean("Deployed") ? 8 : 1);
	}

	@Override
	public void onArmorTickAny(Level world, final LivingEntity player, ItemStack itemStack) {
		if (itemStack.getTag().getBoolean("Deployed")) {
			player.setDeltaMovement(player.getDeltaMovement().x, (Math.max(-0.1f, player.getDeltaMovement().y)), player.getDeltaMovement().z);
			player.fallDistance = 0f;
			/*
			 * if (player.ticksExisted % 30 == 0) { itemStack.damageItem(1, player); }
			 */
			if (player.isOnGround() || player.isEyeInFluid(net.minecraft.tags.FluidTags.WATER))
				itemStack.getTag().putBoolean("Deployed", false);
		}
	}
}
