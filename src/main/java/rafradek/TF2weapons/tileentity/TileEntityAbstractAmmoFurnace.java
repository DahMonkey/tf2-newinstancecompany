package rafradek.TF2weapons.tileentity;

import com.google.common.collect.Lists;
import rafradek.TF2weapons.block.BlockAmmoFurnace;
import rafradek.TF2weapons.inventory.ContainerAmmoFurnace;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public abstract class TileEntityAbstractAmmoFurnace extends BaseContainerBlockEntity implements BlockEntityTicker, WorldlyContainer {
	protected int cookTime;
	protected int totalCookTime;
	protected String furnaceCustomName;

	private ShapelessOreRecipe cachedRecipe;
	private int cachedSlot = -1;

	@Override
	public int getContainerSize() {
		return getInventory().size();
	}

	/**
	 * Returns the stack in the given slot.
	 */
	@Override
	@Nullable
	public ItemStack getStackInSlot(int index) {
		return getInventory().get(index);
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns
	 * them in a new stack.
	 */
	@Override
	@Nullable
	public ItemStack decrStackSize(int index, int count) {
		return ContainerHelper.getAndSplit(getInventory(), index, count);
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	@Override
	@Nullable
	public ItemStack removeStackFromSlot(int index) {
		return ContainerHelper.getAndRemove(getInventory(), index);
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be
	 * crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
		boolean flag = !stack.isEmpty() && stack.sameItem(getInventory().get(index))
				&& ItemStack.tagMatches(stack, getInventory().get(index));
		getInventory().set(index, stack);

		if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit())
			stack.setCount(this.getInventoryStackLimit());

		if (index < 9 && !flag) {
			if (index == cachedSlot) {
				this.totalCookTime = this.getCookTime(stack);
				this.cookTime = 0;
				this.setChanged();
			}
		}
	}
	
	@Override
	public boolean hasCustomName() {
		return this.furnaceCustomName != null && !this.furnaceCustomName.isEmpty();
	}
	
	public void setCustomInventoryName(String p_145951_1_) {
		this.furnaceCustomName = p_145951_1_;
	}

	@Override
	public void readFromNBT(CompoundTag compound) {
		this.cookTime = compound.getInt("CookTime");
		this.totalCookTime = compound.getInt("CookTimeTotal");
		if (compound.contains("CustomName", 8))
			this.furnaceCustomName = compound.getString("CustomName");
	}

	@Override
	public CompoundTag saveAdditional(CompoundTag compound) {
		super.writeToNBT(compound);
		compound.putInt("CookTime", this.cookTime);
		compound.putInt("CookTimeTotal", this.totalCookTime);
		if (this.hasCustomName())
			compound.putString("CustomName", this.furnaceCustomName);

		return compound;
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64,
	 * possibly will be extended.
	 */
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	@Override
	public void update() {
		boolean burning = isBurning();
		boolean flag1 = false;
		if (!this.level.isClientSide) {
			if (canSmelt()) {
				tickFuel();
			}

			if (isBurning() && canSmelt()) {
				++this.cookTime;

				if (this.cookTime == this.totalCookTime) {
					this.cookTime = 0;
					this.totalCookTime = this.getCookTime(getInventory().get(0));
					this.smeltItem();
					flag1 = true;
				}
			} else
				this.cookTime = 0;
		} else if (!isBurning() && this.cookTime > 0)
			this.cookTime = Mth.clamp(this.cookTime - 2, 0, this.totalCookTime);

		if (isBurning() != burning) {
			flag1 = true;
			BlockAmmoFurnace.setState(burning, this.level, this.pos);
		}
		if (flag1)
			this.setChanged();
	}

	public int getCookTime(@Nullable ItemStack stack) {
		return 200;
	}

	/**
	 * Returns true if the furnace can smelt an item, i.e. has a source item,
	 * destination stack isn't full, etc.
	 */

	protected boolean canSmelt() {
		if (cachedSlot == -1 || cachedRecipe == null |! TF2Util.matches(cachedRecipe.getRecipeOutput(), getInventory().get(cachedSlot))) {
			for (int i = 0; i < 9; i++) if (canFitRecipe(i)) return true;
			if (cachedSlot != -1) cachedSlot = -1;
			if (cachedRecipe != null) cachedRecipe = null;
			return false;
		}
		return TF2Util.matches(cachedRecipe.getRecipeOutput(), getInventory().get(cachedSlot));
	}

	private boolean canFitRecipe(int slot) {
		ItemStack base = getInventory().get(slot);
		if (base != null &! base.isEmpty()) {
			for (ShapelessOreRecipe recipe : TF2CraftingManager.AMMO_RECIPES) {
				if (recipe == null || recipe.getRecipeOutput() == null) continue;
				if (TF2Util.matches(recipe.getRecipeOutput(), base)) {
					int[] sizes = new int[9];
					List<ItemStack> items = Lists.newArrayList(getOutput());
					for (int i = 0; i < 9; i++) sizes[i] = items.get(i).getCount();
					for (Ingredient output : recipe.getIngredients()) {
						if (output.getMatchingStacks().length > 0) {
							ItemStack stack1 = output.getMatchingStacks()[0];
							int count = stack1.getCount();
							for (int i = 0; i < 9; i++) {
								ItemStack stack2 = items.get(i);
								if (stack1.getItem() == stack2.getItem() && stack1.getDamageValue() == stack2.getDamageValue()) {
									if (sizes[i] + count <= stack1.getMaxStackSize()) {
										sizes[i] += count;
										break;
									} else {
										count = sizes[i] + count - stack1.getMaxStackSize();
										sizes[i] = stack1.getMaxStackSize();
									}
								} else if (stack2.isEmpty()) {
									sizes[i] = count;
									items.set(i, stack1.copy());
									break;
								}
								if (i == 8) return false;
							}
						} else break;
					}
					cachedSlot = slot;
					cachedRecipe = recipe;
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Turn one item from the furnace source stack into the appropriate smelted item
	 * in the furnace result stack
	 */
	public void smeltItem() {
		if (canSmelt()) {
			int ammoToConsume = cachedRecipe.getRecipeOutput().getCount();
			ItemStack base = getInventory().get(cachedSlot);
			int ammoConsumed = Math.min(base.getCount(), ammoToConsume);
			base.shrink(ammoConsumed);
			ammoToConsume -= ammoConsumed;
			if (base.getCount() <= 0)
				setInventorySlotContents(cachedSlot, ItemStack.EMPTY);

			if (ammoToConsume <= 0) {
				for (Ingredient obj : cachedRecipe.getIngredients()) {
					ItemStack out = obj.getMatchingStacks()[0];
					for (int j = 10; j < 19; j++) {
						boolean handled = false;
						ItemStack inSlot = this.getStackInSlot(j);
						if (inSlot.isEmpty()) {
							this.setInventorySlotContents(j, out.copy());
							handled = true;
						} else if (out.sameItem(inSlot) && ItemStack.tagMatches(out, inSlot)) {
							int size = out.getCount() + inSlot.getCount();

							if (size <= out.getMaxStackSize()) {
								inSlot.setCount(size);
								handled = true;
							}
						}
						if (handled)
							break;
					}
				}
				return;
			}
		}
	}

	@Override
	public boolean isUsableByPlayer(Player player) {
		return this.level.getBlockEntity(this.pos) != this ? false
				: player.distanceToSqr(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(Player player) {}

	@Override
	public void closeInventory(Player player) {}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
		return this.isItemValidForSlot(index, itemStackIn);
	}

	@Override
	public AbstractContainerMenu createContainer(Inventory inventory, Player player) {
		return new ContainerAmmoFurnace(inventory, this);
	}

	@Override
	public void clear() {
		for (int i = 0; i < getInventory().size(); ++i)
			getInventory().set(i, ItemStack.EMPTY);
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : getInventory()) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public abstract boolean isBurning();

	protected abstract void tickFuel();

	protected abstract NonNullList<ItemStack> getInventory();

	protected abstract List<ItemStack> getOutput();
	
}
