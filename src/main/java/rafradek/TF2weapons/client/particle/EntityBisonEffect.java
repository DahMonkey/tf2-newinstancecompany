package rafradek.TF2weapons.client.particle;

import rafradek.TF2weapons.client.TF2EventsClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityBisonEffect extends Particle {

	public EntityBisonEffect(Level world, double p_i46352_2_, double p_i46352_4_, double p_i46352_6_, int color) {
		super(world, p_i46352_2_, p_i46352_4_, p_i46352_6_);
		setParticleTexture(TF2EventsClient.bisonIcon);
		setPosition(getX(),
				getY(), getZ());
		quadSize *= random.nextFloat() * 0.1F + 0.3F;
		setColor((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
		//particleAlpha = Math.min(1 / particleScale * 3, 1);
		lifetime = 15;
	}

	@Override
	public void tick() {
		super.tick();
		if (age < 5) quadSize += 0.2F;
		else quadSize -=0.12F;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBrightnessForRender(float p_70070_1_) {
		return 15728880;
	}

	public float getBrightness(float p_70013_1_) {
		return 1.0F;
	}
	
	public int getFXLayer()
    {
        return 1;
    }
}
