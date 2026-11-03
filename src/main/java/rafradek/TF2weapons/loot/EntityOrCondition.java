package rafradek.TF2weapons.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import rafradek.TF2weapons.TF2weapons;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntityOrCondition implements LootItemCondition {

	LootItemCondition[] conditions;
	boolean negate;

	public EntityOrCondition(LootItemCondition[] conditions, boolean negate) {
		this.negate = negate;
		this.conditions = conditions;
	}

	@Override
	public boolean testCondition(RandomSource random, LootContext context) {

		for (LootItemCondition condition : conditions) {
			if (condition.testCondition(random, context))
				return !this.negate;
		}
		return this.negate;
	}

	public static class Serializer extends LootItemCondition.Serializer<EntityOrCondition> {
		public Serializer() {
			super(new ResourceLocation(TF2weapons.MOD_ID, "or"), EntityOrCondition.class);
		}

		@Override
		public void serialize(JsonObject json, EntityOrCondition value, JsonSerializationContext context) {
			json.addProperty("negate", value.negate);
		}

		@Override
		public EntityOrCondition deserialize(JsonObject json, JsonDeserializationContext context) {
			JsonArray arr = GsonHelper.getJsonArray(json, "conditions");
			LootItemCondition[] conditions = new LootItemCondition[arr.size()];
			for (int i = 0; i < conditions.length; i++) {
				conditions[i] = LootConditionManager
						.getSerializerForName(new ResourceLocation(arr.get(i).getAsString()))
						.deserialize(json, context);
			}
			return new EntityOrCondition(conditions, GsonHelper.getBoolean(json, "negate"));
		}
	}
}
