package rafradek.TF2weapons.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IItemNoSwitch {

	boolean stopSlotSwitch(ItemStack stack, LivingEntity living);

	default void forceItemSlot(ItemStack stack, Level world, Entity entityIn, int itemSlot, boolean isSelected) {
		if (!world.isClientSide) {
			if (!isSelected && this.stopSlotSwitch(stack, (Player) entityIn)) {
				ItemStack old = ((Player) entityIn).getMainHandItem();
				if (stack == ((Player) entityIn).getInventory().getItem(itemSlot)) {
					if (old.getItem() instanceof IItemNoSwitch
							&& ((IItemNoSwitch) old.getItem()).stopSlotSwitch(old, (Player) entityIn)) {
						((Player) entityIn).getInventory().setItem(itemSlot, ItemStack.EMPTY);
						((Player) entityIn).dropItem(stack, true);
					} else {
						((Player) entityIn).getInventory().setItem(itemSlot, old);
						((Player) entityIn).setItemInHand(InteractionHand.MAIN_HAND, stack);
					}
				} else if (stack == ((Player) entityIn).getOffhandItem()) {
					((Player) entityIn).dropItem(stack, true);
					((Player) entityIn).getInventory().offhand.set(0, ItemStack.EMPTY);
				}
			}
		}
	}
}