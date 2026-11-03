package rafradek.TF2weapons.entity.mercenary;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.entity.ai.EntityAIStickybomb;
import rafradek.TF2weapons.entity.projectile.EntityStickybomb;
import rafradek.TF2weapons.item.ItemChargingTarge;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemStickyLauncher;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.WeaponData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.function.Predicate;

public class EntityDemoman extends EntityTF2Character {

	private static final EntityDataAccessor<Boolean> SENTRY_BUSTER = SynchedEntityData.createKey(EntityDemoman.class,
			EntityDataSerializers.BOOLEAN);

	public int chargeCool = 0;

	public LivingEntity target;
	public BlockPos targetpos;

	public EntityDemoman(Level par1World) {
		super(par1World);
		this.goalSelector.addGoal(5, new EntityAIStickybomb(this, 1, 10f));
		if (this.attack != null) {
			this.attack.setRange(20F);
			this.attack.projSpeed = 1.16205f;
			this.attack.gravity = 0.0381f;
			this.attack.releaseAt = 0.03f;
			this.moveAttack.setDodge(true, false);
		}
		this.rotation = 10;
		// this.ammoLeft = 20;
		this.experienceValue = 15;

		// this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
		// ItemUsable.getNewStack("Minigun"));

	}

	@Override
	protected void addWeapons() {
		super.addWeapons();
		if (this.rand.nextFloat() > 0.18f && !this.loadout.getStackInSlot(1).isEmpty()
				&& this.loadout.getStackInSlot(1).getItem() instanceof ItemChargingTarge) {
			ItemStack sword = ItemFromData.getRandomWeapon(this.rand,
					Predicates.and(ItemFromData.VISIBLE_WEAPON, (Predicate<WeaponData>) input -> !input.getBoolean(PropertyType.STOCK)
							&& ItemFromData.isItemOfClassSlot(input, 2, getTF2Class())));
			if (!sword.isEmpty())
				this.loadout.setStackInSlot(2, sword);
		}
		if (this.isGiant()) {
			TF2Attribute.setAttribute(this.loadout.getStackInSlot(0), MapList.nameToAttribute.get("FireRateBonus"),
					0.5f);
			TF2Attribute.setAttribute(this.loadout.getStackInSlot(0), MapList.nameToAttribute.get("ClipSizeBonus"), 4f);
			TF2Attribute.setAttribute(this.loadout.getStackInSlot(0), MapList.nameToAttribute.get("ReloadRateBonus"),
					0.25f);
			TF2Attribute.setAttribute(this.loadout.getStackInSlot(0), MapList.nameToAttribute.get("SpreadAdd"), 0.1f);
		}
	}
	/*
	 * protected ResourceLocation getLootTable() { return TF2weapons.lootDemoman; }
	 */
	/*
	 * protected void addWeapons() {
	 * this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
	 * ItemFromData.getNewStack("grenadelauncher")); }
	 */

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(SENTRY_BUSTER, false);
	}

	private void setBusterInternal() {
		this.entityData.set(SENTRY_BUSTER, true);
		this.targetTasks.taskEntries.clear();
		this.goalSelector.removeGoal(attack);
	}

	public void setBuster(LivingEntity target) {
		this.setBusterInternal();
		this.target = target;
	}

	public void setBuster(BlockPos target) {
		this.setBusterInternal();
		this.targetpos = target;
	}

	@Override
	public float[] getDropChance() {
		return new float[] { 0.065f, 0.065f, 0.11f };
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();

		this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(35.0D);
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(17.5D);
		this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.15D);
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.12347D);
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}

	@Override
	public SpawnGroupData onInitialSpawn(DifficultyInstance p_180482_1_, SpawnGroupData data) {
		data = super.onInitialSpawn(p_180482_1_, data);
		if (!this.loadout.getStackInSlot(1).isEmpty()
				&& this.loadout.getStackInSlot(1).getItem() instanceof ItemChargingTarge) {
			this.getAttribute(Attributes.MAX_HEALTH)
					.addTransientModifier(new AttributeModifier("ShieldHP", 3, 0));
			this.heal(3);
			this.getAttribute(Attributes.MOVEMENT_SPEED)
					.addTransientModifier(new AttributeModifier("ShieldMove", 0.02, 2));
		}
		return data;
	}

	@Override
	public int getDefaultSlot() {
		return this.loadout.getStackInSlot(1).getItem() instanceof ItemChargingTarge ? 2 : 0;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		this.chargeCool--;
		if (!this.level.isClientSide) {
			if (!this.getWepCapability().activeBomb.isEmpty()
					&& this.loadout.getStackInSlot(1).getItem() instanceof ItemStickyLauncher) {
				EntityStickybomb bomb = this.getWepCapability().activeBomb
						.get(this.rand.nextInt(this.getWepCapability().activeBomb.size()));
				for (LivingEntity target : this.level.getEntitiesOfClass(LivingEntity.class,
						bomb.getBoundingBox().inflate(5))) {
					if (this.isValidTarget(target) && target.distanceToSqr(bomb) < 7
							&& this.getEntitySenses().canSee(target) && target.hasLineOfSight(bomb)) {
						((ItemWeapon) this.loadout.getStackInSlot(1).getItem())
								.altFireTick(this.loadout.getStackInSlot(1), this, world);
						break;
					}
				}
			}
			boolean chargetick = this.tickCount % 4 == 0;
			if (chargetick && this.loadout.getStackInSlot(1).getItem() instanceof ItemChargingTarge) {
				this.setItemInHand(InteractionHand.OFF_HAND, this.loadout.getStackInSlot(1));
				this.switchSlot(2);
				if (this.getTarget() != null && this.getEntitySenses().canSee(this.getTarget())) {
					if (this.chargeCool < 0) {
						this.moveAttack.setDodge(false, false);
						this.chargeCool = 300;
						this.playSound(TF2Sounds.WEAPON_SHIELD_CHARGE, 2F, 1F);
						this.addEffect(new MobEffectInstance(TF2weapons.charging, 40));
					}
				}
			} else if (chargetick && this.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ItemChargingTarge) {
				this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
				this.switchSlot(this.getDefaultSlot());
			}
		}
	}

	@Override
	protected void onFinishedPotionEffect(MobEffectInstance effect) {
		super.onFinishedPotionEffect(effect);
		if (effect.getEffect() == TF2weapons.charging)
			this.moveAttack.setDodge(true, false);
	}

	@Override
	public void onEquipItem(int slot, ItemStack stack) {
		super.onEquipItem(slot, stack);
		this.attack.fireAtFeet = slot == 1 ? TF2Attribute.getModifier("Explosion Radius", stack, 1, this) : 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_DEMOMAN_SAY;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TF2Sounds.MOB_DEMOMAN_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_DEMOMAN_DEATH;
	}

	@Override
	protected ResourceLocation getLootTable() {
		return TF2weapons.lootDemoman;
	}

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
		if (this.rand.nextFloat() < 0.075f + p_70628_2_ * 0.0375f)
			this.entityDropItem(ItemFromData.getNewStack("grenadelauncher"), 0);
		if (this.rand.nextFloat() < 0.06f + p_70628_2_ * 0.03f)
			this.entityDropItem(ItemFromData.getNewStack("stickybomblauncher"), 0);
	}

	@Override
	public float getAttributeModifier(String attribute) {
		if (attribute.equals("Spread") && this.getMainHandItem() != null
				&& this.getMainHandItem().getItem() instanceof ItemStickyLauncher)
			return 3f;
		return super.getAttributeModifier(attribute);
	}

	@Override
	public TF2Class getTF2Class() {
		return TF2Class.DEMOMAN;
	}

	public void explode() {}

	/*
	 * @Override public float getAttributeModifier(String attribute) {
	 * if(attribute.equals("Minigun Spinup")){ return
	 * super.getAttributeModifier(attribute)*1.5f; } return
	 * super.getAttributeModifier(attribute); }
	 */
}
