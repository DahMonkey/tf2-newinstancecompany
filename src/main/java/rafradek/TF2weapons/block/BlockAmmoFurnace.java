package rafradek.TF2weapons.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.tileentity.TileEntityAbstractAmmoFurnace;
import rafradek.TF2weapons.tileentity.TileEntityAmmoFurnace;

public class BlockAmmoFurnace extends BaseEntityBlock {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty BURNING = BooleanProperty.create("burning");
	private final boolean electric;

	public BlockAmmoFurnace(boolean electric, Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BURNING, false));
		this.electric = electric;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityAmmoFurnace(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null
				: createTickerHelper(type, TF2weapons.TILE_AMMO_FURNACE, TileEntityAbstractAmmoFurnace::serverTick);
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!world.isClientSide)
			TF2weapons.openGui(player, 3, world, pos.getX(), pos.getY(), pos.getZ());
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	public void animateTick(BlockState stateIn, Level world, BlockPos pos, RandomSource random) {
		if (stateIn.getValue(BURNING)) {
			Direction enumfacing = stateIn.getValue(FACING);
			double d0 = pos.getX() + 0.5D;
			double d1 = pos.getY() + random.nextDouble() * 6.0D / 16.0D;
			double d2 = pos.getZ() + 0.5D;
			double d4 = random.nextDouble() * 0.6D - 0.3D;
			if (random.nextDouble() < 0.1D)
				world.playLocalSound(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, SoundEvents.FURNACE_FIRE_CRACKLE,
						SoundSource.BLOCKS, 1.0F, 1.0F, false);
			double x = d0 + enumfacing.getStepX() * 0.52D + (enumfacing.getAxis() == Direction.Axis.Z ? d4 : 0);
			double z = d2 + enumfacing.getStepZ() * 0.52D + (enumfacing.getAxis() == Direction.Axis.X ? d4 : 0);
			world.addParticle(ParticleTypes.SMOKE, x, d1, z, 0.0D, 0.0D, 0.0D);
			world.addParticle(ParticleTypes.FLAME, x, d1, z, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		if (stack.hasCustomHoverName()) {
			BlockEntity tileentity = world.getBlockEntity(pos);
			if (tileentity instanceof TileEntityAbstractAmmoFurnace)
				((TileEntityAbstractAmmoFurnace) tileentity).setCustomInventoryName(stack.getHoverName());
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			BlockEntity tileentity = world.getBlockEntity(pos);
			if (tileentity instanceof TileEntityAbstractAmmoFurnace) {
				Containers.dropContents(world, pos, (TileEntityAbstractAmmoFurnace) tileentity);
				world.updateNeighbourForOutputSignal(pos, this);
			}
		}
		super.onRemove(state, world, pos, newState, isMoving);
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
		builder.add(FACING, BURNING);
	}

	/** Switches the burning flag; 1.19 keeps the block entity when only the state changes. */
	public static void setState(boolean active, Level world, BlockPos pos) {
		BlockState iblockstate = world.getBlockState(pos);
		if (iblockstate.hasProperty(BURNING) && iblockstate.getValue(BURNING) != active)
			world.setBlock(pos, iblockstate.setValue(BURNING, active), 3);
	}
}
