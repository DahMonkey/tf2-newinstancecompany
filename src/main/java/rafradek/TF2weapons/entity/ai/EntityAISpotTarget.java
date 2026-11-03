package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.item.ItemDisguiseKit;
import rafradek.TF2weapons.util.TF2Util;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import java.util.function.Predicate;

public class EntityAISpotTarget<T extends LivingEntity> extends Goal {

	/** The entity that this task belongs to */
	protected final EntityBuilding mob;
	/**
	 * If true, EntityAI targets must be able to be seen (cannot be blocked by
	 * walls) to be suitable targets.
	 */
	protected boolean shouldCheckSight;
	/**
	 * When true, only entities that can be reached with minimal effort will be
	 * targetted.
	 */
	private final boolean nearbyOnly;
	/**
	 * When nearbyOnly is true: 0 -> No target, but OK to search; 1 -> Nearby target
	 * found; 2 -> Target too far.
	 */
	private int targetSearchStatus;
	/**
	 * When nearbyOnly is true, this throttles target searching to avoid excessive
	 * pathfinding.
	 */
	private int targetSearchDelay;
	/**
	 * If @shouldCheckSight is true, the number of ticks before the interuption of
	 * this AITastk when the entity does't see the target
	 */
	protected T target;
	protected int unseenMemoryTicks;

	public int targetChoosen = 0;
	private Class<T> targetClass;
	private Sorter theNearestAttackableTargetSorter;
	private Predicate<T> targetEntitySelector;
	public T targetEntity;
	private boolean targetLock;
	private int targetUnseenTicks;

	public EntityAISpotTarget(EntityBuilding p_i1665_1_, Class<T> p_i1665_2_, boolean p_i1665_4_, boolean p_i1665_5_,
			final Predicate<T> p_i1665_6_, boolean targetLock, boolean allowBehind) {
		super();
		this.nearbyOnly = p_i1665_4_;
		this.shouldCheckSight = p_i1665_5_;
		this.mob = p_i1665_1_;
		this.targetClass = p_i1665_2_;
		this.theNearestAttackableTargetSorter = new NearestAttackableTargetGoal.Sorter(p_i1665_1_);
		this.setFlags(TF2Util.mutexFlags(1));
		this.targetLock = targetLock;
		this.targetEntitySelector = target -> {
			if (p_i1665_6_ != null && !p_i1665_6_.apply(target))
				return false;
			else {
				// System.out.println("found "+target.getClass().getName()+"
				// "+EntityAINearestChecked.this.taskOwner.getClass().getName());
				if (target instanceof LivingEntity) {
					double d0 = EntityAISpotTarget.this.getTargetDistance();

					if (target.isShiftKeyDown())
						d0 *= 0.800000011920929D;

					if (target instanceof Player && target.isInvisible()) {
						float f = ((Player) target).getArmorVisibility();

						if (f < 0.1F)
							f = 0.1F;

						d0 *= 0.7F * f;
					}
					if (target.getCapability(TF2weapons.WEAPONS_CAP).isPresent()
							&& (target.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks >= 20
									|| ItemDisguiseKit.isDisguised(target, mob)))
						d0 = 0;
					boolean fastCheck = allowBehind;
					if (target.distanceTo(mob) > d0 || (!fastCheck && !TF2Util.lookingAtFast(mob, 86,
							target.getX(), target.getY() + target.getEyeHeight(), target.getZ())))
						return false;

				}

				return EntityAISpotTarget.this.isSuitableTarget(target, false);
			}
		};
	}

	protected double getTargetDistance() {
		AttributeInstance iattributeinstance = this.mob.getAttribute(Attributes.FOLLOW_RANGE);
		return iattributeinstance == null ? 16.0D : iattributeinstance.getValue();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */

	/**
	 * Reset the task's internal state. Called when this task is interrupted by
	 * another one
	 */
	@Override
	public void stop() {
		this.mob.setTarget((LivingEntity) null);
		this.target = null;
	}

	@Override
	public boolean canUse() {
		double d0 = this.getTargetDistance() / 2;
		if (((this.mob.getTarget() == null)
				|| this.mob.getTarget().distanceToSqr(mob) > d0 * d0))
			this.targetChoosen++;
		if (((this.mob.getTarget() == null) && this.targetChoosen > 1) || this.targetChoosen > 5
				|| !this.targetLock) {
			// System.out.println("executing
			// "+this.taskOwner.getClass().getName());
			this.targetChoosen = 0;
			double d1 = this.getTargetDistance();
			List<T> list = this.mob.level.getEntitiesOfClass(this.targetClass,
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
		super.start();
		this.targetSearchStatus = 0;
		this.targetSearchDelay = 0;
		this.targetUnseenTicks = 0;
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

			if (team != null && team1 == team)
				return false;
			else {
				double d0 = this.getTargetDistance();

				if (this.mob.distanceToSqr(entitylivingbase) > d0 * d0)
					return false;
				else {
					if (this.shouldCheckSight)
						if (this.mob.getSensing().hasLineOfSight(entitylivingbase))
							this.targetUnseenTicks = 0;
						else if (++this.targetUnseenTicks > 60)
							return false;

					return !(entitylivingbase instanceof Player)
							|| !((Player) entitylivingbase).getAbilities().disableDamage;
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
		else if (!this.mob.canAttackClass(target.getClass()))
			return false;
		else {
			Team team = this.mob.getTeam();
			Team team1 = target.getTeam();
			if ((team != null && team1 == team))
				return false;
			else {
				if (this.mob instanceof OwnableEntity
						&& ((OwnableEntity) this.mob).getOwnerUUID() != null) {
					if (target instanceof OwnableEntity && ((OwnableEntity) this.mob).getOwnerUUID()
							.equals(((OwnableEntity) target).getOwnerUUID()))
						return false;

					if (target == ((OwnableEntity) this.mob).getOwner())
						return false;
				}
				if (target instanceof Player && !includeInvincibles
						&& ((Player) target).getAbilities().disableDamage)
					return false;

				return !this.shouldCheckSight || this.mob.getSensing().hasLineOfSight(target);
			}
		}
	}
}
