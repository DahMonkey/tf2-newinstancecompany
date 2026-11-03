package rafradek.TF2weapons.common;

import rafradek.TF2weapons.util.TF2HitResult;
import net.minecraft.world.phys.BlockHitResult;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.client.renderer.entity.RenderTF2Character;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.mercenary.EntityEngineer;
import rafradek.TF2weapons.entity.mercenary.EntityMedic;
import rafradek.TF2weapons.entity.mercenary.EntityScout;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.projectile.EntityGrapplingHook;
import rafradek.TF2weapons.entity.projectile.EntityStickybomb;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.potion.PotionTF2;
import rafradek.TF2weapons.potion.PotionTF2Item;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

public class WeaponsCapability implements ICapabilityProvider, INBTSerializable<CompoundTag> {

	public static final int MAX_METAL = 200;
	public static final int MAX_METAL_ENGINEER = 500;
	public static final int PLAYER_MINCOOL = -200;

	public LivingEntity owner;
	public int state;
	public int minigunTicks;
	public InteractionHand reloadingHand;
	public int reloadCool;
	public int lastFire;
	// public int critTime;
	// public int healTarget = -1;
	public boolean mainHand;
	public HashMap<String, Integer> effectsCool = new HashMap<>();
	public int chargeTicks;
	// public boolean charging;
	public int critTimeCool;
	@SuppressWarnings("unchecked")
	public Deque<TF2Message.PredictionMessage>[] predictionList = new Deque[8];
	public float recoil;
	public int invisTicks;
	public int disguiseTicks;
	public boolean pressedStart;
	public int airJumps;
	public EntitySentry controlledSentry;
	public ResourceLocation skinDisguise;
	public boolean skinRetrieved;
	public String lastDisguiseValue;
	public boolean lastDisgused;
	public String skinType;
	// public int zombieHuntTicks;
	public int ticksBash;
	public boolean bashCritical;
	// public int collectedHeads;
	public int collectedHeadsTime;
	public boolean wornEye;
	public int killsSpinning;
	// public int cratesOpened;
	// public float dodgedDmg;
	public int tickAirblasted;
	// public int killsAirborne;
	public int itProtection;
	public int killsAirborne;
	public int focusShotTicks;
	public int focusShotRemaining;
	public int fanCool;
	public boolean appliedMouseSlow;
	public int hitNoMiss;
	public int sentryTargets = 5;
	public boolean dispenserPlayer;
	public boolean teleporterPlayer;
	public boolean teleporterEntity;
	public boolean forcedClass;
	public float lastHitCharge;
	public SynchedEntityData entityData;

	public LivingEntity entityDisguise;

	public ArrayList<EntityStickybomb> activeBomb = new ArrayList<>();
	public float oldFactor;
	public int expJumpGround;
	public double lastPosX;
	public double lastPosY;
	public double lastPosZ;
	public ItemStack lastWeapon = ItemStack.EMPTY;
	public long ticksTotal;
	public boolean fireCoolReduced;
	public boolean autoFire;
	public LivingEntity lastAttacked;
	public boolean stabbedDisguise;
	private boolean canExpJump = true;

	public double gravity = -0.08;
	public float maxmetal = 1;
	public float damageArmorMin;

	public EnumMap<InteractionHand, ItemStack> stackActive = new EnumMap<>(InteractionHand.class);

	private float[] rageDrain = new float[RageType.values().length];
	public int disguiseCounter;

	private EntityGrapplingHook grapplingHook;

