package rafradek.TF2weapons.loot;

import rafradek.TF2weapons.item.ItemFromData;
import java.util.Random;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RandomWeaponItemFunction extends LootItemFunction {

	public RandomWeaponItemFunction() {
		super(new LootItemCondition[]{});
	}

	@Override
	public ItemStack apply(ItemStack stack, RandomSource random, LootContext context) {
		//loottweaker support
		if (random == null && context == null) return stack;
		if (!(stack.getDamageValue() == 9 || stack.getDamageValue() == 10)) return stack;
		return ItemFromData.getRandomWeapon(stack, random);
	}

}
