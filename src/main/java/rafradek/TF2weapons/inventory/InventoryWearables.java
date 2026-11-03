package rafradek.TF2weapons.inventory;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.item.ItemToken;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

public class InventoryWearables extends SimpleContainer implements ICapabilityProvider, INBTSerializable<ListTag> {

	public LivingEntity owner;
	public static final int USED_SLOTS = 8;
	private final NonNullList<ItemStack> inventoryContentsOld;
	public ItemStack origHead = ItemStack.EMPTY;

	public InventoryWearables(LivingEntity ply) {
		super("Wearables", false, 13);
		this.inventoryContentsOld = NonNullList.withSize(13, ItemStack.EMPTY);
		owner = ply;
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < USED_SLOTS; i++)
			if (!this.getStackInSlot(i).isEmpty())
				return false;
		return true;
	}

	public void updateSlots() {
		for (int i = 0; i < USED_SLOTS; i++) {
			ItemStack stack = this.getStackInSlot(i);
			ItemStack old = inventoryContentsOld.get(i);
			if (!ItemStack.matches(stack, old)) {
				inventoryContentsOld.set(i, old);
				TF2Util.sendTracking(new TF2Message.WearableChangeMessage(owner, i, stack), owner);
			}
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing) {
		return TF2weapons.INVENTORY_CAP != null && capability == TF2weapons.INVENTORY_CAP;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing) {
		if (TF2weapons.INVENTORY_CAP != null && capability == TF2weapons.INVENTORY_CAP)
			return TF2weapons.INVENTORY_CAP.cast(this);
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
		if (!WeaponsCapability.get(owner).forcedClass) {
			ItemStack token = this.getStackInSlot(4);
			((ItemToken) TF2weapons.itemToken).updateAttributes(token, owner);
		}
		// System.out.println("Reading ");
	}

}
