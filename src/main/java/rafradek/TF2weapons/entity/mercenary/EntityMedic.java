package rafradek.TF2weapons.entity.mercenary;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.entity.ai.EntityAINearestChecked;
import rafradek.TF2weapons.entity.ai.EntityAIUseMedigun;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemMedigun;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityMedic extends EntityTF2Character {

	// public boolean melee;

	public EntityAIUseMedigun useMedigun = new EntityAIUseMedigun(this, 1.0F, 20.0F);

	public EntityMedic(Level par1World) {
		super(par1World);
		this.targetTasks.taskEntries.clear();
		this.targetSelector.addGoal(1,
				this.findplayer = new EntityAINearestChecked<LivingEntity>(this, LivingEntity.class, true,
						false,
						Predicates.and(this::isValidTarget,
								target -> (target.getHealth() < target.getMaxHealth()
										|| (target == this.getOwner() && this.getAbsorptionAmount() == 0)
										|| (target instanceof Player
												&& target.getCapability(TF2weapons.PLAYER_CAP).orElse(null).medicCall > 0))),
						false, true) {

					@Override
					public boolean shouldExecute() {
						return canHeal() && super.shouldExecute();
					}
				});
		this.targetSelector.addGoal(2, new EntityAINearestChecked<LivingEntity>(this, LivingEntity.class, true,
				false, this::isValidTarget, false, false) {
			@Override
			public boolean shouldExecute() {
				return canHeal() && super.shouldExecute();
			}
		});
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this, true));
		this.targetSelector.addGoal(4, new EntityAINearestChecked<>(this, LivingEntity.class, true, false,
				super::isValidTarget, true, false));
		// this.ammoLeft = 1;
		this.experienceValue = 15;
		this.rotation = 15;
		// this.tasks.removeTask(attack);
		this.stepHeight = 1f;
		if (par1World != null) {
			this.goalSelector.addGoal(3, useMedigun);
			this.friendly = true;
		}
		// this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
		// ItemUsable.getNewStack("Minigun"));

	}

	public boolean canHeal() {
		return this.loadout.getStackInSlot(1).getItem() instanceof ItemMedigun
				&& ((ItemMedigun) this.loadout.getStackInSlot(1).getItem())
						.isAmmoSufficient(this.loadout.getStackInSlot(1), this, true);
	}

	@Override
	public boolean isReloadPressed() {
		if (this.getMainHandItem().getItem() instanceof ItemMedigun
				&& ((ItemMedigun) this.getMainHandItem().getItem()).isShieldResist(this.getMainHandItem(),
						this)) {
			DamageSource source = null;
			if (this.getTarget() != null && this.getTarget().getLastDamageSource() != null) {
				source = this.getTarget().getLastDamageSource();
			} else {
				source = this.getLastDamageSource();
			}
			if (source != null) {
				int type = 0;
				if (source.isExplosion())
					type = 1;
				else if (source.isFireDamage())
					type = 2;
				if (type != this.getMainHandItem().getTag().getByte("ResType"))
					return true;
			}
		}
		return false;
	}

	@Override
	protected void addWeapons() {
		super.addWeapons();
		if (this.isGiant()) {
			if (this.getOwnerId() == null) {
				TF2Attribute.setAttribute(this.loadout.getStackInSlot(1), MapList.nameToAttribute.get("HealRateBonus"),
						10000f);
				TF2Attribute.setAttribute(this.loadout.getStackInSlot(1), MapList.nameToAttribute.get("OverHealBonus"),
						0f);
			} else {
				TF2Attribute.setAttribute(this.loadout.getStackInSlot(1), MapList.nameToAttribute.get("HealRateBonus"),
						2.25f);
				TF2Attribute.setAttribute(this.loadout.getStackInSlot(1), MapList.nameToAttribute.get("OverHealBonus"),
						0f);
			}
		}
	}

	@Override
	protected ResourceLocation getLootTable() {
		return TF2weapons.lootMedic;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(30.0D);
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(15D);
		this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.1D);
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.14111D);
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}

	@Override
	public void aiStep() {

		super.aiStep();
		if (this.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).getHealTarget() > 0)
			this.ignoreFrustumCheck = true;
		else
			this.ignoreFrustumCheck = false;
		if (!this.level.isClientSide) {
			AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
			if (this.tickCount % 4 == 0) {
				for (AttributeModifier modifier : playerAttributes) {
					speed.removeModifier(modifier);
				}
				playerAttributes.clear();
				if (this.getTarget() != null && this.friendly && this.getTarget() instanceof Player) {
					for (AttributeModifier modifier : this.getTarget()
							.getAttribute(Attributes.MOVEMENT_SPEED).getModifiers()) {
						if (modifier.getAmount() > 0) {
							TF2Util.addModifierSafe(this, Attributes.MOVEMENT_SPEED, modifier, true);
							this.playerAttributes.add(modifier);
						}
					}
					// System.out.println("modyfikatory: "+playerAttributes.size()+"
					// "+speed.getModifiers().size());
				}
			}
		}

	}

	@Override
	public void setTarget(LivingEntity entity) {
		this.alert = true;
		if (TF2Util.isAlliedTo(this, entity)) {
			// System.out.println("friendly");
			if (!friendly) {
				this.friendly = true;

			}
		} else if (entity != null && this.friendly) {
			// System.out.println("not friendly");
			this.friendly = false;
		}
		this.switchSlot(this.getDefaultSlot());
		// System.out.println("Attack Target Set: "+entity);
		super.setTarget(entity);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_MEDIC_SAY;
	}

	@Override
	public int getDefaultSlot() {
		return this.friendly ? 1 : 0;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TF2Sounds.MOB_MEDIC_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_MEDIC_DEATH;
	}

	/**
	 * Plays step sound at given x, y, z for the entity
	 */

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
		if (this.rand.nextFloat() < 0.15f + p_70628_2_ * 0.075f)
			this.entityDropItem(ItemFromData.getNewStack("syringegun"), 0);
		if (this.rand.nextFloat() < 0.08f + p_70628_2_ * 0.03f)
			this.entityDropItem(ItemFromData.getNewStack("medigun"), 0);
	}

	@Override
	public float getAttributeModifier(String attribute) {
		if (TF2ConfigVars.scaleAttributes
				&& !(this.getTarget() instanceof Player || (this.getTarget() instanceof OwnableEntity
						&& ((OwnableEntity) this.getTarget()).getOwnerId() != null))) {
			if (attribute.equals("Heal"))
				return this.scaleWithDifficulty(0.75f, 1f);
			if (attribute.equals("Overheal"))
				return this.scaleWithDifficulty(0.55f, 1f);
		}
		return super.getAttributeModifier(attribute);
	}

	@Override
	public float getMotionSensitivity() {
		return 0f;
	}

	@Override
	public boolean isValidTarget(LivingEntity target) {
		return !((target instanceof EntityMedic
				&& (((EntityTF2Character) target).isRobot() || target.getHealth() >= target.getMaxHealth()))
				|| target instanceof EntityBuilding) && TF2Util.isAlliedTo(EntityMedic.this, target);
	}

	@Override
	public TF2Class getTF2Class() {
		return TF2Class.MEDIC;
	}
}
