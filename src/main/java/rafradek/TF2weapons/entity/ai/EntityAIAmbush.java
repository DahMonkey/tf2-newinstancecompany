package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntitySpy;
import rafradek.TF2weapons.item.ItemCloak;
import rafradek.TF2weapons.item.ItemDisguiseKit;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class EntityAIAmbush extends Goal {

	public EntitySpy host;
	public int counter;

	public EntityAIAmbush(EntitySpy entitySpy) {
		this.host = entitySpy;
		this.setFlags(TF2Util.mutexFlags(3));
	}

	@Override
	public boolean shouldExecute() {
		LivingEntity target = this.host.getTarget();
		if (!this.host.hasWatch() || target == null) {
			this.host.isAmbushing = false;
			return false;
		}

		double dist = target.distanceToSqr(this.host);
		boolean isAttacked = this.host.getLastHurtByMob() != null
				&& this.host.tickCount - this.host.getRevengeTimer() < 45;
		boolean enemyLooking = !this.host.getWepCapability().isDisguised()
				&& !this.host.loadout.getStackInSlot(3).getTag().getBoolean("Active")
				&& this.host.getSensing().canSee(target)
				&& TF2Util.lookingAtFast(target, dist > 400 ? 50 : 105, this.host.getX(), this.host.getY(), this.host.getZ());
		boolean isAtEnemy = dist < 9
				&& !TF2Util.lookingAtFast(target, 105, this.host.getX(), this.host.getY(), this.host.getZ());
		return host.isAmbushing = !isAttacked && !enemyLooking && !isAtEnemy;
	}

	@Override
	public void resetTask() {
		// System.out.println("Stop");
		this.host.getNavigation().stop();
		if (this.host.loadout.getStackInSlot(3).getTag().getBoolean("Active"))
			((ItemCloak) this.host.loadout.getStackInSlot(3).getItem()).setCloak(false,
					this.host.loadout.getStackInSlot(3), this.host, this.host.level);
		this.host.isAmbushing = false;
		/*
		 * if(this.host.cloak.getTagCompound().getBoolean("Active")){
		 * ((ItemCloak)this.host.cloak.getItem()).altUse(this.host.cloak, this.host,
		 * this.host.world); }
		 */
	}

	@Override
	public void updateTask() {
		// System.out.println("Tick");
		LivingEntity target = this.host.getTarget();
		if ((target != null && target.deathTime > 0) || host.deathTime > 0) {
			this.resetTask();
			return;
		}
		if (target == null)
			return;
		/*
		 * if(!this.host.cloak.getTagCompound().getBoolean("Active")){
		 *
		 * ((ItemCloak)this.host.cloak.getItem()).setCloak(true, this.host.cloak,
		 * this.host, this.host.world); }
		 */
		ItemStack cloak = this.host.loadout.getStackInSlot(3);
		double dist = target.distanceToSqr(this.host);
		boolean enemyLooking = !cloak.getTag().getBoolean("Active")
				&& this.host.getSensing().canSee(target)
				&& TF2Util.lookingAtFast(target, 105, this.host.getX(), this.host.getY(), this.host.getZ());

		if (cloak.getDamageValue() < cloak.getMaxDamage() - 72 && !cloak.getTag().getBoolean("Active")
				&& !enemyLooking)
			((ItemCloak) cloak.getItem()).setCloak(true, cloak, this.host, this.host.level);

		if (!this.host.getWepCapability().isDisguised()) {
			if (this.host.getTarget() != null && this.host.getTarget() instanceof Player)
				ItemDisguiseKit.startDisguise(this.host, this.host.level, "M:" + TF2weapons.animalsDisguise
						.get(this.host.getRandom().nextInt(TF2weapons.animalsDisguise.size())));
			else
				ItemDisguiseKit.startDisguise(this.host, this.host.level, "T:Engineer");
		}
		// this.host.getNavigator().tryMoveToEntityLiving(target, 1);
		this.host.getLookControl().setLookPosition(target.getX(), target.getY() + target.getEyeHeight(), target.getZ(), 90,
				90.0F);

		if (dist > 16 || !TF2Util.lookingAtFast(target, 105, this.host.getX(), this.host.getY(), this.host.getZ())) {
			float x = -Mth.sin(target.getYRot() / 180.0F * (float) Math.PI);
			float z = Mth.cos(target.getYRot() / 180.0F * (float) Math.PI);
			Vec3 pos = new Vec3(target.getX() - x * 2, target.getY(), target.getZ() - z * 2);
			host.getNavigation().tryMoveToXYZ(target.getX() - x * 2, target.getY(), target.getZ() - z * 2, 1);
		} else {
			float x = -Mth.sin(target.getYRot() / 180.0F * (float) Math.PI);
			float z = Mth.cos(target.getYRot() / 180.0F * (float) Math.PI);
			Vec3 pos = new Vec3(x, 0, z).cross(new Vec3(0, 1, 0));
			host.getNavigation().tryMoveToXYZ(target.getX() + pos.x * 3 - x * 2, target.getY(),
					target.getZ() + pos.z * 3 - z * 2, 1f);
		}
		// this.host.getLookHelper().setLookPosition(target.posX+x*2,target.posY+target.getEyeHeight(),target.posZ+z*2,
		// 90f, 90.0F);

	}
}
