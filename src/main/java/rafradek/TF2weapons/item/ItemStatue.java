package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.EntityStatue;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.TF2weapons.util.TF2Util;

@SuppressWarnings("deprecation")
public class ItemStatue extends Item {

	public ItemStatue() {
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".statue");
	}

	@Override
	public InteractionResult useOn(Player player, Level world, BlockPos pos, InteractionHand hand,
			Direction facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getItemInHand(hand);
		if (!world.isClientSide && stack.hasTag() && stack.getTag().contains("Statue")) {
			EntityStatue statue = new EntityStatue(world);
			statue.readAdditionalSaveData(stack.getTag().getCompound("Statue"));
			BlockPos off = pos.move(facing);
			statue.setPos(off.getX() + 0.5, off.getY(), off.getZ() + 0.5);
			statue.rotationYaw = player.yHeadRot;
			statue.yBodyRot = player.yHeadRot;

			statue.ticksLeft = -1;
			world.addFreshEntity(statue);
			if (!player.getAbilities().instabuild)
				stack.shrink(1);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}

	public static ItemStack getStatue(EntityStatue statue) {
		ItemStack stack = new ItemStack(TF2weapons.itemStatue);
		stack.setTag(new CompoundTag());
		CompoundTag tag = new CompoundTag();
		statue.addAdditionalSaveData(tag);
		stack.getTag().put("Statue", tag);
		return stack;
	}

	@Override
	public String getName(ItemStack stack) {
		if (!stack.hasTag())
			return super.getName(stack);
		if (!stack.getTag().getCompound("Statue").getBoolean("Player"))
			return TF2Util.translate("entity."
					+ EntityList.getTranslationName(new ResourceLocation(
							stack.getTag().getCompound("Statue").getCompound("Entity").getString("id")))
					+ ".name") + " " + TF2Util.translate(this.getDescriptionId() + ".name");
		else
			return TF2Util.translate(this.getDescriptionId() + ".player.name");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		if (stack.hasTag()) {
			if (stack.getTag().getCompound("Statue").getBoolean("Player")) {
				tooltip.add(
						stack.getTag().getCompound("Statue").getCompound("Profile").getString("Name"));
			}
		}
	}

}
