package rafradek.TF2weapons.tileentity;

import rafradek.TF2weapons.TF2weapons;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityCapturePoint extends BlockEntity implements BlockEntityTicker, IEntityConfigurable {

	private static final String[] OUTPUT_NAMES = { "On RED started capture", "On BLU started capture", "On RED capture",
			"On BLU capture", "On RED capture break", "On BLU capture break", "Capture progress" };
	public Team team;
	public float captureProgress;
	public Team contestingTeam;
	public int enemyCount;
	public boolean blocked;
	public boolean enabled = true;
	public int updateTicks = 0;
	public Team defaultTeam;
	public int pointId = 0;
	public int nextPointId = -1;
	private EntityOutputManager outputManager = new EntityOutputManager(this);

	public void setEnabled(boolean enable) {

		this.level.addBlockEvent(this.pos, this.getBlockType(), 0, enabled ? 1 : 0);
		if (this.enabled != enable) {
			this.enabled = enable;
			this.level.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
		}

	}

	@Override
	public void update() {
		if (!this.level.isClientSide) {
			float prevProgress = captureProgress;
			if (this.enabled) {
				boolean blocked = false;
				Team enemyteam = null;
				int enemynumber = 0;
				for (LivingEntity living : this.level.getEntitiesOfClass(LivingEntity.class,
						new AABB(this.pos).inflate(2), entityf -> (entityf.isAlive()
								&& entityf.getTeam() != null && entityf.getCapability(TF2weapons.WEAPONS_CAP).isPresent()))) {
					if (living.getTeam() == team || (contestingTeam != null && living.getTeam() != contestingTeam))
						blocked = true;
					else if (living.getTeam() == contestingTeam || contestingTeam == null) {
						enemyteam = living.getTeam();
						enemynumber += 1;
					}

				}
				if (enemyteam != null && contestingTeam == null) {
					contestingTeam = enemyteam;
					if (enemyteam.getName().equals("RED"))
						this.activateOutput("On RED started capture");
					else if (enemyteam.getName().equals("BLU"))
						this.activateOutput("On BLU started capture");
				}
				this.blocked = blocked;
				this.enemyCount = enemynumber;
				System.out.println("blocked: " + blocked + " " + this.enemyCount + " " + this.captureProgress + " "
						+ this.team + " " + this.contestingTeam);
			} else {
				captureProgress = 0;
				this.enemyCount = 0;
			}
			if (contestingTeam != null) {
				float delta = 0;
				if (!blocked && enemyCount > 0) {
					delta = 0.01f * enemyCount;
				}
				if (enemyCount == 0) {
					delta = -0.01f;
				}
				if (++this.updateTicks % 5 == 0) {
					this.level.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
					this.activateOutput("Capture progress", this.captureProgress, 5);
				}
				captureProgress += delta;
				if (captureProgress >= 1) {
					team = contestingTeam;
					contestingTeam = null;
					captureProgress = 0;
					if (team.getName().equals("RED"))
						this.activateOutput("On RED capture");
					else if (team.getName().equals("BLU"))
						this.activateOutput("On BLU capture");
				} else if (this.enemyCount == 0 && captureProgress <= 0) {
					contestingTeam = null;
					if (team.getName().equals("RED"))
						this.activateOutput("On RED capture break");
					else if (team.getName().equals("BLU"))
						this.activateOutput("On BLU capture break");
				}
			}
		}
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag compound) {
		super.writeToNBT(compound);
		compound.put("Config", this.getOutputManager().writeConfig(new CompoundTag()));
		compound.putBoolean("Enabled", this.enabled);
		if (this.team != null)
			compound.putString("Team", this.team.getName());

		return compound;
	}

	@Override
	public void readFromNBT(CompoundTag compound) {
		super.readFromNBT(compound);
		this.getOutputManager().readConfig(compound.getCompound("Config"));
		this.enabled = compound.getBoolean("Enabled");
		if (this.hasWorld() && compound.contains("Team"))
			this.team = this.level.getScoreboard().getTeam(compound.getString("Team"));
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			this.enabled = type != 0;
			return true;
		} else {
			return super.receiveClientEvent(id, type);
		}
	}

	/*
	 * @Nullable public SPacketUpdateTileEntity getUpdatePacket() { NBTTagCompound
	 * tag = new NBTTagCompound(); if (this.maxprogress > 0) { tag.setByte("P",
	 * (byte) ((float)this.progress/(float)this.maxprogress*7f)); if (this.progress
	 * > 0) tag.setByte("C", (byte)
	 * ItemToken.getClassID(TF2Util.getWeaponUsedByClass(this.weapon.extractItem(
	 * hasWeapon,64,true)))); } return new SPacketUpdateTileEntity(this.pos, 9999,
	 * tag); }
	 * 
	 * public void onDataPacket(net.minecraft.network.NetworkManager net,
	 * net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
	 * this.progressClient = pkt.getNbtCompound().getByte("P"); this.classType =
	 * pkt.getNbtCompound().getByte("C"); }
	 */

	@Override
	public boolean hasCapability(Capability<?> capability,
			@Nullable Direction facing) {
		if (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability,
			@Nullable Direction facing) {
		/*
		 * if (facing != null && capability ==
		 * net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) { if
		 * (facing.getAxis() == Axis.Y) return
		 * CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.weapon); else if
		 * (facing.getAxis() == Axis.X) return
		 * CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.parts); else return
		 * CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.money); } else
		 */
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
		tag.putInt("Control Point ID", this.pointId);
		tag.putInt("Next Control Point ID", this.nextPointId);
		if (this.defaultTeam != null)
			tag.putString("T:Default Team", this.defaultTeam.getName());
		else
			tag.putString("T:Default Team", "");
		return tag;
	}

	@Override
	public void readConfig(CompoundTag tag) {
		this.pointId = tag.getInt("Control Point ID");
		this.nextPointId = tag.getInt("Next Control Point ID");
		if (this.hasWorld() && tag.contains("T:Default Team"))
			this.defaultTeam = this.level.getScoreboard().getTeam(tag.getString("T:Default Team"));
	}

	@Override
	public EntityOutputManager getOutputManager() {
		return this.outputManager;
	}

	@Override
	public String[] getOutputs() {
		return OUTPUT_NAMES;
	}

}
