package rafradek.TF2weapons.inventory;

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

public class ContainerAmmoFurnace extends AbstractContainerMenu {

	private final AbstractContainerMenu tileFurnace;
	private int cookTime;
	private int totalCookTime;
	private int furnaceBurnTime;
	private int currentItemBurnTime;

	public ContainerAmmoFurnace(Inventory inventory, AbstractContainerMenu furnaceInventory) {
		this.tileFurnace = furnaceInventory;
		for (int i = 0; i < 9; i++)
			this.addSlot(new Slot(furnaceInventory, i, 9 + (i % 3) * 18, 17 + (i / 3) * 18));
		this.addSlot(new SlotFurnaceFuel(furnaceInventory, 9, 80, 53));
		for (int i = 0; i < 9; i++)
			this.addSlot(new SlotFurnaceOutput(inventory.player, furnaceInventory, i + 10,
					116 + (i % 3) * 18, 17 + (i / 3) * 18));

		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 9; ++j)
				this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

		for (int k = 0; k < 9; ++k)
			this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
	}

	@Override
	public void addListener(ContainerListener listener) {
		super.addListener(listener);
		listener.sendAllWindowProperties(this, this.tileFurnace);
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (int i = 0; i < this.listeners.size(); ++i) {
			ContainerListener icontainerlistener = this.listeners.get(i);

			if (this.cookTime != this.tileFurnace.getField(2))
				icontainerlistener.sendWindowProperty(this, 2, this.tileFurnace.getField(2));

			if (this.furnaceBurnTime != this.tileFurnace.getField(0))
				icontainerlistener.sendWindowProperty(this, 0, this.tileFurnace.getField(0));

			if (this.currentItemBurnTime != this.tileFurnace.getField(1))
				icontainerlistener.sendWindowProperty(this, 1, this.tileFurnace.getField(1));

			if (this.totalCookTime != this.tileFurnace.getField(3))
				icontainerlistener.sendWindowProperty(this, 3, this.tileFurnace.getField(3));
		}

		this.cookTime = this.tileFurnace.getField(2);
		this.furnaceBurnTime = this.tileFurnace.getField(0);
		this.currentItemBurnTime = this.tileFurnace.getField(1);
		this.totalCookTime = this.tileFurnace.getField(3);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void updateProgressBar(int id, int data) {
		this.tileFurnace.setField(id, data);
	}

	@Override
	public boolean canInteractWith(Player player) {
		return this.tileFurnace.isUsableByPlayer(player);
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

			if (index > 9 && index < 19) {
				if (!this.moveItemStackTo(itemstack1, 19, 55, true))
					return ItemStack.EMPTY;

				slot.onSlotChange(itemstack1, itemstack);
			} else if (index > 9) {
				if (itemstack1.getItem() instanceof ItemAmmo) {
					if (!this.moveItemStackTo(itemstack1, 0, 9, false))
						return ItemStack.EMPTY;
				} else if (AbstractFurnaceBlockEntity.isItemFuel(itemstack1)) {
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
