package rafradek.TF2weapons.entity.projectile;

import com.google.common.collect.Iterables;
import io.netty.buffer.ByteBuf;
import rafradek.TF2weapons.TF2ConfigVars;
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
import rafradek.TF2weapons.util.TF2Util;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import java.util.function.Predicate;

@Optional.Interface(iface = "atomicstryker.dynamiclights.client.IDynamicLightSource", modid = "dynamiclights", striprefs = true)
public abstract class EntityProjectileBase extends Entity
implements Projectile, Projectile, IDynamicLightSource, IEntityAdditionalSpawnData {
	public HashSet<Entity> hitEntities = new HashSet<>();
	// private Block field_145790_g;
	/** Seems to be some sort of timer for animating an arrow. */
	/** The owner of this arrow. */
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

	private static final EntityDataAccessor<Byte> CRITICAL = SynchedEntityData.createKey(EntityProjectileBase.class,
			EntityDataSerializers.BYTE);
	public static final EntityDataAccessor<Byte> TYPE = SynchedEntityData.createKey(EntityProjectileBase.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> STICK = SynchedEntityData.createKey(EntityProjectileBase.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float> STICK_X = SynchedEntityData.createKey(EntityProjectileBase.class,
			EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> STICK_Y = SynchedEntityData.createKey(EntityProjectileBase.class,
			EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> STICK_Z = SynchedEntityData.createKey(EntityProjectileBase.class,
			EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> GRAVITY = SynchedEntityData.createKey(EntityProjectileBase.class,
			EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> PENETRATE = SynchedEntityData.createKey(EntityProjectileBase.class,
			EntityDataSerializers.BOOLEAN);

	public EntityProjectileBase(Level p_i1753_1_) {
		super(p_i1753_1_);
		this.setSize(0.5F, 0.5F);
	}

	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		this.shootingEntity = shooter;
		this.usedWeapon = weapon.copy();
		this.usedWeaponOrig = weapon;
		this.moveTo(shooter.getX(), shooter.getY() + shooter.getEyeHeight(), shooter.getZ(),
				shooter.yHeadRot, shooter.getXRot() + this.getPitchAddition());
		Vec3 look = Vec3.fromPitchYaw(shooter.getXRot() + this.getPitchAddition(), shooter.yHeadRot)
				.scale(80).add(shooter.getEyePosition(1f));
		Vec3 trace;
		if (shooter instanceof Player) {
			HitResult ray = Iterables.getFirst(TF2Util.pierce(world, shooter, this.getX(), this.getY(), this.getZ(),
					look.x, look.y, look.z, false, 0, false), null);
			trace = ray.getLocation();
		} else
			trace = look;
		this.setPos(this.getX() - (Mth.cos(this.getYRot() / 180.0F * (float) Math.PI) * 0.16F
				* (hand == InteractionHand.MAIN_HAND ? 1 : -1)), this.getY(), this.getZ());
		this.setPos(this.getX(), this.getY() - (shooter instanceof EntitySentry ? -0.1D : (shooter instanceof Player ? 0.1D : 0D)), this.getZ());
		this.setPos(this.getX(), this.getY(), this.getZ() - (Mth.sin(this.getYRot() / 180.0F * (float) Math.PI) * 0.16F
				* (hand == InteractionHand.MAIN_HAND ? 1 : -1)));
		if (trace.lengthSqr() < 2)
			trace = look;
		this.setPos(this.getX(), this.getY(), this.getZ());
		boolean nospread = false;
		if (TF2Attribute.getModifier("Onyx Projectile", this.usedWeapon, 0, shooter) != 0) {
			this.damageModifier = TF2Attribute.getModifier("Onyx Projectile", this.usedWeapon, 0, shooter);
			nospread = true;
		}

		this.shoot(trace.x - this.getX(), trace.y - this.getY(), trace.z - this.getZ(),
				((ItemWeapon) this.usedWeapon.getItem()).getProjectileSpeed(usedWeapon, shooter),
				nospread ? 0
						: ((ItemWeapon) this.usedWeapon.getItem()).getWeaponSpread(usedWeapon, shooter)
						* (133.3333333f));
		if (((ItemWeapon) this.usedWeapon.getItem()).canPenetrate(this.usedWeapon, this.shootingEntity)) {
			this.setPenetrate();
		}

		if (((ItemWeapon) this.usedWeapon.getItem()).holdingMode(this.usedWeapon, shooter) != 0) {
			this.chargeLevel = ((ItemWeapon) this.usedWeapon.getItem()).getCharge(shooter, usedWeapon);
		}
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
	public boolean isImmuneToExplosions() {
		return true;
	}

	/**
	 * Similar to setArrowHeading, it's point the throwable entity to a x, y, z
	 * direction.
	 */
	@Override
	public void shoot(double x, double y, double z, float speed, float spread) {

		if (spread > 0) {
			float xzlen = Mth.sqrt((float)x * x + z * z);
			float yaw = (float) (Mth.atan2(x, z));
			float pitch = (float) (Mth.atan2(y, xzlen));
			Vec3 random = TF2Util.radiusRandom2D(spread * 0.0075f, world.random, yaw, pitch, speed);
			// System.out.println("motion: "+p_70186_1_+" "+p_70186_3_+"
			// "+p_70186_5_+" "+f2);
			x = random.x;
			y = random.y;
			z = random.z;
		} else {
			double len = Mth.sqrt((float)x * x + y * y + z * z);
			x = x / len * speed;
			y = y / len * speed;
			z = z / len * speed;
		}

		this.setDeltaMovement((x), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (y), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (z));

		float f3 = Mth.sqrt((float)x * x + z * z);
		this.yRotO = this.rotationYaw = (float) (Mth.atan2(x, z) * 180.0D / Math.PI);
		this.xRotO = this.rotationPitch = (float) (Mth.atan2(y, f3) * 180.0D / Math.PI);
	}

	public void face(double x, double y, double z, float speedmult) {
		float speed = (float) Math
				.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().y * this.getDeltaMovement().y + this.getDeltaMovement().z * this.getDeltaMovement().z);
		x -= this.getX();
		y -= this.getY();
		z -= this.getZ();
		this.shoot(x, y, z, speed * speedmult, 0);
	}

	public void face(LivingEntity target, float speedmult) {
		float speed = (float) Math
				.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().y * this.getDeltaMovement().y + this.getDeltaMovement().z * this.getDeltaMovement().z);
		double x = target.getX();
		double y = target.getY() + target.getEyeHeight();
		double z = target.getZ();

		speed = speed * speedmult;

		double dist = target.distanceTo(this);
		float gravity = (float) this.getGravityOverride();
		int ticksToReach = Mth.ceil(dist / speed);

		if (ticksToReach != 0) {
			x += target.getDeltaMovement().x * ticksToReach * 1;
			z += target.getDeltaMovement().z * ticksToReach * 1;
			// y += this.velTarget.y * moveTicks;
			// double yFall = !target.onGround? -target.motionY : 0;
			if (!target.isInWater()) {
				int ticksGrav = ticksToReach - 1;
				int i = (ticksGrav * ticksGrav + ticksGrav) / 2;
				y += gravity * i;
				/*
				 * if (!target.onGround && target.motionY < 0) yFall += MathHelper.clamp(0.08,
				 * 0, 0.1) * i;
				 */
				/*
				 * for (int i = 1; i <= ticksToReach; i++) { lookY += gravity * i;
				 *
				 *
				 * }
				 */
			}

			/*
			 * RayTraceResult mop =
			 * this.entityHost.world.rayTraceBlocks(this.attackTarget.getPositionVector(),
			 * this.attackTarget.getPositionVector().addVector(0, -0.3 - yFall, 0)); if (mop
			 * != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) yFall =
			 * this.attackTarget.posY - mop.hitVec.y; shouldFireProj = mop != null ||
			 * this.attackTarget.motionY <= 0f; lookY -= yFall;
			 *
			 * if (this.fireAtFeet > 0 && this.entityHost.world.rayTraceBlocks( new
			 * Vec3d(this.entityHost.posX, this.entityHost.posY +
			 * this.entityHost.getEyeHeight(), this.entityHost.posZ), new Vec3d(lookX,
			 * this.attackTarget.posY, lookZ), false, true, false) == null) { lookY -=
			 * (this.attackTarget.height/2)*this.fireAtFeet;
			 *
			 * }
			 */
		}
		this.face(x, y, z, speedmult);
	}
	/**
	 * Sets the position and rotation. Only difference from the other one is no
	 * bounding on the rotation. Args: posX, posY, posZ, yaw, pitch
	 */
	/*
	 * @SideOnly(Side.CLIENT) public void setPositionAndRotation2(double p_70056_1_,
	 * double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int
	 * p_70056_9_) { this.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
	 * this.setRotation(p_70056_7_, p_70056_8_); }
	 */

	/**
	 * Sets the velocity to the args. Args: x, y, z
	 */
	// @SideOnly(Side.CLIENT)
	@Override
	public void setVelocity(double p_70016_1_, double p_70016_3_, double p_70016_5_) {
		this.setDeltaMovement((p_70016_1_), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (p_70016_3_), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (p_70016_5_));

		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			float f = Mth.sqrt((float)p_70016_1_ * p_70016_1_ + p_70016_5_ * p_70016_5_);
			this.yRotO = this.rotationYaw = (float) (Mth.atan2(p_70016_1_, p_70016_5_) * 180.0D
					/ Math.PI);
			this.xRotO = this.rotationPitch = (float) (Mth.atan2(p_70016_3_, f) * 180.0D / Math.PI);
			this.xRotO = this.getXRot();
			this.yRotO = this.getYRot();
			this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
		}
	}

	public void explode(double x, double y, double z, Entity direct, float damageMult) {
		if (world.isClientSide || this.shootingEntity == null)
			return;
		this.discard();
		float size = this.getExplosionSize()
				* TF2Attribute.getModifier("Explosion Radius", this.usedWeapon, 1, this.shootingEntity);
		if (TF2Attribute.getModifier("Airborne Bonus", this.usedWeapon, 0, this.shootingEntity) != 0)
			size *= 0.8f;
		TF2Util.explosion(world, shootingEntity, usedWeapon, this, direct, x, y, z, size,
				damageMult * this.damageModifier, this.getCritical(), this.getDistanceToTarget(null, x, y, z));
	}

	public SoundEvent getExplosionSound() {
		return ItemFromData.getSound(this.usedWeapon, PropertyType.EXPLOSION_SOUND);
	}

	public void addDamageTypes(DamageSource source) {

	}

	public boolean attackDirect(Entity target, double pushForce, boolean headshot, Vec3 hitPos) {
		if (!this.level.isClientSide) {
			if (target != null && shootingEntity != null && target.getTeam() != null && target.getTeam().equals(shootingEntity.getTeam()) &!
					(target == shootingEntity || (shootingEntity instanceof OwnableEntity && ((OwnableEntity) shootingEntity).getOwner() == target))) return false;
			if(!this.hitEntities.contains(target)) {
				this.hitEntities.add(target);
				float distance = this.getDistanceToTarget(target, hitPos.x, hitPos.y, hitPos.z);
				DamageSourceProjectile src = TF2Util.causeBulletDamage(this.usedWeapon, this.shootingEntity, this);
				int critical = TF2Util.calculateCritPost(
						target, shootingEntity, headshot ? ((ItemWeapon) this.usedWeapon.getItem())
								.getHeadshotCrit(shootingEntity, this.usedWeapon) : this.getCritical(),
								this.usedWeapon, src);
				float dmg = TF2Util.calculateDamage(target, world, this.shootingEntity, usedWeapon, critical, distance)
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
						Vec3 pushvec = new Vec3(target.getX() - hitPos.x, target.getY() + target.getBbHeight() / 2 - hitPos.y,
								target.getZ() - hitPos.z).normalize();
						pushvec = pushvec.scale(((ItemWeapon) this.usedWeapon.getItem())
								.getKnockbackForDamage(usedWeapon, shootingEntity, dmg, src));
						if (target instanceof LivingEntity) {
							pushvec = pushvec.scale(1 - ((LivingEntity) target).getAttributes()
									.getAttributeInstance(Attributes.KNOCKBACK_RESISTANCE)
									.getValue());
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
			return (float) new Vec3(this.shootingEntity.getX(), this.shootingEntity.getY(), this.shootingEntity.getZ())
					.distanceTo(new Vec3(x, y, z));
		else
			return (float) TF2Util.getDistanceBox(this.shootingEntity, target.getX(), target.getY(), target.getZ(),
					target.getBbWidth() + 0.1, target.getBbHeight() + 0.1);
	}

	@Override
	public Entity changeDimension(int dimensionId) {
		return null;
	}

	public float getExplosionSize() {
		return 2.8f;
	}

	public void trace() {
		boolean flag = this.shootingEntity.getCapability(TF2weapons.WEAPONS_CAP).isPresent();
		if (flag) {
			WeaponsCapability.get(this.shootingEntity).lastHitCharge = this.chargeLevel;
		}
		boolean headshot = this.usedWeapon.isEmpty() || !flag ? false
				: ((ItemWeapon) this.usedWeapon.getItem()).canHeadshot(this.shootingEntity, this.usedWeapon);
		for (HitResult target : TF2Util.pierce(this.level, this.shootingEntity, this.getX(), this.getY(), this.getZ(),
				this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z, headshot,
				this.getCollisionSize(), this.canPenetrate(), this.getCollisionPredicate())) {

			if (target.getEntity() != null && target.getEntity() instanceof Player) {
				Player entityplayer = (Player) target.getEntity();

				if (entityplayer.getAbilities().disableDamage/*
				 * || this.shootingEntity instanceof EntityPlayer &&
				 * !((EntityPlayer)
				 * this.shootingEntity).canAttackPlayer(entityplayer)
				 */)
					continue;
			}

			if (target.getEntity() != null && !ForgeEventFactory.onProjectileImpact(this, target)
					&& !(TF2Util.isAlliedTo(this.shootingEntity, target.getEntity())
							&& (this.tickCount < 3 && !world.isClientSide && ((ItemWeapon) this.usedWeapon.getItem())
									.onHit(usedWeapon, shootingEntity, target.getEntity(), 1, 0, true)))) {
				this.onHitMob(target.getEntity(), target);
			}

			else if (target.getType() == Type.BLOCK && !this.useCollisionBox()) {
				if (TF2Attribute.getModifier("Detonate", usedWeapon, 0, shootingEntity) != 0) {
					TF2Attribute.setAttribute(usedWeapon, TF2Attribute.attributes[0], 0);
					this.explode(target.getLocation().x + target.getDirection().getStepX() * 0.05,
							target.getLocation().y + target.getDirection().getStepY() * 0.05,
							target.getLocation().z + target.getDirection().getStepZ() * 0.05, null, 1f);
					return;
				}
				int attr = this.level.isClientSide ? 0
						: (int) TF2Attribute.getModifier("Coll Remove", this.usedWeapon, 0, this.shootingEntity);
				if (attr == 0 && !ForgeEventFactory.onProjectileImpact(this, target)) {
					BlockPos blpos = target.getBlockPos();
					this.onHitGround(blpos.getX(), blpos.getY(), blpos.getZ(), target);
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
				this.explode(this.getX() + this.getDeltaMovement().x * 0.5, this.getY() + this.getDeltaMovement().y * 0.5,
						this.getZ() + this.getDeltaMovement().z * 0.5, null, 1);
				return;
			}
		}
	}

	protected Predicate<Entity> getCollisionPredicate() {
		return TF2Util.TARGETABLE;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		if (this.tickCount > this.getMaxTime()) {
			this.discard();
			return;
		}

		if (this.getCritical() == -1) {
			this.setCritical(0);
			this.explode(getX(), getY(), getZ(), null, 1f);
			return;
		}

		super.onUpdate();

		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			float f = Mth.sqrt((float)this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z);
			this.yRotO = this.rotationYaw = (float) (Math.atan2(this.getDeltaMovement().x, this.getDeltaMovement().z) * 180.0D
					/ Math.PI);
			this.xRotO = this.rotationPitch = (float) (Math.atan2(this.getDeltaMovement().y, f) * 180.0D / Math.PI);
		}

		if (this.shootingEntity != null) {

			this.trace();

		}
		float f2;
		if (this.isSticked()) {
			this.setPos(this.entityData.get(STICK_X), this.entityData.get(STICK_Y),
					this.entityData.get(STICK_Z));
			if (!this.level.isClientSide && this.tickCount % 5 == 0
					&& this.level.getCollisionBoxes(this, this.getBoundingBox().inflate(0.1f, 0.1f, 0.1f)).isEmpty())
				this.setSticked(false);
		}
		if (this.moveable()) {
			if (this.shootingEntity != null && this.getHomingAngle() > 0f && !this.level.isClientSide) {
				if (this.homingTarget == null || !this.homingTarget.isAlive()) {
					List<Entity> targets = this.level.getEntities(this,
							this.getBoundingBox().inflate(128),
							entity -> (entity.isAlive() && entity instanceof LivingEntity
									&& TF2Util.isEnemy(shootingEntity, (LivingEntity) entity)
									&& !this.hitEntities.contains(entity)));
					this.homingTarget = TF2Util.getClosestEntityInCone(this.position(),
							this.position().add(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z), targets,
							this.getHomingAngle());
				}
				if (this.hitEntities.contains(this.homingTarget)) {
					this.homingTarget = null;
				}
				if (this.homingTarget != null) {
					float sq = Mth.sqrt((float)this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z);
					float yaw = (float) (Mth.atan2(this.getDeltaMovement().x, this.getDeltaMovement().z) * 180.0D / Math.PI);
					float pitch = (float) (Mth.atan2(this.getDeltaMovement().y, sq) * 180.0D / Math.PI);
					Vec3 offset = new Vec3(this.homingTarget.getX() - this.getX(),
							this.homingTarget.getY() - this.getY() + this.homingTarget.getBbHeight() / 2D,
							this.homingTarget.getZ() - this.getZ());
					float targetsq = Mth.sqrt((float)offset.x * offset.x + offset.z * offset.z);
					float targetyaw = (float) (Mth.atan2(offset.x, offset.z) * 180.0D / Math.PI);
					float targetpitch = (float) (Mth.atan2(offset.y, targetsq) * 180.0D / Math.PI);
					yaw += Mth.clamp(Mth.wrapDegrees(targetyaw - yaw), -this.getHomingTurnSpeed(),
							this.getHomingTurnSpeed());
					pitch += Mth.clamp(Mth.wrapDegrees(targetpitch - pitch), -this.getHomingTurnSpeed(),
							this.getHomingTurnSpeed());
					offset = Vec3.fromPitchYaw(pitch, yaw).scale(Mth.sqrt((float)
							this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().y * this.getDeltaMovement().y + this.getDeltaMovement().z * this.getDeltaMovement().z));
					this.hurtMarked = true;
					this.setDeltaMovement((-offset.x), this.getDeltaMovement().y, this.getDeltaMovement().z);
					this.setDeltaMovement(this.getDeltaMovement().x, (-offset.y), this.getDeltaMovement().z);
					this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (offset.z));
				}

			}

			if (!this.useCollisionBox()) {
				this.setPos(this.getX() + (this.getDeltaMovement().x), this.getY(), this.getZ());
				this.setPos(this.getX(), this.getY() + (this.getDeltaMovement().y), this.getZ());
				this.setPos(this.getX(), this.getY(), this.getZ() + (this.getDeltaMovement().z));
			} else
				this.move(MoverType.SELF, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
			float f3 = (float) (1 - this.getGravityOverride() / 5);
			this.setDeltaMovement(this.getDeltaMovement().x * (f3), this.getDeltaMovement().y, this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * (f3), this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z * (f3));
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y - (this.getGravityOverride()), this.getDeltaMovement().z);
			f2 = Mth.sqrt((float)this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z);
			if (f2 > 0.1 || Math.abs(this.getDeltaMovement().y) > this.getGravityOverride() * 3) {
				this.setYRot(((float) (Math.atan2(this.getDeltaMovement().x, this.getDeltaMovement().z) * 180.0D / Math.PI)));

				for (this.rotationPitch = (float) (Math.atan2(this.getDeltaMovement().y, f2) * 180.0D / Math.PI); this.getXRot()
						- this.xRotO < -180.0F; this.xRotO -= 360.0F)
					;

				while (this.getXRot() - this.xRotO >= 180.0F)
					this.xRotO += 360.0F;

				while (this.getYRot() - this.yRotO < -180.0F)
					this.yRotO -= 360.0F;

				while (this.getYRot() - this.yRotO >= 180.0F)
					this.yRotO += 360.0F;

				this.setXRot((this.xRotO + (this.getXRot() - this.xRotO) * 0.2F));
				this.setYRot((this.yRotO + (this.getYRot() - this.yRotO) * 0.2F));
			}
		}
		if (this.level.isClientSide)
			for (int j = 0; j < this.getSpeed(); ++j) {
				double pX = this.getX() - this.getDeltaMovement().x * j / this.getSpeed() - this.getDeltaMovement().x;
				double pY = this.getY() + (this.useCollisionBox() ? this.getBbHeight() / 2 : 0)
						- this.getDeltaMovement().y * j / this.getSpeed() - this.getDeltaMovement().y;
				double pZ = this.getZ() - this.getDeltaMovement().z * j / this.getSpeed() - this.getDeltaMovement().z;
				if (this.getCritical() == 2)
					ClientProxy.spawnCritParticle(this.level, pX, pY, pZ, TF2Util.getTeamColor(this.shootingEntity));
				this.spawnParticles(pX, pY, pZ);

			}
		if (this.isInWaterRainOrBubble())
			this.clearFire();

		if (!this.useCollisionBox()) {
			this.setPos(this.getX(), this.getY(), this.getZ());
			this.doBlockCollisions();
		}
	}

	// @SideOnly(Side.CLIENT)
	@Override
	public void setPositionAndRotationDirect(double p_180426_1_, double p_180426_3_, double p_180426_5_,
			float p_180426_7_, float p_180426_8_, int p_180426_9_, boolean p_180426_10_) {
		if (this.moveable())
			super.setPositionAndRotationDirect(p_180426_1_, p_180426_3_, p_180426_5_, p_180426_7_, p_180426_8_,
					p_180426_9_, p_180426_10_);
	}

	/*
	 * public void setPosition(double x, double y, double z) { this.posX = x;
	 * this.posY = y; this.posZ = z; float f = this.width / 2f; float f1 =
	 * this.height /2f; this.setEntityBoundingBox(new AxisAlignedBB(x - (double)f,
	 * y-(double)f1, z - (double)f, x + (double)f, y + (double)f1, z + (double)f));
	 * }
	 */

	@Override
	public void move(MoverType type, double x, double y, double z) {
		if (this.noPhysics) {
			this.setBoundingBox(this.getBoundingBox().move(x, y, z));
			this.resetPositionToBB();
		} else {
			this.level.profiler.startSection("move");
			// double d3 = this.posX;
			// double d4 = this.posY;
			// double d5 = this.posZ;

			if (this.isInPowderSnow) {
				this.isInPowderSnow = false;
				x *= 0.25D;
				y *= 0.05000000074505806D;
				z *= 0.25D;
				this.setDeltaMovement((0.0D), this.getDeltaMovement().y, this.getDeltaMovement().z);
				this.setDeltaMovement(this.getDeltaMovement().x, (0.0D), this.getDeltaMovement().z);
				this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (0.0D));
			}

			double d3 = x;
			double d4 = y;
			double d5 = z;

			List<AABB> list1 = this.level.getCollisionBoxes(this, this.getBoundingBox().expandTowards(x, y, z));
			AABB axisalignedbb = this.getBoundingBox();
			int i = 0;

			for (int j = list1.size(); i < j; ++i)
				y = list1.get(i).calculateYOffset(this.getBoundingBox(), y);

			double limit = y / d4;
			if (!this.isSticky())
				this.setBoundingBox(this.getBoundingBox().move(0.0D, y, 0.0D));
			boolean i_ = this.onGround || d4 != y && d4 < 0.0D;
			int j4 = 0;

			for (int k = list1.size(); j4 < k; ++j4)
				x = list1.get(j4).calculateXOffset(this.getBoundingBox(), x);

			if (this.isSticky()) {
				if (x / d3 < limit)
					limit = x / d3;
			} else
				this.setBoundingBox(this.getBoundingBox().move(x, 0.0D, 0.0D));
			j4 = 0;

			for (int k4 = list1.size(); j4 < k4; ++j4)
				z = list1.get(j4).calculateZOffset(this.getBoundingBox(), z);

			if (this.isSticky()) {
				if (z / d5 < limit)
					limit = z / d5;
				this.setBoundingBox(this.getBoundingBox().move(d3 * limit, d4 * limit, d5 * limit));
			} else
				this.setBoundingBox(this.getBoundingBox().move(0.0D, 0.0D, z));

			if (this.maxUpStep > 0.0F && i_ && (d3 != x || d5 != z)) {
				double d11 = x;
				double d7 = y;
				double d8 = z;
				AABB axisalignedbb1 = this.getBoundingBox();
				this.setBoundingBox(axisalignedbb);
				y = this.maxUpStep;
				List<AABB> list = this.level.getCollisionBoxes(this,
						this.getBoundingBox().expandTowards(d3, y, d5));
				AABB axisalignedbb2 = this.getBoundingBox();
				AABB axisalignedbb3 = axisalignedbb2.expandTowards(d3, 0.0D, d5);
				double d9 = y;
				int l = 0;

				for (int i1 = list.size(); l < i1; ++l)
					d9 = list.get(l).calculateYOffset(axisalignedbb3, d9);

				axisalignedbb2 = axisalignedbb2.move(0.0D, d9, 0.0D);
				double d15 = d3;
				int j1 = 0;

				for (int k1 = list.size(); j1 < k1; ++j1)
					d15 = list.get(j1).calculateXOffset(axisalignedbb2, d15);

				axisalignedbb2 = axisalignedbb2.move(d15, 0.0D, 0.0D);
				double d16 = d5;
				int l1 = 0;

				for (int i2 = list.size(); l1 < i2; ++l1)
					d16 = list.get(l1).calculateZOffset(axisalignedbb2, d16);

				axisalignedbb2 = axisalignedbb2.move(0.0D, 0.0D, d16);
				AABB axisalignedbb4 = this.getBoundingBox();
				double d17 = y;
				int j2 = 0;

				for (int k2 = list.size(); j2 < k2; ++j2)
					d17 = list.get(j2).calculateYOffset(axisalignedbb4, d17);

				axisalignedbb4 = axisalignedbb4.move(0.0D, d17, 0.0D);
				double d18 = d3;
				int l2 = 0;

				for (int i3 = list.size(); l2 < i3; ++l2)
					d18 = list.get(l2).calculateXOffset(axisalignedbb4, d18);

				axisalignedbb4 = axisalignedbb4.move(d18, 0.0D, 0.0D);
				double d19 = d5;
				int j3 = 0;

				for (int k3 = list.size(); j3 < k3; ++j3)
					d19 = list.get(j3).calculateZOffset(axisalignedbb4, d19);

				axisalignedbb4 = axisalignedbb4.move(0.0D, 0.0D, d19);
				double d20 = d15 * d15 + d16 * d16;
				double d10 = d18 * d18 + d19 * d19;

				if (d20 > d10) {
					x = d15;
					z = d16;
					y = -d9;
					this.setBoundingBox(axisalignedbb2);
				} else {
					x = d18;
					z = d19;
					y = -d17;
					this.setBoundingBox(axisalignedbb4);
				}

				int l3 = 0;

				for (int i4 = list.size(); l3 < i4; ++l3)
					y = list.get(l3).calculateYOffset(this.getBoundingBox(), y);

				this.setBoundingBox(this.getBoundingBox().move(0.0D, y, 0.0D));

				if (d11 * d11 + d8 * d8 >= x * x + z * z) {
					x = d11;
					y = d7;
					z = d8;
					this.setBoundingBox(axisalignedbb1);
				}
			}

			this.level.profiler.endSection();
			this.level.profiler.startSection("rest");
			this.resetPositionToBB();
			this.horizontalCollision = d3 != x || d5 != z;
			this.verticalCollision = d4 != y;
			this.onGround = this.verticalCollision && d4 < 0.0D;
			this.collided = this.horizontalCollision || this.verticalCollision;
			if (this.isSticky() && !this.level.isClientSide && this.collided)
				this.setSticked(true);
			j4 = Mth.floor(this.getX());
			int l4 = Mth.floor(this.getY() - 0.20000000298023224D);
			int i5 = Mth.floor(this.getZ());
			BlockPos blockpos = new BlockPos(j4, l4, i5);
			BlockState iblockstate = this.level.getBlockState(blockpos);

			if (iblockstate.getMaterial() == Material.AIR) {
				BlockPos blockpos1 = blockpos.below();
				BlockState iblockstate1 = this.level.getBlockState(blockpos1);
				Block block1 = iblockstate1.getBlock();

				if (block1 instanceof FenceBlock || block1 instanceof WallBlock || block1 instanceof FenceGateBlock) {
					iblockstate = iblockstate1;
					blockpos = blockpos1;
				}
			}
			this.stickedBlock = blockpos;
			this.updateFallState(y, this.onGround, iblockstate, blockpos);
			/*
			 * double limit=y/d7; if(!this.isSticky()){
			 * this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0. 0D, y,
			 * 0.0D)); } if(this.isSticky()){ if(x/d6<limit){ limit=x/d6; } }
			 * if(this.isSticky()){ if(z/d8<limit){ limit=z/d8; }
			 * this.setEntityBoundingBox(this.getEntityBoundingBox().offset(d6*
			 * limit,d7*limit,d8*limit)); }
			 */

			if (d3 != x)
				this.onHitBlockX();

			if (d5 != z)
				this.onHitBlockZ();
			Block block = iblockstate.getBlock();
			if (d4 != y)
				this.onHitBlockY(block);

			if (this.canTriggerWalking() && this.getVehicle() == null)
				/*
				 * double d15 = this.posX - d3; double d16 = this.posY - d4; double d17 =
				 * this.posZ - d5;
				 *
				 * if (block1 != Blocks.ladder) { d16 = 0.0D; }
				 */
				if (block != null && this.onGround)
					block.onEntityCollidedWithBlock(this.level, blockpos, iblockstate, this);

			try {
				this.doBlockCollisions();
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
				CrashReportCategory crashreportcategory = crashreport
						.makeCategory("Entity being checked for collision");
				this.addEntityCrashInfo(crashreportcategory);
				throw new ReportedException(crashreport);
			}

			this.level.profiler.endSection();
		}
	}

	/*
	 * public void resetPositionToBB() { AxisAlignedBB axisalignedbb =
	 * this.getEntityBoundingBox(); this.posX = (axisalignedbb.minX +
	 * axisalignedbb.maxX) / 2.0D; this.posY = (axisalignedbb.minY +
	 * axisalignedbb.maxY) / 2.0D; this.posZ = (axisalignedbb.minZ +
	 * axisalignedbb.maxZ) / 2.0D; }
	 */
	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	@Override
	public void addAdditionalSaveData(CompoundTag p_70014_1_) {

	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditionalSaveData(CompoundTag p_70037_1_) {

	}

	public abstract void onHitGround(int x, int y, int z, HitResult mop);

	public abstract void onHitMob(Entity entityHit, HitResult mop);

	@Override
	protected boolean canTriggerWalking() {
		return false;
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
		/*
		 * if(!this.world.isRemote){ EntityTracker
		 * tracker=((WorldServer)this.world).getEntityTracker();
		 * tracker.sendToAllTrackingEntity(this, new S12PacketEntityVelocity(this));
		 * tracker.sendToAllTrackingEntity(this, new S18PacketEntityTeleport(this)); }
		 */
	}

	/**
	 * Whether the arrow has a stream of critical hit particles flying behind it.
	 */
	public int getCritical() {
		return this.entityData.get(CRITICAL);
	}

	public int getType() {
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

	@Override
	public Entity getThrower() {
		return this.shootingEntity;
	}

	@Override
	public void setThrower(Entity entity) {
		this.shootingEntity = (LivingEntity) entity;

	}

	public void setType(int type) {
		this.entityData.set(TYPE, (byte) type);
	}

	public void setPenetrate() {
		this.entityData.set(PENETRATE, true);
	}

	public boolean isSticky() {
		return false;
	}

	public void onHitBlockX() {
		this.setDeltaMovement((0), this.getDeltaMovement().y, this.getDeltaMovement().z);
	}

	public void onHitBlockY(Block block) {
		block.onLanded(this.level, this);
	}

	public void onHitBlockZ() {
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (0));
	}

	public abstract void spawnParticles(double x, double y, double z);

	public int getMaxTime() {
		return 1000;
	}

	@Override
	public boolean writeToNBTOptional(CompoundTag tagCompund) {
		return false;
	}

	public boolean useCollisionBox() {
		return false;
	}

	public float getCollisionSize() {
		return 0.3f;
	}

	public boolean isPushable() {
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float damage) {
		if (this.isInvulnerableTo(source)) {
			return false;
		} else if (source.getEntity() != null && source.getEntity() instanceof LivingEntity
				&& !TF2Util.isAlliedTo(source.getEntity(), this.shootingEntity)
				&& !(source.isExplosion() || source.isFireDamage())) {
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
	public boolean isInRangeToRenderDist(double distance) {
		double d0 = this.getBoundingBox().getAverageEdgeLength();

		if (Double.isNaN(d0))
			d0 = 1.0D;

		d0 = d0 * 512.0D * getRenderDistanceWeight();
		return distance < d0 * d0;
	}

	@Optional.Method(modid = "dynamiclights")
	public void makeLit() {
		if (TF2ConfigVars.dynamicLightsProj)
			DynamicLights.addLightSource(this);
	}

	@Optional.Method(modid = "dynamiclights")
	@Override
	public Entity getAttachmentEntity() {
		return this;
	}

	@Optional.Method(modid = "dynamiclights")
	@Override
	public int getLightLevel() {
		return 9;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeInt((int) (this.getDeltaMovement().x * 8000D));
		buffer.writeInt((int) (this.getDeltaMovement().y * 8000D));
		buffer.writeInt((int) (this.getDeltaMovement().z * 8000D));
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		this.setDeltaMovement((buffer.readInt() / 8000D), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (buffer.readInt() / 8000D), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (buffer.readInt() / 8000D));
	}

}