package rafradek.TF2weapons.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.Level;

public class ParticleGasSmoke extends Particle {

	public ParticleGasSmoke(Level world, double p_i46352_2_, double p_i46352_4_, double p_i46352_6_, int color) {
		super(world, p_i46352_2_, p_i46352_4_, p_i46352_6_);
		this.setPos(this.getX(), this.getY(), this.getZ());
		this.quadSize *= this.random.nextFloat() * 0.1F + 0.3F;
		this.setColor((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
		this.quadSize = this.random.nextFloat() * this.random.nextFloat() * 6.0F + 1.0F;
		this.lifetime = (int) (16.0D / (this.random.nextFloat() * 0.8D + 0.2D)) + 2;
		// this.particleAlpha = Math.min(1 / this.particleScale * 3, 1);
	}

	@Override
	public void tick() {
		super.tick();
		this.setParticleTextureIndex(7 - this.age * 8 / this.lifetime);
		if (this.age++ >= this.lifetime) {
			this.setExpired();
		}
	}

}
