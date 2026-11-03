package rafradek.TF2weapons.tileentity;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.block.BlockOverheadDoor;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;

public class TileEntityOverheadDoor extends BlockEntity implements BlockEntityTicker {

	public float amountScrolled = 1;
	public Team team;
	public Allow allow;
	boolean entitySome;
	// boolean master;
	public BlockPos minBounds;
	public BlockPos maxBounds;
	public float motion;
	public boolean powered;
	public boolean hasEntity;
	public boolean clientOpen;
	public boolean lastIsEntity;
	public long tickTime;

	public enum Allow {
		ENTITY, PLAYER, TEAM
	}

	public TileEntityOverheadDoor() {}

	public float getUpSpeed() {
		return 0.25f;
	}

	public float getDownSpeed() {
		return 0.25f;
	}

	@Override
	public void invalidate() {

		if (this.hasWorld()) {
			for (Direction facing : Direction.HORIZONTALS) {
				BlockPos sidepos = this.pos.move(facing);
				BlockEntity ent = this.level.getBlockEntity(sidepos);
				if (ent instanceof TileEntityOverheadDoor) {
					((TileEntityOverheadDoor) ent).updateMasterStatus();
				}
			}
			this.dropController();
		}
		super.invalidate();
	}

	@Override
	public void update() {

		if (minBounds == null)
			minBounds = this.pos;
		if (minBounds != null) {
			BlockState state = this.level.getBlockState(minBounds.down());
			if (state.getBlock().isAir(state, world, minBounds.down()))
				this.minBounds = minBounds.down();
			else if (this.level.getBlockState(this.minBounds.down()).getBlock() instanceof BlockOverheadDoor) {
				this.minBounds = minBounds.down();
				this.amountScrolled += 1;
			} else if (!(this.level.getBlockState(this.minBounds).getBlock() instanceof BlockOverheadDoor
					|| world.isEmptyBlock(this.minBounds))) {
				this.minBounds = new BlockPos(this.minBounds.getX(), this.pos.getY(), this.minBounds.getZ());
			}
		}

		if (!this.level.isClientSide && this.allow != null && !powered) {
			hasEntity = this.level.getEntitiesOfClass(LivingEntity.class,
					new AABB(this.maxBounds, this.minBounds).inflate(2, 1, 2),
					input -> ((allow == Allow.PLAYER && input instanceof Player)
							|| (allow == Allow.TEAM && input.getTeam() == team)
							|| (allow == Allow.ENTITY && input instanceof LivingEntity)))
					.size() > 0;

		} else
			hasEntity = false;
		this.motion = 0;
		boolean isEntity = powered || this.hasEntity || this.entitySome || this.clientOpen;

		if (isEntity && !this.entitySome && !this.level.isClientSide)
			this.updateAmountScrolled(15, false);

		if (isEntity && amountScrolled > 0.25) {
			this.motion = -this.getUpSpeed();

		}
		if (!isEntity && !entitySome && pos.getY() - this.minBounds.getY() + 1 > amountScrolled) {
			this.motion = this.getDownSpeed();

		}
		if (this.motion != 0) {
			amountScrolled += this.motion;

		}
		if (lastIsEntity != isEntity) {
			this.level.addBlockEvent(this.pos, this.getBlockType(), 1, isEntity ? 1 : 0);
			if (!entitySome) {
				this.updateAmountScrolled(15, true);
			}
		}

		boolean isClosed = this.level.getBlockState(this.minBounds).getBlock() == this.getBlockType();
		// this.world.setBlockState(pos,
		// this.world.getBlockState(pos).withProperty(BlockOverheadDoor.SLIDING,
		// !isClosed));
		if (isClosed && pos.getY() - this.minBounds.getY() + 1 > amountScrolled) {
			if (!this.level.isClientSide) {
				for (int y = pos.getY() - 1; y >= this.minBounds.getY(); y--) {
					// IBlockState state = this.world.getBlockState(pos);
					BlockPos doorpos = new BlockPos(pos.getX(), y, pos.getZ());
					if (this.level.getBlockState(doorpos).getBlock() == this.getBlockType())
						this.level.removeBlock(doorpos, false);
				}
				this.level.setBlockAndUpdate(pos,
						this.level.getBlockState(pos).setValue(BlockOverheadDoor.SLIDING, true));
			}
		} else if (!isClosed && pos.getY() - this.minBounds.getY() + 1 <= amountScrolled) {

			BlockState state = this.level.getBlockState(pos).setValue(BlockOverheadDoor.HOLDER, false)
					.setValue(BlockOverheadDoor.SLIDING, false);
			for (int y = pos.getY() - 1; y >= this.minBounds.getY(); y--) {
				BlockPos doorpos = new BlockPos(pos.getX(), y, pos.getZ());
				if (this.level.isEmptyBlock(doorpos)) {
					if (!this.level.isClientSide)
						this.level.setBlock(doorpos, state);
				}

				else
					this.minBounds = new BlockPos(pos.getX(), doorpos.getY() + 1, pos.getZ());
			}
			if (!this.level.isClientSide)
				this.level.setBlockAndUpdate(pos,
						this.level.getBlockState(pos).setValue(BlockOverheadDoor.SLIDING, false));
		}
		// System.out.println("done"+this.minBounds.getY()+" "+this.pos.getY()+" ");
		this.lastIsEntity = isEntity;
		this.tickTime = this.level.getGameTime();
		entitySome = false;
	}

