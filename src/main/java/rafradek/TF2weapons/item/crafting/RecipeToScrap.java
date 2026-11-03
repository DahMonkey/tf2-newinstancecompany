package rafradek.TF2weapons.item.crafting;

import rafradek.TF2weapons.util.TF2Util;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import java.util.ArrayList;
import java.util.HashSet;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

public class RecipeToScrap implements TF2Recipe, IRecipeTF2 {

	public int token;

	public RecipeToScrap(int id) {
		this.token = id;
	}

	@Override
	public boolean matches(CraftingContainer inv, Level world) {
		ArrayList<ItemStack> stacks = new ArrayList<>();

		HashSet<String> classnames = new HashSet<>();
		for (int x = 0; x < inv.getContainerSize(); x++) {
			ItemStack stack = inv.getItem(x);
			if (!stack.isEmpty())
				if (stack.getItem() instanceof ItemFromData
						&& ItemFromData.getData(stack).getInt(PropertyType.COST) >= 6 && (token == -1 || ItemFromData
								.getData(stack).get(PropertyType.SLOT).containsKey(TF2Class.getClass(token).getName()))) {
					stacks.add(stack);
				} else
					return false;
		}
		// System.out.println("matches "+(australium&&stack2!=null));
		return stacks.size() == (token != -1 ? 3 : 2);
	}

	@Override
	public ItemStack getCraftingResult(CraftingContainer inv) {
		if (token == -1)
			return TF2Util.metaStack(TF2weapons.itemTF2, 1, 3);
		else
			return TF2Util.metaStack(TF2weapons.itemToken, 1, token);
	}

	@Override
	public ItemStack getRecipeOutput() {
		if (token == -1)
			return TF2Util.metaStack(TF2weapons.itemTF2, 1, 3);
		else
			return TF2Util.metaStack(TF2weapons.itemToken, 1, token);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(inv.getContainerSize(), ItemStack.EMPTY);

		for (int i = 0; i < nonnulllist.size(); ++i) {
			ItemStack itemstack = inv.getItem(i);
			nonnulllist.set(i, (itemstack.hasCraftingRemainingItem() ? itemstack.getCraftingRemainingItem() : ItemStack.EMPTY));
		}

		return nonnulllist;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public ItemStack getSuggestion(int slot) {
		if (slot < 2 && this.token == -1) {
			return TF2Util.metaStack(TF2weapons.itemTF2, 1, 9);
		} else if (slot < 3 && token != -1) {
			ItemStack stack = TF2Util.metaStack(TF2weapons.itemTF2, 1, 9);
			stack.setTag(new CompoundTag());
			stack.getTag().putByte("Token", (byte) this.token);
			return stack;
		}
		return ItemStack.EMPTY;
	}
}
