package rafradek.TF2weapons.entity.mercenary;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemSniperRifle;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.util.TF2Class;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class EntitySniper extends EntityTF2Character {

	public EntitySniper(Level par1World) {
		this(TF2weapons.SNIPER, par1World);
	}

	public EntitySniper(EntityType<? extends EntitySniper> type, Level par1World) {
		super(type, par1World);
		// this.ammoLeft = 18;
		this.xpReward = 15;
		this.rotation = 3;
		if (this.attack != null) {
			attack.setRange(50);
			attack.releaseAt = 1;
		}

	}

	/*
	 * protected void addWeapons() {
	 * this.loadout[0]=ItemFromData.getNewStack("sniperrifle");
	 * this.loadout[1]=ItemFromData.getNewStack("sniperrifle"); }
	 */
	@Override
	protected ResourceLocation getLootTable() {
		return TF2weapons.lootSniper;
	}

	@Override
	public float[] getDropChance() {
		return new float[] { 0.045f, 0.12f, 0.11f };
	}

	public static AttributeSupplier.Builder createAttributes() {
		return EntityTF2Character.createAttributes()
				.add(Attributes.FOLLOW_RANGE, 60.0D)
				.add(Attributes.MAX_HEALTH, 12.5D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.1D)
				.add(Attributes.MOVEMENT_SPEED, 0.1329D)
				.add(Attributes.ATTACK_DAMAGE, 6.0D);
	}

	@Override
	protected void addWeapons() {
		super.addWeapons();
		if (this.isRobot())
			TF2Attribute.setAttribute(this.loadout.getStackInSlot(0), MapList.nameToAttribute.get("NoHeadshot"), 1f);
	}

	@Override
	public void aiStep() {
		if (!this.level.isClientSide && this.getMainHandItem().getItem() instanceof ItemSniperRifle
				&& (this.getTarget() == null || !this.getTarget().isAlive())
				&& this.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).isCharging())
			((ItemSniperRifle) this.getItemInHand(InteractionHand.MAIN_HAND).getItem())
					.disableZoom(this.getItemInHand(InteractionHand.MAIN_HAND), this);
		if (this.getItemInHand(InteractionHand.MAIN_HAND) != null
				&& !this.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).isCharging()) {
			this.ignoreFrustumCheck = false;
			this.rotation = 15;
		} else {
			this.ignoreFrustumCheck = true;
			this.rotation = 3;
		}

		if (this.level.isClientSide && this.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).isCharging()
				&& this.getAttribute(Attributes.MOVEMENT_SPEED)
						.getModifier(ItemSniperRifle.slowdownUUID) == null) {
			this.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).chargeTicks = 0;
			this.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).setCharging(false);
		}

		if (this.getTarget() != null && this.loadout.getStackInSlot(1).getItem() instanceof ItemWeapon) {

			if (this.usedSlot == 0 && this.getTarget().distanceToSqr(this) < 42) {
				this.switchSlot(1);
			} else if (this.usedSlot == 1 && this.getTarget().distanceToSqr(this) > 42) {
				this.switchSlot(0);
			}
		}

		// System.out.println("state:
		// "+this.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getBoolean("Zoomed")+"
		// "+this.world.isRemote);
		// System.out.println(this.getAttackTarget()!=null&&!this.getAttackTarget().isEntityAlive());
		/*
		 * if(this.getAttackTarget()!=null&&this.getEntitySenses().canSee(this.
		 * getAttackTarget())&&){
		 * ((ItemRangedWeapon)this.getHeldItem(EnumHand.MAIN_HAND).getItem()).
		 * altUse(getHeldItem(EnumHand.MAIN_HAND), this, worldObj); } else
		 * if(this.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getBoolean(
		 * "Zoomed")){
		 * ((ItemRangedWeapon)this.getHeldItem(EnumHand.MAIN_HAND).getItem()).
		 * altUse(getHeldItem(EnumHand.MAIN_HAND), this, worldObj); }
		 */
		super.aiStep();
		if (this.getLastHurtByMobTimestamp() == this.tickCount - 1) {
			this.lastRotation[0] += 4;
		}

	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_SNIPER_SAY;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TF2Sounds.MOB_SNIPER_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SNIPER_DEATH;
	}

	/**
	 * Plays step sound at given x, y, z for the entity
	 */

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int p_70628_2_, boolean p_70628_1_) {
		if (this.random.nextFloat() < 0.15f + p_70628_2_ * 0.075f)
			this.spawnAtLocation(ItemFromData.getNewStack("smg"), 0);
		if (this.random.nextFloat() < 0.05f + p_70628_2_ * 0.025f)
			this.spawnAtLocation(ItemFromData.getNewStack("sniperrifle"), 0);
	}

	@Override
	public float getAttributeModifier(String attribute) {
		return super.getAttributeModifier(attribute);
	}

	@Override
	public float getMotionSensitivity() {
		return this.scaleWithDifficulty(0.75f, 0.28f);
	}

	@Override
	public TF2Class getTF2Class() {
		return TF2Class.SNIPER;
	}

	@Override
	public boolean canBecomeGiant() {
		return false;
	}
}
