package rafradek.TF2weapons.tileentity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import rafradek.TF2weapons.TF2weapons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EntityOutputManager {
	public Level world;
	public Multimap<String, Tuple<BlockPos, Integer>> outputs = HashMultimap.create();
	public String name = "";
	public IEntityConfigurable entity;

	public EntityOutputManager(IEntityConfigurable entity) {
		this.entity = entity;
	}

	public void setWorld(Level world) {
		this.level = world;
	}

	public void loadOutputs(CompoundTag tag) {
		outputs.clear();
		for (String key : tag.getAllKeys()) {
			ListTag list = tag.getList(key, 11);
			for (int i = 0; i < list.size(); i++) {
				int[] arr = ((IntArrayTag) list.get(i)).getIntArray();
				outputs.put(key, new Tuple<>(new BlockPos(arr[0], arr[1], arr[2]), arr[3]));
			}
		}
	}

	public CompoundTag saveOutputs(CompoundTag tag) {
		for (String key : outputs.keySet()) {
			ListTag list = new ListTag();
			for (Tuple<BlockPos, Integer> pos : outputs.get(key)) {
				list.add(new IntArrayTag(new int[] { pos.getA().getX(), pos.getA().getY(),
						pos.getA().getZ(), pos.getB() }));
			}
			tag.put(key, list);
		}
		return tag;
	}

	public void readConfig(CompoundTag tag) {
		this.loadOutputs(tag.getCompound("Outputs"));
		this.name = tag.getString("Link Name");
		this.entity.readConfig(tag);
	}

	public CompoundTag writeConfig(CompoundTag tag) {
		tag.put("Outputs", this.saveOutputs(new CompoundTag()));
		tag.putString("Link Name", name);
		this.entity.writeConfig(tag);
		return tag;
	}

	public void activateOutput(String output, float power, int minTime) {
		TF2weapons.LOGGER.info("activated " + output);
		for (Tuple<BlockPos, Integer> tup : outputs.get(output)) {
			BlockPos pos = tup.getA();
			BlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof BlockButton) {
				world.setBlock(pos, state.setValue(BlockButton.POWERED, Boolean.valueOf(true)), 3);
				world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
				world.notifyNeighborsOfStateChange(pos.move(state.getValue(BlockDirectional.FACING).getOpposite()),
						state.getBlock(), false);
				world.scheduleUpdate(pos, state.getBlock(),
						(int) (state.getBlock().tickRate(world) * power * 15f / tup.getB()));
			} else if (state.getBlock() instanceof BlockLever) {
				state = state.setValue(BlockLever.POWERED, tup.getB() != 0);
				Direction enumfacing = state.getValue(BlockLever.FACING).getFacing();
				world.setBlock(pos, state, 3);
				world.notifyNeighborsOfStateChange(pos.move(enumfacing.getOpposite()), state.getBlock(), false);
				world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
			} else if (state.getBlock() instanceof BlockRedstoneComparator) {
				world.setBlock(pos, state.setValue(BlockButton.POWERED, Boolean.valueOf(true)), 3);
				if (world.getBlockEntity(pos) != null)
					((TileEntityComparator) world.getBlockEntity(pos)).setOutputSignal((int) (tup.getB() * power));

				world.scheduleUpdate(pos, state.getBlock(), minTime);
				world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
			} else if (state.getBlock() instanceof BlockRedstoneWire) {
				state = state.setValue(BlockRedstoneWire.POWER, 15);
				world.setBlock(pos, state, 3);
				world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
			}
		}
	}
}
