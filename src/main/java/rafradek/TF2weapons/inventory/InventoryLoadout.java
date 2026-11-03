package rafradek.TF2weapons.inventory;

import com.google.common.collect.Multimap;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class InventoryLoadout extends ItemStackHandler {

	LivingEntity living;
	private final NonNullList<ItemStack> inventoryContentsOld;

	public InventoryLoadout(int size, LivingEntity ent) {
		super(size);
		this.inventoryContentsOld = NonNullList.withSize(size, ItemStack.EMPTY);
		living = ent;
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		ItemStack old = this.getStackInSlot(slot);
		super.setStackInSlot(slot, stack);
		if (!old.isEmpty())
			living.getAttributes()
					.removeAttributeModifiers(old.getAttributeModifiers(old.getItem().getEquipmentSlot(old)));
		if (!stack.isEmpty()) {
			Multimap<String, AttributeModifier> modifiers = stack
					.getAttributeModifiers(stack.getItem().getEquipmentSlot(stack));
			modifiers.removeAll(Attributes.ARMOR.getName());
			modifiers.removeAll(Attributes.ARMOR_TOUGHNESS.getName());
			living.getAttributes().applyAttributeModifiers(modifiers);
		}
	}

	public void updateSlots() {
		for (int i = 0; i < this.getSlots(); i++) {
			ItemStack stack = this.getStackInSlot(i);
			ItemStack old = inventoryContentsOld.get(i);

			if ((stack.getItem() instanceof ItemFromData
					&& ((ItemFromData) stack.getItem()).getVisibilityFlags(stack, living) != 0)
					|| (old.getItem() instanceof ItemFromData
							&& ((ItemFromData) old.getItem()).getVisibilityFlags(old, living) != 0)
							&& !ItemStack.matches(stack, old)) {
				inventoryContentsOld.set(i, stack.copy());
				TF2Util.sendTracking(new TF2Message.WearableChangeMessage(living, 20 + i, stack), living);
			}
		}
	}
}
