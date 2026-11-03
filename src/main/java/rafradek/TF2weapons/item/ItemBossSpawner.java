package rafradek.TF2weapons.item;

import rafradek.TF2weapons.util.TF2Util;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.boss.EntityHHH;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import rafradek.TF2weapons.entity.boss.EntityMonoculus;
import rafradek.TF2weapons.entity.boss.EntityTF2Boss;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBossSpawner extends LegacyItem {

	public static final String[] NAMES = { "hhh", "monoculus", "merasmus" };

	public static BlockPattern patternHHH = FactoryBlockPattern.start().aisle("NAN", "BCB", "NBN")
			.where('A', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.LIT_PUMPKIN)))
			.where('C', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.OBSIDIAN)))
			.where('B', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.HAY_BLOCK)))
			.where('N', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();

	public ItemBossSpawner() {
		super();
		this.setHasSubtypes(true);
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		return "item." + TF2weapons.MOD_ID + "." + NAMES[stack.getDamageValue() % NAMES.length];
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
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced) {
		tooltip.add(Component.literal(I18n.get("item." + TF2weapons.MOD_ID + NAMES[stack.getDamageValue() % NAMES.length] + ".desc")));
	}

	@Override
	public InteractionResult useOn(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing,
			float hitX, float hitY, float hitZ) {
		if (world.isClientSide || TF2ConfigVars.disableBossSpawnItems)
			return InteractionResult.SUCCESS;
		EntityTF2Boss boss = null;
		long time = world.getDayTime();
		ItemStack stack = player.getItemInHand(hand);
		TF2PlayerCapability cap = TF2PlayerCapability.get(player);
		if (time % 24000 < 13500 || time % 24000 > 21500) {
			player.sendSystemMessage(Component.translatable("gui.boss.night"));
			return InteractionResult.SUCCESS;
		}

		if (stack.getDamageValue() == 0 && cap.hhhSummonedDay < time / 24000) {
			BlockPattern.PatternHelper pattern = patternHHH.match(world, pos);
			if (world.getBlockState(pos).getBlock() == Blocks.PORTAL) {
				boss = new EntityHHH(world);
				cap.hhhSummonedDay = (int) (time / 24000);
			} else {
				player.sendSystemMessage(Component.translatable("gui.boss.portal"));
				return InteractionResult.SUCCESS;
			}
		} else if (stack.getDamageValue() == 1 && cap.monoculusSummonedDay < time / 24000) {
			if (world.getBlockState(pos).getBlock() == Blocks.PORTAL) {
				boss = new EntityMonoculus(world);
				cap.monoculusSummonedDay = (int) (time / 24000);
			} else {
				player.sendSystemMessage(Component.translatable("gui.boss.portal"));
				return InteractionResult.SUCCESS;
			}
		} else if (stack.getDamageValue() == 2 && cap.merasmusSummonedDay < time / 24000) {
			if (world.getBlockState(pos).getBlock() == Blocks.PORTAL) {
				boss = new EntityMerasmus(world);
				cap.merasmusSummonedDay = (int) (time / 24000);
			} else {
				player.sendSystemMessage(Component.translatable("gui.boss.portal"));
				return InteractionResult.SUCCESS;
			}
		} else {
			player.sendSystemMessage(Component.translatable("gui.boss.nextnight"));
		}
		if (boss != null) {
			boss.moveTo(pos.getX() + 0.5D, pos.getY() + 0.05D, pos.getZ() + 0.5D, 0.0F, 0.0F);
			boss.onInitialSpawn(world.getCurrentDifficultyAt(pos), null);
			boss.summoned = true;
			world.addFreshEntity(boss);
			stack.shrink(1);
		}
		return InteractionResult.SUCCESS;
	}
}
