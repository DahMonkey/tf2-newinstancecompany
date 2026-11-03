package rafradek.TF2weapons.inventory;

import net.minecraft.world.inventory.ContainerData;

/** 1.12 IInventory fields (getField/setField), synced to open menus through a ContainerData. */
public interface IFieldHolder {
	int getField(int id);

	void setField(int id, int value);

	int getFieldCount();

	default ContainerData asContainerData() {
		IFieldHolder holder = this;
		return new ContainerData() {
			@Override
			public int get(int index) {
				return holder.getField(index);
			}

			@Override
			public void set(int index, int value) {
				holder.setField(index, value);
			}

			@Override
			public int getCount() {
				return holder.getFieldCount();
			}
		};
	}
}
