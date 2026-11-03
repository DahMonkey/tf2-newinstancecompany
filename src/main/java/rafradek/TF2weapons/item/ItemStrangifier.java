package rafradek.TF2weapons.item;

import net.minecraft.network.chat.Component;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.util.PropertyType;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemStrangifier extends ItemApplicableEffect {

	public ItemStrangifier() {
		this.setMaxStackSize(1);
	}

	@Override
	public Component getName(ItemStack stack) {
		if (stack.hasTag() && MapList.nameToData.containsKey(stack.getTag().getString("Weapon"))) {
			return Component.literal(I18n.get("weapon." + stack.getTag().getString("Weapon")) + " "
					+ super.getName(stack).getString());
		}
		return Component.literal(super.getName(stack).getString());
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced) {
		if (!stack.hasTag())
			tooltip.add(Component.literal("Applicable to any weapon"));
		tooltip.add(Component.literal("Combine with the matching weapon in a crafting table"));
	}

	@Override
	public void inventoryTick(ItemStack stack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		if (stack.hasTag() && stack.getTag().contains("class")) {
			ItemStack data = ItemFromData.getRandomWeapon(par2World.random,
					Predicates.and(ItemFromData.VISIBLE_WEAPON, weapon -> (weapon.getInt(PropertyType.COST) >= 9
							&& weapon.get(PropertyType.SLOT).containsKey(stack.getTag().getString("class")))));
			if (!data.isEmpty())
				stack.getTag().putString("Weapon", ItemFromData.getData(data).getName());
			stack.getTag().remove("class");
		}
	}

	@Override
	public boolean isApplicable(ItemStack stack, ItemStack weapon) {
		return stack.hasTag() && stack.getTag().contains("Weapon") ? super.isApplicable(stack, weapon)
				: weapon.getItem() instanceof ItemUsable || weapon.getItem() instanceof ItemCloak
						|| weapon.getItem() instanceof ItemPDA || weapon.getItem() instanceof ItemBackpack;
	}

	@Override
	public void apply(ItemStack stack, ItemStack weapon) {
		weapon.getTag().putBoolean("Strange", true);
	}
}
