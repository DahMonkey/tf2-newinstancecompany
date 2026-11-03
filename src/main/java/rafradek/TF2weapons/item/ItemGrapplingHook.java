package rafradek.TF2weapons.item;

import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.projectile.EntityGrapplingHook;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ItemGrapplingHook extends ItemProjectileWeapon {

	@Override
	public void onProjectileShoot(ItemStack stack, EntityProjectileBase proj, LivingEntity living, Level world,
			int thisCritical, InteractionHand hand) {

		if (proj instanceof EntityGrapplingHook && WeaponsCapability.get(living) != null) {
			WeaponsCapability.get(living).setGrapplingHook((EntityGrapplingHook) proj);
		}
	}

	@Override
	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {
		return super.canFire(world, living, stack) && WeaponsCapability.get(living) != null
				&& !WeaponsCapability.get(living).isGrappling()
				&& TF2Util.pierce(world, living, 256, false, 1f, false).get(0).getType() != HitResult.Type.MISS;
	}

	@Override
	public boolean endUse(ItemStack stack, LivingEntity living, Level world, int action, int newState) {
		boolean use = super.endUse(stack, living, world, action, newState);

		if ((newState & 1) == 0 && WeaponsCapability.get(living) != null
				&& WeaponsCapability.get(living).getGrapplingHook() != null
				&& WeaponsCapability.get(living).getGrapplingHook().stickedEntity == null) {
			WeaponsCapability.get(living).setGrapplingHook(null);
		}

		return use;
	}

	@Override
	public boolean startUse(ItemStack stack, LivingEntity living, Level world, int action, int newState) {
		boolean use = super.endUse(stack, living, world, action, newState);

		if ((newState & 1) == 1 && (action & 1) == 0 && WeaponsCapability.get(living) != null
				&& WeaponsCapability.get(living).getGrapplingHook() != null
				&& WeaponsCapability.get(living).getGrapplingHook().stickedEntity != null) {
			WeaponsCapability.get(living).setGrapplingHook(null);
		}

		return use;
	}
}
