package rafradek.TF2weapons.inventory;

import rafradek.TF2weapons.TF2weapons;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

public class InventoryAmmoBelt extends SimpleContainer implements ICapabilityProvider, INBTSerializable<ListTag> {

	public InventoryAmmoBelt() {
		super("AmmoBelt", false, 9);
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < this.getSizeInventory(); i++)
			if (this.getStackInSlot(0) != null)
				return false;
		return true;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing) {
		return TF2weapons.INVENTORY_BELT_CAP != null && capability == TF2weapons.INVENTORY_BELT_CAP;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing) {
		if (TF2weapons.INVENTORY_BELT_CAP != null && capability == TF2weapons.INVENTORY_BELT_CAP)
			return TF2weapons.INVENTORY_BELT_CAP.cast(this);
		return null;
	}

	@Override
	public ListTag serializeNBT() {
		ListTag list = new ListTag();
		for (int i = 0; i < this.getSizeInventory(); i++) {
			ItemStack itemstack = this.getStackInSlot(i);

			if (!itemstack.isEmpty()) {
				CompoundTag nbttagcompound = new CompoundTag();
				nbttagcompound.putByte("Slot", (byte) i);
				itemstack.writeToNBT(nbttagcompound);
				list.add(nbttagcompound);
			}
		}
		// System.out.println("Saving ");
		return list;
	}

	@Override
	public void deserializeNBT(ListTag nbt) {

		for (int i = 0; i < nbt.size(); ++i) {
			CompoundTag nbttagcompound = nbt.getCompound(i);
			int j = nbttagcompound.getByte("Slot");
			this.setInventorySlotContents(j, new ItemStack(nbttagcompound));
		}
		// System.out.println("Reading ");
	}
}
