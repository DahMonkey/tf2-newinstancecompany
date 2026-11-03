package rafradek.TF2weapons.entity.mercenary;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability.RageType;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntitySoldier extends EntityTF2Character {

	public boolean rocketJump;
	public boolean rocketJumper;
	public boolean airborne;
	private boolean activateBackpack;

	public EntitySoldier(Level par1World) {
		super(par1World);
		// this.rotation=90;
		if (this.attack != null) {
			attack.setRange(35);
			attack.fireAtFeet = 1;
			attack.projSpeed = 1.04f;
			attack.explosive = true;
			moveAttack.setDodge(true, this.rocketJumper);
		}
		// this.ammoLeft = 20;
		this.xpReward = 15;
		// this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
		// ItemUsable.getNewStack("Minigun"));
		this.goalSelector.addGoal(5, new UseBackpack());
	}

	/*
	 * protected void addWeapons() {
	 * this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
	 * ItemFromData.getNewStack("rocketlauncher")); }
	 */
	@Override
	protected void addWeapons() {
		super.addWeapons();
		if (this.loadout.getStackInSlot(1).getItem() instanceof ItemBackpack) {
			this.getWepCapability().setRage(RageType.BANNER, 1f);
			if (this.isGiant())
				TF2Attribute.setAttribute(this.loadout.getStackInSlot(1),
						MapList.nameToAttribute.get("EffectDurationBonus"), 999f);
			// this.getCapability(TF2weapons.INVENTORY_CAP,
			// null).setInventorySlotContents(2, this.loadout.getStackInSlot(1));

		}
		if (this.isGiant()) {
			TF2Attribute.setAttribute(this.loadout.getStackInSlot(0), MapList.nameToAttribute.get("FireRateBonus"),
					0.5f);
			TF2Attribute.setAttribute(this.loadout.getStackInSlot(0), MapList.nameToAttribute.get("ClipSizeBonus"), 3f);
			TF2Attribute.setAttribute(this.loadout.getStackInSlot(0), MapList.nameToAttribute.get("ReloadRateBonus"),
					0.3f);
		}
	}

	@Override
	protected ResourceLocation getLootTable() {
		return TF2weapons.lootSoldier;
	}

	@Override
	public float[] getDropChance() {
		return new float[] { 0.05f, 0.12f, 0.11f };
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(50.0D);
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
		this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.25D);
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.10583D);
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}

	@Override
	public void setRobot(int robot) {
		super.setRobot(robot);
	}

	@Override
	public float getStockWeight(int slot) {
		return this.isRobot() ? 5.5f - Math.min(3.5f, this.robotStrength / 2f) : 2.7f;
	}

	@Override
	public void aiStep() {

		if (!this.level.isClientSide && this.getTarget() != null) {

			if (this.loadout.getStackInSlot(0).getItem() instanceof ItemWeapon) {
				if (this.usedSlot == 0 && (this.rocketJumper || this.hasBaseJumper()) && this.getHealth() > 7f
						&& !this.airborne && this.onGround
						&& ItemWeapon.getWeapon(this.getMainHandItem()).getClip(this.getMainHandItem()) == 0)
					this.rocketJump = true;
			}

			if (!this.isRobot() && this.getDiff() > 1
					&& this.loadout.getStackInSlot(1).getItem() instanceof ItemWeapon) {

				if (this.usedSlot == 0
						&& ItemWeapon.getWeapon(this.getMainHandItem()).getClip(this.getMainHandItem()) == 0
						&& ItemWeapon.getWeapon(this.loadout.getStackInSlot(1))
								.getClip(this.loadout.getStackInSlot(1)) > 0
						&& this.distanceToSqr(this.getTarget()) < 36) {
					// System.out.println("Shotgun switch");
					this.switchSlot(1);
				} else if (this.usedSlot == 1
						&& (ItemWeapon.getWeapon(this.getMainHandItem()).getClip(this.getMainHandItem()) == 0
								|| this.distanceToSqr(this.getTarget()) > 40)) {
					this.switchSlot(0);
				}
			}
		}

		if (!this.level.isClientSide && this.getUseItem().getItem() instanceof ItemHorn) {
			ItemStack backpack = ItemBackpack.getBackpack(this);
			if (backpack.getItem() instanceof ItemSoldierBackpack && (72000 - this.getUseItemRemainingTicks()) > ItemFromData
					.getData(backpack).getInt(PropertyType.FIRE_SPEED)) {
				this.stopActiveHand();
				this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
			}
		}

		if (!this.level.isClientSide && !this.onGround
				&& (this.fallDistance > 2 || (this.getWepCapability().isExpJump() && this.getDeltaMovement().y < 0))
				&& this.hasBaseJumper())
			ItemBackpack.getBackpack(this).getTag().putBoolean("Deployed", true);
		// if (!this.world.isRemote)
		// System.out.println(this.moveForward+ " "+this.moveStrafing);

		/*
		 * if(this.rocketJump&&this.getEntityData().getCompoundTag("TF2").
		 * getShort("reload")<=50){
		 * TF2ActionHandler.playerAction.get(this.world.isRemote).put(this, 1);
		 * this.jump=true; this.rotationYaw-= this.rotationPitch=8;
		 * this.getLookHelper().setLookPosition(this.posX,this.posY-1,this.posZ, 180,
		 * 90.0F); }
		 */
		/*
		 * if(this.rocketJump&&!this.onGround){ this.rocketJump=false;
		 * TF2ActionHandler.playerAction.get(this.world.isRemote).put(this, 0); }
		 */
		if (this.airborne)
			this.jump = false;
		super.aiStep();
		/*
		 * if(this.ammoLeft>0&&this.getAttackTarget()!=null&&this.
		 * getDistanceSq(this.getAttackTarget())<=400&&(!
		 * TF2ActionHandler.playerAction.get(this.world.isRemote).containsKey
		 * (this)||(TF2ActionHandler.playerAction.get(this.world.isRemote).
		 * get(this)&3)==0)){
		 * TF2ActionHandler.playerAction.get(this.world.isRemote).put(this,
		 * TF2ActionHandler.playerAction.get(this.world.isRemote).containsKey
		 * (this)?TF2ActionHandler.playerAction.get(this.world.isRemote).get(
		 * this)+2:2); }
		 */
	}

	public boolean hasBaseJumper() {
		return ItemBackpack.getBackpack(this).getItem() instanceof ItemParachute;
	}

	public void activateBackpack() {
		ItemStack backpack = ItemBackpack.getBackpack(this);
		if (this.getWepCapability().getRage(RageType.BANNER) >= 1f) {
			this.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(TF2weapons.itemHorn));
			this.startUsingItem(InteractionHand.OFF_HAND);
			if (TF2Util.getTeamForDisplay(this) == 1)
				this.playSound(ItemFromData.getSound(backpack, PropertyType.HORN_BLU_SOUND), 0.8f, 1f);
			else
				this.playSound(ItemFromData.getSound(backpack, PropertyType.HORN_RED_SOUND), 0.8f, 1f);
		}
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		super.fall(distance, this.airborne ? damageMultiplier * 0.35f : damageMultiplier);
		this.airborne = false;
	}

	@Override
	public void onShot() {
		if (this.rocketJump) {
			this.jump = true;
			this.yHeadRot = this.yHeadRot + 180;
			this.setXRot((50));
			this.rocketJump = false;
			this.airborne = true;
			// this.getLookHelper().setLookPosition(this.posX,this.posY-1,this.posZ,
			// 180, 90.0F);
		}
	}

	@Override
	public void onEquipItem(int slot, ItemStack stack) {
		super.onEquipItem(slot, stack);
		this.attack.fireAtFeet = slot == 0 ? TF2Attribute.getModifier("Explosion Radius", stack, 1, this) : 0;
		this.rocketJumper = !this.isRobot()
				&& (TF2Attribute.getModifier("Airborne Bonus", stack, 0, this) != 0 || this.random.nextBoolean());
	}

	@Override
	public TF2Class getTF2Class() {
		return TF2Class.SOLDIER;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_SOLDIER_SAY;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TF2Sounds.MOB_SOLDIER_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SOLDIER_DEATH;
	}

	/**
	 * Plays step sound at given x, y, z for the entity
	 */

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
		if (this.random.nextFloat() < 0.15f + p_70628_2_ * 0.075f)
			this.spawnAtLocation(ItemFromData.getNewStack("shotgun"), 0);
		if (this.random.nextFloat() < 0.05f + p_70628_2_ * 0.025f)
			this.spawnAtLocation(ItemFromData.getNewStack("rocketlauncher"), 0);
	}

	public class UseBackpack extends Goal {

		@Override
		public boolean shouldExecute() {
			if (getOwner() == null && getDiff() > 1 && (getTarget() != null || isGiant())
					&& activeItemStack.isEmpty()) {
				ItemStack backpack = ItemBackpack.getBackpack(EntitySoldier.this);
				return backpack.getItem() instanceof ItemSoldierBackpack
						&& getWepCapability().getRage(RageType.BANNER) >= 1f;
			}
			return false;
		}

		@Override
		public void updateTask() {
			activateBackpack();
		}
	}
}
