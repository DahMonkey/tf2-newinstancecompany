package rafradek.TF2weapons.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityCritEffect extends Particle {

	public EntityCritEffect(Level world, double p_i46352_2_, double p_i46352_4_, double p_i46352_6_, int color) {
		super(world, p_i46352_2_, p_i46352_4_, p_i46352_6_);
		this.setParticleTextureIndex(65);
		this.setPos(this.getX() + this.random.nextFloat() * 0.15f - 0.075f,
				this.getY() + this.random.nextFloat() * 0.15f - 0.075f, this.getZ() + this.random.nextFloat() * 0.15f - 0.075f);
		this.quadSize *= this.random.nextFloat() * 0.5F + 0.5F;
		this.setColor((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y + (0.03), this.getDeltaMovement().z);
		// this.particleAlpha = Math.min(1 / this.particleScale * 3, 1);
		this.lifetime = 20;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.quadSize -= 0.05F;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBrightnessForRender(float p_70070_1_) {
		return 15728880;
	}

	public float getBrightness(float p_70013_1_) {
		return 1.0F;
	}
}
