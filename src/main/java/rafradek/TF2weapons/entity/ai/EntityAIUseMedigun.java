package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemMedigun;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EntityAIUseMedigun extends EntityAIUseRangedWeapon {

	public EntityAIUseMedigun(EntityTF2Character par1IRangedAttackMob, float par2, float par5) {
		super(par1IRangedAttackMob, par2, par5);

	}

	@Override
	public boolean canUse() {
		return this.entityHost.getMainHandItem().getItem() instanceof ItemMedigun && super.canUse();
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */

	/**
	 * Resets the task
	 */

	@Override
	public void stop() {
		this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setHealTarget(-1);
		this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = 0;
		TF2Util.sendTracking(new TF2Message.ActionMessage(0, entityHost), entityHost);
		TF2Util.sendTracking(new TF2Message.CapabilityMessage(entityHost, false), entityHost);
		if (this.jump)
			this.entityHost.jump = false;
		this.attackTarget = null;
		this.comeCloser = 0;
		this.rangedAttackTime = -1;
		this.pressed = false;
	}

	@Override
	public void tick() {
		if ((this.attackTarget != null && this.attackTarget.deathTime > 0) || this.entityHost.deathTime > 0) {
			this.stop();
			return;
		}
		if (this.attackTarget == null)
			return;
		double d0 = this.entityHost.distanceToSqr(this.attackTarget.getX(), this.attackTarget.getBoundingBox().minY,
				this.attackTarget.getZ());

		double lookX = this.attackTarget.getX();
		double lookY = this.attackTarget.getY() + this.attackTarget.getEyeHeight();
		double lookZ = this.attackTarget.getZ();
		ItemStack stack = this.entityHost.getMainHandItem();
		/*
		 * boolean stay = this.entityHost.getEntitySenses().canSee(this.attackTarget);
		 * this.entityHost.setJumping(true); if (stay) { ++this.comeCloser; if (d0 <=
		 * (double) 12) this.comeCloser = 20; } else this.comeCloser = 0;
		 * //System.out.println(this.comeCloser+" "+this.attackRangeSquared+" "+d0); if
		 * (d0 <= 52 && this.comeCloser >= 20) { if (!this.dodging) {
		 * this.entityHost.getNavigator().clearPath(); this.dodging = true; } } else {
		 * this.dodging = false;
		 * this.entityHost.getNavigator().tryMoveToEntityLiving(this.attackTarget,
		 * this.entityMoveSpeed); }
		 */

		this.entityHost.getLookControl().setLookPosition(lookX, lookY, lookZ, this.entityHost.rotation, 90.0F);
		// this.entityHost.getLookHelper().setLookPositionWithEntity(this.attackTarget,
		// 1.0F, 90.0F);
		double range = ItemFromData.getData(stack).getFloat(PropertyType.RANGE);
		if (d0 <= range * range) {

			if (!pressed || this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getHealTarget() == -1) {
				pressed = true;
				this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null)
						.setHealTarget(this.attackTarget.getId());
				this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = 1;
				TF2Util.sendTracking(new TF2Message.ActionMessage(1, entityHost), entityHost);
				TF2Util.sendTracking(new TF2Message.CapabilityMessage(entityHost, false), entityHost);
				// System.out.println("co�do");
			} else {
				if (((ItemMedigun) stack.getItem()).shouldActivateCharge(stack, this.entityHost, this.attackTarget)
						|| (this.attackTarget instanceof Player
								&& this.attackTarget.getCapability(TF2weapons.PLAYER_CAP).orElse(null).medicCharge)) {
					MobEffect effect = ((ItemMedigun) stack.getItem()).getPotion(stack, this.entityHost);
					if (!((ItemMedigun) stack.getItem()).isAlreadyUbered(stack, this.entityHost))
						((ItemMedigun) stack.getItem()).startUse(stack, this.entityHost, this.entityHost.level, 0, 2);
				}
			}

		} else {
			if (pressed) {
				if (this.jump)
					this.entityHost.jump = false;
				this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setHealTarget(-1);
				this.entityHost.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state = 0;
				TF2Util.sendTracking(new TF2Message.ActionMessage(0, entityHost), entityHost);
				TF2Util.sendTracking(new TF2Message.CapabilityMessage(entityHost, false), entityHost);
				// System.out.println("co�z");
			}
			pressed = false;
		}
		// if(){
		if (this.jump && d0 < this.jumprange)
			this.entityHost.jump = true;
		else if (this.jump)
			this.entityHost.jump = false;

		// }
	}

}
