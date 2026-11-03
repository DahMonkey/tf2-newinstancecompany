package rafradek.TF2weapons.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/** Merasmus's hiding-spot prop block, looking like a vanilla ore block. */
public class BlockProp extends Block {

	public static final EnumProperty<EnumBlockType> TYPE = EnumProperty.create("type", EnumBlockType.class);

	public BlockProp(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, EnumBlockType.DIAMOND));
	}

	public BlockState getStateFromMeta(int meta) {
		return this.defaultBlockState().setValue(TYPE, EnumBlockType.values()[meta]);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TYPE);
	}

	public enum EnumBlockType implements StringRepresentable {
		DIAMOND("diamond"), IRON("iron"), GOLD("gold"), OBSIDIAN("obsidian");

		private final String name;

		EnumBlockType(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
