package rafradek.TF2weapons.block;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.tileentity.TileEntityRobotDeploy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
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

public class BlockRobotDeploy extends BaseEntityBlock {

	public static final BooleanProperty HOLDER = BooleanProperty.create("holder");
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	public static final BooleanProperty JOINED = BooleanProperty.create("joined");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public BlockRobotDeploy() {
		super(Material.IRON);
		this.setDefaultState(this.blockState.getBaseState().setValue(FACING, Direction.NORTH)
				.setValue(HOLDER, true).setValue(JOINED, false).setValue(ACTIVE, false));
	}

	@Override
	public RenderShape getRenderType(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity createNewTileEntity(Level world, int meta) {
		if ((meta & 4) == 4) {
			TileEntityRobotDeploy upgrades = new TileEntityRobotDeploy();
			// upgrades.generateUpgrades();
			return upgrades;
		}
		return null;
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player,
			InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
		if (!world.isClientSide) {
			if (state.getValue(HOLDER))
				FMLNetworkHandler.openGui(player, TF2weapons.instance, 6, world, pos.getX(), pos.getY(),
						pos.getZ());
			for (int x = -1; x < 2; x++)
				for (int y = -1; y < 1; y++)
					for (int z = -1; z < 2; z++)
						if (world.getBlockState(pos.add(x, y, z)).getBlock() instanceof BlockRobotDeploy
								&& world.getBlockState(pos.add(x, y, z)).getValue(HOLDER)) {
							FMLNetworkHandler.openGui(player, TF2weapons.instance, 6, world, pos.getX() + x,
									pos.getY() + y, pos.getZ() + z);
							return true;
						}

		}
		return true;
	}

	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY,
			float hitZ, int meta, LivingEntity placer) {
		return this.defaultBlockState().setValue(FACING, placer.getDirection().getOpposite());
	}

	@Override
	public void onBlockDestroyedByPlayer(Level world, BlockPos pos, BlockState state) {

	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer,
			ItemStack stack) {
		world.setBlock(pos, state = state.setValue(FACING, placer.getDirection().getOpposite()), 2);

		if (placer instanceof Player) {
			BlockEntity ent = world.getBlockEntity(pos);
			for (Direction facing : Direction.HORIZONTALS) {
				BlockPos offpos = pos.move(facing);
				BlockEntity entoff = world.getBlockEntity(offpos);
				if (entoff instanceof TileEntityRobotDeploy && !world.getBlockState(offpos).getValue(JOINED)
						&& world.getBlockState(offpos).getValue(HOLDER)) {
					Direction placefacing = facing.getClockWise();
					if (placefacing == placer.getDirection()) {
						CompoundTag tag = new CompoundTag();
						entoff.writeToNBT(tag);
						tag.putInt("x", pos.getX());
						tag.putInt("y", pos.getY());
						tag.putInt("z", pos.getZ());
						((TileEntityRobotDeploy) ent).readFromNBT(tag);
						world.setBlockAndUpdate(offpos, world.getBlockState(offpos).setValue(HOLDER, false));
						world.setTileEntity(offpos, null);
						placefacing = placefacing.getOpposite();
					} else {
						state = state.setValue(HOLDER, false);
					}
					state = state.setValue(FACING, placefacing).setValue(JOINED, true);
					world.setBlock(pos, state);
					world.setBlockAndUpdate(offpos,
							world.getBlockState(offpos).setValue(FACING, placefacing).setValue(JOINED, true));
					if (world.getBlockState(offpos.above()).getBlock() == this)
						world.setBlockAndUpdate(offpos.above(), world.getBlockState(offpos.above())
								.setValue(FACING, placefacing).setValue(JOINED, true));
					break;
				}
			}

			/*
			 * TileEntity entbelow = world.getTileEntity(pos.down()); TileEntity entup =
			 * world.getTileEntity(pos.up()); IBlockState statebelow =
			 * world.getBlockState(pos.down()); IBlockState stateup =
			 * world.getBlockState(pos.up()); if (entbelow instanceof
			 * TileEntityRobotDeploy && !world.getBlockState(pos.down()).getValue(JOINED))
			 * { world.setBlockState(pos, state = state.withProperty(JOINED, true));
			 * world.setBlockState(pos, state = state.withProperty(HOLDER, false));
			 * world.setTileEntity(pos, null); world.setBlockState(pos.down(),
			 * statebelow = statebelow.withProperty(JOINED, true)); } if (entup instanceof
			 * TileEntityRobotDeploy && !world.getBlockState(pos.up()).getValue(JOINED)) {
			 * world.setBlockState(pos, state.withProperty(JOINED, true)); NBTTagCompound
			 * tag=new NBTTagCompound();
			 * ((TileEntityRobotDeploy)ent).readFromNBT(entup.writeToNBT(tag));
			 * world.setBlockState(pos.up(), stateup = stateup.withProperty(HOLDER,
			 * false)); world.setTileEntity(pos.up(), null);
			 * world.setBlockState(pos.up(), stateup = stateup.withProperty(JOINED,
			 * true)); }
			 */
			if (ent instanceof TileEntityRobotDeploy)
				((TileEntityRobotDeploy) ent).setOwner(placer.getName(), placer.getUUID());
		}
		if (world.isEmptyBlock(pos.above()))
			world.setBlock(pos.above(), state.setValue(HOLDER, false), 2);
	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state) {

		if (state.getValue(HOLDER)) {
			if (world.getBlockState(pos.above()).getBlock() == this) {
				world.setBlockToAir(pos.above());
			}
		} else {
			if (world.getBlockState(pos.below()).getBlock() == this) {
				world.setBlockToAir(pos.below());
			}
			if (world.getBlockState(pos.above()).getBlock() == this
					&& !world.getBlockState(pos.above()).getValue(HOLDER)) {
				world.setBlockToAir(pos.above());
			}
		}

		if (state.getValue(JOINED)) {
			if (state.getValue(HOLDER) || (world.getBlockState(pos.below()).getBlock() == this
					&& world.getBlockState(pos.below()).getValue(HOLDER)))
				world.destroyBlock(pos.move(state.getValue(FACING).getClockWise()), true);
			else
				world.destroyBlock(pos.move(state.getValue(FACING).getCounterClockWise()), true);
		}

		BlockEntity ent = world.getBlockEntity(pos);
		if (ent instanceof TileEntityRobotDeploy) {
			((TileEntityRobotDeploy) ent).dropInventory();
		}
	}

	@Override
	public boolean canPlaceBlockAt(Level world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos)
				&& world.getBlockState(pos.above()).getBlock().isReplaceable(world, pos.above());
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
		return this.defaultBlockState().setValue(FACING, Direction.from3DDataValue((meta & 3) + 2))
				.setValue(HOLDER, (meta & 4) == 4).setValue(JOINED, (meta & 8) == 8);
	}

	@Override
	public int getMetaFromState(BlockState state) {
		return state.getValue(FACING).get3DDataValue() - 2 + (state.getValue(HOLDER) ? 4 : 0)
				+ (state.getValue(JOINED) ? 8 : 0);
	}

	@Override
	protected StateDefinition createBlockState() {
		return new StateDefinition(this, new Property[] { FACING, HOLDER, JOINED, ACTIVE });
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> list) {
		list.add(TF2Util.metaStack(this, 1, 4));
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
}
