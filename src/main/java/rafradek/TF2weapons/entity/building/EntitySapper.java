package rafradek.TF2weapons.entity.building;

import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.item.ItemWrench;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntitySapper extends EntityBuilding {

	public EntityBuilding sappedBuilding;
	public ItemStack sapperItem;

	public EntitySapper(Level world) {
		super(world);
		this.setSize(1f, 1.1f);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level.isClientSide) {
			if (this.sappedBuilding == null || !this.sappedBuilding.isAlive()) {
				this.discard();
				return;
			}
			TF2Util.dealDamage(this.sappedBuilding, this.level, this.getOwner(), sapperItem, 0, 0.25f,
					TF2Util.causeBulletDamage(sapperItem, this.getOwner(), this));

			if (!this.isAlive() && this.sappedBuilding != null)
				this.sappedBuilding.sapper = null;
		}

	}

	@Override
	public SoundEvent getSoundNameForState(int state) {
		switch (state) {
		case 0:
			return TF2Sounds.MOB_SAPPER_IDLE;
		default:
			return null;
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if ((source instanceof TF2DamageSource) && !((TF2DamageSource) source).getWeapon().isEmpty()
				&& ((TF2DamageSource) source).getWeapon().getItem() instanceof ItemWrench)
			super.hurt(source, amount);
		return false;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(12D);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SAPPER_DEATH;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag par1NBTTagCompound) {
		super.addAdditionalSaveData(par1NBTTagCompound);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag par1NBTTagCompound) {
		super.readAdditionalSaveData(par1NBTTagCompound);
	}
}
