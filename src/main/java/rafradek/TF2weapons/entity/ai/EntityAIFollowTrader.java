package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character.Order;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.world.entity.ai.goal.Goal;

public class EntityAIFollowTrader extends Goal {

	public EntityTF2Character owner;
	private int timeToRecalcPath;

	public EntityAIFollowTrader(EntityTF2Character entity) {
		this.owner = entity;
		this.setFlags(TF2Util.mutexFlags(1));
	}

	@Override
	public boolean canUse() {
		return this.owner.getOwner() != null && this.owner.distanceToSqr(this.owner.getOwner()) > 100
				&& this.owner.isAlive() && this.owner.getOrder() == Order.FOLLOW
				&& !(this.owner.getTarget() != null && this.owner.getOwner().distanceToSqr(this.owner) < 600);
	}

	@Override
	public void start() {
		this.timeToRecalcPath = 0;
	}

	@Override
	public void stop() {
		this.owner.getNavigation().stop();
	}

	@Override
	public boolean canContinueToUse() {
		return !this.owner.getNavigation().isDone() && this.owner.distanceToSqr(this.owner.getOwner()) > 100;
	}

	@Override
	public void tick() {

		if (this.owner.getTarget() == null || (this.owner.getTarget().distanceToSqr(this.owner) < this.owner
				.getTarget().distanceToSqr(this.owner.getOwner())))
			this.owner.getLookControl().setLookAt(this.owner.getOwner(), 10.0F, 10F);

		if (--this.timeToRecalcPath <= 0) {
			this.timeToRecalcPath = 8;

			double distance = this.owner.distanceToSqr(this.owner.getOwner());
			boolean move = this.owner.getNavigation().moveTo(this.owner.getOwner(), 1.08f);

			if ((!move && distance > 750) || (this.owner.getNavigation().isDone() && distance > 144)) {

				TF2Util.teleportSafe(owner, owner.getOwner());
			}
		}
	}

}
