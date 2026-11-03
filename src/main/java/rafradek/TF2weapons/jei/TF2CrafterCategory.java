package rafradek.TF2weapons.jei;

import com.google.common.collect.Lists;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemApplicableEffect;
import rafradek.TF2weapons.item.ItemTF2;
import rafradek.TF2weapons.item.crafting.AustraliumRecipe;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.Recipe;

public class TF2CrafterCategory implements IRecipeCategory<TF2CrafterRecipeWrapper> {

	public static final String ID = new ResourceLocation(TF2weapons.MOD_ID, "tf2_crafter").toString();

	private final ICraftingGridHelper craftingGridHelper;
	private final IDrawable background;

	public TF2CrafterCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 60, 116, 54);
		craftingGridHelper = guiHelper.createCraftingGridHelper(1, 0);
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public String getModName() {
		return TF2weapons.MOD_ID;
	}

	@Override
	public String getTitle() {
		return Component.translatable("jei.category.rafradek_tf2_weapons.TF2Crafter").getString();
	}

	@Override
	public String getUid() {
		return ID;
	}

	public static List<TF2CrafterRecipeWrapper> getRecipes() {
		List<TF2CrafterRecipeWrapper> recipes = new ArrayList<>();
		for (Recipe recipe : TF2CraftingManager.INSTANCE.getRecipeList()) {
			recipes.add(new TF2CrafterRecipeWrapper(recipe));
			if (recipe instanceof AustraliumRecipe) {
				recipes.add(new CustomAustraliumRecipeWrapper(DiggerItem.class));
				recipes.add(new CustomAustraliumRecipeWrapper(SwordItem.class));
				recipes.add(new CustomAustraliumRecipeWrapper(BowItem.class));
			}
		}
		return recipes;
	}

	@Override
	public void setRecipe(IRecipeLayout layout, TF2CrafterRecipeWrapper wrapper, IIngredients ingredients) {
		IGuiItemStackGroup items = layout.getItemStacks();
		IFocus<?> focus = layout.getFocus();
		items.init(0, false, 94, 18);
		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				int index = 1 + x + (y * 3);
				items.init(index, true, x * 18, y * 18);
			}
		}
		if (wrapper.isShapeless())
			layout.setShapeless();
		if (wrapper.getUpgradeInputSlot() == -1) {
			craftingGridHelper.setInputs(items, ingredients.getInputs(VanillaTypes.ITEM));
			items.set(0, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
		} else {
			List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
			List<ItemStack> output = Lists.newArrayList();
			if (focus != null)
				if (focus.getMode() == IFocus.Mode.OUTPUT && isValidFocusForUpgrades(focus))
					inputs.set(wrapper.getUpgradeInputSlot(),
							Lists.newArrayList(wrapper.downgradeStack((ItemStack) focus.getValue())));
			craftingGridHelper.setInputs(items, inputs);
			if (focus != null && focus.getMode() == IFocus.Mode.INPUT && isValidFocusForUpgrades(focus))
				output.add(wrapper.upgradeStack((ItemStack) focus.getValue()));
			else {
				for (ItemStack stack : ingredients.getInputs(VanillaTypes.ITEM).get(wrapper.getUpgradeInputSlot())) {
					output.add(wrapper.upgradeStack(stack));
				}
			}
			items.set(0, output);
		}
	}

	private boolean isValidFocusForUpgrades(IFocus<?> focus) {
		if (!(focus.getValue() instanceof ItemStack))
			return false;
		Item item = ((ItemStack) focus.getValue()).getItem();
		return !(item instanceof BlockItem || item instanceof ItemTF2 || item instanceof ItemTF2
				|| item instanceof ItemApplicableEffect);
	}

}
