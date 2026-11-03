package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.entity.EntityDummy;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import rafradek.TF2weapons.util.PropertyType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemCleaver extends ItemProjectileWeapon {

	public ItemCleaver() {
		super();
		this.setMaxStackSize(16);
	}

	@Override
	public boolean doMuzzleFlash(ItemStack stack, LivingEntity attacker, InteractionHand hand) {
		return false;
	}

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		if (super.use(stack, living, world, hand, message) && !world.isClientSide) {
			if (living instanceof Player)
				((Player) living).getCooldowns().setCooldown(this, (int) (this.getFiringSpeed(stack, living)
						/ 50
						* (TF2ConfigVars.fastItemCooldown ? 1f : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
			if (living instanceof Player && !((Player) living).getAbilities().instabuild
					&& !TF2ConfigVars.freeUseItems)
				stack.shrink(1);

		}
		return true;
	}

	@Override
	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {
		return super.canFire(world, living, stack)
				&& !(living instanceof Player && ((Player) living).getCooldowns().isOnCooldown(this));
	}

	@Override
	public boolean isProjectileInfinite(LivingEntity living, ItemStack stack) {
		return !(living instanceof EntityDummy) && (TF2ConfigVars.freeUseItems || !(living instanceof Player)
				|| ((Player) living).getAbilities().instabuild);
	}
}
