package rafradek.TF2weapons.item;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import rafradek.TF2weapons.TF2weapons;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ItemAmmoBelt extends LegacyItem {

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player living, InteractionHand hand) {
		if (!world.isClientSide)
			TF2weapons.openGui((net.minecraft.world.entity.player.Player) living, 0, world, 0, 0, 0);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, living.getItemInHand(hand));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return TF2weapons.MOD_ID + ":textures/models/tf2/ammo_belt.png";
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return new Provider();
	}

	public static class Provider extends ItemStackHandler implements ICapabilityProvider {

		public Provider() {
			super(9);
		}

		public boolean hasCapabilityLegacy(Capability<?> capability, Direction facing) {
			return capability == ForgeCapabilities.ITEM_HANDLER;
		}

		@Override
	public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(Capability<T> capability, @javax.annotation.Nullable Direction facing) {
		T value = this.getCapabilityLegacy(capability, facing);
		return value == null ? net.minecraftforge.common.util.LazyOptional.empty() : net.minecraftforge.common.util.LazyOptional.of(() -> value);
	}

	public <T> T getCapabilityLegacy(Capability<T> capability, Direction facing) {
			if (capability == ForgeCapabilities.ITEM_HANDLER)
				return (T) (Object) this;
			return null;
		}

	}
}
