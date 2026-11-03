package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.util.TF2Util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class EntityAIMoveTowardsRestriction2 extends Goal {

	private final PathfinderMob creature;
	private double movePosX;
	private double movePosY;
	private double movePosZ;
	private final double movementSpeed;

	public EntityAIMoveTowardsRestriction2(PathfinderMob creatureIn, double speedIn) {
		this.creature = creatureIn;
		this.movementSpeed = speedIn;
		this.setFlags(TF2Util.mutexFlags(1));
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean canUse() {

		if (this.creature.isWithinRestriction()) {
			return false;
		} else {

			BlockPos blockpos = this.creature.getRestrictCenter();

			Vec3 vec3d = this.creature.getRestrictRadius() == 0 ? Vec3.atBottomCenterOf(blockpos)
					: DefaultRandomPos.getPosTowards(this.creature,
							(int) Math.min(this.creature.getRestrictRadius(), 16), 7,
							new Vec3(blockpos.getX(), blockpos.getY(), blockpos.getZ()), Math.PI / 2);

			if (vec3d == null) {

				return false;
			} else {
				this.movePosX = vec3d.x;
				this.movePosY = vec3d.y;
				this.movePosZ = vec3d.z;
				return true;
			}
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean canContinueToUse() {
		return !this.creature.getNavigation().isDone();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void start() {
		this.creature.getNavigation().moveTo(this.movePosX, this.movePosY, this.movePosZ, this.movementSpeed);
	}

}
