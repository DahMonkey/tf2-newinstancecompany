package rafradek.TF2weapons.block;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.tileentity.TileEntityAmmoFurnace;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

public class BlockAmmoFurnace extends BaseEntityBlock {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty BURNING = BooleanProperty.create("burning");
	private final boolean electric;

	public BlockAmmoFurnace(boolean electric) {
		super(Material.IRON);
		this.setSoundType(SoundType.METAL);
		this.setDefaultState(
				this.blockState.getBaseState().setValue(FACING, Direction.NORTH).setValue(BURNING, false));
		setCreativeTab(TF2weapons.tabsurvivaltf2);
		this.electric = electric;
	}

	@Override
	public RenderShape getRenderType(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity createNewTileEntity(Level world, int meta) {
		return new TileEntityAmmoFurnace();
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player,
			InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
		if (!world.isClientSide)
			FMLNetworkHandler.openGui(player, TF2weapons.instance, 3, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("incomplete-switch")
	public void randomDisplayTick(BlockState stateIn, Level world, BlockPos pos, Random rand) {
		if (stateIn.getValue(BURNING)) {
			Direction enumfacing = stateIn.getValue(FACING);
			double d0 = pos.getX() + 0.5D;
			double d1 = pos.getY() + rand.nextDouble() * 6.0D / 16.0D;
			double d2 = pos.getZ() + 0.5D;
			double d3 = 0.52D;
			double d4 = rand.nextDouble() * 0.6D - 0.3D;

			if (rand.nextDouble() < 0.1D)
				world.playSound(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
						SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);

			switch (enumfacing) {
			case WEST:
				world.addParticle(ParticleTypes.SMOKE_NORMAL, d0 - 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D,
						new int[0]);
				world.addParticle(ParticleTypes.FLAME, d0 - 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D, new int[0]);
				break;
			case EAST:
				world.addParticle(ParticleTypes.SMOKE_NORMAL, d0 + 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D,
						new int[0]);
				world.addParticle(ParticleTypes.FLAME, d0 + 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D, new int[0]);
				break;
			case NORTH:
				world.addParticle(ParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 - 0.52D, 0.0D, 0.0D, 0.0D,
						new int[0]);
				world.addParticle(ParticleTypes.FLAME, d0 + d4, d1, d2 - 0.52D, 0.0D, 0.0D, 0.0D, new int[0]);
				break;
			case SOUTH:
				world.addParticle(ParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 + 0.52D, 0.0D, 0.0D, 0.0D,
						new int[0]);
				world.addParticle(ParticleTypes.FLAME, d0 + d4, d1, d2 + 0.52D, 0.0D, 0.0D, 0.0D, new int[0]);
			}
		}
	}

	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY,
			float hitZ, int meta, LivingEntity placer) {
		return getDefaultState().setValue(FACING, placer.getDirection().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		world.setBlock(pos, state.setValue(FACING, placer.getDirection().getOpposite()), 2);
		if (stack.hasCustomHoverName()) {
			BlockEntity tileentity = world.getBlockEntity(pos);
			if (tileentity instanceof TileEntityAmmoFurnace)
				((TileEntityAmmoFurnace) tileentity).setCustomInventoryName(stack.getHoverName());
		}
	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state) {
		BlockEntity tileentity = world.getBlockEntity(pos);
		if (tileentity instanceof TileEntityAmmoFurnace) {
			Containers.dropInventoryItems(world, pos, (TileEntityAmmoFurnace) tileentity);
			world.updateComparatorOutputLevel(pos, this);
		}
		super.breakBlock(world, pos, state);
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
		Direction facing = Direction.from3DDataValue(meta & 7);
		if (facing.getAxis() == Direction.Axis.Y)
			facing = Direction.NORTH;
		return getDefaultState().setValue(FACING, facing).setValue(BURNING, (meta & 8) == 8);
	}

	@Override
	public int getMetaFromState(BlockState state) {
		return state.getValue(FACING).get3DDataValue() + (state.getValue(BURNING) ? 8 : 0);
	}

	@Override
	protected StateDefinition createBlockState() {
		return new StateDefinition(this, new Property[] { FACING, BURNING });
	}

	public static void setState(boolean active, Level world, BlockPos pos) {
		BlockState iblockstate = world.getBlockState(pos);
		BlockEntity tileentity = world.getBlockEntity(pos);
		world.setBlockAndUpdate(pos, iblockstate.setValue(BURNING, active));
		if (tileentity != null) {
			tileentity.validate();
			world.setTileEntity(pos, tileentity);
		}
	}
}
