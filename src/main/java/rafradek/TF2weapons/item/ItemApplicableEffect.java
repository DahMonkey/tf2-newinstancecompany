package rafradek.TF2weapons.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemApplicableEffect extends Item {

	public boolean isApplicable(ItemStack stack, ItemStack weapon) {
		return stack.hasTag()
				&& ItemFromData.getData(weapon).getName().equals(stack.getTag().getString("Weapon"));
	}

	public void apply(ItemStack stack, ItemStack weapon) {}

}
