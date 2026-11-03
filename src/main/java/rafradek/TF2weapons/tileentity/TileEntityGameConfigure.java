package rafradek.TF2weapons.tileentity;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.arena.GameArena;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityGameConfigure extends BlockEntity implements BlockEntityTicker, IEntityConfigurable {

	private static final String[] OUTPUT_NAMES = {};
	private EntityOutputManager outputManager = new EntityOutputManager(this);
	private String name = "";

	@Override
	public void update() {

	}

	@Override
	public CompoundTag writeToNBT(CompoundTag compound) {
		super.writeToNBT(compound);
		compound.put("Config", this.getOutputManager().writeConfig(new CompoundTag()));

		return compound;
	}

	@Override
	public void readFromNBT(CompoundTag compound) {
		super.readFromNBT(compound);
		this.getOutputManager().readConfig(compound.getCompound("Config"));

	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		return super.receiveClientEvent(id, type);
	}

	@Override
	public boolean hasCapability(net.minecraftforge.common.getAbilities().Capability<?> capability,
			@Nullable net.minecraft.util.EnumFacing facing) {
		if (facing != null && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(net.minecraftforge.common.getAbilities().Capability<T> capability,
			@Nullable net.minecraft.util.EnumFacing facing) {
		return super.getCapability(capability, facing);
	}

	@Override
	public void onLoad() {}

	@Override
	public boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newSate) {
		return super.shouldRefresh(world, pos, oldState, newSate);
	}

	@Override
	protected void setWorldCreate(Level world) {
		this.setWorld(world);
	}

	@Override
	public void setWorld(Level world) {
		super.setWorld(world);
		this.getOutputManager().level = world;
	}

	@Override
	public CompoundTag writeConfig(CompoundTag tag) {
		tag.putString("Arena Name", this.getName());
		GameArena arena = this.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.get(this.getName());
		if (arena != null) {
			arena.writeConfig(tag);
		} else {
			tag.putIntArray("Min Bounds", new int[3]);
			tag.putIntArray("Max Bounds", new int[3]);
		}
		return tag;
	}

	@Override
	public void readConfig(CompoundTag tag) {
		this.name = tag.getString("Arena Name").trim();
		if (!this.name.isEmpty()) {
			GameArena arena = this.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.get(this.getName());
			if (arena != null) {
				arena.readConfig(tag);
			} else {
				arena = new GameArena(this.getWorld(), this.name, this.getBlockPos());
				arena.readConfig(tag);
				this.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.put(this.name, arena);
			}
		}
	}

	@Override
	public EntityOutputManager getOutputManager() {
		return this.outputManager;
	}

	@Override
	public String[] getOutputs() {
		return OUTPUT_NAMES;
	}

	public String getName() {
		return name;
	}

	public void removeGameArena() {
		GameArena arena = this.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).gameArenas.get(this.getName());
		if (arena != null && arena.getName().equals(this.name)) {
			arena.markDelete = true;
		}
	}

}
