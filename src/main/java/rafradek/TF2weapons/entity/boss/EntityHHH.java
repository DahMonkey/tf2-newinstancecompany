package rafradek.TF2weapons.entity.boss;

import rafradek.TF2weapons.common.TF2Achievements;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityHHH extends EntityTF2Boss {

	public int targetTime;
	private int attackTick;
	private int scareTick;
	private int teleportTime;
	private int toTeleportTime;
	public int begin = 30;
	private float damageTakenNoPath;
	private int aboveGroundTicks;
	private int dashCool;
	private int dashTick;
	private int targetInAir;
	private Vec3 dashMotion;
	private List<LivingEntity> possibleTargets = new ArrayList<>();
	private static UUID slowDash = UUID.fromString("03edb08b-0a2f-4040-9c9c-062d0c2e2a85");
	private static UUID fasterSpeed = UUID.fromString("03edb08b-0a2f-4040-9c9c-062d0c2e2b85");
	private Vec3 lastPos = Vec3.ZERO;

	public EntityHHH(Level world) {
		this(TF2weapons.HHH, world);
	}

	public EntityHHH(EntityType<? extends EntityHHH> type, Level world) {
		super(type, world);
		this.setSize(0.9f, 2.85f);
		this.maxUpStep = 1.05f;
		this.setNoAi(true);
		this.setPathPriority(BlockPathTypes.LAVA, 8.0F);
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemFromData.getNewStack("headtaker"));
		this.getMainHandItem().enchant(Enchantments.KNOCKBACK, 10);
	}

	@Override
	protected void initEntityAI() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new EntityAIAirAttack());
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		/*
		 * this.targetTasks.addTask(1, new
		 * EntityAINearestAttackableTarget<EntityLivingBase>(this,
		 * EntityLivingBase.class,2, false,false,new Predicate<EntityLivingBase>(){
		 *
		 * @Override public boolean apply(EntityLivingBase input) { return
		 * input.getActivePotionEffect(TF2weapons.it)!=null; }
		 *
		 * }));
		 */
		this.targetSelector.addGoal(2,
				new NearestAttackableTargetGoal<>(this, EntitySentry.class, 2, false, false, null));
		/*
		 * this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false, new
		 * Class[0])); this.targetTasks.addTask(3, new
		 * EntityAINearestAttackableTarget<EntityLivingBase>(this,
		 * EntityLivingBase.class,2, false,false,new Predicate<EntityLivingBase>(){
		 *
		 * @Override public boolean apply(EntityLivingBase input) { return (input
		 * instanceof EntityTF2Character || input instanceof EntityPlayer) &&
		 * getDistanceSq(input)<600; }
		 *
		 * }));
		 */

	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		// this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).
		this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(128.0D);
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(105);
		this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.10D);
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(13D);

	}

	@Override
	public SpawnGroupData onInitialSpawn(DifficultyInstance diff, SpawnGroupData p_110161_1_) {
		p_110161_1_ = super.onInitialSpawn(diff, p_110161_1_);
		this.getAttribute(Attributes.MOVEMENT_SPEED)
				.setBaseValue(this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue()
						* (0.88 + this.level * 0.12));
		this.getAttribute(Attributes.ATTACK_DAMAGE)
				.setBaseValue(this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue()
						* (0.9 + this.level * 0.1));

		return p_110161_1_;
	}

	@Override
	public void dropFewItems(boolean hit, int looting) {
		if (this.random.nextBoolean() || this.level == 1)
			this.spawnAtLocation(ItemFromData.getNewStack("headtaker"), 0);
		// ItemStack hat=ItemFromData.getNewStack("monoculus");
		// hat.getTagCompound().setShort("BossLevel",(short)this.level);
		// this.entityDropItem(hat, 0);
	}

	@Override
	public void aiStep() {

		if (this.getMainHandItem().isEmpty() || !(this.getMainHandItem().getItem() instanceof ItemWeapon))
			this.setItemInHand(InteractionHand.MAIN_HAND, ItemFromData.getNewStack("headtaker"));

		super.aiStep();
		if (this.begin-- > 20 && this.level.isClientSide)
			for (int i = 0; i < 40; i++) {
				Vec3 pos = TF2Util.radiusRandom2D(2.2f, this.random);
				this.level.addParticle(ParticleTypes.PORTAL, pos.x + this.getX(), this.getY() - 0.5,
						pos.y + this.getZ(), 0, 0, 0, new int[0]);
			}

		if (!world.isClientSide && this.begin <= 0) {
			if (this.random.nextInt(20) == 0) {
				TF2Util.addModifierSafe(this, Attributes.MOVEMENT_SPEED, new AttributeModifier(fasterSpeed,
						"fastspeed", TF2Util.lerp(0.2f, 0.0f, this.getHealth() / this.getMaxHealth()), 2), false);
			}
			if (this.begin == 0) {
				this.setNoAi(false);
				this.getAttribute(Attributes.ARMOR).removeModifier(BOSS_ARMOR_SPAWN);
			}
			if (this.getTarget() != null && !(this.getTarget() instanceof EntitySentry)
					&& this.getTarget().getEffect(TF2weapons.it) == null) {
				LivingEntity lastTarget = this.getTarget().getLastHurtMob();
				this.setTarget(null);
				if (lastTarget != null && lastTarget.getEffect(TF2weapons.it) != null)
					this.setTarget(lastTarget);
			}
			this.scareTick--;
			if (this.getTarget() == null && this.tickCount % 5 == 0) {
				List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class,
						this.getBoundingBox().inflate(20, 10, 20),
						input -> (input instanceof EntityTF2Character || input instanceof Player
								|| input == getRevengeTarget()) && getDistanceSq(input) < 600
								&& TargetGoal.isSuitableTarget(EntityHHH.this, input, false, false));
				Collections.sort(list, new NearestAttackableTargetGoal.Sorter(this));
				if (list.size() > 0)
					this.setTarget(list.get(0));
				// list.get(0).addPotionEffect(new PotionEffect(TF2weapons.it,600));
				else if (this.getLastHurtByMob() != null) {
					this.setTarget(this.getLastHurtByMob());
				}
				if (this.scareTick <= 0) {
					boolean one = false;
					List<LivingEntity> lists = this.level.getEntitiesOfClass(LivingEntity.class,
							this.getBoundingBox().inflate(10, 10, 10),
							input -> getDistanceSq(input) < 100 && !TF2Util.isAlliedTo(EntityHHH.this, input)
									&& !(input.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
											&& input.getItemBySlot(EquipmentSlot.HEAD).getItem() == Item
													.byBlock(Blocks.PUMPKIN)));
					if (lists.size() > 1) {
						for (LivingEntity living : lists) {
							one = true;
							TF2Util.stun(living, 80, false);
						}
					}
					if (one)
						this.playSound(TF2Sounds.MOB_HHH_ALERT, 1.75F, 1);
					this.scareTick = 200;
				}
			}
			if (this.getTarget() != null) {
				if (!this.getTarget().isOnGround())
					this.targetInAir += 1;
				if (this.level > 1) {
					if (this.dashCool-- <= 0) {
						this.dashCool = 300;
						this.dashTick = 35;
						this.dashMotion = this.getVectorForRotation(0, this.yHeadRot).scale(0.45);
						TF2Util.addModifierSafe(this, Attributes.MOVEMENT_SPEED,
								new AttributeModifier(slowDash, "dash", -0.5, 2), false);
					}
					if (this.dashTick-- > 0) {
						if (this.dashTick < 20) {
							this.setDeltaMovement(this.getDeltaMovement().x + (dashMotion.x), this.getDeltaMovement().y, this.getDeltaMovement().z);
							this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y + (dashMotion.y), this.getDeltaMovement().z);
							this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z + (dashMotion.z));
						}
						if (this.dashTick < 12
								&& this.distanceToSqr(this.getTarget()) < this.getTarget().distanceToSqr(
										this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z))
							this.dashTick = 0;
					} else
						this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(slowDash);
				}

			}
			if (--this.teleportTime <= 0 && this.getTarget() != null && this.tickCount % 5 == 0) {
				Path path = this.getNavigation().createPath(this.getTarget());
				boolean shouldTeleport = false;
				if (path == null)
					shouldTeleport = this.distanceToSqr(this.getTarget()) > 1.5 && this
							.distanceToSqr(this.getTarget().getX(), this.getY(), this.getTarget().getZ()) < 1;
				else {
					if (this.tickCount % 20 == 0) {
						if (this.lastPos.distanceToSqr(this.position()) < 2) {
							shouldTeleport = true;
						}
						this.lastPos = this.position();
					}
					if (!shouldTeleport)
						shouldTeleport = Math.abs(path.getFinalPathPoint().y - this.getTarget().getY()) > 1.5
								&& this.getTarget().isOnGround();
				}
				if (shouldTeleport) {
					this.toTeleportTime = 40;
					this.teleportTime = 250;
					this.setNoAi(true);
					this.setShiftKeyDown(true);
				}
			}
			/*
			 * if(--this.teleportTime<=0&&this.getAttackTarget()!=null&&(this.getNavigator()
			 * .noPath()||this.getDistanceSq(this.getAttackTarget())>1.5&&this.getDistanceSq
			 * (this.getAttackTarget().posX, this.posY, this.getAttackTarget().posZ)<0.75)){
			 *
			 * }
			 */
			if (this.toTeleportTime > 0) {
				if (this.level instanceof ServerLevel) {
					ServerLevel world = (ServerLevel) this.level;
					world.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() - 0.5, this.getZ(), 8, 2.7, 0, 2.7,
							0, new int[0]);
				}
				if (--this.toTeleportTime == 0) {
					this.setShiftKeyDown(false);
					this.setNoAi(false);
					Vec3 pos = null;
					if (this.getTarget() != null)
						pos = new Vec3(this.getTarget().getX(), this.getTarget().getY(),
								this.getTarget().getZ());
					/*
					 * else pos = RandomPositionGenerator.findRandomTarget(this, 16, 6);
					 */
					if (pos != null && this.attemptTeleportForce(pos.x, pos.y, pos.z)) {
						this.addEffect(new MobEffectInstance(TF2weapons.stun, 30, 3));
						for (int i = 0; i < 40; i++) {
							Vec3 partpos = TF2Util.radiusRandom2D(2.7f, this.random);
							this.level.addParticle(ParticleTypes.PORTAL, partpos.x + this.getX(), this.getY() - 0.5,
									partpos.y + this.getZ(), 0, 0, 0, new int[0]);
						}
					}
				}
			}

			if (--this.attackTick <= 0) {
				List<LivingEntity> ents = this.level
						.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3, 1, 3),
								input -> getDistanceSq(input) < 4 && !TF2Util.isAlliedTo(EntityHHH.this, input)
										&& (TF2Util.lookingAt(EntityHHH.this, 30, input.getX(),
												input.getY() + input.getEyeHeight(), input.getZ())
												|| input.getCollisionBoundingBox() != null));
				if (!ents.isEmpty()) {
					this.swing(InteractionHand.MAIN_HAND);
					this.attackTick = 20;
					for (LivingEntity living : ents) {
						this.attackEntityAsMob(living);
					}
				}
			}
		}

	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if (super.hurt(source, amount)) {
			if (this.getTarget() == null || this.getNavigation().isDone()) {
				this.damageTakenNoPath += amount;
				if (this.damageTakenNoPath >= 20 && this.teleportTime <= 50) {
					this.damageTakenNoPath = 0;
					this.toTeleportTime = 40;
					this.teleportTime = 250;
					this.setNoAi(true);
					this.setShiftKeyDown(true);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (super.attackEntityAsMob(entityIn)) {

			this.playSound(TF2Sounds.MOB_HHH_HIT, 1F, 1F);
			if (!entityIn.isAlive())
				this.playSound(TF2Sounds.MOB_HHH_ATTACK, 1.75f, 1);
			return true;
		}
		return false;
	}

	@Override
	public AABB getBreakingBB() {
		if (this.getTarget() != null) {
			AABB orig = this.getBoundingBox();
			return new AABB(orig.minX - 0.5, orig.minY + 1, orig.minZ - 0.5, orig.maxX + 0.5, orig.maxY,
					orig.maxZ + 0.5);
		} else
			return super.getBreakingBB();
	}

	@Override
	public boolean breakBlocks() {
		boolean flag = super.breakBlocks();
		if (flag) {
			this.swing(InteractionHand.MAIN_HAND);
			this.playSound(TF2Sounds.MOB_HHH_MISS, 1, 1);
		}
		return flag;
	}

	@Override
	public int getTalkInterval() {
		return 130;
	}

	@Override
	public void discard() {
		if (this.getTarget() != null) {
			this.getTarget().removeEffect(TF2weapons.it);
		}
		super.discard();
	}

	@Override
	public void setTarget(LivingEntity ent) {
		if (this.getTarget() != null && ent instanceof EntityBuilding)
			return;
		if (ent != null && ent.getCapability(TF2weapons.WEAPONS_CAP).isPresent()
				&& ent.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).itProtection > 0)
			return;

		if (ent != this.getTarget() && this.getTarget() != null) {
			boolean hadeffect = this.getTarget().getEffect(TF2weapons.it) != null;
			this.getTarget().removeEffect(TF2weapons.it);
			if (ent == null && hadeffect)
				this.getTarget().addEffect(new MobEffectInstance(TF2weapons.it, 12));
		}
		if (ent != null) {
			/*
			 * if(ent.getActivePotionEffect(TF2weapons.it)==null&&this.scareTick<=0){
			 * for(EntityLivingBase
			 * living:this.world.getEntitiesWithinAABB(EntityLivingBase.class,
			 * this.getEntityBoundingBox().grow(10, 10, 10), new
			 * Predicate<EntityLivingBase>(){
			 *
			 * @Override public boolean apply(EntityLivingBase input) { return
			 * getDistanceSq(input)<100 && !TF2weapons.isOnSameTeam(EntityHHH.this, input)
			 * && !(input.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null &&
			 * input.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem()==Item.
			 * getItemFromBlock(Blocks.PUMPKIN)); }
			 *
			 * })){ TF2weapons.stun(living, 100, false); } this.scareTick=200; }
			 */
			this.targetTime = 300;
			ent.addEffect(new MobEffectInstance(TF2weapons.it, 600));
			if (ent != this.getTarget() && this.random.nextBoolean()) {

			}
		}
		super.setTarget(ent);
	}

	/*
	 * public void addAchievement(EntityPlayer player){
	 * super.addAchievement(player); player.awardStat(TF2Achievements.HHH); }
	 */
	@Override
	public SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_HHH_SAY;
	}

	@Override
	public SoundEvent getDeathSound() {
		return TF2Sounds.MOB_HHH_DEFEAT;
	}

	@Override
	public SoundEvent getAppearSound() {
		return TF2Sounds.MOB_HHH_START;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putShort("Begin", (short) this.begin);
		nbt.putShort("Scare", (short) this.scareTick);
		nbt.putShort("Teleport", (short) this.teleportTime);
		nbt.putFloat("NoPathDamange", this.damageTakenNoPath);
		nbt.putShort("DashCool", (short) this.dashCool);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		this.damageTakenNoPath = nbt.getFloat("NoPathDamage");
		this.begin = nbt.getShort("Begin");
		this.scareTick = nbt.getShort("Scare");
		this.teleportTime = nbt.getShort("Teleport");
		this.dashCool = nbt.getShort("DashCool");
	}

	@Override
	public void returnSpawnItems() {
		this.spawnAtLocation(TF2Util.metaStack(TF2weapons.itemBossSpawn, 1, 0), 0);
	}

	public class EntityAIAirAttack extends Goal {

		public int ticksInAir;
		public int attackTime;
		public int attackCooldown;

		@Override
		public boolean canUse() {
			return getTarget() != null;
		}

		@Override
		public void tick() {
			if (getTarget() == null)
				return;
			LivingEntity target = getTarget();
			if (!target.isOnGround()) {
				this.ticksInAir++;
				if (this.ticksInAir > 30) {
					this.attackTime = 60;
				}
			} else
				this.ticksInAir--;

			if (--this.attackTime > 0) {
				this.setFlags(TF2Util.mutexFlags(3));
				this.ticksInAir = 0;
				EntityHHH.this.faceEntity(target, 45, 90);
				EntityHHH.this.getLookControl().setLookAt(target, 90, 90);
				if (this.attackTime == 30) {
					ItemStack stack = ItemFromData.getNewStack("hhhaxe");
					try {
						EntityProjectileBase proj = MapList.projectileClasses
								.get(ItemFromData.getData(stack).getString(PropertyType.PROJECTILE))
								.getConstructor(Level.class).newInstance(EntityHHH.this.level);
						proj.initProjectile(EntityHHH.this, InteractionHand.MAIN_HAND, stack);
						double x = target.getX();
						double y = target.getY() + target.getEyeHeight();
						double z = target.getZ();
						// float speed = TF2Attribute.getModifier("Proj Speed", stackW,
						// ItemFromData.getData(stackW).getFloat(PropertyType.PROJECTILE_SPEED),
						// living);
						proj.face(target, 1);
						EntityHHH.this.level.addFreshEntity(proj);
					} catch (Exception e) {

					}
				}
			} else
				this.setFlags(TF2Util.mutexFlags(0));
		}

	}
}
