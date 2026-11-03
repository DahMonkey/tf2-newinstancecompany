package rafradek.TF2weapons.client.particle;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityBubbleEffect extends LegacyParticle {

	protected EntityBubbleEffect(Level world, double x, double y, double z, double motionX, double motionY,
			double motionZ, int time) {
		super(world, x, y, z);
		this.xd = motionX;
		this.yd = motionY;
		this.zd = motionZ;
		this.rCol = this.gCol = this.bCol = 1.0F;
		this.quadSize = 0.5f;
		this.lifetime = 20;
		this.gravity = 0;
		this.setParticleTextureIndex(32);
	}

	@Override
	public void tick() {
		super.tick();
		this.alpha *= 0.9f;
	}

	public static EntityBubbleEffect createNewEffect(Level world, Vec3 pos, Vec3 motion) {
		return new EntityBubbleEffect(world, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z, 5);
	}
}
