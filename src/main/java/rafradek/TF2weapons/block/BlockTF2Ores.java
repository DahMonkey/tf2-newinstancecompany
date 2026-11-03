package rafradek.TF2weapons.block;

import net.minecraft.core.NonNullList;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockTF2Ores extends DropExperienceBlock {

	public static final EnumProperty<EnumOreType> TYPE = EnumProperty.<EnumOreType>create("oreType", EnumOreType.class);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> list) {
		for (EnumOreType type : EnumOreType.values()) list.add(new ItemStack(this, 1, type.ordinal()));
	}

	@Override
	public BlockState getStateFromMeta(int meta) {
		return getDefaultState().setValue(TYPE, EnumOreType.values()[meta]);
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		return state.getValue(TYPE).ordinal();
	}

	@Override
	protected StateDefinition createBlockState() {
		return new StateDefinition(this, new Property[] { TYPE });
	}
	
	public enum EnumOreType implements StringRepresentable {
		
		COPPER("copper"), LEAD("lead"), AUSTRALIUM("australium");
		
		private String name;
		
		EnumOreType(String name) {
			this.name = name;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
	}
	
}
