package rafradek.TF2weapons.entity.projectile;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityRocketEffect extends Particle {
	public EntityRocket rocket;
	private boolean nextDead;

	public EntityRocketEffect(Level par1World, EntityRocket rocket) {
		super(par1World, rocket.getX(), rocket.getY(), rocket.getZ());
		// this.noClip=true;
		this.rocket = rocket;
		this.quadSize = 1;
		this.lifetime = 200;
		this.setSize(0.1f, 0.1f);
		this.setParticleTextureIndex(163);
		this.alpha = 0.75f;
		// this.setParticleIcon(TF2EventBusListener.pelletIcon);
		// this.setParticleTextureIndex(81);
		this.multipleParticleScaleBy(2);
		this.setColor(1f, 0.5f, 0f);
		this.particleTextureJitterX = this.random.nextFloat();
		this.particleTextureJitterY = this.random.nextFloat();
	}

	@Override
	public void onUpdate() {
		if (rocket.isRemoved()) {
			this.setExpired();
			return;
		}
		this.setPos(rocket.xo - rocket.getDeltaMovement().x * 0.5f, rocket.yo - rocket.getDeltaMovement().y * 0.5f,
				rocket.zo - rocket.getDeltaMovement().z * 0.5f);
		this.setDeltaMovement((rocket.getDeltaMovement().x), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (rocket.getDeltaMovement().y), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (rocket.getDeltaMovement().z));
		super.onUpdate();
	}

	/*
	 * public int getFXLayer() { return 2; }
	 */

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBrightnessForRender(float p_70070_1_) {
		return 15728880;
	}

	public float getBrightness(float p_70013_1_) {
		return 1.0F;
	}
}
