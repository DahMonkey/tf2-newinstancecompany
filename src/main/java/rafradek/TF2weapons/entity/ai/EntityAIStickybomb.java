package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityDemoman;
import rafradek.TF2weapons.item.ItemStickyLauncher;
import rafradek.TF2weapons.item.ItemUsable;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class EntityAIStickybomb extends Goal {

	/** The entity the AI instance has been applied to */
	protected final EntityDemoman entityHost;

	/**
	 * The entity (as a RangedAttackMob) the AI instance has been applied to.
	 */

	protected float entityMoveSpeed;

	/**
	 * The maximum time the AI has to wait before peforming another ranged attack.
	 */
	protected float attackRangeSquared;

	protected boolean pressed;
	protected boolean dodging;
	public boolean jump;
	public float dodgeSpeed = 1f;
	public int jumprange;
	public int searchTimer;
	public Vec3 attackTarget;

	public float gravity;

	public boolean explosive;

	public EntityAIStickybomb(EntityDemoman par1IRangedAttackMob, float par2, float par5) {
		this.entityHost = par1IRangedAttackMob;
		this.entityMoveSpeed = par2;
		this.attackRangeSquared = par5 * par5;

		this.setFlags(TF2Util.mutexFlags(3));
	}

	public void setRange(float range) {
		this.attackRangeSquared = range * range;
	}

	@Override
	public boolean canUse() {
		if (this.entityHost.getMainWeapon() != -1 && this.entityHost.getMainWeapon() != 1)
			return false;
		if (this.entityHost.isRobot() || this.entityHost.getTarget() != null
				|| this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).activeBomb.size() > 7)
			return false;
		ItemStack stickybomb = this.entityHost.loadout.getStackInSlot(1);
		return stickybomb.getItem() instanceof ItemStickyLauncher
				&& ((ItemUsable) stickybomb.getItem()).isAmmoSufficient(stickybomb, this.entityHost, true);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean canContinueToUse() {
		return this.canUse() || !this.entityHost.getNavigation().isDone();
	}

	/**
	 * Resets the task
	 */
	@Override
	public void stop() {
		if (this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state != 0) {
			pressed = false;
			if (this.entityHost.getMainHandItem().getItem() instanceof ItemWeapon)
				((ItemWeapon) this.entityHost.getItemInHand(InteractionHand.MAIN_HAND).getItem()).endUse(
						this.entityHost.getItemInHand(InteractionHand.MAIN_HAND), this.entityHost, this.entityHost.level,
						this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state, 0);
			this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = 0;
			TF2Util.sendTracking(new TF2Message.ActionMessage(0, entityHost), entityHost);
		}
		this.entityHost.getNavigation().stop();
		this.attackTarget = null;
		this.entityHost.switchSlot(0);
	}

	/**
	 * Updates the task
	 */

	@Override
	public void tick() {
		if (this.attackTarget == null)
			this.attackTarget = DefaultRandomPos.getPos(this.entityHost, 10, 4);
		if (this.attackTarget == null)
			return;
		this.entityHost.switchSlot(1);
		if (!(this.entityHost.getMainHandItem().getItem() instanceof ItemStickyLauncher))
			return;
		double d0 = this.entityHost.distanceToSqr(this.attackTarget.x, this.attackTarget.y, this.attackTarget.z);

		double lookX = this.attackTarget.x;
		double lookY = this.attackTarget.y;
		double lookZ = this.attackTarget.z;

		if (d0 <= this.attackRangeSquared) {
			if (!this.dodging) {
				this.entityHost.getNavigation().stop();
				this.dodging = true;
			}
		} else {
			this.dodging = false;

			this.entityHost.getNavigation().moveTo(this.attackTarget.x, this.attackTarget.y, this.attackTarget.z,
					this.entityMoveSpeed);
		}
		this.entityHost.getLookControl().setLookAt(lookX, lookY, lookZ, this.entityHost.rotation, 90.0F);
		if (d0 <= this.attackRangeSquared) {
			if (!pressed) {
				pressed = true;
				((ItemUsable) this.entityHost.getItemInHand(InteractionHand.MAIN_HAND).getItem()).startUse(
						this.entityHost.getItemInHand(InteractionHand.MAIN_HAND), this.entityHost, this.entityHost.level,
						this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state, 1);
				this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = 1;
				TF2Util.sendTracking(new TF2Message.ActionMessage(1, entityHost), entityHost);
			}

		} else {
			if (pressed) {
				((ItemUsable) this.entityHost.getItemInHand(InteractionHand.MAIN_HAND).getItem()).endUse(
						this.entityHost.getItemInHand(InteractionHand.MAIN_HAND), this.entityHost, this.entityHost.level,
						this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state, 0);
				this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = 0;
				TF2Util.sendTracking(new TF2Message.ActionMessage(0, entityHost), entityHost);
			}
			pressed = false;
		}
		// if(){
		// }
	}

}
