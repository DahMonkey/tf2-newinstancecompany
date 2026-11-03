package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.InvasionEvent;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemEventMaker extends Item {

	public ItemEventMaker() {
		this.setHasSubtypes(true);
		this.setUnlocalizedName("eventmaker");
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		return super.getDescriptionId(stack) + "." + stack.getDamageValue();
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player living, InteractionHand hand) {
		ItemStack stack = living.getItemInHand(hand);
		if (!world.isClientSide && !TF2ConfigVars.disableInvasionItems) {
			if (TF2Util.getTeam(living) == null && TF2ConfigVars.canJoin) {
				living.sendSystemMessage(Component.translatable("item.eventmaker.noteam"));
				return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
			}
			if (!world.getCapability(TF2weapons.WORLD_CAP).orElse(null).startInvasion(living,
					stack.getDamageValue() % InvasionEvent.DIFFICULTY.length, living.getAbilities().instabuild)) {
				living.sendSystemMessage(Component.translatable("item.eventmaker.fail"));
				return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
			} else {
				stack.shrink(1);
			}
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);

	}

	@Override
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < InvasionEvent.DIFFICULTY.length; i++)
			par3List.add(new ItemStack(this, 1, i));
	}
}
