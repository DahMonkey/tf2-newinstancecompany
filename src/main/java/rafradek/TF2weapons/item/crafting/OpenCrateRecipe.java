package rafradek.TF2weapons.item.crafting;

import rafradek.TF2weapons.util.TF2Util;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemCrate;
import rafradek.TF2weapons.item.ItemFromData;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

public class OpenCrateRecipe extends IForgeRegistryEntry.Impl<Recipe>
		implements Recipe, IRecipeTF2 {

	@Override
	public boolean matches(CraftingContainer inv, Level world) {
		boolean key = false;
		ItemStack crate = ItemStack.EMPTY;

		for (int x = 0; x < inv.getContainerSize(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (stack.getItem() == TF2weapons.itemTF2 && stack.getDamageValue() == 7) {
					if (!key)
						key = true;
					else
						return false;
				} else if (crate.isEmpty() && stack.getItem() instanceof ItemCrate)
					crate = stack;
				else
					return false;
		}
		// System.out.println("matches "+(australium&&stack2!=null));
		return key && crate != null;
	}

	@Override
	public ItemStack getCraftingResult(CraftingContainer inv) {
		ItemStack stack2 = ItemStack.EMPTY;

		for (int x = 0; x < inv.getContainerSize(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (!(stack.getItem() == TF2weapons.itemTF2 && stack.getDamageValue() == 7))
					stack2 = stack;
		}
		// System.out.println("OutPut: "+stack2);
		if (!stack2.isEmpty()) {
			stack2 = stack2.copy();
			stack2.getTag().putBoolean("Open", true);
			stack2.setCount(1);
		}
		return stack2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		// ItemStack stack=ItemFromData.getNewStack("crate1");
		// stack.getTagCompound().setBoolean("Open", true);
		return TF2Util.metaStack(TF2weapons.itemTF2, 1, 8);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(inv.getContainerSize(), ItemStack.EMPTY);

		for (int i = 0; i < nonnulllist.size(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			nonnulllist.set(i, ForgeHooks.getContainerItem(itemstack));
		}

		return nonnulllist;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public ItemStack getSuggestion(int slot) {
		return slot == 0 ? TF2Util.metaStack(TF2weapons.itemTF2, 1, 7)
				: (slot == 1 ? ItemFromData.getNewStack("crate1") : ItemStack.EMPTY);
	}

}
