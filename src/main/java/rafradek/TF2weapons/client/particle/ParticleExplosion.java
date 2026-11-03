package rafradek.TF2weapons.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rafradek.TF2weapons.client.compat.IParticleFactory;
import rafradek.TF2weapons.util.TF2Util;

/** Invisible emitter spawning vanilla explosion puffs in a growing sphere. */
public class ParticleExplosion extends LegacyParticle {

	private int timeSinceStart;
	private final int maximumTime = 5;
	private final float size;

	public ParticleExplosion(Level world, double startX, double startY, double startZ, double x, double y, double z) {
		super(world, startX, startY, startZ, 0, 0, 0);
		this.size = (float) (x - 0.5);
		this.setParticleTextureIndex(0);
	}

	@Override
	public void tick() {
		++this.timeSinceStart;
		float scale = ((float) this.timeSinceStart / (float) this.maximumTime) * this.size;
		float count = 0.33f * scale * this.size * scale;
		for (int i = 0; i < count; ++i) {
			Vec3 pos = TF2Util.rangeRandom3D(scale, this.random);
			this.level.addParticle(ParticleTypes.EXPLOSION, this.x + pos.x, this.y + pos.y, this.z + pos.z,
					(float) this.timeSinceStart / (float) this.maximumTime, 0.0D, 0.0D);
		}
		if (this.timeSinceStart >= this.maximumTime)
			this.remove();
	}

	@Override
	public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
	}

	public static class Factory implements IParticleFactory {
		@Override
		public Particle createParticle(int particleID, Level world, double x, double y, double z, double xSpeed,
				double ySpeed, double zSpeed, int... params) {
			return new ParticleExplosion(world, x, y, z, xSpeed, ySpeed, zSpeed);
		}
	}
}
