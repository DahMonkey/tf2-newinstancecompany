package rafradek.TF2weapons.block;

import rafradek.TF2weapons.tileentity.TileEntityCapturePoint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockCapturePoint extends BaseEntityBlock {

	public static final BooleanProperty HOLDER = BooleanProperty.create("holder");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public BlockCapturePoint() {
		super(Material.IRON);
		setDefaultState(blockState.getBaseState().setValue(FACING, Direction.NORTH)
				.setValue(HOLDER, true));
	}

	@Override
	public RenderShape getRenderType(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity createNewTileEntity(Level world, int meta) {
		return  ((meta & 4) == 4) ? new TileEntityCapturePoint() : null;
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY,
			float hitZ, int meta, LivingEntity placer) {
		return this.defaultBlockState().setValue(FACING, placer.getDirection().getOpposite());
	}

	@Override
	public void onBlockAdded(Level world, BlockPos pos, BlockState state) {
		this.updateState(world, pos, state);
	}

	private void updateState(Level world, BlockPos pos, BlockState state) {
		/*
		 * TileEntity ent = world.getTileEntity(pos); if (ent instanceof
		 * TileEntityResupplyCabinet) if (((TileEntityResupplyCabinet)
		 * ent).redstoneActivate) ((TileEntityResupplyCabinet)
		 * ent).setEnabled(world.isBlockPowered(pos));
		 */
	}

	@Override
	public void onBlockDestroyedByPlayer(Level world, BlockPos pos, BlockState state) {

	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		this.updateState(world, fromPos, state);
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer,
			ItemStack stack) {
		world.setBlock(pos, state = state.setValue(FACING, placer.getDirection().getOpposite()), 2);

		if (placer instanceof Player) {

		}
		/*
		 * for (int x=-1; x < 2; x++) { for (int z=-1; z < 2; z++) { BlockPos off =
		 * pos.add(x, 0, z); if ((x != 0 || z != 0) &&
		 * world.getBlockState(off).getBlock().isReplaceable(world, off))
		 * world.setBlockState(off, state.withProperty(HOLDER, false), 2); } }
		 */

	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state) {

		if (state.getValue(HOLDER)) {
			for (int x = -1; x < 2; x++) {
				for (int z = -1; z < 2; z++) {
					BlockPos off = pos.add(x, 0, z);
					if ((x != 0 || z != 0) && world.getBlockState(off).getBlock() == this)
						world.removeBlock(off, false);
				}
			}
		} else {
			for (int x = -1; x < 2; x++) {
				for (int z = -1; z < 2; z++) {
					BlockPos off = pos.add(x, 0, z);
					if ((x != 0 || z != 0) && world.getBlockState(off).getBlock() == this
							&& world.getBlockState(off).getValue(HOLDER))
						world.destroyBlock(pos, false);
				}
			}
		}
	}

	@Override
	public boolean canPlaceBlockAt(Level world, BlockPos pos) {
		boolean canPlace = super.canPlaceBlockAt(world, pos);
		if (canPlace) {
			for (int x = -1; x < 2; x++) {
				for (int z = -1; z < 2; z++) {
					BlockPos off = pos.add(x, 0, z);
					if ((x != 0 || z != 0) && !world.getBlockState(off).getBlock().isReplaceable(world, off))
						return false;
				}
			}
			return true;
		} else
			return false;
	}

	@Override
	public BlockState withRotation(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState withMirror(BlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}

	@Override
	public BlockState getStateFromMeta(int meta) {
		return this.defaultBlockState().setValue(FACING, Direction.from3DDataValue((meta & 3) + 2)).setValue(HOLDER,
				(meta & 4) == 4);
	}

	@Override
	public int getMetaFromState(BlockState state) {
		return state.getValue(FACING).get3DDataValue() - 2 + (state.getValue(HOLDER) ? 4 : 0);
	}

	@Override
	protected StateDefinition createBlockState() {
		return new StateDefinition(this, new Property[] { FACING, HOLDER });
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 4));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(BlockState state) {
		return false;
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState blockState, Level world, BlockPos pos) {
		if (!(world.getBlockEntity(pos) instanceof TileEntityCapturePoint))
			return 0;
		return (int) (((TileEntityCapturePoint) world.getBlockEntity(pos)).captureProgress * 15);
	}
}
