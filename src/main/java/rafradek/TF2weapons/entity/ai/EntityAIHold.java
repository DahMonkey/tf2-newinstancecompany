package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character.Order;
import net.minecraft.world.entity.ai.goal.Goal;

public class EntityAIHold extends Goal {

	public EntityTF2Character owner;
	private int timeToRecalcPath;

	public EntityAIHold(EntityTF2Character entity) {
		this.owner = entity;
		this.setFlags(TF2Util.mutexFlags(1));
	}

	@Override
	public boolean shouldExecute() {
		return this.owner.getOwner() != null && this.owner.distanceToSqr(this.owner.getOwner()) > 100
				&& this.owner.isAlive() && this.owner.getOrder() == Order.FOLLOW
				&& !(this.owner.getTarget() != null && this.owner.getOwner().distanceToSqr(this.owner) < 600);
	}

	@Override
	public void startExecuting() {
		this.timeToRecalcPath = 0;
	}

	@Override
	public void resetTask() {
		this.owner.getNavigation().stop();
	}

	@Override
	public boolean shouldContinueExecuting() {
		return !this.owner.getNavigation().isDone() && this.owner.distanceToSqr(this.owner.getOwner()) > 100;
	}

	@Override
	public void updateTask() {

		if (this.owner.getTarget() == null || (this.owner.getTarget().distanceToSqr(this.owner) < this.owner
				.getTarget().distanceToSqr(this.owner.getOwner())))
			this.owner.getLookControl().setLookPositionWithEntity(this.owner.getOwner(), 10.0F, 10F);

		if (--this.timeToRecalcPath <= 0) {
			this.timeToRecalcPath = 8;

			this.owner.getNavigation().tryMoveToEntityLiving(this.owner.getOwner(), 1.2f);
		}
	}
}
