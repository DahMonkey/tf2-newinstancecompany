package rafradek.TF2weapons.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityLightDynamic extends Entity {

	public int timeToLive;
	public Entity parent;

	public EntityLightDynamic(Level world) {
		super(world);
	}

	public EntityLightDynamic(Level world, Entity parent, int timeToLive) {
		super(world);
		this.setPos(parent.getX(), parent.getY() + parent.getEyeHeight(), parent.getZ());
		this.parent = parent;
		this.timeToLive = timeToLive;
	}

	public EntityLightDynamic(Level world, BlockPos pos, int timeToLive) {
		super(world);
		this.setPos(pos.getX(), pos.getY(), pos.getZ());
		this.timeToLive = timeToLive;
	}

	@Override
	protected void defineSynchedData() {}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.parent != null)
			this.setPos(this.parent.getX(), this.parent.getY() + this.parent.getEyeHeight(), this.parent.getZ());
		this.move(MoverType.SELF, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
		if (this.tickCount >= timeToLive || (this.parent != null && this.parent.isRemoved())) {
			this.discard();
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isInRangeToRender3d(double x, double y, double z) {
		return false;
	}
}
