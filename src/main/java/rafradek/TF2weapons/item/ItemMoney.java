package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.inventory.InventoryWearables;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMoney extends Item {

	public ItemMoney() {
		this.setHasSubtypes(true);
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".tf2money");
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		return super.getDescriptionId(stack) + "." + stack.getDamageValue();
	}

	public int getValue(ItemStack stack) {
		switch (stack.getDamageValue()) {
		case 0:
			return 1 * stack.getCount();
		case 1:
			return 9 * stack.getCount();
		case 2:
			return 81 * stack.getCount();
		}
		return stack.getCount();
	}

	@Override
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < 3; i++)
			par3List.add(new ItemStack(this, 1, i));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		tooltip.add(I18n.get("item."+TF2weapons.MOD_ID+".tf2money.desc", getValue(stack)));
	}

	public static void collect(ItemStack stack, Player player) {
		int type = stack.getDamageValue();
		InventoryWearables inv = player.getCapability(TF2weapons.INVENTORY_CAP).orElse(null);
		int total;
		if (type == 0) {
			total = stack.getCount() + inv.getStackInSlot(type + 5).getCount();
			if (total > stack.getMaxStackSize()) {
				int subtract = Mth.ceil((total - stack.getMaxStackSize()) / 9f);
				total -= subtract * 9;
				type = 1;
				stack.setCount(subtract);
				stack.setDamageValue(1);
			} else {
				inv.setInventorySlotContents(5, stack.copy());
				stack.setCount(0);
			}
			inv.getStackInSlot(5).setCount(total);
		}
		if (type == 1) {
			total = stack.getCount() + inv.getStackInSlot(type + 5).getCount();
			if (total > stack.getMaxStackSize()) {
				int subtract = Mth.ceil((total - stack.getMaxStackSize()) / 9f);
				total -= subtract * 9;
				type = 2;
				stack.setCount(subtract);
				stack.setDamageValue(2);
			} else {
				inv.setInventorySlotContents(6, stack.copy());
				stack.setCount(0);
			}
			inv.getStackInSlot(6).setCount(total);
		}
		if (type == 2) {
			total = stack.getCount() + inv.getStackInSlot(type + 5).getCount();
			if (total > stack.getMaxStackSize()) {
				stack.setCount(total - stack.getMaxStackSize());
				inv.getStackInSlot(type + 5).setCount(stack.getMaxStackSize());
			} else {
				inv.setInventorySlotContents(type + 5, stack.copy());
				inv.getStackInSlot(type + 5).setCount(total);
				stack.setCount(0);
			}

		}
	}
}
