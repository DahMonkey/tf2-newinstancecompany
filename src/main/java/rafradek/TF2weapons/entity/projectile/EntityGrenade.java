package rafradek.TF2weapons.entity.projectile;

import rafradek.TF2weapons.TF2weapons;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemWeapon;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;

public class EntityGrenade extends EntityProjectileBase {

	public boolean hitGround;

	public int fuse = 46;
	private static final EntityDataAccessor<Byte> BOMB = SynchedEntityData.createKey(EntityGrenade.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> BURST = SynchedEntityData.createKey(EntityGrenade.class,
			EntityDataSerializers.BOOLEAN);

	public EntityGrenade(Level p_i1756_1_) {
		this(TF2weapons.GRENADE, p_i1756_1_);
	}

	public EntityGrenade(EntityType<? extends EntityGrenade> type, Level p_i1756_1_) {
		super(type, p_i1756_1_);
		this.setSize(0.3f, 0.3f);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		super.initProjectile(shooter, hand, weapon);
		this.setSize(0.3f, 0.3f);
		int weaponmode = (int) TF2Attribute.getModifier("Weapon Mode", this.usedWeapon, 0, shooter);
		if (weaponmode == 1) {
			this.setBomb(1);
			this.setSize(0.7f, 0.7f);
			this.fuse = 26 + this.random.nextInt(20);
			double motion = 0.8f + this.random.nextDouble() * 0.55;
			this.setDeltaMovement(this.getDeltaMovement().x * (motion), this.getDeltaMovement().y, this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * (motion), this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z * (motion));
		} else if (weaponmode == 2) {
			this.setBomb(2);
			this.fuse = (int) (20 - ((ItemWeapon) weapon.getItem()).getCharge(shooter, weapon) * 20);
		} else {
			this.fuse = (int) (TF2Attribute.getModifier("Fuse Time", weapon, 2.3f, shooter) * 20);
		}
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(BOMB, (byte) 0);
		this.entityData.define(BURST, false);
	}

	public void setBomb(int val) {
		this.entityData.set(BOMB, (byte) val);
	}

	public int getBomb() {
		return this.entityData.get(BOMB);
	}

	public void setBurst(boolean burst) {
		this.entityData.set(BURST, burst);
	}

	public boolean isBurst() {
		return this.entityData.get(BURST);
	}

	@Override
	public float getPitchAddition() {
		return -3;
	}

	@Override
	public float getExplosionSize() {
		return 3.05f;
	}

	@Override
	public void onHitGround(int x, int y, int z, HitResult mop) {

	}

	@Override
	public SoundEvent getExplosionSound() {
		return this.isBurst() ? TF2Sounds.GRENADE_EXPLODESPECIAL : super.getExplosionSound();
	}

	@Override
	public void explode(double x, double y, double z, Entity direct, float damageMult) {
		super.explode(x, y, z, direct, damageMult);
		if (world.isClientSide || this.shootingEntity == null)
			return;
		if (!this.isBurst()) {
			int grenadeSpecialist = (int) TF2Attribute.getModifier("Grenade Specialist", this.usedWeapon, 0f,
					shootingEntity);
			int grenadeMult = (int) (2 * (direct == null ? 1.5f : 1));
			double rndSpread = direct == null ? 0.4f : 0.1f;
			for (int i = 0; i < grenadeSpecialist * grenadeMult; i++) {
				EntityGrenade burst = new EntityGrenade(world);
				burst.initProjectile(this.shootingEntity, InteractionHand.MAIN_HAND, usedWeaponOrig);
				burst.setPos(x, y, z);
				double motionmult = 1D + (this.random.nextDouble() * 0.3 * grenadeSpecialist) - 0.15 * grenadeSpecialist;
				if (direct == null)
					motionmult += 0.25D + (this.random.nextDouble() * 0.1 * grenadeSpecialist);
				burst.shoot(-this.getDeltaMovement().x * motionmult * 0.07 + this.random.nextDouble() * rndSpread * 2 - rndSpread, 0.2,
						-this.getDeltaMovement().z * motionmult * 0.07 + this.random.nextDouble() * rndSpread * 2 - rndSpread,
						(float) motionmult * 0.3f, 0f);
				burst.setBurst(true);
				burst.damageModifier = this.damageModifier * 0.5f;
				this.level.addFreshEntity(burst);
			}
		}

	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		if (!this.hitGround && !this.isBurst()) {
			if (getBomb() == 0) {
				this.explode(mop.getLocation().x, mop.getLocation().y, mop.getLocation().z, mop.getEntity(), 1);
			} else if (!this.hitEntities.contains(entityHit)) {

				if (this.attackDirect(entityHit, 1, mop.hitInfo instanceof Boolean ? (Boolean) mop.hitInfo : false,
						mop.getLocation()) && entityHit instanceof LivingEntity) {
					((LivingEntity) entityHit).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 3));

				}

				/*
				 * if(mop.sideHit==EnumFacing.EAST || mop.sideHit==EnumFacing.WEST)
				 * this.onHitBlockX(); else if(mop.sideHit==EnumFacing.NORTH ||
				 * mop.sideHit==EnumFacing.SOUTH) this.onHitBlockZ(); else
				 * this.onHitBlockY(null);
				 */
				this.setDeltaMovement(this.getDeltaMovement().x * (0.6), this.getDeltaMovement().y, this.getDeltaMovement().z);
				this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * (0.65), this.getDeltaMovement().z);
				this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z * (0.6));
			}
		}
	}

	public double maxMotion() {
		return Math.max(this.getDeltaMovement().x, Math.max(this.getDeltaMovement().y, this.getDeltaMovement().z));
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.fuse--;
		if (this.fuse <= 0)
			this.explode(this.getX(), this.getY() + this.getBbHeight() / 2, this.getZ(), null, this.getBomb() > 0 ? 1 : 0.64f);
		if (this.collided && !this.hitGround) {
			this.hitGround = true;
			if (!this.level.isClientSide) {
				int attr = (int) TF2Attribute.getModifier("Coll Remove", this.usedWeapon, 0, this.shootingEntity);
				if (attr == 2 || this.isBurst())
					this.explode(this.getX(), this.getY(), this.getZ(), null, this.getBomb() > 0 ? 1 : 0.64f);
				if (attr == 1)
					this.discard();
			}
		}
	}

	@Override
	public void spawnParticles(double x, double y, double z) {

	}

	@Override
	protected float getSpeed() {
		return 1.16205f;
	}

	@Override
	public double getGravity() {
		return 0.0381f;
	}

	@Override
	public boolean useCollisionBox() {
		return true;
	}

	@Override
	public void onHitBlockX() {
		this.setDeltaMovement((-this.getDeltaMovement().x * 0.18), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (this.getDeltaMovement().y * 0.8), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (this.getDeltaMovement().z * 0.8));
	}

	@Override
	public void onHitBlockY(Block block) {
		this.setDeltaMovement((this.getDeltaMovement().x * 0.8), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (-this.getDeltaMovement().y * 0.18), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (this.getDeltaMovement().z * 0.8));
	}

	@Override
	public void onHitBlockZ() {
		this.setDeltaMovement((this.getDeltaMovement().x * 0.8), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (this.getDeltaMovement().y * 0.8), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (-this.getDeltaMovement().z * 0.18));
	}

	@Override
	public void notifyDataManagerChange(EntityDataAccessor<?> key) {
		if (BOMB.equals(key) && this.getBomb() == 1) {
			this.setSize(0.7f, 0.7f);
		}
	}
}
