package rafradek.TF2weapons.entity.building;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.BuildingSound;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.IEntityTF2;
import rafradek.TF2weapons.entity.mercenary.EntityEngineer;
import rafradek.TF2weapons.item.ItemPDA;
import rafradek.TF2weapons.item.ItemSapper;
import rafradek.TF2weapons.util.PlayerPersistStorage;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.Optional;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.ItemStackHandler;

public class EntityBuilding extends Mob implements OwnableEntity, IEntityTF2 {

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

	private static final EntityDataAccessor<Byte> VIS_TEAM = SynchedEntityData.defineId(EntityBuilding.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> LEVEL = SynchedEntityData.defineId(EntityBuilding.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> SOUND_STATE = SynchedEntityData.defineId(EntityBuilding.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Integer> PROGRESS = SynchedEntityData.defineId(EntityBuilding.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> CONSTRUCTING = SynchedEntityData.defineId(EntityBuilding.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> ENERGY = SynchedEntityData.defineId(EntityBuilding.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Byte> SAPPED = SynchedEntityData.defineId(EntityBuilding.class,
			EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(EntityBuilding.class,
			EntityDataSerializers.OPTIONAL_UUID);

	private static final Logger LOGGER = LogManager.getLogger();

	public static final UUID UPGRADE_HEALTH_UUID = UUID.fromString("1184831d-b1dc-40c8-86e6-34fa8f30bada");

	public static final DamageSource DETONATE = new DamageSource("detonate").bypassArmor()
			.bypassMagic();
	public static final int SENTRY_COST = 130;
	public static final int DISPENSER_COST = 100;
	public static final int TELEPORTER_COST = 50;
	public static final int SENTRY_MINI_COST = 100;
	public static final int SENTRY_DISPOSABLE_COST = 60;

	public LivingEntity owner;
	public BuildingSound buildingSound;
	public int wrenchBonusTime;
	public float wrenchBonusMult;
	public ItemStack sapper = ItemStack.EMPTY;
	public LivingEntity sapperOwner;
	public boolean playerOwner;
	public boolean redeploy;
	public String ownerName;
	public EnergyStorage energy;
	public int ticksNoOwner;
	private boolean engMade;
	public ItemStackHandler charge;
	public boolean fromPDA;
	private int disposableID = -1;
	public UUID ownerEntityID;

	protected EntityBuilding(EntityType<? extends EntityBuilding> type, Level world) {
		super(type, world);
		this.applyTasks();
		this.setHealth(0.1f);
		this.energy = new EnergyStorage(40000);
		this.charge = new ItemStackHandler(1);
		// this.notifyDataManagerChange(LEVEL);
		this.adjustSize();
		enablePersistence();
	}

	public void applyTasks() {

	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {
		if (entityIn.getBoundingBox().intersects(this.getCollisionBoundingBox()))
			super.applyEntityCollision(entityIn);
	}

	public int getMaxLevel() {
		return 3;
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		this.adjustSize();

		// System.out.println("Watcher update: "+data);
		if (!this.level.isClientSide && CONSTRUCTING.equals(key)) {
			this.setSoundState(this.entityData.get(CONSTRUCTING) >= this.getConstructionTime() ? 0 : 25);
		}
		if (this.level.isClientSide && SOUND_STATE.equals(key)) {
			SoundEvent sound = this.getSoundNameForState(this.getSoundState());
			if (sound != null) {
				// System.out.println("Playing Sound: "+sound);
				if (this.buildingSound != null)
					this.buildingSound.stopPlaying();
				this.buildingSound = new BuildingSound(this, sound, this.getSoundState());
				ClientProxy.playBuildingSound(buildingSound);
			} else {
				if (this.buildingSound != null)
					this.buildingSound.stopPlaying();
			}
		}
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!this.level.isClientSide && player == this.getOwner() && hand == InteractionHand.MAIN_HAND) {
			this.grab();
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		}
		return InteractionResult.PASS;
	}

	@Override
	public ItemStack getPickedResult(HitResult target) {
		ItemStack stack = TF2Util.metaStack(TF2weapons.itemBuildingBox, 1,
				(this instanceof EntitySentry ? 18 : (this instanceof EntityDispenser ? 20 : 22)) + this.getEntTeam());

		return stack;
	}

	public void grab() {
		if (!this.isDisabled() && this.disposableID == -1) {
			boolean grabbed = true;
			if (this.owner instanceof EntityEngineer) {
				CompoundTag tag = new CompoundTag();
				this.addAdditionalSaveData(tag);
				((EntityEngineer) this.owner).grabbed = tag;
				((EntityEngineer) this.owner).grabbedid = this.getBuildingID();
				((EntityEngineer) this.owner).loadout.getStackInSlot(3).getTag().putByte("Building",
						(byte) (this.getBuildingID() + 1));
				((EntityEngineer) this.owner).switchSlot(3);
			} else if (this.fromPDA) {
				int slotpda = TF2Util.getFirstItemSlot(((Player) this.getOwner()).inventory,
						stack -> stack.getItem() instanceof ItemPDA);
				if (slotpda != -1) {
					CompoundTag tag = new CompoundTag();
					this.addAdditionalSaveData(tag);
					TF2PlayerCapability.get((Player) this.getOwner()).carrying = tag;
					TF2PlayerCapability.get((Player) this.getOwner()).carryingType = this.getBuildingID();
					this.clearReferences();
				} else
					grabbed = false;
			} else {
				ItemStack stack = this.getPickedResult(null);
				stack.setTag(new CompoundTag());
				stack.getTag().put("SavedEntity", new CompoundTag());
				this.addAdditionalSaveData(stack.getTag().getCompound("SavedEntity"));
				this.spawnAtLocation(stack, 0);
			}

			// System.out.println("Saved:
			// "+stack.getTagCompound().getCompoundTag("SavedEntity"));
			if (grabbed)
				this.discard();
		}
	}

	public void adjustSize() {

	}

	@Override
	public boolean isPotionApplicable(MobEffectInstance potioneffectIn) {
		return potioneffectIn.getEffect() == TF2weapons.stun;
	}

	public SoundEvent getSoundNameForState(int state) {
		return state == 50 ? TF2Sounds.MOB_SAPPER_IDLE : null;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 16D * TF2ConfigVars.damageMultiplier)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1D)
				.add(Attributes.MOVEMENT_SPEED, 0D);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source == DamageSource.ON_FIRE)
			return false;
		return super.hurt(source, amount);
	}

	@Override
	public void setFire(int time) {
		super.setSecondsOnFire(0);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(VIS_TEAM, (byte) this.random.nextInt(2));
		this.entityData.define(OWNER_UUID, Optional.<UUID>absent());
		this.entityData.define(LEVEL, (byte) 1);
		this.entityData.define(SOUND_STATE, (byte) 25);
		this.entityData.define(PROGRESS, 0);
		this.entityData.define(SAPPED, (byte) 0);
		this.entityData.define(CONSTRUCTING, 0);
		this.entityData.define(ENERGY, 0);

	}

	public int getSoundState() {
		return this.entityData.get(SOUND_STATE);
	}

	public void setSoundState(int state) {
		this.entityData.set(SOUND_STATE, (byte) state);
	}

	@Override
	public UUID getOwnerUUID() {
		return this.entityData.get(OWNER_UUID).orNull();
	}

	@Override
	public LivingEntity getOwner() {
		if (this.owner != null && !(this.owner instanceof Player && this.owner.isRemoved()))
			return this.owner;
		else if (this.getOwnerUUID() != null)
			return this.owner = this.level.getPlayerByUUID(this.getOwnerUUID());
		return null;
	}

	public void setOwner(LivingEntity owner) {
		this.owner = owner;
		if (owner instanceof Player) {
			this.ownerName = owner.getName();
			this.entityData.set(OWNER_UUID, Optional.of(owner.getUUID()));
		} else if (this.getOwnerUUID() != null)
			this.entityData.set(OWNER_UUID, Optional.absent());
		else if (owner != null)
			this.engMade = true;
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (this.getOwner() instanceof EntityEngineer) {
			WeaponsCapability.get(this.getOwner()).giveMetal(
					getCost(this.getBuildingID(), ((EntityEngineer) this.getOwner()).loadout.getStackInSlot(2)) / 2);
		}
		this.clearReferences();
	}

	public void clearReferences() {
		if (this.getOwnerUUID() != null && this.fromPDA) {
			if (this.disposableID == -1)
				PlayerPersistStorage.get(this.level, getOwnerUUID()).buildings[this.getBuildingID()] = null;
			else {
				try {
					PlayerPersistStorage.get(this.level, getOwnerUUID()).disposableBuildings.remove(this.getUUID());
				} catch (IndexOutOfBoundsException e) {
					LOGGER.error("Disposable ID out of bounds");
				}
			}
		}
	}

	@Override
	public void tick() {
		this.setDeltaMovement((0), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (0));
		if (this.firstUpdate && !this.level.isClientSide && this.fromPDA
				&& !PlayerPersistStorage.get(this.level, this.getOwnerUUID()).allowBuilding(this)) {
			this.discard();
			return;
		}

		if (!this.level.isClientSide && this.engMade && this.getOwnerUUID() == null
				&& (this.owner == null || this.owner.isRemoved()) && this.ticksNoOwner++ >= 120)
			this.setHealth(0);
		else
			this.ticksNoOwner = 0;
		if (this.getDeltaMovement().y > 0)
			this.setDeltaMovement(this.getDeltaMovement().x, (0), this.getDeltaMovement().z);
		if (!this.level.isClientSide) {

			if (this.tickCount % 80 == 0) {
				int j1 = Mth.floor(this.getYRot() * 256.0F / 360.0F);
				int l1 = Mth.floor(this.getXRot() * 256.0F / 360.0F);
				((ServerLevel) this.level).getEntityTracker().sendToTracking(this,
						new SPacketEntity.S16PacketEntityLook(this.getId(), (byte) j1, (byte) l1, true));
			}

			if (this.fromPDA && this.tickCount % 5 == 0 && this.isAlive()) {
				PlayerPersistStorage storage = PlayerPersistStorage.get(this.level, this.getOwnerUUID());
				if (this.disposableID == -1) {
					if (storage.buildings[this.getBuildingID()] == null
							|| !storage.buildings[this.getBuildingID()].getA().equals(this.getUUID())) {
						this.setHealth(0);
						this.onDeath(DETONATE);
						return;
					}
					CompoundTag tag = storage.buildings[this.getBuildingID()].getB();
					this.addAdditionalSaveData(tag);
				} else {
					if (!storage.disposableBuildings.contains(this.getUUID())) {
						this.setHealth(0);
						this.onDeath(DETONATE);
						return;
					}
				}
				// storage.buildings[this.getBuildingID()] = new
				// Tuple<>(this.getUniqueID(),tag);
			}

			if (this.isSapped())
				TF2Util.dealDamage(this, this.level, this.sapperOwner, this.sapper, 0,
						this.sapper.isEmpty() ? 0.14f
								: ((ItemSapper) this.sapper.getItem()).getWeaponDamage(sapper, this.sapperOwner, this),
								TF2Util.causeDirectDamage(this.sapper, this.sapperOwner));

			if (this.charge.getStackInSlot(0).getCapability(CapabilityEnergy.ENERGY).isPresent()) {
				this.energy.receiveEnergy(this.charge.getStackInSlot(0).getCapability(CapabilityEnergy.ENERGY).orElse(null)
						.extractEnergy(this.energy.receiveEnergy(this.energy.getMaxEnergyStored(), true), false),
						false);
			}
			this.setInfoEnergy(this.energy.getEnergyStored());
			if (this.getOwnerUUID() != null && shouldUseBlocks())
				for (Direction facing : Direction.VALUES) {
					BlockPos pos = this.blockPosition().relative(facing);
					BlockEntity ent = this.level.getBlockEntity(pos);

					if (ent != null) {
						this.drawFromBlock(pos, ent, facing);
					}
				}
		}

		super.tick();

		if (this.isConstructing())
			this.updateConstruction();
		this.wrenchBonusTime--;

	}

	public void detonate() {
		this.setHealth(0);
		this.onDeath(DETONATE);
	}

	public void drawFromBlock(BlockPos pos, BlockEntity ent, Direction facing) {
		if (ent.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
			this.energy.receiveEnergy(ent.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite())
					.extractEnergy(this.energy.receiveEnergy(this.energy.getMaxEnergyStored(), true), false), false);
		}
	}

	public boolean shouldUseBlocks() {
		return this.energy.getEnergyStored() != this.energy.getMaxEnergyStored();
	}

	public boolean consumeEnergy(int amount) {
		return this.energy.getEnergyStored() >= amount && this.energy.extractEnergy(amount, true) == amount
				&& this.energy.extractEnergy(amount, false) == amount;
	}

	public int getMinEnergy() {
		return 0;
	}

	public void setSapped(LivingEntity owner, ItemStack sapper) {
		this.sapperOwner = owner;
		this.sapper = sapper;
		this.entityData.set(SAPPED, (byte) 2);
		this.setSoundState(50);
	}

	@Override
	public boolean isAIDisabled() {
		return super.isAIDisabled() || this.isDisabled();
	}

	public boolean isSapped() {
		return this.entityData.get(SAPPED) > 0;
	}

	public boolean isDisabled() {
		return !this.isAlive() || this.isConstructing() || this.isSapped()
				|| this.energy.getEnergyStored() < this.getMinEnergy()
				|| this.getEffect(TF2weapons.stun) != null;
	}

	public void removeSapper() {
		entityData.set(SAPPED, (byte) (entityData.get(SAPPED) - 1));
		if (!isSapped()) {
			this.setSoundState(0);
			this.playSound(TF2Sounds.MOB_SAPPER_DEATH, 1.5f, 1f);
			this.dropItem(Items.IRON_INGOT, 1);
		}
	}

	@Override
	public AABB getCollisionBox(Entity entityIn) {
		return ((entityIn != null && !TF2Util.isAlliedTo(entityIn, this)) || entityIn == this.getOwner())
				&& this.isAlive() ? entityIn.getBoundingBox() : null;
	}

	@Override
	public AABB getCollisionBoundingBox() {
		if (!this.isAlive())
			return null;
		/*
		 * else if(this.height>this.getCollHeight()){ AxisAlignedBB
		 * colBox=this.getEntityBoundingBox();
		 * colBox=colBox.grow((this.getCollWidth()-this.width)/2,
		 * (this.getCollHeight()-this.height)/2, (this.getCollWidth()-this.width)/2);
		 * colBox=colBox.offset(0, this.getEntityBoundingBox().minY-colBox.minY, 0);
		 * return colBox; }
		 */
		return this.getBoundingBox();
	}

	@Override
	public Team getTeam() {
		if (this.getOwner() != null) {
			return this.getOwner().getTeam();
		} else if (this.getOwnerUUID() != null) {
			return this.level.getScoreboard().getPlayersTeam(this.ownerName);
		} else {
			switch (this.getEntTeam()) {
			case 0:
				return this.level.getScoreboard().getPlayerTeam("RED");
			case 1:
				return this.level.getScoreboard().getPlayerTeam("BLU");
			case 2:
				return this.level.getScoreboard().getPlayerTeam("Robots");
			default:
				return this.level.getScoreboard().getPlayerTeam("RED");
			}
		}
	}

	public int getProgress() {
		if (this.isConstructing())
			return (int) (((float) this.entityData.get(CONSTRUCTING) / this.getConstructionTime()) * 200);
		else
			return this.entityData.get(PROGRESS);
	}

	public void setProgress(int progress) {
		this.entityData.set(PROGRESS, progress);
	}

	public int getInfoEnergy() {
		return this.entityData.get(ENERGY);
	}

	public void setInfoEnergy(int energy) {
		this.entityData.set(ENERGY, energy);
	}

	public int getLevel() {
		return this.entityData.get(LEVEL);
	}

	public void setLevel(int level) {
		this.entityData.set(LEVEL, (byte) level);
	}

	public void upgrade() {
		this.setLevel(this.getLevel() + 1);
		this.setProgress(0);
		this.getAttribute(Attributes.MAX_HEALTH)
		.setBaseValue(this.getAttribute(Attributes.MAX_HEALTH).getBaseValue() * 1.2);
		this.setHealth(this.getMaxHealth());
		this.adjustSize();
	}

	public int getEntTeam() {
		return this.entityData.get(VIS_TEAM);
	}

	public void setEntTeam(int team) {
		this.entityData.set(VIS_TEAM, (byte) team);
	}

	public boolean isConstructing() {
		return this.entityData.get(CONSTRUCTING) < this.getConstructionTime();
	}

	public void setConstructing(boolean constr) {

		this.entityData.set(CONSTRUCTING, constr ? 0 : this.getConstructionTime());
	}

	public void updateConstruction() {
		if (!this.redeploy)
			this.heal((this.getConstructionRate() * this.getMaxHealth()) / this.getConstructionTime());
		this.entityData.set(CONSTRUCTING, this.entityData.get(CONSTRUCTING) + this.getConstructionRate());
		if (this.redeploy && this.entityData.get(CONSTRUCTING) >= this.getConstructionTime())
			this.redeploy = false;
	}
	/*
	 * @Override public boolean writeToNBTOptional(NBTTagCompound tagCompund) {
	 * return this.getOwnerUUID() != null ? super.writeToNBTOptional(tagCompund) :
	 * false; }
	 */

	@Override
	public void addAdditionalSaveData(CompoundTag par1NBTTagCompound) {
		super.addAdditionalSaveData(par1NBTTagCompound);

		par1NBTTagCompound.putByte("Team", (byte) this.getEntTeam());
		par1NBTTagCompound.putByte("Level", (byte) this.getLevel());
		par1NBTTagCompound.putShort("Progress", (byte) this.getProgress());
		par1NBTTagCompound.putShort("Sapper", this.entityData.get(SAPPED));
		par1NBTTagCompound.putShort("Construction", this.entityData.get(CONSTRUCTING).shortValue());
		par1NBTTagCompound.putByte("WrenchBonus", (byte) this.wrenchBonusTime);
		par1NBTTagCompound.putBoolean("Redeploy", this.redeploy);
		par1NBTTagCompound.putBoolean("EngMade", this.engMade);
		par1NBTTagCompound.putBoolean("FromPDA", this.fromPDA);
		par1NBTTagCompound.putByte("TicksOwnerless", (byte) this.ticksNoOwner);
		par1NBTTagCompound.put("Charge", this.charge.serializeNBT());
		par1NBTTagCompound.putInt("Energy", this.energy.getEnergyStored());
		par1NBTTagCompound.putByte("DisposableID", (byte) this.disposableID);
		if (this.getOwner() != null && !(this.getOwner() instanceof Player))
			par1NBTTagCompound.putUUID("OwnerE", this.getOwner().getUUID());
		if (this.getOwnerUUID() != null) {
			par1NBTTagCompound.putUUID("Owner", this.getOwnerUUID());
			par1NBTTagCompound.putString("OwnerName", this.ownerName);
		}
		if (this.isDisabled()) {
			par1NBTTagCompound.putBoolean("NoAI", false);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);

		this.setEntTeam(tag.getByte("Team"));
		this.setLevel(tag.getByte("Level"));
		this.setProgress(tag.getByte("Progress"));
		this.entityData.set(CONSTRUCTING, (int) tag.getShort("Construction"));
		this.wrenchBonusTime = tag.getByte("WrenchBonus");
		this.redeploy = tag.getBoolean("Redeploy");
		this.ticksNoOwner = tag.getByte("Ownerless");
		this.engMade = tag.getBoolean("EngMade");
		this.fromPDA = tag.getBoolean("FromPDA");
		this.charge.deserializeNBT(tag.getCompound("Charge"));
		this.energy.receiveEnergy(tag.getInt("Energy"), false);
		this.disposableID = tag.getByte("DisposableID");
		if (tag.getByte("Sapper") != 0)
			this.setSapped(this, ItemStack.EMPTY);
		if (tag.hasUUID("OwnerE"))
			this.ownerEntityID = tag.getUUID("OwnerE");
		if (tag.hasUUID("Owner")) {
			UUID ownerID = tag.getUUID("Owner");
			this.entityData.set(OWNER_UUID, Optional.of(ownerID));
			this.ownerName = tag.getString("OwnerName");
			this.getOwner();
			this.setPersistenceRequired();
		}
		if (owner != null) {
			this.setEntTeam(TF2Util.getTeamForDisplay(owner));
		}
	}

	public int getBuildingID() {
		return 0;
	}

	public float getCollHeight() {
		return 1f;
	}

	public float getCollWidth() {
		return 0.95f;
	}

	public boolean canUseWrench() {
		return this.canUseWrenchImportant() || this.getLevel() < this.getMaxLevel();
	}

	public boolean canUseWrenchImportant() {
		return this.getMaxHealth() > this.getHealth();
	}

	@Override
	public boolean canBeHitWithPotion() {
		return false;
	}

	@Override
	protected float updateDistance(float p_110146_1_, float p_110146_2_) {
		this.yBodyRot = this.getYRot();
		return p_110146_2_;
	}

	@Override
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
		LivingEntity attacker = this.getAttackingEntity();
		if (this.fromPDA || (TF2Util.isAlliedTo(attacker, this) && this.getOwnerUUID() == null))
			return;
		if (!(this.getOwner() instanceof EntityEngineer && ((EntityEngineer) this.getOwner()).buildCount >= 3)) {
			int count = this.getOwner() instanceof Player ? this.getIronDrop()
					: Mth.ceil(this.getIronDrop() / 2);
			for (int i = 0; i < count; i++)
				this.dropItem(Items.IRON_INGOT, 1);
		}
	}

	@Override
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
		super.dropEquipment(wasRecentlyHit, lootingModifier);
		this.spawnAtLocation(this.charge.getStackInSlot(0), 0);
	}

	public int getIronDrop() {
		return 1 + this.getLevel();
	}

	@Override
	protected boolean canDespawn() {
		return this.getOwnerUUID() == null && (this.getOwner() == null || !this.getOwner().isAlive());
	}

	@OnlyIn(Dist.CLIENT)
	public void renderGUI(BufferBuilder renderer, Tessellator tessellator, Player player, int width, int height,
			GuiComponent gui) {

	}

	public int getGuiHeight() {
		return 48;
	}

	public int getConstructionTime() {
		return 21000;
	}

	public int getConstructionRate() {
		int i = 50;
		if (this.wrenchBonusTime > 0)
			i += 75 * this.wrenchBonusMult;
		if (this.redeploy)
			i += 100;
		if (TF2ConfigVars.fastBuildEngineer && this.getOwner() != null && this.getOwner() instanceof EntityEngineer)
			i += 125;
		// System.out.println("Constr: "+i);
		return i;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	@Override
	public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(Capability<T> capability, @javax.annotation.Nullable Direction facing) {
		T value = this.getCapabilityLegacy(capability, facing);
		return value == null ? net.minecraftforge.common.util.LazyOptional.empty() : net.minecraftforge.common.util.LazyOptional.of(() -> value);
	}

	public <T> T getCapabilityLegacy(Capability<T> capability, Direction facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return (T) this.energy;
		}
		return super.getCapability(capability, facing).orElse(null);
	}

	public boolean hasCapabilityLegacy(Capability<?> capability, Direction facing) {
		return capability == CapabilityEnergy.ENERGY || super.getCapability(capability, facing).isPresent();
	}

	public static int getCost(int building, ItemStack wrench) {
		if (building == 0)
			return TF2Attribute.getModifier("Weapon Mode", wrench, 0f, null) == 2 ? EntityBuilding.SENTRY_MINI_COST
					: EntityBuilding.SENTRY_COST;
		if (building == 1)
			return EntityBuilding.DISPENSER_COST;
		else if (building == 4)
			return EntityBuilding.SENTRY_DISPOSABLE_COST;
		else
			return (int) (EntityBuilding.TELEPORTER_COST
					/ TF2Attribute.getModifier("Teleporter Cost", wrench, 1f, null));
	}

	@Override
	public boolean hasHead() {
		return false;
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
		return true;
	}

	@Override
	public boolean isBackStabbable(LivingEntity attacker, ItemStack knife) {
		return false;
	}

	public int getDisposableID() {
		return disposableID;
	}

	public void setDisposableID(int disposableID) {
		this.disposableID = disposableID;
	}

}
