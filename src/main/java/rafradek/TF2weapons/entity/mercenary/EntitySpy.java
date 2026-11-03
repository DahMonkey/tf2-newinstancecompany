package rafradek.TF2weapons.entity.mercenary;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.entity.ai.EntityAIAmbush;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.item.ItemCloak;
import rafradek.TF2weapons.item.ItemDisguiseKit;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemMeleeWeapon;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;

public class EntitySpy extends EntityTF2Character {

	public int weaponCounter;
	public int cloakCounter;

	public float prevHealth;
	public boolean isAmbushing;

	public EntitySpy(Level p_i1738_1_) {
		this(TF2weapons.SPY, p_i1738_1_);
	}

	public EntitySpy(EntityType<? extends EntitySpy> type, Level p_i1738_1_) {
		super(type, p_i1738_1_);
		this.xpReward = 15;
		this.rotation = 20;
		this.goalSelector.addGoal(2,
				new AvoidEntityGoal<LivingEntity>(this, LivingEntity.class,
						entity -> (entity == this.getTarget()
								&& TF2Util.lookingAtFast(entity, 105, this.getX(), this.getY(), this.getZ())),
						4, 1.0f, 1.0f, entity -> true));
		this.goalSelector.addGoal(3, new EntityAIAmbush(this));
		this.goalSelector.removeGoal(avoidSentry);
		if (this.attack != null) {
			attack.setRange(30f);
			this.goalSelector.addGoal(4, this.attack);
		}
		this.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks = 20;
		if (!this.level.isClientSide) {

			this.getWepCapability().setDisguised(true);
			this.getWepCapability().setDisguiseType(
					"M:" + TF2weapons.animalsDisguise.get(this.getRandom().nextInt(TF2weapons.animalsDisguise.size())));
		}
	}

	@Override
	public float[] getDropChance() {
		return new float[] { 0.1f, 0.7f, 0.08f, 0.05f };
	}

	@Override
	protected ResourceLocation getDefaultLootTable() {
		return TF2weapons.lootSpy;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 80;
	}

