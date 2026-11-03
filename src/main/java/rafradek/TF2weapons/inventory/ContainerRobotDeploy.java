package rafradek.TF2weapons.inventory;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemMoney;
import rafradek.TF2weapons.item.ItemRobotPart;
import rafradek.TF2weapons.tileentity.TileEntityRobotDeploy;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerRobotDeploy extends AbstractContainerMenu {

	private static final String[] EMPTY_NAMES = { TF2weapons.MOD_ID + ":items/robot_part_1_1_empty",
			TF2weapons.MOD_ID + ":items/robot_part_1_2_empty", TF2weapons.MOD_ID + ":items/robot_part_1_3_empty",
			TF2weapons.MOD_ID + ":items/robot_part_2_1_empty", TF2weapons.MOD_ID + ":items/robot_part_2_2_empty",
			TF2weapons.MOD_ID + ":items/robot_part_3_1_empty", TF2weapons.MOD_ID + ":items/robot_part_3_2_empty",
			TF2weapons.MOD_ID + ":items/weapon_empty_0" };
	TileEntityRobotDeploy tileEntity;
	public int progress = 0;
	public int maxprogress = 500;

	@Override
	public boolean canInteractWith(Player player) {
		return player.level.getBlockEntity(tileEntity.getBlockPos()) != tileEntity ? false
				: player.distanceToSqr(tileEntity.getBlockPos().getX() + 0.5D, tileEntity.getBlockPos().getY() + 0.5D,
						tileEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
	}

	public ContainerRobotDeploy(Inventory inventory, TileEntityRobotDeploy tileEntity) {

		this.tileEntity = tileEntity;
		for (int i = 0; i < 9; i++) {
			boolean first = i == 0;
			this.addSlot(new SlotItemHandler(tileEntity.weapon, i, 80 + (i % 3) * 18, 17 + (i / 3) * 18) {
				@Override
				@Nullable
				@OnlyIn(Dist.CLIENT)
				public String getSlotTexture() {
					return first ? EMPTY_NAMES[7] : null;
				}
			});
		}

		for (int i = 0; i < 3; i++) {
			this.addSlot(new SlotItemHandler(tileEntity.parts, i + 0, 9 + i * 18, 17) {
				@Override
				@Nullable
				@OnlyIn(Dist.CLIENT)
				public String getSlotTexture() {
					return EMPTY_NAMES[this.getSlotIndex()];
				}
			});
		}
		for (int i = 0; i < 2; i++) {
			this.addSlot(new SlotItemHandler(tileEntity.parts, i + 3, 9 + i * 18, 38) {
				@Override
				@Nullable
				@OnlyIn(Dist.CLIENT)
				public String getSlotTexture() {
					return EMPTY_NAMES[this.getSlotIndex()];
				}
			});
		}
		for (int i = 0; i < 2; i++) {
			this.addSlot(new SlotItemHandler(tileEntity.parts, i + 5, 9 + i * 18, 59) {
				@Override
				@Nullable
				@OnlyIn(Dist.CLIENT)
				public String getSlotTexture() {
					return EMPTY_NAMES[this.getSlotIndex()];
				}
			});
		}

		for (int i = 0; i < 3; i++) {
			this.addSlot(new SlotItemHandler(tileEntity.money, i, 9 + i * 18, 80) {
				@Override
				@Nullable
				@OnlyIn(Dist.CLIENT)
				public String getSlotTexture() {
					return ContainerWearables.CURRENCY_EMPTY[this.getSlotIndex()];
				}
			});
		}

		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 9; ++j)
				this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 111 + i * 18));

		for (int k = 0; k < 9; ++k)
			this.addSlot(new Slot(inventory, k, 8 + k * 18, 169));
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (int i = 0; i < this.listeners.size(); ++i) {
			ContainerListener icontainerlistener = this.listeners.get(i);

			if (this.progress != this.tileEntity.progress)
				icontainerlistener.sendWindowProperty(this, 0, this.tileEntity.progress);
			if (this.maxprogress != this.tileEntity.maxprogress)
				icontainerlistener.sendWindowProperty(this, 1, this.tileEntity.maxprogress);
		}
		this.progress = this.tileEntity.progress;
		this.maxprogress = this.tileEntity.maxprogress;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void updateProgressBar(int id, int data) {
		if (id == 0)
			progress = data;
		if (id == 1)
			maxprogress = data;
	}

	@Override
	@Nullable
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < 19) {
				if (!this.moveItemStackTo(itemstack1, 19, 55, true))
					return ItemStack.EMPTY;

				slot.onSlotChange(itemstack1, itemstack);
			} else {
				if (itemstack1.getItem() instanceof ItemRobotPart) {
					if (!this.getSlot(9 + itemstack1.getDamageValue()).isItemValid(itemstack1)
							|| !this.moveItemStackTo(itemstack1, itemstack1.getDamageValue() + 9,
									itemstack1.getDamageValue() + 10, false))
						return ItemStack.EMPTY;
				} else if (itemstack1.getItem() instanceof ItemMoney) {
					if (!this.getSlot(16 + itemstack1.getDamageValue()).isItemValid(itemstack1)
							|| !this.moveItemStackTo(itemstack1, itemstack1.getDamageValue() + 16,
									itemstack1.getDamageValue() + 17, false))
						return ItemStack.EMPTY;
				} else if (TF2Util.getWeaponUsedByClass(itemstack1) != null) {
					boolean merged = false;
					for (int i = 0; i < 9; i++) {
						if (this.getSlot(i).isItemValid(itemstack1)
								&& this.moveItemStackTo(itemstack1, i, i + 1, false)) {
							merged = true;
							break;
						}
					}
					if (!merged)
						return ItemStack.EMPTY;
				} else if (index >= 19 && index < 46) {
					if (!this.moveItemStackTo(itemstack1, 46, 55, false))
						return ItemStack.EMPTY;
				} else if (index >= 46 && index < 55 && !this.moveItemStackTo(itemstack1, 19, 46, false)) {
					return ItemStack.EMPTY;
				}
			}

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
