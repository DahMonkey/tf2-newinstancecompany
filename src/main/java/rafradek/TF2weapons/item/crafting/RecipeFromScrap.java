package rafradek.TF2weapons.item.crafting;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemTF2;
import rafradek.TF2weapons.item.ItemToken;
import java.util.ArrayList;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

public class RecipeFromScrap extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<Recipe>
		implements Recipe, IRecipeTF2 {

	public boolean withToken;

	public RecipeFromScrap(boolean withToken) {
		this.withToken = withToken;
	}

	@Override
	public boolean matches(CraftingContainer inv, Level world) {
		ArrayList<ItemStack> stacks = new ArrayList<>();

		int tokens = 0;
		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (stack.getItem() instanceof ItemTF2 && stack.getDamageValue() == 3)
					stacks.add(stack);
				else if (this.withToken && stack.getItem() instanceof ItemToken && tokens == 0)
					tokens++;
				else
					return false;
		}
		// System.out.println("matches "+(australium&&stack2!=null));
		return stacks.size() == 2;
	}

	@Override
	public ItemStack getCraftingResult(CraftingContainer inv) {
		ItemStack result = new ItemStack(TF2weapons.itemTF2, 1, 9);
		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (stack.getItem() instanceof ItemToken) {
				result.setTag(new CompoundTag());
				result.getTag().putByte("Token", (byte) stack.getDamageValue());
			}
		}
		return result;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(TF2weapons.itemTF2, 1, 9);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < nonnulllist.size(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			nonnulllist.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
		}

		return nonnulllist;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public ItemStack getSuggestion(int slot) {
		switch (slot) {
		case 0:
			return new ItemStack(TF2weapons.itemTF2, 1, 3);
		case 1:
			return new ItemStack(TF2weapons.itemTF2, 1, 3);
		case 2:
			return new ItemStack(TF2weapons.itemToken, 1, 0);
		default:
			return ItemStack.EMPTY;
		}
	}
}
