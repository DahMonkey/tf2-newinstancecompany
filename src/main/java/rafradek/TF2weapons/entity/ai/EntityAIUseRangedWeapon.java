package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemMedigun;
import rafradek.TF2weapons.item.ItemUsable;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityAIUseRangedWeapon extends Goal {

	protected final EntityTF2Character entityHost;

	protected LivingEntity attackTarget;
	public boolean reloading;

	protected int rangedAttackTime;
	protected float entityMoveSpeed;
	protected int comeCloser;

	private float attackRange;
	protected float attackRangeSquared;
	protected float attackRangeSSquared;

	protected boolean pressed;
	protected boolean dodging;
	public boolean jump;
	public float dodgeSpeed = 1f;
	public int jumprange;
	public float projSpeed;
	public double fireAtFeet;
	public double lastMotionY;

	public Vec3 velTarget;
	public Vec3 posTarget;
	public int ticksUpdatePos;

	public float gravity;

	public boolean explosive;

	public float releaseAt;

	public EntityAIUseRangedWeapon(EntityTF2Character par1IRangedAttackMob, float par2, float par5) {
		this.rangedAttackTime = -1;
		this.comeCloser = 0;
		this.entityHost = par1IRangedAttackMob;
		this.entityMoveSpeed = par2;
		this.attackRange = par5;
		this.attackRangeSquared = par5 * par5;

		this.setFlags(TF2Util.mutexFlags(2));
	}

	public void setRange(float range) {
		this.attackRange = range;
		this.attackRangeSquared = range * range;
		this.attackRangeSSquared = (range + 5) * (range + 5);
	}

	public float getRange() {
		return this.attackRange;
	}

	public float getRangeSq() {
		return this.attackRangeSquared;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean canUse() {
		LivingEntity LivingEntity = this.entityHost.getTarget();

		if (LivingEntity == null)
			return false;
		else {
			if (this.attackTarget != LivingEntity)
				this.lastMotionY = LivingEntity.getDeltaMovement().y;
			this.attackTarget = LivingEntity;
			return !this.entityHost.getMainHandItem().isEmpty() && this.entityHost.eating <= 0
					&& (this.entityHost.getMainHandItem().getItem() instanceof ItemUsable);
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean canContinueToUse() {
		return this.canUse();
	}

	private int getPosUpdateTime() {
		return Math.max(1, Math.round(this.entityHost.scaleWithDifficulty(18, 1) * TF2ConfigVars.accurracyMult));
	}

	/**
	 * Resets the task
	 */
	@Override
	public void stop() {
		if ((this.entityHost.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemWeapon)
				&& this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state != 0) {
			pressed = false;
			((ItemWeapon) this.entityHost.getItemInHand(InteractionHand.MAIN_HAND).getItem()).endUse(
					this.entityHost.getItemInHand(InteractionHand.MAIN_HAND), this.entityHost, this.entityHost.level,
					this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state, 0);
			this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = 0;
			TF2Util.sendTracking(new TF2Message.ActionMessage(0, entityHost), entityHost);
		}
		if (this.jump)
			this.entityHost.jump = false;
		this.attackTarget = null;
		this.comeCloser = 0;
		this.rangedAttackTime = -1;
		this.posTarget = null;
		this.velTarget = null;
	}

	@Override
	public void tick() {
		if ((this.attackTarget != null && this.attackTarget.deathTime > 0) || this.entityHost.deathTime > 0) {
			this.stop();
			return;
		}
		if (this.attackTarget == null)
			return;

		int updateTime = this.getPosUpdateTime();
		if (--this.ticksUpdatePos <= 0 || this.posTarget == null) {

			if (this.posTarget != null)
				this.velTarget = this.attackTarget.position().subtract(this.posTarget).scale(1D / updateTime);
			else
				this.velTarget = Vec3.ZERO;
			this.posTarget = this.attackTarget.position();
			this.ticksUpdatePos = updateTime;
		}

		ItemStack item = this.entityHost.getItemInHand(InteractionHand.MAIN_HAND);

		if (!(item.getItem() instanceof ItemWeapon) || item.getItem() instanceof ItemMedigun)
			return;
		ItemWeapon weapon = ((ItemWeapon) item.getItem());

		double d0 = this.entityHost.distanceToSqr(this.attackTarget.getX(), this.attackTarget.getBoundingBox().minY,
				this.attackTarget.getZ());

		int moveTicks = updateTime - this.ticksUpdatePos;

		double lookX = this.posTarget.x + this.velTarget.x * moveTicks;
		double lookY = this.posTarget.y + this.attackTarget.getEyeHeight() + this.velTarget.y * moveTicks;
		double lookZ = this.posTarget.z + this.velTarget.z * moveTicks;
		boolean shouldFireProj = true;
		float dist = this.entityHost.distanceTo(this.attackTarget);
		if (this.projSpeed > 0) {

			int ticksToReach = Mth.ceil(dist / this.projSpeed);
			float overshoot = entityHost.scaleWithDifficulty(0.6f, 0f) * TF2ConfigVars.accurracyMult;
			ticksToReach = Math
					.round(ticksToReach * (this.entityHost.getRandom().nextFloat() * overshoot * 2 + 1 - overshoot));
			lookX += this.velTarget.x * ticksToReach * 1;
			lookZ += this.velTarget.z * ticksToReach * 1;
			lookY = this.posTarget.y + this.attackTarget.getBbHeight() / 2 + this.velTarget.y * moveTicks;
			if (this.fireAtFeet == -1 || this.entityHost.level.rayTraceBlocks(new Vec3(this.entityHost.getX(),
					this.entityHost.getY() + this.entityHost.getEyeHeight(), this.entityHost.getZ()),
					new Vec3(lookX, lookY, lookZ), false, true, false) != null)
				lookY += this.attackTarget.getEyeHeight() - this.attackTarget.getBbHeight() / 2;
			double yFall = !this.attackTarget.isOnGround() ? -this.attackTarget.getDeltaMovement().y : 0;
			if (!this.attackTarget.isInWater()) {
				int ticksGrav = ticksToReach - 1;
				int i = (ticksGrav * ticksGrav + ticksGrav) / 2;
				lookY += gravity * i;
				if (!this.attackTarget.isOnGround() && this.attackTarget.getDeltaMovement().y < 0)
					yFall += Mth.clamp(this.lastMotionY - this.attackTarget.getDeltaMovement().y, 0, 0.1) * i;
				/*
				 * for (int i = 1; i <= ticksToReach; i++) { lookY += gravity * i;
				 * 
				 * 
				 * }
				 */
			}

			HitResult mop = this.entityHost.level.rayTraceBlocks(this.attackTarget.position(),
					this.attackTarget.position().add(0, -0.3 - yFall, 0));
			if (mop != null && mop.getType() == HitResult.Type.BLOCK)
				yFall = this.attackTarget.getY() - mop.getLocation().y;
			shouldFireProj = mop != null || this.attackTarget.getDeltaMovement().y <= 0f;
			lookY -= yFall;

			if (this.fireAtFeet > 0 && this.entityHost.level.rayTraceBlocks(
					new Vec3(this.entityHost.getX(), this.entityHost.getY() + this.entityHost.getEyeHeight(),
							this.entityHost.getZ()),
					new Vec3(lookX, this.attackTarget.getY(), lookZ), false, true, false) == null) {
				lookY -= (this.attackTarget.getBbHeight() / 2) * this.fireAtFeet;

			}
			// System.out.println("look: "+(this.lastMotionY-this.attackTarget.motionY));

			/*
			 * RayTraceResult ray = this.entityHost.rayTrace(128d, 1f); if (ray != null)
			 * ((WorldServer)this.entityHost.world).spawnParticle(EnumParticleTypes.CRIT,
			 * ray.hitVec.x, ray.hitVec.y, ray.hitVec.z,1,0, 0, 0, 0, new int[0]);
			 */
		}

		this.lastMotionY = this.attackTarget.getDeltaMovement().y;
		boolean charged = weapon.holdingMode(item, entityHost) > 0
				&& weapon.getCharge(entityHost, item) >= this.releaseAt;
		// System.out.println("charge: "+weapon.getCharge(entityHost, item));
		boolean stay = this.entityHost.getSensing().hasLineOfSight(this.attackTarget)
				|| (this.projSpeed > 0 && this.attackTarget.getDeltaMovement().y > 0);

		boolean fire = stay && this.entityHost.getEffect(TF2weapons.stun) == null && shouldFireProj
				&& !charged
				&& TF2Util.lookingAt(this.entityHost,
						(this.explosive && d0 < 16 ? 30 : 0) + 2
								+ Math.toDegrees(Mth.atan2(this.attackTarget.getBbWidth() / 2, dist)),
						lookX, lookY, lookZ);

		if (!this.reloading && (this.entityHost.level.getDifficulty() != Difficulty.HARD || !fire)
				&& weapon.getClip(item) == 0 && weapon.hasClip(item))
			this.reloading = true;
		else if (this.reloading && (weapon.getClip(item) == weapon.getWeaponClipSize(item, entityHost)
				|| this.entityHost.getAmmo() == 0))
			this.reloading = false;

		this.entityHost.getLookControl().setLookAt(lookX, lookY, lookZ, this.entityHost.rotation, 90.0F);
		if ((!reloading) && fire
				&& d0 <= (this.attackTarget instanceof EntityBuilding ? this.attackRangeSSquared
						: this.attackRangeSquared)
				&& (((ItemUsable) item.getItem()).isAmmoSufficient(item, entityHost, true))
				&& (((ItemUsable) item.getItem()).shouldEntityFire(item, entityHost, attackTarget))) {
			this.reloading = false;
			if (!pressed) {
				pressed = true;
				int state = this.entityHost.getState(true);
				weapon.startUse(item, this.entityHost, this.entityHost.level,
						this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state, state);
				this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = state;
				TF2Util.sendTracking(new TF2Message.ActionMessage(state, entityHost), entityHost);
			}

		} else if (weapon.getCharge(entityHost, item) >= this.releaseAt) {
			if (pressed) {
				if (this.jump)
					this.entityHost.jump = false;
				int valuedef = this.entityHost.getState(false);
				weapon.endUse(item, this.entityHost, this.entityHost.level,
						this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state, valuedef);
				this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = valuedef;
				TF2Util.sendTracking(new TF2Message.ActionMessage(valuedef, entityHost), entityHost);
			}
			pressed = false;
		}
	}

}
