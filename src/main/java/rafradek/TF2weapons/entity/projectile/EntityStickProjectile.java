package rafradek.TF2weapons.entity.projectile;

import net.minecraft.world.phys.BlockHitResult;
import rafradek.TF2weapons.TF2weapons;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityStickProjectile extends EntityProjectileSimple {

	public boolean sticked;

	public boolean clientSticked;

	public EntityStickProjectile(Level world) {
		this(TF2weapons.SYRINGE, world);
	}

	public EntityStickProjectile(EntityType<? extends EntityStickProjectile> type, Level world) {
		super(type, world);
		this.setSize(0.3F, 0.3F);

	}

	@Override
	public void onHitGround(int x, int y, int z, HitResult mop) {
		super.onHitGround(x, y, z, mop);
		if (!this.level.isEmptyBlock(mop.getBlockPos())) {
			this.setPos(mop.getLocation().x + ((BlockHitResult) mop).getDirection().getStepX() * 0.1,
					mop.getLocation().y + ((BlockHitResult) mop).getDirection().getStepY() * 0.1f,
					mop.getLocation().z + ((BlockHitResult) mop).getDirection().getStepZ() * 0.1);
			this.sticked = true;
			this.stickedBlock = ((BlockHitResult) mop).getBlockPos();
		}
	}

	@Override
	public void onUpdate() {
		if (this.tickCount > this.getMaxTime()
				|| (this.level.isClientSide && this.sticked && this.level.isEmptyBlock(stickedBlock))) {
			this.discard();
			return;
		} else if (!this.sticked)
			super.onUpdate();
	}

	@Override
	public void discard() {
		if (this.level.isClientSide && this.sticked && this.getThrower() != null && !this.clientSticked) {
			EntityStickProjectile ent = new EntityStickProjectile(this.level);
			ent.absMoveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
			ent.sticked = true;
			ent.setType(this.getProjType());
			ent.setThrower(this.getThrower());
			ent.clientSticked = true;
			ent.stickedBlock = this.stickedBlock;
			this.level.addFreshEntity(ent);
		}
		super.discard();
	}

	@Override
	public void spawnParticles(double x, double y, double z) {}

	@Override
	public boolean moveable() {
		return !this.sticked;
	}
}
