package rafradek.TF2weapons.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

/** Shapeless recipe of the mod's crafting table. */
public class ShapelessOreRecipe implements TF2Recipe {

	protected ItemStack output;
	protected NonNullList<TF2Ingredient> input = NonNullList.create();

	public ShapelessOreRecipe(ResourceLocation group, Object result, Object... recipe) {
		this.output = result instanceof ItemStack ? ((ItemStack) result).copy()
				: new ItemStack((ItemLike) result);
		for (Object in : recipe)
			this.input.add(TF2Ingredient.of(in));
	}

	@Override
	public boolean matches(CraftingContainer inv, Level world) {
		boolean[] used = new boolean[this.input.size()];
		int found = 0;
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty())
				continue;
			boolean matched = false;
			for (int j = 0; j < this.input.size(); j++) {
				if (!used[j] && this.input.get(j).test(stack)) {
					used[j] = true;
					matched = true;
					found++;
					break;
				}
			}
			if (!matched)
				return false;
		}
		return found == this.input.size();
	}

	@Override
	public ItemStack getCraftingResult(CraftingContainer inv) {
		return this.output.copy();
	}

	@Override
	public ItemStack getRecipeOutput() {
		return this.output;
	}

	@Override
	public NonNullList<TF2Ingredient> getIngredients() {
		return this.input;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= this.input.size();
	}
}
