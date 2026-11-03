package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.mercenary.EntitySniper;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemMedigun;
import rafradek.TF2weapons.item.ItemUsable;
import rafradek.TF2weapons.item.ItemWeapon;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class EntityAIMoveAttack extends Goal {

	/** The entity the AI instance has been applied to */
	protected final EntityTF2Character entityHost;

	/**
	 * The entity (as a RangedAttackMob) the AI instance has been applied to.
	 */
	protected LivingEntity attackTarget;

	/**
	 * A decrementing tick that spawns a ranged attack once this value reaches 0. It
	 * is then set back to the maxRangedAttackTime.
	 */
	protected int rangedAttackTime;
	protected float entityMoveSpeed;
	protected int comeCloser;

	/**
	 * The maximum time the AI has to wait before peforming another ranged attack.
	 */
	private float attackRange;
	protected float attackRangeSquared;

	private float attackRangeMin;
	protected float attackRangeMinSquared;

	protected boolean inRange;
	protected boolean dodge;
	public boolean jump;
	public float dodgeSpeed = 1f;
	public int jumprange;
	public float projSpeed;
	public double fireAtFeet;

	public float gravity;

	public boolean explosive;

	public boolean dodgeHeadFor;

	private float attackRangeSSquared;
	public boolean backpath;

	public EntityAIMoveAttack(EntityTF2Character par1IRangedAttackMob, float par2, float par5) {
		this.rangedAttackTime = -1;
		this.comeCloser = 0;
		this.entityHost = par1IRangedAttackMob;
		this.entityMoveSpeed = par2;
		this.attackRange = par5;
		this.attackRangeSquared = par5 * par5;
		this.attackRangeMin = 3;
		this.attackRangeMinSquared = 9;

		this.setMutexBits(1);
	}

	public void setRange(float range, float minrange) {
		this.attackRange = range;
		this.attackRangeSquared = range * range;
		this.attackRangeSSquared = (range + 5) * (range + 5);
		this.attackRangeMin = minrange;
		this.attackRangeMinSquared = minrange * minrange;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		LivingEntity LivingEntity = this.entityHost.getTarget();

		if (LivingEntity == null)
			return false;
		else {
			this.attackTarget = LivingEntity;
			return !this.entityHost.getMainHandItem().isEmpty()
					&& (this.entityHost.getMainHandItem().getItem() instanceof ItemWeapon
							|| this.entityHost.getMainHandItem().getItem() instanceof ItemMedigun);
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean shouldContinueExecuting() {
		return this.shouldExecute() || !this.entityHost.getNavigation().noPath();
	}

	/**
	 * Resets the task
	 */
	@Override
	public void resetTask() {
		if (this.jump)
			this.entityHost.jump = false;
		this.entityHost.getNavigation().clearPath();
		this.attackTarget = null;
		this.comeCloser = 0;
		this.rangedAttackTime = -1;
		this.backpath = false;
	}

	/**
	 * Updates the task
	 */

	@Override
	public void updateTask() {
		if ((this.attackTarget != null && this.attackTarget.deathTime > 0) || this.entityHost.deathTime > 0) {
			this.resetTask();
			return;
		}
		if (this.attackTarget == null)
			return;
		ItemStack item = this.entityHost.getItemInHand(InteractionHand.MAIN_HAND);

		if (!(item.getItem() instanceof ItemUsable))
			return;

		double d0 = this.entityHost.distanceToSqr(this.attackTarget.getX(), this.attackTarget.getBoundingBox().minY,
				this.attackTarget.getZ());

		boolean stay = this.entityHost.getEntitySenses().canSee(this.attackTarget)
				|| (this.projSpeed > 0 && this.attackTarget.getDeltaMovement().y > 0);
		// this.entityHost.setJumping(true);

		float range = this.attackTarget instanceof EntityBuilding ? this.attackRangeSSquared : this.attackRangeSquared;
		if (stay) {
			++this.comeCloser;
			if (d0 <= this.attackRangeMinSquared + 2
					|| d0 <= (double) this.attackRangeSquared / (this.entityHost instanceof EntitySniper ? 2f : 4f))
				this.comeCloser = 20;
		} else
			this.comeCloser = 0;

		if ((d0 <= range && this.comeCloser >= 20) || this.entityHost.getWepCapability().isExpJump()) {
			if (!this.inRange) {
				this.entityHost.getNavigation().clearPath();
				this.inRange = true;
				this.backpath = false;
			}
		} else {
			this.inRange = false;
			/*
			 * if(this.entityHost.onGround&&this.entityHost instanceof
			 * EntitySoldier&&this.entityHost.getHeldItem(EnumHand.MAIN_HAND).
			 * getItemDamage()<this.entityHost.getHeldItem(EnumHand.MAIN_HAND).
			 * getMaxDamage()-1){ ((EntitySoldier)this.entityHost).rocketJump=true; }
			 */
			this.entityHost.getNavigation().tryMoveToEntityLiving(this.attackTarget, this.entityMoveSpeed);
			this.backpath = false;
		}
		// this.entityHost.getLookHelper().setLookPosition(lookX, lookY, lookZ,
		// this.entityHost.rotation, 90.0F);
		// this.entityHost.getLookHelper().onUpdateLook();
		// if(!(this.entityHost instanceof
		// EntitySoldier&&((EntitySoldier)this.entityHost).rocketJump))

		// this.entityHost.getLookHelper().setLookPositionWithEntity(this.attackTarget,
		// 1.0F, 90.0F);
		// if(){
		if (this.jump && d0 < this.jumprange)
			this.entityHost.jump = true;
		else if (this.jump)
			this.entityHost.jump = false;
		boolean nopath = (this.entityHost.getNavigation().noPath() || (this.entityHost.tickCount % 20) == 0);
		if (d0 < this.attackRangeMinSquared && (nopath || !backpath)) {
			for (int i = 0; i < 10; i++) {
				Vec3 Vec3 = DefaultRandomPos.findRandomTarget(this.entityHost, 0, 0);

				if (Vec3 != null) {
					Vec3 off = this.entityHost.position().subtract(this.attackTarget.position())
							.normalize();
					double offsetX = off.x * 3;
					double offsetY = 0;
					double offsetZ = off.z * 3;
					this.entityHost.getNavigation().tryMoveToXYZ(Vec3.x + offsetX, Vec3.y + offsetY, Vec3.z + offsetZ,
							this.entityMoveSpeed * this.dodgeSpeed);
					backpath = true;
					break;
				}
			}
		} else if (this.dodge && nopath) {
			Vec3 Vec3 = DefaultRandomPos.findRandomTarget(this.entityHost, 4, 2);

			if (Vec3 != null) {
				backpath = false;
				double offsetX = this.dodgeHeadFor ? this.attackTarget.getX() - this.entityHost.getX() : 0;
				double offsetY = this.dodgeHeadFor ? this.attackTarget.getY() - this.entityHost.getY() : 0;
				double offsetZ = this.dodgeHeadFor ? this.attackTarget.getZ() - this.entityHost.getZ() : 0;
				this.entityHost.getNavigation().tryMoveToXYZ(Vec3.x + offsetX, Vec3.y + offsetY, Vec3.z + offsetZ,
						this.entityMoveSpeed * this.dodgeSpeed);
			}
		}
		// }
	}

	public void setDodge(boolean i, boolean headFor) {
		this.dodge = i;
		this.dodgeHeadFor = headFor;
	}

}