	public boolean hasWatch() {
		return this.loadout.getStackInSlot(3).getItem() instanceof ItemCloak;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level.isClientSide) {
			if (this.tickCount == 0 && this.isRobot() && this.hasWatch()) {
				((ItemCloak) this.loadout.getStackInSlot(3).getItem()).setCloak(true, this.loadout.getStackInSlot(3),
						this, this.level);
				this.getWepCapability().setInvisible(this.hasWatch());
			}
			this.prevHealth = this.getHealth();

			this.cloakCounter--;
			LivingEntity target = this.getTarget();
			if (target == null && !this.getWepCapability().isDisguised()) {
				ItemDisguiseKit.startDisguise(this, this.level, "M:"
						+ TF2weapons.animalsDisguise.get(this.getRandom().nextInt(TF2weapons.animalsDisguise.size())));
			}
			if (this.hasWatch()) {
				this.loadout.getStackInSlot(3).getItem().inventoryTick(this.loadout.getStackInSlot(3), this.level, this, 0,
						true);
				/*
				 * if (target != null &&
				 * this.loadout.getStackInSlot(3).getTagCompound().getBoolean("Active")) {
				 * boolean useKnife = false; if ((this.getRevengeTarget() != null &&
				 * this.ticksExisted - this.getLastHurtByMobTimestamp() < 45) || (useKnife =
				 * (this.getDistanceSq(target) < (TF2Util.lookingAtFast(target, 105, this.posX,
				 * this.posY, this.posZ) ? 6 : 300)))) {
				 * 
				 * ((ItemCloak) this.loadout.getStackInSlot(3).getItem()).setCloak(
				 * !this.getWepCapability().isInvisible(), this.loadout.getStackInSlot(3), this,
				 * this.world); if (useKnife) { this.cloakCounter = 36; } else this.cloakCounter
				 * = 20 + (int) ((16 - this.getDistance(target)) * 10); }
				 * 
				 * }
				 * 
				 * //System.out.println(this.loadout.getStackInSlot(3)); if (target != null &&
				 * this.loadout.getStackInSlot(3).getItemDamage() <
				 * this.loadout.getStackInSlot(3).getMaxDamage() - 72 && this.cloakCounter <= 0
				 * && !this.loadout.getStackInSlot(3).getTagCompound().getBoolean("Active"))
				 * ((ItemCloak) this.loadout.getStackInSlot(3).getItem()).setCloak(true,
				 * this.loadout.getStackInSlot(3), this, this.world);
				 */
			}
			this.weaponCounter--;
			if (this.weaponCounter <= 0 && this.getTarget() != null && (this.isAmbushing || this.distanceToSqr(
					this.getTarget()) < (TF2Util.lookingAtFast(target, 105, this.getX(), this.getY(), this.getZ()) ? 4
							: 600))) {
				this.setCombatTask(false);
				this.weaponCounter = 8;
			} else if (this.weaponCounter <= 0 && this.getMainHandItem() == this.loadout.getStackInSlot(2)) {
				this.setCombatTask(true);
				this.weaponCounter = 3;
			}
			if (this.getTarget() != null && this.getTarget() instanceof EntityBuilding
					&& ((EntityBuilding) this.getTarget()).isSapped()
					&& ((EntityBuilding) this.getTarget()).getOwner() != null)
				this.setTarget(((EntityBuilding) this.getTarget()).getOwner());
		}
	}

	@Override
	public int[] getValidSlots() {
		return new int[] { 0, 1, 2, 3 };
	}

	@Override
	protected void addWeapons() {
		super.addWeapons();
		this.loadout.setStackInSlot(1, ItemFromData.getRandomWeaponOfSlotMob(getTF2Class(), 1, this.random, true,
				this.getStockWeight(1), this.noEquipment));
		this.loadout.getStackInSlot(1).setCount(64);
	}

	@Override
	public net.minecraft.network.chat.Component getName() {
		if (this.level.isClientSide && ClientProxy.getLocalPlayer() != null && this.getWepCapability().isDisguised()) {
			String username = this.getWepCapability().getDisguiseType().substring(2);

			if (TF2Util.isAlliedTo(ClientProxy.getLocalPlayer(), this)) {
				return net.minecraft.network.chat.Component.literal(super.getName().getString() + " [" + username + "]");
			} else {
				if (this.getWepCapability().getDisguiseType().startsWith("M:")) {
					net.minecraft.world.entity.EntityType<?> type = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
							.getValue(new ResourceLocation(username));
					return type != null ? type.getDescription() : net.minecraft.network.chat.Component.literal(username);
				} else
					return PlayerTeam.formatNameForTeam(
							rafradek.TF2weapons.client.ClientProxy.getClientLevel().getScoreboard().getPlayersTeam(username),
							net.minecraft.network.chat.Component.literal(username));
			}
		} else
			return super.getName();
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_SPY_SAY;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TF2Sounds.MOB_SPY_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SPY_DEATH;
	}

	/**
	 * Plays step sound at given x, y, z for the entity
	 */
	public void setCombatTask(boolean ranged) {
		this.ranged = ranged;
		if (ranged) {

			this.switchSlot(0);
		} else if (this.getTarget() instanceof EntityBuilding) {
			this.switchSlot(1);
		} else {
			this.switchSlot(2);
		}
	}

	@Override
	public float getAttributeModifier(String attribute) {
		float base = super.getAttributeModifier(attribute);
		if (attribute.equals("Damage") && this.getMainHandItem().getItem() instanceof ItemMeleeWeapon)
			base *= 0.9f;
		return base;
	}

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int p_70628_2_, boolean p_70628_1_) {
		if (this.random.nextFloat() < 0.085f + p_70628_2_ * 0.05f)
			this.spawnAtLocation(ItemFromData.getNewStack("revolver"), 0);
		if (this.random.nextFloat() < 0.06f + p_70628_2_ * 0.025f)
			this.spawnAtLocation(ItemFromData.getNewStack("butterflyknife"), 0);
		if (this.random.nextFloat() < 0.05f + p_70628_2_ * 0.025f)
			this.spawnAtLocation(ItemFromData.getNewStack("cloak"), 0);
		if (this.random.nextFloat() < 0.05f + p_70628_2_ * 0.025f)
			this.spawnAtLocation(new ItemStack(TF2weapons.itemDisguiseKit), 0);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return EntityTF2Character.createAttributes()
				.add(Attributes.FOLLOW_RANGE, 50.0D)
				.add(Attributes.MAX_HEALTH, 12.5D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.1D)
				.add(Attributes.MOVEMENT_SPEED, 0.14111D)
				.add(Attributes.ATTACK_DAMAGE, 4.0D);
	}

	@Override
	public TF2Class getTF2Class() {
		return TF2Class.SPY;
	}

	@Override
	public boolean canBecomeGiant() {
		return false;
	}

	@Override
	public boolean isValidTarget(LivingEntity living) {
		return super.isValidTarget(living)
				&& !(living instanceof EntityBuilding && ((EntityBuilding) living).isSapped());
	}
}
