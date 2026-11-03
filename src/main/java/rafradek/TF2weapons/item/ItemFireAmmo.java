package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemFireAmmo extends ItemAmmo {

	public int uses;
	int type;

	public ItemFireAmmo(int type, int uses, int maxStack) {
		this.type = type;
		this.uses = uses;
		this.setMaxStackSize(maxStack);
		this.setHasSubtypes(false);
	}

	@Override
	public int getTypeInt(ItemStack stack) {
		return type;
	}

	@Override
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.allowedIn(par2CreativeTabs))
			return;
		par3List.add(new ItemStack(this));
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return uses - 1;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getMaxStackSize(ItemStack stack) {
		return this.getMaxStackSize();
	}

	public int restoreAmmo(ItemStack stack, int amount) {
		if (amount > 0) {
			if (stack.getCount() == 1) {
				int itemDamage = stack.getDamageValue();
				stack.setDamageValue(Math.max(0, itemDamage - amount));
				return Math.min(itemDamage, amount);
			}
		}
		return 0;
	}

	@Override
	public int consumeAmmo(LivingEntity living, ItemStack stack, int amount) {
		if (stack == STACK_FILL)
			return 0;
		if (amount > 0) {
			int left = Math.max(0, amount - this.getAmount(stack));
			if (stack.getCount() > 1) {
				ItemStack remain = stack.split(1);
				remain.hurtAndBreak(amount, living, broken -> {});
				if (living instanceof Player)
					remain = TF2Util.pickAmmo(remain, (Player) living, true);
				if (!remain.isEmpty())
					living.spawnAtLocation(remain, 0).setPickUpDelay(0);
			} else {
				stack.setDamageValue(stack.getDamageValue() + amount);
				if (stack.getDamageValue() > stack.getMaxDamage())
					stack.shrink(1);
			}

			return left;
			/*
			 * if (stack.getCount() <= 0 && living instanceof EntityPlayer) { if
			 * (!living.getCapability(TF2weapons.INVENTORY_CAP,
			 * null).getStackInSlot(3).isEmpty()){ IItemHandlerModifiable invAmmo =
			 * (IItemHandlerModifiable) living.getCapability(TF2weapons.INVENTORY_CAP,
			 * null).getStackInSlot(3)
			 * .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null); for (int
			 * i = 0; i < invAmmo.getSlots(); i++) { ItemStack stackInv =
			 * invAmmo.getStackInSlot(i); if (stack == stackInv) { invAmmo.setStackInSlot(i,
			 * ItemStack.EMPTY); return; } } } ((EntityPlayer)
			 * living).inventory.removeItem(stack);
			 * 
			 * }
			 */
		}
		return 0;
	}

	@Override
	public int getAmount(ItemStack stack) {
		return (uses - stack.getDamageValue()) * stack.getCount();
	}
}
