package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;

public class ItemAmmoPackage extends LegacyItem {

	public static final int[] AMMO_PACKAGE_MIN = new int[] { 1, 3, 10, 10, 3, 20, 1, 2, 2, 20, 60, 2, 200, 2, 1 };
	public static final int[] AMMO_PACKAGE_MAX = new int[] { 1, 3, 10, 10, 4, 25, 2, 3, 3, 40, 60, 3, 200, 3, 2 };

	public ItemAmmoPackage() {
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
	}

	@Override
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		par3List.add(TF2Util.metaStack(this, 1, 1));
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return stack.getDamageValue() % 16 == 0 ? 64 : 1;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack itemStackIn = player.getItemInHand(hand);
		if (!world.isClientSide) {
			int ammoType = itemStackIn.getDamageValue() % 16;
			for (ItemStack stack : player.getInventory().items) {
				if (!stack.isEmpty() && stack.getItem() instanceof ItemUsable
						&& ((ItemUsable) stack.getItem()).getAmmoType(stack) != 0
						&& ((ItemUsable) stack.getItem()).getAmmoType(stack) < ItemAmmo.AMMO_TYPES.length) {
					ammoType = ((ItemUsable) stack.getItem()).getAmmoType(stack);
					// itemStackIn.setItemDamage();
					break;
				}
			}
			ItemStack out = convertPackage(itemStackIn, player, ammoType);
			out = TF2Util.mergeStackByDamage(
					player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null), out);
			if (!player.getInventory().add(out))
				player.spawnAtLocation(out, 0);
			itemStackIn.shrink(1);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
	}

	public static ItemStack getAmmoForType(int ammoType, int amount) {
		Item item;
		switch (ammoType) {
		case 2:
			item = TF2weapons.itemAmmoMinigun;
			break;
		case 3:
			item = TF2weapons.itemAmmoPistol;
			break;
		case 5:
			item = TF2weapons.itemAmmoSMG;
			break;
		case 9:
			item = TF2weapons.itemAmmoSyringe;
			break;
		case 10:
			item = TF2weapons.itemAmmoFire;
			break;
		case 12:
			item = TF2weapons.itemAmmoMedigun;
			break;
		default:
			item = TF2weapons.itemAmmo;
		}
		ItemStack ammo = new ItemStack(item);
		if (item instanceof ItemFireAmmo) {
			if (amount > ammo.getMaxDamage() + 1) {
				ammo.setCount(Math.round((float) amount / (ammo.getMaxDamage() + 1)));
			} else
				ammo.setDamageValue(ammo.getMaxDamage() + 1 - amount);
		} else {
			ammo.setDamageValue(ammoType);
			ammo.setCount(amount);
		}
		return ammo;
	}

	public static ItemStack convertPackage(ItemStack stack, Player player, int ammoType) {

		if (ammoType == 0)
			ammoType = player.getRandom().nextInt(13) + 1;

		int amount = AMMO_PACKAGE_MIN[ammoType] + player.getRandom().nextInt(AMMO_PACKAGE_MAX[ammoType] + 1);
		amount *= stack.getDamageValue() / 16 + 1;
		ItemStack ammo = getAmmoForType(ammoType, amount);
		WeaponsCapability.get(player)
				.setMetal(WeaponsCapability.get(player).getMetal() + 40 * (stack.getDamageValue() / 16 + 1));
		return ammo;
		/*
		 * if(stack.getMetadata()==10){ stack=new ItemStack(TF2weapons.itemAmmoFire,1);
		 * stack.setItemDamage((int)(stack.getMaxDamage()*(0.3f+player.getRNG().
		 * nextFloat()*0.3f))); return stack; } else if(stack.getMetadata()==12){
		 * stack=new
		 * ItemStack(TF2weapons.itemAmmoMedigun,1,(int)(stack.getMaxDamage()*(0.4f+
		 * player.getRNG().nextFloat()*0.4f)));
		 * stack.setItemDamage((int)(stack.getMaxDamage()*(0.3f+player.getRNG().
		 * nextFloat()*0.3f))); return stack; } else{ return new
		 * ItemStack(TF2weapons.itemAmmo,AMMO_PACKAGE_MIN[stack.getMetadata()]+player.
		 * getRNG().nextInt(AMMO_PACKAGE_MAX[stack.getMetadata()]+1),stack.getMetadata()
		 * ); }
		 */
	}
}
