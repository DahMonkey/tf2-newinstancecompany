package rafradek.TF2weapons.entity.projectile;

import rafradek.TF2weapons.TF2weapons;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2ConfigVars;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityFuryFireball extends EntityProjectileSimple {

	public EntityFuryFireball(Level world) {
		this(TF2weapons.FURY_FIREBALL, world);
	}

	public EntityFuryFireball(EntityType<? extends EntityFuryFireball> type, Level world) {
		super(type, world);
	}

	@Override
	public void spawnParticles(double x, double y, double z) {
		if (this.isInWater())
			this.level.addParticle(ParticleTypes.BUBBLE, x, y, z, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
		else
			this.level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
	}

	@Override
	public void addDamageTypes(DamageSource source) {
		source.setIsFire();
	}

		public int getBrightnessForRender() {
		return 15728880;
	}

	@Override
	public boolean isOnFire() {
		return true;
	}

	@Override
	public int getMaxTime() {
		return 4;
	}

	@Override
	public float getCollisionSize() {
		return 0.4f;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRender(double x, double y, double z) {
		double d0 = this.getX() - x;
		double d1 = this.getY() - y;
		double d2 = this.getZ() - z;
		double d3 = d0 * d0 + d1 * d1 + d2 * d2;
		return this.shouldRenderAtSqrDistance(d3);
	}
}
