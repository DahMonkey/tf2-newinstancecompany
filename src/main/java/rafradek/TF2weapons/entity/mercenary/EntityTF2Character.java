package rafradek.TF2weapons.entity.mercenary;

import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.util.RandomSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.*;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.IEntityTF2;
import rafradek.TF2weapons.entity.ai.*;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.inventory.InventoryLoadout;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.util.*;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.ItemStackHandler;
import rafradek.TF2weapons.util.TF2Util;

public abstract class EntityTF2Character extends PathfinderMob implements Enemy, Merchant, IEntityTF2, OwnableEntity {

	public static final UUID SPEED_MULT_UUID = UUID.fromString("8ca1776e-72e8-4394-9d0f-0564fdec0b44");
	public static final UUID SPEED_GIANT_MULT_UUID = UUID.fromString("8ca1776e-72e8-4394-9d0f-0564fdec0b41");
	public static final UUID HEALTH_MULT_UUID = UUID.fromString("8ca1776e-72e8-4394-9d0f-0564fdec0b32");
	public static final UUID KNOCKBACK_MULT_UUID = UUID.fromString("8ca1776e-72e8-4394-9d0f-0564fdec0b35");
	public static final UUID FOLLOW_MULT_UUID = UUID.fromString("8ca1776e-72e8-4394-9d0f-0564fdec0b51");
	public static final UUID ARMOR_MULT_UUID = UUID.fromString("8ca1776e-72e8-4452-9d0f-0564fdec0b51");

	public float[] lastRotation;
	public boolean jump;
	public boolean friendly;
	public boolean ranged;
	public EntityAIUseRangedWeapon attack;
	public EntityAIMoveAttack moveAttack;
	public EntityAINearestChecked<LivingEntity> findplayer = new EntityAINearestChecked<>(this,
			LivingEntity.class, true, false, this::isValidTarget, true, false);
	protected MeleeAttackGoal attackMeele = new MeleeAttackGoal(this, 1.1F, false);
	public RandomStrollGoal wander;
	// public int ammoLeft;
	public boolean unlimitedAmmo;
	public boolean natural;
	private boolean noAmmo;
	public boolean alert;
	public static int nextEntTeam = -1;
	public Player trader;
	public Player lastTrader;
	public Map<Player, Integer> tradeCount;
	public InventoryLoadout loadout;
	public ItemStackHandler loadoutHeld;
	public ItemStackHandler refill;
	// public int heldWeaponSlot;

