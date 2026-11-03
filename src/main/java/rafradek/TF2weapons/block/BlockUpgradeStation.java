package rafradek.TF2weapons.block;

import net.minecraft.util.RandomSource;
import rafradek.TF2weapons.util.TF2Util;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.tileentity.TileEntityUpgrades;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockUpgradeStation extends BaseEntityBlock {

	public static final BooleanProperty HOLDER = BooleanProperty.create("holder");
	public static final BooleanProperty PLACED = BooleanProperty.create("placed");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public BlockUpgradeStation() {
		super(Material.IRON);
		setSoundType(SoundType.METAL);
		setDefaultState(blockState.getBaseState().setValue(FACING, Direction.NORTH)
				.setValue(HOLDER, true).setValue(PLACED, false));
		setCreativeTab(TF2weapons.tabutilitytf2);
	}

	@Override
	public RenderShape getRenderType(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity createNewTileEntity(Level world, int meta) {
		return  ((meta & 8) == 8) ? new TileEntityUpgrades(world) : null;
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
		if (!world.isClientSide)
			if (state.getValue(HOLDER)) FMLNetworkHandler.openGui(player, TF2weapons.instance, 2, world, pos.getX(), pos.getY(), pos.getZ());
			else for (int x = -1; x < 2; x++)
				for (int y = -1; y < 2; y++)
					for (int z = -1; z < 2; z++)
						if (world.getBlockState(pos.add(x, y, z)).getBlock() instanceof BlockUpgradeStation
								&& world.getBlockState(pos.add(x, y, z)).getValue(HOLDER)) {
							FMLNetworkHandler.openGui(player, TF2weapons.instance, 2, world, pos.getX() + x,
									pos.getY() + y, pos.getZ() + z);
							return true;
						}
		return true;
	}

	@Override
	@Deprecated
	public float getBlockHardness(BlockState blockState, Level world, BlockPos pos) {
		return blockState.getValue(PLACED) ? blockHardness : -1;
	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state) {
		BlockEntity tileentity = world.getBlockEntity(pos);
		if (state.getValue(PLACED) && tileentity instanceof TileEntityUpgrades) {
			ItemStack itemstack = new ItemStack(Item.byBlock(this));
			CompoundTag nbt = new CompoundTag();
			CompoundTag tetag = new CompoundTag();
			((TileEntityUpgrades) tileentity).writeToNBT(tetag);
			nbt.remove("id");
			nbt.put("BlockEntityTag", tetag);
			itemstack.setTag(nbt);
			spawnAsEntity(world, pos, itemstack);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
		return getDefaultState().setValue(FACING, placer.getDirection().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer,
			ItemStack stack) {
		state = state.setValue(FACING, placer.getDirection().getOpposite()).setValue(PLACED, true);
		world.setBlock(pos, state, 2);
		Direction dirop = placer.getDirection().getClockWise();
		BlockState statehelp = state.setValue(HOLDER, false);
		if (world.isEmptyBlock(pos.move(dirop)))
			world.setBlock(pos.move(dirop), statehelp);
		if (world.isEmptyBlock(pos.move(dirop, -1)))
			world.setBlock(pos.move(dirop, -1), statehelp);
		if (world.isEmptyBlock(pos.move(dirop, -1).above()))
			world.setBlock(pos.move(dirop, -1).above(), statehelp);
		if (world.isEmptyBlock(pos.above()))
			world.setBlock(pos.above(), statehelp);
		if (world.isEmptyBlock(pos.move(dirop).above()))
			world.setBlock(pos.move(dirop).above(), statehelp);
	}

	@Override
	public void onBlockDestroyedByPlayer(Level world, BlockPos pos, BlockState state) {
		super.onBlockDestroyedByPlayer(world, pos, state);
		if (state.getValue(HOLDER))
			breakBlockAround(world, pos, state);
		for (int x = -1; x < 2; x++)
			for (int y = -1; y < 2; y++)
				for (int z = -1; z < 2; z++)
					if (world.getBlockState(pos.add(x, y, z)).getBlock() instanceof BlockUpgradeStation
							&& world.getBlockState(pos.add(x, y, z)).getValue(HOLDER)) {
						breakBlockAround(world, pos.add(x, y, z), state);
						world.destroyBlock(pos.add(x, y, z), true);
						return;
					}
	}

	public void breakBlockAround(Level world, BlockPos pos, BlockState state) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (world.getBlockState(pos.add(x, y, z)).getBlock() instanceof BlockUpgradeStation
							&& !world.getBlockState(pos.add(x, y, z)).getValue(HOLDER)) {
						world.setBlockToAir(pos.add(x, y, z));
					}
				}
			}
		}

	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 */
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
				.setValue(HOLDER, (meta & 8) == 8).setValue(PLACED, (meta & 4) == 4);
	}

	@Override
	public int getMetaFromState(BlockState state) {
		return state.getValue(FACING).get3DDataValue() - 2 + (state.getValue(HOLDER) ? 8 : 0)
				+ (state.getValue(PLACED) ? 4 : 0);
	}

	@Override
	protected StateDefinition createBlockState() {
		return new StateDefinition(this, new Property[] { FACING, HOLDER, PLACED });
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> list) {
		list.add(TF2Util.metaStack(this, 1, 8));
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
	public Item getItemDropped(BlockState state, RandomSource random, int fortune) {
		return null;
	}
}