	private static final EntityDataAccessor<Boolean> EXP_JUMP = new EntityDataAccessor<>(6, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> CHARGING = new EntityDataAccessor<>(11, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<String> DISGUISE_TYPE = new EntityDataAccessor<>(7, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Boolean> DISGUISED = new EntityDataAccessor<>(8, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> INVIS = new EntityDataAccessor<>(9, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> FEIGN = new EntityDataAccessor<>(10, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> CRIT_TIME = new EntityDataAccessor<>(0, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> HEADS = new EntityDataAccessor<>(1, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> HEAL_TARGET = new EntityDataAccessor<>(2, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> METAL = new EntityDataAccessor<>(3, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> TOKEN_USED = new EntityDataAccessor<>(12, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Byte> CAN_FIRE = new EntityDataAccessor<>(13, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Float> UBER_VIEW = new EntityDataAccessor<>(14, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> GRAPPLING = new EntityDataAccessor<>(15, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> GRAPPLED = new EntityDataAccessor<>(16, EntityDataSerializers.BOOLEAN);
	private static final EnumMap<RageType, EntityDataAccessor<Float>> RAGE = new EnumMap<>(RageType.class);
	private static final EnumMap<RageType, EntityDataAccessor<Boolean>> RAGE_ACTIVE = new EnumMap<>(RageType.class);
	public static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES,
			new LinkedBlockingQueue<Runnable>());

	// public int killsSpinning;

	/*
	 * public HashMap<Class<? extends Entity>, Short> highestBossLevel = new
	 * HashMap<>(); public int nextBossTicks; public int stickybombKilled; public
	 * boolean engineerKilled; public boolean sentryKilled; public boolean
	 * dispenserKilled; public int[] cachedAmmoCount=new int[15]; public int
	 * headshotsRow; public int sapperTime; public EntityLivingBase
	 * buildingOwnerKill;
	 */

	public WeaponsCapability(LivingEntity entity) {
		this.owner = entity;

		for (int i = 0; i < this.predictionList.length; i++)
			this.predictionList[i] = new ArrayDeque<>();
		this.entityData = new SynchedEntityData(entity);
		this.entityData.define(CRIT_TIME, 0);
		this.entityData.define(HEADS, 0);
		this.entityData.define(HEAL_TARGET, -1);
		this.entityData.define(METAL, MAX_METAL);
		this.entityData.define(FEIGN, false);
		this.entityData.define(INVIS, false);
		this.entityData.define(DISGUISED, false);
		this.entityData.define(DISGUISE_TYPE, "");
		this.entityData.define(EXP_JUMP, false);
		this.entityData.define(CHARGING, false);
		this.entityData.define(TOKEN_USED, -1);
		this.entityData.define(CAN_FIRE, (byte) 0);
		this.entityData.define(UBER_VIEW, 0f);
		this.entityData.define(GRAPPLED, false);
		this.entityData.define(GRAPPLING, false);
		for (RageType type : RageType.values()) {
			this.entityData.define(RAGE.get(type), 0f);
			this.entityData.define(RAGE_ACTIVE.get(type), false);
		}
		// this.nextBossTicks = (int) (entity.world.getWorldTime() +
		// entity.getRNG().nextInt(360000));
	}

	public int getCritTime() {
		return this.entityData.get(CRIT_TIME);
	}

	public void setCritTime(int time) {
		this.entityData.set(CRIT_TIME, time);
	}

	public int getHealTarget() {
		return this.entityData.get(HEAL_TARGET);
	}

	public void setHealTarget(int target) {
		this.entityData.set(HEAL_TARGET, target);
	}

	public float getUberView() {
		return this.entityData.get(UBER_VIEW);
	}

	public void setUberView(float uber) {
		this.entityData.set(UBER_VIEW, uber);
	}

	public int getHeads() {
		return this.entityData.get(HEADS);
	}

	public int getMetal() {
		return this.entityData.get(METAL);
	}

	public boolean hasMetal(int metal) {
		boolean hasIngot = this.owner instanceof Player && TF2Util.hasEnoughItem(
				((Player) this.owner).getInventory(), stackL -> stackL.getItem() == Items.IRON_INGOT,
				Mth.ceil((metal - this.getMetal()) / 50f));
		return hasIngot || this.getMetal() >= metal;
	}

	public int consumeMetal(int metal, boolean allowPartial) {
		int usedMetal = 0;

		if (this.owner instanceof Player) {
			ItemStack ingot = new ItemStack(Items.IRON_INGOT);
			while (((allowPartial && this.getMetal() == 0) || (!allowPartial && this.getMetal() < metal))
					&& ((Player) this.owner).getInventory().contains(ingot)) {
				((Player) this.owner).getInventory().clearOrCountMatchingItems(s -> s.is(Items.IRON_INGOT), 1,
						((Player) this.owner).inventoryMenu.getCraftSlots());
				this.setMetal(this.getMetal() + 50);
			}
		}
		if (allowPartial || this.getMetal() >= metal) {
			usedMetal = Math.min(this.getMetal(), metal);
			this.setMetal(this.getMetal() - usedMetal);
		}
		return usedMetal;
	}

	public void setMetal(int metal) {
		this.entityData.set(METAL, Mth.clamp(metal, 0, this.getMaxMetal()));
	}

	public void giveMetal(int metal) {
		this.entityData.set(METAL, Mth.clamp(this.getMetal() + metal, 0, this.getMaxMetal()));
	}

	public int getMaxMetal() {
		return (int) ((this.owner instanceof EntityEngineer ? TF2ConfigVars.maxMetalEngineer
				: WeaponsCapability.MAX_METAL) * this.maxmetal);
	}

	public float getRage(RageType type) {
		if (type == null)
			return 0;
		return this.entityData.get(RAGE.get(type));
	}

	public void setRage(RageType type, float rage) {
		this.entityData.set(RAGE.get(type), rage);
	}

	public boolean isRageActive(RageType type) {
		if (type == null)
			return false;
		return this.entityData.get(RAGE_ACTIVE.get(type));
	}

	public void setRageActive(RageType type, boolean active, float drain) {

		this.entityData.set(RAGE_ACTIVE.get(type), active);
		if (active)
			this.rageDrain[type.ordinal()] = drain * 0.1f;
		else
			this.rageDrain[type.ordinal()] = 0f;
	}

	/*
	 * public float getPhlogRage() { return this.dataManager.get(PHLOG_RAGE); }
	 * 
	 * public void setPhlogRage(float rage) { this.dataManager.set(PHLOG_RAGE,
	 * rage); }
	 * 
	 * public float getKnockbackRage() { return
	 * this.dataManager.get(KNOCKBACK_RAGE); }
	 * 
	 * public void setKnockbackRage(float rage) {
	 * this.dataManager.set(KNOCKBACK_RAGE, rage); }
	 */
	public boolean isInvisible() {
		return this.entityData.get(INVIS);
	}

	public void setInvisible(boolean invis) {
		this.entityData.set(INVIS, invis);
	}

	public void setDisguised(boolean val) {
		this.entityData.set(DISGUISED, val);
		this.disguiseTicks = 0;
		if (!val) {
			this.stabbedDisguise = false;
		}
	}

	public boolean isDisguised() {
		return this.entityData.get(DISGUISED);
	}

	public void setExpJump(boolean val) {
		this.entityData.set(EXP_JUMP, val);
		if (val && !this.owner.level.isClientSide) {
			this.expJumpGround = 2;
			TF2Util.sendTracking(new TF2Message.ActionMessage(28, this.owner), this.owner);
		}
	}

	public boolean isExpJump() {
		return this.entityData.get(EXP_JUMP);
	}

	public void setDisguiseType(String val) {
		this.entityData.set(DISGUISE_TYPE, val);
	}

	public String getDisguiseType() {
		return this.entityData.get(DISGUISE_TYPE);
	}

	public void setFeign(boolean val) {
		this.entityData.set(FEIGN, val);
	}

	public boolean isFeign() {
		return this.entityData.get(FEIGN);
	}

	public void setCharging(boolean val) {
		this.entityData.set(CHARGING, val);
		if (!val)
			this.chargeTicks = 0;
	}

	public boolean isCharging() {
		return this.entityData.get(CHARGING);
	}

	public void setUsedToken(int val) {
		this.entityData.set(TOKEN_USED, val);
	}

	public int getUsedToken() {
		return this.entityData.get(TOKEN_USED);
	}

	public boolean canFire(InteractionHand hand, boolean primary) {
		int flags = this.entityData.get(CAN_FIRE);
		return (flags & (1 << hand.ordinal())) == (1 << hand.ordinal())
				&& (((flags & 4) == 4 && primary) || ((flags & 8) == 8 && !primary));

	}

	public void setCanFire(boolean fire, InteractionHand hand, boolean primary) {
		int flag = 0;
		if (hand == InteractionHand.MAIN_HAND)
			flag += 1;
		else
			flag += 2;
		if (primary)
			flag += 4;
		else
			flag += 8;
		if (fire)
			this.entityData.set(CAN_FIRE, (byte) (this.entityData.get(CAN_FIRE) | flag));
		else
			this.entityData.set(CAN_FIRE, (byte) (this.entityData.get(CAN_FIRE) & ~(flag)));
	}

	public void addEffectCooldown(String name, int time) {
		this.effectsCool.put(name, time);
		if (this.owner instanceof ServerPlayer)
			TF2weapons.network.sendTo(new TF2Message.EffectCooldownMessage(name, time), (ServerPlayer) this.owner);
	}

	public void addHead(ItemStack weapon) {

		this.collectedHeadsTime = owner.tickCount;
		this.entityData.set(HEADS, this.entityData.get(HEADS) + 1);
		/*
		 * this.owner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.
		 * MAX_HEALTH) .applyModifier(new AttributeModifier(HEADS_HEALTH,
		 * "Heads modifier", collectedHeads, 0));
		 * this.owner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.
		 * MOVEMENT_SPEED) .applyModifier(new AttributeModifier(HEADS_SPEED,
		 * "Heads modifier", collectedHeads * 0.04, 2));
		 */
		this.owner.heal(TF2Attribute.getModifier("Max Health Kill", weapon, 0, this.owner));
	}

	public boolean focusedShot(ItemStack stack) {
		int stackLevel = (int) TF2Attribute.getModifier("Focus", stack, 0, owner);
		return stackLevel > 0 && this.focusShotTicks > this.focusShotTime(stack);
	}

	public int focusShotTime(ItemStack stack) {
		int stackLevel = (int) TF2Attribute.getModifier("Focus", stack, 0, owner);
		return (68 - stackLevel * 21 + ((ItemUsable) stack.getItem()).getFiringSpeed(stack, owner) / 50) * 2;
	}

	public void onChangeValue(EntityDataAccessor<?> param, Object newValue) {
		if (param.getId() == 11 && !((Boolean) newValue)) {
			this.chargeTicks = 0;
		}
		if (param.getId() == 6 && (Boolean) newValue)
			this.expJumpGround = 2;
	}

	public boolean isUsingParachute() {
		ItemStack stack = this.owner.getItemBySlot(EquipmentSlot.CHEST);
		return stack.getItem() instanceof ItemParachute && stack.getTag().getBoolean("Deployed");
	}

	public void tick() {
		// System.out.println("graczin"+state);

		Iterator<Entry<String, Integer>> efiterator = effectsCool.entrySet().iterator();
		while (efiterator.hasNext()) {
			Entry<String, Integer> entry = efiterator.next();
			entry.setValue(entry.getValue() - 1);
			if (entry.getValue() <= 0)
				efiterator.remove();
		}
		if (!this.owner.level.isClientSide && TF2ConfigVars.headCountDrop && this.entityData.get(HEADS) > 0
				&& collectedHeadsTime < this.owner.tickCount
						- Math.max(100, 2000 - Mth.log2(this.entityData.get(HEADS)) * 300)) {
			this.entityData.set(HEADS, this.entityData.get(HEADS) - 1);
			collectedHeadsTime = this.owner.tickCount;

			/*
			 * this.owner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.
			 * MAX_HEALTH) .applyModifier(new AttributeModifier(HEADS_HEALTH,
			 * "Heads modifier", collectedHeads, 0));
			 * this.owner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.
			 * MOVEMENT_SPEED) .applyModifier(new AttributeModifier(HEADS_SPEED,
			 * "Heads modifier", collectedHeads * 0.04, 2));
			 */
		}

		/*
		 * if(itemProperties.get(client).get(player)==null){
		 * itemProperties.get(client).put(player, new NBTTagCompound()); }
		 */
		// player.getEntityData().setTag("TF2", tag);
		// this.zombieHuntTicks--;

		this.ticksBash--;
		this.itProtection--;
		this.fanCool--;
		this.ticksTotal++;
		this.lastFire -= 50;
		if (this.airJumps > 0 && this.owner.isOnGround()) {
			this.airJumps = 0;
		}

		if (this.owner.isSprinting() && this.getUsedToken() >= 0) {
			if (this.owner.isOnGround()) {
				owner.setDeltaMovement(owner.getDeltaMovement().x * (0.82D), owner.getDeltaMovement().y, owner.getDeltaMovement().z);
				owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y, owner.getDeltaMovement().z * (0.82D));
			}
			// owner.setSprinting(false);
		}

		if (!this.owner.level.isClientSide) {
			if (this.owner.tickCount % 20 == 0 && (this.owner instanceof EntityMedic || this.getUsedToken() == 6)) {
				int lastHitTime = this.owner.tickCount - this.owner.getPersistentData().getInt("lasthit");
				if (lastHitTime >= 120)
					this.owner.heal(0.6f);
				else if (lastHitTime >= 60)
					this.owner.heal(TF2Util.lerp(0.6f, 0.3f, (lastHitTime - 60) / 60f));
				else
					this.owner.heal(0.3f);
			}
		}

		if (this.owner.level.isClientSide)
			TF2weapons.proxy.updateSniperMouseSlow(this);

		if (this.owner.tickCount % 2 == 0)
			for (RageType type : RageType.values()) {
				if (this.isRageActive(type)) {
					this.setRage(type, Math.max(this.getRage(type) - this.rageDrain[type.ordinal()], 0f));
					if (this.getRage(type) <= 0f)
						this.setRageActive(type, false, 0f);
				}
			}

		if (this.reloadCool > 0)
			this.reloadCool -= 50;
		boolean hadItem = false;
		boolean continueReload = false;

		if (!this.owner.level.isClientSide && this.owner instanceof Player && this.owner.tickCount % 20 == 0) {
			this.maxmetal = TF2Attribute.getModifier("Max Metal",
					TF2Util.getBestItem(((Player) this.owner).getInventory(), (stack1,
							stack2) -> (int) Math.signum(TF2Attribute.getModifier("Max Metal", stack1, 1, this.owner)
									- TF2Attribute.getModifier("Max Metal", stack2, 1, this.owner)),
							stackl -> TF2Attribute.getModifier("Max Metal", stackl, 1, this.owner) != 1),
					1, this.owner);
		}

		if (this.grapplingHook != null) {

			if (!this.grapplingHook.isAlive()) {
				this.setGrapplingHook(null);
			}

			else if (this.grapplingHook.sticked) {
				// if (this.owner.ticksExisted % 40 == 0 && this.grapplingHook.stickedEntity ==
				// null && TF2Util.pierce(this.owner.world, this.owner, this.owner.posX,
				// this.owner.posY + this.owner.getEyeHeight(), this.owner.posZ,
				// this.grapplingHook.posX, this.grapplingHook.posY, this.grapplingHook.posZ,
				// false, 0f, false).get(0).typeOfHit == RayTraceResult.HitResult.Type.BLOCK)
				// this.setGrapplingHook(null);
				// else {
				Vec3 motionVec = new Vec3(this.grapplingHook.getX() - this.owner.getX(),
						this.grapplingHook.getY() - this.owner.getY() - this.owner.getEyeHeight(),
						this.grapplingHook.getZ() - this.owner.getZ());
				if (motionVec.lengthSqr() > 0.77 * 0.77)
					motionVec = motionVec.normalize().scale(0.77);
				this.owner.setDeltaMovement((motionVec.x), this.owner.getDeltaMovement().y, this.owner.getDeltaMovement().z);
				this.owner.setDeltaMovement(this.owner.getDeltaMovement().x, (motionVec.y), this.owner.getDeltaMovement().z);
				this.owner.setDeltaMovement(this.owner.getDeltaMovement().x, this.owner.getDeltaMovement().y, (motionVec.z));
				this.owner.hasImpulse = true;
				this.owner.setOnGround(false);
				this.owner.hurtMarked = true;
				this.setGrappled(true);
				// }
			} else {
				this.setGrappled(false);
			}
		}

		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = owner.getItemInHand(hand);

			boolean isUseableWeapon = !stack.isEmpty() && stack.getItem() instanceof ItemUsable
					&& (hand == InteractionHand.MAIN_HAND
							|| (hand == InteractionHand.OFF_HAND && !hadItem
									&& ((ItemUsable) stack.getItem()).getDoubleWieldBonus(stack, owner) != 1)
							|| ItemUsable.isDoubleWielding(owner));

			if (isUseableWeapon) {

				hadItem = true;
				ItemUsable item = (ItemUsable) stack.getItem();

				int state = item.getStateOverride(stack, owner, this.state);
				if (this.fireCoolReduced && this.owner.level.isClientSide) {
					this.setPrimaryCooldown(hand,
							this.getPrimaryCooldown(hand) - (int) (item.getFiringSpeed(stack, this.owner)
									* (1 - (1 / TF2Attribute.getModifier("Fire Rate Hit", stack, 1, this.owner)))));
					this.fireCoolReduced = false;
				}

				if (!(item instanceof ItemWeapon) || ((ItemWeapon) item).getClip(stack) == 0)
					this.autoFire = false;

				if (!(this.owner instanceof Player)
						|| (this.owner.level.isClientSide && this.owner != ClientProxy.getLocalPlayer()))
					item.inventoryTick(stack, owner.level, owner, 0, true);
				WeaponData.WeaponDataCapability stackcap = stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null);
				if (TF2Attribute.getModifier("Focus", stack, 0, owner) != 0) {
					this.focusShotTicks += this.owner.isSprinting() ? 1 : 2;
					if (this.owner.isSprinting() && this.focusShotTicks >= (this.focusShotTime(stack) * 3 / 4)) {
						this.focusShotTicks = this.focusShotTime(stack) * 3 / 4;
					}
					this.focusShotRemaining--;
				}
				if (stackcap.active == 1) {
					if (!this.lastWeapon.isEmpty()) {
						stackcap.fire1Cool = stackcap.fire2Cool = (int) Math.max(stackcap.fire1Cool,
								((TF2Attribute.getModifier("Holster Time", this.lastWeapon, 1f, this.owner) - 1f)
										* item.getDeployTime(stack, owner)));

						this.lastWeapon = ItemStack.EMPTY;
					}
					if (stackcap.fire1Cool <= 0) {
						this.startUsingItem(hand, stack);
					}
				}

				if (stackcap.active == 2) {
					this.stackActive.put(hand, stack);
				}
				if (owner.level.isClientSide)
					stack.setPopTime(0);
				if (owner instanceof EntityTF2Character) {
					EntityTF2Character shooter = ((EntityTF2Character) owner);
					if (shooter.getTarget() != null) {
						shooter.targetPrevPos[1] = shooter.targetPrevPos[0];
						shooter.targetPrevPos[3] = shooter.targetPrevPos[2];
						shooter.targetPrevPos[5] = shooter.targetPrevPos[4];
						shooter.targetPrevPos[0] = shooter.getTarget().getX();
						shooter.targetPrevPos[2] = shooter.getTarget().getY();
						shooter.targetPrevPos[4] = shooter.getTarget().getZ();
					}
				}

				this.setCanFire(item.canFireInternal(owner.level, owner, stack, hand), hand, true);
				this.setCanFire(item.canAltFireInternal(owner.level, owner, stack, hand), hand, false);

				this.stateDo(owner, stack, hand, state);

				if ((!owner.level.isClientSide || owner != Minecraft.getInstance().player)
						&& stack.getItem() instanceof ItemWeapon && ((ItemWeapon) stack.getItem()).hasClip(stack)
						&& (!item.searchForAmmo(owner, stack).isEmpty() || owner.level.isClientSide)) {
					ItemWeapon weapon = (ItemWeapon) item;
					boolean full = (weapon.getClip(stack) >= weapon.getWeaponClipSize(stack, owner)
							&& TF2Attribute.getModifier("Overload", stack, 0, owner) == 0);
					boolean emptyMag = weapon.getClip(stack) == 0
							&& TF2Attribute.getModifier("Auto Fire", stack, 0, owner) == 0;
					if (((state & 4) != 0 || emptyMag || continueReload) && stackcap.active == 2
							&& this.reloadingHand == null && !full && this.reloadCool <= 0
							&& (this.getPrimaryCooldown() <= 0)
							&& owner.getEffect(TF2weapons.stun) == null) {
						this.reloadingHand = hand;
						this.reloadCool = ((ItemWeapon) stack.getItem()).getWeaponFirstReloadTime(stack, owner);

						if (!owner.level.isClientSide && owner instanceof ServerPlayer)
							TF2weapons.network.sendTo(new TF2Message.UseMessage(weapon.getClip(stack), true, -1, hand),
									(ServerPlayer) owner);

						if (owner.level.isClientSide && ((ItemWeapon) stack.getItem()).IsReloadingFullClip(stack))
							TF2weapons.proxy.playReloadSound(owner, stack);

					} else if (this.getPrimaryCooldown() <= 0
							|| ((ItemWeapon) stack.getItem()).IsReloadingFullClip(stack))
						while (this.reloadingHand == hand && this.reloadCool <= 0 && !full) {
							// System.out.println("On client:
							// "+owner.world.isRemote);
							int maxAmmoUse = item.getAmmoAmount(owner, stack);
							int consumeAmount = 0;

							boolean overload = weapon.getClip(stack) >= weapon.getWeaponClipSize(stack, owner);
							if (((ItemWeapon) stack.getItem()).IsReloadingFullClip(stack)) {
								consumeAmount += Math.min(
										weapon.getWeaponClipSize(stack, owner) - weapon.getClip(stack), maxAmmoUse);
								weapon.consumeClip(stack, -consumeAmount, owner, hand);
								maxAmmoUse -= consumeAmount;

								/*
								 * if (maxAmmoUse > 0 && ItemUsable.isDoubleWielding(owner)) { consumeAmount +=
								 * Math.min(owner.getHeldItemOffhand().getItemDamage(), maxAmmoUse);
								 * owner.getHeldItemOffhand() .setItemDamage(Math.max(0, stack.getItemDamage() -
								 * consumeAmount)); }
								 */

							} else {
								consumeAmount = 1;
								weapon.consumeClip(stack, -1, owner, hand);
								// TF2weapons.proxy.playReloadSound(owner, stack);
							}

							if (overload) {
								consumeAmount = 0;
								weapon.consumeClip(stack, 1, owner, hand);
								weapon.onOverload(stack, owner, hand);
							}
							if (!owner.level.isClientSide)
								item.consumeAmmoGlobal(owner, stack, consumeAmount);
							if (!owner.level.isClientSide && owner instanceof ServerPlayer)
								TF2weapons.network.sendTo(new TF2Message.UseMessage(weapon.getClip(stack),
										weapon.getClip(stack) != weapon.getWeaponClipSize(stack, owner), -1, hand),
										(ServerPlayer) owner);

							this.reloadCool += ((ItemWeapon) stack.getItem()).getWeaponReloadTime(stack, owner);

							if (weapon.getClip(stack) == weapon.getWeaponClipSize(stack, owner)) {
								this.stopReload();
								continueReload = true;
							}
						}
				} else if (this.reloadingHand == hand
						&& !(owner.level.isClientSide && owner == Minecraft.getInstance().player))
					this.reloadingHand = null;
			}
			if (this.stackActive.get(hand) != null) {
				if (owner.level.isClientSide && !(owner instanceof Player) && isUseableWeapon
						&& stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).isPresent()
						&& stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active != 2
						&& stack.getItem().shouldCauseReequipAnimation(this.stackActive.get(hand), stack, false)) {
					stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active = 2;
				}
				if (!(isUseableWeapon && stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).isPresent()
						&& stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active == 2)) {
					boolean revert = false;
					ItemStack activestack = this.stackActive.get(hand);
					if (activestack.getCount() == 0) {
						activestack.setCount(1);
						revert = true;
					}
					if (activestack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active == 2) {
						this.setInactiveHand(hand, activestack);
					}
					if (revert) {
						activestack.setCount(0);
					}
				}
			}
		}
		if (!hadItem) {
			owner.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(ItemMinigun.slowdown);
			owner.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(ItemSniperRifle.slowdown);
			owner.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(ItemHuntsman.slowdown);
		}

		if (!this.owner.level.isClientSide && this.entityData.isDirty()) {
			TF2Util.sendTracking(new TF2Message.CapabilityMessage(this.owner, false), this.owner);
		}

		MobEffectInstance charging = owner.getEffect(TF2weapons.charging);

		/*
		 * if (owner instanceof EntityPlayer && owner.onGround && owner.motionY < 0) {
		 * this.gravity = owner.motionY/0.98; System.out.println(this.gravity); }
		 */

		if (!owner.isOnGround() && charging != null)
			this.setExpJump(true);

		if (this.isExpJump() && this.canExpJump()) {
			boolean enchanted = TF2ConfigVars.enchantedExplosion && !owner.isFallFlying() && !this.isUsingParachute()
					&& !(owner instanceof Player && ((Player) owner).getAbilities().flying)
					&& !owner.getItemBySlot(EquipmentSlot.FEET).getItem().builtInRegistryHolder().key().location().toString()
							.equals("tconstruct:slime_boots");
			if (owner.isOnGround()) {
				BlockPos pos = owner.blockPosition().below();
				BlockState block = owner.level.getBlockState(pos);
				owner.setDeltaMovement(owner.getDeltaMovement().x * (block.getFriction(owner.level, pos, owner)), owner.getDeltaMovement().y, owner.getDeltaMovement().z);
				owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y, owner.getDeltaMovement().z * (block.getFriction(owner.level, pos, owner)));
			}

			if (this.expJumpGround > 0) {
				this.expJumpGround--;
				owner.setOnGround(false);
				owner.hasImpulse = true;
			} else if (owner.isOnGround() || owner.isInWater()) {
				this.killsAirborne = 0;
				this.setExpJump(false);
				if (enchanted && this.oldFactor != 0) {
					owner.flyingSpeed = this.oldFactor;
					enchanted = false;
				}
			}

			if (!enchanted && owner.flyingSpeed == 0 && this.oldFactor != 0) {
				owner.flyingSpeed = this.oldFactor;
			}
			if (enchanted) {
				if (owner.flyingSpeed != 0) {
					this.oldFactor = owner.flyingSpeed;
					owner.flyingSpeed = 0;
				}
				// System.out.println(""+owner.motionX+ " " + owner.motionY +" "+ owner.motionZ
				// + " "+ (owner.posX - this.lastPosX));

				/*
				 * if(!owner.world.isRemote && owner instanceof EntityPlayer &&
				 * !TF2weapons.server.isSinglePlayer()) { boolean loaded =
				 * owner.world.isBlockLoaded(owner.getPosition()); owner.motionX = loaded ? 50 :
				 * 5; owner.motionZ = loaded ? 50 : 5; }
				 */
				if (owner.level.isClientSide && owner instanceof Player
						&& !owner.level.hasChunkAt(owner.blockPosition())) {
					owner.setDeltaMovement(owner.getDeltaMovement().x * (0.99), owner.getDeltaMovement().y, owner.getDeltaMovement().z);
					owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y, owner.getDeltaMovement().z * (0.99));
				}
				if ((!owner.level.isClientSide || owner instanceof Player)
						&& !(TF2weapons.squakeLoaded && owner instanceof Player)) {

					double speed = Math.sqrt(owner.getDeltaMovement().x * owner.getDeltaMovement().x + owner.getDeltaMovement().z * owner.getDeltaMovement().z);

					Vec3 moveDir = TF2Util.getMovementVector(owner);
					double combSpeed = owner.getDeltaMovement().x * moveDir.x + owner.getDeltaMovement().z * moveDir.y;

					double maxSpeed = charging != null ? owner.getSpeed() * 2.16 : 0.026;
					double friction = charging != null ? 0.08 : 0.4;

					combSpeed = (maxSpeed) - combSpeed;
					double accel = Math.max(speed, maxSpeed) * friction;
					if (accel > combSpeed)
						accel = combSpeed;

					if (accel > 0) {
						owner.setDeltaMovement(owner.getDeltaMovement().x + (moveDir.x * accel), owner.getDeltaMovement().y, owner.getDeltaMovement().z);
						owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y, owner.getDeltaMovement().z + (moveDir.y * accel));
					}
					owner.setDeltaMovement(owner.getDeltaMovement().x / (0.91), owner.getDeltaMovement().y, owner.getDeltaMovement().z);
					owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y, owner.getDeltaMovement().z / (0.91));

				}
				owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y / (0.98), owner.getDeltaMovement().z);
				owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y + (0.08 - TF2ConfigVars.explosiveJumpGravity), owner.getDeltaMovement().z);

			}
			owner.getBoundingBox();
			// owner.motionX += owner.motionX -livin
			// owner.motionZ = owner.motionZ * 1.035;
		} else if (this.oldFactor != 0) {
			owner.flyingSpeed = this.oldFactor;
			this.oldFactor = 0;
		}

		if (this.isExpJump() || charging != null) {
			Vec3 vec = new Vec3(owner.getDeltaMovement().x, owner.getDeltaMovement().y, owner.getDeltaMovement().z);
			Vec3 vec2 = new Vec3(owner.getDeltaMovement().x, 0, owner.getDeltaMovement().z);
			if (owner.isOnGround())
				vec = vec2;
			double motion = vec.length();
			// double motiona = vec2.lengthVector();
			if (motion >= 0.375 && (TF2ConfigVars.allowTrimp == 2 || (TF2ConfigVars.allowTrimp == 1 && Direction
					.getNearest((float) vec.x, (float) vec.y, (float) vec.z) != Direction.DOWN))) {
				vec2 = vec2.normalize();
				Vec3 entPos = owner.position();
				Vec3 vecOff = vec.add(vec2.scale(owner.getBbWidth() * 1.5));
				BlockHitResult rayTrace = TF2Util.rayTraceBlocks(owner.level, entPos, entPos.add(vecOff));
				BlockPos pos = new BlockPos(vecOff.add(entPos));
				if (rayTrace != null) {
					pos = rayTrace.getBlockPos().relative(rayTrace.getDirection());
					// System.out.println(rayTrace.getBlockPos());
				}
				if (owner.level.getBlockState(pos.below()).isCollisionShapeFullBlock(owner.level, pos.below())) {
					Vec3 step = TF2Util.getHeightVec(owner.level, pos);
					Vec3 normal = step.normalize();
					if (normal.y != 1D && normal.dot(vecOff) < 0 && !(charging != null && normal.y > 0.75)) {
						double backoff = vec.dot(normal);
						vec = new Vec3(vec.x - normal.x * backoff, vec.y - normal.y * backoff,
								vec.z - normal.z * backoff);
						owner.setDeltaMovement((vec.x), owner.getDeltaMovement().y, owner.getDeltaMovement().z);
						owner.setDeltaMovement(owner.getDeltaMovement().x, (vec.y), owner.getDeltaMovement().z);
						owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y, (vec.z));
						if (charging != null) {
							double mult = motion / vec.length();
							owner.setDeltaMovement(owner.getDeltaMovement().x * (mult), owner.getDeltaMovement().y, owner.getDeltaMovement().z);
							owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y * (mult), owner.getDeltaMovement().z);
							owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y, owner.getDeltaMovement().z * (mult));
						}
						rayTrace = TF2Util.rayTraceBlocks(owner.level, vecOff.add(entPos), vecOff.add(entPos).add(vec));
						if (rayTrace != null) {
							owner.setPos(owner.getX(),
									owner.getY() + 1 - rayTrace.getLocation().y + rayTrace.getBlockPos().getY(), owner.getZ());
						}

					}
				}
			}
		}
		if (!owner.level.isClientSide && this.disguiseTicks > 0) {

			if (++this.disguiseTicks >= 40) {
				this.disguise();
			} else if (this.invisTicks < 20) {
				((ServerLevel) owner.level).sendParticles(ParticleTypes.LARGE_SMOKE, owner.getX(), owner.getY(),
						owner.getZ(), 2, 0.2, 1, 0.2, 0.04f);
			}
		}

