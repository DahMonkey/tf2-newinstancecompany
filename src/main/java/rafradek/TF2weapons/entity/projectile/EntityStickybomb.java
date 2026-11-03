package rafradek.TF2weapons.entity.projectile;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;

public class EntityStickybomb extends EntityProjectileBase {

	public int stickCooldown;

	public EntityStickybomb(Level p_i1756_1_) {
		this(TF2weapons.STICKYBOMB, p_i1756_1_);
	}

	public EntityStickybomb(EntityType<? extends EntityStickybomb> type, Level p_i1756_1_) {
		super(type, p_i1756_1_);
		this.setSize(0.3f, 0.3f);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		super.initProjectile(shooter, hand, weapon);
		this.setSize(0.3f, 0.3f);
		this.setProjType((int) TF2Attribute.getModifier("Weapon Mode", this.usedWeapon, 0, shooter));
	}

	@Override
	public float getPitchAddition() {
		return 3;
	}

	@Override
	public boolean hurt(DamageSource source, float damage) {
		if (source.isExplosion() && !TF2Util.isAlliedTo(source.getEntity(), this.shootingEntity)) {
			this.addStickCooldown();
		}
		return super.hurt(source, damage);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
	}

	public int getArmTime() {
		return Math.round(TF2Attribute.getModifier("Arm Time", this.usedWeapon, 0.8f, this.shootingEntity) * 20);
	}

	@Override
	public boolean isPickable() {
		return isSticked();
	}

	@Override
	public void onHitGround(int x, int y, int z, BlockHitResult mop) {

	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {

	}

	public double maxMotion() {
		return Math.max(this.getDeltaMovement().x, Math.max(this.getDeltaMovement().y, this.getDeltaMovement().z));
	}

	@Override
	public void spawnParticles(double x, double y, double z) {

	}

	public void addStickCooldown() {
		this.stickCooldown = 20;
		this.setSticked(false);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.stickCooldown > 0) {
			this.stickCooldown--;
		}
		if (this.shootingEntity == null || !this.shootingEntity.isAlive())
			this.discard();
	}

	@Override
	public void remove(Entity.RemovalReason removalReason) {
		super.remove(removalReason);
		if (!this.level.isClientSide)
			this.shootingEntity.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).activeBomb.remove(this);
	}

	@Override
	protected float getSpeed() {
		return 0.7667625f;
	}

	@Override
	public double getGravity() {
		return 0.0381f;
	}

	@Override
	public boolean isSticky() {
		return this.stickCooldown <= 0;
	}

	@Override
	public boolean useCollisionBox() {
		return true;
	}

	@Override
	public int getMaxTime() {
		return 72000;
	}

	@Override
	public float getExplosionSize() {
		return 3.05f;
	}

	@Override
	public void onHitBlockX() {
		this.setDeltaMovement((0), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (0), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (0));
	}

	@Override
	public void onHitBlockY(Block block) {
		this.setDeltaMovement((0), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (0), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (0));
	}

	@Override
	public void onHitBlockZ() {
		this.setDeltaMovement((0), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (0), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (0));
	}

	@Override
	public boolean isCurrentlyGlowing() {
		return super.isCurrentlyGlowing() || (this.getProjType() == 1 && this.level.isClientSide
				&& this.shootingEntity == ClientProxy.getLocalPlayer() && this.tickCount >= this.getArmTime()
				&& TF2Util.lookingAt(this.shootingEntity, 30, this.getX(), this.getY(), this.getZ()));
	}

	@Override
	public float getDistanceToTarget(Entity target, double x, double y, double z) {
		if (this.tickCount >= 100) {
			return ((ItemWeapon) this.usedWeapon.getItem()).getWeaponDamageFalloff(this.usedWeapon);
		} else
			return super.getDistanceToTarget(target, x, y, z);
	}
}
