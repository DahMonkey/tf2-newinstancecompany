package rafradek.TF2weapons.item;

import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.entity.EntityDummy;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.mercenary.EntityScout;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.projectile.EntityBall;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemProjectileWeapon extends ItemWeapon {

	@Override
	public void shoot(ItemStack stack, LivingEntity living, Level world, int thisCritical, InteractionHand hand) {
		if (!world.isClientSide) {
			// System.out.println("Tick: "+living.ticksExisted);
			EntityProjectileBase proj;
			/*
			 * double oldX=living.posX; double oldY=living.posY; double oldZ=living.posZ;
			 * float oldPitch=living.rotationPitch; float oldYaw=living.rotationYawHead;
			 * if(this.usePrediction()&&living instanceof EntityPlayer){ PredictionMessage
			 * message=TF2ProjectileHandler.nextShotPos.get(living); living.posX=message.x;
			 * living.posY=message.y; living.posZ=message.z;
			 * living.rotationYawHead=message.yaw; living.rotationPitch=message.pitch; }
			 */
			try {
				proj = MapList.projectileClasses.get(ItemFromData.getData(stack).getString(PropertyType.PROJECTILE))
						.getConstructor(Level.class).newInstance(world);
				proj.initProjectile(living, hand, stack);
				// proj.setIsCritical(thisCritical);
				proj.setCritical(thisCritical);
				this.onProjectileShoot(stack, proj, living, world, thisCritical, hand);
				world.addFreshEntity(proj);
				proj.infinite = this.isProjectileInfinite(living, stack);
				proj.trace();

			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		// living.posX=oldX;
		// living.posY=oldY;
		// living.posZ=oldZ;
		// living.rotationPitch=oldPitch;
		// living.rotationYawHead=oldYaw;
	}

	public void onProjectileShoot(ItemStack stack, EntityProjectileBase proj, LivingEntity living, Level world,
			int thisCritical, InteractionHand hand) {}

	@Override
	public void onDealDamage(ItemStack stack, LivingEntity attacker, Entity target, DamageSource source,
			float amount) {
		super.onDealDamage(stack, attacker, target, source, amount);
		if (target instanceof LivingEntity && !(target instanceof EntityBuilding)
				&& getData(stack).getName().equals("sandmanball")) {
			EntityBall ball = (EntityBall) source.getDirectEntity();
			double reduce = Math.max(0.5, (25
					- ((LivingEntity) target).getAttribute(Attributes.ARMOR).getValue())
					/ 25D);
			if (attacker instanceof EntityTF2Character) {
				reduce *= ((EntityTF2Character) attacker).scaleWithDifficulty(0.5f, 1);
			}
			if (!ball.canBePickedUp && ball.throwPos.distanceToSqr(target.position()) > 1100) {
				TF2Util.stun((LivingEntity) target, (int) (160 * reduce), true);
				target.playSound(TF2Sounds.WEAPON_STUN_MAX, 4f, 1f);
			} else if (!ball.canBePickedUp && ball.throwPos.distanceToSqr(target.position()) > 12) {
				TF2Util.stun((LivingEntity) target,
						(int) (ball.throwPos.distanceTo(target.position()) * 8 * reduce), false);
				target.playSound(TF2Sounds.WEAPON_STUN, 1.6f, 1f);
			}
		}
	}

	public boolean isProjectileInfinite(LivingEntity living, ItemStack stack) {
		return !(living instanceof EntityDummy) && this.searchForAmmo(living, stack) == ItemAmmo.STACK_FILL;
	}

	@Override
	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {
		return /*
				 * (((!(living instanceof EntityPlayer) || ) ||
				 * TF2ProjectileHandler.nextShotPos.containsKey(living))||world. isRemote
				 */super.canFire(world, living, stack) && !(living instanceof EntityScout
				&& ((EntityScout) living).usedSlot == 2 && ((EntityScout) living).ballCooldown > 0);
	}

	@Override
	public void onOverload(ItemStack stack, LivingEntity owner, InteractionHand hand) {
		this.shoot(stack, owner, owner.level, -1, hand);
	}
}
