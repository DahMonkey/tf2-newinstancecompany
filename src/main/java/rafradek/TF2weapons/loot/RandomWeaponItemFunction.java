package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemFromData;

/**
 * Turns "random weapon" / "random hat" tokens (item_tf2 meta 9 and 10) into an actual random item.
 * In 1.12 a mixin added this to every loot entry of item_tf2; the port's loot tables list it.
 */
public class RandomWeaponItemFunction extends LootItemConditionalFunction {

	public RandomWeaponItemFunction(LootItemCondition[] conditions) {
		super(conditions);
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext context) {
		if (!(stack.getDamageValue() == 9 || stack.getDamageValue() == 10))
			return stack;
		return ItemFromData.getRandomWeapon(stack, context.getRandom());
	}

	@Override
	public LootItemFunctionType getType() {
		return TF2weapons.RANDOM_WEAPON_ITEM;
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<RandomWeaponItemFunction> {
		@Override
		public RandomWeaponItemFunction deserialize(JsonObject object, JsonDeserializationContext context,
				LootItemCondition[] conditions) {
			return new RandomWeaponItemFunction(conditions);
		}
	}
}
