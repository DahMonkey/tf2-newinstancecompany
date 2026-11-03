package rafradek.TF2weapons.tileentity;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rafradek.TF2weapons.block.BlockAmmoFurnace;
import rafradek.TF2weapons.inventory.IFieldHolder;
import rafradek.TF2weapons.item.crafting.ShapelessOreRecipe;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import rafradek.TF2weapons.item.crafting.TF2Ingredient;
import rafradek.TF2weapons.util.TF2Util;

/**
 * Ammo furnace: breaks ammo back down into its crafting ingredients. Slots 0-8 take ammo, 9 is fuel and
 * 10-18 hold the output.
 */
public abstract class TileEntityAbstractAmmoFurnace extends BlockEntity implements WorldlyContainer, IFieldHolder {
	protected int cookTime;
	protected int totalCookTime;
	protected Component furnaceCustomName;

	private ShapelessOreRecipe cachedRecipe;
	private int cachedSlot = -1;

	public TileEntityAbstractAmmoFurnace(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public int getContainerSize() {
		return getInventory().size();
	}

	@Override
	public ItemStack getItem(int index) {
		return getInventory().get(index);
	}

	public ItemStack getStackInSlot(int index) {
		return getItem(index);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		ItemStack stack = ContainerHelper.removeItem(getInventory(), index, count);
		if (!stack.isEmpty())
			this.setChanged();
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return ContainerHelper.takeItem(getInventory(), index);
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		boolean flag = !stack.isEmpty() && stack.sameItem(getInventory().get(index))
				&& ItemStack.tagMatches(stack, getInventory().get(index));
		getInventory().set(index, stack);

		if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize())
			stack.setCount(this.getMaxStackSize());

		if (index < 9 && !flag && index == cachedSlot) {
			this.totalCookTime = this.getCookTime(stack);
			this.cookTime = 0;
			this.setChanged();
		}
	}

	public void setInventorySlotContents(int index, ItemStack stack) {
		this.setItem(index, stack);
	}

	public boolean hasCustomName() {
		return this.furnaceCustomName != null;
	}

	public void setCustomInventoryName(Component name) {
		this.furnaceCustomName = name;
	}

	public Component getDisplayName() {
		return this.hasCustomName() ? this.furnaceCustomName : Component.translatable("container.ammofurnace");
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		NonNullList<ItemStack> inv = getInventory();
		for (int i = 0; i < inv.size(); i++)
			inv.set(i, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, inv);
		this.cookTime = compound.getInt("CookTime");
		this.totalCookTime = compound.getInt("CookTimeTotal");
		if (compound.contains("CustomName", 8))
			this.furnaceCustomName = Component.Serializer.fromJson(compound.getString("CustomName"));
		this.readExtra(compound);
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		ContainerHelper.saveAllItems(compound, getInventory());
		compound.putInt("CookTime", this.cookTime);
		compound.putInt("CookTimeTotal", this.totalCookTime);
		if (this.hasCustomName())
			compound.putString("CustomName", Component.Serializer.toJson(this.furnaceCustomName));
		this.writeExtra(compound);
	}

	protected void readExtra(CompoundTag compound) {
	}

	protected void writeExtra(CompoundTag compound) {
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, TileEntityAbstractAmmoFurnace tile) {
		tile.update();
	}

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
			BlockAmmoFurnace.setState(isBurning(), this.level, this.worldPosition);
		}
		if (flag1)
			this.setChanged();
	}

	public int getCookTime(@Nullable ItemStack stack) {
		return 200;
	}

	protected boolean canSmelt() {
		if (cachedSlot == -1 || cachedRecipe == null
				|| !TF2Util.matches(cachedRecipe.getRecipeOutput(), getInventory().get(cachedSlot))) {
			for (int i = 0; i < 9; i++)
				if (canFitRecipe(i))
					return true;
			cachedSlot = -1;
			cachedRecipe = null;
			return false;
		}
		return true;
	}

	private boolean canFitRecipe(int slot) {
		ItemStack base = getInventory().get(slot);
		if (base.isEmpty())
			return false;
		for (ShapelessOreRecipe recipe : TF2CraftingManager.AMMO_RECIPES) {
			if (recipe == null || recipe.getRecipeOutput() == null)
				continue;
			if (TF2Util.matches(recipe.getRecipeOutput(), base)) {
				int[] sizes = new int[9];
				List<ItemStack> items = Lists.newArrayList(getOutput());
				for (int i = 0; i < 9; i++)
					sizes[i] = items.get(i).getCount();
				for (TF2Ingredient output : recipe.getIngredients()) {
					if (output.getMatchingStacks().length == 0)
						break;
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
						if (i == 8)
							return false;
					}
				}
				cachedSlot = slot;
				cachedRecipe = recipe;
				return true;
			}
		}
		return false;
	}

	public void smeltItem() {
		if (!canSmelt())
			return;
		int ammoToConsume = cachedRecipe.getRecipeOutput().getCount();
		ItemStack base = getInventory().get(cachedSlot);
		int ammoConsumed = Math.min(base.getCount(), ammoToConsume);
		base.shrink(ammoConsumed);
		ammoToConsume -= ammoConsumed;
		if (base.getCount() <= 0)
			setItem(cachedSlot, ItemStack.EMPTY);

		if (ammoToConsume <= 0) {
			for (TF2Ingredient obj : cachedRecipe.getIngredients()) {
				ItemStack out = obj.getMatchingStacks()[0];
				for (int j = 10; j < 19; j++) {
					boolean handled = false;
					ItemStack inSlot = this.getItem(j);
					if (inSlot.isEmpty()) {
						this.setItem(j, out.copy());
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
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return this.level.getBlockEntity(this.worldPosition) == this && player.distanceToSqr(
				this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) <= 64.0D;
	}

	public boolean isUsableByPlayer(Player player) {
		return this.stillValid(player);
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
		return this.canPlaceItem(index, itemStackIn);
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return this.isItemValidForSlot(index, stack);
	}

	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return this.canExtractItem(index, stack, direction);
	}

	public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
		return true;
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < getInventory().size(); ++i)
			getInventory().set(i, ItemStack.EMPTY);
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : getInventory())
			if (!itemstack.isEmpty())
				return false;
		return true;
	}

	public abstract boolean isBurning();

	protected abstract void tickFuel();

	protected abstract NonNullList<ItemStack> getInventory();

	protected abstract List<ItemStack> getOutput();
}
