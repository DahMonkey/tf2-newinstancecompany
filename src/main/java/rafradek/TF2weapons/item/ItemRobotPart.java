package rafradek.TF2weapons.item;

import rafradek.TF2weapons.util.TF2Util;

import rafradek.TF2weapons.TF2weapons;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemRobotPart extends Item {

	public static final int[] LEVEL = { 0, 0, 0, 1, 1, 2, 2 };
	public static final int[] LEVEL1 = { 0, 1, 2 };
	public static final int[] LEVEL2 = { 3, 4 };
	public static final int[] LEVEL3 = { 5, 6 };

	public ItemRobotPart() {
		this.setHasSubtypes(true);
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".robotpart");
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		return super.getDescriptionId(stack) + "." + stack.getDamageValue();
	}

	public static int getLevel(ItemStack stack) {
		return LEVEL[stack.getDamageValue() % LEVEL.length];
	}

	public static int getVariant(ItemStack stack) {
		return stack.getDamageValue() % 3;
	}

	@Override
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < LEVEL.length; i++)
			par3List.add(TF2Util.metaStack(this, 1, i));
	}

}
