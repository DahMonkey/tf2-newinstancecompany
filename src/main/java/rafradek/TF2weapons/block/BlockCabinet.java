package rafradek.TF2weapons.block;

import rafradek.TF2weapons.TF2weapons;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;

public class BlockCabinet extends Block {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public BlockCabinet() {
		super(Material.IRON);
		setSoundType(SoundType.METAL);
		setDefaultState(blockState.getBaseState().setValue(FACING, Direction.NORTH));
		setCreativeTab(TF2weapons.tabsurvivaltf2);
	}

	@Override
	public RenderShape getRenderType(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, @Nullable Direction side, float hitX, float hitY, float hitZ) {
		if (!world.isClientSide) TF2weapons.openGui((net.minecraft.world.entity.player.Player) player, 1, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
		return this.defaultBlockState().setValue(FACING, placer.getDirection().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		world.setBlock(pos, state.setValue(FACING, placer.getDirection().getOpposite()), 2);
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
		return this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(2 + (meta & 3)));
	}

	@Override
	public int getMetaFromState(BlockState state) {
		return state.getValue(FACING).get3DDataValue() - 2;
	}

	@Override
	protected StateDefinition createBlockState() {
		return new StateDefinition(this, new Property[] { FACING });
	}
}
