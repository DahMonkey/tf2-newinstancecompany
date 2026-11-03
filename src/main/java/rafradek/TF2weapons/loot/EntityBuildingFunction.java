package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntityBuildingFunction extends LootItemFunction {

	// public int[] possibleValues;
	// public int[] withClass;
	public EntityBuildingFunction(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		Entity entity = context.getEntity(EntityTarget.THIS);
		if (entity != null && entity instanceof EntityTF2Character)
			stack.setDamageValue(18 + rand.nextInt(3) * 2 + ((EntityTF2Character) entity).getEntTeam());
		return stack;
	}

	public static class Serializer extends LootItemFunction.Serializer<EntityBuildingFunction> {
		public Serializer() {
			super(new ResourceLocation("random_meta"), EntityBuildingFunction.class);
		}

		@Override
		public void serialize(JsonObject object, EntityBuildingFunction functionClazz,
				JsonSerializationContext serializationContext) {
			// object.add("possibleValues",
			// serializationContext.serialize(functionClazz.possibleValues));
			// object.add("data",
			// serializationContext.serialize(functionClazz.metaRange));
		}

		@Override
		public EntityBuildingFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
				LootItemCondition[] conditionsIn) {
			return new EntityBuildingFunction(conditionsIn);
		}
	}
}
