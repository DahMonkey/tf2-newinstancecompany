package rafradek.TF2weapons.client.particle;

import rafradek.TF2weapons.client.PyrolandRenderer;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemProjectileWeapon;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityFlameEffect extends Particle {

	protected EntityFlameEffect(Level world, double p_i1209_2_, double p_i1209_4_, double p_i1209_6_, double motionX,
			double motionY, double motionZ, int time) {
		super(world, p_i1209_2_, p_i1209_4_, p_i1209_6_);
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		// this.noClip=false;
		this.particleMaxAge = time;
		if (PyrolandRenderer.INSTANCE.shouldRenderPyrovision() && rand.nextInt(5)>1) {
			this.setParticleTextureIndex(65);
			particleRed = rand.nextFloat();
			particleGreen = rand.nextFloat();
			particleBlue = rand.nextFloat();
		} else {
			this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
			this.setParticleTextureIndex(48);
		}
	}

	@Override
	public void renderParticle(BufferBuilder p_180434_1_, Entity p_180434_2_, float p_180434_3_, float p_180434_4_,
			float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_) {
		float f6 = (this.particleAge + p_180434_3_) / this.particleMaxAge;
		this.particleScale = 1.0F + f6 * f6 * 5.5F;
		super.renderParticle(p_180434_1_, p_180434_2_, p_180434_3_, p_180434_4_, p_180434_5_, p_180434_6_, p_180434_7_,
				p_180434_8_);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.particleAlpha *= 0.9f;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBrightnessForRender(float p_70070_1_) {
		return 15728880;
	}

	public static EntityFlameEffect createNewEffect(Level world, LivingEntity living, float step, boolean heater) {
		if (!heater) {
			Vec3 look = living.getLookAngle();
			Vec3 tangent = new Vec3(-look.z, 0, look.x);
			Vec3 pos = living.position().add(tangent.x*0.3, living.getEyeHeight(), tangent.z*0.3)
					.add(look.scale(0.5));
			double motionX = -Mth.sin(living.yHeadRot / 180.0F * (float) Math.PI)
					* Mth.cos(living.getXRot() / 180.0F * (float) Math.PI);
			double motionZ = Mth.cos(living.yHeadRot / 180.0F * (float) Math.PI)
					* Mth.cos(living.getXRot() / 180.0F * (float) Math.PI);
			double motionY = (-Mth.sin(living.getXRot() / 180.0F * (float) Math.PI));
			float f2 = Mth.sqrt((float)motionX * motionX + motionY * motionY + motionZ * motionZ);
			float speed = ((ItemProjectileWeapon) living.getMainHandItem().getItem())
					.getProjectileSpeed(living.getMainHandItem(), living);
			motionX = (motionX / f2 + world.rand.nextGaussian() * (world.rand.nextBoolean() ? -1 : 1) * 0.045D) * speed
					+ living.getDeltaMovement().x;
			motionY = (motionY / f2 + world.rand.nextGaussian() * (world.rand.nextBoolean() ? -1 : 1) * 0.045D) * speed
					+ living.getDeltaMovement().y;
			motionZ = (motionZ / f2 + world.rand.nextGaussian() * (world.rand.nextBoolean() ? -1 : 1) * 0.045D) * speed
					+ living.getDeltaMovement().z;

			return new EntityFlameEffect(world, pos.x + motionX * step, pos.y + motionY * step, pos.z + motionZ * step,
					motionX, motionY, motionZ,
					Math.round(3 + (TF2Attribute.getModifier("Flame Range", living.getMainHandItem(), 2, null))));
		} else {
			double posX = living.getX();
			double posY = living.getY() + 0.1;
			double posZ = living.getZ();
			float angle = living.getRandom().nextFloat() * 2 * (float) Math.PI;
			double motionX = -Mth.sin(angle) * Mth.cos(2 / 180.0F * (float) Math.PI);
			double motionZ = Mth.cos(angle) * Mth.cos(2 / 180.0F * (float) Math.PI);
			double motionY = (-Mth.sin(2 / 180.0F * (float) Math.PI));
			float f2 = Mth.sqrt((float)motionX * motionX + motionY * motionY + motionZ * motionZ);
			float speed = 0.9f;
			motionX = (motionX / f2 + world.rand.nextGaussian() * (world.rand.nextBoolean() ? -1 : 1) * 0.045D) * speed;
			motionY = (motionY / f2 + world.rand.nextGaussian() * (world.rand.nextBoolean() ? -1 : 1) * 0.045D) * speed;
			motionZ = (motionZ / f2 + world.rand.nextGaussian() * (world.rand.nextBoolean() ? -1 : 1) * 0.045D) * speed;

			return new EntityFlameEffect(world, posX + motionX * step, posY + motionY * step, posZ + motionZ * step,
					motionX, motionY, motionZ, 5);
		}
	}
}
