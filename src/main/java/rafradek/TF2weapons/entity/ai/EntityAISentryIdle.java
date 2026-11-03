package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.entity.building.EntitySentry;
import net.minecraft.world.entity.ai.goal.Goal;

public class EntityAISentryIdle extends Goal {

	EntitySentry host;
	boolean direction;

	public EntityAISentryIdle(EntitySentry sentry) {
		this.host = sentry;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		return this.host.getTarget() == null && !this.host.isDisabled();
	}

	@Override
	public void updateTask() {
		// System.out.println("Rotating "+this.host.rotationYaw+"
		// "+this.host.rotationYawHead);
		if (this.direction) {
			this.host.rotationYawHead += 2.5f;
			if (this.host.yHeadRot >= this.host.getYRot() + 50)
				this.direction = false;
		} else {
			this.host.rotationYawHead -= 2.5f;
			if (this.host.yHeadRot <= this.host.getYRot() - 50)
				this.direction = true;
		}
	}

}
