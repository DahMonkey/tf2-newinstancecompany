package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.util.PropertyType;

/** Random (possibly Valve or upgraded) weapon ("set_weapon_upgraded"). */
public class RandomWeaponFunction extends LootItemConditionalFunction {

	public final NumberProvider upgradeRange;
	public final float valveWepChance;

	public RandomWeaponFunction(LootItemCondition[] conditionsIn, NumberProvider range, float valve) {
		super(conditionsIn);
		this.upgradeRange = range;
		this.valveWepChance = valve;
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext context) {
		RandomSource random = context.getRandom();
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
		int upgrades = upgradeRange.getInt(context);
		if (upgrades > 0 && !stack.isEmpty())
			TF2Attribute.upgradeItemStack(stack, upgrades, random);
		return stack;
	}

	@Override
	public LootItemFunctionType getType() {
		return TF2weapons.SET_WEAPON_UPGRADED;
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<RandomWeaponFunction> {
		@Override
		public void serialize(JsonObject object, RandomWeaponFunction function, JsonSerializationContext context) {
			super.serialize(object, function, context);
			object.add("upgrade_range", context.serialize(function.upgradeRange));
			object.addProperty("valve_chance", function.valveWepChance);
		}

		@Override
		public RandomWeaponFunction deserialize(JsonObject object, JsonDeserializationContext context,
				LootItemCondition[] conditionsIn) {
			return new RandomWeaponFunction(conditionsIn,
					GsonHelper.getAsObject(object, "upgrade_range", context, NumberProvider.class),
					GsonHelper.getAsFloat(object, "valve_chance", 0));
		}
	}
}
