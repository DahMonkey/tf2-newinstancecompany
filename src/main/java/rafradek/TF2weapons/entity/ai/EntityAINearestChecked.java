package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityMedic;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemDisguiseKit;
import rafradek.TF2weapons.util.TF2Util;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import java.util.function.Predicate;

public class EntityAINearestChecked<T extends LivingEntity> extends TargetGoal {
	public int targetChoosen = 0;
	private Class<T> targetClass;
	private java.util.Comparator<net.minecraft.world.entity.Entity> theNearestAttackableTargetSorter;
	private Predicate<T> targetEntitySelector;
	public LivingEntity targetEntity;
	private boolean targetLock;
	private int targetUnseenTicks;

	public EntityAINearestChecked(PathfinderMob p_i1665_1_, Class<T> p_i1665_2_, boolean p_i1665_4_,
			boolean p_i1665_5_, final Predicate<T> p_i1665_6_, boolean targetLock, boolean allowBehind) {
		super(p_i1665_1_, p_i1665_4_, p_i1665_5_);
		this.targetClass = p_i1665_2_;
		this.theNearestAttackableTargetSorter = java.util.Comparator.comparingDouble(e -> p_i1665_1_.distanceToSqr(e));
		this.setFlags(TF2Util.mutexFlags(1));
		this.targetLock = targetLock;
		this.targetEntitySelector = target -> {
			if (p_i1665_6_ != null && !p_i1665_6_.test(target))
				return false;
			else {
				// System.out.println("found "+target.getClass().getName()+"
				// "+EntityAINearestChecked.this.taskOwner.getClass().getName());
				if (target instanceof LivingEntity) {
					double d0 = EntityAINearestChecked.this.getFollowDistance();

					if (target.isShiftKeyDown())
						d0 *= 0.800000011920929D;

					if (target instanceof Player && target.isInvisible()) {
						float f = target.getArmorCoverPercentage();

						if (f < 0.1F)
							f = 0.1F;

						d0 *= 0.7F * f;
					}
					if (target.getCapability(TF2weapons.WEAPONS_CAP).isPresent()
							&& (target.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks >= 20
									|| ItemDisguiseKit.isDisguised(target, mob)))
						d0 = 1;
					boolean fastCheck = allowBehind || (!(target instanceof Player)
							&& (TF2ConfigVars.naturalCheck.equals("Fast") && mob instanceof EntityTF2Character
									&& ((EntityTF2Character) mob).natural));
					if (target.distanceTo(mob) > d0 || (!fastCheck && !TF2Util.lookingAtFast(mob, 86,
							target.getX(), target.getY() + target.getEyeHeight(), target.getZ())))
						return false;

				}

				return EntityAINearestChecked.this.isSuitableTarget(target, false);
			}
		};
	}

	@Override
	public boolean canUse() {
		double d0 = this.getFollowDistance() / 2;
		if (((this.mob.getTarget() == null)
				|| this.mob.getTarget().distanceToSqr(mob) > d0 * d0))
			this.targetChoosen++;
		if (((this.mob.getTarget() == null) && this.targetChoosen > 1) || this.targetChoosen > 5
				|| !this.targetLock) {
			// System.out.println("executing
			// "+this.taskOwner.getClass().getName());
			this.targetChoosen = 0;
			double d1 = this.getFollowDistance();
			List<? extends LivingEntity> list = this.mob.level.getEntitiesOfClass(this.targetClass,
					this.mob.getBoundingBox().inflate(d1, d0, d1), this.targetEntitySelector);
			Collections.sort(list, this.theNearestAttackableTargetSorter);

			if (list.isEmpty())
				// System.out.println("emptylist
				// "+this.taskOwner.getClass().getName());
				return false;
			else {
				this.targetEntity = list.get(0);
				return true;
			}
		}
		return false;
	}

	@Override
	public void start() {
		this.mob.setTarget(this.targetEntity);
		if (this.mob instanceof EntityTF2Character && this.mob.getTarget() != null) {
			EntityTF2Character shooter = ((EntityTF2Character) this.mob);
			shooter.targetPrevPos[0] = shooter.getTarget().getX();
			shooter.targetPrevPos[2] = shooter.getTarget().getY();
			shooter.targetPrevPos[4] = shooter.getTarget().getZ();
		}
		super.start();
	}

	/**
	 * A method used to see if an entity is a suitable target through a number of
	 * checks. Args : entity, canTargetInvinciblePlayer
	 */
	@Override
	public boolean canContinueToUse() {
		LivingEntity entitylivingbase = this.mob.getTarget();

		if (entitylivingbase == null)
			return false;
		else if (!entitylivingbase.isAlive())
			return false;
		else if (!this.targetLock && this.mob.tickCount % 13 == 0)
			return this.canUse();
		else {
			Team team = this.mob.getTeam();
			Team team1 = entitylivingbase.getTeam();

			if ((team != null && team1 == team) && !(this.mob instanceof EntityMedic))
				return false;
			else {
				double d0 = this.getFollowDistance();

				if (this.mob.distanceToSqr(entitylivingbase) > d0 * d0)
					return false;
				else {
					if (this.mustSee)
						if (this.mob.getSensing().hasLineOfSight(entitylivingbase))
							this.targetUnseenTicks = 0;
						else if (++this.targetUnseenTicks > 60)
							return false;

					return !(entitylivingbase instanceof Player)
							|| !((Player) entitylivingbase).getAbilities().invulnerable;
				}
			}
		}
	}

		protected boolean isSuitableTarget(LivingEntity target, boolean includeInvincibles) {
		if (target == null)
			return false;
		else if (target == this.mob)
			return false;
		else if (!target.isAlive())
			return false;
		else if (!this.mob.canAttackType(target.getType()))
			return false;
		else {
			Team team = this.mob.getTeam();
			Team team1 = target.getTeam();
			boolean medic = (this.mob instanceof EntityMedic);
			if ((team != null && team1 == team) && !medic)
				return false;
			else {
				if (!medic && this.mob instanceof OwnableEntity
						&& ((OwnableEntity) this.mob).getOwnerUUID() != null) {
					if (target instanceof OwnableEntity && ((OwnableEntity) this.mob).getOwnerUUID()
							.equals(((OwnableEntity) target).getOwnerUUID()))
						return false;

					if (target == ((OwnableEntity) this.mob).getOwner())
						return false;
				}
				if (target instanceof Player && !includeInvincibles
						&& ((Player) target).getAbilities().invulnerable)
					return false;

				return !this.mustSee || this.mob.getSensing().hasLineOfSight(target);
			}
		}
	}
}
