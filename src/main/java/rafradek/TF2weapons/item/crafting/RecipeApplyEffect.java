package rafradek.TF2weapons.item.crafting;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemApplicableEffect;
import rafradek.TF2weapons.item.ItemFromData;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

public class RecipeApplyEffect implements Recipe, IRecipeTF2 {

	public static final ItemStack ITEM = ItemFromData.getNewStack("minigun");

	public Item type;

	public RecipeApplyEffect(Item type) {
		this.type = type;

	}

	@Override
	public boolean matches(CraftingContainer inv, Level world) {
		ItemStack effect = ItemStack.EMPTY;
		ItemStack stack2 = ItemStack.EMPTY;

		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (effect.isEmpty() && stack.getItem() == type) {
					effect = stack;
				} else if (stack2.isEmpty() && stack.getItem() != type)
					stack2 = stack;
				else
					return false;
		}
		// System.out.println("matches "+(australium&&stack2!=null));
		return !effect.isEmpty() && !stack2.isEmpty()
				&& ((ItemApplicableEffect) effect.getItem()).isApplicable(effect, stack2);
	}

	@Override
	public ItemStack getCraftingResult(CraftingContainer inv) {
		ItemStack effect = ItemStack.EMPTY;
		ItemStack stack2 = ItemStack.EMPTY;

		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (effect.isEmpty() && stack.getItem() == type) {
					effect = stack;
				} else if (stack2.isEmpty() && stack.getItem() != type)
					stack2 = stack;
		}
		// System.out.println("OutPut: "+stack2);
		if (!stack2.isEmpty()) {
			stack2 = stack2.copy();
			if (!stack2.hasTag())
				stack2.setTag(new CompoundTag());
			stack2.setCount(1);
			((ItemApplicableEffect) effect.getItem()).apply(effect, stack2);

			// stack2.getTagCompound().setBoolean("Australium", true);
			//
		}
		return stack2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		ItemStack stack = ITEM.copy();
		((ItemApplicableEffect) type).apply(new ItemStack(type), stack);
		return stack;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		NonNullList<ItemStack> aitemstack = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < aitemstack.size(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			aitemstack.set(i, ForgeHooks.getContainerItem(itemstack));
		}

		return aitemstack;
	}

	@Override
	public Recipe setRegistryName(ResourceLocation name) {
		return null;
	}

	@Override
	public ResourceLocation getRegistryName() {
		return new ResourceLocation(TF2weapons.MOD_ID, this.type.toString());
	}

	@Override
	public Class<Recipe> getRegistryType() {
		return Recipe.class;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public ItemStack getSuggestion(int slot) {
		return slot == 0 ? ITEM : slot == 1 ? new ItemStack(this.type) : ItemStack.EMPTY;
	}

}
