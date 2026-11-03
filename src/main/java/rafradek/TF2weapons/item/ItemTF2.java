package rafradek.TF2weapons.item;

import net.minecraft.network.chat.Component;
import rafradek.TF2weapons.util.TF2Util;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.tileentity.TileEntityRobotDeploy;
import rafradek.TF2weapons.util.TF2Class;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemTF2 extends LegacyItem {

	public static final String[] NAMES = new String[] { "ingotCopper", "ingotLead", "ingotAustralium", "scrapMetal",
			"reclaimedMetal", "refinedMetal", "nuggetAustralium", "key", "crate", "randomWeapon", "randomHat",
			"logicBoard", "robotPartsMedium", "robotPartsGiant" };

	public ItemTF2() {
		this.setHasSubtypes(true);
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".tf2item");
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		StringBuilder name =  new StringBuilder("item.");
		name.append(TF2weapons.MOD_ID);
		name.append(".");
		name.append(NAMES[stack.getDamageValue() % NAMES.length]);
		if (stack.hasTag() && stack.getTag().getBoolean("Australium")) name.append("Australium");
		return name.toString();
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return (stack.getDamageValue() == 9 || stack.getDamageValue() == 10) ? 1 : 64;
	}

	@Override
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < 8; i++)
			par3List.add(TF2Util.metaStack(this, 1, i));
		par3List.add(TF2Util.metaStack(this, 1, 11));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced) {
		if (stack.hasTag() && stack.getTag().contains("Token")) {
			tooltip.add(Component.literal(TF2Class.getClass(stack.getTag().getByte("Token")).getLocalizedName().getString()));
		}
		if (stack.getDamageValue() == 12 || stack.getDamageValue() == 13)
			tooltip.add(Component.literal("Right click to gain robot parts"));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player living, InteractionHand hand) {
		ItemStack stack = living.getItemInHand(hand);
		if (stack.getDamageValue() == 12 || stack.getDamageValue() == 13) {
			if (!world.isClientSide) {
				int notDropPiece = living.getRandom().nextInt(ItemRobotPart.LEVEL.length);
				for (int i = 0; i < ItemRobotPart.LEVEL.length; i++) {
					if (i != notDropPiece)
						ItemHandlerHelper.giveItemToPlayer(living,
								TF2Util.metaStack(TF2weapons.itemRobotPart,
										stack.getDamageValue() == 13
												? TileEntityRobotDeploy.GIANT_REQUIRE[ItemRobotPart.LEVEL[i]]
												: TileEntityRobotDeploy.NORMAL_REQUIRE[ItemRobotPart.LEVEL[i]],
										i),
								0);
				}
				stack.shrink(1);
			}
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		} else if (stack.getDamageValue() == 9 || stack.getDamageValue() == 10) {
			if (!world.isClientSide) {
				ItemStack weapon = ItemFromData.getRandomWeapon(stack, living.getRandom());
				living.setItemInHand(hand, weapon);
			}
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		} else
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);
	}

}
