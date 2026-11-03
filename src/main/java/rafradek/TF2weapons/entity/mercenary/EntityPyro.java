package rafradek.TF2weapons.entity.mercenary;

import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability.RageType;
import rafradek.TF2weapons.entity.ai.EntityAIAirblast;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntityPyro extends EntityTF2Character {

	public EntityPyro(Level par1World) {
		this(TF2weapons.PYRO, par1World);
	}

	public EntityPyro(EntityType<? extends EntityPyro> type, Level par1World) {
		super(type, par1World);
		this.goalSelector.addGoal(3, new EntityAIAirblast(this));
		if (this.attack != null) {
			this.moveAttack.setDodge(true, true);
			this.attack.dodgeSpeed = 1.2f;
			this.attack.setRange(6.2865F);
			this.attack.projSpeed = 1.2570f;
		}
		this.rotation = 16;
		// this.ammoLeft = 250;
		this.xpReward = 15;
		// ((PathNavigateGround)this.getNavigator()).set(true);
		// this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
		// ItemUsable.getNewStack("Minigun"));

	}

	@Override
	protected ResourceLocation getLootTable() {
		return TF2weapons.lootPyro;
	}

	@Override
	public float[] getDropChance() {
		return new float[] { 0.062f, 0.12f, 0.11f };
	}

	@Override
	protected void addWeapons() {
		super.addWeapons();

		if (this.isGiant()) {
			TF2Attribute.setAttribute(this.loadout.getStackInSlot(1), MapList.nameToAttribute.get("FireRateBonus"),
					0.42f);
		}

		if (ItemFromData.getData(this.getItemBySlot(EquipmentSlot.HEAD)).getName().equals("head_prize")
				&& this.random.nextInt(3) == 0)
			this.setCustomNameTag("Pywwo OwO");
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source == DamageSource.ON_FIRE)
			return false;
		return super.hurt(source, amount);
	}

	@Override
	public void setFire(int time) {
		super.setSecondsOnFire(1);
	}

	/*
	 * protected void addWeapons() {
	 * this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
	 * ItemFromData.getNewStack("flamethrower")); }
	 */
	@Override
	public void aiStep() {

		if (!this.level.isClientSide && this.getTarget() != null) {
			if (this.getDiff() > 1) {
				boolean isFlareGun = this.loadout.getStackInSlot(1).getItem() instanceof ItemProjectileWeapon;
				if ((isFlareGun
						&& (this.isRobot() || (this.usedSlot == 0 && this.distanceToSqr(this.getTarget()) > 64)))
						|| (!isFlareGun && this.getTarget().hasEffect(MobEffects.FIRE_RESISTANCE))) {
					// System.out.println("Shotgun switch");
					this.switchSlot(1);
					// this.ammoLeft++;
				} else if (this.usedSlot == 1 && this.distanceToSqr(this.getTarget()) < 44) {
					this.switchSlot(0);
				}
			}
			ItemStack weapon = this.getMainHandItem();
			if (weapon.getItem() instanceof ItemFlameThrower
					&& ((ItemFromData) weapon.getItem()).getMaxRage(weapon, this) > 0
					&& ((ItemFromData) weapon.getItem()).getRage(weapon, this) >= ((ItemFromData) weapon.getItem())
							.getMaxRage(weapon, this)) {
				this.addEffect(new MobEffectInstance(TF2weapons.stun, 40, 1));
				this.addEffect(new MobEffectInstance(TF2weapons.noKnockback, 40, 0));
				TF2Util.addAndSendEffect(this, new MobEffectInstance(TF2weapons.uber, 40, 0));
				this.getWepCapability().setRageActive(RageType.PHLOG, true, 2f);
				this.playSound(ItemFromData.getSound(weapon, PropertyType.CHARGE_SOUND), this.getSoundVolume(),
						this.getSoundPitch());
			}

			ItemStack backpack = ItemBackpack.getBackpack(this);
			if (!this.level.isClientSide && backpack.getItem() instanceof ItemJetpack) {
				if (this.distanceToSqr(this.getTarget()) > 120
						&& ((ItemJetpack) backpack.getItem()).canActivate(backpack, this)) {
					((ItemJetpack) backpack.getItem()).activateJetpack(backpack, this, true);

				}
			}
		}
		super.aiStep();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(35.0D);
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(17.5D);
		this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.15D);
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.1329D);
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}

	/*
	 * public void onLivingUpdate() { super.onLivingUpdate();
	 * if(this.ammoLeft>0&&this.getAttackTarget()!=null&&this.
	 * getDistanceSq(this.getAttackTarget())<=400&&(!TF2ActionHandler.
	 * playerAction.get(this.world.isRemote).containsKey(this)||(
	 * TF2ActionHandler.playerAction.get(this.world.isRemote).get(this)&3)==0 )){
	 * TF2ActionHandler.playerAction.get(this.world.isRemote).put(this,
	 * TF2ActionHandler.playerAction.get(this.world.isRemote).containsKey(
	 * this)?TF2ActionHandler.playerAction.get(this.world.isRemote).get(this) +2:2);
	 * } }
	 */
	@Override
	protected SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_PYRO_SAY;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TF2Sounds.MOB_PYRO_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_PYRO_DEATH;
	}

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
		if (this.random.nextFloat() < 0.15f + p_70628_2_ * 0.075f)
			this.spawnAtLocation(ItemFromData.getNewStack("shotgun"), 0);
		if (this.random.nextFloat() < 0.05f + p_70628_2_ * 0.025f)
			this.spawnAtLocation(ItemFromData.getNewStack("flamethrower"), 0);
	}

	@Override
	public float getAttributeModifier(String attribute) {
		if (shouldScaleAttributes())
			if (attribute.equals("Damage"))
				return 0.93f;
		return super.getAttributeModifier(attribute);
	}

	/*
	 * @Override public float getAttributeModifier(String attribute) {
	 * if(attribute.equals("Minigun Spinup")){ return
	 * super.getAttributeModifier(attribute)*1.5f; } return
	 * super.getAttributeModifier(attribute); }
	 */
	@Override
	public TF2Class getTF2Class() {
		return TF2Class.PYRO;
	}

}
