package rafradek.TF2weapons.entity.projectile;

import rafradek.TF2weapons.TF2weapons;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2ConfigVars;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

public class EntityFlare extends EntityProjectileSimple {

	public EntityFlare(Level world) {
		this(TF2weapons.FLARE, world);
	}

	public EntityFlare(EntityType<? extends EntityFlare> type, Level world) {
		super(type, world);
	}

	@Override
	public double getGravity() {
		return 0.019f;
	}

	@Override
	public void spawnParticles(double x, double y, double z) {
		if (this.isInWater())
			this.level.addParticle(ParticleTypes.WATER_BUBBLE, x, y, z, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
		else
			this.level.addParticle(ParticleTypes.SMOKE_NORMAL, x, y, z, 0, 0, 0);
	}

	@Override
	public int getBrightnessForRender() {
		return 15728880;
	}

	@Override
	public void addDamageTypes(DamageSource source) {
		source.setIsFire();
	}

	@Override
	public boolean isBurning() {
		return true;
	}

	@Override
	public float getExplosionSize() {
		return 2.1f;
	}
}
