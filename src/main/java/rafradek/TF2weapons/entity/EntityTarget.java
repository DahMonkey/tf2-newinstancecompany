package rafradek.TF2weapons.entity;

import rafradek.TF2weapons.TF2weapons;
import java.text.DecimalFormat;
import net.minecraft.core.NonNullList;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeHooks;

public class EntityTarget extends ArmorStand {

	public EntityTarget(Level world) {
		super(world);
		this.setSize(0.6F, 1.8625F);
	}

	public EntityTarget(Level world, double d, double d1, double e, boolean creative) {
		super(world, d, d1, e);
		this.setSize(0.6F, 1.8625F);
		this.creative = creative;
	}

	private static final EntityDataAccessor<Float> LAST_DAMAGE = SynchedEntityData.<Float>createKey(EntityTarget.class,
			EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DPS = SynchedEntityData.<Float>createKey(EntityTarget.class,
			EntityDataSerializers.FLOAT);

	public float[] dps = new float[3];

	public int startAttack = -1;
	public int lastAttack;
	public int deltaTime;
	public float total;

	public boolean creative;

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!this.level.isClientSide && this.tickCount % 5 == 0) {
			DecimalFormat format = new DecimalFormat("#.##");
			if (this.startAttack != -1 && this.tickCount - this.lastAttack >= 35) {
				this.startAttack = -1;
				this.total = 0;
			} else if (this.startAttack != 1) {

			}
			/*
			 * this.dps[2]=this.dps[1]; this.dps[1]=this.dps[0]; this.dps[0]=0f;
			 * DecimalFormat format = new DecimalFormat("#.##");
			 */
			this.setCustomNameTag("Last: " + format.format(this.getLastDamage()) + " DPS: "
					+ format.format(total * (20f / (this.lastAttack - this.startAttack + this.deltaTime))) + " Total: "
					+ format.format(total));
		}
	}

	@Override
	public void notifyDataManagerChange(EntityDataAccessor<?> key) {
		if (STATUS.equals(key)) {
			this.setSize(0.6F, 1.8625F);
		} else
			super.notifyDataManagerChange(key);
	}

	@Override
	public void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(LAST_DAMAGE, 0f);
		this.entityData.define(DPS, 0f);
	}

	@Override
	public void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!this.level.isClientSide && !this.isRemoved()) {
			if (DamageSource.OUT_OF_WORLD.equals(source) || (source.getTrueSource() instanceof LivingEntity
					&& source.getDamageType().equals("player") && source.getTrueSource().isShiftKeyDown())) {
				source.setExplosion();
				Block.spawnAsEntity(world, getPosition().up(), new ItemStack(TF2weapons.itemTarget));
				super.hurt(source, amount);
				return false;
			} else {
				if (this.invulnerableTime > this.maxHurtResistantTime / 2.0F) {
					if (amount <= this.lastDamage) {
						return false;
					}

					amount -= this.lastDamage;
					this.lastDamage = amount;
				} else {
					this.lastDamage = amount;
					this.invulnerableTime = this.maxHurtResistantTime;
					this.maxHurtTime = 10;
					this.hurtTime = this.maxHurtTime;
				}

				amount = ForgeHooks.onLivingDamage(this, source,
						this.applyPotionDamageCalculations(source,
								ArmorProperties.applyArmor(this, (NonNullList<ItemStack>) this.getArmorSlots(),
										source, ForgeHooks.onLivingHurt(this, source, amount))));
				this.entityData.set(LAST_DAMAGE, amount);
				this.dps[0] += amount;
				if (this.startAttack == -1) {
					this.startAttack = this.tickCount;
					this.total = 0;
				}
				this.deltaTime = this.tickCount - this.lastAttack;
				this.lastAttack = this.tickCount;

				this.total += amount;
				this.invulnerableTime = 10;
				DecimalFormat format = new DecimalFormat("#.##");
				this.setCustomNameTag("Last: " + format.format(this.getLastDamage()) + " DPS: "
						+ format.format(total * (20f / (this.lastAttack - this.startAttack + this.deltaTime)))
						+ " Total: " + format.format(total));
				for (ItemStack stack : this.getArmorSlots()) {
					stack.setDamageValue(0);
				}
				return this.creative;
			}
		} else {
			return false;
		}
	}

	@Override
	public float getEyeHeight() {
		return this.isBaby() ? this.getBbHeight() * 0.5F : 1.6825F;
	}

	@Override
	public boolean getAlwaysRenderNameTag() {
		return true;
	}

	public float getLastDamage() {
		return this.entityData.get(LAST_DAMAGE);
	}

	public float getDPS() {
		return this.entityData.get(DPS);
	}
}
