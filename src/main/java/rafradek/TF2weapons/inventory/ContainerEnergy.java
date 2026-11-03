package rafradek.TF2weapons.inventory;

import rafradek.TF2weapons.entity.building.EntityBuilding;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerEnergy extends AbstractContainerMenu {

	public ContainerEnergy(EntityBuilding building, Inventory inv) {
		this.addSlotToContainer(new SlotItemHandler(building.charge, 0, 7, 80) {
			@Override
			public boolean isItemValid(@Nullable ItemStack stack) {
				return stack.getCapability(CapabilityEnergy.ENERGY).isPresent() && super.isItemValid(stack);
			}
		});

		for (int k = 0; k < 3; ++k)
			for (int i1 = 0; i1 < 9; ++i1)
				this.addSlotToContainer(new Slot(inv, i1 + k * 9 + 9, 26 + i1 * 18, 113 + k * 18));

		for (int l = 0; l < 9; ++l)
			this.addSlotToContainer(new Slot(inv, l, 26 + l * 18, 171));
	}

	@Override
	public boolean canInteractWith(Player player) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index != 0 && itemstack1.getCapability(CapabilityEnergy.ENERGY).isPresent()) {
				if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 0 && index < 10) {
				if (!this.mergeItemStack(itemstack1, 10, 37, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 1, 10, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemstack1);
		}

		return itemstack;
	}
}
