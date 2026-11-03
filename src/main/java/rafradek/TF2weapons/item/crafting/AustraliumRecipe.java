package rafradek.TF2weapons.item.crafting;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.*;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

public class AustraliumRecipe implements Recipe, IRecipeTF2 {

	public static final ItemStack ITEM = ItemFromData.getNewStack("minigun");

	static {
		ITEM.getTag().putBoolean("Australium", true);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level world) {
		int australium = 0;
		ItemStack stack2 = ItemStack.EMPTY;

		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (stack.getItem() == TF2weapons.itemTF2 && stack.getDamageValue() == 2) {
					if (australium < 8)
						australium++;
					else
						return false;
				} else if (stack2.isEmpty() && (stack.getItem() instanceof DiggerItem
						|| stack.getItem() instanceof SwordItem || stack.getItem() instanceof BowItem
						|| stack.getItem() instanceof ItemUsable || stack.getItem() instanceof ItemCloak
						|| stack.getItem() instanceof ItemPDA || stack.getItem() instanceof ItemBackpack))
					stack2 = stack;
				else
					return false;
		}
		// System.out.println("matches "+(australium&&stack2!=null));
		return australium == 8 && !stack2.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(CraftingContainer inv) {
		ItemStack stack2 = ItemStack.EMPTY;

		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (!(stack.getItem() == TF2weapons.itemTF2 && stack.getDamageValue() == 2))
					stack2 = stack;
		}
		// System.out.println("OutPut: "+stack2);
		if (!stack2.isEmpty()) {
			stack2 = stack2.copy();
			if (!stack2.hasTag())
				stack2.setTag(new CompoundTag());
			stack2.setCount(1);
			stack2.getTag().putBoolean("Australium", true);
			// stack2.getTagCompound().setBoolean("Strange", true);
		}
		return stack2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ITEM;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		NonNullList<ItemStack> aitemstack = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < aitemstack.size(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			aitemstack.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
		}

		return aitemstack;
	}

	@Override
	public Recipe setRegistryName(ResourceLocation name) {
		return null;
	}

	@Override
	public ResourceLocation getRegistryName() {
		return new ResourceLocation(TF2weapons.MOD_ID, "australium_recipe");
	}

	@Override
	public Class<Recipe> getRegistryType() {
		return Recipe.class;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width >= 3 && height >= 3;
	}

	@Override
	public ItemStack getSuggestion(int slot) {
		return slot == 4 ? new ItemStack(TF2weapons.itemTF2, 1, 9) : new ItemStack(TF2weapons.itemTF2, 1, 2);
	}

}
