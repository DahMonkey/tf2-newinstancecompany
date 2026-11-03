package rafradek.TF2weapons.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IItemSlotNumber {

	public boolean catchSlotHotkey(ItemStack stack, Player player);

	public void onSlotSelection(ItemStack stack, Player player, int slot);
}
