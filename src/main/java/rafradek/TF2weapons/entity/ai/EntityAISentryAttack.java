package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class EntityAISentryAttack extends Goal {

	public EntitySentry host;
	private boolean lockTarget;

	public EntityAISentryAttack(EntitySentry sentry) {
		this.host = sentry;
		this.setFlags(TF2Util.mutexFlags(1);
	}

	@Override
	public void resetTask() {
		this.host.setSoundState(0);
	}

	@Override
	public boolean shouldExecute() {
		return !this.host.isDisabled() && !this.host.isControlled() && (this.host.getTarget()) != null
				&& this.host.getSensing().canSee(this.host.getTarget());
	}

	@Override
	public void updateTask() {

		LivingEntity target = this.host.getTarget();
		if (target == null)
			return;

		if ((target != null && target.deathTime > 0) || this.host.deathTime > 0) {
			this.resetTask();
			return;
		}
		LivingEntity owner = this.host.getOwner();
		if (owner == null || owner.isRemoved())
			owner = this.host;
		double lookX = target.getX();
		double lookY = target.getY() + target.getBbHeight() / 2;
		double lookZ = target.getZ();
		if (this.lockTarget)
			this.host.getLookControl().setLookPosition(lookX, lookY, lookZ, 30, 75);
		else
			this.host.getLookControl().setLookPosition(lookX, lookY, lookZ,
					(5f + this.host.getLevel() * 2.25f) * (this.host.isMini() ? 1.35f : 1f), 50);
		if (TF2Util.lookingAt(this.host, 24, lookX, lookY, lookZ)) {
			this.lockTarget = true;
			this.host.shootBullet(owner);
			this.host.shootRocket(owner);
		} else {
			this.lockTarget = false;
			if (this.host.getSoundState() > 2)
				this.host.setSoundState(1);
		}
	}
}
