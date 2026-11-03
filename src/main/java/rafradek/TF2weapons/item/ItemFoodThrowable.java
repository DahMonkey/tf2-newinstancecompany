package rafradek.TF2weapons.item;

import com.google.common.collect.Iterables;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import rafradek.TF2weapons.TF2ConfigVars;

/** Sandvich and chocolate: food that heals whoever picks it up when thrown. */
public class ItemFoodThrowable extends LegacyItem {

	public int waitTime;
	private final int healAmount;

	public ItemFoodThrowable(int amount, float saturation, boolean isWolfFood, int waitTime,
			java.util.function.Supplier<net.minecraft.world.effect.MobEffectInstance> effect, Properties properties) {
		super(properties.food(new FoodProperties.Builder().nutrition(amount).saturationMod(saturation).alwaysEat()
				.effect(effect, 1f).build()));
		this.waitTime = waitTime;
		this.healAmount = amount;
	}

	public int getHealAmount(ItemStack stack) {
		return this.healAmount;
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
		if (entityItem.getItem().hasTag() && entityItem.getItem().getTag().getBoolean("IsHealing")) {
			LivingEntity living = Iterables.getFirst(entityItem.level.getEntitiesOfClass(LivingEntity.class,
					entityItem.getBoundingBox(), test -> (!(test instanceof Player)
							&& test.getHealth() < test.getMaxHealth() && test.canChangeDimensions() && test.isAlive())),
					null);

			if (living != null) {
				living.heal(living.getMaxHealth() * this.getHealAmount(entityItem.getItem()) / 28f);
				entityItem.getItem().shrink(1);
				if (entityItem.getItem().hasTag() && entityItem.getItem().getTag().getBoolean("IsHealing"))
					entityItem.getItem().setTag(null);
			}
		}
		return false;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity living) {
		int count = stack.getCount();
		ItemStack result = super.finishUsingItem(stack, world, living);
		if (!world.isClientSide && living instanceof Player)
			((Player) living).getCooldowns().addCooldown(this,
					TF2ConfigVars.fastItemCooldown ? this.waitTime / 2 : this.waitTime);
		if (TF2ConfigVars.freeUseItems)
			result.setCount(count);
		return result;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn) {
		return super.use(world, player, handIn);
	}
}
