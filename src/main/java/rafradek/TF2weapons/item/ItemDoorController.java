package rafradek.TF2weapons.item;

import rafradek.TF2weapons.tileentity.TileEntityOverheadDoor;
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
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemDoorController extends Item {

	public static final String[] NAMES = { "players", "mobs", "RED", "BLU" };

	public ItemDoorController() {
		this.setHasSubtypes(true);
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		return super.getDescriptionId(stack) + "." + NAMES[stack.getDamageValue() % NAMES.length];
	}

	@Override
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < NAMES.length; i++)
			par3List.add(TF2Util.metaStack(this, 1, i));
	}

	@Override
	public InteractionResult useOn(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing,
			float hitX, float hitY, float hitZ) {
		if (!world.isClientSide) {
			BlockEntity ent = world.getBlockEntity(pos);
			if (ent instanceof TileEntityOverheadDoor) {
				((TileEntityOverheadDoor) ent)
						.setController(NAMES[player.getItemInHand(hand).getDamageValue() % NAMES.length]);
				if (!player.getAbilities().instabuild)
					player.getItemInHand(hand).shrink(1);
			} else
				return InteractionResult.PASS;
		}
		return InteractionResult.SUCCESS;
	}
}
