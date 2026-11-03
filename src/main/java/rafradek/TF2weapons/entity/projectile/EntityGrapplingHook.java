package rafradek.TF2weapons.entity.projectile;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2weapons;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityGrapplingHook extends EntityProjectileSimple {

	public boolean sticked;

	public Entity stickedEntity;

	public double relativeX;
	public double relativeY;
	public double relativeZ;

	public EntityGrapplingHook(Level world) {
		this(TF2weapons.GRAPPLING_HOOK, world);
	}

	public EntityGrapplingHook(EntityType<? extends EntityGrapplingHook> type, Level world) {
		super(type, world);
	}

	@Override
	public void onHitGround(int x, int y, int z, HitResult mop) {
		if (!sticked && !this.level.isEmptyBlock(mop.getBlockPos())) {
			this.setPos(mop.getLocation().x + ((BlockHitResult) mop).getDirection().getStepX() * 0.1,
					mop.getLocation().y + ((BlockHitResult) mop).getDirection().getStepY() * 0.1f,
					mop.getLocation().z + ((BlockHitResult) mop).getDirection().getStepZ() * 0.1);
			this.sticked = true;
			this.stickedBlock = ((BlockHitResult) mop).getBlockPos();
		}
	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		if (!sticked) {
			super.onHitMob(entityHit, mop);
			this.setPos(mop.getLocation().x + ((BlockHitResult) mop).getDirection().getStepX() * 0.1,
					mop.getLocation().y + ((BlockHitResult) mop).getDirection().getStepY() * 0.1f,
					mop.getLocation().z + ((BlockHitResult) mop).getDirection().getStepZ() * 0.1);
			this.relativeX = this.getX() - entityHit.getX();
			this.relativeY = this.getY() - entityHit.getY();
			this.relativeZ = this.getZ() - entityHit.getZ();
			this.sticked = true;
			this.stickedEntity = entityHit;
		}
	}

	@Override
	public void onUpdate() {
		if (this.tickCount > this.getMaxTime() || (!this.level.isClientSide && this.sticked
				&& (stickedBlock == null || this.level.isEmptyBlock(stickedBlock))
				&& (this.stickedEntity == null || !this.stickedEntity.isAlive()))) {
			this.discard();
			return;
		} else if (this.stickedEntity != null) {
			if (!this.level.isClientSide && this.stickedEntity instanceof LivingEntity && this.tickCount % 10 == 0) {
				((LivingEntity) this.stickedEntity).addEffect(new MobEffectInstance(TF2weapons.bleeding, 10));
			}
			this.setPos(relativeX + this.stickedEntity.getX(), relativeY + this.stickedEntity.getY(),
					relativeZ + this.stickedEntity.getZ());
		} else if (!this.sticked)
			super.onUpdate();
	}

	@Override
	public boolean canPenetrate() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public double getGravity() {
		return 0;
	}

}