	public boolean isPowered() {
		return this.hasEntity || this.powered || this.clientOpen || this.entitySome;
	}

	public void updateAmountScrolled(int reach, boolean updateEvents) {
		Direction facing = this.level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING).rotateAround(Axis.Y);
		if (facing.getAxisDirection() == AxisDirection.NEGATIVE)
			facing = facing.getOpposite();
		int axispos = TF2Util.getValueOnAxis(this.minBounds, facing.getAxis());
		int minReach = axispos - reach;
		int maxReach = axispos + reach;
		for (int i = axispos - 1; i >= minReach; i--) {
			BlockPos pos = TF2Util.setValueOnAxis(this.pos, facing.getAxis(), i);
			BlockEntity ent = this.level.getBlockEntity(pos);
			if (ent instanceof TileEntityOverheadDoor) {
				if (((TileEntityOverheadDoor) ent).amountScrolled >= this.amountScrolled) {
					if (!updateEvents)
						((TileEntityOverheadDoor) ent).entitySome = true;
					else
						this.level.addBlockEvent(pos, this.getBlockType(), 1,
								((TileEntityOverheadDoor) ent).isPowered() ? 1 : 0);
				}
			} else
				break;
		}
		for (int i = axispos + 1; i <= maxReach; i++) {
			BlockPos pos = TF2Util.setValueOnAxis(this.pos, facing.getAxis(), i);
			BlockEntity ent = this.level.getBlockEntity(pos);
			if (ent instanceof TileEntityOverheadDoor) {
				if (((TileEntityOverheadDoor) ent).amountScrolled >= this.amountScrolled) {
					if (!updateEvents)
						((TileEntityOverheadDoor) ent).entitySome = true;
					else
						this.level.addBlockEvent(pos, this.getBlockType(), 1,
								((TileEntityOverheadDoor) ent).isPowered() ? 1 : 0);
				}
			} else
				break;
		}
	}

	@Override
	public void onLoad() {
		updateMasterStatus();
	}

	public void updateMasterStatus() {
		Direction facing = this.level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING).rotateAround(Axis.Y);
		if (facing.getAxisDirection() == AxisDirection.NEGATIVE)
			facing = facing.getOpposite();
		BlockPos minBounds = this.pos;
		BlockPos maxBounds = this.pos;
		/*
		 * for(int i = 1; i <= 16; i++) { Vec3i offset = facing.getDirectionVec();
		 * offset = new BlockPos(offset.getX()*i, offset.getY()*i, offset.getZ()*i);
		 * BlockPos nearpos = this.pos.add(offset); if
		 * (this.world.getBlockState(nearpos).getBlock() == this.getBlockType()) {
		 * TileEntity ent = this.world.getTileEntity(nearpos); if (ent instanceof
		 * TileEntityOverheadDoor && ((TileEntityOverheadDoor)ent).master) { BlockPos
		 * pos = ((TileEntityOverheadDoor)ent).minBounds; pos =
		 * TF2Util.setValueOnAxis(pos, facing.getAxis(),
		 * Math.min(TF2Util.getValueOnAxis(pos, facing.getAxis()),
		 * TF2Util.getValueOnAxis(this.pos, facing.getAxis())));
		 * ((TileEntityOverheadDoor)ent).minBounds = pos; this.amountScrolled =
		 * ((TileEntityOverheadDoor)ent).amountScrolled; this.minBounds = new
		 * BlockPos(this.pos.getX(),((TileEntityOverheadDoor)ent).minBounds.getY(),this.
		 * pos.getZ()); //this.master = false; return; } maxBounds = nearpos; } else
		 * break; } for(int i = -1; i >= -16; i--) { Vec3i offset =
		 * facing.getDirectionVec(); offset = new BlockPos(offset.getX()*i,
		 * offset.getY()*i, offset.getZ()*i); BlockPos nearpos = this.pos.add(offset);
		 * 
		 * if (this.world.getBlockState(nearpos).getBlock() == this.getBlockType()) {
		 * TileEntity ent = this.world.getTileEntity(nearpos);
		 * 
		 * if (ent instanceof TileEntityOverheadDoor &&
		 * ((TileEntityOverheadDoor)ent).master) { BlockPos pos =
		 * ((TileEntityOverheadDoor)ent).maxBounds; pos = TF2Util.setValueOnAxis(pos,
		 * facing.getAxis(), Math.max(TF2Util.getValueOnAxis(pos, facing.getAxis()),
		 * TF2Util.getValueOnAxis(this.pos, facing.getAxis())));
		 * ((TileEntityOverheadDoor)ent).maxBounds = pos; this.amountScrolled =
		 * ((TileEntityOverheadDoor)ent).amountScrolled; this.minBounds = new
		 * BlockPos(this.pos.getX(),((TileEntityOverheadDoor)ent).minBounds.getY(),this.
		 * pos.getZ()); //this.master = false; return; } minBounds = nearpos; } else
		 * break; }
		 */
		// this.master = true;
		this.minBounds = minBounds;
		this.maxBounds = maxBounds;
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			if (this.level.isClientSide) {
				if (type == 0)
					this.clientOpen = false;
				else
					this.clientOpen = true;
			}
			return true;
		}
		return super.receiveClientEvent(id, type);
	}

	@Override
	public boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newSate) {
		return oldState.getBlock() != newSate.getBlock() || !newSate.getValue(BlockOverheadDoor.HOLDER);
	}

	@Override
	public AABB getRenderBoundingBox() {
		if (this.minBounds == null)
			return new AABB(pos);
		else
			return new AABB(pos, this.minBounds).grow(1);
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag compound) {
		super.writeToNBT(compound);
		// compound.setBoolean("Master", this.master);
		if (team != null)
			compound.putString("Team", this.team.getName());
		if (allow != null)
			compound.putByte("Allow", (byte) this.allow.ordinal());
		return compound;
	}

	@Override
	public void readFromNBT(CompoundTag compound) {
		super.readFromNBT(compound);
		// this.master = compound.getBoolean("Master");
		if (this.hasWorld() && compound.contains("Team"))
			this.team = this.level.getScoreboard().getTeam(compound.getString("Team"));
		if (compound.contains("Allow"))
			this.allow = Allow.values()[compound.getByte("Allow")];
	}

	public boolean setController(String string) {
		this.dropController();
		if (string.equals("players"))
			allow = Allow.PLAYER;
		else if (string.equals("mobs"))
			allow = Allow.ENTITY;
		else {
			Team target = this.level.getScoreboard().getTeam(string);
			if (target == null)
				return false;
			allow = Allow.TEAM;
			team = target;
		}
		return true;
	}

	public void dropController() {
		if (allow == null)
			return;
		int meta = 0;
		if (allow == Allow.ENTITY)
			meta = 1;
		else if (allow == Allow.TEAM) {
			if (this.team != null && team.getName().equals("RED"))
				meta = 2;
			else if (this.team != null && team.getName().equals("BLU"))
				meta = 3;
		}
		this.getWorld().addFreshEntity(new ItemEntity(this.level, this.pos.getX(), this.pos.getY(), this.pos.getZ(),
				new ItemStack(TF2weapons.itemDoorController, 1, meta)));
	}

	@Override
	protected void setWorldCreate(Level world) {
		this.setWorld(world);
	}
}
