package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.mercenary.EntityEngineer;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class EntityAIUseWrangler extends EntityAIUseRangedWeapon {

	public EntityAIUseWrangler(EntityTF2Character par1IRangedAttackMob, float par2, float par5) {
		super(par1IRangedAttackMob, par2, par5);
		this.setFlags(TF2Util.mutexFlags(3));
	}

	@Override
	public boolean canUse() {
		return ((EntityEngineer) this.entityHost).shouldUseWrangler() && super.canUse();
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */

	/**
	 * Resets the task
	 */

	@Override
	public void stop() {
		if (this.entityHost.usedSlot == 1) {
			this.entityHost.switchSlot(0);
			this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = 0;
			TF2Util.sendTracking(new TF2Message.ActionMessage(0, entityHost), entityHost);

		}
		if (this.jump)
			this.entityHost.jump = false;
		this.attackTarget = null;
		this.comeCloser = 0;
		this.rangedAttackTime = -1;
		this.pressed = false;

	}

	@Override
	public void tick() {
		if ((this.attackTarget != null && this.attackTarget.deathTime > 0) || this.entityHost.deathTime > 0
				|| !((EntityEngineer) this.entityHost).shouldUseWrangler()) {
			this.stop();
			return;
		}
		if (this.attackTarget == null)
			return;
		double d0 = this.entityHost.distanceToSqr(this.attackTarget.getX(), this.attackTarget.getBoundingBox().minY,
				this.attackTarget.getZ());

		double lookX = this.attackTarget.getX();
		double lookY = this.attackTarget.getY() + this.attackTarget.getEyeHeight();
		double lookZ = this.attackTarget.getZ();
		this.entityHost.switchSlot(1);
		ItemStack stack = this.entityHost.getMainHandItem();
		EntitySentry sentry = ((EntityEngineer) this.entityHost).sentry;
		/*
		 * boolean stay = this.entityHost.getEntitySenses().canSee(this.attackTarget);
		 * this.entityHost.setJumping(true); if (stay) { ++this.comeCloser; if (d0 <=
		 * (double) 12) this.comeCloser = 20; } else this.comeCloser = 0;
		 * //System.out.println(this.comeCloser+" "+this.attackRangeSquared+" "+d0); if
		 * (d0 <= 52 && this.comeCloser >= 20) { if (!this.dodging) {
		 * this.entityHost.getNavigator().clearPath(); this.dodging = true; } } else {
		 * this.dodging = false;
		 * this.entityHost.getNavigator().tryMoveToEntityLiving(this.attackTarget,
		 * this.entityMoveSpeed); }
		 */

		this.entityHost.getLookControl().setLookAt(lookX, lookY, lookZ, this.entityHost.rotation, 90.0F);
		// this.entityHost.getLookHelper().setLookPositionWithEntity(this.attackTarget,
		// 1.0F, 90.0F);
		double range = 60;
		java.util.List<net.minecraft.world.phys.HitResult> hits = TF2Util.pierce(this.entityHost.level, sentry, sentry.getX(),
				sentry.getY() + sentry.getEyeHeight(), sentry.getZ(), entityHost.getTarget().getX(),
				entityHost.getTarget().getY() + entityHost.getTarget().getEyeHeight(),
				entityHost.getTarget().getZ(), false, 0, false);
		Entity entityhit = hits.isEmpty() ? null : rafradek.TF2weapons.util.TF2HitResult.getEntity(hits.get(0));
		boolean overlap = entityhit == this.entityHost;

		if (overlap) {
			Vec3 movevec = Vec3.directionFromRotation(0, sentry.yHeadRot + 90);
			this.entityHost.getNavigation().moveTo(this.entityHost.getX() + movevec.x,
					this.entityHost.getY() + movevec.y, this.entityHost.getZ() + movevec.z, 1f);
		}

		if (d0 <= range * range && !overlap) {

			if (!pressed) {
				pressed = true;
				this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = 3;
				TF2Util.sendTracking(new TF2Message.ActionMessage(3, entityHost), entityHost);
				// System.out.println("co�do");
			}

		} else {
			if (pressed) {
				if (this.jump)
					this.entityHost.jump = false;
				this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = 0;
				TF2Util.sendTracking(new TF2Message.ActionMessage(0, entityHost), entityHost);

				// System.out.println("co�z");
			}
			pressed = false;
		}
		// if(){
		if (this.jump && d0 < this.jumprange)
			this.entityHost.jump = true;
		else if (this.jump)
			this.entityHost.jump = false;

		// }
	}

}
