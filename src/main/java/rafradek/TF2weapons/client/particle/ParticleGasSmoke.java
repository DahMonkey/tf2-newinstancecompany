package rafradek.TF2weapons.client.particle;

import net.minecraft.world.level.Level;

public class ParticleGasSmoke extends LegacyParticle {

	public ParticleGasSmoke(Level world, double x, double y, double z, int color) {
		super(world, x, y, z);
		this.setColor((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
		this.quadSize = (this.random.nextFloat() * this.random.nextFloat() * 6.0F + 1.0F) * 0.1f;
		this.lifetime = (int) (16.0D / (this.random.nextFloat() * 0.8D + 0.2D)) + 2;
		this.fullBright = false;
		this.setParticleTextureIndex(7);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isAlive())
			this.setParticleTextureIndex(Math.max(0, 7 - this.age * 8 / this.lifetime));
	}
}
