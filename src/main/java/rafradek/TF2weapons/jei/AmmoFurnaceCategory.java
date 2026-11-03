package rafradek.TF2weapons.jei;

import com.google.common.collect.Lists;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import javax.annotation.Nonnull;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class AmmoFurnaceCategory implements IRecipeCategory<AmmoFurnaceCategory.Wrapper> {

	public static final String ID = new ResourceLocation(TF2weapons.MOD_ID, "ammo_furnace").toString();
	public static final ResourceLocation TEXTURE = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/jei/ammo_furnace.png");

	private final IDrawable background;
	private final IDrawableAnimated progress;
	private final IDrawableAnimated flame;

	public AmmoFurnaceCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(TEXTURE, 0, 0, 120, 56);
		IDrawableStatic progressDrawable = guiHelper.createDrawable(TEXTURE, 14, 56, 24, 16);
		progress = guiHelper.createAnimatedDrawable(progressDrawable, 300, IDrawableAnimated.StartDirection.LEFT,
				false);
		IDrawableStatic flameDrawable = guiHelper.createDrawable(TEXTURE, 0, 56, 14, 14);
		flame = guiHelper.createAnimatedDrawable(flameDrawable, 300, IDrawableAnimated.StartDirection.TOP, true);
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(@Nonnull Minecraft minecraft) {
		progress.draw(minecraft, 34, 20);
		flame.draw(minecraft, 39, 39);
	}

	@Override
	public String getModName() {
		return TF2weapons.MOD_ID;
	}

	@Override
	public String getTitle() {
		return Component.translatable("jei.category.rafradek_tf2_weapons.AmmoFurnace").getString();
	}

	@Override
	public String getUid() {
		return ID;
	}

	public static List<Wrapper> getRecipes() {
		List<Wrapper> recipes = Lists.newArrayList();
		for (ShapelessOreRecipe recipe : TF2CraftingManager.AMMO_RECIPES) {
			if (recipe != null)
				recipes.add(new Wrapper(recipe));
		}
		return recipes;
	}

	@Override
	public void setRecipe(IRecipeLayout layout, Wrapper wrapper, IIngredients ingredients) {
		IGuiItemStackGroup items = layout.getItemStacks();
		IFocus<?> focus = layout.getFocus();
		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				int index = x + (y * 3);
				items.init(index, false, x * 18 + 65, y * 18 + 1);
			}
			items.init(9, true, 5, 18);
		}
		for (int i = 0; i < wrapper.outputs.size(); i++)
			items.set(i, wrapper.outputs.get(i));
		items.set(9, wrapper.input);
	}

	public static class Wrapper implements IRecipeWrapper {

		protected ItemStack input;
		protected List<ItemStack> outputs = Lists.newArrayList();

		public Wrapper(ShapelessOreRecipe base) {
			input = base.getRecipeOutput();
			for (Ingredient ingredient : base.getIngredients())
				outputs.add(ingredient.getMatchingStacks()[0]);
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(VanillaTypes.ITEM, input);
			ingredients.setOutputs(VanillaTypes.ITEM, outputs);
		}

	}

}
