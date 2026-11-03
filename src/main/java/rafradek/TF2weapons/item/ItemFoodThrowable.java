package rafradek.TF2weapons.item;

import com.google.common.collect.Iterables;
import rafradek.TF2weapons.TF2ConfigVars;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemFoodThrowable extends ItemFood {

	public int waitTime;

	public ItemFoodThrowable(int amount, float saturation, boolean isWolfFood, int waitTime) {
		super(amount, saturation, isWolfFood);
		this.waitTime = waitTime;
		this.setAlwaysEdible();
	}

	@Override
	public boolean onEntityItemUpdate(ItemEntity entityItem) {
		if (entityItem.getItem().hasTag() && entityItem.getItem().getTag().getBoolean("IsHealing")) {
			LivingEntity living = Iterables.getFirst(entityItem.level.getEntitiesOfClass(LivingEntity.class,
					entityItem.getBoundingBox(), test -> (!(test instanceof Player)
							&& test.getHealth() < test.getMaxHealth() && test.canChangeDimensions() && test.isAlive())),
					null);

			if (living != null) {
				living.heal(living.getMaxHealth() * this.getHealAmount(entityItem.getItem()) / 28f);
				entityItem.getItem().shrink(1);
				if (entityItem.getItem().getTag().getBoolean("IsHealing"))
					entityItem.getItem().setTag(null);
			}
		}
		return false;
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, Player player) {

		if (!TF2ConfigVars.fastItemCooldown) {
			/*
			 * if (!player.getCooldownTracker().hasCooldown(this)) {
			 * player.getCooldownTracker().setCooldown(this, waitTime); if
			 * (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
			 * item.getTagCompound().setBoolean("IsHealing", true); }
			 */
		}
		return true;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, Level world, Player player) {
		super.onFoodEaten(stack, world, player);
		if (!world.isClientSide) {
			player.getCooldowns().setCooldown(this, TF2ConfigVars.fastItemCooldown ? waitTime / 2 : waitTime);
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn) {
		ItemStack previous = player.getItemInHand(handIn);
		InteractionResultHolder<ItemStack> result = super.use(world, player, handIn);
		if (TF2ConfigVars.freeUseItems)
			result.getResult().setCount(previous.getCount());
		return result;
	}
}
