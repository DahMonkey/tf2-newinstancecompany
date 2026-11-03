package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemGas extends ItemProjectileWeapon {

	public ItemGas() {
		super();
		this.setMaxStackSize(64);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public CreativeModeTab getCreativeTab() {
		return TF2weapons.tabutilitytf2;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isBarVisible(ItemStack stack) {
		Integer value = rafradek.TF2weapons.client.ClientProxy.getLocalPlayer().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).effectsCool
				.get(getData(stack).getName());
		return value != null && value > 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBarWidth(ItemStack stack) {
		return Math.round(13f * (1f - (float) this.getDurabilityForDisplayLegacy(stack)));
	}

	public double getDurabilityForDisplayLegacy(ItemStack stack) {
		Integer value = rafradek.TF2weapons.client.ClientProxy.getLocalPlayer().getCapability(TF2weapons.WEAPONS_CAP).orElse(null).effectsCool
				.get(getData(stack).getName());
		return (double) (value != null ? value : 0) / (double) 1600;
	}

	@Override
	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {
		Integer value = living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).effectsCool.get(getData(stack).getName());
		return (value == null || value <= 0) && super.canFire(world, living, stack)
				&& !(living instanceof Player && ((Player) living).getCooldowns().isOnCooldown(this));
	}

	@Override
	public boolean doMuzzleFlash(ItemStack stack, LivingEntity attacker, InteractionHand hand) {
		return false;
	}

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		if (super.use(stack, living, world, hand, message) && !world.isClientSide) {
			if (living instanceof Player && !((Player) living).getAbilities().instabuild
					&& !TF2ConfigVars.freeUseItems)
				stack.shrink(1);
			if (living.getCapability(TF2weapons.WEAPONS_CAP).isPresent())
				WeaponsCapability.get(living).addEffectCooldown(getData(stack).getName(), 600);
			// ((EntityPlayer) living).getCooldownTracker().setCooldown(this, (int)
			// (this.getFiringSpeed(stack, living)/50 *
			// (TF2ConfigVars.fastItemCooldown ? 1f:
			// getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
		}
		return true;
	}
}