	public int followTicks;
	public MerchantOffers tradeOffers;
	public double[] targetPrevPos = new double[9];
	private static final EntityDataAccessor<Byte> VIS_TEAM = SynchedEntityData.defineId(EntityTF2Character.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> DIFFICULTY = SynchedEntityData.defineId(EntityTF2Character.class,
			EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(EntityTF2Character.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Byte> ORDER = SynchedEntityData.defineId(EntityTF2Character.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> MAIN_WEAPON = SynchedEntityData.defineId(EntityTF2Character.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> ACTIVE_SLOT = SynchedEntityData.defineId(EntityTF2Character.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> SHARE = SynchedEntityData.defineId(EntityTF2Character.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Byte> ROBOT = SynchedEntityData.defineId(EntityTF2Character.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> FRONT = SynchedEntityData.defineId(EntityTF2Character.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> SIDE = SynchedEntityData.defineId(EntityTF2Character.class,
			EntityDataSerializers.BYTE);
	public float rotation;
	public int traderFollowTicks;
	public int usedSlot = 0;
	public int bannerTeam = -1;
	private UUID followID;
	public int tradeLevel;
	public String ownerName;
	private int[] ammoCount;
	public int preferredSlot = 0;
	public float eating = 0;
	public AvoidEntityGoal<EntitySentry> avoidSentry;
	private int alertTime;
	private LivingEntity alertTarget;
	public int difficulty = 0;
	public ArrayList<AttributeModifier> playerAttributes = new ArrayList<>();
	public boolean[] isEmpty;
	protected boolean noEquipment;
	public boolean spawnMedic;
	private int robotSizeEnsured;
	public float robotStrength = 1f;
	public BlockPos spawnPos = BlockPos.ZERO;
	public InvasionEvent event;
	public boolean damagedByEnv;
	private WeaponsCapability weaponCap;
	private int moneyDrop;
	// private InventoryWearables wearablesCap;

	protected EntityTF2Character(EntityType<? extends EntityTF2Character> type, Level p_i1738_1_) {
		super(type, p_i1738_1_);
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new EntityAIMoveTowardsRestriction2(this, 1.25f));
		this.goalSelector.addGoal(1, avoidSentry = new AvoidEntityGoal<EntitySentry>(this, EntitySentry.class, 25, 1.0, 1.0, living -> {
			EntitySentry sentry = (EntitySentry) living;
			double range = sentry.getAttribute(Attributes.FOLLOW_RANGE).getValue() + 1;
			range *= range;
			return !TF2Util.isAlliedTo(this, sentry) && !sentry.isDisabled() && sentry.distanceToSqr(this) < range;
		}) {
			@Override
			public boolean canContinueToUse() {
				double range = toAvoid.getAttribute(Attributes.FOLLOW_RANGE)
						.getValue() + 1;
				range *= range;
				return super.canContinueToUse() && !(this.toAvoid == null
						|| this.toAvoid.distanceToSqr(EntityTF2Character.this) > range);
			}
		});
		this.goalSelector.addGoal(2, new EntityAIFollowTrader(this));
		this.goalSelector.addGoal(3, new EntityAIFindDispenser(this, 20f));
		this.goalSelector.addGoal(6, wander = new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, EntityTF2Character.class, 8.0F));
		this.goalSelector.addGoal(7, new EntityAISeek(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(5, findplayer);
		this.targetSelector.addGoal(3, new EntityAIOwnerHurt(this));
		this.targetSelector.addGoal(4,
				new EntityAINearestChecked<Mob>(this, Mob.class, true, false,
						target -> ((target instanceof Enemy && target.distanceToSqr(this.getOwner()) < 400)
								|| target.getTarget() == this.getOwner()),
						true, false) {
					@Override
					public boolean canUse() {
						return ((EntityTF2Character) this.mob).getOwner() != null && super.canUse();
					}
				});
		// this.lookHelper=new
		// this.motionSensitivity=4;
		this.rotation = 17;
		this.lastRotation = new float[20];
		this.loadout = new InventoryLoadout(5, this);// NonNullList.withSize(5,ItemStack.EMPTY);
		this.loadoutHeld = new ItemStackHandler(8);
		this.refill = new ItemStackHandler(1);
		this.ammoCount = new int[4];
		this.isEmpty = new boolean[4];
		this.handDropChances[0] = 0;
		this.armorDropChances[0] = 0.25f;
		// this.jumpMovementFactor = 0.1f;
		if (p_i1738_1_ != null) {
			// this.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().setTag("Attributes",
			// (NBTTagCompound)
			// ((ItemUsable)this.getHeldItem(EnumHand.MAIN_HAND).getItem()).buildInAttributes.copy());

			this.goalSelector.addGoal(4, this.attack = new EntityAIUseRangedWeapon(this, 1.0F, 19.9F));
			this.goalSelector.addGoal(4, this.moveAttack = new EntityAIMoveAttack(this, 1.0F, 20.0F));
			// this.setCombatTask(true);
		}
		this.tradeCount = new HashMap<>();
		/*
		 * for (int i = 0; i < this.e.length; ++i) { this.equipmentDropChances[i] =
		 * 0.0F; }
		 */
	}

	/*
	 * public EntityLookHelper getLookHelper() { return this.lookHelper; }
	 */
	@Override
	public ItemStack getPickedResult(HitResult target) {
		ItemStack stack = TF2Util.metaStack(TF2weapons.itemPlacer, 1, getTF2Class().getIndex() + 9 * (this.getEntTeam()));

		// stack.setTagCompound(new NBTTagCompound());
		// stack.getTagCompound().setTag("SavedEntity", new NBTTagCompound());
		// this.writeEntityToNBT(stack.getTagCompound().getCompoundTag("SavedEntity"));
		return stack;
	}

	public float getWidth() {
		return 0.6f;
	}

	public float getHeight() {
		return 1.8f;
	}

	protected void addWeapons() {
		TF2Class clazz = getTF2Class();
		// System.out.println("Class name: "+className);
		this.loadout.setStackInSlot(0, ItemFromData.getRandomWeaponOfSlotMob(clazz, 0, this.random, false,
				this.getStockWeight(0), this.noEquipment));
		this.loadout.setStackInSlot(1, ItemFromData.getRandomWeaponOfSlotMob(clazz, 1, this.random, false,
				this.getStockWeight(1), this.noEquipment));
		this.loadout.setStackInSlot(2, ItemFromData.getRandomWeaponOfSlotMob(clazz, 2, this.random, false,
				this.getStockWeight(2), this.noEquipment));
		this.loadout.setStackInSlot(3, ItemFromData.getRandomWeaponOfSlotMob(clazz, 3, this.random, false,
				this.getStockWeight(3), this.noEquipment));
		if (!this.noEquipment && !this.isRobot()) {
			if (this.random.nextInt(Math.max(1, (int) ((14 - this.level.getDifficulty().getId() * 3)
					/ TF2ConfigVars.hatMercenaryMult))) == 0) {
				this.tradeLevel = 1;
				this.difficulty = 1;
				this.xpReward *= 2;
				ItemStack hat = ItemFromData.getRandomWeaponOfSlotMob(clazz, 9, this.random, false,
						this.getStockWeight(9), false);

				if (!hat.isEmpty() && this.random.nextInt(9) == 0) {
					hat.getTag().putByte("UEffect", (byte) this.random.nextInt(10));
					this.armorDropChances[0] = 0.35f;
					this.tradeLevel = 2;
					this.difficulty = 2;
					this.xpReward *= 1.5;
					for (int i = 0; i < this.loadout.getSlots(); i++) {
						TF2Attribute.upgradeItemStack(this.loadout.getStackInSlot(i),
								Math.min(1600, 640 + (int) (this.level.getDayTime() / 2000)), random);
					}

				}

				this.setItemSlot(EquipmentSlot.HEAD, hat);
				if (this.level.getDayTime() > 48000)
					for (int i = 0; i < this.loadout.getSlots(); i++) {
						TF2Attribute.upgradeItemStack(this.loadout.getStackInSlot(i),
								Math.min(800, 232 + (int) (this.level.getDayTime() / 4000)), random);
					}
			} else if (this.random.nextInt(2 + Math.min(5, (int) (this.level.getDayTime() / 160000))) == 0) {
				ItemStack hat = ItemFromData.getRandomWeapon(random, data -> data.getBoolean(PropertyType.F2P));
				this.setItemSlot(EquipmentSlot.HEAD, hat);
				this.armorDropChances[0] = 0.35f;
				this.difficulty = -1;
				this.tradeLevel = -1;
			}
		}
	}

	public float getStockWeight(int slot) {
		return this.isRobot() ? 4.5f - Math.min(2.5f, this.robotStrength / 2f) : 2f;
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficulty) {
		if (this.noEquipment || this.isRobot())
			return;

		float chance = 1f + 3f * this.tradeLevel + this.tradeLevel * this.tradeLevel * 2f;
		if (this.random.nextFloat() < TF2ConfigVars.armorMult * chance * difficulty.getEffectiveDifficulty() * 0.3f) {
			int i = this.random.nextInt(2);
			float f = (this.level.getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F) / chance;

			if (this.random.nextFloat() < 0.095F * chance) {
				++i;
			}

			if (this.random.nextFloat() < 0.095F * chance) {
				++i;
				this.getAttribute(Attributes.ARMOR)
						.addTransientModifier(new AttributeModifier("hatarmor", 1, AttributeModifier.Operation.fromValue(0)));
			}

			if (this.random.nextFloat() < 0.095F * chance) {
				++i;
				this.getAttribute(Attributes.ARMOR_TOUGHNESS)
						.addTransientModifier(new AttributeModifier("hatarmort", 2, AttributeModifier.Operation.fromValue(0)));
			}

			boolean flag = true;

			for (EquipmentSlot entityequipmentslot : EquipmentSlot.values()) {
				if (entityequipmentslot.getType() == EquipmentSlot.Type.ARMOR) {
					ItemStack itemstack = this.getItemBySlot(entityequipmentslot);

					if (!flag && this.random.nextFloat() < f) {
						break;
					}

					flag = false;

					if (itemstack.isEmpty() && entityequipmentslot != EquipmentSlot.HEAD) {
						Item item = Mob.getEquipmentForSlot(entityequipmentslot, i);

						if (item != null) {
							this.setItemSlot(entityequipmentslot, new ItemStack(item));
						}
					}
				}
			}
		}
	}

	/*
	 * public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn){
	 * if(slotIn==EntityEquipmentSlot.MAINHAND){
	 * //System.out.println("Held item: "+this.loadout[this.heldWeaponSlot]); return
	 * this.loadout[this.heldWeaponSlot]; } else{ return
	 * super.getItemStackFromSlot(slotIn); } }
	 */
	/*
	 * public void setItemStackToSlot(EntityEquipmentSlot slotIn, @Nullable
	 * ItemStack stack) { if(slotIn==EntityEquipmentSlot.MAINHAND){
	 * this.loadout[this.heldWeaponSlot]=stack; } else{
	 * super.setItemStackToSlot(slotIn, stack); } }
	 */
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(VIS_TEAM, (byte) this.random.nextInt(2));
		this.entityData.define(DIFFICULTY, (byte) 0);
		this.entityData.define(OWNER_UUID, Optional.empty());
		this.entityData.define(ORDER, (byte) 0);
		this.entityData.define(SHARE, false);
		this.entityData.define(ROBOT, (byte) 0);
		this.entityData.define(FRONT, (byte) 0);
		this.entityData.define(SIDE, (byte) 0);
		this.entityData.define(MAIN_WEAPON, (byte) -1);
		this.weaponCap = new WeaponsCapability(this);
		// this.wearablesCap = new InventoryWearables(this);
	}

	public int getEntTeam() {
		return this.isRobot() ? 2 : this.entityData.get(VIS_TEAM);
	}

	public int getDiff() {
		return Math.max(1, this.entityData.get(DIFFICULTY) + this.difficulty + (this.isGiant() ? 2 : 0));
	}

	public void setEntTeam(int team) {
		this.entityData.set(VIS_TEAM, (byte) team);
	}

	public void setDiff(int diff) {
		this.entityData.set(DIFFICULTY, (byte) diff);
	}

	public Order getOrder() {
		return Order.values()[this.entityData.get(ORDER)];
	}

	public void setOrder(Order order) {
		this.entityData.set(ORDER, (byte) order.ordinal());
	}

	public int getMainWeapon() {
		return this.entityData.get(MAIN_WEAPON);
	}

	public void setMainWeapon(int slot) {
		this.entityData.set(MAIN_WEAPON, (byte) slot);
	}

	public boolean isSharing() {
		return this.entityData.get(SHARE);
	}

	public boolean isRobot() {
		return this.entityData.get(ROBOT) != 0;
	}

	public int getRobotSize() {
		return this.entityData.get(ROBOT);
	}

	public boolean isGiant() {
		return this.entityData.get(ROBOT) > 1;
	}

	public void setRobot(int robot) {
		this.entityData.set(ROBOT, (byte) robot);
		if (robot > 0) {
			// this.unlimitedAmmo = true;
			if (this.getOwnerUUID() != null)
				TF2Util.addModifierSafe(this, Attributes.FOLLOW_RANGE,
						new AttributeModifier(FOLLOW_MULT_UUID, "GiantRange", 0.4, AttributeModifier.Operation.fromValue(2)), true);
			if (robot > 1) {
				this.maxUpStep = 1f;
				this.goalSelector.removeGoal(avoidSentry);
				TF2Util.addModifierSafe(this, Attributes.ARMOR,
						new AttributeModifier(ARMOR_MULT_UUID, "GiantArmor", -0.5, AttributeModifier.Operation.fromValue(2)), true);
				TF2Util.addModifierSafe(this, Attributes.MAX_HEALTH,
						new AttributeModifier(HEALTH_MULT_UUID, "GiantHealth", (0.25 + this.robotStrength) * 2.5, AttributeModifier.Operation.fromValue(1)),
						true);
				TF2Util.addModifierSafe(this, Attributes.KNOCKBACK_RESISTANCE,
						new AttributeModifier(KNOCKBACK_MULT_UUID, "GiantKnockback", 0.6, AttributeModifier.Operation.fromValue(0)), true);
				TF2Util.addModifierSafe(this, Attributes.FOLLOW_RANGE,
						new AttributeModifier(FOLLOW_MULT_UUID, "GiantRange", 0.8, AttributeModifier.Operation.fromValue(2)), true);
				this.setHealth(this.getMaxHealth());
				if (!(this instanceof EntityScout || this instanceof EntityMedic))
					TF2Util.addModifierSafe(this, Attributes.MOVEMENT_SPEED,
							new AttributeModifier(SPEED_GIANT_MULT_UUID, "GiantSpeed", -0.43, AttributeModifier.Operation.fromValue(2)), true);
			}
		}
	}

	public void setSharing(boolean share) {
		this.entityData.set(SHARE, share);
	}

	public int getAmmo() {
		return this.getAmmo(this.usedSlot);
	}

	@Override
	public void setSpeed(float speedIn) {
		if (this.isGiant()) {
			speedIn = Math.max(speedIn,
					(float) this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue()
							* (this.getRobotSize() == 2 ? 0.4f : 0.5f));
		}
		super.setSpeed(speedIn);
	}

	@Override
	public boolean canBeAffected(MobEffectInstance potioneffectIn) {
		return !(this.isGiant() && potioneffectIn.getEffect() == TF2weapons.stun);
	}

	@Override
	public void setTarget(LivingEntity target) {

		super.setTarget(target);
		if (this.isTrading() && target != null && this.getTradingPlayer() != this.getOwner()) {
			this.setTradingPlayer(null);
			// System.out.println(target);
		}
		if (!this.alert)
			for (EntityTF2Character ent : this.level.getEntitiesOfClass(EntityTF2Character.class, new AABB(
					this.getX() - 15, this.getY() - 6, this.getZ() - 15, this.getX() + 15, this.getY() + 6, this.getZ() + 15)))
				if (TF2Util.isAlliedTo(this, ent) && !TF2Util.isAlliedTo(this, target)
						&& (ent.getTarget() == null || ent.getTarget().isRemoved())) {
					ent.alert = true;
					ent.alertTarget = target;
					ent.alertTime = ent.tickCount + 8 + this.getRandom().nextInt(14);

				}
	}

	public void useAmmo(int amount) {
		if (!this.unlimitedAmmo)
			this.ammoCount[this.usedSlot] -= amount;

	}

	public boolean shouldScaleAttributes() {
		return this.getTarget() != null && TF2ConfigVars.scaleAttributes
				&& (this.getTarget() instanceof Player || this.getTarget() instanceof OwnableEntity
						&& ((OwnableEntity) this.getTarget()).getOwnerUUID() != null)
				&& this.getOwnerUUID() == null;
	}

	public float getAttributeModifier(String attribute) {
		float base = 1f;
		if (this.getEffect(TF2weapons.sapped) != null)
			base *= 2f;
		if (shouldScaleAttributes())
			if (attribute.equals("Knockback"))
				base *= this.scaleWithDifficulty(0.4f, 1f);
			else if (attribute.equals("Fire Rate"))
				base *= this.scaleWithDifficulty(1.9f, 1f);
			else if (attribute.equals("Spread")) {
				base *= this.scaleWithDifficulty(1.9f, 1f);
			} else if (attribute.equals("Damage") && this.getMainHandItem().getItem() instanceof ItemMeleeWeapon)
				base *= 1.25f;
		if (attribute.equals("Auto Fire"))
			return 0f;
		if (attribute.equals("Overload"))
			return 0f;
		return base;
	}

	public boolean hasHeldInventory() {
		return !(this.isRobot() && this.getOwnerUUID() != null);
	}

	public boolean isReloadPressed() {
		return true;
	}

	@Override
	public void aiStep() {

		if (this.level.isClientSide) {
			this.zza = this.entityData.get(FRONT) / 128f;
			this.xxa = this.entityData.get(SIDE) / 128f;
		}

		super.aiStep();

		if (!this.level.isClientSide) {
			this.entityData.set(FRONT, (byte) Mth.clamp(Math.round(this.zza * 128f), -128, 127));
			this.entityData.set(SIDE, (byte) Mth.clamp(Math.round(this.xxa * 128f), -128, 127));
		}

		this.updateSwingTime();
		ItemStack hat = this.getItemBySlot(EquipmentSlot.HEAD);
		if (!hat.isEmpty() && hat.getItem() instanceof ItemWearable) {
			((ItemWearable) hat.getItem()).onUpdateWearing(hat, this.level, this);
		}
		ItemStack backpack = ItemBackpack.getBackpack(this);
		if (!backpack.isEmpty() && backpack.getItem() instanceof ItemBackpack) {
			((ItemBackpack) backpack.getItem()).onArmorTickAny(this.level, this, backpack);
		}
		if (this.getTarget() != null && !this.getTarget().isAlive())
			this.setTarget(null);
		if (!this.friendly && this.getTarget() instanceof EntityTF2Character
				&& TF2Util.isAlliedTo(this, this.getTarget()))
			this.setTarget(null);
		if (this.jump && this.onGround)
			this.jumpFromGround();
		if (this.spawnMedic) {

			// medic.setEntTeam(event.getWorld().rand.nextInt(2));

			TF2CharacterAdditionalData data = new TF2CharacterAdditionalData();
			data.team = this.getEntTeam();
			data.natural = true;
			data.allowGiant = this.isGiant();

			EntityMedic medic = new EntityMedic(this.level);
			medic.moveTo(this.getX() + this.random.nextDouble() * 0.5 - 0.25, this.getY(),
					this.getZ() + this.random.nextDouble() * 0.5 - 0.25, this.random.nextFloat() * 360.0F, 0.0F);
			TF2Util.initialSpawn(medic, this.level.getCurrentDifficultyAt(this.blockPosition()), data);

			this.level.addFreshEntity(medic);
			this.spawnMedic = false;
		}

		if (!this.noAmmo && this.getTarget() != null && Math.abs(this.getYRot()
				- this.yHeadRot) > 60/*
											 * TF2ActionHandler.playerAction.get ().get(this)!=null&&(
											 * TF2ActionHandler.playerAction.get ().get(this)&3)>0
											 */)
			if (this.yHeadRot - this.getYRot() > 60)
				this.setYRot((this.yHeadRot + 60));
			else
				this.setYRot((this.yHeadRot - 60));
		// if (this.hasRestriction()) {

		if (this.firstTick) {
			this.spawnPos = this.blockPosition();
		}

		if (!this.level.isClientSide) {
			if (this.isReloadPressed())
				this.getWepCapability().state |= 4;
			else if (!this.isReloadPressed()) {
				this.getWepCapability().state &= ~4;
			}
			if (this.tickCount % 2 == 0) {
				this.loadout.updateSlots();
			}
			if (this.isRobot()) {
				if (this.getTarget() == null
						|| (this.friendly && this.getTarget().getKillCredit() == null)) {
					this.noActionTime += 1;
					if (this.noActionTime > 800 + this.getRobotSize() * 500)
						this.discard();
				}
				if (this.getEffect(TF2weapons.sapped) != null) {
					this.setXRot((40));
				}
			}
			if (this.getOrder() == Order.HOLD && !this.hasRestriction())
				this.restrictTo(this.blockPosition(), 12);
			else if (this.getOrder() == Order.FOLLOW && this.hasRestriction())
				this.clearRestriction();

			this.setDiff(this.level.getDifficulty().getId());
			if (this.isTrading() && (this.trader.distanceToSqr(trader) > 100 || !this.isAlive()))
				this.setTradingPlayer(null);

			if (this.getMainHandItem().getItem() instanceof ItemUsable) {

				if (!((ItemUsable) this.getMainHandItem().getItem()).isAmmoSufficient(this.getMainHandItem(),
						this, true)) {
					if (!this.refill(this.usedSlot))
						this.switchSlot(this.getFirstSlotWithAmmo(), true, false);
				}

				// if(this.getAmmo() == 0)
				// System.out.println("IsSufficient "+
				// ((ItemUsable)this.getHeldItemMainhand().getItem()).isAmmoSufficient(this.getHeldItemMainhand(),
				// this, true));
				// this.noAmmo = true;
			}
			if (this.preferredSlot != this.usedSlot) {
				this.switchSlot(this.preferredSlot);
			}
			if (this.traderFollowTicks > 0) {
				this.traderFollowTicks--;
				if (this.followID != null && this.lastTrader == null) {
					this.lastTrader = this.level.getPlayerByUUID(this.followID);
				}
			}
			if (this.refill.getStackInSlot(0).getItem() instanceof ItemAmmo
					|| this.refill.getStackInSlot(0).getItem() instanceof ArrowItem) {
				ItemStack ammoStack = this.refill.getStackInSlot(0);
				int ammoType = ammoStack.getItem() instanceof ItemAmmo
						? ((ItemAmmo) ammoStack.getItem()).getTypeInt(ammoStack)
						: 1000;
				for (int i = 0; i < this.ammoCount.length; i++) {
					if (this.getAmmo(i) < this.getMaxAmmo(i) && ammoType == ItemFromData
							.getData(this.loadout.getStackInSlot(i)).getInt(PropertyType.AMMO_TYPE)) {
						if (ammoStack.isDamageableItem()) {
							int oldAmmo = this.getMaxAmmo(i) - this.ammoCount[i];
							this.ammoCount[i] += Math.min((ammoStack.getMaxDamage() - ammoStack.getDamageValue()) * 2,
									this.getMaxAmmo(i) - this.ammoCount[i]);
							ammoStack.setDamageValue(ammoStack.getDamageValue() + oldAmmo);
							if (ammoStack.getDamageValue() > ammoStack.getMaxDamage())
								this.refill.setStackInSlot(0, ItemStack.EMPTY);

						} else
							this.ammoCount[i] += this.refill
									.extractItem(0, (this.getMaxAmmo(i) - this.ammoCount[i]) / 2, false).getCount() * 2;
					}

				}
			} else if (this.getUseItem().isEmpty() && this.getTarget() == null
					&& this.getHealth() / this.getMaxHealth() < 0.7f
					&& this.refill.getStackInSlot(0).isEdible()) {
				this.setItemInHand(InteractionHand.OFF_HAND, this.refill.getStackInSlot(0));
				this.startUsingItem(InteractionHand.OFF_HAND);
				// this.eating =
				// ((ItemFood)this.refill.getStackInSlot(0).getItem()).getHealAmount(this.refill.getStackInSlot(0));
			}

			/*
			 * if(this.eating > 0 && this.ticksExisted % 20 == 0) { this.heal(Math.min(3,
			 * this.eating)); this.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,
			 * 20)); this.playSound(SoundEvents.PLAYER, volume, pitch); this.eating
			 * -=Math.min(3, this.eating); }
			 */
			if (this.alert && this.alertTarget != null && this.alertTime == this.tickCount) {
				this.setTarget(this.alertTarget);
				this.alert = false;
			}
		}

		for (int i = 19; i > 0; i--)
			this.lastRotation[i] = this.lastRotation[i - 1];
		this.lastRotation[0] = (float) Math.sqrt((this.yHeadRot - this.yHeadRotO)
				* (this.yHeadRot - this.yHeadRotO)
				+ (this.getXRot() - this.xRotO) * (this.getXRot() - this.xRotO));

	}

	@Override
	protected float getWaterSlowDown() {
		return this.getOwnerUUID() != null ? 1.0F : 0.9F;
	}

	@Override
	public void travel(Vec3 travelVector) {
		float move = this.getSpeed();
		if (move > 0 && this.isControlledByLocalInstance())
			travelVector = new Vec3(travelVector.x / move, travelVector.y, travelVector.z / move);
		super.travel(travelVector);
	}

	@Override
	protected void completeUsingItem() {
		if (!this.useItem.isEmpty() && this.isUsingItem() && this.useItem.getUseAnimation() == UseAnim.EAT
				&& this.useItem.isEdible()) {
			this.heal(this.useItem.getItem().getFoodProperties(this.useItem, this).getNutrition());
		}
		super.completeUsingItem();
		this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
	}

	public boolean canBecomeGiant() {
		return true;
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance p_180482_1_, MobSpawnType spawnReason, @Nullable SpawnGroupData entityLivingData, @Nullable CompoundTag spawnTag) {
		super.finalizeSpawn(levelAccessor, p_180482_1_, spawnReason, entityLivingData, spawnTag);
		TF2CharacterAdditionalData data;
		if (!(entityLivingData instanceof TF2CharacterAdditionalData)) {
			data = new TF2CharacterAdditionalData();
			data.natural = true;
			if (nextEntTeam >= 0) {
				data.team = nextEntTeam;
				nextEntTeam = -1;
			}

			if (this.bannerTeam != -1) {
				data.team = this.bannerTeam;
			} else {
				List<EntityTF2Character> list = this.level.getEntitiesOfClass(EntityTF2Character.class,
						this.getBoundingBox().inflate(40, 4.0D, 40), null);

				boolean found = false;
				for (EntityTF2Character ent : list) {
					if (ent.getOwnerUUID() == null) {
						found = true;
						data.team = list.get(0).getEntTeam();
						break;
					}
				}
				if (!found)
					data.team = this.random.nextInt(2);
			}
		} else
			data = (TF2CharacterAdditionalData) entityLivingData;
		if (entityLivingData instanceof TF2CharacterAdditionalData) {
			this.natural = (data.natural);
			this.setEntTeam(data.team);
			this.noEquipment = (data.noEquipment);
		}
		if (this.natural) {

		}
		if (data.team == 2) {
			this.setRobot(1);
			this.setMoneyDrop(7);
			if (data.isGiant /* || (data.natural && this.canBecomeGiant() && data.allowGiant) */) {
				// this.spawnMedic = data.natural && this.rand.nextFloat() < 0.15 *
				// this.robotStrength + 0.15;
				this.setRobot(2);
				this.setMoneyDrop(14 + (this.difficulty * 10));
			}
		}
		this.addWeapons();
		this.populateDefaultEquipmentSlots(this.random, p_180482_1_);
		this.switchSlot(this.getDefaultSlot(), false, true);

		int i = 3;
		for (ItemStack stack : this.getArmorSlots()) {
			this.isEmpty[i] = stack.isEmpty();
			i--;
		}
		if (!this.level.getGameRules().getBoolean(TF2weapons.DO_TF2_AI)) {
			this.goalSelector.getAvailableGoals().clear();
			this.targetSelector.getAvailableGoals().clear();
		}
		this.getAttribute(Attributes.MAX_HEALTH)
				.addTransientModifier(new AttributeModifier("damageModifier", TF2ConfigVars.damageMultiplier - 1, AttributeModifier.Operation.fromValue(2)));
		this.applySpeed();
		this.setHealth(this.getMaxHealth());
		for (int j = 0; j < this.ammoCount.length; j++) {
			this.ammoCount[j] = this.getMaxAmmo(j);
			if (this.isRobot())
				this.ammoCount[j] *= 20;
		}

		return data;
	}

	public void applySpeed() {
		this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MULT_UUID);
		if (this.getOwnerUUID() == null && this.getRobotSize() < 2)
			this.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
					new AttributeModifier(SPEED_MULT_UUID, "speedModifier", TF2ConfigVars.speedMult - 1, AttributeModifier.Operation.fromValue(2)));
	}

	public int getDefaultSlot() {
		return 0;
	}

	@Override
	protected float getJumpPower() {
		return 0.5f;
	}

	@Override
	public void die(DamageSource s) {
		if (s.getEntity() != null && s.getEntity() instanceof ServerPlayer
				&& !TF2Util.isAlliedTo(this, s.getEntity())) {
			ServerPlayer player = (ServerPlayer) s.getEntity();
			if (s.getEntity().getTeam() != null || TF2ConfigVars.neutralAttack) {
				if (!this.isRobot()) {
					player.awardStat(TF2Achievements.KILLED_MERC);
					if (TF2Achievements.read(player, TF2Achievements.KILLED_MERC) >= 5
							&& TF2Achievements.read(player, TF2Achievements.CONTRACT_DAY) == 0/*
																		 * player.getCapability(TF2weapons.PLAYER_CAP,
																		 * null).nextContractDay == -1
																		 */)
						player.awardStat(TF2Achievements.CONTRACT_DAY, (int) (this.level.getDayTime() / 24000 + 1));
				} else {
					player.awardStat(TF2weapons.robotsKilled);
					TF2PlayerCapability.get(player).robotsKilledInvasion += 1;
				}
			}
			// player.awardStat(TF2Achievements.FIRST_ENCOUNTER);

		}
		if (this.lastHurtByPlayer != null && TF2Util.isAlliedTo(this, this.lastHurtByPlayer))
			this.xpReward = 0;
		super.die(s);
	}

	public boolean isValidTarget(LivingEntity living) {
		boolean hostilemob = false;
		if (TF2ConfigVars.attackMobs && !this.isRobot() && living instanceof Mob
				&& TF2Util.isHostile(living)) {
			double rangesq = living.getAttribute(Attributes.FOLLOW_RANGE).getValue() + 1;
			rangesq *= rangesq;
			hostilemob = living.distanceToSqr(EntityTF2Character.this) < rangesq;
		}
		if (living.getTeam() != null || (TF2ConfigVars.neutralAttack && living instanceof Player) || hostilemob) {
			return !TF2Util.isAlliedTo(EntityTF2Character.this, living)
					&& (!(living instanceof EntityTF2Character && TF2ConfigVars.naturalCheck.equals("Never"))
							|| (!((EntityTF2Character) living).natural || !natural));
		}
		return false;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!(player.getMainHandItem() != null
				&& player.getMainHandItem().getItem() instanceof ItemMonsterPlacerPlus)
				&& (this.getOwner() == player || this.getTarget() == null || this.friendly)
				&& this.isAlive() && !this.isTrading() && !this.isBaby() && !player.isShiftKeyDown()) {
			if (this.level.isClientSide && !this.isRobot() && player.getTeam() == null
					&& ((this.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state & 1) == 0 || this.friendly)
					&& !player.isCreative())
				ClientProxy.displayScreenConfirm("Choose a team to interact",
						"Visit the Mann Co. Store located in a village");
			if (!this.level.isClientSide && (TF2Util.isAlliedTo(this, player) || player.isCreative())) {
				this.setTradingPlayer(player);
				TF2weapons.openGui(player, 4, this.level, this.getId(), 0, 0);
			}

			player.awardStat(Stats.TALKED_TO_VILLAGER);
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else
			return super.mobInteract(player, hand);
	}

	public boolean isTrading() {
		return this.trader != null;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);

		if (tag.getCompound("ForgeCaps").contains(TF2weapons.MOD_ID + ":weaponscap")) {
			tag.put("WeaponsCap", tag.getCompound("ForgeCaps").getCompound(TF2weapons.MOD_ID + ":weaponscap"));
			tag.getCompound("ForgeCaps").remove(TF2weapons.MOD_ID + ":weaponscap");
		}
		this.weaponCap.deserializeNBT(tag.getCompound("WeaponsCap"));
		// this.wearablesCap.deserializeNBT(tag.getTagList("WearablesCap", 10));

		if (tag.contains("AmmoC"))
			this.ammoCount = tag.getIntArray("AmmoC");
		if (this.ammoCount.length == 0)
			this.ammoCount = new int[3];
		this.unlimitedAmmo = tag.getBoolean("UnlimitedAmmo");
		this.setEntTeam(tag.getByte("Team"));
		this.natural = tag.getBoolean("Natural");
		this.robotStrength = tag.getFloat("RobotStrength");
		this.setRobot(tag.getByte("Robot"));
		this.setMoneyDrop(tag.getInt("MoneyDrop"));

		if (tag.get("Loadout") instanceof ListTag) {
			ListTag list = (ListTag) tag.get("Loadout");

			if (list != null) {
				for (int i = 0; i < list.size(); ++i) {
					CompoundTag nbttagcompound = list.getCompound(i);
					int j = nbttagcompound.getByte("Slot");
					this.loadout.setStackInSlot(j, ItemStack.of(nbttagcompound));
				}
			}
		} else
			this.loadout.deserializeNBT(tag.getCompound("Loadout"));
		this.loadoutHeld.deserializeNBT(tag.getCompound("LoadoutHeld"));

		if (this.loadoutHeld.getSlots() < 8) {
			this.loadoutHeld.setSize(8);
			this.loadoutHeld.setStackInSlot(7, this.loadoutHeld.getStackInSlot(6));
			this.loadoutHeld.setStackInSlot(6, this.loadoutHeld.getStackInSlot(5));
			this.loadoutHeld.setStackInSlot(5, this.loadoutHeld.getStackInSlot(4));
			this.loadoutHeld.setStackInSlot(4, this.loadoutHeld.getStackInSlot(3));
		}

		this.refill.setStackInSlot(0, ItemStack.of(tag.getCompound("Refill")));

		if (tag.contains("Offers")) {
			this.tradeOffers = new MerchantOffers(tag.getCompound("Offers"));
		}
		this.preferredSlot = tag.getByte("PSlot");
		this.switchSlot(tag.getByte("Slot"), true, true);
		this.setMainWeapon(tag.getByte("MainWeapon"));
		if (tag.contains("FollowTrader")) {
			this.followID = tag.getUUID("FollowTrader");
			this.traderFollowTicks = tag.getInt("FollowTraderTicks");
		}

		if (!this.level.getGameRules().getBoolean(TF2weapons.DO_TF2_AI)) {
			this.goalSelector.getAvailableGoals().clear();
			this.targetSelector.getAvailableGoals().clear();
		}

		if (tag.hasUUID("Owner")) {
			UUID ownerID = tag.getUUID("Owner");
			this.entityData.set(OWNER_UUID, Optional.of(ownerID));
			this.ownerName = tag.getString("OwnerName");
			this.getOwner();
			this.setPersistenceRequired();
			this.setOrder(Order.values()[tag.getByte("Order")]);

			// this.world.getCapability(TF2weapons.WORLD_CAP,
			// null).lostMercPos.remove(this.getOwnerUUID(), this.getPos());
			PlayerPersistStorage.get(this.level, this.getOwnerUUID()).medicMercPos.remove(this.blockPosition());
			PlayerPersistStorage.get(this.level, this.getOwnerUUID()).restMercPos.remove(this.blockPosition());
		}

		this.tradeLevel = tag.getByte("TradeLevel");
		this.difficulty = tag.getByte("Difficulty");
		byte[] empty = tag.getByteArray("Empty");
		for (int i = 0; i < empty.length; i++) {
			this.isEmpty[i] = empty[i] != 0;
		}

	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
		for (AttributeModifier modifier : playerAttributes) {
			speed.removeModifier(modifier);
		}

		super.addAdditionalSaveData(tag);
		tag.put("WeaponsCap", this.weaponCap.serializeNBT());
		// tag.setTag("WearablesTag", this.wearablesCap.serializeNBT());

		tag.putIntArray("AmmoC", ammoCount);
		tag.putBoolean("UnlimitedAmmo", this.unlimitedAmmo);
		tag.putByte("Team", (byte) this.getEntTeam());
		tag.putBoolean("Natural", this.natural);
		/*
		 * NBTTagList list = new NBTTagList();
		 *
		 * for (int i = 0; i < this.loadout.getSlots(); i++) { ItemStack itemstack =
		 * this.loadout.getStackInSlot(i);
		 *
		 * if (!itemstack.isEmpty()) { NBTTagCompound nbttagcompound = new
		 * NBTTagCompound(); nbttagcompound.setByte("Slot", (byte) i);
		 * itemstack.writeToNBT(nbttagcompound); list.appendTag(nbttagcompound); } }
		 * tag.setTag("Loadout", list);
		 */
		tag.putByte("Robot", (byte) this.getRobotSize());
		tag.put("Loadout", this.loadout.serializeNBT());
		tag.put("LoadoutHeld", loadoutHeld.serializeNBT());
		tag.put("Refill", this.refill.getStackInSlot(0).serializeNBT());
		if (this.tradeOffers != null)
			tag.put("Offers", this.tradeOffers.createTag());
		if (this.usedSlot == -1)
			this.usedSlot = this.getDefaultSlot();
		tag.putByte("Slot", (byte) this.usedSlot);
		tag.putByte("PSlot", (byte) this.preferredSlot);
		if (this.lastTrader != null) {
			tag.putUUID("TraderFollow", this.lastTrader.getUUID());
			tag.putInt("TraderFollowTicks", this.traderFollowTicks);
		}
		if (this.getOwnerUUID() != null) {
			tag.putUUID("Owner", this.getOwnerUUID());
			tag.putString("OwnerName", this.ownerName);
			tag.putByte("Order", (byte) this.getOrder().ordinal());
			if (this.getOrder() == Order.FOLLOW) {
				this.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).getPlayerStorage(this.getOwnerUUID()).lostMercPos
						.add(this.blockPosition());
				this.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).getPlayerStorage(this.getOwnerUUID()).setSave();
			} else if (this.getOrder() == Order.HOLD && this instanceof EntityMedic) {
				this.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).getPlayerStorage(this.getOwnerUUID()).medicMercPos
						.add(this.blockPosition());
				this.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).getPlayerStorage(this.getOwnerUUID()).setSave();
			} else if (this.getOrder() == Order.HOLD && !(this instanceof EntityEngineer)) {
				this.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).getPlayerStorage(this.getOwnerUUID()).restMercPos
						.add(this.blockPosition());
				this.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).getPlayerStorage(this.getOwnerUUID()).setSave();
			}
		}
		tag.putByte("MainWeapon", (byte) this.getMainWeapon());
		tag.putByte("TradeLevel", (byte) this.tradeLevel);
		tag.putByteArray("Empty", new byte[] { (byte) (isEmpty[0] ? 1 : 0), (byte) (isEmpty[1] ? 1 : 0),
				(byte) (isEmpty[2] ? 1 : 0), (byte) (isEmpty[3] ? 1 : 0) });
		tag.putByte("Difficulty", (byte) this.difficulty);
		tag.putFloat("RobotStrength", (short) this.robotStrength);
		tag.putInt("MoneyDrop", this.moneyDrop);

	}

	@Override
	public boolean checkSpawnRules(LevelAccessor levelAccessor, MobSpawnType spawnReason) {
		if (detectBanner())
			return this.level.getDifficulty() != Difficulty.PEACEFUL && super.checkSpawnRules(levelAccessor, spawnReason);

		if (this.isRemoved() || (TF2ConfigVars.overworldOnly && this.level.dimension() != Level.OVERWORLD)) {
			return false;
		}

		boolean naturalGround = TF2Util.isNaturalBlock(this.level, this.blockPosition().below(),
				this.level.getBlockState(this.blockPosition().below()));
		boolean validLight = this.isValidLightLevel(naturalGround, this.isRobot());

		if (!validLight)
			return false;

		return this.level.getDifficulty() != Difficulty.PEACEFUL && super.checkSpawnRules(levelAccessor, spawnReason);
	}

	public boolean detectBanner() {
		// System.out.println("Bannrs: "+this.world.getCapability(TF2weapons.WORLD_CAP,
		// null).banners.size());
		Iterator<BlockPos> iterator = this.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).banners.iterator();
		while (iterator.hasNext()) {
			BlockPos pos = iterator.next();
			if (pos.distSqr(this.blockPosition()) < 1200) {
				BlockEntity banner = this.level.getBlockEntity(pos);
				if (banner != null && banner instanceof BannerBlockEntity) {
					boolean fast = false;
					for (BannerPattern pattern : TF2EventsCommon.getPatterns((BannerBlockEntity) banner)) {
						if (pattern == TF2weapons.redPattern)
							this.bannerTeam = 0;
						else if (pattern == TF2weapons.bluPattern)
							this.bannerTeam = 1;
						else if (pattern == TF2weapons.neutralPattern) {
							this.discard();
							return false;
						} else if (pattern == TF2weapons.fastSpawn)
							fast = true;
					}
					return fast && pos.distSqr(this.blockPosition()) < 800;
				} else {
					iterator.remove();
					return false;
				}
			}
		}
		return false;
	}

	@Override
	protected void actuallyHurt(DamageSource damageSrc, float damageAmount) {
		if (!this.isInvulnerableTo(damageSrc)) {
			damageAmount = ForgeHooks.onLivingHurt(this, damageSrc, damageAmount);
			if (damageAmount <= 0)
				return;
			damageAmount = this.getDamageAfterArmorAbsorb(damageSrc, damageAmount);
			if (damageAmount <= 0)
				return;
			damageAmount = this.getDamageAfterMagicAbsorb(damageSrc, damageAmount);
			float f = damageAmount;
			damageAmount = Math.max(damageAmount - this.getAbsorptionAmount(), 0.0F);
			this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - damageAmount));
			damageAmount = ForgeHooks.onLivingDamage(this, damageSrc, damageAmount);

			if (damageAmount != 0.0F) {
				float f1 = this.getHealth();
				this.setHealth(this.getHealth() - damageAmount);
				this.getCombatTracker().recordDamage(damageSrc, f1, damageAmount);
			}
		}
	}

	protected boolean isValidLightLevel(boolean isNaturalBlock, boolean event) {

		BlockPos blockpos = new BlockPos(this.getX(), this.getBoundingBox().minY, this.getZ());
		int skylight = this.level.getBrightness(LightLayer.SKY, blockpos);
		if (16 + skylight < this.random.nextInt(32))
			return false;
		else {
			int lightbl = this.level.getBrightness(LightLayer.BLOCK, blockpos);
			if ((!isNaturalBlock && skylight < 13) || (lightbl > 1 && !event))
				return false;
			int i = this.level.getMaxLocalRawBrightness(blockpos);
			/*
			 * if (this.world.isThundering()) { int j = this.world.getSkylightSubtracted();
			 * this.world.setSkylightSubtracted(10); i =
			 * this.world.getLightFromNeighbors(blockpos);
			 * this.world.setSkylightSubtracted(j); }
			 */
			LevelChunk chunk = this.level.getChunkAt(blockpos);
			boolean spawnDay = event
					|| (this.random.nextInt(32) == 0 && new java.util.Random(chunk.getPos().toLong() ^ 987234911L).nextInt(10) == 0);
			return spawnDay || i <= 4 + this.random.nextInt(4);
		}
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void tick() {
		super.tick();
		if (!this.level.isClientSide && this.getOwnerUUID() == null && this.level.getDifficulty() == Difficulty.PEACEFUL)
			this.discard();
	}

	@Override
	public Team getTeam() {
		if (this.getOwner() != null)
			return this.getOwner().getTeam();
		else if (this.ownerName != null && !this.ownerName.isEmpty())
			return this.level.getScoreboard().getPlayersTeam(this.ownerName);
		return this.getEntTeam() == 0 ? this.level.getScoreboard().getPlayerTeam("RED")
				: this.getEntTeam() == 1 ? this.level.getScoreboard().getPlayerTeam("BLU")
						: this.level.getScoreboard().getPlayerTeam("Robots");
	}

	@Override
	protected SoundEvent getSwimSound() {
		return SoundEvents.HOSTILE_SWIM;
	}

	@Override
	protected SoundEvent getSwimSplashSound() {
		return SoundEvents.HOSTILE_SPLASH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.lastHurtByPlayerTime > 0 && source == DamageSource.MAGIC)
			this.lastHurtByPlayerTime = Math.max(20, this.lastHurtByPlayerTime);
		if (this.isInvulnerableTo(source))
			return false;
		else if (super.hurt(source, amount)) {
			if (event != null && (!(source.getEntity() instanceof LivingEntity)
					|| source.getEntity() == TF2weapons.dummyEnt)) {
				event.onDamageEnv(this, source, amount);
				this.damagedByEnv = true;
			}
			if (event != null && (source.getDirectEntity() instanceof EntitySentry)) {
				event.onDamageSentry(this, (EntitySentry) source.getDirectEntity(), source, amount);
			}
			Entity entity = source.getEntity();
			return this.getVehicle() != entity && this.getVehicle() != entity ? true : true;
		} else
			return false;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.HOSTILE_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.HOSTILE_DEATH;
	}

	@Override
	public boolean doHurtTarget(Entity entityIn) {
		float f = (float) this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
		int i = 0;

		if (entityIn instanceof LivingEntity) {
			f += EnchantmentHelper.getDamageBonus(this.getMainHandItem(),
					((LivingEntity) entityIn).getMobType());
			i += EnchantmentHelper.getKnockbackBonus(this);
		}

		boolean flag = entityIn.hurt(DamageSource.mobAttack(this), f);

		if (flag) {
			if (i > 0 && entityIn instanceof LivingEntity) {
				((LivingEntity) entityIn).knockback(i * 0.5F, Mth.sin(this.getYRot() * 0.017453292F),
						(-Mth.cos(this.getYRot() * 0.017453292F)));
				this.setDeltaMovement(this.getDeltaMovement().x * (0.6D), this.getDeltaMovement().y, this.getDeltaMovement().z);
				this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z * (0.6D));
			}

			int j = EnchantmentHelper.getFireAspect(this);

			if (j > 0)
				entityIn.setSecondsOnFire(j * 4);

			if (entityIn instanceof Player) {
				Player entityplayer = (Player) entityIn;
				ItemStack itemstack = this.getMainHandItem();
				ItemStack itemstack1 = entityplayer.isUsingItem() ? entityplayer.getUseItem() : null;

				if (!itemstack.isEmpty() && itemstack1 != null && itemstack.getItem() instanceof AxeItem
						&& itemstack1.getItem() == Items.SHIELD) {
					float f1 = 0.25F + EnchantmentHelper.getBlockEfficiency(this) * 0.05F;

					if (this.random.nextFloat() < f1) {
						entityplayer.getCooldowns().addCooldown(Items.SHIELD, 100);
						this.level.broadcastEntityEvent(entityplayer, (byte) 30);
					}
				}
			}

			this.doEnchantDamageEffects(this, entityIn);
		}

		return flag;
	}

	/**
	 * Checks if the entity's current position is a valid location to spawn this
	 * entity.
	 */

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.ATTACK_DAMAGE);
	}

	@Override
	protected boolean shouldDropLoot() {
		return true;
	}

	@Override
	public int getAmbientSoundInterval() {
		return this.getOwnerUUID() != null ? 1000 : 320;
	}

	@Override
	public float getVoicePitch() {
		return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.08F + 1.5F
				: (this.random.nextFloat() - this.random.nextFloat()) * 0.08F + 1.0F;
	}

	@Override
	protected float getSoundVolume() {
		return TF2ConfigVars.mercenaryVolume;
	}

	public float getMotionSensitivity() {
		return this.scaleWithDifficulty(0.18f, 0.03f);
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slotIn) {
		if (!this.level.isClientSide && slotIn == EquipmentSlot.MAINHAND)
			return this.loadout.getStackInSlot(this.usedSlot);
		else
			return super.getItemBySlot(slotIn);
	}

	public void onShot() {

	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return this.natural && this.getOwnerUUID() == null;
	}

	/*
	 * @Nullable protected ResourceLocation getLootTable() { return
	 * TF2weapons.lootTF2Character; }
	 */
	@Override
	public void setTradingPlayer(Player player) {
		this.trader = player;
	}

	@Override
	public Player getTradingPlayer() {
		return this.trader;
	}

	@Override
	public MerchantOffers getOffers() {
		if (this.tradeOffers == null)
			makeOffers();
		return tradeOffers;
	}

	public void makeOffers() {
		this.tradeOffers = new MerchantOffers();
		if (this.isRobot() || this.tradeLevel == -1)
			return;
		int weaponCount = 1 + this.random.nextInt(2 + this.tradeLevel);
		for (int i = 0; i < weaponCount; i++) {

			boolean buyItem = this.random.nextBoolean();
			int slot = getValidSlots()[this.random.nextInt(getValidSlots().length)];

			ItemStack item = ItemFromData.getRandomWeaponOfSlotMob(getTF2Class(), slot, this.getRandom(), false, 1, false);

			if (!item.isEmpty()) {
				int cost = ItemFromData.getData(item).getInt(PropertyType.COST);
				int basecost = cost;
				if (!buyItem) {
					if (this.tradeLevel == 1 && this.random.nextFloat() < 0.6f) {
						int upgrade = Math.min(800, 232 + (int) (this.level.getDayTime() / 4000));
						TF2Attribute.upgradeItemStack(this.loadout.getStackInSlot(0), upgrade, random);
						cost += upgrade / cost > 13 ? 5 : 10;
						if (this.random.nextFloat() < 0.25f) {
							item.getTag().putBoolean("Strange", true);
							cost += basecost * 8 + cost * 0.12f;
						}
					} else if (this.tradeLevel == 2) {
						int upgrade = Math.min(800, 232 + (int) (this.level.getDayTime() / 4000));
						TF2Attribute.upgradeItemStack(this.loadout.getStackInSlot(0),
								Math.min(3000, 1000 + (int) (this.level.getDayTime() / 2000)), random);
						cost += upgrade / cost > 13 ? 5 : 10;
						if (this.random.nextFloat() < 0.6f) {
							item.getTag().putBoolean("Strange", true);
							cost += basecost * 8 + cost * 0.12f;
						}
						if (this.random.nextFloat() < 0.25f) {
							item.getTag().putBoolean("Australium", true);
							cost += basecost * 16 + cost * 0.06f;
						}
						if (this.random.nextFloat() < 0.3f) {
							ArrayList<TF2Attribute> list = new ArrayList<>(Arrays.asList(TF2Attribute.attributes));
							list.removeIf(attr -> attr == null || attr.perKill == 0);
							item.getTag().putShort("StreakAttrib",
									(short) list.get(this.random.nextInt(list.size())).id);
							float random = this.random.nextFloat();
							int level = (random < 0.15 ? 3 : random < 0.5 ? 2 : 1);
							item.getTag().putByte("StreakLevel", (byte) level);
							cost += basecost * 8 * level + cost * 0.08f;
						}
					}
				}
				int costmult = cost < 27 || buyItem ? 0 : cost < 81 ? 1 : 2;
				ItemStack metal = TF2Util.metaStack(TF2weapons.itemTF2,
						Math.max(1, (cost / (costmult == 0 ? 9 : costmult == 1 ? 27 : 81))), 3 + costmult);
				ItemStack metal2 = TF2Util.metaStack(TF2weapons.itemTF2, Math.max(1, (cost / (costmult == 1 ? 9 : 27)) % 3),
						2 + costmult);
				this.tradeOffers.add(new MerchantOffer(buyItem ? item : metal,
						!buyItem && costmult > 0 ? metal2 : ItemStack.EMPTY, buyItem ? metal : item, 1, 0, 0f));
			}
		}
		int hatCount = this.random.nextInt(2 + this.tradeLevel);

		for (int i = 0; i < hatCount; i++) {

			boolean buyItem = this.random.nextBoolean();
			ItemStack item = ItemFromData.getRandomWeaponOfType("cosmetic", this.random, false);
			int cost = Math.max(1, ItemFromData.getData(item).getInt(PropertyType.COST));
			if (i == 0 && this.tradeLevel == 2) {
				((ItemWearable) item.getItem()).applyRandomEffect(item, random);
				cost *= 6 + this.random.nextInt(6);
				buyItem = false;
			}
			ItemStack metal = TF2Util.metaStack(TF2weapons.itemTF2, cost / 18, 5);
			ItemStack metal2 = TF2Util.metaStack(TF2weapons.itemTF2, this.random.nextInt(3), 4);
			if (metal2.getCount() == 0)
				metal2 = ItemStack.EMPTY;

			this.tradeOffers.add(new MerchantOffer(buyItem ? item : metal, buyItem ? ItemStack.EMPTY : metal2,
					buyItem ? metal : item, 1, 0, 0f));
		}

		if (this.tradeLevel >= 1) {
			boolean buyKey = this.random.nextBoolean();
			ItemStack item = TF2Util.metaStack(TF2weapons.itemTF2, 1, 7);
			ItemStack metal = TF2Util.metaStack(TF2weapons.itemTF2, 4, 5);
			ItemStack metal2 = TF2Util.metaStack(TF2weapons.itemTF2, this.random.nextInt(3), 4);
			if (metal2.getCount() == 0)
				metal2 = ItemStack.EMPTY;

			this.tradeOffers.add(new MerchantOffer(buyKey ? item : metal, buyKey ? ItemStack.EMPTY : metal2,
					buyKey ? metal : item, 1, 0, 0f));

			ArrayList<TF2Attribute> list = new ArrayList<>(Arrays.asList(TF2Attribute.attributes));
			list.removeIf(attr -> attr == null || attr.perKill == 0);
			int killstreakCount = this.random.nextInt(3 * this.tradeLevel);
			for (int i = 0; i < killstreakCount; i++) {
				int level = 0;
				int cost = 18;
				float random = this.random.nextFloat();
				if (this.tradeLevel == 2 && random < 0.05f) {
					level = 2;
					cost = 72;
				} else if (random < 0.2f * this.tradeLevel)
					level = 1;
				cost = 39;
				item = TF2Util.metaStack(TF2weapons.itemKillstreak, 1,
						list.get(this.random.nextInt(list.size())).id + (level << 9));
				if (i == 0 && (this.tradeLevel > 1)) {

				}

				this.addTradeOffer(item, cost, this.random.nextBoolean());
			}
		}
	}

	private void addTradeOffer(ItemStack toBuy, int cost, boolean buy) {
		boolean refined = cost >= 9;
		ItemStack ingot = TF2Util.metaStack(TF2weapons.itemTF2, cost / (refined ? 9 : 3), refined ? 5 : 4);
		ItemStack nugget = TF2Util.metaStack(TF2weapons.itemTF2, cost % (refined ? 9 : 3), 3);
		if (buy)
			this.tradeOffers.add(new MerchantOffer(ingot.getCount() > 0 ? ingot : nugget,
					nugget.getCount() > 0 ? nugget : ItemStack.EMPTY, toBuy, 100, 0, 0f));
		else
			this.tradeOffers
					.add(new MerchantOffer(toBuy, ItemStack.EMPTY, ingot.getCount() > 0 ? ingot : nugget, 100, 0, 0f));
	}

	@Override
	public void overrideOffers(MerchantOffers recipeList) {
		this.tradeOffers = recipeList;

	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
		return size.height * 0.9f;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		float scale = this.getRobotSize() == 2 ? 1.65f : this.getRobotSize() > 2 ? 1.88f : 1f;
		return EntityDimensions.scalable(Math.min(Math.max(0.98f, this.getWidth()), this.getWidth() * scale),
				this.getHeight() * scale);
	}

	public int[] getValidSlots() {
		return new int[] { 0, 1, 2 };
	}

	public float[] getDropChance() {
		return new float[] { 0.08f, 0.15f, 0.15f };
	}

	@Override
	public void notifyTrade(MerchantOffer recipe) {
		recipe.increaseUses();
		if (this.getOwnerUUID() != null) {
			if (recipe.getResult().getItem() instanceof ItemWeapon) {
				this.loadout.setStackInSlot(
						ItemFromData.getSlotForClass(ItemFromData.getData(recipe.getResult()), this),
						recipe.getResult());
				this.switchSlot(0);
			}

			else if (recipe.getResult().getItem() instanceof ItemWearable) {
				this.setItemSlot(EquipmentSlot.HEAD, recipe.getResult());
				this.armorDropChances[0] = 0f;
			}
		}
		this.ambientSoundTime = -this.getAmbientSoundInterval();
		this.playSound(SoundEvents.VILLAGER_YES, this.getSoundVolume(), this.getVoicePitch());
		int i = 3 + this.random.nextInt(4);

		this.lastTrader = this.trader;
		this.tradeCount.put(this.trader,
				this.tradeCount.containsKey(trader) ? this.tradeCount.get(this.trader) + 1 : 1);
		this.traderFollowTicks = Math.min(4800, this.tradeCount.get(this.trader) * 250 + 350);
		if (recipe.shouldRewardExp())
			this.level.addFreshEntity(new ExperienceOrb(this.level, this.getX(), this.getY() + 0.5D, this.getZ(), i));

	}

	public void switchSlot(int slot) {
		this.switchSlot(slot, false, false);
	}

	public void switchSlot(int slot, boolean noAmmoSwitch, boolean forceRefresh) {
		ItemStack stack = this.loadout.getStackInSlot(slot);

		if (this.getMainWeapon() != -1 && this.getMainWeapon() != slot && !forceRefresh && !noAmmoSwitch)
			return;
		if (stack.isEmpty())
			return;
		if (stack.getItem() instanceof ItemFromData && !((ItemFromData) stack.getItem()).canSwitchTo(stack)) {
			return;
		}
		if (!noAmmoSwitch)
			this.preferredSlot = slot;

		boolean itemChange = !(stack.getItem() instanceof ItemUsable);

		if ((slot != this.usedSlot || forceRefresh) && stack.getItem() instanceof ItemUsable) {
			if (!((ItemUsable) stack.getItem()).isAmmoSufficient(stack, this, true))
				if (!this.refill(this.usedSlot)) {
					this.switchSlot(this.getFirstSlotWithAmmo(), true, forceRefresh);
					return;
				}
			itemChange = true;
		}

		if (itemChange) {
			if (this.getMainHandItem().getItem() instanceof ItemUsable && slot != this.usedSlot)
				((ItemUsable) this.getMainHandItem().getItem()).holster(getWepCapability(), stack, this, this.level);

			this.setItemInHand(InteractionHand.MAIN_HAND, stack);

			if (stack.getItem() instanceof ItemUsable)
				this.onEquipItem(slot, stack);
			if (slot != this.usedSlot)
				this.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setPrimaryCooldown(400);
			this.usedSlot = slot;
		}
	}

	public void switchSlotActual(int slot) {
		this.switchSlot(slot, false, false);
	}

	public void onEquipItem(int slot, ItemStack stack) {

		WeaponData data = ItemFromData.getData(stack);
		this.attack.explosive = TF2Attribute.EXPLOSIVE.test(stack);
		this.attack.projSpeed = TF2Attribute.getModifier("Proj Speed", stack,
				data.getFloat(PropertyType.PROJECTILE_SPEED), this);
		this.attack.fireAtFeet = 0;
		if (stack.getItem() instanceof ItemHuntsman) {
			this.attack.fireAtFeet = -1;
			this.getWepCapability().chargeTicks = ((ItemWeapon) stack.getItem()).holdingMode(stack, this);
		}

		if (this.isGiant()) {
			this.attack.setRange(data.getFloat(PropertyType.EFFICIENT_RANGE) * 1.75f);
		} else {
			this.attack.setRange(data.getFloat(PropertyType.EFFICIENT_RANGE));
		}

		this.moveAttack.setRange(data.getFloat(PropertyType.EFFICIENT_RANGE), data.get(PropertyType.MINIMAL_RANGE, 3f));
		((ItemUsable) stack.getItem()).draw(getWepCapability(), stack, this, this.level);
		if (stack.getItem() instanceof ItemProjectileWeapon) {
			String projName = data.getString(PropertyType.PROJECTILE);
			try {
				EntityProjectileBase proj = MapList.projectileClasses.get(projName).getConstructor(Level.class)
						.newInstance(this.level);
				proj.initProjectile(this, InteractionHand.MAIN_HAND, stack);
				this.attack.gravity = (float) proj.getGravityOverride();
			} catch (Exception e) {

			}
		}
		this.getWepCapability().chargeTicks = 0;
		/*
		 * if(projName.equals("grenade") || projName.equals("sticky")){
		 * this.attack.gravity=0.0381f; } else if(projName.equals("flare")){
		 * this.attack.gravity=0.019f; }
		 */

	}

	@Override
	public void notifyTradeUpdated(ItemStack stack) {}

	@Override
	public int getVillagerXp() {
		return 0;
	}

	@Override
	public void overrideXp(int xp) {}

	@Override
	public boolean showProgressBar() {
		return false;
	}

	@Override
	public SoundEvent getNotifyTradeSound() {
		return SoundEvents.VILLAGER_YES;
	}

	@Override
	public boolean isClientSide() {
		return this.level.isClientSide;
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 4;
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource source, int lootingModifier, boolean wasRecentlyHit) {
		for (int i = 0; i < 4; i++) {
			if (!this.loadoutHeld.getStackInSlot(i).isEmpty() || !this.hasHeldInventory()) {
				if (!this.loadout.getStackInSlot(i).isEmpty())
					TF2Util.setThrower(this.spawnAtLocation(this.loadout.getStackInSlot(i), 0), this.getOwnerUUID());
				this.loadout.setStackInSlot(i, this.loadoutHeld.getStackInSlot(i));
			}
		}
		for (int i = 0; i < 4; i++)
			if (!this.loadoutHeld.getStackInSlot(i + 4).isEmpty() || !this.hasHeldInventory()) {
				if (!this.getItemBySlot(EquipmentSlot.values()[5 - i]).isEmpty())
					TF2Util.setThrower(this.spawnAtLocation(this.getItemBySlot(EquipmentSlot.values()[5 - i]), 0), this.getOwnerUUID());
				this.setItemSlot(EquipmentSlot.values()[5 - i], this.loadoutHeld.getStackInSlot(i + 4));
				this.setDropChance(EquipmentSlot.values()[5 - i], i == 0 ? 0.25f : 0f);
			}
		if (!this.isRobot() && this.getOwnerUUID() != null) {
			ItemStack stack = TF2Util.metaStack(TF2weapons.itemTF2, this.isSharing() ? 2 : 1, 2);
			if (this.getOwner() != null && this.distanceToSqr(this.getOwner()) < 100)
				this.spawnAtLocation(stack, 0);
			else {
				Map<String, MerchantOffers> map = this.level.getCapability(TF2weapons.WORLD_CAP).orElse(null).lostItems;
				if (!map.containsKey(this.ownerName))
					map.put(ownerName, new MerchantOffers());
				map.get(ownerName).add(new MerchantOffer(ItemStack.EMPTY, ItemStack.EMPTY, stack, 1, 0, 0f));
			}
		}
		LivingEntity attacker = this.getKillCredit();
		if (!this.refill.getStackInSlot(0).isEmpty()
				&& (attacker == this.getOwner() || !TF2Util.isAlliedTo(attacker, this))) {
			this.spawnAtLocation(this.refill.getStackInSlot(0), 0);
		}

		boolean isPlayerAttacker = (attacker instanceof Player
				|| (attacker instanceof OwnableEntity && ((OwnableEntity) attacker).getOwnerUUID() != null))
				&& (attacker.getTeam() != null || TF2ConfigVars.neutralAttack) && TF2Util.isEnemy(attacker, this);
		if (!this.isRobot() && isPlayerAttacker)
			for (int i = 0; i < loadout.getSlots(); i++) {
				ItemStack stack = loadout.getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() instanceof ItemFromData
						&& this.random.nextFloat() <= this.getDropChance()[i] * (1 + lootingModifier * 0.4f)) {
					stack.setCount(1);
					if (stack.getItem() instanceof ItemFromData)
						TF2Attribute
								.upgradeItemStack(stack,
										(int) (Math.min(320, this.level.getDayTime() / 4000)
												+ random.nextInt((int) Math.min(720, this.level.getDayTime() / 2000))),
										random);
					this.spawnAtLocation(stack, 0);
				}
			}
		else if (this.armorDropChances[0] < 1) {
			this.armorDropChances[0] = 0;
		}
		if (this.getMoneyDrop() > 0 && (isPlayerAttacker || this.getOwnerUUID() != null)) {
			for (int i = 0; i < this.getMoneyDrop() / 64; i++) {
				this.spawnAtLocation(TF2Util.metaStack(TF2weapons.itemMoney, 64, 0), 0);
			}

			this.spawnAtLocation(TF2Util.metaStack(TF2weapons.itemMoney, this.getMoneyDrop() % 64, 0), 0);
			this.setMoneyDrop(0);
		}
		if (this.isRobot() && this.getOwnerUUID() != null) {
			this.spawnAtLocation(TF2Util.metaStack(TF2weapons.itemTF2, 1, this.isGiant() ? 13 : 12), 0f);
		}
		super.dropCustomDeathLoot(source, lootingModifier, wasRecentlyHit);
	}


	public WeaponsCapability getWepCapability() {
		return this.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
	}

	@Override
	public int getExperienceReward() {
		Player player = this.lastHurtByPlayer;
		if (player != null && TF2Util.isAlliedTo(player, this))
			return 0;
		else if (this.isRobot() && !this.isGiant())
			return super.getExperienceReward() / 2;
		else
			return super.getExperienceReward();

	}
	/*
	 * @Override public void writeSpawnData(ByteBuf buffer) { PacketBuffer
	 * packet=new PacketBuffer(buffer); for(int i=0;i<this.loadout.length;i++){
	 * packet.writeByte(i); packet.writeItemStackToBuffer(this.loadout[i]); } }
	 *
	 * @Override public void readSpawnData(ByteBuf additionalData) { PacketBuffer
	 * packet=new PacketBuffer(additionalData); while(packet.readableBytes()>0){ try
	 * { this.loadout[packet.readByte()]=packet.readItemStackFromBuffer(); } catch
	 * (IOException e) { e.printStackTrace(); } } }
	 */

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (ROBOT.equals(key)) {
			if (this.robotSizeEnsured != this.getRobotSize()) {
				this.robotSizeEnsured = this.getRobotSize();
				this.refreshDimensions();
			}
		}
	}

	@Override
	public UUID getOwnerUUID() {
		return this.getEntityData().get(OWNER_UUID).orElse(null);
	}

	@Override
	public Entity getOwner() {
		if (this.getOwnerUUID() != null)
			return this.level.getPlayerByUUID(this.getEntityData().get(OWNER_UUID).orElse(null));
		else
			return null;
	}

	public void setOwner(LivingEntity owner) {
		if (owner == null) {
			this.ownerName = null;
			this.entityData.set(OWNER_UUID, Optional.empty());
		} else if (owner instanceof Player) {
			this.ownerName = owner.getName().getString();
			this.entityData.set(OWNER_UUID, Optional.of(owner.getUUID()));
			this.setPersistenceRequired();
		}

	}

	public void setOwnerID(String name, UUID owner) {
		this.ownerName = name;
		this.entityData.set(OWNER_UUID, Optional.of(owner));
		this.setPersistenceRequired();
	}

	public static enum Order {
		FOLLOW, HOLD;
	}

	public int getMaxAmmo(int slot) {

		int ammo = ItemFromData.getData(this.loadout.getStackInSlot(slot == -1 ? this.usedSlot : slot))
				.getInt(PropertyType.MAX_AMMO);
		return (int) (this.getOwnerUUID() != null ? ammo : this.scaleWithDifficulty(0.5f, 1f) * ammo);
	}

	public int getAmmo(int slot) {
		if (slot == 1000 || slot == -1 || slot >= ammoCount.length)
			slot = 0;
		if (this.unlimitedAmmo)
			return this.getMaxAmmo(slot);
		return ammoCount[slot];
	}

	public boolean isAmmoFull() {
		if (this.unlimitedAmmo)
			return true;
		else {
			for (int i = 0; i < ammoCount.length; i++) {
				ItemStack stack = loadout.getStackInSlot(i);
				if (stack.getItem() instanceof ItemUsable && (this.ammoCount[i] < this.getMaxAmmo(i)))
					return false;
			}
		}
		return true;
	}

	public int getFirstSlotWithAmmo() {
		if (this.unlimitedAmmo)
			return this.getMainWeapon() == -1 ? this.getDefaultSlot() : this.getMainWeapon();
		for (int i = 0; i < loadout.getSlots(); i++) {
			ItemStack stack = loadout.getStackInSlot(i);
			if (stack.getItem() instanceof ItemUsable) {
				if (((ItemUsable) stack.getItem()).isAmmoSufficient(stack, this, true))
					return i;
				else if (refill(i))
					return i;
			}
		}
		return 2;
	}

	public boolean refill(int slot) {
		if (!this.refill.getStackInSlot(0).isEmpty() && TF2Util.isOre("ingotLead", this.refill.getStackInSlot(0))) {
			this.refill.extractItem(0, 1, false);
			this.ammoCount[slot] = (int) (this.getMaxAmmo(slot) * 0.4f);
			return true;
		}
		if (this.isRobot() && this.refill.getStackInSlot(0).getItem() instanceof ItemMoney) {
			ItemStack stack = this.refill.extractItem(0, 1, false);
			this.ammoCount[slot] = this.getMaxAmmo(slot) * ((ItemMoney) stack.getItem()).getValue(stack);
			return true;
		}
		return false;
	}

	public boolean restoreAmmo(float p) {
		for (int i = 0; i < ammoCount.length; i++) {
			if (loadout.getStackInSlot(i).getItem() instanceof ItemUsable) {
				int maxAmmo = getMaxAmmo(i);
				ammoCount[i] = Math.min(ammoCount[i] + (int) (maxAmmo * p), maxAmmo);
			}
		}
		return true;
	}

	public int getState(boolean onTarget) {
		return onTarget ? 1 : 0;
	}

	public abstract TF2Class getTF2Class();

	/*
	 * 0% - easy 50% - medium 75% - hard 87.5% - hard + hat 93.75% - hard + unusual
	 */
	public float scaleWithDifficulty(float min, float max) {
		int diff = this.getDiff();
		float div = (1 << (diff - 1));
		if (this.isRobot())
			div = Math.max(div * 0.67f, 1f);
		if (diff < 3 && !this.shouldScaleAttributes())
			diff = 3;
		return TF2Util.lerp(min, max, 1 - (1f / div));
	}

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
		return true;
	}

	@Override
	public boolean isBuilding() {
		return false;
	}

	@Override
	public boolean isBackStabbable(LivingEntity attacker, ItemStack knife) {
		return true;
	}

	public boolean isTargetEnemy() {
		return !this.friendly;
	}

	public int getMoneyDrop() {
		return moneyDrop;
	}

	public void setMoneyDrop(int moneyDrop) {
		this.moneyDrop = moneyDrop;
	}

	@Override
	public float getBackstabDamageReduction(LivingEntity attacker, ItemStack knife, float mult) {
		if (this.isGiant()) {
			mult *= 0.6f;
			if (this.getPersistentData().contains(NBTLiterals.BACKSTAB_MULT))
				mult /= this.getPersistentData().getFloat(NBTLiterals.BACKSTAB_MULT);
		}
		return mult;
	}

}
