package rafradek.TF2weapons.block;

import net.minecraft.core.NonNullList;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockProp extends Block {

	public static final EnumProperty<EnumBlockType> TYPE = EnumProperty.<EnumBlockType>create("type",
			EnumBlockType.class);

	public BlockProp(Material materialIn) {
		super(materialIn);
	}

	public BlockProp(Material blockMaterialIn, MaterialColor blockMapColorIn) {
		super(blockMaterialIn, blockMapColorIn);
	}

	@OnlyIn(Dist.CLIENT)
	public void getSubBlocks(Item itemIn, CreativeModeTab tab, NonNullList<ItemStack> list) {

	}

	@Override
	public BlockState getStateFromMeta(int meta) {
		return this.defaultBlockState().setValue(TYPE, EnumBlockType.values()[meta]);
	}

	@Override
	public int getMetaFromState(BlockState state) {
		return (state.getValue(TYPE)).ordinal();
	}

	@Override
	protected StateDefinition createBlockState() {
		return new StateDefinition(this, new Property[] { TYPE });
	}

	public enum EnumBlockType implements StringRepresentable {
		DIAMOND("diamond"), IRON("iron"), GOLD("gold"), OBSIDIAN("obsidian");

		private final String name;

		private EnumBlockType(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}
}
