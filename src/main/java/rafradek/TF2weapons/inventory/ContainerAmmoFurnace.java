package rafradek.TF2weapons.inventory;

import net.minecraft.world.Container;
import rafradek.TF2weapons.item.ItemAmmo;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ContainerAmmoFurnace extends LegacyContainer {

	public final Container tileFurnace;

	public ContainerAmmoFurnace(Inventory inventory, Container furnaceInventory) {
		this.tileFurnace = furnaceInventory;
		for (int i = 0; i < 9; i++)
			this.addSlot(new Slot(furnaceInventory, i, 9 + (i % 3) * 18, 17 + (i / 3) * 18));
		this.addSlot(new Slot(furnaceInventory, 9, 80, 53) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return net.minecraftforge.common.ForgeHooks.getBurnTime(stack, null) > 0
						|| stack.is(net.minecraft.world.item.Items.BUCKET);
			}
		});
		for (int i = 0; i < 9; i++)
			this.addSlot(new net.minecraft.world.inventory.FurnaceResultSlot(inventory.player, furnaceInventory, i + 10,
					116 + (i % 3) * 18, 17 + (i / 3) * 18));

		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 9; ++j)
				this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

		for (int k = 0; k < 9; ++k)
			this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
		if (furnaceInventory instanceof IFieldHolder)
			this.addDataSlots(((IFieldHolder) furnaceInventory).asContainerData());
	}

	@Override
	public boolean canInteractWith(Player player) {
		return this.tileFurnace.stillValid(player);
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
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();

			if (index > 9 && index < 19) {
				if (!this.moveItemStackTo(itemstack1, 19, 55, true))
					return ItemStack.EMPTY;

				slot.onQuickCraft(itemstack1, itemstack);
			} else if (index > 9) {
				if (itemstack1.getItem() instanceof ItemAmmo) {
					if (!this.moveItemStackTo(itemstack1, 0, 9, false))
						return ItemStack.EMPTY;
				} else if (net.minecraftforge.common.ForgeHooks.getBurnTime(itemstack1, null) > 0) {
					if (!this.moveItemStackTo(itemstack1, 9, 10, false))
						return ItemStack.EMPTY;
				} else if (index >= 19 && index < 46) {
					if (!this.moveItemStackTo(itemstack1, 46, 55, false))
						return ItemStack.EMPTY;
				} else if (index >= 46 && index < 55 && !this.moveItemStackTo(itemstack1, 19, 46, false))
					return ItemStack.EMPTY;
			} else if (!this.moveItemStackTo(itemstack1, 19, 55, false))
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
}
