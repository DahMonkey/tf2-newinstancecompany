package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import rafradek.TF2weapons.TF2weapons;
import java.util.Random;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntityNBTCondition implements LootItemCondition {

	CompoundTag tagValue;
	boolean negate;

	public EntityNBTCondition(CompoundTag test, boolean negate) {
		this.negate = negate;
		this.tagValue = test;
	}

	public EntityNBTCondition(String nbttag, Number test) {

	}

	@Override
	public boolean testCondition(RandomSource random, LootContext context) {

		Entity test = context.getEntity(EntityTarget.THIS);
		CompoundTag tag = test.save(new CompoundTag());
		CompoundTag tagmerge = new CompoundTag();
		for (String key : tagValue.getAllKeys())
			tagmerge.put(key, tag.getTag(key));

		// tagmerge.merge(tagValue);
		return (!negate && tagmerge.equals(tagValue)) || (negate && !tagmerge.equals(tagValue));
	}

	public static class Serializer extends LootItemCondition.Serializer<EntityNBTCondition> {
		public Serializer() {
			super(new ResourceLocation(TF2weapons.MOD_ID, "nbt_test"), EntityNBTCondition.class);
		}

		@Override
		public void serialize(JsonObject json, EntityNBTCondition value, JsonSerializationContext context) {
			json.addProperty("test", value.tagValue.toString());
			json.addProperty("negate", value.negate);
		}

		@Override
		public EntityNBTCondition deserialize(JsonObject json, JsonDeserializationContext context) {
			try {
				return new EntityNBTCondition(TagParser.parseTag(GsonHelper.getString(json, "test", "{}")),
						GsonHelper.getBoolean(json, "negate", false));
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}
			return new EntityNBTCondition(new CompoundTag(), false);
		}
	}
}
