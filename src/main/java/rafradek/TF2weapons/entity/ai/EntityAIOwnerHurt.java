package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.entity.mercenary.EntityMedic;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

public class EntityAIOwnerHurt extends TargetGoal {

	public EntityAIOwnerHurt(PathfinderMob creature) {
		super(creature, false, false);
	}

	@Override
	public boolean shouldExecute() {
		Player owner=(Player) ((EntityTF2Character) this.taskOwner).getOwner();
		return owner != null && (owner.getLastHurtByMob() != null || owner.getLastHurtMob() != null);
	}

	public void startExecuting()
    {
		Player owner=(Player) ((EntityTF2Character) this.taskOwner).getOwner();
		if(owner.getLastHurtByMob() != null && this.isSuitableTarget(owner.getLastHurtByMob(), false))
			this.taskOwner.setTarget(owner.getLastHurtByMob());
		else if(owner.getLastHurtMob() != null && this.isSuitableTarget(owner.getLastHurtMob(), false))
			this.taskOwner.setTarget(owner.getLastHurtMob());
    }
	
	@Override
	protected boolean isSuitableTarget(LivingEntity target, boolean includeInvincibles) {
		if (target == null)
			return false;
		else if (target == this.taskOwner)
			return false;
		else if (!target.isAlive())
			return false;
		else if (!this.taskOwner.canAttackClass(target.getClass()))
			return false;
		else {
			Team team = this.taskOwner.getTeam();
			Team team1 = target.getTeam();

			if ((team != null && team1 == team) && !(this.taskOwner instanceof EntityMedic))
				return false;
			else {
				if (this.taskOwner instanceof OwnableEntity
						&& ((OwnableEntity) this.taskOwner).getOwnerId() != null) {
					if (target instanceof OwnableEntity && ((OwnableEntity) this.taskOwner).getOwnerId()
							.equals(((OwnableEntity) target).getOwnerId()))
						return false;

					if (target == ((OwnableEntity) this.taskOwner).getOwner())
						return false;
				} else if (target instanceof Player && !includeInvincibles
						&& ((Player) target).getAbilities().disableDamage)
					return false;

				return !this.shouldCheckSight || this.taskOwner.getEntitySenses().canSee(target);
			}
		}
	}
}
