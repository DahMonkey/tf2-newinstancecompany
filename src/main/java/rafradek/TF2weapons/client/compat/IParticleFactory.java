package rafradek.TF2weapons.client.compat;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.Level;

/** 1.12 IParticleFactory: creates the mod's particles directly (they are not registered types). */
@FunctionalInterface
public interface IParticleFactory {
	Particle createParticle(int particleID, Level world, double x, double y, double z, double xSpeed, double ySpeed,
			double zSpeed, int... params);
}
