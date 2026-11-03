package rafradek.TF2weapons.entity.projectile;

import com.google.common.collect.Iterables;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.DamageSourceProjectile;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2HitResult;
import rafradek.TF2weapons.util.TF2Util;

import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Base of all TF2 projectiles. Ported onto 1.19's {@link Projectile}; flight, homing, sticking,
 * penetration and hit logic are unchanged from 1.12. The 1.12 hand-written collision code is
 * replaced by vanilla collision, with sticky projectiles stopping at the first contact.
 */
public abstract class EntityProjectileBase extends Projectile implements IEntityAdditionalSpawnData {
	public HashSet<Entity> hitEntities = new HashSet<>();
	/** The owner of this projectile. */
	public LivingEntity shootingEntity;
	public ItemStack usedWeapon = ItemStack.EMPTY;
	public ItemStack usedWeaponOrig = ItemStack.EMPTY;
	public double gravity = 0.05;
	public float health = 4f;
	public float distanceTravelled;
	public BlockPos stickedBlock;
	public EntitySentry sentry;

	public boolean reflected;
	public boolean infinite;

	public double cachedGravity = -1;
	public float damageModifier = 1f;
	public float chargeLevel;

	public Entity homingTarget;
	public float homingAngle;
	public float homingSpeed;

	private EntityDimensions customSize;

