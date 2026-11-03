package rafradek.TF2weapons.item.crafting;

import rafradek.TF2weapons.item.ItemFromData;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class IngredientWeapon extends Ingredient {

	private final ItemStack stack;

	protected IngredientWeapon(String string) {
		super(ItemFromData.getNewStack(string));
		this.stack = ItemFromData.getNewStack(string);
	}

	@Override
	public boolean apply(@Nullable ItemStack input) {
		if (input == null)
			return false;
		// Can't use areItemStacksEqualUsingNBTShareTag because it compares stack size
		// as well
		return this.stack.getItem() == input.getItem() && this.stack.getDamageValue() == input.getDamageValue()
				&& ItemFromData.getData(input).equals(ItemFromData.getData(stack));
	}

	@Override
	public boolean isSimple() {
		return false;
	}
}
