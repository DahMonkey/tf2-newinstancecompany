package rafradek.TF2weapons.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import rafradek.TF2weapons.client.PyrolandRenderer;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemProjectileWeapon;

public class EntityFlameEffect extends LegacyParticle {

	protected EntityFlameEffect(Level world, double x, double y, double z, double motionX, double motionY,
			double motionZ, int time) {
		super(world, x, y, z);
		this.xd = motionX;
		this.yd = motionY;
		this.zd = motionZ;
		this.gravity = 0;
		this.friction = 1;
		this.lifetime = time;
		if (PyrolandRenderer.INSTANCE.shouldRenderPyrovision() && this.random.nextInt(5) > 1) {
			this.setParticleTextureIndex(65);
			this.rCol = this.random.nextFloat();
			this.gCol = this.random.nextFloat();
			this.bCol = this.random.nextFloat();
		} else {
			this.rCol = this.gCol = this.bCol = 1.0F;
			this.setParticleTextureIndex(48);
		}
	}

	@Override
	public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
		float f6 = (this.age + partialTicks) / this.lifetime;
		this.quadSize = 0.1F * (1.0F + f6 * f6 * 5.5F);
		super.render(buffer, camera, partialTicks);
	}

	@Override
	public void tick() {
		super.tick();
		this.alpha *= 0.9f;
	}

	public static EntityFlameEffect createNewEffect(Level world, LivingEntity living, float step, boolean heater) {
		double posX, posY, posZ, motionX, motionY, motionZ;
		float speed;
		int time;
		if (!heater) {
			Vec3 look = living.getLookAngle();
			Vec3 tangent = new Vec3(-look.z, 0, look.x);
			Vec3 pos = living.position().add(tangent.x * 0.3, living.getEyeHeight(), tangent.z * 0.3)
					.add(look.scale(0.5));
			posX = pos.x;
			posY = pos.y;
			posZ = pos.z;
			motionX = -Mth.sin(living.yHeadRot / 180.0F * (float) Math.PI)
					* Mth.cos(living.getXRot() / 180.0F * (float) Math.PI);
			motionZ = Mth.cos(living.yHeadRot / 180.0F * (float) Math.PI)
					* Mth.cos(living.getXRot() / 180.0F * (float) Math.PI);
			motionY = -Mth.sin(living.getXRot() / 180.0F * (float) Math.PI);
			speed = ((ItemProjectileWeapon) living.getMainHandItem().getItem())
					.getProjectileSpeed(living.getMainHandItem(), living);
			time = Math.round(3 + TF2Attribute.getModifier("Flame Range", living.getMainHandItem(), 2, null));
		} else {
			posX = living.getX();
			posY = living.getY() + 0.1;
			posZ = living.getZ();
			float angle = living.getRandom().nextFloat() * 2 * (float) Math.PI;
			motionX = -Mth.sin(angle) * Mth.cos(2 / 180.0F * (float) Math.PI);
			motionZ = Mth.cos(angle) * Mth.cos(2 / 180.0F * (float) Math.PI);
			motionY = -Mth.sin(2 / 180.0F * (float) Math.PI);
			speed = 0.9f;
			time = 5;
		}
		double f2 = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
		double vx = (motionX / f2 + world.random.nextGaussian() * 0.045D) * speed;
		double vy = (motionY / f2 + world.random.nextGaussian() * 0.045D) * speed;
		double vz = (motionZ / f2 + world.random.nextGaussian() * 0.045D) * speed;
		if (!heater) {
			vx += living.getDeltaMovement().x;
			vy += living.getDeltaMovement().y;
			vz += living.getDeltaMovement().z;
		}
		return new EntityFlameEffect(world, posX + motionX * step, posY + motionY * step, posZ + motionZ * step, vx, vy,
				vz, time);
	}
}
