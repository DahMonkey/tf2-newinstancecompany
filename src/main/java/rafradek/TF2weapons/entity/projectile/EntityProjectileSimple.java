package rafradek.TF2weapons.entity.projectile;

import net.minecraft.world.phys.BlockHitResult;
import rafradek.TF2weapons.util.TF2HitResult;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemSniperRifle;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityProjectileSimple extends EntityProjectileBase {

	float damage = -1;
	boolean impact = false;

	public EntityProjectileSimple(Level world) {
		this(TF2weapons.PROJECTILE_SIMPLE, world);
	}

	public EntityProjectileSimple(EntityType<? extends EntityProjectileSimple> type, Level world) {
		super(type, world);
		this.setSize(0.3F, 0.3F);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		String name = ItemFromData.getData(weapon).getString(PropertyType.PROJECTILE);
		if (name.equals("repairclaw"))
			this.setProjType(0);
		else if (name.equals("syringe"))
			this.setProjType(1);
		else if (name.equals("cleaver"))
			this.setProjType(2);
		else if (name.equals("arrow"))
			this.setProjType(3);
		else if (name.equals("pomson"))
			this.setProjType(4);
		else if (name.equals("hhhaxe"))
			this.setProjType(8);

		super.initProjectile(shooter, hand, weapon);
		this.setSize(0.3F, 0.3F);
		if (this.usedWeapon.getTag().getBoolean("ArrowLit")) {
			this.usedWeaponOrig.getTag().putBoolean("ArrowLit", false);
			this.setSecondsOnFire(1000);
		}
	}

	@Override
	public void onHitGround(int x, int y, int z, BlockHitResult mop) {
		if (!this.level.isClientSide) {
			this.impact = true;

			if (ItemFromData.getData(this.usedWeapon).hasProperty(PropertyType.HIT_SOUND)) {
				SoundEvent event = ItemFromData.getData(this.usedWeapon).hasProperty(PropertyType.HIT_WORLD_SOUND)
						? ItemFromData.getSound(this.usedWeapon, PropertyType.HIT_WORLD_SOUND)
						: ItemFromData.getSound(this.usedWeapon, PropertyType.HIT_SOUND);
				this.playSound(event, 1.3f, 1f);
			}

			if (TF2Attribute.getModifier("Destroy Block", this.usedWeapon, 0, shootingEntity) > 0) {

				float damage = this.damage;
				if (damage == -1) {
					damage = TF2Util.calculateDamage(TF2weapons.dummyEnt, this.level, this.shootingEntity,
							this.usedWeapon, this.getCritical(),
							(float) this.shootingEntity.position().distanceTo(mop.getLocation()));
					if (this.usedWeapon.getItem() instanceof ItemSniperRifle)
						damage *= 2.52f;
					damage *= TF2Attribute.getModifier("Destroy Block", this.usedWeapon, 0, this.shootingEntity);
				}
				this.damage = TF2Util.damageBlock(mop.getBlockPos(), this.shootingEntity, this.level, this.usedWeapon,
						this.getCritical(), damage, null, null);
				if (this.damage <= 0)
					this.discard();
			} else
				this.discard();
		}
	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		attackDirect(entityHit, 1, TF2HitResult.getHitInfo(mop) instanceof Boolean ? (Boolean) TF2HitResult.getHitInfo(mop) : false, mop.getLocation());
	}

	@Override
	public void spawnParticles(double x, double y, double z) {
		if (this.getProjType() == 4) {
			ClientProxy.spawnBisonParticle(this.level, x, y, z, TF2Util.getTeamColor(this.shootingEntity));
		}
	}

	@Override
	public boolean isPushable() {
		return this.getProjType() != 1;
	}

	@Override
	public double getGravity() {
		return this.getProjType() == 4 ? 0 : super.getGravity();
	}
}
