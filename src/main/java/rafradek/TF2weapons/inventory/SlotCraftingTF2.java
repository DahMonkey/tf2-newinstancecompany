package rafradek.TF2weapons.inventory;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.WeaponData;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;

public class SlotCraftingTF2 extends ResultSlot {

	private CraftingContainer craftMatrix;
	private Player player;

	public SlotCraftingTF2(Player player, CraftingContainer craftingInventory, AbstractContainerMenu inventoryIn,
			int slotIndex, int xPosition, int yPosition) {
		super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
		this.player = player;
		craftMatrix = craftingInventory;
	}

	@Override
	public ItemStack onTake(Player player, ItemStack stack) {
		if (player.level.isClientSide) return stack;
		if (stack.getItem() == TF2weapons.itemTF2 && stack.getDamageValue() == 9) {
			if (stack.hasTag()) {
				TF2Class clazz = TF2Class.getClass(stack.getTag().getByte("Token"));
				stack = ItemFromData.getRandomWeapon(player.getRandom(), Predicates.<WeaponData>and(
						ItemFromData.VISIBLE_WEAPON, test -> test.get(PropertyType.SLOT).containsKey(clazz.getName())));
			} else
				stack = ItemFromData.getRandomWeapon(player.getRandom(), ItemFromData.VISIBLE_WEAPON);
			// player.addStat(TF2Achievements.HOME_MADE);
			player.getInventory().setItemStack(stack);
		} else if (stack.getItem() == TF2weapons.itemTF2 && stack.getDamageValue() == 10) {
			stack = ItemFromData.getRandomWeaponOfType("cosmetic", player.getRandom(), false);
			player.getInventory().setItemStack(stack);
		}
		if (stack.hasTag() && stack.getTag().getBoolean("Australium")) {
			// player.addStat(TF2Achievements.SHINY);
		}
		net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(player, stack, craftMatrix);
		this.onCrafting(stack);
		net.minecraftforge.common.ForgeHooks.setCraftingPlayer(player);
		NonNullList<ItemStack> aitemstack = TF2CraftingManager.INSTANCE.getRemainingItems(this.craftMatrix,
				player.level);
		net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

		for (int i = 0; i < aitemstack.size(); ++i) {
			ItemStack itemstack = this.craftMatrix.getStackInSlot(i);
			ItemStack itemstack1 = aitemstack.get(i);

			if (!itemstack.isEmpty()) {
				this.craftMatrix.decrStackSize(i, 1);
				itemstack = this.craftMatrix.getStackInSlot(i);
			}

			if (!itemstack1.isEmpty())
				if (itemstack.isEmpty())
					this.craftMatrix.setInventorySlotContents(i, itemstack1);
				else if (ItemStack.isSame(itemstack, itemstack1)
						&& ItemStack.tagMatches(itemstack, itemstack1)) {
					itemstack1.grow(itemstack.getCount());
					this.craftMatrix.setInventorySlotContents(i, itemstack1);
				} else if (!this.player.getInventory().addItemStackToInventory(itemstack1))
					this.player.dropItem(itemstack1, false);
		}
		return stack;
	}
}
