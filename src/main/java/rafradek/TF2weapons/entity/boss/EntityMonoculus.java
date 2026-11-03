package rafradek.TF2weapons.entity.boss;

import javax.annotation.Nullable;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.core.particles.DustParticleOptions;
import rafradek.TF2weapons.common.TF2Achievements;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemProjectileWeapon;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2Util;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityMonoculus extends EntityTF2Boss {

	public static final int LASER_DURATION = 90;
	public static final int LASER_START = 60;
	public int teleport = 200;
	public int angryTicks = 0;
	public int begin = 30;
	public float toAngry = 0;
	public int laserTime;
	public int laserCooldown = 150;

	private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(EntityMonoculus.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> LASER = SynchedEntityData.defineId(EntityMonoculus.class,
			EntityDataSerializers.BOOLEAN);

	public EntityMonoculus(Level world) {
		this(TF2weapons.MONOCULUS, world);
	}

	public EntityMonoculus(EntityType<? extends EntityMonoculus> type, Level world) {
		super(type, world);
		this.setSize(4, 4);
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemFromData.getNewStack("mnceye"));
		this.setItemInHand(InteractionHand.OFF_HAND, ItemFromData.getNewStack("mnclaser"));
		this.moveControl = new MonoculusMoveHelper(this);
	}

	@Override
	public void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ANGRY, false);
		this.entityData.define(LASER, false);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(5, new AIRandomFly(this));
		this.goalSelector.addGoal(3, new AILaserAttack(this));
		this.goalSelector.addGoal(6, new AIFireballAttack(this));
		this.goalSelector.addGoal(7, new AILookAround(this));

		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class,
				0, true, false, input -> input instanceof Player || input instanceof EntityTF2Character) {
			@Override
			protected double getFollowDistance() {
				return super.getFollowDistance() * 0.5;
			}
		});
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 5, false, false,
				input -> input instanceof Player));
	}

	@Override
	protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {}

	public boolean isAngry() {
		return this.getEntityData().get(ANGRY);
	}

	public void setAngry(int ticks) {
		this.angryTicks = ticks;
		boolean angry = ticks > 0;
		this.toAngry = 0;
		if (angry)
			TF2Attribute.setAttribute(this.getMainHandItem(), TF2Attribute.attributes[19],
					3 * (0.75f + this.bossLevel * 0.25f));
		else
			TF2Attribute.setAttribute(this.getMainHandItem(), TF2Attribute.attributes[19],
					1 * (0.75f + this.bossLevel * 0.25f));

		this.getEntityData().set(ANGRY, angry);
	}

	public boolean isLaser() {
		return this.getEntityData().get(LASER);
	}

	public void setLaser(boolean laser) {
		this.getEntityData().set(LASER, laser);
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {

		if (super.hurt(source, amount)) {
			if (source instanceof TF2DamageSource) {
				if (((TF2DamageSource) source).getCritical() > 0) {
					if (!this.isAngry())
						this.setAngry(160);
				}
			} else {
				this.toAngry += amount;
				if (this.toAngry >= 40 * (1 + bossLevel * 0.08f)) {
					this.setAngry(120);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public SoundEvent getDeathSound() {
		return TF2Sounds.MOB_MONOCULUS_DEFEAT;
	}

	@Override
	public SoundEvent getAppearSound() {
		return TF2Sounds.MOB_MONOCULUS_START;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putShort("Begin", (short) this.begin);
		nbt.putShort("Angry", (short) this.angryTicks);
		nbt.putShort("Teleport", (short) this.teleport);
		nbt.putShort("LaserCooldown", (short) this.laserCooldown);

	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		this.begin = nbt.getShort("Begin");
		this.angryTicks = nbt.getShort("Angry");
		this.setAngry(this.angryTicks);
		this.teleport = nbt.getShort("Teleport");
		this.laserCooldown = nbt.getShort("LaserCooldown");
	}

	/** Ghast-style flying movement. */
	@Override
	public void travel(Vec3 travel) {
		if (this.isInWater()) {
			this.moveRelative(0.02F, travel);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
		} else if (this.isInLava()) {
			this.moveRelative(0.02F, travel);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
		} else {
			BlockPos ground = this.getBlockPosBelowThatAffectsMyMovement();
			float f = 0.91F;
			if (this.onGround)
				f = this.level.getBlockState(ground).getFriction(this.level, ground, this) * 0.91F;
			float f1 = 0.16277137F / (f * f * f);
			f = 0.91F;
			if (this.onGround)
				f = this.level.getBlockState(ground).getFriction(this.level, ground, this) * 0.91F;
			this.moveRelative(this.onGround ? 0.1F * f1 : 0.02F, travel);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(f));
		}
		this.calculateEntityAnimation(this, false);
	}

	@Override
	public boolean onClimbable() {
		return false;
	}

	@Override
	public void aiStep() {
		if (this.getMainHandItem().isEmpty() || !(this.getMainHandItem().getItem() instanceof ItemWeapon))
			this.setItemInHand(InteractionHand.MAIN_HAND, ItemFromData.getNewStack("mnceye"));

		super.aiStep();
		if (this.begin-- > 20 && this.level.isClientSide)
			for (int i = 0; i < 40; i++) {
				Vec3 pos = TF2Util.radiusRandom2D(2.7f, this.random);
				this.level.addParticle(DustParticleOptions.REDSTONE, pos.x + this.getX(), this.getY() - 0.5,
						pos.y + this.getZ(), 0.55, 0.12, 1);
			}
		if (this.tickCount == 1) {

		}

		this.laserTime--;
		if (this.level.isClientSide) {
			this.noCulling = this.isLaser();
		}
		// System.out.println(" "+this.rotationPitch);
		if (!this.level.isClientSide) {

			if (this.begin == 0) {
				this.setNoAi(false);
				this.getAttribute(Attributes.ARMOR).removeModifier(BOSS_ARMOR_SPAWN);
			}
			if (this.getTarget() == null) {
				double f = (float) Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z);
				this.setYRot((-((float) Mth.atan2(this.getDeltaMovement().x, this.getDeltaMovement().z)) * (180F / (float) Math.PI)));
				this.setXRot((-((float) Mth.atan2(this.getDeltaMovement().y, f)) * (180F / (float) Math.PI)));
				this.yBodyRot = this.getYRot();
				this.yHeadRot = this.getYRot();
			} else {
				LivingEntity entitylivingbase = this.getTarget();
				double d0 = 64.0D;

				if (entitylivingbase.distanceToSqr(this) < 4096.0D) {
					double d1 = entitylivingbase.getX() - this.getX();
					double d2 = entitylivingbase.getZ() - this.getZ();
					double d3 = entitylivingbase.getY() - (this.getY() + this.getEyeHeight());
					if (this.isLaser())
						d3 += entitylivingbase.getEyeHeight();
					double f = (float) Math.sqrt(d1 * d1 + d2 * d2);
					float clamp = this.isLaser() ? 1.35f : 10f;
					float yaw = -((float) Mth.atan2(d1, d2)) * (180F / (float) Math.PI);
					float pitch = -((float) Mth.atan2(d3, f)) * (180F / (float) Math.PI);
					yaw = Mth.clamp(Mth.wrapDegrees(yaw - this.yHeadRot), -clamp, clamp);
					pitch = Mth.clamp(Mth.wrapDegrees(pitch - this.getXRot()), -clamp * 33,
							clamp * 33);
					this.setYRot((this.getYRot() + yaw));
					this.yBodyRot = this.getYRot();
					this.setXRot((this.getXRot() + pitch));
					this.yHeadRot = this.getYRot();
				}
			}

			if (this.bossLevel > 1)
				this.laserCooldown--;

			if (this.tickCount % 20 == 0 && !this.isAngry() && this.random.nextInt(20) == 0)
				this.setAngry(100);

			if (this.tickCount % 5 == 0) {
				if (this.getTarget() != null && this.level.clip(new net.minecraft.world.level.ClipContext(this.getEyePosition(1),
						this.getTarget().position(), net.minecraft.world.level.ClipContext.Block.COLLIDER,
						net.minecraft.world.level.ClipContext.Fluid.NONE, this))
						.getType() != HitResult.Type.MISS) {
					TF2Attribute.setAttribute(this.getMainHandItem(), TF2Attribute.attributes[39],
							this.isAngry() ? 0.8f : 0.45f);
					TF2Attribute.setAttribute(this.getOffhandItem(), TF2Attribute.attributes[39], 0f);
				} else {
					TF2Attribute.setAttribute(this.getMainHandItem(), TF2Attribute.attributes[39], 0f);
					// TF2Attribute.setAttribute(this.getHeldItemOffhand(),
					// TF2Attribute.attributes[39], 0f);
				}
			}
			this.toAngry = Math.max(0, this.toAngry - 0.4f);
			if (this.isAngry() && --this.angryTicks <= 0)
				this.setAngry(0);
			if (--this.teleport <= 0 && !this.isLaser())
				for (int i = 0; i < 10; i++) {

					double x = this.getX() + random.nextDouble() * 48 - 24;
					double z = this.getZ() + random.nextDouble() * 48 - 24;
					double y = this.getY() + random.nextDouble() * 48 - 24;
					y = Math.min(y, this.level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, y, z)).getY() + 16);
					if (this.randomTeleport(x, y, z, false)) {
						for (int j = 0; j < 40; j++) {
							Vec3 pos = TF2Util.radiusRandom3D(2.7f, this.random);
							this.level.addParticle(ParticleTypes.PORTAL, pos.x + this.getX(), pos.y + this.getY(),
									pos.z + this.getZ(), 0, 0, 0);
						}
						this.teleport += 160 + random.nextInt(80);
						break;
					}
				}
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return EntityTF2Boss.createAttributes()
				.add(Attributes.FOLLOW_RANGE, 256.0D)
				.add(Attributes.MAX_HEALTH, 160)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.11D)
				.add(Attributes.ATTACK_DAMAGE, 7D);
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance diff, MobSpawnType spawnReason, @Nullable SpawnGroupData p_110161_1_, @Nullable CompoundTag spawnTag) {
		p_110161_1_ = super.finalizeSpawn(levelAccessor, diff, spawnReason, p_110161_1_, spawnTag);
		this.getAttribute(Attributes.MOVEMENT_SPEED)
				.setBaseValue(this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue()
						* (0.95 + this.bossLevel * 0.05));
		return p_110161_1_;
	}

	@Override
	public void dropFewItems(boolean hit, int looting) {
		if (this.random.nextBoolean()) {
			ItemStack bomb = ItemFromData.getNewStack("bombinomicon");
			TF2Attribute.setAttribute(bomb, MapList.nameToAttribute.get("BombEnemy"), 1.45f + this.bossLevel * 0.2f);
			this.spawnAtLocation(bomb, 0);
		}
		ItemStack hat = ItemFromData.getNewStack("monoculus");
		hat.getTag().putShort("BossLevel", (short) this.bossLevel);
		this.spawnAtLocation(hat, 0);
	}

	/*
	 * public void addAchievement(EntityPlayer player){
	 * super.addAchievement(player); player.awardStat(TF2Achievements.MONOCULUS); }
	 */
	@Override
	protected float getStandingEyeHeight(net.minecraft.world.entity.Pose pose, net.minecraft.world.entity.EntityDimensions size) {
		return size.height / 2;
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		if (this.level.isClientSide && LASER.equals(key))
			this.laserTime = 100;
	}

	static class AIFireballAttack extends Goal {
		private final EntityMonoculus parentEntity;
		private int attackTimer;

		public int triple;

		public AIFireballAttack(EntityMonoculus ghast) {
			this.parentEntity = ghast;
			this.setFlags(TF2Util.mutexFlags(4));
		}

		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		@Override
		public boolean canUse() {
			return this.parentEntity.begin <= 0
					&& (this.parentEntity.getTarget() != null || this.parentEntity.envDamage > 0);
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		@Override
		public void start() {
			// this.attackTimer = 0;
		}

		/**
		 * Resets the task
		 */
		/*
		 * public void resetTask() { this.parentEntity.setAttacking(false); }
		 */

		/**
		 * Updates the task
		 */
		@Override
		public void tick() {
			LivingEntity entitylivingbase = this.parentEntity.getTarget();
			if (entitylivingbase == null)
				entitylivingbase = this.parentEntity;
			double d0 = 64.0D;

			if (entitylivingbase.distanceToSqr(this.parentEntity) < 4096.0D) {
				Level world = this.parentEntity.level;
				this.attackTimer--;

				if (this.attackTimer <= 0) {
					if (parentEntity.envDamage > 0)
						parentEntity.envDamage -= 5;
					double d1 = 4.0D;
					if (this.parentEntity.isAngry())
						this.parentEntity.playSound(TF2Sounds.MOB_MONOCULUS_SHOOT_MAD, 3f, 1);
					else
						this.parentEntity.playSound(TF2Sounds.MOB_MONOCULUS_SHOOT, 3f, 1);

					((ItemProjectileWeapon) this.parentEntity.getMainHandItem().getItem()).shoot(
							this.parentEntity.getMainHandItem(), this.parentEntity, world, 2, InteractionHand.MAIN_HAND);
					if (this.triple > 0) {
						triple--;
						this.attackTimer = Math.max(4, 6 - this.parentEntity.bossLevel / 3);

					} else {
						this.attackTimer = Math.max(11, 30 - this.parentEntity.bossLevel * 2);
						if (this.parentEntity.isAngry())
							triple = 2;
					}
					// System.out.println(this.attackTimer);

				}
			} else if (this.attackTimer > 0)
				--this.attackTimer;

			// this.parentEntity.setAttacking(this.attackTimer > 10);
		}
	}

	static class AILaserAttack extends Goal {
		private final EntityMonoculus parentEntity;

		public int damageCooldown;

		public AILaserAttack(EntityMonoculus ghast) {
			this.parentEntity = ghast;
			this.setFlags(TF2Util.mutexFlags(4));
		}

		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		@Override
		public boolean canUse() {
			return this.parentEntity.begin <= 0 && this.parentEntity.laserCooldown <= 0
					&& (this.parentEntity.getTarget() != null
							&& TF2Util.lookingAt(this.parentEntity, 13, this.parentEntity.getTarget()));
		}

		@Override
		public boolean canContinueToUse() {
			return this.parentEntity.begin <= 0 && this.parentEntity.laserCooldown <= 0
					&& (this.parentEntity.getTarget() != null || this.parentEntity.envDamage > 0);
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		@Override
		public void start() {
			this.parentEntity.laserTime = LASER_DURATION + this.parentEntity.bossLevel * 10;
		}

		/**
		 * Resets the task
		 */

		@Override
		public void stop() {
			this.parentEntity.setLaser(false);
			this.parentEntity.laserCooldown = 140;
		}

		/**
		 * Updates the task
		 */
		@Override
		public void tick() {
			if (this.parentEntity.laserTime <= 0) {
				this.parentEntity.laserCooldown = 260;
				this.parentEntity.setLaser(false);
			} else
				this.parentEntity.setLaser(true);
			if (this.parentEntity.laserTime < LASER_START + this.parentEntity.bossLevel * 10
					&& --this.damageCooldown <= 0) {
				((ItemWeapon) this.parentEntity.getOffhandItem().getItem()).use(
						this.parentEntity.getOffhandItem(), this.parentEntity, this.parentEntity.level,
						InteractionHand.OFF_HAND, null);
				this.damageCooldown = Math.max(20 - this.parentEntity.bossLevel, 10);
			}
			for (HitResult trace : TF2Util.pierce(this.parentEntity.level, parentEntity, 120, false, 0.5f, true)) {
				// if (trace.entityHit != null) {

				// }
				((ServerLevel) parentEntity.level).sendParticles(ParticleTypes.CRIT, trace.getLocation().x, trace.getLocation().y,
						trace.getLocation().z, 1, 0D, 0D, 0D, 0D);
			}
		}
	}

	static class AILookAround extends Goal {
		private final EntityMonoculus parentEntity;

		public AILookAround(EntityMonoculus ghast) {
			this.parentEntity = ghast;
			this.setFlags(TF2Util.mutexFlags(2));
		}

		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		@Override
		public boolean canUse() {
			return this.parentEntity.begin <= 0;
		}

		/**
		 * Updates the task
		 */
		@Override
		public void tick() {
			/*
			 * if (this.parentEntity.getAttackTarget() == null) { double f =
			 * MathHelper.sqrt(this.parentEntity.motionX * this.parentEntity.motionX +
			 * this.parentEntity.motionZ * this.parentEntity.motionZ);
			 * this.parentEntity.rotationYaw = -((float)
			 * MathHelper.atan2(this.parentEntity.motionX, this.parentEntity.motionZ)) *
			 * (180F / (float) Math.PI); this.parentEntity.rotationPitch = -((float)
			 * MathHelper.atan2(this.parentEntity.motionY, f)) (180F / (float) Math.PI);
			 * this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw;
			 * this.parentEntity.rotationYawHead = this.parentEntity.rotationYaw; } else {
			 * EntityLivingBase entitylivingbase = this.parentEntity.getAttackTarget();
			 * double d0 = 64.0D;
			 *
			 * if (entitylivingbase.getDistanceSq(this.parentEntity) < 4096.0D) { double d1
			 * = entitylivingbase.posX - this.parentEntity.posX; double d2 =
			 * entitylivingbase.posZ - this.parentEntity.posZ; double d3 =
			 * entitylivingbase.posY - (this.parentEntity.posY +
			 * this.parentEntity.getEyeHeight()); double f = MathHelper.sqrt(d1 * d1 + d2 *
			 * d2); this.parentEntity.rotationYaw = -((float) MathHelper.atan2(d1, d2)) *
			 * (180F / (float) Math.PI); this.parentEntity.renderYawOffset =
			 * this.parentEntity.rotationYaw; this.parentEntity.rotationPitch = -((float)
			 * MathHelper.atan2(d3, f)) * (180F / (float) Math.PI);
			 * this.parentEntity.rotationYawHead = this.parentEntity.rotationYaw; } }
			 */
			// System.out.println("coto: "+this.parentEntity.rotationPitch);
		}
	}

	static class AIRandomFly extends Goal {
		private final EntityMonoculus parentEntity;

		private boolean movingToHome;

		public AIRandomFly(EntityMonoculus ghast) {
			this.parentEntity = ghast;
			this.setFlags(TF2Util.mutexFlags(1));
		}

		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		@Override
		public boolean canUse() {
			if (this.parentEntity.begin > 0)
				return false;
			MoveControl entitymovehelper = this.parentEntity.getMoveControl();

			if (!entitymovehelper.hasWanted()
					|| (!this.parentEntity.isWithinRestriction() && !movingToHome))
				return true;
			else {
				double d0 = entitymovehelper.getWantedX() - this.parentEntity.getX();
				double d1 = entitymovehelper.getWantedY() - this.parentEntity.getY();
				double d2 = entitymovehelper.getWantedZ() - this.parentEntity.getZ();
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;
				return d3 < 1.0D || d3 > 3600.0D;
			}
		}

		/**
		 * Returns whether an in-progress EntityAIBase should continue executing
		 */
		@Override
		public boolean canContinueToUse() {
			return false;
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		@Override
		public void start() {
			net.minecraft.util.RandomSource random = this.parentEntity.getRandom();
			double d0 = this.parentEntity.getX() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
			BlockPos pos = this.parentEntity.level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, this.parentEntity.blockPosition());
			double d1 = Math.min(pos.getY() + 16, this.parentEntity.getY() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			double d2 = this.parentEntity.getZ() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
			LivingEntity target = this.parentEntity.getTarget();
			this.movingToHome = this.parentEntity.getRestrictCenter().distToCenterSqr(d0, d1, d2) < this.parentEntity
					.distanceToSqr(Vec3.atCenterOf(this.parentEntity.getRestrictCenter()));
			boolean flyToPlayer = target != null && target.distanceToSqr(this.parentEntity) > 680;

			if ((!flyToPlayer || target.distanceToSqr(d0, d1, d2) < target.distanceToSqr(this.parentEntity))
					&& (flyToPlayer || (this.parentEntity.isWithinRestriction() || movingToHome)))
				this.parentEntity.getMoveControl().setWantedPosition(d0, d1, d2, 0.3d);
		}
	}

	static class MonoculusMoveHelper extends MoveControl {
		private final EntityMonoculus parentEntity;
		private int courseChangeCooldown;

		public MonoculusMoveHelper(EntityMonoculus ghast) {
			super(ghast);
			this.parentEntity = ghast;
		}

		@Override
		public void tick() {
			double speed = this.speedModifier
					* this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
			if (this.parentEntity.isLaser())
				speed *= 0.2;
			if (this.operation == MoveControl.Operation.MOVE_TO) {
				double d0 = this.wantedX - this.parentEntity.getX();
				double d1 = this.wantedY - this.parentEntity.getY();
				double d2 = this.wantedZ - this.parentEntity.getZ();
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;

				if (this.courseChangeCooldown-- <= 0) {
					this.courseChangeCooldown = 0;
					d3 = (float) Math.sqrt(d3);

					if (this.isNotColliding(this.wantedX, this.wantedY, this.wantedZ, d3)) {
						this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().x + (d0 / d3 * speed), this.parentEntity.getDeltaMovement().y, this.parentEntity.getDeltaMovement().z);
						this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().x, this.parentEntity.getDeltaMovement().y + (d1 / d3 * speed), this.parentEntity.getDeltaMovement().z);
						this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().x, this.parentEntity.getDeltaMovement().y, this.parentEntity.getDeltaMovement().z + (d2 / d3 * speed));
					} else
						this.operation = MoveControl.Operation.WAIT;
				}
			}
		}

		/**
		 * Checks if entity bounding box is not colliding with terrain
		 */
		private boolean isNotColliding(double x, double y, double z, double p_179926_7_) {
			double d0 = (x - this.parentEntity.getX()) / p_179926_7_;
			double d1 = (y - this.parentEntity.getY()) / p_179926_7_;
			double d2 = (z - this.parentEntity.getZ()) / p_179926_7_;
			AABB axisalignedbb = this.parentEntity.getBoundingBox();

			for (int i = 1; i < p_179926_7_; ++i) {
				axisalignedbb = axisalignedbb.move(d0, d1, d2);

				if (!this.parentEntity.level.noCollision(this.parentEntity, axisalignedbb))
					return false;
			}

			return true;
		}
	}

	@Override
	public void returnSpawnItems() {
		this.spawnAtLocation(TF2Util.metaStack(TF2weapons.itemBossSpawn, 1, 1), 0);
	}
}
