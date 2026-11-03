package rafradek.TF2weapons.client.particle;

import net.minecraft.world.level.Level;
import rafradek.TF2weapons.client.TF2EventsClient;

public class EntityBisonEffect extends LegacyParticle {

	public EntityBisonEffect(Level world, double x, double y, double z, int color) {
		super(world, x, y, z);
		this.setParticleTexture(TF2EventsClient.bisonIcon);
		this.xd = this.yd = this.zd = 0;
		this.quadSize *= this.random.nextFloat() * 0.1F + 0.3F;
		this.setColor((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
		this.lifetime = 15;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.age < 5)
			this.quadSize += 0.2F;
		else
			this.quadSize -= 0.12F;
	}
}
