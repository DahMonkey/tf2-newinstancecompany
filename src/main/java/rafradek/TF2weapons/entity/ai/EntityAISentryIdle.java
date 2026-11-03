package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.util.TF2Util;

import rafradek.TF2weapons.entity.building.EntitySentry;
import net.minecraft.world.entity.ai.goal.Goal;

public class EntityAISentryIdle extends Goal {

	EntitySentry host;
	boolean direction;

	public EntityAISentryIdle(EntitySentry sentry) {
		this.host = sentry;
		this.setFlags(TF2Util.mutexFlags(1));
	}

	@Override
	public boolean canUse() {
		return this.host.getTarget() == null && !this.host.isDisabled();
	}

	@Override
	public void tick() {
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
