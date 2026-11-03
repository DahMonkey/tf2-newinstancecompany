package rafradek.TF2weapons.block;

import net.minecraft.util.RandomSource;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.tileentity.TileEntityOverheadDoor;
import rafradek.TF2weapons.tileentity.TileEntityOverheadDoor.Allow;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;

public class BlockOverheadDoor extends BaseEntityBlock {

	public static final BooleanProperty HOLDER = BooleanProperty.create("holder");
	public static final BooleanProperty SLIDING = BooleanProperty.create("sliding");
	public static final BooleanProperty CONTROLLER = BooleanProperty.create("controller");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	protected static final AABB SOUTH_AABB = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.03125D);
	protected static final AABB NORTH_AABB = new AABB(0.0D, 0.0D, 0.96875D, 1.0D, 1.0D, 1.0D);
	protected static final AABB WEST_AABB = new AABB(0.96875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	protected static final AABB EAST_AABB = new AABB(0.0D, 0.0D, 0.0D, 0.03125D, 1.0D, 1.0D);

	public BlockOverheadDoor() {
		super(Material.IRON, MaterialColor.IRON);
		this.setLightOpacity(TF2ConfigVars.doorBlockLight ? 255 : 0);
		this.setDefaultState(this.blockState.getBaseState().setValue(FACING, Direction.NORTH)
				.setValue(HOLDER, true).setValue(SLIDING, false));
	}

	@Override
	public RenderShape getRenderType(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity createNewTileEntity(Level world, int meta) {
		return  ((meta & 4) == 4) ? new TileEntityOverheadDoor() : null;
	}

	@Override
	public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
		switch (state.getValue(FACING)) {
		case NORTH:
			return SOUTH_AABB;
		case EAST:
			return WEST_AABB;
		case SOUTH:
			return NORTH_AABB;
		case WEST:
			return EAST_AABB;
		default:
			return FULL_BLOCK_AABB;
		}
	}

	@Override
	public AABB getCollisionBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
		if (state.getValue(SLIDING)) return NULL_AABB;
		switch (state.getValue(FACING)) {
		case NORTH:
			return SOUTH_AABB;
		case EAST:
			return WEST_AABB;
		case SOUTH:
			return NORTH_AABB;
		case WEST:
			return EAST_AABB;
		default:
			return NULL_AABB;
		}
	}

	@Override
	public void onBlockAdded(Level world, BlockPos pos, BlockState state) {
		world.scheduleUpdate(pos, this, tickRate(world));
	}

	@Override
	public BlockFaceShape getBlockFaceShape(BlockGetter world, BlockState state, BlockPos pos, Direction face) {
		return face == state.getValue(FACING) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean isFullCube(BlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}

	@Override
	public int getLightOpacity(BlockState state, BlockGetter world, BlockPos pos) {
		return state.getValue(SLIDING) ? 0 : super.getLightOpacity(state, world, pos);
	}

	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
		return getDefaultState().setValue(FACING, placer.getDirection().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		world.setBlock(pos, state.setValue(FACING, placer.getDirection().getOpposite()), 2);
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
		return getDefaultState().setValue(FACING, Direction.from3DDataValue(2 + (meta & 3)))
				.setValue(HOLDER, (meta & 4) == 4).setValue(SLIDING, (meta & 8) == 8);
	}

	@Override
	public int getMetaFromState(BlockState state) {
		return state.getValue(FACING).get3DDataValue() - 2 + (state.getValue(HOLDER) ? 4 : 0)
				+ (state.getValue(SLIDING) ? 8 : 0);
	}

	@Override
	protected StateDefinition createBlockState() {
		return new StateDefinition(this, new Property[] { FACING, HOLDER, SLIDING });
	}

	@Override
	public void onBlockDestroyedByPlayer(Level world, BlockPos pos, BlockState state) {
		BlockPos.MutableBlockPos off = new BlockPos.MutableBlockPos(pos).relative(Direction.UP);
		while (world.getBlockState(off).getBlock() == this) {
			world.destroyBlock(off, true);
			off.relative(Direction.UP);
		}
		off.setY(pos.getY() - 1);
		while (world.getBlockState(off).getBlock() == this) {
			world.destroyBlock(off, true);
			off.relative(Direction.DOWN);
		}
	}

	@Override
	public Item getItemDropped(BlockState state, RandomSource random, int fortune) {
		return state.getValue(HOLDER) ? super.getItemDropped(state, random, fortune) : Items.AIR;
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		BlockEntity te = world.getBlockEntity(pos);
		if (te instanceof TileEntityOverheadDoor) ((TileEntityOverheadDoor) te).powered = world.isBlockPowered(pos);
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockPathTypes getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos) {
		return super.getAiPathNodeType(state, world, pos);
	}

	@Override
	public boolean isPassable(BlockGetter world, BlockPos pos) {
		if (world.getBlockState(pos).getValue(SLIDING)) return true;
		BlockPos.MutableBlockPos off = new BlockPos.MutableBlockPos(pos);
		for (int y = 0; y < 5; y++) {
			if (world.getBlockEntity(off) instanceof TileEntityOverheadDoor) {
				TileEntityOverheadDoor ent = (TileEntityOverheadDoor) world.getBlockEntity(off);
				return ent.allow == Allow.ENTITY || ent.allow == Allow.TEAM;
			}
			off.relative(Direction.UP);
		}
		return false;
	}
}
