package rafradek.TF2weapons.inventory;

import net.minecraft.world.Container;
import rafradek.TF2weapons.TF2weapons;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerRecover extends LegacyContainer {

	private final ItemStackHandler lostItemsHandler;

	public ContainerRecover(Container inventory, ItemStackHandler dispenserInventoryIn) {
		this.lostItemsHandler = dispenserInventoryIn;

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new SlotItemHandler(dispenserInventoryIn, j + i * 9, 8 + j * 18, 18 + i * 18) {
					@Override
					public boolean isItemValid(@Nullable ItemStack stack) {
						return false;
					}
				});
			}
		}

		for (int k = 0; k < 3; ++k) {
			for (int i1 = 0; i1 < 9; ++i1) {
				this.addSlot(new Slot(inventory, i1 + k * 9 + 27, 8 + i1 * 18, 85 + k * 18));
			}
		}

		for (int l = 0; l < 9; ++l) {
			this.addSlot(new Slot(inventory, l, 8 + l * 18, 143));
		}
	}

	public static int getAustraliumCount(Player player) {
		int count = 0;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() == TF2weapons.itemTF2 && stack.getDamageValue() == 6)
					count++;
				else if (stack.getItem() == TF2weapons.itemTF2 && stack.getDamageValue() == 2)
					count += 9;
				else if (Block.getBlockFromItem(stack.getItem()) == TF2weapons.blockAustralium)
					count += 81;
			}
		}
		return count;
	}

	/**
	 * Determines whether supplied player can use this container
	 */
	@Override
	public boolean canInteractWith(Player player) {
		return true;
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

			if (index < 27) {
				if (!this.moveItemStackTo(itemstack1, 27, 63, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 0, 27, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.getCount() == 0) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemstack1);
		}

		return itemstack;
	}

}
