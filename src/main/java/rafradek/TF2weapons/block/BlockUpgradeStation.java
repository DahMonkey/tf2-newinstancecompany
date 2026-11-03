package rafradek.TF2weapons.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.tileentity.TileEntityUpgrades;

/**
 * Upgrade station: a 2x2 multi-block. The holder block keeps the upgrades; the helper blocks around it
 * forward clicks to it. PLACED marks stations placed by players (breakable, drop themselves).
 */
public class BlockUpgradeStation extends BaseEntityBlock {

	public static final BooleanProperty HOLDER = BooleanProperty.create("holder");
	public static final BooleanProperty PLACED = BooleanProperty.create("placed");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public BlockUpgradeStation(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HOLDER, true)
				.setValue(PLACED, false));
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	@Nullable
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HOLDER) ? new TileEntityUpgrades(pos, state) : null;
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!world.isClientSide) {
			BlockPos holder = state.getValue(HOLDER) ? pos : findHolder(world, pos);
			if (holder != null)
				TF2weapons.openGui(player, 2, world, holder.getX(), holder.getY(), holder.getZ());
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Nullable
	private static BlockPos findHolder(BlockGetter world, BlockPos pos) {
		for (int x = -1; x < 2; x++)
			for (int y = -1; y < 2; y++)
				for (int z = -1; z < 2; z++) {
					BlockState other = world.getBlockState(pos.offset(x, y, z));
					if (other.getBlock() instanceof BlockUpgradeStation && other.getValue(HOLDER))
						return pos.offset(x, y, z);
				}
		return null;
	}

	@Override
	@SuppressWarnings("deprecation")
	public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
		return state.getValue(PLACED) ? super.getDestroyProgress(state, player, world, pos) : 0;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(PLACED,
				true);
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		if (placer == null)
			return;
		Direction dirop = placer.getDirection().getClockWise();
		BlockState statehelp = state.setValue(HOLDER, false);
		for (BlockPos helper : new BlockPos[] { pos.relative(dirop), pos.relative(dirop, -1),
				pos.relative(dirop, -1).above(), pos.above(), pos.relative(dirop).above() })
			if (world.isEmptyBlock(helper))
				world.setBlock(helper, statehelp, 3);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && !world.isClientSide) {
			if (state.getValue(HOLDER)) {
				BlockEntity tileentity = world.getBlockEntity(pos);
				if (state.getValue(PLACED) && tileentity instanceof TileEntityUpgrades) {
					ItemStack itemstack = new ItemStack(this);
					tileentity.saveToItem(itemstack);
					popResource(world, pos, itemstack);
				}
				super.onRemove(state, world, pos, newState, isMoving);
				breakBlockAround(world, pos);
				return;
			} else {
				BlockPos holder = findHolder(world, pos);
				if (holder != null)
					world.destroyBlock(holder, false);
			}
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}

	public void breakBlockAround(Level world, BlockPos pos) {
		for (int x = -1; x <= 1; x++)
			for (int y = -1; y <= 1; y++)
				for (int z = -1; z <= 1; z++) {
					BlockState other = world.getBlockState(pos.offset(x, y, z));
					if (other.getBlock() instanceof BlockUpgradeStation && !other.getValue(HOLDER))
						world.setBlock(pos.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
				}
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, HOLDER, PLACED);
	}
}
