package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.item.ItemWearable;
import rafradek.TF2weapons.util.PropertyType;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RandomWeaponFunction extends LootItemFunction {

	// public int[] possibleValues;
	// public int[] withClass;
	public RandomValueRange upgradeRange;
	public float valveWepChance;

	public RandomWeaponFunction(LootItemCondition[] conditionsIn, RandomValueRange range, float valve) {
		super(conditionsIn);
		this.upgradeRange = range;
		this.valveWepChance = valve;
	}

	@Override
	public ItemStack apply(ItemStack stack, RandomSource random, LootContext context) {
		//loottweaker support
		if (random == null && context == null) {
			CompoundTag nbt = stack.getTag();
			stack = TF2Util.metaStack(TF2weapons.itemTF2, 1, stack.getItem() instanceof ItemWearable ? 10 : 9);
		 	stack.setTag(nbt);
			return stack;
		}
		if (this.valveWepChance * 0.35f < random.nextFloat()) {
			stack = ItemFromData.getRandomWeapon(random,
					input -> (!input.getBoolean(PropertyType.HIDDEN) && input.getInt(PropertyType.ROLL_HIDDEN) == 0
							&& !input.getString(PropertyType.CLASS).equals("cosmetic")
							&& !input.getString(PropertyType.CLASS).equals("crate")));
		} else {
			stack = ItemFromData.getRandomWeapon(random, input -> (input.getInt(PropertyType.ROLL_HIDDEN) == 2));
		}
		if (!stack.isEmpty() && stack.getItem() instanceof ItemWeapon && this.valveWepChance >= random.nextFloat()) {
			stack.getTag().putBoolean("Valve", true);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[0], 1.15f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[2], 2.5f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[6], 0.85f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[61], 1.5f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[37], 1f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[45], 5f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[48], 5);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[49], 0.85f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[46], 1.15f);
		}
		if (upgradeRange.getMax() > 0)
			TF2Attribute.upgradeItemStack(stack, this.upgradeRange.generateInt(random), random);
		return stack;
	}

	public static class Serializer extends LootItemFunction.Serializer<RandomWeaponFunction> {
		public Serializer() {
			super(new ResourceLocation("set_weapon_upgraded"), RandomWeaponFunction.class);
		}

		@Override
		public void serialize(JsonObject object, RandomWeaponFunction functionClazz,
				JsonSerializationContext serializationContext) {
			object.add("upgrade_range", serializationContext.serialize(functionClazz.upgradeRange));
			object.add("valve_chance", serializationContext.serialize(functionClazz.valveWepChance));
			// object.add("possibleValues",
			// serializationContext.serialize(functionClazz.possibleValues));
			// object.add("data",
			// serializationContext.serialize(functionClazz.metaRange));
		}

		@Override
		public RandomWeaponFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
				LootItemCondition[] conditionsIn) {
			return new RandomWeaponFunction(conditionsIn,
					GsonHelper.deserializeClass(object, "upgrade_range", deserializationContext, RandomValueRange.class),
					object.get("valve_chance").getAsFloat());
		}
	}
}
