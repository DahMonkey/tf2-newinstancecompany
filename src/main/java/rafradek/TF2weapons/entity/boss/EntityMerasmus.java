package rafradek.TF2weapons.entity.boss;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import rafradek.TF2weapons.common.TF2Achievements;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.block.BlockProp;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.client.particle.EnumTF2Particles;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemProjectileWeapon;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.TF2Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityMerasmus extends EntityTF2Boss {

	private int begin = 30;
	private int teleportCooldown = 240;
	private int bombCooldown = 160;
	private int bombDuration;
	public boolean hidden;
	public int topBlock;
	public BlockPos hiddenBlock;
	private int hideCount;
	public ArrayList<BlockPos> usedPos = new ArrayList<>();
	private static final EntityDataAccessor<Boolean> SPELL_BOMB = SynchedEntityData.defineId(EntityMerasmus.class,
			EntityDataSerializers.BOOLEAN);

	public EntityMerasmus(Level world) {
		this(TF2weapons.MERASMUS, world);
	}

	public EntityMerasmus(EntityType<? extends EntityMerasmus> type, Level world) {
		super(type, world);
		this.setSize(1.15f, 3.5f);
		this.maxUpStep = 1.05f;
		this.setNoAi(true);
		this.setItemInHand(InteractionHand.MAIN_HAND, ItemFromData.getNewStack("mrsbomb"));
	}

	@Override
	public void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(SPELL_BOMB, false);
	}

	@Override
	protected void initEntityAI() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(4, new AIAttack(this));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, false, new Class[0]));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class,
				3, false, false, input -> input instanceof EntityTF2Character || input instanceof Player) {
			@Override
			protected double getTargetDistance() {
				return super.getTargetDistance() * 0.35;
			}

			@Override
			public boolean canUse() {

				return super.canUse();
			}
		});
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(this, Player.class, 5,
				false, false, input -> input instanceof Player) {
			@Override
			public boolean canUse() {
				boolean ex = super.canUse();
				return ex;
			}
		});
	}

	public static AttributeSupplier.Builder createAttributes() {
		return EntityTF2Boss.createAttributes()
				.add(Attributes.FOLLOW_RANGE, 256.0D)
				.add(Attributes.MAX_HEALTH, 200)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.098D)
				.add(Attributes.ATTACK_DAMAGE, 0D);
	}

	protected PathNavigation getNewNavigator(Level world) {
		return new WallClimberNavigation(this, world);
	}

	@Override
	public float getEyeHeight() {
		return 2.1f;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		float prevHP = this.getHealth();
		if (super.hurt(source, amount)) {
			float newHP = this.getHealth();
			if (this.isAlive() && !this.isBombSpell()) {
				if (this.hideCount == 0 && newHP / this.getMaxHealth() <= 0.55f) {
					this.hide(true);
					this.hideCount = 1;
				} else if (this.hideCount == 1 && newHP / this.getMaxHealth() <= 0.1f) {
					this.hide(true);
					this.hideCount = 2;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void aiStep() {
		if (this.getMainHandItem().isEmpty() || !(this.getMainHandItem().getItem() instanceof ItemWeapon))
			this.setItemInHand(InteractionHand.MAIN_HAND, ItemFromData.getNewStack("mrsbomb"));

		super.aiStep();
		if (this.getEffect(TF2weapons.stun) != null)
			this.setXRot((90));
		if (this.begin-- > 20 && this.level.isClientSide)
			for (int i = 0; i < 40; i++) {
				Vec3 pos = TF2Util.radiusRandom2D(2.2f, this.random);
				this.level.addParticle(ParticleTypes.PORTAL, pos.x + this.getX(), this.getY() - 0.5,
						pos.y + this.getZ(), 0, 0, 0, new int[0]);
			}

		if (!this.level.isClientSide) {
			if (this.begin == 0) {
				this.setNoAi(false);
				this.getAttribute(Attributes.ARMOR).removeModifier(BOSS_ARMOR_SPAWN);
			}
			this.bombDuration--;
			for (LivingEntity living : this.level.getEntitiesOfClass(LivingEntity.class,
					this.getBoundingBox().inflate(1.5, 1, 1.5), input -> input.getEffect(TF2weapons.bombmrs) != null)) {
				living.removeEffect(TF2weapons.bombmrs);
				living.removeEffect(TF2weapons.stun);
				living.removeEffect(MobEffects.CONFUSION);
				this.playSound(TF2Sounds.MOB_MERASMUS_STUN, 1F, 1f);
				this.hurt(new EntityDamageSource("magicb", living).setMagic(), 15);
				this.addEffect(new MobEffectInstance(TF2weapons.stun, 120, 3));
				this.teleportCooldown = 120;
			}
			if (!this.hidden) {

				if (this.tickCount % 5 == 0) {
					if (this.getTarget() != null && !this.getSensing().hasLineOfSight(this.getTarget())) {
						TF2Attribute.setAttribute(this.getMainHandItem(), TF2Attribute.attributes[39], 0.35f);
					} else
						TF2Attribute.setAttribute(this.getMainHandItem(), TF2Attribute.attributes[39], 0f);
				}

				if (this.bombCooldown-- <= 0) {
					List<Player> list = this.level.getEntitiesOfClass(Player.class,
							this.getBoundingBox().inflate(30, 15, 30), input -> getDistanceSq(input) < 900
									&& !TF2Util.isAlliedTo(EntityMerasmus.this, input) && TargetGoal
											.isSuitableTarget(EntityMerasmus.this, input, false, false));
					if (!list.isEmpty()) {
						Player living = list.get(this.random.nextInt(list.size()));
						living.addEffect(new MobEffectInstance(TF2weapons.bombmrs, 300));
						TF2Util.stun(living, 300, false);
						((ServerPlayer) living).connection
								.send(new ClientboundSoundPacket(TF2Sounds.MOB_MERASMUS_HEADBOMB,
										this.getSoundSource(), living.getX(), living.getY(), living.getZ(), 4F, 1f));
						// this.teleportCooldown=90;
					}
					this.bombCooldown = Math.max(680 - this.playersAttacked * 60, 480);
				}

				if (this.teleportCooldown-- <= 0 && !this.isBombSpell()) {
					this.teleport();
				}
				if (this.isBombSpell()) {
					float prevPitch = this.getXRot();
					float prevYaw = this.yHeadRot;
					if (this.tickCount % 8 == 0) {
						for (int i = 0; i < 16; i++) {
							this.setXRot((-70 + this.random.nextFloat() * 120f));
							this.yHeadRot = i * 22.5f + this.random.nextFloat() * 22.5f;
							((ItemProjectileWeapon) this.getMainHandItem().getItem())
									.shoot(this.getMainHandItem(), this, this.level, 0, InteractionHand.MAIN_HAND);
						}
					}
					if (this.getY() < topBlock) {
						this.setDeltaMovement(this.getDeltaMovement().x, (0.15f), this.getDeltaMovement().z);
					} else {
						this.setDeltaMovement(this.getDeltaMovement().x, (0), this.getDeltaMovement().z);
					}
					if (this.bombDuration == 200 || !this.getMoveControl().isUpdating()) {

						Random random = this.getRandom();
						BlockPos pos = this.level.getTopSolidOrLiquidBlock(this.blockPosition());
						double d0 = this.getX();
						double d2 = this.getZ();
						if (pos.getY() + 7 < this.getY()) {
							d0 += (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
							d2 += (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
						}
						double d1 = pos.getY() + 7 + random.nextInt(3);
						this.getMoveControl().setWantedPosition(d0, this.topBlock, d2, 1.0D);

					}
					this.setXRot((prevPitch));
					this.yHeadRot = prevYaw;
					if (this.bombDuration < 40) {
						this.setBombSpell(false);
					}
				}
			}

			if (this.hidden) {
				if (this.tickCount % 5 == 0)
					this.heal(this.getMaxHealth() * 0.001f);
				if (this.level.getBlockState(this.hiddenBlock).getBlock() != TF2weapons.blockProp) {
					this.hide(false);
				}
			}
		}

	}

	public void teleport() {
		this.teleportCooldown = 240;
		this.playSound(TF2Sounds.MOB_MERASMUS_DISAPPEAR, 1F, 1f);

		for (int i = 0; i < 10; i++) {
			double x;
			double z;
			if (this.getTarget() != null) {
				x = this.getTarget().getX() + random.nextDouble() * 40 - 20;
				z = this.getTarget().getZ() + random.nextDouble() * 40 - 20;
			} else {
				x = this.getX() + random.nextDouble() * 40 - 20;
				z = this.getZ() + random.nextDouble() * 40 - 20;
			}
			double y = this.level.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY() + 3;
			if (this.attemptTeleport(x, y, z)) {
				this.playSound(TF2Sounds.MOB_MERASMUS_APPEAR, 1F, 1f);
				for (int j = 0; j < 40; j++) {
					Vec3 pos = TF2Util.radiusRandom3D(2.7f, this.random);
					this.level.addParticle(ParticleTypes.PORTAL, pos.x + this.getX(), pos.y + this.getY(),
							pos.z + this.getZ(), 0, 0, 0, new int[0]);
				}
				this.teleportCooldown += 200 + random.nextInt(80);
				break;
			}
		}
	}

	public boolean isBombSpell() {
		return this.getEntityData().get(SPELL_BOMB);
	}

	public void setBombSpell(boolean bomb) {
		this.getEntityData().set(SPELL_BOMB, bomb);
		this.getNavigation().stop();
		if (bomb) {
			this.moveHelper = new FloatingMoveHelper(this);
			this.setItemSlot(EquipmentSlot.OFFHAND, ItemFromData.getNewStack("bombinomicon"));
			this.playSound(TF2Sounds.MOB_MERASMUS_BOMBINOMICON, 3.3F, 1F);
		} else {
			this.moveHelper = new MoveControl(this);
			this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
		}
		TF2Attribute.setAttribute(this.getMainHandItem(), TF2Attribute.attributes[19], bomb ? 0.65f : 1);
		TF2Attribute.setAttribute(this.getMainHandItem(), TF2Attribute.attributes[39], bomb ? 0f : 0.3f);
	}

	@Override
	public void dropFewItems(boolean hit, int looting) {
		ItemStack hat = ItemFromData.getNewStack("merasmushat");
		hat.getTag().putShort("BossLevel", (short) this.level);
		this.spawnAtLocation(hat, 0);
	}

	/*
	 * public void addAchievement(EntityPlayer player){
	 * super.addAchievement(player); player.awardStat(TF2Achievements.MERASMUS); }
	 */
	@Override
	public SoundEvent getDeathSound() {
		return TF2Sounds.MOB_MERASMUS_DEFEAT;
	}

	@Override
	public SoundEvent getAppearSound() {
		return TF2Sounds.MOB_MERASMUS_START;
	}

	@Override
	public void remove(Entity.RemovalReason removalReason) {
		super.remove(removalReason);
		for (BlockPos pos : this.usedPos) {
			if (this.level.getBlockState(pos).getBlock() == TF2weapons.blockProp)
				this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putBoolean("Hidden", this.hidden);
		if (hidden) {
			nbt.putIntArray("HiddenPos",
					new int[] { this.hiddenBlock.getX(), this.hiddenBlock.getY(), this.hiddenBlock.getZ() });
			ListTag list = new ListTag();
			nbt.put("Props", list);
			for (BlockPos pos : this.usedPos)
				list.add(new IntArrayTag(new int[] { pos.getX(), pos.getY(), pos.getZ() }));
		}
		nbt.putShort("Begin", (short) this.begin);
		nbt.putShort("Teleport", (short) this.teleportCooldown);
		nbt.putShort("BombCooldown", (short) this.bombCooldown);
		nbt.putShort("BombDuration", (short) this.bombDuration);
		nbt.putShort("TopBlock", (short) this.topBlock);
		nbt.putByte("HideCount", (byte) this.hideCount);
		nbt.putBoolean("Bomb", this.isBombSpell());

	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		this.begin = nbt.getShort("Begin");
		this.setBombSpell(nbt.getBoolean("Bomb"));
		this.bombCooldown = nbt.getShort("BombCooldown");
		this.bombDuration = nbt.getShort("BombDuration");
		this.topBlock = nbt.getShort("TopBlock");
		this.teleportCooldown = nbt.getShort("Teleport");
		this.hidden = nbt.getBoolean("Hidden");
		this.hideCount = nbt.getByte("HideCount");
		if (hidden) {
			this.setNoAi(true);
			int[] pos = nbt.getIntArray("HiddenPos");
			this.hiddenBlock = new BlockPos(pos[0], pos[1], pos[2]);
			ListTag list = nbt.getList("Props", 11);
			for (int i = 0; i < list.size(); i++) {
				int[] arr = list.getIntArrayAt(i);
				this.usedPos.add(new BlockPos(arr[0], arr[1], arr[2]));
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void hide(boolean hide) {
		this.hidden = hide;
		this.setDeltaMovement((0), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (0), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (0));
		if (hide) {
			this.navigator.stop();
			// this.moveHelper=null;
			this.playSound(TF2Sounds.MOB_MERASMUS_HIDE, this.getSoundVolume(), 1F);
			this.setInvisible(true);
			int blockCount = (int) Math.min(100,
					10 * (0.7f + 0.3f * this.playersAttacked) * (0.9f + 0.1f * this.level));
			BlockPos initial = this.blockPosition();
			for (int i = 0; i < blockCount; i++) {
				BlockPos pos = initial.add(
						this.random.nextInt((int) (40 + blockCount * 0.3f)) - 20 - (int) (blockCount * 0.15f), 0,
						this.random.nextInt((int) (40 + blockCount * 0.3f)) - 20 - (int) (blockCount * 0.15f));
				pos = this.level.getTopSolidOrLiquidBlock(pos);
				// pos=pos.add(0, 1, 0);
				if (this.level.getBlockState(pos).getMaterial().isReplaceable()) {
					this.level.setBlockAndUpdate(pos, TF2weapons.blockProp
							.getStateFromMeta(this.random.nextInt(BlockProp.EnumBlockType.values().length)));
				}
				if (i == 0)
					this.hiddenBlock = pos;
				this.teleportTo(pos.getX(), -20, pos.getZ());
				this.usedPos.add(pos);
			}

		} else {
			this.setInvisible(false);
			for (BlockPos pos : this.usedPos) {
				if (this.level.getBlockState(pos).getBlock() == TF2weapons.blockProp)
					this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			}
			this.usedPos.clear();
			this.teleportTo(this.hiddenBlock.getX() + 0.5, this.hiddenBlock.getY(),
					this.hiddenBlock.getZ() + 0.5);
			this.teleportCooldown = 20;
		}
		this.setNoAi(hide);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void travel(float strafe, float forward, float par3) {
		if (!this.isBombSpell() && !this.hidden) {
			super.travel(strafe, forward, par3);
			return;
		}
		if (this.isInWater()) {
			this.moveRelative(strafe, forward, par3, 0.02F);
			this.move(MoverType.SELF, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x * (0.800000011920929D), this.getDeltaMovement().y, this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * (0.800000011920929D), this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z * (0.800000011920929D));
		} else if (this.isInLava()) {
			this.moveRelative(strafe, forward, par3, 0.02F);
			this.move(MoverType.SELF, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x * (0.5D), this.getDeltaMovement().y, this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * (0.5D), this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z * (0.5D));
		} else {
			float f = 0.91F;

			if (this.onGround)
				f = this.level
						.getBlockState(new BlockPos(Mth.floor(this.getX()),
								Mth.floor(this.getBoundingBox().minY) - 1, Mth.floor(this.getZ())))
						.getBlock().slipperiness * 0.91F;

			float f1 = 0.16277136F / (f * f * f);
			this.moveRelative(strafe, forward, par3, this.onGround ? 0.1F * f1 : 0.02F);
			f = 0.91F;

			if (this.onGround)
				f = this.level
						.getBlockState(new BlockPos(Mth.floor(this.getX()),
								Mth.floor(this.getBoundingBox().minY) - 1, Mth.floor(this.getZ())))
						.getBlock().slipperiness * 0.91F;

			this.move(MoverType.SELF, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x * (f), this.getDeltaMovement().y, this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * (f), this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z * (f));
		}

		this.animationSpeedOld = this.animationSpeed;
		double d1 = this.getX() - this.xo;
		double d0 = this.getZ() - this.zo;
		float f2 = (float) Math.sqrt(d1 * d1 + d0 * d0) * 4.0F;

		if (f2 > 1.0F)
			f2 = 1.0F;

		this.animationSpeed += (f2 - this.animationSpeed) * 0.4F;
		this.limbSwing += this.animationSpeed;
	}

	public static class AIAttack extends Goal {

		public EntityMerasmus host;

		public int attacksMade;
		public int attackDuration;
		public boolean lastAttackMagic;
		public boolean liftup;

		public AIAttack(EntityMerasmus host) {
			this.host = host;
		}

		@Override
		public boolean canUse() {
			return (this.host.getTarget() != null && this.host.getEffect(TF2weapons.stun) == null)
					|| host.envDamage > 0;
		}

		@Override
		public void tick() {
			LivingEntity target = this.host.getTarget();
			if (target == null)
				target = this.host;
			Level world = this.host.level;
			this.host.getLookControl().setLookAt(target, 30F, 90F);
			if (attackDuration < 20) {
				this.host.getNavigation().moveTo(target, 1f);
			}
			if (--this.attackDuration <= 0) {
				if (host.envDamage > 0)
					host.envDamage -= 6;
				this.host.swing(InteractionHand.MAIN_HAND);
				boolean sup = this.host.level > 1 && (this.attacksMade % 7 == 0 || (this.attacksMade - 1) % 7 == 0);
				if (this.attacksMade > 0 && this.attacksMade % 13 == 0) {
					this.host.setBombSpell(true);
					this.host.bombDuration = 200;
					this.host.teleport();
					BlockPos pos = this.host.level.getTopSolidOrLiquidBlock(this.host.blockPosition());
					this.host.topBlock = pos.getY() + 7 + this.host.random.nextInt(3);
					this.attackDuration = 200;
				} else if (this.attacksMade % 2 == 0) {
					this.attackDuration = 20 - this.host.level / 4;
					if (target != host && this.host.distanceToSqr(target) < 6) {
						if (this.host.doHurtTarget(target)) {
							target.knockback(this.host, 1.5f, Mth.sin(this.host.getYRot() * 0.017453292F),
									(-Mth.cos(this.host.getYRot() * 0.017453292F)));
						}
					} else {
						this.host.faceEntity(target, 180, 90);
						((ItemProjectileWeapon) this.host.getMainHandItem().getItem())
								.shoot(this.host.getMainHandItem(), this.host, world, 0, InteractionHand.MAIN_HAND);
						if (sup) {
							Vec3 right = this.host.getVectorForRotation(0, this.host.yHeadRot + 90);
							this.host.rotationYawHead -= 24;
							for (int i = -2; i <= 2; i++) {
								this.host.setPos(this.host.getX() + (right.x * i), this.host.getY(), this.host.getZ());
								this.host.setPos(this.host.getX(), this.host.getY(), this.host.getZ() + (right.z * i));
								this.host.rotationYawHead += 12;
								((ItemProjectileWeapon) this.host.getMainHandItem().getItem()).shoot(
										this.host.getMainHandItem(), this.host, world, 0, InteractionHand.MAIN_HAND);
							}
							this.host.rotationYawHead -= 24;
						}
					}
				} else {
					this.attackDuration = (int) (55 / (0.91 + this.host.level * 0.09f));
					this.host.getNavigation().stop();
					this.host.playSound(TF2Sounds.MOB_MERASMUS_SPELL, 2F, 1F);
					boolean attacked = false;
					double range = 10d + this.host.level * 0.8;
					for (LivingEntity living : world.getEntitiesOfClass(LivingEntity.class,
							this.host.getBoundingBox().inflate(range, range * 0.4, range),
							input -> input.distanceToSqr(host) < range * range
									&& !TF2Util.isAlliedTo(host, input)
									&& TargetGoal.isSuitableTarget(host, input, false, false))) {
						TF2Util.sendParticle(EnumTF2Particles.BULLET_TRACER, this.host, this.host.getX(),
								this.host.getY() + this.host.getBbHeight() / 2, this.host.getZ(), living.getX(),
								living.getY() + living.getBbHeight() / 2, living.getZ(), 1, 0, 0x60FF60, 10000);
						living.hurt(
								new EntityDamageSource("magicm", this.host).setMagic().setDifficultyScaled(),
								4.4f + this.host.level * 0.7f);
						living.push(0, 0.7, 0);
						living.onGround = false;
						if (living instanceof ServerPlayer)
							TF2Util.sendTracking(new TF2Message.VelocityAddMessage(new Vec3(0, 0.7, 0), true), living);
						if (living.getCapability(TF2weapons.WEAPONS_CAP).isPresent())
							WeaponsCapability.get(living).setExpJump(true);
						else
							living.fallDistance = -10;
						attacked = true;
					}
					if (!attacked)
						this.host.teleportCooldown -= 20;
				}
				if (sup)
					this.attackDuration *= 0.35f;
				this.attacksMade++;
			}
		}
	}

	static class FloatingMoveHelper extends MoveControl {
		private final EntityMerasmus parentEntity;
		private int courseChangeCooldown;

		public FloatingMoveHelper(EntityMerasmus ghast) {
			super(ghast);
			this.parentEntity = ghast;
		}

		@Override
		public void onUpdateMoveHelper() {
			if (this.action == MoveControl.Action.MOVE_TO) {
				double d0 = this.getX() - this.parentEntity.getX();
				double d1 = this.getY() - this.parentEntity.getY();
				double d2 = this.getZ() - this.parentEntity.getZ();
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;

				if (this.courseChangeCooldown-- <= 0) {
					this.courseChangeCooldown += this.parentEntity.getRandom().nextInt(5) + 2;
					d3 = (float) Math.sqrt(d3);

					if (this.isNotColliding(this.getX(), this.getY(), this.getZ(), d3)) {
						this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().x + (d0 / d3 * 0.05D), this.parentEntity.getDeltaMovement().y, this.parentEntity.getDeltaMovement().z);
						this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().x, this.parentEntity.getDeltaMovement().y, this.parentEntity.getDeltaMovement().z + (d2 / d3 * 0.05D));
					} else
						this.action = MoveControl.Action.WAIT;
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

				if (!this.parentEntity.level.getCollisionBoxes(this.parentEntity, axisalignedbb).isEmpty())
					return false;
			}

			return true;
		}
	}

	@Override
	public void setTarget(LivingEntity ent) {
		if (this.getTarget() != null && ent instanceof EntityBuilding)
			return;
		super.setTarget(ent);
	}

	@Override
	public void returnSpawnItems() {
		if (!this.usedPos.isEmpty())
			this.setPos(hiddenBlock.getX(), hiddenBlock.getY(), hiddenBlock.getZ());
		this.spawnAtLocation(TF2Util.metaStack(TF2weapons.itemBossSpawn, 1, 1), 0);
	}
}
