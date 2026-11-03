package rafradek.TF2weapons.entity.boss;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import rafradek.TF2weapons.common.TF2Achievements;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.IEntityTF2;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemMinigun;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.capabilities.Capability;

public abstract class EntityTF2Boss extends Monster implements IEntityTF2 {

	private net.minecraft.world.entity.EntityDimensions legacySize;

	/** 1.12 setSize: overrides the entity type's size for this entity. */
	public void setSize(float width, float height) {
		this.legacySize = net.minecraft.world.entity.EntityDimensions.scalable(width, height);
		this.refreshDimensions();
	}

	@Override
	public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
		return this.legacySize != null ? this.legacySize : super.getDimensions(pose);
	}

	protected final ServerBossEvent bossInfo = (new ServerBossEvent(this.getHoverName(), BossEvent.Color.PURPLE,
			BossEvent.Overlay.PROGRESS));
	public int level = 1;
	public int timeLeft = 2400;
	public HashSet<Player> attackers = new HashSet<>();
	public HashSet<UUID> participateUUID = new HashSet<>();
	public int playersAttacked = 0;
	private int blockBreakCounter = 27;
	public BlockPos spawnPos;
	protected float envDamage;
	protected float healthForPlayer;
	public boolean summoned;
	public float desiredHealth = 0;
	protected static final UUID BOSS_ARMOR_SPAWN = UUID.fromString("2d4ac0f2-a8f5-4a13-b04d-67d97bd71320");

	public WeaponsCapability weaponCap;

	protected EntityTF2Boss(EntityType<? extends EntityTF2Boss> type, Level world) {
		super(type, world);
		if (!this.level.isClientSide)
			this.setGlowing(true);
		this.inventoryHandsDropChances = new float[] { 0, 0 };
		this.weaponCap = new WeaponsCapability(this);
		this.weaponCap.setCanExpJump(false);
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source == DamageSource.DROWN || source == DamageSource.LAVA || source == DamageSource.ON_FIRE)
			return false;
		if (source instanceof TF2DamageSource) {
			if (source.getEntity() == this)
				return false;
			if (((TF2DamageSource) source).getCritical() > 0) {
				amount *= 0.7f;
			}
			if (!((TF2DamageSource) source).getWeapon().isEmpty()
					&& ((TF2DamageSource) source).getWeapon().getItem() instanceof ItemMinigun)
				amount *= 0.36f;
		}
		if (source.getDirectEntity() instanceof EntitySentry
				&& !((EntitySentry) source.getDirectEntity()).fromPDA) {
			amount *= 0.3f;
		}
		if (source.getEntity() == null || WeaponsCapability.get(source.getEntity()) == null) {
			amount *= 0.5f;
		}
		if (super.hurt(source, amount * (this.getMaxHealth() / this.desiredHealth))) {
			if (source.getEntity() != null && source.getEntity() instanceof Player)
				this.attackers.add((Player) source.getEntity());
			if (!(source.getEntity() instanceof LivingEntity))
				this.envDamage += amount;
			return true;
		}
		return false;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		super.fall(distance, 0);
	}

	@Override
	public void push(Entity entityIn) {

	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	protected float getWaterSlowDown() {
		return 1F;
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int lootingModifier, boolean wasRecentlyHit) {
		int count = this.playersAttacked;
		if (count > 4) {
			count = 2 + this.playersAttacked / 2;
		}
		for (int i = 0; i < count; i++) {
			if (i > 0)
				this.dropFewItems(wasRecentlyHit, lootingModifier);
			this.spawnAtLocation(TF2Util.metaStack(TF2weapons.itemTF2, this.level / 2 + Mth.log2(this.level) + 2, 2),
					0);
			ItemStack weapon = ItemFromData.getRandomWeapon(this.random, ItemFromData.VISIBLE_WEAPON);
			if (this.level > 1)
				TF2Attribute.upgradeItemStack(weapon, 40 + (this.level - 2) * 55 + Mth.log2(this.level) * 40,
						random);
			this.spawnAtLocation(weapon, 0);
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		this.timeLeft--;
		if (!this.level.isClientSide) {
			if (this.tickCount % 20 == 0) {
				for (LivingEntity living : this.level.getEntitiesOfClass(LivingEntity.class,
						this.getBoundingBox().inflate(100, 28, 100),
						input -> input.getCapability(TF2weapons.WEAPONS_CAP).isPresent()
								&& (TF2Util.getOwnerIfOwnable(input) instanceof Player
										|| input.distanceToSqr(EntityTF2Boss.this) < 900))) {

					if (!this.participateUUID.contains(living.getUUID())) {
						this.participateUUID.add(living.getUUID());
						this.desiredHealth += this.healthForPlayer;

						if (living instanceof Player) {
							this.desiredHealth += this.healthForPlayer * 3;
						}
						float per = this.getHealth() / this.getMaxHealth();

						this.getAttribute(Attributes.MAX_HEALTH)
								.setBaseValue(Math.min(1000, desiredHealth));

						this.setHealth(per * this.getMaxHealth());

					}
				}
			}
			if (this.getTarget() != null && !this.getTarget().isAlive()) {
				this.setTarget(null);
			}
			if (timeLeft == 2250)
				this.setGlowing(false);
			if (timeLeft == 1200)
				this.playSound(TF2Sounds.MOB_BOSS_ESCAPE_60, 4F, 1f);
			else if (timeLeft == 200)
				this.playSound(TF2Sounds.MOB_BOSS_ESCAPE_10, 4F, 1f);
			else if (timeLeft <= 0) {
				this.playSound(TF2Sounds.MOB_BOSS_ESCAPE, 4F, 1f);
				if (this.summoned)
					this.returnSpawnItems();
				this.discard();
			}
			this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
			--this.blockBreakCounter;

			if (this.blockBreakCounter <= 0 && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
				this.blockBreakCounter = 27;
				breakBlocks();
			}
		}

	}

	public void returnSpawnItems() {

	}

	public boolean breakBlocks() {
		return this.breakBlocks(this.getBreakingBB());
	}

	public boolean breakBlocks(AABB box) {
		boolean flag = false;
		for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); ++x)
			for (int y = Mth.floor(box.minY); y <= Mth.floor(box.maxY); ++y)
				for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); ++z) {
					BlockPos blockpos = new BlockPos(x, y, z);
					BlockState iblockstate = this.level.getBlockState(blockpos);
					Block block = iblockstate.getBlock();

					if (!block.isAir(iblockstate, this.level, blockpos) && !iblockstate.getMaterial().isLiquid()
							&& WitherBoss.canDestroyBlock(block)
							&& block.canEntityDestroy(iblockstate, world, blockpos, this))
						flag = this.level.destroyBlock(blockpos, true) || flag;
				}

		if (flag)
			this.level.levelEvent((Player) null, 1022, this.blockPosition(), 0);
		return flag;
	}

	@Override
	public float getSoundVolume() {
		return TF2ConfigVars.bossVolume;
	}

	public AABB getBreakingBB() {
		return this.getBoundingBox();
	}

	@Override
	public boolean isNonBoss() {
		return false;
	}

	public SoundEvent getAppearSound() {
		return null;
	}

	@Override
	public Team getTeam() {
		return this.level.getScoreboard().getPlayerTeam("TF2Bosses");
	}

	@Override
	public void setSecondsOnFire(int time) {
		super.setSecondsOnFire(1);
	}

	@Override
	public void travel(float m1, float m2, float m3) {
		float move = this.getSpeed();
		super.travel(m1 / move, m2, m3 / move);
	}

	public boolean attemptTeleportForce(double x, double y, double z) {
		this.breakBlocks(this.getBoundingBox().move(x - this.getX(), y - this.getY(), z - this.getZ()));
		double d0 = this.getX();
		double d1 = this.getY();
		double d2 = this.getZ();
		this.setPos((x), this.getY(), this.getZ());
		this.setPos(this.getX(), (y), this.getZ());
		this.setPos(this.getX(), this.getY(), (z));
		boolean flag = false;
		BlockPos blockpos = this.blockPosition();
		Level world = this.level;
		Random random = this.getRandom();

		if (world.isLoaded(blockpos)) {
			boolean flag1 = false;

			while (!flag1 && blockpos.getY() > 0) {
				BlockPos blockpos1 = blockpos.below();
				BlockState iblockstate = world.getBlockState(blockpos1);

				if (iblockstate.getMaterial().blocksMovement()) {
					flag1 = true;
				} else {
					--this.getY();
					blockpos = blockpos1;
				}
			}

			if (flag1) {
				this.teleportTo(this.getX(), this.getY(), this.getZ());

				if (world.getCollisionBoxes(null, this.getBoundingBox()).isEmpty()
						&& !world.containsAnyLiquid(this.getBoundingBox())) {
					flag = true;
				}
			}
		}

		if (!flag) {
			this.teleportTo(d0, d1, d2);
			return false;
		} else {
			int i = 128;

			for (int j = 0; j < 128; ++j) {
				double d6 = j / 127.0D;
				float f = (random.nextFloat() - 0.5F) * 0.2F;
				float f1 = (random.nextFloat() - 0.5F) * 0.2F;
				float f2 = (random.nextFloat() - 0.5F) * 0.2F;
				double d3 = d0 + (this.getX() - d0) * d6 + (random.nextDouble() - 0.5D) * this.getBbWidth() * 2.0D;
				double d4 = d1 + (this.getY() - d1) * d6 + random.nextDouble() * this.getBbHeight();
				double d5 = d2 + (this.getZ() - d2) * d6 + (random.nextDouble() - 0.5D) * this.getBbWidth() * 2.0D;
				world.addParticle(ParticleTypes.PORTAL, d3, d4, d5, f, f1, f2);
			}

			if (this instanceof PathfinderMob) {
				((PathfinderMob) this).getNavigation().stop();
			}

			return true;
		}
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance diff, MobSpawnType spawnReason, @Nullable SpawnGroupData p_110161_1_, @Nullable CompoundTag spawnTag) {
		int players = 0;
		int highestLevel = 0;
		int statmult = 0;
		this.spawnPos = this.blockPosition();
		for (LivingEntity living : this.level.getEntitiesOfClass(LivingEntity.class,
				this.getBoundingBox().inflate(100, 28, 100),
				input -> input.getCapability(TF2weapons.WEAPONS_CAP).isPresent()
						&& (TF2Util.getOwnerIfOwnable(input) instanceof Player
								|| input.distanceToSqr(EntityTF2Boss.this) < 900))) {

			statmult++;
			this.participateUUID.add(living.getUUID());
			if (living instanceof Player) {
				players++;
				statmult += 3;
				Player player = (Player) living;
				if (player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).highestBossLevel.get(this.getClass()) == null) {
					player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).highestBossLevel.put(this.getClass(), (short) 0);
				}
				int level = player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).highestBossLevel.get(this.getClass());
				if (level > highestLevel)
					highestLevel = level;
				player.sendSystemMessage(Component.translatable("tf2boss.appear",
						new Object[] { this.getHoverName(), Math.min(30, highestLevel + 1) }));
			}
		}
		highestLevel++;
		this.level = Math.min(30, highestLevel);
		// System.out.println("Level: " + this.level + " player: " + players);
		this.healthForPlayer = (float) (this.getAttribute(Attributes.MAX_HEALTH).getBaseValue()
				* 0.1f * (0.5f + highestLevel * 0.5f));
		float desiredHealth = (float) this.getAttribute(Attributes.MAX_HEALTH).getBaseValue()
				* (0.6f + statmult * 0.1f) * (0.5f + highestLevel * 0.5f);
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.min(1000, desiredHealth));
		this.desiredHealth = desiredHealth;
		this.setHealth(this.getMaxHealth());
		TF2Attribute.setAttribute(this.getMainHandItem(), TF2Attribute.attributes[19],
				1 * (0.7f + highestLevel * 0.3f));
		this.xpReward = (int) (200 * (0.5f + players * 0.5f) * (0.35f + highestLevel * 0.65f));
		this.playersAttacked = players;
		this.playSound(this.getAppearSound(), 4F, 1);
		this.getAttribute(Attributes.ARMOR)
				.addTransientModifier(new AttributeModifier(EntityTF2Boss.BOSS_ARMOR_SPAWN, "armorspawn", 20, AttributeModifier.Operation.fromValue(0)));
		return p_110161_1_;
	}

	@Override
	public void die(DamageSource cause) {
		super.die(cause);
		for (Player player : this.attackers) {
			int level = player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).highestBossLevel.get(this.getClass()) != null
					? player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).highestBossLevel.get(this.getClass())
					: 0;
			if (this.level > level)
				player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).highestBossLevel.put(this.getClass(),
						(short) this.level);
			player.sendSystemMessage(
					Component.translatable("tf2boss.death", new Object[] { this.getHoverName(), this.level }));
		}
	}

	/*
	 * public void addAchievement(EntityPlayer player){ if(this.level>=30)
	 * player.awardStat(TF2Achievements.BOSS_30_LVL); }
	 */
	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putShort("Level", (short) this.level);
		nbt.putShort("Players", (short) this.playersAttacked);
		nbt.putShort("TimeLeft", (short) this.timeLeft);
		nbt.putFloat("DamageMult", this.desiredHealth);
		nbt.putBoolean("Summoned", this.summoned);
		nbt.putFloat("HealthForPlayer", this.healthForPlayer);

		ListTag playerleast = new ListTag();
		ListTag playermost = new ListTag();
		for (Player player : this.attackers) {
			playerleast.add(LongTag.valueOf(player.getUUID().getLeastSignificantBits()));
			playermost.add(LongTag.valueOf(player.getUUID().getMostSignificantBits()));
		}
		nbt.put("PlayerLeast", playerleast);
		nbt.put("PlayerMost", playermost);

		ListTag participateleast = new ListTag();
		ListTag participatemost = new ListTag();
		for (UUID participate : this.participateUUID) {
			participateleast.add(LongTag.valueOf(participate.getLeastSignificantBits()));
			participatemost.add(LongTag.valueOf(participate.getMostSignificantBits()));
		}
		nbt.put("ParticipateLeast", participateleast);
		nbt.put("ParticipateMost", participatemost);

		if (this.spawnPos != null)
			nbt.putIntArray("SpawnPos", new int[] { this.spawnPos.getX(), this.spawnPos.getY(), this.spawnPos.getZ() });
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		level = nbt.getShort("Level");
		this.playersAttacked = nbt.getShort("Players");
		this.timeLeft = nbt.getShort("TimeLeft");
		this.desiredHealth = nbt.getFloat("DamageMult");
		this.summoned = nbt.getBoolean("Summoned");
		this.healthForPlayer = nbt.getFloat("HealthForPlayer");

		ListTag playerleast = nbt.getList("PlayerLeast", 4);
		ListTag playermost = nbt.getList("PlayerMost", 4);
		for (int i = 0; i < playerleast.size(); i++) {
			this.attackers.add(this.level.getPlayerByUUID(
					new UUID(((LongTag) playermost.get(i)).getLong(), ((LongTag) playerleast.get(i)).getLong())));
		}
		ListTag participateleast = nbt.getList("ParticipateLeast", 4);
		ListTag participatemost = nbt.getList("ParticipateMost", 4);
		for (int i = 0; i < participateleast.size(); i++) {
			this.participateUUID.add(new UUID(((LongTag) participatemost.get(i)).getLong(),
					((LongTag) participateleast.get(i)).getLong()));
		}

		if (this.timeLeft < 2250)
			this.setGlowing(false);
		if (nbt.contains("SpawnPos")) {
			int[] arr = nbt.getIntArray("SpawnPos");
			this.spawnPos = new BlockPos(arr[0], arr[1], arr[2]);
		}
	}

	@Override
	public void addTrackingPlayer(ServerPlayer player) {
		super.addTrackingPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	@Override
	public void removeTrackingPlayer(ServerPlayer player) {
		super.removeTrackingPlayer(player);
		this.bossInfo.removePlayer(player);
	}

	@Override
	public boolean canBeAffected(MobEffectInstance potioneffectIn) {
		return potioneffectIn.getEffect() == TF2weapons.stun && potioneffectIn.getAmplifier() >= 3;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	@Override
	public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(Capability<T> capability, @javax.annotation.Nullable Direction facing) {
		T value = this.getCapabilityLegacy(capability, facing);
		return value == null ? net.minecraftforge.common.util.LazyOptional.empty() : net.minecraftforge.common.util.LazyOptional.of(() -> value);
	}

	public <T> T getCapabilityLegacy(Capability<T> capability, Direction facing) {
		if (capability == TF2weapons.WEAPONS_CAP) {
			return (T) this.weaponCap;
		}
		/*
		 * else if (capability == TF2weapons.INVENTORY_CAP){ return (T)
		 * this.wearablesCap; }
		 */
		return super.getCapability(capability, facing).orElse(null);
	}

	public boolean hasCapabilityLegacy(Capability<?> capability, Direction facing) {
		return capability == TF2weapons.WEAPONS_CAP
				/* || capability == TF2weapons.INVENTORY_CAP */ || super.getCapability(capability, facing).isPresent();
	}

	@Override
	public boolean hasHead() {
		return true;
	}

	@Override
	public AABB getHeadBox() {
		return null;
	}

	@Override
	public boolean hasDamageFalloff() {
		return false;
	}

	@Override
	public boolean isBuilding() {
		return false;
	}

	@Override
	public boolean isBackStabbable(LivingEntity attacker, ItemStack knife) {
		return true;
	}
}