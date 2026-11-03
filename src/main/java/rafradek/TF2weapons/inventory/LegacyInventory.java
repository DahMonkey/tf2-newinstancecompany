package rafradek.TF2weapons.inventory;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** SimpleContainer with the 1.12 InventoryBasic method names the mod's inventories use. */
public class LegacyInventory extends SimpleContainer {
	private final String name;

	public LegacyInventory(String name, boolean customName, int size) {
		super(size);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public ItemStack getStackInSlot(int index) {
		return this.getItem(index);
	}

	public void setInventorySlotContents(int index, ItemStack stack) {
		this.setItem(index, stack);
	}

	public ItemStack decrStackSize(int index, int count) {
		return this.removeItem(index, count);
	}

	public ItemStack removeStackFromSlot(int index) {
		return this.removeItemNoUpdate(index);
	}

	public int getInventoryStackLimit() {
		return this.getMaxStackSize();
	}

	public boolean isUsableByPlayer(Player player) {
		return this.stillValid(player);
	}

	public void markDirty() {
		this.setChanged();
	}
}
