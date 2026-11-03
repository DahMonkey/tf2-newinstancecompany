package rafradek.TF2weapons.client.particle;

import rafradek.TF2weapons.client.compat.BufferBuilder;

import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ParticleExplosion extends Particle {

	private int timeSinceStart;
	/** the maximum time for the explosion */
	private final int maximumTime = 5;

	private float size = 4;

	public ParticleExplosion(Level par1World, double startX, double startY, double startZ, double x, double y,
			double z) {
		super(par1World, startX, startY, startZ, 0, 0, 0);
		this.size = (float) (x - 0.5);
	}

	@Override
	public void tick() {
		++this.timeSinceStart;
		float scale = ((float) timeSinceStart / (float) this.maximumTime) * size;
		float count = 0.33f * scale * size * scale;
		for (int i = 0; i < count; ++i) {
			Vec3 pos = TF2Util.rangeRandom3D(scale, this.random);
			double d0 = this.getX() + pos.x;
			double d1 = this.getY() + pos.y;
			double d2 = this.getZ() + pos.z;
			this.level.addParticle(ParticleTypes.EXPLOSION, d0, d1, d2,
					(float) this.timeSinceStart / (float) this.maximumTime, 0.0D, 0.0D);
		}

		if (this.timeSinceStart == this.maximumTime) {
			this.setExpired();
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {}

	@Override
	public int getFXLayer() {
		return 1;
	}

	public static class Factory implements IParticleFactory {

		@Override
		public Particle createParticle(int particleID, Level world, double xCoordIn, double yCoordIn, double zCoordIn,
				double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
			return new ParticleExplosion(world, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		}

	}
}
