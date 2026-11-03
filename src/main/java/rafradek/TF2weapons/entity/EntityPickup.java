package rafradek.TF2weapons.entity;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class EntityPickup extends Entity {

	public int age = 0;
	public int lifespan = 1200;
	public boolean rematerialize;
	private int rematerializeTime = 0;
	public float hoverStart;
	private static final EntityDataAccessor<Boolean> DISABLED = SynchedEntityData.<Boolean>createKey(EntityPickup.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Byte> TYPE = SynchedEntityData.<Byte>createKey(EntityPickup.class,
			EntityDataSerializers.BYTE);

	public enum Type {
		AMMO_SMALL(0, 0.2f, TF2Util.metaStack(TF2weapons.itemPickup, 1, 0), true),
		AMMO_NORMAL(0, 0.5f, TF2Util.metaStack(TF2weapons.itemPickup, 1, 1), true),
		AMMO_BIG(0, 1f, TF2Util.metaStack(TF2weapons.itemPickup, 1, 2), true),
		HEALTH_SMALL(0.205f, 0f, TF2Util.metaStack(TF2weapons.itemPickup, 1, 3), true),
		HEALTH_MEDIUM(0.5f, 0f, TF2Util.metaStack(TF2weapons.itemPickup, 1, 4), true),
		HEALTH_BIG(1f, 0f, TF2Util.metaStack(TF2weapons.itemPickup, 1, 5), true),
		SANDVICH(0.5f, 0f, TF2Util.metaStack(TF2weapons.itemSandvich, 1, 2), false);

		float healthRegen;
		float ammoRegen;
		public ItemStack model;
		public boolean visible;

		Type(float healthRegen, float ammoRegen, ItemStack model, boolean visible) {
			this.healthRegen = healthRegen;
			this.ammoRegen = ammoRegen;
			this.model = model;
			this.visible = visible;
		}
	}

	public EntityPickup(Level world, Type type, boolean rematerialize) {
		this(world);
		this.setType(type);
		this.rematerialize = rematerialize;
	}

	public EntityPickup(Level world) {
		super(world);
		this.setSize(0.25f, 0.25f);
		this.hoverStart = (float) (Math.random() * Math.PI * 2.0D);
	}

	public void setCollected() {
		this.entityData.set(DISABLED, true);
		this.rematerializeTime = 300;
		if (!this.rematerialize)
			this.discard();
	}

	@Override
	public void onCollideWithPlayer(Player entityIn) {
		if (!this.level.isClientSide && !this.isDisabled()) {
			if (this.getType().healthRegen > 0 && entityIn.getHealth() < entityIn.getMaxHealth()) {
				entityIn.heal(this.getType().healthRegen * entityIn.getMaxHealth());
				this.setCollected();
			}
			if (this.getType().ammoRegen > 0) {
				if (TF2Util.restoreAmmoToWeapons(entityIn, this.getType().ammoRegen))
					this.setCollected();
			}
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!this.level.isClientSide && this.isDisabled() && --this.rematerializeTime <= 0) {
			this.entityData.set(DISABLED, false);
		}

		this.xo = this.getX();
		this.yo = this.getY();
		this.zo = this.getZ();
		double d0 = this.getDeltaMovement().x;
		double d1 = this.getDeltaMovement().y;
		double d2 = this.getDeltaMovement().z;

		if (!this.hasNoGravity()) {
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y - (0.03999999910593033D), this.getDeltaMovement().z);
		}

		if (this.level.isClientSide) {
			this.noPhysics = false;
		} else {
			this.noPhysics = this.pushOutOfBlocks(this.getX(),
					(this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
		}

		this.move(MoverType.SELF, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
		boolean flag = (int) this.xo != (int) this.getX() || (int) this.yo != (int) this.getY()
				|| (int) this.zo != (int) this.getZ();

		if (flag || this.tickCount % 25 == 0) {
			if (this.level.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA) {
				this.setDeltaMovement(this.getDeltaMovement().x, (0.20000000298023224D), this.getDeltaMovement().z);
				this.setDeltaMovement(((this.random.nextFloat() - this.random.nextFloat()) * 0.2F), this.getDeltaMovement().y, this.getDeltaMovement().z);
				this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F));
				this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
			}
		}

		float f = 0.98F;

		if (this.onGround) {
			BlockPos underPos = new BlockPos(Mth.floor(this.getX()),
					Mth.floor(this.getBoundingBox().minY) - 1, Mth.floor(this.getZ()));
			BlockState underState = this.level.getBlockState(underPos);
			f = underState.getBlock().getSlipperiness(underState, this.level, underPos, this) * 0.98F;
		}

		this.setDeltaMovement(this.getDeltaMovement().x * (f), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * (0.9800000190734863D), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z * (f));

		if (this.onGround) {
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * (-0.5D), this.getDeltaMovement().z);
		}

		if (this.age != -32768) {
			++this.age;
		}

		this.handleWaterMovement();

		if (!this.level.isClientSide) {
			double d3 = this.getDeltaMovement().x - d0;
			double d4 = this.getDeltaMovement().y - d1;
			double d5 = this.getDeltaMovement().z - d2;
			double d6 = d3 * d3 + d4 * d4 + d5 * d5;

			if (d6 > 0.01D) {
				this.hasImpulse = true;
			}
			if (this.age > this.lifespan && !this.rematerialize)
				this.discard();
		}

	}

	public void setType(Type type) {
		this.entityData.set(TYPE, (byte) type.ordinal());
	}

	public Type getType() {
		return Type.values()[this.entityData.get(TYPE)];
	}

	public boolean isDisabled() {
		return this.entityData.get(DISABLED);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DISABLED, false);
		this.entityData.define(TYPE, (byte) 0);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		this.setType(Type.values()[compound.getByte("Type")]);
		this.age = compound.getInt("Age");
		this.lifespan = compound.getInt("Lifespan");
		this.rematerialize = compound.getBoolean("Rematerialize");
		this.entityData.set(DISABLED, compound.getBoolean("Disabled"));
		this.rematerializeTime = compound.getInt("RematerializeTime");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.putByte("Type", (byte) this.getType().ordinal());
		compound.putInt("Age", this.age);
		compound.putInt("Lifespan", this.lifespan);
		compound.putInt("RematerializeTime", this.rematerializeTime);
		compound.putBoolean("Rematerialize", this.rematerialize);
		compound.putBoolean("Disabled", this.isDisabled());
	}

}
