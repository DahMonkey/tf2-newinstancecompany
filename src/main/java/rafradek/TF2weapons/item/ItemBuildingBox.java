package rafradek.TF2weapons.item;

import net.minecraft.network.chat.Component;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.util.TF2Class;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.TF2weapons.util.TF2Util;

@SuppressWarnings("deprecation")
public class ItemBuildingBox extends ItemMonsterPlacerPlus implements IItemNoSwitch {
	public ItemBuildingBox() {
		this.setCreativeTab(TF2weapons.tabspawnertf2);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".buildingbox");
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return stack.hasTag() && stack.getTag().contains("MaxStack")
				? stack.getTag().getInt("MaxStack")
				: 1;
	}

	@Override
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.allowedIn(par2CreativeTabs))
			return;
		for (int i = 18; i < 24; i++)
			par3List.add(TF2Util.metaStack(this, 1, i));
	}

	@Override
	public InteractionResult useOn(Player player, Level world, BlockPos pos, InteractionHand hand,
			Direction facing, float hitX, float hitY, float hitZ) {
		if (ItemToken.allowUse(player, TF2Class.ENGINEER)) {
			return super.useOn(player, world, pos, hand, facing, hitX, hitY, hitZ);
		} else {
			return InteractionResult.FAIL;
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		if (ItemToken.allowUse(player, TF2Class.ENGINEER)) {
			return super.use(world, player, hand);
		} else {
			return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));
		}
	}

	@Override
	public Component getName(ItemStack stack) {
		// (I18n.translateToLocal(this.getUnlocalizedName()+".name")).trim();
		int i = stack.getDamageValue() / 2;
		String s1 = "sentry";
		if (stack.hasTag() && stack.getTag().getBoolean("Mini"))
			s1 += "mini";
		switch (i) {
		case 10:
			s1 = "dispenser";
			break;
		case 11:
			s1 = "teleporter";
			break;
		}
		return Component.literal(TF2Util.translate(this.getDescriptionId() + "." + s1 + ".name"));
	}

	@OnlyIn(Dist.CLIENT)
	public int colorMultiplier(ItemStack p_82790_1_, int p_82790_2_) {
		return 16777215;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced) {
		if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
			tooltip.add(Component.literal("Hold " + KeyMapping.getDisplayString("key.sneak").get() + " to spawn natural building"));
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entityIn, int itemSlot, boolean isSelected) {
		this.forceItemSlot(stack, world, entityIn, itemSlot, isSelected);
	}

	@Override
	public boolean stopSlotSwitch(ItemStack stack, LivingEntity living) {
		return stack.hasTag() && stack.getTag().getCompound("SavedEntity") != null;
	}
}
