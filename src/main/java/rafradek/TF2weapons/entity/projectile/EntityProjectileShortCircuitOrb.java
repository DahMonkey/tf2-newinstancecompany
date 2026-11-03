package rafradek.TF2weapons.entity.projectile;

import rafradek.TF2weapons.TF2weapons;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.PropertyType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityProjectileShortCircuitOrb extends EntityProjectileSimple {

	double struck;

	public EntityProjectileShortCircuitOrb(Level world) {
		this(TF2weapons.SHORT_CIRCUIT_ORB, world);
	}

	public EntityProjectileShortCircuitOrb(EntityType<? extends EntityProjectileShortCircuitOrb> type, Level world) {
		super(type, world);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		super.initProjectile(shooter, hand, weapon);
		this.setProjType(5);
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean canPenetrate() {
		return true;
	}

	@Override
	public double getGravity() {
		return 0;
	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		super.onHitMob(entityHit, mop);
		if (entityHit instanceof EntityProjectileBase && ((EntityProjectileBase) entityHit).isPushable()) {
			entityHit.discard();
			this.playSound(ItemFromData.getSound(usedWeapon, PropertyType.SPECIAL_1_SOUND), 1.0f, 1.0f);
		}
	}

	@Override
	public void onHitGround(int x, int y, int z, HitResult mop) {
		if (this.struck == 0)
			super.onHitGround(x, y, z, mop);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.hitEntities.size() > 0) {
			this.hitEntities.clear();
		}
	}
}
