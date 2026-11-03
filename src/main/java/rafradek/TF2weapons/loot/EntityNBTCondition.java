package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import rafradek.TF2weapons.TF2weapons;

/** Tests that the looted entity's saved NBT contains the given values. */
public class EntityNBTCondition implements LootItemCondition {

	final CompoundTag tagValue;
	final boolean negate;

	public EntityNBTCondition(CompoundTag test, boolean negate) {
		this.negate = negate;
		this.tagValue = test;
	}

	@Override
	public boolean test(LootContext context) {
		Entity test = context.getParamOrNull(LootContextParams.THIS_ENTITY);
		if (test == null)
			return negate;
		CompoundTag tag = test.saveWithoutId(new CompoundTag());
		CompoundTag tagmerge = new CompoundTag();
		for (String key : tagValue.getAllKeys()) {
			Tag value = tag.get(key);
			if (value != null)
				tagmerge.put(key, value);
		}
		return negate != tagmerge.equals(tagValue);
	}

	@Override
	public LootItemConditionType getType() {
		return TF2weapons.NBT_TEST;
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EntityNBTCondition> {
		@Override
		public void serialize(JsonObject json, EntityNBTCondition value, JsonSerializationContext context) {
			json.addProperty("test", value.tagValue.toString());
			json.addProperty("negate", value.negate);
		}

		@Override
		public EntityNBTCondition deserialize(JsonObject json, JsonDeserializationContext context) {
			try {
				return new EntityNBTCondition(TagParser.parseTag(GsonHelper.getAsString(json, "test", "{}")),
						GsonHelper.getAsBoolean(json, "negate", false));
			} catch (CommandSyntaxException e) {
				TF2weapons.LOGGER.error("Bad nbt_test loot condition", e);
			}
			return new EntityNBTCondition(new CompoundTag(), false);
		}
	}
}
