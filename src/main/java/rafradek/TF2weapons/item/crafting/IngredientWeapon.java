package rafradek.TF2weapons.item.crafting;

import rafradek.TF2weapons.item.ItemFromData;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;

public class IngredientWeapon extends TF2Ingredient {

	public IngredientWeapon(String string) {
		super(stack -> stack.getItem() == ItemFromData.getNewStack(string).getItem()
				&& ItemFromData.getData(stack).equals(ItemFromData.getData(ItemFromData.getNewStack(string))),
				new ItemStack[] { ItemFromData.getNewStack(string) });
	}
}
