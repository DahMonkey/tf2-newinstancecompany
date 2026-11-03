package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import rafradek.TF2weapons.TF2weapons;

/** True when any of "conditions" is true (inverted by "negate"). */
public class EntityOrCondition implements LootItemCondition {

	final LootItemCondition[] conditions;
	final boolean negate;

	public EntityOrCondition(LootItemCondition[] conditions, boolean negate) {
		this.negate = negate;
		this.conditions = conditions;
	}

	@Override
	public boolean test(LootContext context) {
		for (LootItemCondition condition : conditions)
			if (condition.test(context))
				return !this.negate;
		return this.negate;
	}

	@Override
	public LootItemConditionType getType() {
		return TF2weapons.OR;
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EntityOrCondition> {
		@Override
		public void serialize(JsonObject json, EntityOrCondition value, JsonSerializationContext context) {
			json.add("conditions", context.serialize(value.conditions));
			json.addProperty("negate", value.negate);
		}

		@Override
		public EntityOrCondition deserialize(JsonObject json, JsonDeserializationContext context) {
			return new EntityOrCondition(
					GsonHelper.getAsObject(json, "conditions", context, LootItemCondition[].class),
					GsonHelper.getAsBoolean(json, "negate", false));
		}
	}
}
