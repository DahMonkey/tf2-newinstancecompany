package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import rafradek.TF2weapons.TF2weapons;

/** Replacement for 1.12's "set_data": sets an item's legacy metadata (stored as damage). */
public class SetMetaFunction extends LootItemConditionalFunction {
	private final NumberProvider meta;

	public SetMetaFunction(LootItemCondition[] conditions, NumberProvider meta) {
		super(conditions);
		this.meta = meta;
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext context) {
		stack.setDamageValue(meta.getInt(context));
		return stack;
	}

	@Override
	public LootItemFunctionType getType() {
		return TF2weapons.SET_META;
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetMetaFunction> {
		@Override
		public void serialize(JsonObject json, SetMetaFunction function, JsonSerializationContext context) {
			super.serialize(json, function, context);
			json.add("meta", context.serialize(function.meta));
		}

		@Override
		public SetMetaFunction deserialize(JsonObject json, JsonDeserializationContext context,
				LootItemCondition[] conditions) {
			return new SetMetaFunction(conditions, GsonHelper.getAsObject(json, "meta", context, NumberProvider.class));
		}
	}
}
