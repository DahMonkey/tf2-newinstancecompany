package rafradek.TF2weapons.client.particle;

import net.minecraft.world.level.Level;

public class EntityCritEffect extends LegacyParticle {

	public EntityCritEffect(Level world, double x, double y, double z, int color) {
		super(world, x, y, z);
		this.setParticleTextureIndex(65);
		this.setPos(this.x + this.random.nextFloat() * 0.15f - 0.075f, this.y + this.random.nextFloat() * 0.15f - 0.075f,
				this.z + this.random.nextFloat() * 0.15f - 0.075f);
		this.quadSize *= this.random.nextFloat() * 0.5F + 0.5F;
		this.setColor((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
		this.yd += 0.03;
		this.lifetime = 20;
	}

	@Override
	public void tick() {
		super.tick();
		this.quadSize -= 0.05F;
		if (this.quadSize <= 0)
			this.remove();
	}
}
