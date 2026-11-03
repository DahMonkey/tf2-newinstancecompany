package rafradek.TF2weapons.entity.building;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.client.Minecraft;
import rafradek.TF2weapons.util.TF2HitResult;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;
import rafradek.TF2weapons.client.compat.DefaultVertexFormats;

import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.GL11;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2EventsCommon;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.client.particle.EnumTF2Particles;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.ai.EntityAISentryAttack;
import rafradek.TF2weapons.entity.ai.EntityAISentryIdle;
import rafradek.TF2weapons.entity.ai.EntityAISpotTarget;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemPDA;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.Contract.Objective;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.ReflectionAccess;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2Util;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntitySentry extends EntityBuilding {

	public ItemStack sentryBullet = ItemFromData.getNewStack("sentrybullet");
	public ItemStack sentryHeat = ItemFromData.getNewStack("sentrybullet");
	public ItemStack sentryRocket = ItemFromData.getNewStack("sentryrocket");
	public float rotationDefault = 0;
	public float attackDelay;
	public int attackDelayRocket;
	public boolean shootRocket;
	public boolean shootBullet;
	public int mercsKilled;
	public float attackRateMult = 1;
	// public SentryLookHelper lookHelper;
	private static final EntityDataAccessor<Integer> AMMO = SynchedEntityData.createKey(EntitySentry.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> ROCKET = SynchedEntityData.createKey(EntitySentry.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> KILLS = SynchedEntityData.createKey(EntitySentry.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> CONTROLLED = SynchedEntityData.createKey(EntitySentry.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Byte> TARGET = SynchedEntityData.createKey(EntitySentry.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> MINI = SynchedEntityData.createKey(EntitySentry.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> HEAT = SynchedEntityData.createKey(EntitySentry.class,
			EntityDataSerializers.INT);

	private static final AttributeModifier MINI_HEALTH_MODIFIER = new AttributeModifier(
			UUID.fromString("1184831d-b1dc-40c8-86e6-34fa8f5abada"), "minisentry", -6, AttributeModifier.Operation.fromValue(0));

	public EntitySentry(Level world) {
		this(TF2weapons.SENTRY, world);
	}

	public EntitySentry(EntityType<? extends EntitySentry> type, Level world) {
		super(type, world);
		this.setSize(0.8f, 0.8f);
		try {
			this.lookControl = new SentryLookHelper(this);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void adjustSize() {
		if (this.getLevel() == 1) {
			this.width = 0.8f;
			this.height = 0.8f;
		} else if (this.getLevel() == 2) {
			this.width = 1f;
			this.height = 1f;
		} else if (this.getLevel() == 3) {
			this.width = 1f;
			this.height = 1.2f;
		}
		if (this.isMini()) {
			this.getBbWidth() *= 0.65f;
			this.getBbHeight() *= 0.65f;
		}
		this.getBbHeight() -= 0.1f;
		this.setSize(this.getBbWidth(), this.getBbHeight() + 0.1f);
		// System.out.println(x);
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.isControlled())
			amount *= 0.5f;
		return super.hurt(source, amount);
	}

	@Override
	public SoundEvent getSoundNameForState(int state) {
		switch (state) {
		case 0:
			return this.getLevel() == 1 ? TF2Sounds.MOB_SENTRY_SCAN_1
					: (this.getLevel() == 2 ? TF2Sounds.MOB_SENTRY_SCAN_2 : TF2Sounds.MOB_SENTRY_SCAN_3);
		// case 2:return TF2weapons.MOD_ID+":mob.sentry.shoot."+this.getLevel();
		case 3:
			return TF2Sounds.MOB_SENTRY_EMPTY;
		default:
			return super.getSoundNameForState(state);
		}
	}

	@Override
	public void setTarget(LivingEntity target) {
		if (TF2Util.isAlliedTo(this, target))
			return;
		if (target != this.getTarget() && target != null)
			this.playSound(TF2Sounds.MOB_SENTRY_SPOT, 1.5f, 1f);
		super.setTarget(target);
	}

	@Override
	public void applyTasks() {

		// this.targetTasks.addTask(1, new EntityAISentryOwnerHurt(this, true));
		this.targetSelector.addGoal(2, new EntityAISpotTarget<>(this, LivingEntity.class, true, true,
				target -> (((((getAttackFlags() & 2) == 2 && getOwnerUUID() != null) && target instanceof Player)
						|| target.getTeam() != null
						|| ((getAttackFlags() & 1) == 1 && (getRevengeTarget() == target
								|| (getOwner() != null && (getOwner().getLastHurtByMob() == target
										|| getOwner().getLastHurtMob() == target))))
						|| ((getAttackFlags() & 4) == 4 && TF2Util.isHostile(target) && getOwnerUUID() != null)
						|| ((getAttackFlags() & 4) == 4 && target instanceof Mob
								&& TF2Util.isAlliedTo(EntitySentry.this, ((Mob) target).getTarget())))
						|| ((getAttackFlags() & 8) == 8 && !(target instanceof Player)
								&& !(TF2Util.isHostile(target)) && getOwnerUUID() != null))
						&& (!TF2Util.isAlliedTo(EntitySentry.this, target))
						&& (!(target instanceof EntityTF2Character && TF2ConfigVars.naturalCheck.equals("Never"))
								|| !((EntityTF2Character) target).natural),
				false, true));
		this.goalSelector.addGoal(1, new EntityAISentryAttack(this));
		this.goalSelector.addGoal(2, new EntityAISentryIdle(this));
	}

	@Override
	public void aiStep() {

		if (!this.level.isClientSide && this.tickCount == 1)
			this.getAttackFlags();
		if (this.rotationDefault == 0)
			this.rotationDefault = this.yHeadRot;
		if (this.attackDelay > 0)
			this.attackDelay--;
		if (this.attackDelayRocket > 0)
			this.attackDelayRocket--;
		this.ignoreFrustumCheck = this.isControlled();
		if (this.isControlled() && !this.level.isClientSide) {
			Vec3 lookVec = Vec3.fromPitchYaw(this.getOwner().getXRot(), this.getOwner().yHeadRot)
					.scale(200);
			HitResult trace = TF2Util.pierce(world, this.getOwner(), this.getOwner().getX(),
					this.getOwner().getY() + this.getOwner().getEyeHeight(), this.getOwner().getZ(),
					this.getOwner().getX() + lookVec.x, this.getOwner().getY() + this.getOwner().getEyeHeight() + lookVec.y,
					this.getOwner().getZ() + lookVec.z, false, 0.01f, false).get(0);
			this.getLookControl().setLookPosition(trace.getLocation().x, trace.getLocation().y, trace.getLocation().z, 30, 75);
		}
		if (this.getTarget() != null
				&& (!this.getTarget().isAlive() || !this.hasLineOfSight(this.getTarget())))
			this.setTarget(null);
		super.aiStep();
	}

	@Override
	public ItemStack getItemInHand(InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND ? sentryRocket : (this.isHeat() ? sentryHeat : sentryBullet);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return EntityBuilding.createAttributes()
				.add(Attributes.FOLLOW_RANGE, 20.0D);
	}
	/* TODO port: // this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValu(1.6D) */

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(AMMO, 150);
		this.entityData.define(ROCKET, 20);
		this.entityData.define(KILLS, 0);
		this.entityData.define(TARGET, (byte) -1);
		this.entityData.define(CONTROLLED, Boolean.valueOf(false));
		this.entityData.define(MINI, Boolean.valueOf(false));
		this.entityData.define(HEAT, 0);
	}

	public void shootRocket(LivingEntity owner) {
		while (this.getLevel() == 3 && this.getRocketAmmo() > 0 && this.attackDelayRocket <= 0
				&& this.consumeEnergy(this.getMinEnergy() * 10)) {
			this.attackDelayRocket += 60;
			if (this.isControlled())
				this.attackDelayRocket *= 0.75f;
			try {
				// System.out.println(owner);
				this.playSound(TF2Sounds.MOB_SENTRY_ROCKET, 1.5f, 1f);
				EntityProjectileBase proj = MapList.projectileClasses
						.get(ItemFromData.getData(this.sentryRocket).getString(PropertyType.PROJECTILE))
						.getConstructor(Level.class).newInstance(this.level);
				proj.initProjectile(this, InteractionHand.MAIN_HAND, this.sentryRocket);
				proj.shootingEntity = owner;
				proj.usedWeapon = sentryRocket;
				proj.sentry = this;
				this.level.addFreshEntity(proj);
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.setRocketAmmo(this.getRocketAmmo() - 1);
			/*
			 * RayTraceResult bullet=TF2weapons.pierce(this.host.world, this.host,
			 * this.host.posX, this.host.posY+this.host.height/2, this.host.posZ,
			 * this.target.posX, this.target.posY+this.target.height/2,
			 * this.target.posZ,false, 0.08f); if(bullet.entityHit!=null){ DamageSource
			 * src=TF2weapons.causeBulletDamage("Sentry Gun", this.host.getOwner(), 0);
			 * TF2weapons.dealDamage(bullet.entityHit, this.host.world, this.host.owner,
			 * null, 0, 1.6f, src); Vec3d dist=new
			 * Vec3d(this.host.posX-bullet.entityHit.posX,this.host.posY-bullet.
			 * entityHit.posY,this.host.posZ-bullet.entityHit.posZ).normalize();
			 * bullet.entityHit.addVelocity(dist.x,dist.y, dist.z); }
			 */

		}
	}

	public void shootBullet(LivingEntity owner) {
		this.setSoundState(this.getAmmo() > 0 && this.energy.getEnergyStored() >= this.getMinEnergy() ? 2 : 3);
		Vec3 attackPos = (this.isControlled()
				? new Vec3(this.getLookControl().getLookPosX() - this.getX(),
						this.getLookControl().getLookPosY() - this.getY() - this.getEyeHeight(),
						this.getLookControl().getLookPosZ() - this.getZ())
				: this.getTarget().getEyePosition(1).subtract(this.getEyePosition(1))).normalize().scale(60)
						.add(this.getEyePosition(1));
		while (this.attackDelay <= 0 && this.getAmmo() > 0 && this.consumeEnergy(this.getMinEnergy())) {
			if (this.getOwnerUUID() != null && this.tickCount % 10 == 0)
				TF2Util.attractMobs(this, this.level);
			float cooldown = this.getLevel() > 1 ? 2.5f : 5f;
			// this.attackDelay += this.getLevel() > 1 ? 2.5f : 5f;
			if (this.isMini())
				cooldown /= 1.5f;
			if (this.isHeat()) {
				cooldown *= this.getLevel() > 1 ? 5f : 4f;
			}
			if (this.isControlled())
				cooldown /= 2f;

			cooldown *= this.attackRateMult;

			this.attackDelay += cooldown;
			if (this.isHeat()) {
				this.playSound(TF2Sounds.WEAPON_MACHINA, 2f, 1f);
			} else {
				this.playSound(this.getLevel() == 1 ? TF2Sounds.MOB_SENTRY_SHOOT_1 : TF2Sounds.MOB_SENTRY_SHOOT_2, 1.5f,
						1f);
			}

			float damage = 1.6f;
			if (this.isHeat()) {
				damage = 4.25f + this.getHeat() * 1.25f;
				if (this.getLevel() > 1)
					damage *= 1.25f;
			}
			if (this.isMini())
				damage *= 0.5f;

			List<HitResult> list = TF2Util.pierce(this.level, this, this.getX(), this.getY() + this.getEyeHeight(),
					this.getZ(), attackPos.x, attackPos.y, attackPos.z, false,
					this.isHeat() ? 0.25f + this.getHeat() * 0.2f : 0.01f, this.isHeat());
			for (HitResult bullet : list) {
				if (bullet == list.get(0)) {
					if (!this.isHeat())
						TF2Util.sendParticle(EnumTF2Particles.BULLET_TRACER, this, this.getX(),
								this.getY() + this.getEyeHeight(), this.getZ(), bullet.getLocation().x, bullet.getLocation().y,
								bullet.getLocation().z, 1, 13, 0, 64);
					else
						TF2Util.sendParticle(EnumTF2Particles.BULLET_TRACER, this, this.getX(),
								this.getY() + this.getEyeHeight(), this.getZ(), bullet.getLocation().x, bullet.getLocation().y,
								bullet.getLocation().z, 1, 0, TF2Util.getTeamColor(this), 1280);
				}

				if (TF2HitResult.getEntity(bullet) != null) {

					DamageSource src = TF2Util.causeBulletDamage(this.getItemInHand(InteractionHand.OFF_HAND), owner, this)
							.setProjectile();
					if (this.fromPDA)
						((TF2DamageSource) src).addAttackFlag(TF2DamageSource.SENTRY_PDA);

					float range = TF2HitResult.getEntity(bullet).distanceTo(this);
					if (range >= (float) this.getAttribute(Attributes.FOLLOW_RANGE)
							.getValue())
						range = Math.max(0.5f, (float) this.getAttribute(Attributes.FOLLOW_RANGE)
								.getValue() / range);
					else
						range = 1;
					if (TF2HitResult.getEntity(bullet) instanceof Player && !(owner instanceof Player)
							&& TF2ConfigVars.scaleAttributes) {
						if (this.isControlled())
							damage *= 0.92f;
						if (world.getDifficulty() == Difficulty.NORMAL) {
							damage *= 0.9f;
						} else if (world.getDifficulty() == Difficulty.EASY) {
							damage *= 0.8f;
						}
					}
					if (TF2Util.dealDamage(TF2HitResult.getEntity(bullet), this.level, owner, this.getItemInHand(InteractionHand.OFF_HAND),
							TF2Util.calculateCritPost(TF2HitResult.getEntity(bullet), null, 0, ItemStack.EMPTY, src), range * damage,
							src)) {
						Vec3 dist = new Vec3(TF2HitResult.getEntity(bullet).getX() - this.getX(), TF2HitResult.getEntity(bullet).getY() - this.getY(),
								TF2HitResult.getEntity(bullet).getZ() - this.getZ()).normalize();
						dist = dist.scale(0.25 * (this.getLevel() > 1 ? 0.7 : 1));
						if (this.isMini())
							dist = dist.scale(0.55);
						if (this.isControlled())
							dist = dist.scale(0.35);
						if (this.isHeat())
							dist = dist.scale(1.5f + 1.5f * this.getHeat());
						if ((TF2HitResult.getEntity(bullet) instanceof EntityTF2Character)
								&& ((EntityTF2Character) TF2HitResult.getEntity(bullet)).isGiant())
							dist = dist.scale(0.35);
						if (TF2HitResult.getEntity(bullet) instanceof LivingEntity)
							dist = dist.scale(1 - ((LivingEntity) TF2HitResult.getEntity(bullet))
									.getAttribute(Attributes.KNOCKBACK_RESISTANCE)
									.getValue());
						if (dist.lengthSqr() > 0f) {
							TF2HitResult.getEntity(bullet).push(dist.x, dist.y, dist.z);
							TF2HitResult.getEntity(bullet).hasImpulse = TF2HitResult.getEntity(bullet).getDeltaMovement().y > 0.05;
							if (TF2HitResult.getEntity(bullet) instanceof ServerPlayer)
								TF2weapons.network.sendTo(
										new TF2Message.VelocityAddMessage(dist, TF2HitResult.getEntity(bullet).hasImpulse),
										(ServerPlayer) TF2HitResult.getEntity(bullet));

							if (TF2HitResult.getEntity(bullet) instanceof LivingEntity) {
								((LivingEntity) TF2HitResult.getEntity(bullet)).setLastAttackedEntity(this);
								((LivingEntity) TF2HitResult.getEntity(bullet)).setLastHurtByMob(this);
								if (!TF2HitResult.getEntity(bullet).isAlive()) {
									this.scoreKill((LivingEntity) TF2HitResult.getEntity(bullet));
								}
							}
						}
					}
				}
			}
			this.setAmmo(this.getAmmo() - 1);
		}
	}

	public void scoreKill(LivingEntity target) {
		this.setKills(this.getKills() + 1);
		if (this.getOwner() instanceof Player && target instanceof EntityTF2Character
				&& !((EntityTF2Character) target).isRobot()) {
			if (++this.mercsKilled % 5 == 0)
				this.getOwner().getCapability(TF2weapons.PLAYER_CAP).orElse(null).completeObjective(Objective.KILLS_SENTRY,
						this.getOffhandItem());
			this.getOwner().getCapability(TF2weapons.PLAYER_CAP).orElse(null).completeObjective(Objective.KILL_W_SENTRY,
					this.getOffhandItem());
		}
		if (this.getOwner() instanceof Player && TF2Util.isEnemy(this.getOwner(), target)) {
			ItemStack stack = TF2Util.getFirstItem(((Player) this.getOwner()).inventory,
					stackl -> stackl.getItem() instanceof ItemPDA);
			if (!stack.isEmpty()) {
				if (!(target instanceof Player)) {
					stack.getTag().putInt("Kills", stack.getTag().getInt("Kills") + 1);
				} else {
					stack.getTag().putInt("PlayerKills",
							stack.getTag().getInt("PlayerKills") + 1);
				}
				TF2EventsCommon.onStrangeUpdate(stack, this.getOwner());
			}
		}
	}

	@Override
	public boolean canEntityBeSeen(Entity entityIn) {
		return this.level.rayTraceBlocks(new Vec3(this.getX(), this.getY() + this.getEyeHeight(), this.getZ()),
				new Vec3(entityIn.getX(), entityIn.getY() + entityIn.getEyeHeight(), entityIn.getZ()), false, true,
				false) == null;
	}

	public int getMaxAmmo() {
		return this.getLevel() == 1 ? 150 : 200;
	}

	public int getAmmo() {
		return this.entityData.get(AMMO);
	}

	public int getKills() {
		return this.entityData.get(KILLS);
	}

	public int getRocketAmmo() {
		return this.entityData.get(ROCKET);
	}

	public int getTargetInfo() {
		return this.entityData.get(TARGET);
	}

	public boolean isMini() {
		return this.entityData.get(MINI);
	}

	public boolean isHeat() {
		return this.getHeat() > 0;
	}

	public int getHeat() {
		return this.entityData.get(HEAT);
	}

	public void setAmmo(int ammo) {
		this.entityData.set(AMMO, ammo);
	}

	public void setRocketAmmo(int ammo) {
		this.entityData.set(ROCKET, ammo);
	}

	public void setKills(int kills) {
		this.entityData.set(KILLS, kills);
	}

	public void setControlled(boolean control) {
		this.entityData.set(CONTROLLED, control);
	}

	public void setTargetInfo(int target) {
		this.entityData.set(TARGET, (byte) target);
	}

	public void setHeat(int heat) {
		this.entityData.set(HEAT, heat);
	}

	public void setMini(boolean mini) {
		this.entityData.set(MINI, mini);

		if (mini) {
			TF2Util.addModifierSafe(this, Attributes.MAX_HEALTH, MINI_HEALTH_MODIFIER, true);
			this.adjustSize();
		}

		if (mini && this.isConstructing())
			this.setHealth(Math.max(this.getHealth(), this.getMaxHealth() * 0.5f));
	}

	@Override
	public int getMaxLevel() {
		return this.isMini() ? 1 : 3;
	}

	public int getAttackFlags() {
		if (this.getTargetInfo() == -1)
			this.setTargetInfo(this.getOwner() != null && this.getOwner() instanceof Player
					? WeaponsCapability.get(this.getOwner()).sentryTargets
					: 5);
		return this.getTargetInfo();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag par1NBTTagCompound) {
		super.addAdditionalSaveData(par1NBTTagCompound);

		par1NBTTagCompound.putShort("Ammo", (short) this.getAmmo());
		par1NBTTagCompound.putShort("RocketAmmo", (short) this.getRocketAmmo());
		par1NBTTagCompound.putShort("Kills", (short) this.getKills());
		par1NBTTagCompound.putShort("MercKills", (short) this.mercsKilled);
		par1NBTTagCompound.putShort("AttackFlags", (short) this.getTargetInfo());
		par1NBTTagCompound.putBoolean("Mini", this.isMini());
		par1NBTTagCompound.putFloat("AttackRateMult", this.attackRateMult);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag par1NBTTagCompound) {
		super.readAdditionalSaveData(par1NBTTagCompound);
		this.setMini(par1NBTTagCompound.getBoolean("Mini"));
		this.setAmmo(par1NBTTagCompound.getShort("Ammo"));
		this.setRocketAmmo(par1NBTTagCompound.getShort("RocketAmmo"));
		this.setKills(par1NBTTagCompound.getShort("Kills"));
		this.mercsKilled = par1NBTTagCompound.getShort("MercKills");
		this.setTargetInfo(par1NBTTagCompound.getShort("AttackFlags"));
		this.attackRateMult = par1NBTTagCompound.getFloat("AttackRateMult");
	}

	@Override
	public float getCollHeight() {
		return 1.2f;
	}

	@Override
	public float getCollWidth() {
		return 1.12f;
	}

	@Override
	public float getEyeHeight() {
		return this.getBbHeight() / 2 + 0.2f;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return this.isSapped() ? null : TF2Sounds.MOB_SENTRY_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SENTRY_DEATH;
	}

	@Override
	public boolean canUseWrench() {
		return super.canUseWrench() || this.getAmmo() < this.getMaxAmmo() || this.getRocketAmmo() < 20;
	}

	@Override
	public boolean canUseWrenchImportant() {
		return super.canUseWrenchImportant() || this.getAmmo() == 0;
	}

	@Override
	public void upgrade() {
		super.upgrade();
		this.setAmmo(200);
	}

	@Override
	public int getMinEnergy() {
		return this.getOwnerUUID() != null ? TF2ConfigVars.sentryUseEnergy : 0;
	}

	@Override
	public boolean shouldUseBlocks() {
		return TF2ConfigVars.sentryUseEnergy >= 0 && super.shouldUseBlocks();
	}

	public boolean isControlled() {
		return this.isAlive() && this.entityData.get(CONTROLLED);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (player == this.getOwner() && hand == InteractionHand.MAIN_HAND) {
			if (!this.level.isClientSide) {
				if (TF2ConfigVars.disableBuildingGui)
					this.grab();
				else
					FMLNetworkHandler.openGui(player, TF2weapons.instance, 5, world, this.getId(), 0, 0);
			}
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		}
		return InteractionResult.sidedSuccess(this.level.isClientSide);
	}

	@Override
	public int getBuildingID() {
		return 0;
	}

	@Override
	public void die(DamageSource s) {
		super.die(s);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderGUI(BufferBuilder renderer, Tessellator tessellator, Player player, int width, int height,
			GuiComponent gui) {
		ClientProxy.setColor(TF2Util.getTeamColor(this), 0.7f, 0, 0.25f, 0.8f);

		gui.drawTexturedModalRect(20, 2, 0, 112, 124, 60);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
		gui.drawTexturedModalRect(0, 0, 0, 48, 144, 64);
		double imagePos = this.getLevel() == 1 ? 0.375D : this.getLevel() == 2 ? 0.1875D : 0D;

		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(19, 56, 0.0D).tex(0.75D, imagePos + 0.1875D).endVertex();
		renderer.pos(67, 56, 0.0D).tex(0.9375D, imagePos + 0.1875D).endVertex();
		renderer.pos(67, 8, 0.0D).tex(0.9375D, imagePos).endVertex();
		renderer.pos(19, 8, 0.0D).tex(0.75D, imagePos).endVertex();
		tessellator.draw();

		if (!this.isAlive())
			return;

		imagePos = this.getLevel() == 3 ? 0D : 0.0625D;
		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(67, 57, 0.0D).tex(0.9375D, 0.0625D + imagePos).endVertex();
		renderer.pos(83, 57, 0.0D).tex(1D, 0.0625D + imagePos).endVertex();
		renderer.pos(83, 41, 0.0D).tex(1D, imagePos).endVertex();
		renderer.pos(67, 41, 0.0D).tex(0.9375D, imagePos).endVertex();
		tessellator.draw();

		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(67, 21, 0.0D).tex(0.9375D, 0.25D).endVertex();
		renderer.pos(83, 21, 0.0D).tex(1D, 0.25D).endVertex();
		renderer.pos(83, 5, 0.0D).tex(1D, 0.1875D).endVertex();
		renderer.pos(67, 5, 0.0D).tex(0.9375D, 0.1875D).endVertex();
		tessellator.draw();

		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(67, 39, 0.0D).tex(0.9375D, 0.1875D).endVertex();
		renderer.pos(83, 39, 0.0D).tex(1D, 0.1875D).endVertex();
		renderer.pos(83, 23, 0.0D).tex(1D, 0.125D).endVertex();
		renderer.pos(67, 23, 0.0D).tex(0.9375D, 0.125D).endVertex();
		tessellator.draw();

		imagePos = this.getLevel() == 1 ? 0.3125D : this.getLevel() == 2 ? 0.375D : 0.4375D;
		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(50, 18, 0.0D).tex(0.9375D, 0.0625D + imagePos).endVertex();
		renderer.pos(66, 18, 0.0D).tex(1D, 0.0625D + imagePos).endVertex();
		renderer.pos(66, 2, 0.0D).tex(1D, imagePos).endVertex();
		renderer.pos(50, 2, 0.0D).tex(0.9375D, imagePos).endVertex();
		tessellator.draw();

		gui.drawString(Minecraft.getInstance().font, Integer.toString(this.getKills()), 85, 9, 16777215);
		float health = this.getHealth() / this.getMaxHealth();
		if (health > 0.33f) {
			GlStateManager.color(0.9F, 0.9F, 0.9F, 1F);
		} else {
			GlStateManager.color(0.85F, 0.0F, 0.0F, 1F);
		}
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		for (int i = 0; i < health * 11; i++) {

			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(19, 55 - i * 5, 0.0D).endVertex();
			renderer.pos(9, 55 - i * 5, 0.0D).endVertex();
			renderer.pos(9, 59 - i * 5, 0.0D).endVertex();
			renderer.pos(19, 59 - i * 5, 0.0D).endVertex();
			tessellator.draw();
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.33F);
		renderer.begin(7, DefaultVertexFormats.POSITION);
		renderer.pos(85, 38, 0.0D).endVertex();
		renderer.pos(140, 38, 0.0D).endVertex();
		renderer.pos(140, 24, 0.0D).endVertex();
		renderer.pos(85, 24, 0.0D).endVertex();
		tessellator.draw();

		renderer.begin(7, DefaultVertexFormats.POSITION);
		renderer.pos(85, 56, 0.0D).endVertex();
		renderer.pos(140, 56, 0.0D).endVertex();
		renderer.pos(140, 42, 0.0D).endVertex();
		renderer.pos(85, 42, 0.0D).endVertex();
		tessellator.draw();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.85F);
		renderer.begin(7, DefaultVertexFormats.POSITION);
		renderer.pos(85, 38, 0.0D).endVertex();
		renderer.pos(85 + (double) this.getAmmo() / (double) this.getMaxAmmo() * 55D, 38, 0.0D).endVertex();
		renderer.pos(85 + (double) this.getAmmo() / (double) this.getMaxAmmo() * 55D, 24, 0.0D).endVertex();
		renderer.pos(85, 24, 0.0D).endVertex();
		tessellator.draw();

		double xOffset = this.getLevel() < 3 ? this.getProgress() * 0.275D : this.getRocketAmmo() * 2.75D;
		renderer.begin(7, DefaultVertexFormats.POSITION);
		renderer.pos(85, 56, 0.0D).endVertex();
		renderer.pos(85 + xOffset, 56, 0.0D).endVertex();
		renderer.pos(85 + xOffset, 42, 0.0D).endVertex();
		renderer.pos(85, 42, 0.0D).endVertex();
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Override
	public int getGuiHeight() {
		return 64;
	}

	@Override
	public int getConstructionTime() {
		return this.isMini() ? 4200 : 10500;
	}
}
