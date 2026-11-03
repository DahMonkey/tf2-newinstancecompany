package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemWrangler extends ItemUsable {

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		return true;
	}

	@Override
	public boolean fireTick(ItemStack stack, LivingEntity living, Level world) {
		EntitySentry sentry = living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).controlledSentry;
		if (sentry != null && sentry.isAlive())
			sentry.shootBullet(living);
		return false;
	}

	@Override
	public boolean altFireTick(ItemStack stack, LivingEntity living, Level world) {
		EntitySentry sentry = living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).controlledSentry;
		if (sentry != null && sentry.isAlive())
			sentry.shootRocket(living);
		return false;
	}

	@Override
	public void draw(WeaponsCapability weaponsCapability, ItemStack stack, final LivingEntity living, Level world) {
		super.draw(weaponsCapability, stack, living, world);
		if (!world.isClientSide) {
			weaponsCapability.controlledSentry = null;
			List<EntitySentry> list = world.getEntitiesOfClass(EntitySentry.class,
					living.getBoundingBox().inflate(128, 128, 128),
					input -> input.getOwner() == living && !input.isDisabled());
			Collections.sort(list, java.util.Comparator.comparingDouble(living::distanceToSqr));
			if (!list.isEmpty()) {
				list.get(0).setControlled(true);
				weaponsCapability.controlledSentry = list.get(0);
			}
		}
	}

	@Override
	public void holster(WeaponsCapability weaponsCapability, ItemStack stack, final LivingEntity living,
			Level world) {
		if (weaponsCapability.controlledSentry != null)
			weaponsCapability.controlledSentry.setControlled(false);
		weaponsCapability.controlledSentry = null;
		super.holster(weaponsCapability, stack, living, world);
	}
}
