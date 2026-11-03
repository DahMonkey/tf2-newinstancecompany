package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.EntityTarget;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ItemTarget extends LegacyItem {

	public ItemTarget() {
		this.setCreativeTab(TF2weapons.tabutilitytf2);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	@Override
	public InteractionResult useOn(Player player, Level world, BlockPos pos, InteractionHand hand,
			Direction facing, float hitX, float hitY, float hitZ) {
		if (facing == Direction.DOWN) {
			return InteractionResult.FAIL;
		} else {
			boolean flag = world.getBlockState(pos).getBlock().isReplaceable(world, pos);
			BlockPos blockpos = flag ? pos : pos.relative(facing);
			ItemStack itemstack = player.getItemInHand(hand);

			if (!player.canPlayerEdit(blockpos, facing, itemstack)) {
				return InteractionResult.FAIL;
			} else {
				BlockPos blockpos1 = blockpos.above();
				boolean flag1 = !world.isEmptyBlock(blockpos)
						&& !world.getBlockState(blockpos).getBlock().isReplaceable(world, blockpos);
				flag1 = flag1 | (!world.isEmptyBlock(blockpos1)
						&& !world.getBlockState(blockpos1).getBlock().isReplaceable(world, blockpos1));

				if (flag1) {
					return InteractionResult.FAIL;
				} else {
					double d0 = blockpos.getX();
					double d1 = blockpos.getY();
					double d2 = blockpos.getZ();
					List<Entity> list = world.getEntities((Entity) null,
							new AABB(d0, d1, d2, d0 + 1.0D, d1 + 2.0D, d2 + 1.0D));

					if (!list.isEmpty()) {
						return InteractionResult.FAIL;
					} else {
						if (!world.isClientSide) {
							world.removeBlock(blockpos, false);
							world.removeBlock(blockpos1, false);
							EntityTarget entityarmorstand = new EntityTarget(world, d0 + 0.5D, d1, d2 + 0.5D,
									player.getAbilities().instabuild);
							float f = Mth.floor(
									(Mth.wrapDegrees(player.getYRot() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
							entityarmorstand.moveTo(d0 + 0.5D, d1, d2 + 0.5D, f, 0.0F);
							this.applyRandomRotations(entityarmorstand, world.random);
							SpawnEggItem.applyItemEntityDataToEntity(world, player, itemstack, entityarmorstand);
							world.addFreshEntity(entityarmorstand);
							world.playSound((Player) null, entityarmorstand.getX(), entityarmorstand.getY(),
									entityarmorstand.getZ(), SoundEvents.ENTITY_ARMORSTAND_PLACE, SoundSource.BLOCKS,
									0.75F, 0.8F);
						}

						itemstack.shrink(1);
						return InteractionResult.SUCCESS;
					}
				}
			}
		}
	}

	private void applyRandomRotations(ArmorStand armorStand, RandomSource random) {
		Rotations rotations = armorStand.getHeadRotation();
		float f = random.nextFloat() * 5.0F;
		float f1 = random.nextFloat() * 20.0F - 10.0F;
		Rotations rotations1 = new Rotations(rotations.getX() + f, rotations.getY() + f1, rotations.getZ());
		armorStand.setHeadRotation(rotations1);
		rotations = armorStand.getBodyRotation();
		f = random.nextFloat() * 10.0F - 5.0F;
		rotations1 = new Rotations(rotations.getX(), rotations.getY() + f, rotations.getZ());
		armorStand.setBodyRotation(rotations1);
	}

}
