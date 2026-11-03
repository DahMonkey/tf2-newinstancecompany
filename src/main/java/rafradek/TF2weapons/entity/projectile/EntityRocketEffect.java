package rafradek.TF2weapons.entity.projectile;

import net.minecraft.world.level.Level;
import rafradek.TF2weapons.client.particle.LegacyParticle;

/** Rocket exhaust spark that follows its rocket. Client only. */
public class EntityRocketEffect extends LegacyParticle {
	public EntityRocket rocket;

	public EntityRocketEffect(Level world, EntityRocket rocket) {
		super(world, rocket.getX(), rocket.getY(), rocket.getZ());
		this.rocket = rocket;
		this.quadSize = 0.2f;
		this.lifetime = 200;
		this.gravity = 0;
		this.setParticleTextureIndex(163);
		this.alpha = 0.75f;
		this.setColor(1f, 0.5f, 0f);
	}

	@Override
	public void tick() {
		if (this.rocket.isRemoved()) {
			this.remove();
			return;
		}
		this.setPos(this.rocket.xo - this.rocket.getDeltaMovement().x * 0.5f,
				this.rocket.yo - this.rocket.getDeltaMovement().y * 0.5f,
				this.rocket.zo - this.rocket.getDeltaMovement().z * 0.5f);
		this.xd = this.yd = this.zd = 0;
		super.tick();
	}
}
