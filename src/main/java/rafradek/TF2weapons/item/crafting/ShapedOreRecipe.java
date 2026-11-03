package rafradek.TF2weapons.item.crafting;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

/** Shaped recipe of the mod's crafting table, using 1.12's pattern-and-key arguments. */
public class ShapedOreRecipe implements TF2Recipe {

	protected ItemStack output;
	protected int width, height;
	protected NonNullList<TF2Ingredient> input;

	public ShapedOreRecipe(ResourceLocation group, Object result, Object... recipe) {
		this.output = result instanceof ItemStack ? ((ItemStack) result).copy()
				: new ItemStack((ItemLike) result);
		int idx = 0;
		java.util.List<String> rows = new java.util.ArrayList<>();
		while (idx < recipe.length && recipe[idx] instanceof String) {
			String row = (String) recipe[idx];
			// A key ingredient may itself be a String (ore name); rows always come before the first char key.
			if (idx + 1 < recipe.length && rows.size() > 0 && recipe[idx] instanceof String
					&& !(recipe[idx + 1] instanceof String) && !(recipe[idx + 1] instanceof Character))
				break;
			rows.add(row);
			idx++;
		}
		Map<Character, TF2Ingredient> keys = new HashMap<>();
		keys.put(' ', TF2Ingredient.EMPTY);
		for (; idx + 1 < recipe.length; idx += 2)
			keys.put((Character) recipe[idx], TF2Ingredient.of(recipe[idx + 1]));
		this.height = rows.size();
		this.width = rows.stream().mapToInt(String::length).max().orElse(0);
		this.input = NonNullList.withSize(this.width * this.height, TF2Ingredient.EMPTY);
		for (int y = 0; y < this.height; y++) {
			String row = rows.get(y);
			for (int x = 0; x < row.length(); x++) {
				TF2Ingredient ing = keys.get(row.charAt(x));
				if (ing == null)
					throw new IllegalArgumentException("Missing recipe key " + row.charAt(x) + " for " + this.output);
				this.input.set(x + y * this.width, ing);
			}
		}
	}

	@Override
	public boolean matches(CraftingContainer inv, Level world) {
		for (int x = 0; x <= inv.getWidth() - this.width; x++)
			for (int y = 0; y <= inv.getHeight() - this.height; y++)
				if (this.matches(inv, x, y, false) || this.matches(inv, x, y, true))
					return true;
		return false;
	}

	private boolean matches(CraftingContainer inv, int startX, int startY, boolean mirror) {
		for (int x = 0; x < inv.getWidth(); x++)
			for (int y = 0; y < inv.getHeight(); y++) {
				int subX = x - startX;
				int subY = y - startY;
				TF2Ingredient target = TF2Ingredient.EMPTY;
				if (subX >= 0 && subY >= 0 && subX < this.width && subY < this.height)
					target = this.input.get((mirror ? this.width - subX - 1 : subX) + subY * this.width);
				ItemStack stack = inv.getItem(x + y * inv.getWidth());
				if (target == TF2Ingredient.EMPTY ? !stack.isEmpty() : !target.test(stack))
					return false;
			}
		return true;
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

	public int getRecipeWidth() {
		return this.width;
	}

	public int getRecipeHeight() {
		return this.height;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width >= this.width && height >= this.height;
	}
}
