package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemFromData;

/** Replaces the stack with a random weapon of a weapon class ("set_weapon_class"). */
public class EntityOfClassFunction extends LootItemConditionalFunction {

	public final String weaponClass;

	public EntityOfClassFunction(LootItemCondition[] conditionsIn, String weaponClass) {
		super(conditionsIn);
		this.weaponClass = weaponClass;
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext context) {
		stack = ItemFromData.getRandomWeaponOfType(weaponClass, context.getRandom(), true);
		if (stack.isEmpty())
			return stack;
		if (!stack.hasTag())
			stack.setTag(new CompoundTag());
		stack.getTag().putBoolean("DropFrom", true);
		return stack;
	}

	@Override
	public LootItemFunctionType getType() {
		return TF2weapons.SET_WEAPON_CLASS;
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<EntityOfClassFunction> {
		@Override
		public void serialize(JsonObject object, EntityOfClassFunction function, JsonSerializationContext context) {
			super.serialize(object, function, context);
			object.addProperty("weaponClass", function.weaponClass);
		}

		@Override
		public EntityOfClassFunction deserialize(JsonObject object, JsonDeserializationContext context,
				LootItemCondition[] conditionsIn) {
			return new EntityOfClassFunction(conditionsIn, object.get("weaponClass").getAsString());
		}
	}
}
