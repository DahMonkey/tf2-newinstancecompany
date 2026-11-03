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
				remain.hurtAndBreak(amount, living);
				if (living instanceof Player)
					remain = TF2Util.pickAmmo(remain, (Player) living, true);
				if (!remain.isEmpty())
					living.spawnAtLocation(remain, 0).setPickupDelay(0);
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
			 * .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null); for (int
			 * i = 0; i < invAmmo.getSlots(); i++) { ItemStack stackInv =
			 * invAmmo.getStackInSlot(i); if (stack == stackInv) { invAmmo.setStackInSlot(i,
			 * ItemStack.EMPTY); return; } } } ((EntityPlayer)
			 * living).inventory.deleteStack(stack);
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

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		if (this.getTypeInt(stack) == 10 && TF2weapons.refinedFuel != null) {
			Provider cap = new Provider(stack, 3, TF2weapons.refinedFuel);
			return cap;
		}
		return null;
	}

	public static class Provider implements ICapabilityProvider, IFluidHandlerItem {

		FluidTankProperties prop;
		ItemStack stack;
		int mult;
		Fluid fluid;

		public Provider(ItemStack stack, int mult, Fluid fluid) {
			this.stack = stack;
			this.mult = mult;
			this.fluid = fluid;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			if (stack.isEmpty())
				return new IFluidTankProperties[0];
			return new IFluidTankProperties[] { new FluidTankProperties(
					new FluidStack(fluid, ((ItemFireAmmo) stack.getItem()).getAmount(stack) * mult),
					((ItemFireAmmo) stack.getItem()).uses * mult) };
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if (stack.isEmpty())
				return 0;
			if (resource.getFluid() != fluid)
				return resource.amount;
			int resourceUsed = Math.max(
					((ItemFireAmmo) stack.getItem()).uses - ((ItemFireAmmo) stack.getItem()).getAmount(stack),
					(resource.amount / mult)) * mult;
			if (doFill) {
				stack.setDamageValue(stack.getDamageValue() - resourceUsed / mult);
			}
			return resourceUsed;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			if (stack.isEmpty())
				return null;
			if (resource.getFluid() != fluid)
				return null;
			int resourceUsed = Math.min(((ItemFireAmmo) stack.getItem()).getAmount(stack), (resource.amount / mult))
					* mult;
			if (doDrain) {
				stack.setDamageValue(stack.getDamageValue() + resourceUsed / mult);
				if (stack.getDamageValue() > stack.getMaxDamage())
					stack.shrink(1);

			}
			return new FluidStack(fluid, resourceUsed);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			return drain(new FluidStack(fluid, maxDrain), doDrain);
		}

		@Override
		public ItemStack getContainer() {
			return this.stack;
		}

		public boolean hasCapabilityLegacy(Capability<?> capability, Direction facing) {
			return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
		}

		@SuppressWarnings("unchecked")
		@Override
	public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(Capability<T> capability, @javax.annotation.Nullable Direction facing) {
		T value = this.getCapabilityLegacy(capability, facing);
		return value == null ? net.minecraftforge.common.util.LazyOptional.empty() : net.minecraftforge.common.util.LazyOptional.of(() -> value);
	}

	public <T> T getCapabilityLegacy(Capability<T> capability, Direction facing) {
			return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY ? (T) this : null;
		}

	}
}
