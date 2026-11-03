package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.util.PropertyType;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemAmmo extends Item {

	public static final String[] AMMO_TYPES = new String[] { "none", "shotgun", "minigun", "pistol", "revolver", "smg",
			"sniper", "rocket", "grenade", "syringe", "fire", "sticky", "medigun", "flare", "ball", "custom" };
	public static final int[] AMMO_MAX_STACK = new int[] { 64, 64, 64, 64, 64, 64, 16, 32, 32, 64, 1, 32, 1, 64, 64,
			64 };
	public static ItemStack STACK_FILL;

	public ItemAmmo() {
		this.setHasSubtypes(true);
	}

	public String getType(ItemStack stack) {
		return AMMO_TYPES[this.getTypeInt(stack)];
	}

	public int getTypeInt(ItemStack stack) {
		return stack.getDamageValue();
	}

	public boolean isValidForWeapon(ItemStack ammo, ItemStack weapon) {
		return getTypeInt(ammo) == ItemFromData.getData(weapon).getInt(PropertyType.AMMO_TYPE);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public CreativeModeTab getCreativeTab() {
		return TF2weapons.tabsurvivaltf2;
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		return super.getDescriptionId(stack) + "." + getType(stack);
	}

	@Override
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 1; i < AMMO_TYPES.length - 1; i++)
			if (i != 10 && i != 12 && i != 2 && i != 3 && i != 5 && i != 9)
				par3List.add(TF2Util.metaStack(this, 1, i));
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return AMMO_MAX_STACK[Mth.clamp(stack.getDamageValue(), 0, AMMO_MAX_STACK.length - 1)];
	}

	public int consumeAmmo(LivingEntity living, ItemStack stack, int amount) {
		if (stack == STACK_FILL)
			return 0;
		// if(EntityDispenser.isNearDispenser(living.world, living)) return;
		if (amount > 0) {
			int left = Math.max(0, amount - stack.getCount());
			stack.shrink(amount);
			return left;

			/*
			 * if (stack.isEmpty() && living instanceof EntityPlayer) {
			 *
			 * if (living.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3) !=
			 * null){ IItemHandlerModifiable invAmmo = (IItemHandlerModifiable)
			 * living.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3)
			 * .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			 *
			 * for (int i = 0; i < invAmmo.getSlots(); i++) { ItemStack stackInv =
			 * invAmmo.getStackInSlot(i); if (stack == stackInv) { invAmmo.setStackInSlot(i,
			 * null); return; } } } ((EntityPlayer) living).inventory.deleteStack(stack); }
			 */
		}
		return 0;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player living, InteractionHand hand) {
		if (!world.isClientSide)
			FMLNetworkHandler.openGui(living, TF2weapons.instance, 0, world, 0, 0, 0);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, living.getItemInHand(hand));
	}

	public int getAmount(ItemStack stack) {
		return stack.getCount();
	}
}
