package rafradek.TF2weapons.inventory;

import com.google.common.collect.Multimap;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerWearables extends AbstractContainerMenu {

	public static final String[] CURRENCY_EMPTY = { TF2weapons.MOD_ID + ":items/money_small_empty",
			TF2weapons.MOD_ID + ":items/money_medium_empty", TF2weapons.MOD_ID + ":items/money_big_empty" };

	private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[] {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
	/** The crafting matrix inventory. */
	/** Determines if inventory manipulation should be handled. */
	public boolean isLocalWorld;
	private final Player player;
	public AbstractContainerMenu wearables;
	public static IItemHandler nullInv = new ItemStackHandler(9);
	public boolean ammoBelt;

	public ContainerWearables(final Inventory inventory, final InventoryWearables wearables,
			boolean localWorld, Player player) {
		this.isLocalWorld = localWorld;
		this.player = player;
		this.wearables = wearables;
		for (int i = 0; i < 2; i++) {
			TF2PlayerCapability.get(player).wearablesAttrib[i] = wearables.getStackInSlot(i)
					.getAttributeModifiers(EquipmentSlot.HEAD);
		}
		TF2PlayerCapability.get(player).wearablesAttrib[2] = wearables.getStackInSlot(2)
				.getAttributeModifiers(EquipmentSlot.CHEST);
		TF2PlayerCapability.get(player).wearablesAttrib[2].removeAll(Attributes.ARMOR);
		TF2PlayerCapability.get(player).wearablesAttrib[2].removeAll(Attributes.ARMOR_TOUGHNESS);

		// slot 0-3
		for (int k = 0; k < 4; ++k) {
			final EquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[k];
			this.addSlot(new Slot(inventory, 36 + (3 - k), 8, 8 + k * 18) {
				/**
				 * Returns the maximum stack size for a given slot (usually the same as
				 * getInventoryStackLimit(), but 1 in the case of armor slots)
				 */
				@Override
				public int getSlotStackLimit() {
					return 1;
				}

				/**
				 * Check if the stack is a valid item for this slot. Always true beside for the
				 * armor slots.
				 */
				@Override
				public boolean isItemValid(@Nullable ItemStack stack) {
					if (stack.isEmpty())
						return false;
					else
						return stack.getItem().isValidArmor(stack, entityequipmentslot, ContainerWearables.this.player);
				}

				@Override
				@Nullable
				@OnlyIn(Dist.CLIENT)
				public String getSlotTexture() {
					return ArmorItem.EMPTY_SLOT_NAMES[entityequipmentslot.get3DDataValue()];
				}
			});
		}
		// slot 4-5
		for (int k = 0; k < 2; k++) {
			int l = k;
			this.addSlot(new Slot(wearables, k, 77, 8 + k * 18) {
				@Override
				public int getSlotStackLimit() {
					return 1;
				}

				@Override
				public void onSlotChanged() {
					super.setChanged();
					if (!ContainerWearables.this.player.level.isClientSide) {
						// System.out.println("changed");
						if (TF2PlayerCapability.get(ContainerWearables.this.player).wearablesAttrib[l] != null) {
							ContainerWearables.this.player.getAttributes().removeAttributeModifiers(
									TF2PlayerCapability.get(ContainerWearables.this.player).wearablesAttrib[l]);
							TF2PlayerCapability.get(ContainerWearables.this.player).wearablesAttrib[l] = null;
						}

						if (!this.getStack().isEmpty()) {
							Multimap<String, AttributeModifier> modifiers = this.getStack()
									.getAttributeModifiers(EquipmentSlot.HEAD);
							ContainerWearables.this.player.getAttributes().applyAttributeModifiers(modifiers);
							TF2PlayerCapability.get(ContainerWearables.this.player).wearablesAttrib[l] = modifiers;
						}
						TF2Util.sendTracking(new TF2Message.WearableChangeMessage(ContainerWearables.this.player,
								this.getSlotIndex(), this.getStack()), ContainerWearables.this.player);
					}
				}

				/**
				 * Check if the stack is a valid item for this slot. Always true beside for the
				 * armor slots.
				 */
				@Override
				public boolean isItemValid(@Nullable ItemStack stack) {
					if (stack.isEmpty())
						return false;
					else
						return stack.getItem() instanceof ItemWearable;
				}

				@Override
				@Nullable
				@OnlyIn(Dist.CLIENT)
				public String getSlotTexture() {
					return ArmorItem.EMPTY_SLOT_NAMES[EquipmentSlot.HEAD.get3DDataValue()];
				}
			});
		}
		// slot 6
		this.addSlot(new Slot(wearables, 2, 77, 44) {
			/**
			 * Check if the stack is a valid item for this slot. Always true beside for the
			 * armor slots.
			 */
			@Override
			public boolean isItemValid(@Nullable ItemStack stack) {
				return stack.getItem() instanceof ItemFromData
						&& stack.getItem().isValidArmor(stack, EquipmentSlot.CHEST, player);
			}

			@Override
			@Nullable
			@OnlyIn(Dist.CLIENT)
			public String getSlotTexture() {
				return ArmorItem.EMPTY_SLOT_NAMES[EquipmentSlot.CHEST.get3DDataValue()];
			}

			@Override
			public void onSlotChanged() {
				super.setChanged();
				if (!ContainerWearables.this.player.level.isClientSide) {
					// System.out.println("changed");
					if (TF2PlayerCapability.get(ContainerWearables.this.player).wearablesAttrib[2] != null) {
						ContainerWearables.this.player.getAttributes().removeAttributeModifiers(
								TF2PlayerCapability.get(ContainerWearables.this.player).wearablesAttrib[2]);
						TF2PlayerCapability.get(ContainerWearables.this.player).wearablesAttrib[2] = null;
					}

					if (!this.getStack().isEmpty()) {
						Multimap<String, AttributeModifier> modifiers = this.getStack()
								.getAttributeModifiers(EquipmentSlot.CHEST);
						modifiers.removeAll(Attributes.ARMOR.getName());
						modifiers.removeAll(Attributes.ARMOR_TOUGHNESS.getName());
						ContainerWearables.this.player.getAttributes().applyAttributeModifiers(modifiers);
						TF2PlayerCapability.get(ContainerWearables.this.player).wearablesAttrib[2] = modifiers;
					}
					TF2Util.sendTracking(new TF2Message.WearableChangeMessage(ContainerWearables.this.player,
							this.getSlotIndex(), this.getStack()), ContainerWearables.this.player);
				}
			}
		});
		// slot 7
		this.addSlot(new Slot(wearables, 3, 154, 28) {
			@Override
			public int getSlotStackLimit() {
				return 1;
			}

			/**
			 * Check if the stack is a valid item for this slot. Always true beside for the
			 * armor slots.
			 */
			@Override
			public boolean isItemValid(@Nullable ItemStack stack) {
				if (stack.isEmpty())
					return false;
				else
					return stack.getItem() instanceof ItemAmmoBelt;
			}

			@Override
			public void onSlotChanged() {

				super.setChanged();
				addSlotAmmo();
			}

			@Override
			@Nullable
			@OnlyIn(Dist.CLIENT)
			public String getSlotTexture() {
				return TF2weapons.MOD_ID + ":items/ammo_belt_empty";
			}
		});
		// slot 8-34
		for (int l = 0; l < 3; ++l)
			for (int j1 = 0; j1 < 9; ++j1)
				this.addSlot(new Slot(inventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));

		// slot 35-43
		for (int i1 = 0; i1 < 9; ++i1)
			this.addSlot(new Slot(inventory, i1, 8 + i1 * 18, 142));

		// slot 44
		this.addSlot(new Slot(wearables, 4, 77, 62) {
			/**
			 * Check if the stack is a valid item for this slot. Always true beside for the
			 * armor slots.
			 */
			@Override
			public boolean isItemValid(@Nullable ItemStack stack) {
				return stack.getItem() instanceof ItemToken;
			}

			@Override
			@Nullable
			@OnlyIn(Dist.CLIENT)
			public String getSlotTexture() {
				return TF2weapons.MOD_ID + ":items/token_empty";
			}
		});

		// slot 45-47
		for (int k = 0; k < 3; k++) {
			int l = k;
			this.addSlot(new Slot(wearables, k + 5, 175, 8 + k * 18) {

				@Override
				public void onSlotChanged() {
					super.setChanged();
					if (!ContainerWearables.this.player.level.isClientSide) {
						// System.out.println("changed");

					}
				}

				/**
				 * Check if the stack is a valid item for this slot. Always true beside for the
				 * armor slots.
				 */
				@Override
				public boolean isItemValid(@Nullable ItemStack stack) {
					if (stack.isEmpty())
						return false;
					else
						return stack.getItem() instanceof ItemMoney && stack.getDamageValue() == l;
				}

				@Override
				@Nullable
				@OnlyIn(Dist.CLIENT)
				public String getSlotTexture() {
					return CURRENCY_EMPTY[l];
				}
			});
		}

		// slot 48-56
		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 3; ++j)
				addSlot(new SlotItemHandler(nullInv, j + i * 3, 98 + j * 18, 18 + i * 18) {

					@Override
					public boolean isItemValid(@Nullable ItemStack stack) {
						return false;
					}
				});

		addSlotAmmo();
		// if(wearables.getStackInSlot(3)!=null &&
		// wearables.getStackInSlot(3).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
		// null))
		// this.addSlotAmmo();
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */

	/**
	 * Called when the container is closed.
	 */

	@Override
	public void onContainerClosed(Player player) {
		super.onContainerClosed(player);

		if (!player.level.isClientSide && !WeaponsCapability.get(player).forcedClass)
			((ItemToken) TF2weapons.itemToken).updateAttributes(this.wearables.getStackInSlot(4), player);
		/*
		 * if (this.wearables.getStackInSlot(3) == null) for (int i = 4; i < 13; ++i) {
		 * ItemStack itemstack = this.wearables.removeStackFromSlot(i);
		 * 
		 * if (item!stack.isEmpty()) player.drop(itemstack, false); }
		 */
	}

	@Override
	public boolean canInteractWith(Player player) {
		return true;
	}

	public void addSlotAmmo() {
		IItemHandler handler = nullInv;
		ammoBelt = false;
		if (!this.wearables.getStackInSlot(3).isEmpty() && this.wearables.getStackInSlot(3)
				.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
			handler = this.wearables.getStackInSlot(3).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
					null);
			ammoBelt = true;
		}
		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 3; ++j) {
				Slot slot = new SlotItemHandler(handler, j + i * 3, 98 + j * 18, 18 + i * 18) {

					@Override
					public boolean isItemValid(@Nullable ItemStack stack) {
						return this.getItemHandler() != nullInv && !stack.isEmpty()
								&& stack.getItem() instanceof ItemAmmo && super.isItemValid(stack);
					}
				};
				this.slots.set(this.slots.size() - 9 + j + i * 3, slot);
				slot.slotNumber = this.slots.size() - 9 + j + i * 3;
			}
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
			EquipmentSlot entityequipmentslot = Mob.getSlotForItemStack(itemstack);

			if (index >= 0 && index < 8) {
				if (!this.moveItemStackTo(itemstack1, 8, 44, false))
					return ItemStack.EMPTY;
			} else if (index >= 45 && index < 58) {
				if (!this.moveItemStackTo(itemstack1, 8, 44, false))
					return ItemStack.EMPTY;
			} else if (itemstack1.getItem() instanceof ItemBackpack && index >= 8) {
				if (!this.moveItemStackTo(itemstack1, 6, 7, false))
					return ItemStack.EMPTY;
			} else if (entityequipmentslot.getSlotType() == EquipmentSlot.Type.ARMOR
					&& !this.slots.get(4 - entityequipmentslot.getSlotIndex()).hasItem()) {
				int i = 4 - entityequipmentslot.getSlotIndex();

				if (!this.moveItemStackTo(itemstack1, i, i + 1, false))
					return ItemStack.EMPTY;
			} else if (itemstack1.getItem() instanceof ItemToken && !this.slots.get(44).hasItem()) {
				if (!this.moveItemStackTo(itemstack1, 44, 45, false))
					return ItemStack.EMPTY;
			} else if (itemstack1.getItem() instanceof ItemAmmoBelt && !this.slots.get(7).hasItem()) {
				if (!this.moveItemStackTo(itemstack1, 7, 8, false))
					return ItemStack.EMPTY;
			} else if (itemstack1.getItem() instanceof ItemWearable && index >= 8) {
				if (!this.moveItemStackTo(itemstack1, 4, 6, false))
					return ItemStack.EMPTY;
			} else if (itemstack1.getItem() instanceof ItemAmmo && ammoBelt && index < 44) {
				if (!this.moveItemStackTo(itemstack1, 48, 57, false))
					return ItemStack.EMPTY;
			} else if (itemstack1.getItem() instanceof ItemMoney) {
				if (!this.moveItemStackTo(itemstack1, itemstack1.getDamageValue() + 45, itemstack1.getDamageValue() + 48,
						false))
					return ItemStack.EMPTY;
			} else if (index >= 8 && index < 35) {
				if (!this.moveItemStackTo(itemstack1, 35, 44, false))
					return ItemStack.EMPTY;
			} else if (index >= 35 && index < 44) {
				if (!this.moveItemStackTo(itemstack1, 8, 35, false))
					return ItemStack.EMPTY;
			} else if (!this.moveItemStackTo(itemstack1, 8, 44, false))
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

	/**
	 * Called to determine if the current slot is valid for the stack merging
	 * (double-click) code. The stack passed in is null for the initial slot that
	 * was double-clicked.
	 */
}