package rafradek.TF2weapons.util;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("deprecation")
public class DamageSourceProjectile extends IndirectEntityDamageSource implements TF2DamageSource {
	public ItemStack weapon;
	public int critical;
	public boolean notProjectile;
	public boolean selfdmg;
	private int attackFlags;
	private float power = 1;

	public DamageSourceProjectile(ItemStack weapon, Entity projectile, Entity shooter) {
		super("bullet", projectile, shooter);
		this.weapon = weapon;
	}

	@Override
	public boolean isDifficultyScaled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see rafradek.TF2weapons.TF2DamageSource#getWeapon()
	 */
	@Override
	public ItemStack getWeapon() {
		return weapon;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see rafradek.TF2weapons.TF2DamageSource#getCritical()
	 */
	@Override
	public int getCritical() {
		return critical;
	}

	/*
	 * public DamageSource bypassesArmor() { this.setDamageBypassesArmor(); return
	 * this; }
	 */
	/**
	 * Returns the message to be displayed on player death.
	 */
	@Override
	public Component getDeathMessage(LivingEntity p_151519_1_) {
		// ItemStack itemstack = this.damageSourceEntity instanceof
		// EntityLivingBase ?
		// ((EntityLivingBase)this.damageSourceEntity).getHeldItem(EnumHand.MAIN_HAND)
		// : null;
		if (this.getEntity() == TF2weapons.dummyEnt)
			return Component.translatable("death.attack.explosion", p_151519_1_.getHoverName());
		String s = "death.attack." + this.msgId;
		String s1 = s + ".item";
		return weapon != null && TF2Util.canTranslate(s1)
				? Component.translatable(s1,
						new Object[] { p_151519_1_.getHoverName(), this.getEntity().getHoverName(),
								weapon.getHoverName() })
				: Component.translatable(s,
						new Object[] { p_151519_1_.getHoverName(), this.getEntity().getHoverName() });
	}

	/*
	 * public String getDeathMessage(EntityPlayer par1EntityPlayer) { return
	 * StatCollector.translateToLocalFormatted("death." + this.damageType, new
	 * Object[] {par1EntityPlayer.getDisplayName(),
	 * this.shooter.getCommandSenderName(),
	 * StatCollector.translateToLocal(this.weapon)}); }
	 */
	@Override
	public Vec3 getDamageLocation() {
		return this.isExplosion() ? null : this.damageSourceEntity.position();
	}

	public void removeProjecileStatus() {
		this.notProjectile = true;
	}

	@Override
	public boolean isProjectile() {
		return super.isProjectile() && !this.notProjectile;
	}

	@Override
	public Entity getTrueSource() {
		return selfdmg ? TF2weapons.dummyEnt : super.getEntity();
	}

	@Override
	public void setAttackSelf() {
		this.selfdmg = true;
	}

	@Override
	public ItemStack getWeaponOrig() {
		return this.getDirectEntity() instanceof EntityProjectileBase
				&& !((EntityProjectileBase) this.getDirectEntity()).usedWeaponOrig.isEmpty()
						? ((EntityProjectileBase) this.getDirectEntity()).usedWeaponOrig
						: this.getWeapon();
	}

	@Override
	public int getAttackFlags() {
		return this.attackFlags;
	}

	@Override
	public void addAttackFlag(int flag) {
		this.attackFlags += flag;
	}

	@Override
	public float getAttackPower() {
		return this.power;
	}

	@Override
	public void setAttackPower(float power) {
		this.power = power;
	}

	@Override
	public DamageSource setCritical(int crit) {
		this.critical = crit;
		return this;
	}

	/*
	 * @Override public void onShieldBlock(EntityLivingBase living) {}
	 */

}
