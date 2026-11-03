package rafradek.TF2weapons.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A recipe of the mod's own crafting table. These recipes live in {@link TF2CraftingManager} and are
 * matched by the table's menu, so they keep the 1.12 IRecipe method names.
 */
public interface TF2Recipe {

	boolean matches(CraftingContainer inv, Level world);

	ItemStack getCraftingResult(CraftingContainer inv);

	ItemStack getRecipeOutput();

	default NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		NonNullList<ItemStack> list = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
		for (int i = 0; i < list.size(); ++i) {
			ItemStack stack = inv.getItem(i);
			if (stack.hasCraftingRemainingItem())
				list.set(i, stack.getCraftingRemainingItem());
		}
		return list;
	}

	default boolean canFit(int width, int height) {
		return true;
	}

	default NonNullList<TF2Ingredient> getIngredients() {
		return NonNullList.create();
	}
}
