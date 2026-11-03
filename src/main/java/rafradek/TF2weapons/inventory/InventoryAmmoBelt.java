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

public class InventoryAmmoBelt extends LegacyInventory implements ICapabilityProvider, INBTSerializable<ListTag> {

	public InventoryAmmoBelt() {
		super("AmmoBelt", false, 9);
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < this.getContainerSize(); i++)
			if (this.getStackInSlot(0) != null)
				return false;
		return true;
	}

	public boolean hasCapabilityLegacy(Capability<?> capability, Direction facing) {
		return TF2weapons.INVENTORY_BELT_CAP != null && capability == TF2weapons.INVENTORY_BELT_CAP;
	}

	@Override
	public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(Capability<T> capability, @javax.annotation.Nullable Direction facing) {
		T value = this.getCapabilityLegacy(capability, facing);
		return value == null ? net.minecraftforge.common.util.LazyOptional.empty() : net.minecraftforge.common.util.LazyOptional.of(() -> value);
	}

	public <T> T getCapabilityLegacy(Capability<T> capability, Direction facing) {
		if (TF2weapons.INVENTORY_BELT_CAP != null && capability == TF2weapons.INVENTORY_BELT_CAP)
			return (T) (Object) this;
		return null;
	}

	@Override
	public ListTag serializeNBT() {
		ListTag list = new ListTag();
		for (int i = 0; i < this.getContainerSize(); i++) {
			ItemStack itemstack = this.getStackInSlot(i);

			if (!itemstack.isEmpty()) {
				CompoundTag nbttagcompound = new CompoundTag();
				nbttagcompound.putByte("Slot", (byte) i);
				itemstack.save(nbttagcompound);
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
			this.setItem(j, ItemStack.of(nbttagcompound));
		}
		// System.out.println("Reading ");
	}
}
