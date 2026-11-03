package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;

/** Picks a building box matching the dropping mercenary's team ("random_meta"). */
public class EntityBuildingFunction extends LootItemConditionalFunction {

	public EntityBuildingFunction(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext context) {
		Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
		if (entity instanceof EntityTF2Character)
			stack.setDamageValue(18 + context.getRandom().nextInt(3) * 2 + ((EntityTF2Character) entity).getEntTeam());
		return stack;
	}

	@Override
	public LootItemFunctionType getType() {
		return TF2weapons.RANDOM_META;
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<EntityBuildingFunction> {
		@Override
		public EntityBuildingFunction deserialize(JsonObject object, JsonDeserializationContext context,
				LootItemCondition[] conditionsIn) {
			return new EntityBuildingFunction(conditionsIn);
		}
	}
}
