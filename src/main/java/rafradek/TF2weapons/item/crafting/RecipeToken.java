package rafradek.TF2weapons.item.crafting;

import rafradek.TF2weapons.util.TF2Util;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.util.TF2Class;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RecipeToken extends ShapelessOreRecipe {

	public RecipeToken(ResourceLocation group, ItemStack result, Object[] recipe, TF2Class clazz) {
		super(group, result, recipe);
		output.setTag(new CompoundTag());
		output.getTag().putByte("Token", (byte) clazz.getIndex());
		input.add(TF2Ingredient.fromStacks(TF2Util.metaStack(TF2weapons.itemToken, 1, clazz.getIndex())));
	}

}
