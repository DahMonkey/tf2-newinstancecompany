package rafradek.TF2weapons.entity.projectile;

import net.minecraft.world.phys.BlockHitResult;
import rafradek.TF2weapons.util.TF2HitResult;
import net.minecraft.core.particles.DustParticleOptions;
import rafradek.TF2weapons.TF2weapons;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityOnyx extends EntityProjectileBase {

	public EntityOnyx(Level p_i1756_1_) {
		this(TF2weapons.ONYX, p_i1756_1_);
	}

	public EntityOnyx(EntityType<? extends EntityOnyx> type, Level p_i1756_1_) {
		super(type, p_i1756_1_);
	}

	@Override
	public int getMaxTime() {
		return 8;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.tickCount >= this.getMaxTime()) {
			this.explode(this.getX(), this.getY(), this.getZ(), null, 1f);
		}
	}

	@Override
	public void spawnParticles(double x, double y, double z) {
		this.level.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.38, 0.08, 0.7);
	}

	@Override
	protected float getSpeed() {
		return 2.5f;
	}

	@Override
	public float getExplosionSize() {
		return 1.7f;
	}

	@Override
	public double getGravity() {
		return 0;
	}

	@Override
	public void onHitGround(int x, int y, int z, HitResult mop) {
		this.explode(mop.getLocation().x + ((BlockHitResult) mop).getDirection().getStepX() * 0.05,
				mop.getLocation().y + ((BlockHitResult) mop).getDirection().getStepY() * 0.05,
				mop.getLocation().z + ((BlockHitResult) mop).getDirection().getStepZ() * 0.05, null, 1f);
	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		this.explode(mop.getLocation().x, mop.getLocation().y, mop.getLocation().z, TF2HitResult.getEntity(mop), 1f);
	}
}
