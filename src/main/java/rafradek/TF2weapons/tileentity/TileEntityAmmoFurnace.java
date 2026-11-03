package rafradek.TF2weapons.tileentity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.inventory.IFieldHolder;

public class TileEntityAmmoFurnace extends TileEntityAbstractAmmoFurnace {
	private static final int[] SLOTS_TOP = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	private static final int[] SLOTS_BOTTOM = new int[] { 10, 11, 12, 13, 14, 15, 16, 17, 18, 9 };
	private static final int[] SLOTS_SIDES = new int[] { 9 };

	private final NonNullList<ItemStack> furnaceItemStacks = NonNullList.withSize(19, ItemStack.EMPTY);
	private int furnaceBurnTime;
	private int currentItemBurnTime;

	public TileEntityAmmoFurnace(BlockPos pos, BlockState state) {
		super(TF2weapons.TILE_AMMO_FURNACE, pos, state);
	}

	@Override
	protected void readExtra(CompoundTag compound) {
		this.furnaceBurnTime = compound.getInt("BurnTime");
		this.currentItemBurnTime = getItemBurnTime(this.furnaceItemStacks.get(9));
	}

	@Override
	protected void writeExtra(CompoundTag compound) {
		compound.putInt("BurnTime", this.furnaceBurnTime);
	}

	@Override
	public boolean isBurning() {
		return this.furnaceBurnTime > 0;
	}

	@Override
	protected void tickFuel() {
	}

	@Override
	protected NonNullList<ItemStack> getInventory() {
		return furnaceItemStacks;
	}

	@Override
	protected List<ItemStack> getOutput() {
		return furnaceItemStacks.subList(10, 19);
	}

	public static boolean isBurning(Container inventory) {
		return inventory instanceof IFieldHolder && ((IFieldHolder) inventory).getField(0) > 0;
	}

	public static int getItemBurnTime(ItemStack stack) {
		return ForgeHooks.getBurnTime(stack, null);
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return side == Direction.DOWN ? SLOTS_BOTTOM : (side == Direction.UP ? SLOTS_TOP : SLOTS_SIDES);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
		if (direction == Direction.DOWN && index == 9) {
			Item item = stack.getItem();
			if (item != Items.WATER_BUCKET && item != Items.BUCKET)
				return false;
		}
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return false;
	}

	@Override
	public int getField(int id) {
		switch (id) {
		case 0:
			return this.furnaceBurnTime;
		case 1:
			return this.currentItemBurnTime;
		case 2:
			return this.cookTime;
		case 3:
			return this.totalCookTime;
		default:
			return 0;
		}
	}

	@Override
	public void setField(int id, int value) {
		switch (id) {
		case 0:
			this.furnaceBurnTime = value;
			break;
		case 1:
			this.currentItemBurnTime = value;
			break;
		case 2:
			this.cookTime = value;
			break;
		case 3:
			this.totalCookTime = value;
		}
	}

	@Override
	public int getFieldCount() {
		return 4;
	}

	private final LazyOptional<IItemHandler> handlerTop = LazyOptional.of(() -> new SidedInvWrapper(this, Direction.UP));
	private final LazyOptional<IItemHandler> handlerBottom = LazyOptional
			.of(() -> new SidedInvWrapper(this, Direction.DOWN));
	private final LazyOptional<IItemHandler> handlerSide = LazyOptional
			.of(() -> new SidedInvWrapper(this, Direction.WEST));

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (facing != null && capability == ForgeCapabilities.ITEM_HANDLER)
			return (facing == Direction.DOWN ? handlerBottom : facing == Direction.UP ? handlerTop : handlerSide).cast();
		return super.getCapability(capability, facing);
	}
}
