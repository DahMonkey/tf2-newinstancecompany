package rafradek.TF2weapons.entity.projectile;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityProjectileEnergy extends EntityProjectileSimple {

	double struck;

	public EntityProjectileEnergy(Level world) {
		super(world);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		super.initProjectile(shooter, hand, weapon);
		this.setType(4);

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
		this.hitEntities.clear();
		if (this.struck == 0) {
			this.struck = new Vec3(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z).length();
		}

	}

	@Override
	public void onHitGround(int x, int y, int z, HitResult mop) {
		if (this.struck == 0)
			super.onHitGround(x, y, z, mop);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.hitEntities.size() > 0) {
			this.hitEntities.clear();
		} else if (this.struck != 0) {
			this.struck = 0;
		}
	}
}
