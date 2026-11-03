package rafradek.TF2weapons.entity.projectile;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.util.DamageSourceProjectile;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityFlame extends EntityProjectileBase {

	public EntityFlame(Level world) {
		super(world);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		super.initProjectile(shooter, hand, weapon);
		this.push(shooter.getDeltaMovement().x, shooter.getDeltaMovement().y, shooter.getDeltaMovement().z);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isInRangeToRender3d(double x, double y, double z) {
		return false;
	}

	@Override
	public void onHitGround(int x, int y, int z, HitResult mop) {
		if (!this.level.isClientSide && TF2ConfigVars.destTerrain == 2
				&& this.level.getBlockState(mop.getBlockPos()).getMaterial().getCanBurn()
				&& this.level.getBlockState(mop.getBlockPos().move(mop.getDirection())).getMaterial() != Material.FIRE
				&& this.level.getBlockState(mop.getBlockPos().move(mop.getDirection())).getBlock().isReplaceable(world,
						getPosition().move(mop.getDirection())))
			this.level.setBlockAndUpdate(mop.getBlockPos().move(mop.getDirection()), Blocks.FIRE.defaultBlockState());
		this.discard();
	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		if (!this.level.isClientSide && !this.hitEntities.contains(entityHit)) {
			this.hitEntities.add(entityHit);
			DamageSourceProjectile src = TF2Util.causeBulletDamage(this.usedWeapon, this.shootingEntity, this);
			src.setIsFire();
			int critical = TF2Util.calculateCritPost(entityHit, shootingEntity, this.getCritical(), this.usedWeapon,
					src);
			// float distance= (float) new Vec3d(this.shootingEntity.posX,
			// this.shootingEntity.posY,
			// this.shootingEntity.posZ).distanceTo(new Vec3d(mop.hitVec.x,
			// mop.hitVec.y, mop.hitVec.z))+5.028f;

			float dmg = TF2Util.calculateDamage(entityHit, world, this.shootingEntity, usedWeapon, critical,
					1f + (float) (this.tickCount - 1) / (this.getMaxTime() - 1));
			// System.out.println("damage: "+dmg);
			// dmg*=ItemUsable.getData(this.usedWeapon).get("Min
			// damage").getDouble()+1-(this.ticksExisted/this.getMaxTime())*ItemUsable.getData(this.usedWeapon).get("Min
			// damage").getDouble();
			if (TF2Util.dealDamage(entityHit, this.level, this.shootingEntity, this.usedWeapon, critical, dmg,
					src.setCritical(critical))
					&& (entityHit.tickCount - entityHit.getPersistentData().getInt("LastHitBurn") > 1
							|| entityHit.getPersistentData().getInt("LastHitBurn") > entityHit.tickCount)) {
				entityHit.getPersistentData().putInt("LastHitBurn", entityHit.tickCount);
				TF2Util.igniteAndAchievement(entityHit, this.shootingEntity, 1,
						TF2Attribute.getModifier("Burn Time", this.usedWeapon, 1, shootingEntity));
			}

		}
	}

	@Override
	public void onUpdate() {
		if (this.isEyeInFluid(Material.WATER)) {
			this.discard();
			return;
		}
		super.onUpdate();
	}

	@Override
	public void spawnParticles(double x, double y, double z) {}

	/*
	 * public double getMaxDistance(){ return 6.2865; }
	 */

	@Override
	public int getMaxTime() {
		return Math.round(3 + (TF2Attribute.getModifier("Flame Range", this.usedWeapon, 2f, this.shootingEntity)));
	}

	@Override
	protected float getSpeed() {
		return 1.2570f;
	}

	@Override
	public double getGravity() {
		return 0;
	}

	@Override
	public float getCollisionSize() {
		return 0.2f + this.tickCount * 0.18f;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean canPenetrate() {
		return true;
	}
}
