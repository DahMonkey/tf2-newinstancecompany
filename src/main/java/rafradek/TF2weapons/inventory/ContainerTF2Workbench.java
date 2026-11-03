package rafradek.TF2weapons.inventory;

import rafradek.TF2weapons.item.crafting.TF2Recipe;
import net.minecraft.world.Container;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemBuildingBox;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWrench;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import rafradek.TF2weapons.util.TF2Util;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ContainerTF2Workbench extends LegacyContainer {
	/** The crafting matrix inventory (3x3). */
	public CraftingContainer craftMatrix = new CraftingContainer(this, 3, 3);
	public net.minecraft.world.inventory.ResultContainer craftResult = new net.minecraft.world.inventory.ResultContainer();
	public final Level world;
	/** Position of the workbench */
	public final BlockPos pos;
	public Player player;
	public int currentRecipe = -1;

	public ContainerTF2Workbench(Player player, Inventory inventory, Level world, BlockPos posIn) {
		this.player = player;
		this.world = world;
		this.pos = posIn;
		this.addSlot(
				new SlotCraftingTF2(inventory.player, this.craftMatrix, this.craftResult, 0, 148, 41));

		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 3; ++j)
				this.addSlot(new Slot(this.craftMatrix, j + i * 3, 86 + j * 18, 23 + i * 18) {

				});

		/*
		 * for (int k = 0; k < 3; ++k) { for (int i1 = 0; i1 < 9; ++i1) {
		 * this.addSlotToContainer(new Slot(cabinet, i1 + k * 9 + 9, 8 + i1 * 18, 91 + k
		 * * 18)); } }
		 */

		for (int k = 0; k < 3; ++k)
			for (int i1 = 0; i1 < 9; ++i1)
				this.addSlot(new Slot(inventory, i1 + k * 9 + 9, 8 + i1 * 18, 98 + k * 18));

		for (int l = 0; l < 9; ++l)
			this.addSlot(new Slot(inventory, l, 8 + l * 18, 156));

		this.onCraftMatrixChanged(this.craftMatrix);
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@Override
	public void onCraftMatrixChanged(Container inventoryIn) {
		ItemStack stack = ItemStack.EMPTY;
		List<TF2Recipe> recipes = TF2CraftingManager.INSTANCE.getRecipeList();
		if (currentRecipe >= 0 && currentRecipe < recipes.size()
				&& recipes.get(currentRecipe).matches(this.craftMatrix, world))
			stack = getReplacement(player, recipes.get(currentRecipe).getCraftingResult(this.craftMatrix));
		// ?TF2CraftingManager.INSTANCE.getRecipeList().get(currentRecipe)TF2CraftingManager.INSTANCE.findMatchingRecipe(this.craftMatrix,
		// this.world);
		else
			stack = getReplacement(player,
					TF2CraftingManager.INSTANCE.findMatchingRecipe(this.craftMatrix, this.world, this.player));
		this.craftResult.setItem(0, stack);
	}

	public static ItemStack getReplacement(Player player, ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() instanceof ItemBuildingBox
				&& player.getTeam() == player.level.getScoreboard().getPlayerTeam("BLU"))
			stack.setDamageValue(stack.getDamageValue() + 1);
		if (!stack.isEmpty() && stack.getItem() == net.minecraft.world.item.Items.RED_BANNER) {
			CompoundTag pattern = new CompoundTag();
			if (player.getTeam() == player.level.getScoreboard().getPlayerTeam("BLU")) {
				ItemStack blue = new ItemStack(net.minecraft.world.item.Items.BLUE_BANNER, stack.getCount());
				blue.setTag(stack.getTag());
				stack = blue;
				pattern.putString("Pattern", "bb");
				pattern.putInt("Color", 15);
			} else {
				pattern.putString("Pattern", "rb");
				pattern.putInt("Color", 15);
			}

			CompoundTag blockTag = stack.getOrCreateTagElement("BlockEntityTag");
			net.minecraft.nbt.ListTag patterns = blockTag.getList("Patterns", 10);
			patterns.add(pattern);
			blockTag.put("Patterns", patterns);
		}
		if (!stack.isEmpty() && stack.getItem() instanceof ItemBuildingBox) {
			if (stack.getDamageValue() / 2 == 11) {
				ItemStack wrench = TF2Util.getFirstItem(player.getInventory(),
						stackL -> TF2Attribute.getModifier("Teleporter Cost", stackL, 1, player) != 1);
				if (!wrench.isEmpty()) {
					stack.setCount((int) TF2Attribute.getModifier("Teleporter Cost", wrench, 1, player));
				}
			} else if (stack.getDamageValue() / 2 == 9) {
				ItemStack gunslinger = TF2Util.getFirstItem(player.getInventory(),
						stackL -> stackL.getItem() instanceof ItemWrench
								&& TF2Attribute.getModifier("Weapon Mode", stackL, 0, player) == 2);
				ItemStack bonusSentry = TF2Util.getFirstItem(player.getInventory(),
						stackL -> TF2Attribute.getModifier("Sentry Bonus", stackL, 1, player) != 1);
				if (!gunslinger.isEmpty()) {
					stack.setTag(new CompoundTag());
					stack.getTag().putBoolean("Mini", true);
					stack.setCount(2);
				}

				if (!bonusSentry.isEmpty()) {
					stack.setCount((int) TF2Attribute.getModifier("Sentry Bonus", bonusSentry, 1, player));
				}
			}
		}
		return stack;
	}

	/**
	 * Called when the container is closed.
	 */
	@Override
	public void onContainerClosed(Player player) {
		super.onContainerClosed(player);

		if (!this.world.isClientSide)
			for (int i = 0; i < 9; ++i) {
				ItemStack itemstack = this.craftMatrix.removeItemNoUpdate(i);

				if (!itemstack.isEmpty())
					player.drop(itemstack, false);
			}
	}

	@Override
	public boolean canInteractWith(Player player) {
		return this.world.getBlockState(this.pos).getBlock() != TF2weapons.blockCabinet ? false
				: player.distanceToSqr(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
						this.pos.getZ() + 0.5D) <= 64.0D;
	}

	/**
	 * Take a stack from the specified inventory slot.
	 */
	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();

			if (itemstack1.getItem() == TF2weapons.itemTF2 && itemstack1.getDamageValue() == 9 &! player.level.isClientSide)
				itemstack1 = ItemFromData.getRandomWeapon(itemstack1, player.getRandom());
			else if (itemstack1.getItem() == TF2weapons.itemTF2 && itemstack1.getDamageValue() == 10 &! player.level.isClientSide)
				itemstack1 = ItemFromData.getRandomWeaponOfType("cosmetic", player.getRandom(), false);
			itemstack = itemstack1.copy();

			if (index == 0) {
				if (!this.moveItemStackTo(itemstack1, 10, 46, true))
					return ItemStack.EMPTY;

				slot.onQuickCraft(itemstack1, itemstack);
			} else if (index >= 10 && index < 37) {
				if (!this.moveItemStackTo(itemstack1, 37, 46, false))
					return ItemStack.EMPTY;
			} else if (index >= 37 && index < 46) {
				if (!this.moveItemStackTo(itemstack1, 10, 37, false))
					return ItemStack.EMPTY;
			} else if (!this.moveItemStackTo(itemstack1, 10, 46, false))
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
	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
		return slotIn.container != this.craftResult && super.canMergeSlot(stack, slotIn);
	}

	@Override
	public boolean enchantItem(Player player, int id) {
		this.currentRecipe = id;
		this.onCraftMatrixChanged(null);
		return true;
	}

	public net.minecraft.world.inventory.ResultContainer getCraftResult() {
		return this.craftResult;
	}

	public CraftingContainer getCraftMatrix() {
		return this.craftMatrix;
	}
}
