package rafradek.TF2weapons.entity.mercenary;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemCleaver;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.TF2Class;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class EntityScout extends EntityTF2Character {
	public boolean doubleJumped;
	private int jumpDelay;

	public int ballCooldown;

	public EntityScout(Level par1World) {
		this(TF2weapons.SCOUT, par1World);
	}

	public EntityScout(EntityType<? extends EntityScout> type, Level par1World) {
		super(type, par1World);
		if (this.attack != null) {
			this.moveAttack.setDodge(true, true);
			this.attack.jump = true;
			this.attack.jumprange = 40;
			this.attack.dodgeSpeed = 1.25f;
		}
		// this.ammoLeft = 24;
		this.xpReward = 15;

		// this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
		// ItemUsable.getNewStack("Minigun"));

	}

	/*
	 * protected void addWeapons() {
	 * this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
	 * ItemFromData.getNewStack("scattergun")); }
	 */
	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int lootingModifier, boolean wasRecentlyHit) {
		if (ItemFromData.getData(this.loadout.getStackInSlot(2)).getName().equals("sandmanball")) {
			this.loadout.setStackInSlot(2, ItemFromData.getNewStack("sandman"));
		}
		super.dropCustomDeathLoot(damageSource, lootingModifier, wasRecentlyHit);
	}

	@Override
	protected void addWeapons() {
		super.addWeapons();
		if (TF2Attribute.getModifier("Crit Stun", this.loadout.getStackInSlot(1), 0, this) != 0) {
			this.loadout.setStackInSlot(2, ItemFromData.getNewStack("sandman"));
		}
	}

	@Override
	protected ResourceLocation getLootTable() {
		return TF2weapons.lootScout;
	}

	@Override
	public float[] getDropChance() {
		return new float[] { 0.1f, 0.12f, 0.11f };
	}

	public static AttributeSupplier.Builder createAttributes() {
		return EntityTF2Character.createAttributes()
				.add(Attributes.FOLLOW_RANGE, 40.0D)
				.add(Attributes.MAX_HEALTH, 12.5D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.17638D)
				.add(Attributes.ATTACK_DAMAGE, 3.0D);
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (jumpDelay > 0 && --jumpDelay == 0 && (this.onGround || !this.doubleJumped))
			this.jump();
		if (this.onGround)
			this.doubleJumped = false;

		if (!this.level.isClientSide
				&& TF2Attribute.getModifier("Crit Stun", this.loadout.getStackInSlot(1), 0, this) != 0) {
			this.ballCooldown--;
			if (this.getTarget() == null
					|| this.getTarget().getEffect(TF2weapons.stun) == null) {
				this.switchSlot(2);
				// if(this.getAttackTarget() != null &&
				// this.getWepCapability().getPrimaryCooldown()<=0 &&
				// this.getEntitySenses().canSee(this.getAttackTarget())) {
				// ((ItemWeapon)this.getHeldItemMainhand().getItem()).altUse(getHeldItemMainhand(),
				// this, world);
				// this.getWepCapability().setPrimaryCooldown(1000);
				// }
			} else if (!this.isRobot()) {
				this.switchSlot(1);
				this.getMainHandItem().setCount(16);
			}
		} else if (!this.level.isClientSide)
			this.switchSlot(0);
	}

	@Override
	protected void jump() {
		super.jump();
		/*
		 * double speed=Math.sqrt(motionX*motionX+motionZ*motionZ); if(speed!=0){
		 *
		 * double speedMultiply=this.getEntityAttribute(SharedMonsterAttributes.
		 * MOVEMENT_SPEED).getAttributeValue()/speed; this.motionX*=speedMultiply;
		 * this.motionZ*=speedMultiply; }
		 */
		this.setDeltaMovement(((-Mth.sin(this.getYRot() / 180.0F * (float) Math.PI))), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, ((Mth.cos(this.getYRot() / 180.0F * (float) Math.PI))));
		float f2 = (float) ((float) Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z)
				* this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
		this.setDeltaMovement(this.getDeltaMovement().x * (f2), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z * (f2));
		this.fallDistance = -3.0F;
		if (!this.doubleJumped && this.jump) {
			this.doubleJumped = true;
			this.jumpDelay = 8;
		}
	}

	@Override
	public float getAttributeModifier(String attribute) {
		if (this.loadout.getStackInSlot(1).getItem() instanceof ItemCleaver && shouldScaleAttributes())
			if (attribute.equals("Fire Rate"))
				return this.scaleWithDifficulty(1.8f, 1f);
		return super.getAttributeModifier(attribute);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_SCOUT_SAY;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TF2Sounds.MOB_SCOUT_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SCOUT_DEATH;
	}

	/*
	 * public int getAttackStrength(Entity par1Entity) { ItemStack itemstack =
	 * this.getHeldItem(EnumHand.MAIN_HAND); float f = (float)(this.getMaxHealth() -
	 * this.getHealth()) / (float)this.getMaxHealth(); int i = 4 +
	 * MathHelper.floor_float(f * 4.0F);
	 *
	 * if (item!stack.isEmpty()) { i += itemstack.getDamageVsEntity(this); }
	 *
	 * return i; }
	 */

	/**
	 * Plays step sound at given x, y, z for the entity
	 */
	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int p_70628_2_, boolean p_70628_1_) {
		if (this.random.nextFloat() < 0.60f + p_70628_2_ * 0.30f)
			this.spawnAtLocation(ItemFromData.getNewStack("bonk"), 0);
		if (this.random.nextFloat() < 0.12f + p_70628_2_ * 0.075f)
			this.spawnAtLocation(ItemFromData.getNewStack("scattergun"), 0);
		if (this.random.nextFloat() < 0.15f + p_70628_2_ * 0.075f)
			this.spawnAtLocation(ItemFromData.getNewStack("pistol"), 0);
	}

	@Override
	public float getMotionSensitivity() {
		return this.scaleWithDifficulty(0.12f, 0.02f);
	}

	@Override
	public void onShot() {
		if (ItemFromData.getData(this.loadout.getStackInSlot(2)).getName().equals("sandmanball"))
			this.ballCooldown = this.getDiff() == 1 ? 340 : (this.getDiff() == 3 ? 160 : 240);
	}

	@Override
	public TF2Class getTF2Class() {
		return TF2Class.SCOUT;
	}

	@Override
	public int getState(boolean onTarget) {
		if (onTarget && this.usedSlot == 2) {
			return 3;
		} else
			return super.getState(onTarget);
	}

}