		if (owner.level.isClientSide) {

			ClientProxy.doChargeTick(owner);
		}
		if (!owner.level.isClientSide && charging != null) {
			if (ItemChargingTarge.getChargingShield(owner).isEmpty()) {
				owner.removeEffect(TF2weapons.charging);
			}
			Vec3 start = owner.position().add(0, owner.getBbHeight() / 2, 0);
			Vec3 end = start.add(-Mth.sin(owner.getYRot() / 180.0F * (float) Math.PI) * 0.7, 0,
					Mth.cos(owner.getYRot() / 180.0F * (float) Math.PI) * 0.7);
			HitResult result = TF2Util
					.pierce(owner.level, owner, start.x, start.y, start.z, end.x, end.y, end.z, false, 0.5f, false)
					.get(0);
			Entity bashed = TF2HitResult.getEntity(result);
			if (bashed != null) {
				float damage = 5;
				if (charging.getDuration() > 30) {
					damage *= 0.5f;
				}
				TF2Util.dealDamage(bashed, bashed.level, owner,
						ItemChargingTarge.getChargingShield(owner), 0, damage,
						TF2Util.causeDirectDamage(ItemChargingTarge.getChargingShield(owner), owner));

				this.bashCritical = charging.getDuration() < 20;
				if (charging.getDuration() < 12)
					TF2Util.playSound(owner, TF2Sounds.WEAPON_SHIELD_HIT_RANGE, 3F, 1F);
				else
					TF2Util.playSound(owner, TF2Sounds.WEAPON_SHIELD_HIT, 0.8F, 1F);
				this.ticksBash = 20;
				owner.setDeltaMovement((0), owner.getDeltaMovement().y, owner.getDeltaMovement().z);
				owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y, (0));
				owner.removeEffect(TF2weapons.charging);

			}
		}
		if (owner.level.isClientSide && WeaponsCapability.get(owner).isDisguised() != this.lastDisgused) {
			this.lastDisgused = WeaponsCapability.get(owner).isDisguised();
			if (owner instanceof Player)
				((Player) owner).refreshDisplayName();
		}
		String disguisetype = this.getDisguiseType();

		if (owner.level.isClientSide && !disguisetype.equals(this.lastDisguiseValue)) {
			if (owner instanceof Player) {
				((Player) owner).refreshDisplayName();
			}
			this.disguiseCounter += 1;
			this.lastDisguiseValue = disguisetype;
			if (this.getDisguiseType().startsWith("P:")) {
				this.skinDisguise = null;
				Player playerDisguise = TF2Util.getPlayerByName(this.owner.level, getDisguiseType().substring(2));
				if (playerDisguise != null && TF2PlayerCapability.get(playerDisguise).isForceClassTexture()
						&& WeaponsCapability.get(playerDisguise).getUsedToken() >= 0) {
					this.skinType = "default";
					this.skinDisguise = new ResourceLocation(RenderTF2Character.TEXTURE_PATH_BASE + (TF2Util.getTeamForDisplay(playerDisguise) == 1 ? "blu" : "red")
							+ "/" + TF2Class.getClass(WeaponsCapability.get(playerDisguise).getUsedToken()).getName() + ".png");
				} else {
					TF2weapons.proxy.loadDisguiseSkin(this, getDisguiseType().substring(2));
				}
			}

		}

		/*
		 * Minecraft.getMinecraft().getSkinManager().loadSkin(new
		 * MinecraftProfileTexture( "http://skins.minecraft.net/MinecraftSkins/"+owner.
		 * getDataManager().get(TF2EventBusListener.ENTITY_DISGUISE_TYPE
		 * ).substring(2)+".png",null), Type.SKIN,new SkinAvailableCallback(){
		 * 
		 * @Override public void skinAvailable(Type typeIn, ResourceLocation location,
		 * MinecraftProfileTexture profileTexture) { if(typeIn==Type.SKIN){
		 * this.skinDisguise=location; System.out.println("RetrieveD"); } }
		 * 
		 * });
		 */
		if (owner.level.isClientSide && owner != ClientProxy.getLocalPlayer()) {
			// System.out.println("uber
			// "+owner.getActivePotionEffect(TF2weapons.uber).getDuration());
			Iterator<MobEffectInstance> iterator = owner.getActiveEffects().iterator();
			while (iterator.hasNext()) {
				MobEffectInstance effect = iterator.next();
				if (effect.getDuration() <= 0
						&& (effect.getEffect() instanceof PotionTF2 || effect.getEffect() instanceof PotionTF2Item)) {
					iterator.remove();
				}
			}
		}
		if (!owner.level.isClientSide && owner.tickCount % 10 == 0 && this.isFeign()
				&& ItemCloak.getFeignDeathWatch(owner).isEmpty()) {
			this.setFeign(false);
		}
		if (!owner.level.isClientSide && owner.fallDistance > 0
				&& owner.getItemBySlot(EquipmentSlot.FEET).getItem() == TF2weapons.itemMantreads) {
			TF2Util.stomp(owner);
		}
		if (this.isInvisible()) {
			// System.out.println("cloak");
			ItemStack cloak = ItemCloak.searchForWatches(owner).getB();
			boolean feign = !cloak.isEmpty() && ((ItemCloak) cloak.getItem()).isFeignDeath(cloak, owner);
			boolean visible = owner.hurtTime == 10 && !feign;
			if (!visible && !feign) {
				List<Entity> closeEntities = owner.level.getEntities(owner,
						owner.getBoundingBox().inflate(1, 2, 1), input -> input instanceof LivingEntity && !TF2Util.isAlliedTo(owner, input));
				for (Entity ent : closeEntities) {
					if (ent.distanceToSqr(owner) < 1) {
						visible = true;
					}
					break;
				}
			}
			if (visible) {
				// System.out.println("reveal");
				this.invisTicks = Math.min(10, this.invisTicks);
				owner.setInvisible(false);
			}
			if (feign)
				this.invisTicks = 20;
			this.invisTicks += 2;
			if (owner.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks < 20) {} else if (!owner.isInvisible()) {
				// System.out.println("full");
				owner.setInvisible(true);
			}
			boolean active = owner.level.isClientSide || !cloak.isEmpty();
			if (!active) {
				this.setInvisible(false);
				owner.setInvisible(false);
				// System.out.println("decloak");
			}
		} else if (owner.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks > 0) {
			this.invisTicks = Math.min(20, this.invisTicks - 1);
			if (owner.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks == 0) {
				owner.setInvisible(false);
			}
		}

		this.lastPosX = owner.getX();
		this.lastPosY = owner.getY();
		this.lastPosZ = owner.getZ();
	}

	public boolean hasCapabilityLegacy(Capability<?> capability, Direction facing) {
		return TF2weapons.WEAPONS_CAP != null && capability == TF2weapons.WEAPONS_CAP;
	}

	@Override
	public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(Capability<T> capability, @javax.annotation.Nullable Direction facing) {
		T value = this.getCapabilityLegacy(capability, facing);
		return value == null ? net.minecraftforge.common.util.LazyOptional.empty() : net.minecraftforge.common.util.LazyOptional.of(() -> value);
	}

	public <T> T getCapabilityLegacy(Capability<T> capability, Direction facing) {
		if (TF2weapons.WEAPONS_CAP != null && capability == TF2weapons.WEAPONS_CAP)
			return (T) (Object) this;
		return null;
	}

	public boolean startedPress() {
		if (this.pressedStart) {
			this.pressedStart = false;
			return true;
		}
		return false;

	}

	public boolean shouldShoot(LivingEntity player, int state, InteractionHand hand, int actualState) {
		return (!(!player.level.isClientSide && player instanceof Player
				&& this.predictionList[hand.ordinal() + (state == 1 ? 0 : 2)].isEmpty()))
				&& !((player.level.isClientSide || !(player instanceof Player)) && (actualState & state) != state);
	}

	public void stateDo(LivingEntity player, ItemStack stack, InteractionHand hand, int state) {
		if (!TF2Util.canInteract(player))
			return;

		ItemUsable item = (ItemUsable) stack.getItem();
		WeaponData.WeaponDataCapability stackcap = WeaponData.getCapability(stack);

		if ((state & 1) != 0 && stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active == 2)

			item.fireTick(stack, player, player.level);
		while (stackcap.fire1Cool <= 0 && shouldShoot(player, 1, hand, state)) {

			TF2Message.PredictionMessage message = null;
			if (!player.level.isClientSide && player instanceof Player) {
				message = this.predictionList[hand.ordinal()].pollLast();
				// this.predictionList[hand.ordinal()] = null;
			}

			if (message == null) {
				boolean canFire = item.canFireInternal(player.level, player, stack, hand);
				if (!canFire)
					break;
			} else {

				this.mainHand = message.hand == InteractionHand.MAIN_HAND;
				if (message.hand != hand || !item.canFireInternal(player.level, player, stack, hand))
					break;
			}
			int fireRate = item.getFiringSpeed(stack, player);
			stackcap.fire1Cool += Math.max(1, fireRate);
			if (ItemUsable.isDoubleWielding(player)) {
				if (hand == InteractionHand.MAIN_HAND)
					this.setPrimaryCooldown(InteractionHand.OFF_HAND,
							Math.max(this.getPrimaryCooldown(InteractionHand.OFF_HAND), fireRate / 2 - 50));
				else if (hand == InteractionHand.OFF_HAND)
					this.setPrimaryCooldown(InteractionHand.MAIN_HAND,
							Math.max(this.getPrimaryCooldown(InteractionHand.MAIN_HAND), fireRate / 2 - 50));
			}

			if (player instanceof EntityTF2Character)
				((EntityTF2Character) player).onShot();

			if (!(item instanceof ItemSapper))
				this.setDisguised(false);

			double oldX = player.getX(), oldY = player.getY(), oldZ = player.getZ();
			float oldPitch = player.getXRot(), oldYaw = player.yHeadRot;

			if (!player.level.isClientSide && player instanceof Player)
				this.preparePlayerPrediction(player, message);
			if (player.level.isClientSide && player == ClientProxy.getLocalPlayer())
				message = new TF2Message.PredictionMessage(player.getX(), player.getY(), player.getZ(), player.getXRot(),
						player.yHeadRot, 1, hand);

			item.use(stack, player, player.level, hand, message);

			player.removeEffect(TF2weapons.charging);

			if (player.level.isClientSide && player == ClientProxy.getLocalPlayer()) {
				TF2weapons.network.sendToServer(message);
			}

			player.setPos((oldX), player.getY(), player.getZ());
			player.setPos(player.getX(), (oldY), player.getZ());
			player.setPos(player.getX(), player.getY(), (oldZ));
			player.yHeadRot = oldYaw;
			player.setXRot((oldPitch));

			this.lastFire = 1250;
			this.fireCoolReduced = false;
			if (stack.getItem() instanceof ItemWeapon) {
				this.stopReload();
			}

			if (stackcap.fire1Cool > 200) {
				this.predictionList[hand.ordinal()].clear();
			}
		}
		if ((state & 2) != 0 && stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active == 2)
			((ItemUsable) stack.getItem()).altFireTick(stack, player, player.level);
		while (stackcap.fire2Cool <= 0 && shouldShoot(player, 2, hand, state)) {

			TF2Message.PredictionMessage message = null;
			if (!player.level.isClientSide && player instanceof Player) {
				message = this.predictionList[hand.ordinal() + 2].pollLast();
				// this.predictionList[hand.ordinal() + 2] = null;
			}

			if (item.getAltFiringSpeed(stack, player) == Short.MAX_VALUE
					|| !item.canAltFireInternal(player.level, player, stack, hand))
				break;
			stackcap.fire2Cool += item.getAltFiringSpeed(stack, player);

			this.setDisguised(false);

			double oldX = player.getX(), oldY = player.getY(), oldZ = player.getZ();
			float oldPitch = player.getXRot(), oldYaw = player.yHeadRot;

			if (!player.level.isClientSide && player instanceof Player)
				this.preparePlayerPrediction(player, message);

			((ItemUsable) stack.getItem()).altUse(stack, player, player.level);

			player.removeEffect(TF2weapons.charging);
			if (player.level.isClientSide && player == ClientProxy.getLocalPlayer())
				TF2weapons.network.sendToServer(new TF2Message.PredictionMessage(player.getX(), player.getY(), player.getZ(),
						player.getXRot(), player.yHeadRot, 2, InteractionHand.MAIN_HAND));

			player.setPos((oldX), player.getY(), player.getZ());
			player.setPos(player.getX(), (oldY), player.getZ());
			player.setPos(player.getX(), player.getY(), (oldZ));
			player.yHeadRot = oldYaw;
			player.setXRot((oldPitch));

			if (!player.level.isClientSide)
				ItemUsable.sps++;
			if (stack.getItem() instanceof ItemWeapon) {
				this.stopReload();
			}

			if (stackcap.fire2Cool > 200) {
				this.predictionList[hand.ordinal() + 2].clear();
			}
		}
	}

	public void updateExpJump() {

	}

	public void preparePlayerPrediction(LivingEntity player, TF2Message.PredictionMessage message) {
		player.setPos((message.x), player.getY(), player.getZ());
		player.setPos(player.getX(), (message.y), player.getZ());
		player.setPos(player.getX(), player.getY(), (message.z));
		player.yHeadRot = message.yaw;
		player.setXRot((message.pitch));
		this.mainHand = message.hand == InteractionHand.MAIN_HAND;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putByte("VisTicks", (byte) this.invisTicks);
		tag.putByte("DisguiseTicks", (byte) this.disguiseTicks);
		CompoundTag cld = new CompoundTag();
		for (Entry<String, Integer> entry : this.effectsCool.entrySet())
			cld.putInt(entry.getKey(), entry.getValue());
		tag.put("Cooldowns", cld);
		tag.putInt("HealTarget", this.getHealTarget());
		tag.putShort("Heads", this.entityData.get(HEADS).shortValue());
		tag.putShort("HeadsCool", (short) (this.collectedHeadsTime - this.owner.tickCount));
		tag.putBoolean("Cloaked", this.isInvisible());
		tag.putBoolean("Disguised", this.isDisguised());
		tag.putString("DisguiseType", this.getDisguiseType());
		tag.putInt("KillsSpinning", this.killsSpinning);
		tag.putInt("FocusedShot", this.focusShotTicks);
		tag.putInt("KnockbackFANCool", this.fanCool);
		tag.putInt("Metal", this.getMetal());
		tag.putFloat("MaxMetal", this.maxmetal);
		for (RageType rage : RageType.values()) {
			tag.putFloat(rage.toString(), this.getRage(rage));
			tag.putBoolean(rage.toString() + "active", this.isRageActive(rage));
			if (this.isRageActive(rage))
				tag.putFloat(rage.toString() + "drain", this.rageDrain[rage.ordinal()]);
		}
		tag.putLong("TicksTotal", this.ticksTotal);
		if (this.forcedClass)
			tag.putByte("Token", (byte) this.getUsedToken());
		// tag.setBoolean("Uber",
		// this.owner.getDataManager().get(TF2EventBusListener.ENTITY_UBER));
		// tag.setFloat("DodgedDmg", this.dodgedDmg);
		// tag.setInteger("KillsSpinning", this.killsSpinning);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.invisTicks = nbt.getByte("VisTicks");
		this.disguiseTicks = nbt.getByte("DisguiseTicks");
		// this.setHealTarget(nbt.getInteger("HealTarget"));
		CompoundTag cld = nbt.getCompound("Cooldowns");
		for (String key : cld.getAllKeys())
			this.effectsCool.put(key, cld.getInt(key));
		this.entityData.set(HEADS, (int) nbt.getShort("Heads"));
		this.collectedHeadsTime = nbt.getShort("HeadsCool");
		this.setInvisible(nbt.getBoolean("Cloaked"));
		this.setDisguised(nbt.getBoolean("Disguised"));
		// this.owner.getDataManager().set(TF2EventBusListener.ENTITY_UBER,
		// nbt.getBoolean("Uber"));
		this.setDisguiseType(nbt.getString("DisguiseType"));
		if (this.isDisguised())
			this.disguise();
		this.killsSpinning = nbt.getInt("KillsSpinning");
		this.focusShotTicks = nbt.getInt("FocusedShot");
		this.fanCool = nbt.getInt("KnockbackFANCool");
		// this.dodgedDmg=nbt.getFloat("DodgedDmg");
		this.maxmetal = nbt.getFloat("MaxMetal");
		this.setMetal(nbt.getInt("Metal"));
		this.ticksTotal = nbt.getLong("TicksTotal");
		for (RageType rage : RageType.values()) {
			this.setRage(rage, nbt.getFloat(rage.toString()));
			this.setRageActive(rage, nbt.getBoolean(rage.toString() + "active"),
					nbt.getFloat(rage.toString() + "drain"));
		}
		if (nbt.contains("Token")) {
			((ItemToken) TF2weapons.itemToken)
					.updateAttributes(TF2Util.metaStack(TF2weapons.itemToken, 1, nbt.getByte("Token")), this.owner);
			this.forcedClass = true;
		}
		// this.killsSpinning=nbt.getInteger("KillsSpinning");
	}

	public int getMaxAirJumps() {
		int amount = 0;
		if (this.owner instanceof EntityScout)
			amount += 1;
		else if (this.owner instanceof Player && (this.getUsedToken() == 0 || (ItemToken.allowUse(owner, TF2Class.SCOUT)
				&& owner.getItemBySlot(EquipmentSlot.FEET).getItem() == TF2weapons.itemScoutBoots))) {
			amount += 1;
		}
		amount = (int) TF2Attribute.getModifier("Triple Jump", this.owner.getMainHandItem(), amount, this.owner);
		return amount;
	}

	public static SynchedEntityData getDataManager(Entity ent) {
		return ent.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).entityData;
	}

	public static WeaponsCapability get(Entity ent) {
		return ent.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
	}

	public int getMinimalCooldown() {
		if (!this.owner.level.isClientSide && this.owner instanceof Player)
			return PLAYER_MINCOOL;
		else
			return 0;
	}

	public int getPrimaryCooldown() {
		return Math.max(this.getPrimaryCooldown(InteractionHand.MAIN_HAND), this.getPrimaryCooldown(InteractionHand.OFF_HAND));
	}

	public void setPrimaryCooldown(int fire1Cool) {
		this.setPrimaryCooldown(InteractionHand.MAIN_HAND, fire1Cool);
		this.setPrimaryCooldown(InteractionHand.OFF_HAND, fire1Cool);
	}

	public int getPrimaryCooldown(InteractionHand hand) {
		if (this.owner.getItemInHand(hand).getItem() instanceof ItemUsable)
			return this.owner.getItemInHand(hand).getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).fire1Cool
					- getMinimalCooldown();
		else
			return 0;
	}

	public void setPrimaryCooldown(InteractionHand hand, int fire1Cool) {
		if (this.owner.getItemInHand(hand).getCapability(TF2weapons.WEAPONS_DATA_CAP).isPresent())
			this.owner.getItemInHand(hand).getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).fire1Cool = fire1Cool
					+ getMinimalCooldown();
	}

	public int getSecondaryCooldown() {
		return Math.max(this.getSecondaryCooldown(InteractionHand.MAIN_HAND), this.getSecondaryCooldown(InteractionHand.OFF_HAND));
	}

	public void setSecondaryCooldown(int fire1Cool) {
		this.setSecondaryCooldown(InteractionHand.MAIN_HAND, fire1Cool);
		this.setSecondaryCooldown(InteractionHand.OFF_HAND, fire1Cool);
	}

	public int getSecondaryCooldown(InteractionHand hand) {
		if (this.owner.getItemInHand(hand).getItem() instanceof ItemUsable)
			return this.owner.getItemInHand(hand).getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).fire2Cool
					- getMinimalCooldown();
		else
			return 0;
	}

	public void setSecondaryCooldown(InteractionHand hand, int fire1Cool) {
		if (this.owner.getItemInHand(hand).getCapability(TF2weapons.WEAPONS_DATA_CAP).isPresent())
			this.owner.getItemInHand(hand).getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).fire2Cool = fire1Cool
					+ getMinimalCooldown();
	}

	public void stopReload() {
		this.reloadCool = 0;
		this.reloadingHand = null;
	}

	public boolean canExpJump() {
		return canExpJump;
	}

	public void setCanExpJump(boolean canExpJump) {
		this.canExpJump = canExpJump;
	}

	public void setActiveHand(InteractionHand hand, ItemStack stack) {
		this.stackActive.put(hand, stack);
		stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active = 2;
		((ItemUsable) stack.getItem()).draw(this, stack, owner, owner.level);

		if ((state & 3) > 0) {
			state = state & 7;
			if ((state & 3) > 0)
				((ItemUsable) stack.getItem()).startUse(stack, owner, owner.level, 0, state & 3);
		}
	}

	public void setInactiveHand(InteractionHand hand, ItemStack stack) {
		this.stackActive.remove(hand);
		if (stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active == 2 && (this.state & 3) > 0)
			((ItemUsable) stack.getItem()).endUse(stack, this.owner, this.owner.level, this.state, 0);

		stack.getCapability(TF2weapons.WEAPONS_DATA_CAP).orElse(null).active = 0;
		((ItemUsable) stack.getItem()).holster(this, stack, this.owner, this.owner.level);
	}

	public void disguise() {
		this.setDisguised(true);
		if (this.invisTicks < 20 && this.owner.level instanceof ServerLevel)
			((ServerLevel) this.owner.level).sendParticles(ParticleTypes.LARGE_SMOKE, this.owner.getX(),
					this.owner.getY(), this.owner.getZ(), 20, 0.2, 1, 0.2, 0.04f);
	}

	public boolean isGrappling() {
		return this.entityData.get(GRAPPLING);
	}

	public boolean isGrappled() {
		return this.entityData.get(GRAPPLED);
	}

	public void setGrappled(boolean grappled) {
		this.entityData.set(GRAPPLED, grappled);
	}

	public EntityGrapplingHook getGrapplingHook() {
		return grapplingHook;
	}

	public void setGrapplingHook(EntityGrapplingHook grapplingHook) {
		if (this.grapplingHook != null) {
			if (this.grapplingHook.isAlive())
				this.grapplingHook.discard();
		}
		this.setGrappled(false);
		this.entityData.set(GRAPPLING, grapplingHook != null);
		this.grapplingHook = grapplingHook;
	}

	public static enum RageType {
		PHLOG, MINICRIT, KNOCKBACK, BANNER
	}

	static {

		for (RageType type : RageType.values()) {
			RAGE.put(type, new EntityDataAccessor<>(127 - type.ordinal() * 2, EntityDataSerializers.FLOAT));
			RAGE_ACTIVE.put(type, new EntityDataAccessor<>(126 - type.ordinal() * 2, EntityDataSerializers.BOOLEAN));
		}
	}

}