	private static final EntityDataAccessor<Byte> CRITICAL = SynchedEntityData.defineId(EntityProjectileBase.class,
			EntityDataSerializers.BYTE);
	public static final EntityDataAccessor<Byte> TYPE = SynchedEntityData.defineId(EntityProjectileBase.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> STICK = SynchedEntityData.defineId(EntityProjectileBase.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float> STICK_X = SynchedEntityData.defineId(EntityProjectileBase.class,
			EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> STICK_Y = SynchedEntityData.defineId(EntityProjectileBase.class,
			EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> STICK_Z = SynchedEntityData.defineId(EntityProjectileBase.class,
			EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> GRAVITY = SynchedEntityData.defineId(EntityProjectileBase.class,
			EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> PENETRATE = SynchedEntityData.defineId(EntityProjectileBase.class,
			EntityDataSerializers.BOOLEAN);

	protected EntityProjectileBase(EntityType<? extends EntityProjectileBase> type, Level level) {
		super(type, level);
	}

	/** 1.12-style size change; also updates the bounding box. */
	protected void setSize(float width, float height) {
		this.customSize = EntityDimensions.scalable(width, height);
		this.refreshDimensions();
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return this.customSize != null ? this.customSize : super.getDimensions(pose);
	}

	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		this.setThrower(shooter);
		this.usedWeapon = weapon.copy();
		this.usedWeaponOrig = weapon;
		this.moveTo(shooter.getX(), shooter.getY() + shooter.getEyeHeight(), shooter.getZ(), shooter.yHeadRot,
				shooter.getXRot() + this.getPitchAddition());
		Vec3 look = Vec3.directionFromRotation(shooter.getXRot() + this.getPitchAddition(), shooter.yHeadRot).scale(80)
				.add(shooter.getEyePosition(1f));
		Vec3 trace;
		if (shooter instanceof Player) {
			HitResult ray = Iterables.getFirst(TF2Util.pierce(level, shooter, this.getX(), this.getY(), this.getZ(),
					look.x, look.y, look.z, false, 0, false), null);
			trace = ray.getLocation();
		} else
			trace = look;
		int side = hand == InteractionHand.MAIN_HAND ? 1 : -1;
		double offY = shooter instanceof EntitySentry ? -0.1D : (shooter instanceof Player ? 0.1D : 0D);
		this.setPos(this.getX() - Mth.cos(this.getYRot() / 180.0F * (float) Math.PI) * 0.16F * side,
				this.getY() - offY, this.getZ() - Mth.sin(this.getYRot() / 180.0F * (float) Math.PI) * 0.16F * side);
		if (trace.lengthSqr() < 2)
			trace = look;
		boolean nospread = false;
		if (TF2Attribute.getModifier("Onyx Projectile", this.usedWeapon, 0, shooter) != 0) {
			this.damageModifier = TF2Attribute.getModifier("Onyx Projectile", this.usedWeapon, 0, shooter);
			nospread = true;
		}

		this.shoot(trace.x - this.getX(), trace.y - this.getY(), trace.z - this.getZ(),
				((ItemWeapon) this.usedWeapon.getItem()).getProjectileSpeed(usedWeapon, shooter),
				nospread ? 0
						: ((ItemWeapon) this.usedWeapon.getItem()).getWeaponSpread(usedWeapon, shooter) * (133.3333333f));
		if (((ItemWeapon) this.usedWeapon.getItem()).canPenetrate(this.usedWeapon, this.shootingEntity))
			this.setPenetrate();

		if (((ItemWeapon) this.usedWeapon.getItem()).holdingMode(this.usedWeapon, shooter) != 0)
			this.chargeLevel = ((ItemWeapon) this.usedWeapon.getItem()).getCharge(shooter, usedWeapon);
		this.getGravityOverride();
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(CRITICAL, (byte) 0);
		this.entityData.define(TYPE, (byte) 0);
		this.entityData.define(STICK, false);
		this.entityData.define(STICK_X, 0f);
		this.entityData.define(STICK_Y, 0f);
		this.entityData.define(STICK_Z, 0f);
		this.entityData.define(PENETRATE, false);
		this.entityData.define(GRAVITY, -1f);
	}

	public float getPitchAddition() {
		return 0;
	}

	@Override
	public boolean ignoreExplosion() {
		return true;
	}

	/** Points the projectile along x, y, z with the given speed and spread. */
	@Override
	public void shoot(double x, double y, double z, float speed, float spread) {
		if (spread > 0) {
			float xzlen = Mth.sqrt((float) (x * x + z * z));
			float yaw = (float) (Mth.atan2(x, z));
			float pitch = (float) (Mth.atan2(y, xzlen));
			Vec3 random = TF2Util.radiusRandom2D(spread * 0.0075f, level.random, yaw, pitch, speed);
			x = random.x;
			y = random.y;
			z = random.z;
		} else {
			double len = Math.sqrt(x * x + y * y + z * z);
			x = x / len * speed;
			y = y / len * speed;
			z = z / len * speed;
		}

		this.setDeltaMovement(x, y, z);
		float f3 = (float) Math.sqrt(x * x + z * z);
		this.setYRot((float) (Mth.atan2(x, z) * 180.0D / Math.PI));
		this.setXRot((float) (Mth.atan2(y, f3) * 180.0D / Math.PI));
		this.yRotO = this.getYRot();
		this.xRotO = this.getXRot();
	}

	public void face(double x, double y, double z, float speedmult) {
		float speed = (float) this.getDeltaMovement().length();
		this.shoot(x - this.getX(), y - this.getY(), z - this.getZ(), speed * speedmult, 0);
	}

	public void face(LivingEntity target, float speedmult) {
		float speed = (float) this.getDeltaMovement().length();
		double x = target.getX();
		double y = target.getY() + target.getEyeHeight();
		double z = target.getZ();

		speed = speed * speedmult;

		double dist = target.distanceTo(this);
		float gravity = (float) this.getGravityOverride();
		int ticksToReach = Mth.ceil(dist / speed);

		if (ticksToReach != 0) {
			x += target.getDeltaMovement().x * ticksToReach;
			z += target.getDeltaMovement().z * ticksToReach;
			if (!target.isInWater()) {
				int ticksGrav = ticksToReach - 1;
				int i = (ticksGrav * ticksGrav + ticksGrav) / 2;
				y += gravity * i;
			}
		}
		this.face(x, y, z, speedmult);
	}

	@Override
	public void lerpMotion(double x, double y, double z) {
		this.setDeltaMovement(x, y, z);
		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			float f = (float) Math.sqrt(x * x + z * z);
			this.setYRot((float) (Mth.atan2(x, z) * 180.0D / Math.PI));
			this.setXRot((float) (Mth.atan2(y, f) * 180.0D / Math.PI));
			this.xRotO = this.getXRot();
			this.yRotO = this.getYRot();
			this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
		}
	}

	public void explode(double x, double y, double z, Entity direct, float damageMult) {
		if (level.isClientSide || this.shootingEntity == null)
			return;
		this.discard();
		float size = this.getExplosionSize()
				* TF2Attribute.getModifier("Explosion Radius", this.usedWeapon, 1, this.shootingEntity);
		if (TF2Attribute.getModifier("Airborne Bonus", this.usedWeapon, 0, this.shootingEntity) != 0)
			size *= 0.8f;
		TF2Util.explosion(level, shootingEntity, usedWeapon, this, direct, x, y, z, size,
				damageMult * this.damageModifier, this.getCritical(), this.getDistanceToTarget(null, x, y, z));
	}

	public SoundEvent getExplosionSound() {
		return ItemFromData.getSound(this.usedWeapon, PropertyType.EXPLOSION_SOUND);
	}

	public void addDamageTypes(DamageSource source) {
	}

	public boolean attackDirect(Entity target, double pushForce, boolean headshot, Vec3 hitPos) {
		if (!this.level.isClientSide) {
			if (target != null && shootingEntity != null && target.getTeam() != null
					&& target.getTeam().equals(shootingEntity.getTeam())
					& !(target == shootingEntity || (shootingEntity instanceof OwnableEntity
							&& ((OwnableEntity) shootingEntity).getOwner() == target)))
				return false;
			if (!this.hitEntities.contains(target)) {
				this.hitEntities.add(target);
				float distance = this.getDistanceToTarget(target, hitPos.x, hitPos.y, hitPos.z);
				DamageSourceProjectile src = TF2Util.causeBulletDamage(this.usedWeapon, this.shootingEntity, this);
				int critical = TF2Util.calculateCritPost(target, shootingEntity,
						headshot ? ((ItemWeapon) this.usedWeapon.getItem()).getHeadshotCrit(shootingEntity,
								this.usedWeapon) : this.getCritical(),
						this.usedWeapon, src);
				float dmg = TF2Util.calculateDamage(target, level, this.shootingEntity, usedWeapon, critical, distance)
						* this.damageModifier;
				src.setCritical(critical);
				this.addDamageTypes(src);

				if (headshot)
					((TF2DamageSource) src).addAttackFlag(TF2DamageSource.HEADSHOT);
				boolean proceed = ((ItemWeapon) this.usedWeapon.getItem()).onHit(usedWeapon, this.shootingEntity,
						target, dmg, critical, false);
				if (!proceed || TF2Util.dealDamage(target, this.level, this.shootingEntity, this.usedWeapon, critical,
						dmg, src)) {
					if (!this.canPenetrate())
						this.discard();
					if (proceed) {
						Vec3 pushvec = new Vec3(target.getX() - hitPos.x,
								target.getY() + target.getBbHeight() / 2 - hitPos.y, target.getZ() - hitPos.z)
										.normalize();
						pushvec = pushvec.scale(((ItemWeapon) this.usedWeapon.getItem())
								.getKnockbackForDamage(usedWeapon, shootingEntity, dmg, src));
						if (target instanceof LivingEntity) {
							pushvec = pushvec.scale(
									1 - ((LivingEntity) target).getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
						}
						target.push(pushvec.x, pushvec.y, pushvec.z);
						target.hasImpulse = target.hasImpulse || -(pushvec.y) > 0.02D;
						if (target instanceof ServerPlayer)
							TF2weapons.network.sendTo(new TF2Message.VelocityAddMessage(pushvec, target.hasImpulse),
									(ServerPlayer) target);
					}
					return true;
				}
			}
		}
		return false;
	}

	public float getDistanceToTarget(Entity target, double x, double y, double z) {
		if (target == null)
			return (float) this.shootingEntity.position().distanceTo(new Vec3(x, y, z));
		else
			return (float) TF2Util.getDistanceBox(this.shootingEntity, target.getX(), target.getY(), target.getZ(),
					target.getBbWidth() + 0.1, target.getBbHeight() + 0.1);
	}

	@Override
	public boolean canChangeDimensions() {
		return false;
	}

	public float getExplosionSize() {
		return 2.8f;
	}

	public void trace() {
		boolean flag = this.shootingEntity.getCapability(TF2weapons.WEAPONS_CAP).isPresent();
		if (flag)
			WeaponsCapability.get(this.shootingEntity).lastHitCharge = this.chargeLevel;
		boolean headshot = this.usedWeapon.isEmpty() || !flag ? false
				: ((ItemWeapon) this.usedWeapon.getItem()).canHeadshot(this.shootingEntity, this.usedWeapon);
		for (HitResult target : TF2Util.pierce(this.level, this.shootingEntity, this.getX(), this.getY(), this.getZ(),
				this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y,
				this.getZ() + this.getDeltaMovement().z, headshot, this.getCollisionSize(), this.canPenetrate(),
				this.getCollisionPredicate())) {

			Entity hitEntity = TF2HitResult.getEntity(target);
			if (hitEntity instanceof Player && ((Player) hitEntity).getAbilities().invulnerable)
				continue;

			if (hitEntity != null && !ForgeEventFactory.onProjectileImpact(this, target)
					&& !(TF2Util.isAlliedTo(this.shootingEntity, hitEntity)
							&& (this.tickCount < 3 && !level.isClientSide && ((ItemWeapon) this.usedWeapon.getItem())
									.onHit(usedWeapon, shootingEntity, hitEntity, 1, 0, true)))) {
				this.onHitMob(hitEntity, target);
			} else if (target.getType() == HitResult.Type.BLOCK && !this.useCollisionBox()) {
				BlockHitResult blockHit = (BlockHitResult) target;
				if (TF2Attribute.getModifier("Detonate", usedWeapon, 0, shootingEntity) != 0) {
					TF2Attribute.setAttribute(usedWeapon, TF2Attribute.attributes[0], 0);
					this.explode(target.getLocation().x + blockHit.getDirection().getStepX() * 0.05,
							target.getLocation().y + blockHit.getDirection().getStepY() * 0.05,
							target.getLocation().z + blockHit.getDirection().getStepZ() * 0.05, null, 1f);
					return;
				}
				int attr = this.level.isClientSide ? 0
						: (int) TF2Attribute.getModifier("Coll Remove", this.usedWeapon, 0, this.shootingEntity);
				if (attr == 0 && !ForgeEventFactory.onProjectileImpact(this, target)) {
					BlockPos blpos = blockHit.getBlockPos();
					this.onHitGround(blpos.getX(), blpos.getY(), blpos.getZ(), blockHit);
				} else if (attr == 2)
					this.explode(target.getLocation().x, target.getLocation().y, target.getLocation().z, null, 1f);
				else
					this.discard();
			}
		}
		if (flag) {
			WeaponsCapability.get(this.shootingEntity).lastHitCharge = 0;
			if (!this.isRemoved() && (WeaponsCapability.get(this.shootingEntity).state & 2) == 2
					&& TF2Attribute.getModifier("Detonate", usedWeapon, 0, shootingEntity) != 0) {
				this.explode(this.getX() + this.getDeltaMovement().x * 0.5,
						this.getY() + this.getDeltaMovement().y * 0.5, this.getZ() + this.getDeltaMovement().z * 0.5,
						null, 1);
			}
		}
	}

	protected Predicate<Entity> getCollisionPredicate() {
		return TF2Util.TARGETABLE;
	}

	@Override
	public void tick() {
		if (this.tickCount > this.getMaxTime()) {
			this.discard();
			return;
		}

		if (this.getCritical() == -1) {
			this.setCritical(0);
			this.explode(getX(), getY(), getZ(), null, 1f);
			return;
		}

		this.baseTick();

		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			Vec3 motion = this.getDeltaMovement();
			float f = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
			this.setYRot((float) (Math.atan2(motion.x, motion.z) * 180.0D / Math.PI));
			this.setXRot((float) (Math.atan2(motion.y, f) * 180.0D / Math.PI));
			this.yRotO = this.getYRot();
			this.xRotO = this.getXRot();
		}

		if (this.shootingEntity != null)
			this.trace();
		if (this.isRemoved())
			return;

		if (this.isSticked()) {
			this.setPos(this.entityData.get(STICK_X), this.entityData.get(STICK_Y), this.entityData.get(STICK_Z));
			if (!this.level.isClientSide && this.tickCount % 5 == 0
					&& this.level.noCollision(this, this.getBoundingBox().inflate(0.1f, 0.1f, 0.1f)))
				this.setSticked(false);
		}
		if (this.moveable()) {
			if (this.shootingEntity != null && this.getHomingAngle() > 0f && !this.level.isClientSide)
				this.updateHoming();

			Vec3 motion = this.getDeltaMovement();
			if (!this.useCollisionBox())
				this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
			else
				this.moveWithCollision(motion);
			float drag = (float) (1 - this.getGravityOverride() / 5);
			motion = this.getDeltaMovement().scale(drag);
			this.setDeltaMovement(motion.x, motion.y - this.getGravityOverride(), motion.z);
			motion = this.getDeltaMovement();
			float f2 = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
			if (f2 > 0.1 || Math.abs(motion.y) > this.getGravityOverride() * 3) {
				this.setYRot((float) (Math.atan2(motion.x, motion.z) * 180.0D / Math.PI));
				this.setXRot((float) (Math.atan2(motion.y, f2) * 180.0D / Math.PI));

				while (this.getXRot() - this.xRotO < -180.0F)
					this.xRotO -= 360.0F;
				while (this.getXRot() - this.xRotO >= 180.0F)
					this.xRotO += 360.0F;
				while (this.getYRot() - this.yRotO < -180.0F)
					this.yRotO -= 360.0F;
				while (this.getYRot() - this.yRotO >= 180.0F)
					this.yRotO += 360.0F;

				this.setXRot(this.xRotO + (this.getXRot() - this.xRotO) * 0.2F);
				this.setYRot(this.yRotO + (this.getYRot() - this.yRotO) * 0.2F);
			}
		}
		if (this.level.isClientSide) {
			Vec3 motion = this.getDeltaMovement();
			for (int j = 0; j < this.getSpeed(); ++j) {
				double pX = this.getX() - motion.x * j / this.getSpeed() - motion.x;
				double pY = this.getY() + (this.useCollisionBox() ? this.getBbHeight() / 2 : 0)
						- motion.y * j / this.getSpeed() - motion.y;
				double pZ = this.getZ() - motion.z * j / this.getSpeed() - motion.z;
				if (this.getCritical() == 2)
					ClientProxy.spawnCritParticle(this.level, pX, pY, pZ, TF2Util.getTeamColor(this.shootingEntity));
				this.spawnParticles(pX, pY, pZ);
			}
		}
		if (this.isInWaterRainOrBubble())
			this.clearFire();

		if (!this.useCollisionBox())
			this.checkInsideBlocks();
	}

	private void updateHoming() {
		if (this.homingTarget == null || !this.homingTarget.isAlive()) {
			List<Entity> targets = this.level.getEntities(this, this.getBoundingBox().inflate(128),
					entity -> (entity.isAlive() && entity instanceof LivingEntity
							&& TF2Util.isEnemy(shootingEntity, (LivingEntity) entity)
							&& !this.hitEntities.contains(entity)));
			this.homingTarget = TF2Util.getClosestEntityInCone(this.position(),
					this.position().add(this.getDeltaMovement()), targets, this.getHomingAngle());
		}
		if (this.hitEntities.contains(this.homingTarget))
			this.homingTarget = null;
		if (this.homingTarget != null) {
			Vec3 motion = this.getDeltaMovement();
			float sq = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
			float yaw = (float) (Mth.atan2(motion.x, motion.z) * 180.0D / Math.PI);
			float pitch = (float) (Mth.atan2(motion.y, sq) * 180.0D / Math.PI);
			Vec3 offset = new Vec3(this.homingTarget.getX() - this.getX(),
					this.homingTarget.getY() - this.getY() + this.homingTarget.getBbHeight() / 2D,
					this.homingTarget.getZ() - this.getZ());
			float targetsq = (float) Math.sqrt(offset.x * offset.x + offset.z * offset.z);
			float targetyaw = (float) (Mth.atan2(offset.x, offset.z) * 180.0D / Math.PI);
			float targetpitch = (float) (Mth.atan2(offset.y, targetsq) * 180.0D / Math.PI);
			yaw += Mth.clamp(Mth.wrapDegrees(targetyaw - yaw), -this.getHomingTurnSpeed(), this.getHomingTurnSpeed());
			pitch += Mth.clamp(Mth.wrapDegrees(targetpitch - pitch), -this.getHomingTurnSpeed(),
					this.getHomingTurnSpeed());
			// 1.12 used Vec3d.fromPitchYaw with the yaw/pitch convention of motion vectors, so x and y flip.
			offset = Vec3.directionFromRotation(pitch, yaw).scale(motion.length());
			this.hurtMarked = true;
			this.setDeltaMovement(-offset.x, -offset.y, offset.z);
		}
	}

	/**
	 * Moves with block collision. Sticky projectiles stop at the first contact (keeping their
	 * direction) and stick; others slide like vanilla entities.
	 */
	protected void moveWithCollision(Vec3 motion) {
		if (this.isSticky()) {
			AABB box = this.getBoundingBox();
			Vec3 allowed = Entity.collideBoundingBox(this, motion, box, this.level, List.of());
			double limit = 1;
			if (motion.x != 0)
				limit = Math.min(limit, allowed.x / motion.x);
			if (motion.y != 0)
				limit = Math.min(limit, allowed.y / motion.y);
			if (motion.z != 0)
				limit = Math.min(limit, allowed.z / motion.z);
			limit = Math.max(0, limit);
			boolean collided = limit < 1;
			this.setPos(this.getX() + motion.x * limit, this.getY() + motion.y * limit, this.getZ() + motion.z * limit);
			this.horizontalCollision = allowed.x != motion.x || allowed.z != motion.z;
			this.verticalCollision = allowed.y != motion.y;
			this.onGround = this.verticalCollision && motion.y < 0;
			if (collided && !this.level.isClientSide)
				this.setSticked(true);
			if (allowed.x != motion.x)
				this.onHitBlockX();
			if (allowed.z != motion.z)
				this.onHitBlockZ();
			if (allowed.y != motion.y)
				this.onHitBlockY(this.getBlockStateOn().getBlock());
		} else {
			this.move(MoverType.SELF, motion);
			if (this.horizontalCollision) {
				Vec3 after = this.getDeltaMovement();
				if (Math.abs(after.x) < Math.abs(motion.x) * 0.5)
					this.onHitBlockX();
				if (Math.abs(after.z) < Math.abs(motion.z) * 0.5)
					this.onHitBlockZ();
			}
			if (this.verticalCollision)
				this.onHitBlockY(this.getBlockStateOn().getBlock());
		}
		this.stickedBlock = this.getStickPos();
	}

	private BlockPos getStickPos() {
		BlockPos blockpos = new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY() - 0.2), Mth.floor(this.getZ()));
		BlockState state = this.level.getBlockState(blockpos);
		if (state.isAir()) {
			BlockPos below = blockpos.below();
			Block block1 = this.level.getBlockState(below).getBlock();
			if (block1 instanceof FenceBlock || block1 instanceof WallBlock || block1 instanceof FenceGateBlock)
				return below;
		}
		return blockpos;
	}

	@Override
	public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
		if (this.moveable())
			super.lerpTo(x, y, z, yaw, pitch, steps, teleport);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
	}

	public abstract void onHitGround(int x, int y, int z, BlockHitResult mop);

	public abstract void onHitMob(Entity entityHit, HitResult mop);

	@Override
	protected MovementEmission getMovementEmission() {
		return MovementEmission.NONE;
	}

	public boolean moveable() {
		return !this.isSticked();
	}

	public float getHomingAngle() {
		return this.homingAngle;
	}

	public float getHomingTurnSpeed() {
		return this.homingSpeed;
	}

	public void setCritical(int critical) {
		this.entityData.set(CRITICAL, (byte) critical);
	}

	public void setSticked(boolean stick) {
		this.entityData.set(STICK, stick);
		this.entityData.set(STICK_X, (float) this.getX());
		this.entityData.set(STICK_Y, (float) this.getY());
		this.entityData.set(STICK_Z, (float) this.getZ());
	}

	/** 0 none, 1 mini-crit, 2 crit, -1 explode next tick. */
	public int getCritical() {
		return this.entityData.get(CRITICAL);
	}

	public int getProjType() {
		return this.entityData.get(TYPE);
	}

	public boolean isSticked() {
		return this.entityData.get(STICK);
	}

	public boolean canPenetrate() {
		return this.entityData.get(PENETRATE);
	}

	protected float getSpeed() {
		return 3;
	}

	public double getGravity() {
		return 0.0381f;
	}

	public double getGravityOverride() {
		if (this.entityData.get(GRAVITY) == -1f && !this.usedWeapon.isEmpty()) {
			this.entityData.set(GRAVITY, ((ItemWeapon) this.usedWeapon.getItem()).getAdditionalGravity(shootingEntity,
					usedWeapon, this.getGravity()));
		}
		return this.entityData.get(GRAVITY);
	}

	public Entity getThrower() {
		return this.shootingEntity;
	}

	public void setThrower(Entity entity) {
		this.shootingEntity = entity instanceof LivingEntity ? (LivingEntity) entity : null;
		super.setOwner(entity);
	}

	@Override
	public void setOwner(Entity entity) {
		this.setThrower(entity);
	}

	@Override
	public Entity getOwner() {
		return this.shootingEntity != null ? this.shootingEntity : super.getOwner();
	}

	public void setProjType(int type) {
		this.entityData.set(TYPE, (byte) type);
	}

	public void setPenetrate() {
		this.entityData.set(PENETRATE, true);
	}

	public boolean isSticky() {
		return false;
	}

	public void onHitBlockX() {
		this.setDeltaMovement(0, this.getDeltaMovement().y, this.getDeltaMovement().z);
	}

	public void onHitBlockY(Block block) {
		block.updateEntityAfterFallOn(this.level, this);
	}

	public void onHitBlockZ() {
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, 0);
	}

