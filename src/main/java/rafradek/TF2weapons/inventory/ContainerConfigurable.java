package rafradek.TF2weapons.inventory;

import rafradek.TF2weapons.tileentity.IEntityConfigurable;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ContainerConfigurable extends AbstractContainerMenu {
	private final Level world;
	private final BlockPos pos;
	public Player player;
	public IEntityConfigurable config;

	public ContainerConfigurable(Player player, Inventory inventory, IEntityConfigurable config,
			Level world, BlockPos posIn) {
		this.config = config;
		this.player = player;
		this.level = world;
		this.pos = posIn;
		/*
		 * this.addSlotToContainer(new Slot(upgradedItem, 0, 108, 8) {
		 * 
		 * @Override public void onSlotChanged() { super.onSlotChanged(); refreshData();
		 * transactions = new int[applicable.size()]; transactionsCost = new
		 * int[applicable.size()];
		 * 
		 * }
		 * 
		 * @Override public boolean isItemValid(@Nullable ItemStack stack) { if
		 * (stack.isEmpty()) return false; else return stack.getItem() instanceof
		 * ItemFromData && (stack.getMaxStackSize() == 1 || stack.getItem() instanceof
		 * ItemCleaver); } });
		 */

		/*
		 * for (int k = 0; k < 3; ++k) { for (int i1 = 0; i1 < 9; ++i1) {
		 * this.addSlotToContainer(new Slot(cabinet, i1 + k * 9 + 9, 8 + i1 * 18, 91 + k
		 * * 18)); } }
		 */

		for (int k = 0; k < 3; ++k)
			for (int i1 = 0; i1 < 9; ++i1)
				this.addSlot(new Slot(inventory, i1 + k * 9 + 9, 36 + i1 * 18, 143 + k * 18 + 300));

		for (int l = 0; l < 9; ++l)
			this.addSlot(new Slot(inventory, l, 36 + l * 18, 201 + 300));
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void updateProgressBar(int id, int data) {}

	@Override
	public void onContainerClosed(Player player) {
		super.onContainerClosed(player);

		if (!this.level.isClientSide) {}
	}

	@Override
	public boolean canInteractWith(Player player) {
		return this.level.getBlockEntity(pos) != this.config ? false
				: player.distanceToSqr(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
						this.pos.getZ() + 0.5D) <= 64.0D;
	}

	/**
	 * Take a stack from the specified inventory slot.
	 */
	@Override
	@Nullable
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index == 0) {
				if (!this.moveItemStackTo(itemstack1, 1, 28, true))
					return ItemStack.EMPTY;

				slot.onSlotChange(itemstack1, itemstack);
			} else if (index >= 1 && index < 28) {
				if (!this.moveItemStackTo(itemstack1, 0, 1, false))
					return ItemStack.EMPTY;
			} else if (!this.moveItemStackTo(itemstack1, 1, 28, false))
				return ItemStack.EMPTY;

			if (itemstack1.isEmpty())
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();

			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;

			slot.onTake(player, itemstack1);
		}

		return itemstack;
	}

	@Override
	public boolean enchantItem(Player player, int id) {

		return true;
	}
}
