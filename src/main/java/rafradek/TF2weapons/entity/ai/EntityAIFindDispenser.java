package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

public class EntityAIFindDispenser extends Goal {

	public EntityTF2Character host;

	public float range;
	public float rangeSq;

	public Entity target;
	public Path path;

	public int delay;

	public EntityAIFindDispenser(EntityTF2Character host, float range) {
		this.host = host;
		this.range = range;
		this.rangeSq = range * range;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		if (this.host.getAmmo() <= 0 || (this.host.getTarget() == null
				&& (this.host.getHealth() < this.host.getMaxHealth() || !this.host.isAmmoFull()))) {
			Entity disp = TF2Util.findAmmoSource(host, range, true);
			if (disp != null) {
				this.target = disp;
				this.path = host.getNavigation().getPathToEntityLiving(disp);
				return this.path != null;
			}
		}
		return false;
	}

	@Override
	public void startExecuting() {
		this.host.getNavigation().setPath(path, 1.0f);
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.target != null && target.isAlive()
				&& !(this.host.isAmmoFull() && this.host.getHealth() >= this.host.getMaxHealth());
	}

	@Override
	public void updateTask() {
		if (this.host.distanceToSqr(target) > 2) {
			if (this.host.tickCount % 9 == 0)
				if (!this.host.getNavigation().tryMoveToEntityLiving(host, 1.0f)) {
					this.target = null;
					return;
				}
			if (this.host.getTarget() == null)
				this.host.getLookControl().setLookPositionWithEntity(this.target, 30, 15);
		} else
			this.host.getNavigation().clearPath();
	}
}