	public abstract void spawnParticles(double x, double y, double z);

	public int getMaxTime() {
		return 1000;
	}

	/** Projectiles are never saved with the world (1.12 writeToNBTOptional returned false). */
	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	public boolean useCollisionBox() {
		return false;
	}

	public float getCollisionSize() {
		return 0.3f;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public boolean hurt(DamageSource source, float damage) {
		if (this.isInvulnerableTo(source)) {
			return false;
		} else if (source.getEntity() instanceof LivingEntity
				&& !TF2Util.isAlliedTo(source.getEntity(), this.shootingEntity)
				&& !(source.isExplosion() || source.isFire())) {
			if (source instanceof TF2DamageSource) {
				damage *= Math.max(1f, TF2Attribute.getModifier("Destroy Projectiles",
						((TF2DamageSource) source).getWeapon(), 0, (LivingEntity) source.getEntity()));
			}
			this.health -= damage;
			if (this.health <= 0)
				this.discard();
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		double d0 = this.getBoundingBox().getSize();
		if (Double.isNaN(d0))
			d0 = 1.0D;
		d0 = d0 * 512.0D * getViewScale();
		return distance < d0 * d0;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		buffer.writeInt((int) (this.getDeltaMovement().x * 8000D));
		buffer.writeInt((int) (this.getDeltaMovement().y * 8000D));
		buffer.writeInt((int) (this.getDeltaMovement().z * 8000D));
		buffer.writeInt(this.shootingEntity != null ? this.shootingEntity.getId() : -1);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf buffer) {
		this.setDeltaMovement(buffer.readInt() / 8000D, buffer.readInt() / 8000D, buffer.readInt() / 8000D);
		int owner = buffer.readInt();
		if (owner >= 0 && this.level.getEntity(owner) instanceof LivingEntity)
			this.shootingEntity = (LivingEntity) this.level.getEntity(owner);
	}

	/** Explosion helper kept for subclasses that use vanilla explosions. */
	protected Explosion.BlockInteraction explosionBlockMode() {
		return Explosion.BlockInteraction.NONE;
	}
}
