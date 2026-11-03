package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.EntityPickup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemPickup extends Item {

	public ItemPickup() {
		this.setHasSubtypes(true);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".tf2pickup");
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		return super.getDescriptionId(stack) + "." + stack.getDamageValue();
	}

	@Override
	public InteractionResult useOn(Player player, Level world, BlockPos pos, InteractionHand hand,
			Direction facing, float hitX, float hitY, float hitZ) {
		if (!world.isClientSide) {
			BlockPos forwardpos = pos.move(facing);
			EntityPickup pickup = new EntityPickup(world,
					EntityPickup.Type.values()[player.getItemInHand(hand).getDamageValue()], true);
			if (!player.isCreative())
				player.getItemInHand(hand).shrink(1);
			pickup.setPos(forwardpos.getX() + 0.5, forwardpos.getY(), forwardpos.getZ() + 0.5);
			world.addFreshEntity(pickup);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < EntityPickup.Type.values().length; i++) {
			if (EntityPickup.Type.values()[i].visible)
				par3List.add(TF2Util.metaStack(this, 1, i));
		}
	}
}
