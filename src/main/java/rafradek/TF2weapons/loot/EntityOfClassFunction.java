package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import rafradek.TF2weapons.item.ItemFromData;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntityOfClassFunction extends LootItemFunction {

	// public int[] possibleValues;
	// public int[] withClass;
	public String weaponClass;

	public EntityOfClassFunction(LootItemCondition[] conditionsIn, String weaponClass) {
		super(conditionsIn);
		this.weaponClass = weaponClass;
	}

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		stack = ItemFromData.getRandomWeaponOfType(weaponClass, rand, true);
		if (! stack.hasTag()) stack.setTag(new CompoundTag());
		stack.getTag().putBoolean("DropFrom", true);
		return stack;
	}

	public static class Serializer extends LootItemFunction.Serializer<EntityOfClassFunction> {
		public Serializer() {
			super(new ResourceLocation("set_weapon_class"), EntityOfClassFunction.class);
		}

		@Override
		public void serialize(JsonObject object, EntityOfClassFunction functionClazz,
				JsonSerializationContext serializationContext) {
			object.addProperty("weaponClass", functionClazz.weaponClass);
			// object.add("possibleValues",
			// serializationContext.serialize(functionClazz.possibleValues));
			// object.add("data",
			// serializationContext.serialize(functionClazz.metaRange));
		}

		@Override
		public EntityOfClassFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
				LootItemCondition[] conditionsIn) {
			return new EntityOfClassFunction(conditionsIn, object.get("weaponClass").getAsString());
		}
	}
}
