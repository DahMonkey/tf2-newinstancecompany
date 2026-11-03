package rafradek.TF2weapons.client.particle;

import rafradek.TF2weapons.client.compat.BufferBuilder;

import rafradek.TF2weapons.client.TF2EventsClient;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityBulletTracer extends Particle {

	private float duration;
	private boolean nextDead;
	private float length;

	private boolean motionless;

	public EntityBulletTracer(Level par1World, double startX, double startY, double startZ, double x, double y, double z, int speed, int color, float length) {
		super(par1World, startX, startY, startZ);
		quadSize = 0.2f;
		// special = special;
		double dist = new Vec3(startX, startY, startZ).distanceTo(new Vec3(x, y, z));
		if (speed > 0) {
			duration = (float) dist / speed;
			setDeltaMovement(((x - startX) / duration), getDeltaMovement().y, getDeltaMovement().z);
			setDeltaMovement(getDeltaMovement().x, ((y - startY) / duration), getDeltaMovement().z);
			setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y, ((z - startZ) / duration));
			this.length = length;
		} else {
			motionless = true;
			duration = length;
			this.length = (float) dist * 1000;
			setDeltaMovement(((x - startX) / dist * 0.001), getDeltaMovement().y, getDeltaMovement().z);
			setDeltaMovement(getDeltaMovement().x, ((y - startY) / dist * 0.001), getDeltaMovement().z);
			setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y, ((z - startZ) / dist * 0.001));
		}
		/*
		 * if (type == 1) { crits=2; motionX *= 0.001; motionY *= 0.001;
		 * motionZ *= 0.001; }
		 */
		lifetime = 200;
		setSize(0.025f, 0.025f);
		// setParticleIcon(Item.itemsList[2498+256].getIconFromDamage(0));
		setParticleTexture(TF2EventsClient.pelletIcon[0]);
		// setParticleTextureIndex(81);
		multipleParticleScaleBy(2);

		if (color == 0) setColor(0.97f, 0.76f, 0.51f);
		else setColor((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
	}

	public EntityBulletTracer(Level par1World, double startX, double startY, double startZ, double x, double y, double z, int duration, int crits, LivingEntity shooter, int type, float length) {
		this(par1World, startX, startY, startZ, x, y, z, duration, 0, length);
		if (crits != 2)
			setColor(0.97f, 0.76f, 0.51f);
		else {
			int color = TF2Util.getTeamColor(shooter);
			setColor(Mth.clamp((color >> 16) / 255f, 0.2f, 1f),
					Mth.clamp((color >> 8 & 255) / 255f, 0.2f, 1f),
					Mth.clamp((color & 255) / 255f, 0.2f, 1f));
		}
	}

	@Override
	public void tick() {
		if (nextDead)
			setExpired();
		nextDead = true;
		// setVelocity(0, 0, 0);
		super.tick();
		setDeltaMovement(getDeltaMovement().x * (1.025D), getDeltaMovement().y, getDeltaMovement().z);
		setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y * (1.025D), getDeltaMovement().z);
		setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z * (1.025D));
		if (duration > 0) {
			duration--;
			if (duration <= 0)
				setExpired();
		}
	}

	@Override
	public void renderParticle(BufferBuilder worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		float x = (float) (xo + (getX() - xo) * partialTicks - interpPosX);
		float y = (float) (yo + (getY() - yo) * partialTicks - interpPosY);
		float z = (float) (zo + (getZ() - zo) * partialTicks - interpPosZ);
		Vec3 rightVec = new Vec3(getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z)
				.cross(Minecraft.getInstance().getCameraEntity().getViewVector(1)).normalize();
		// System.out.println(rightVec);
		float f4 = 0.1F * quadSize;

		int i = getBrightnessForRender(partialTicks);
		int j = i >> 16 & 65535;
		int k = i & 65535;

		float xNext;
		float yNext;
		float zNext;

		if (motionless) {
			xNext = (float) (x + getDeltaMovement().x * length);
			yNext = (float) (y + getDeltaMovement().y * length);
			zNext = (float) (z + getDeltaMovement().z * length);
		} else {
			float length = 2 * this.length;
			if (duration < 1) length *= duration - (int) duration;
			xNext = (float) (x + getDeltaMovement().x * length);
			yNext = (float) (y + getDeltaMovement().y * length);
			zNext = (float) (z + getDeltaMovement().z * length);
		}

		float xMin = particleTexture.getMinU();
		float xMax = particleTexture.getMaxU();
		float yMin = particleTexture.getMinV();
		float yMax = particleTexture.getMaxV();

		worldRendererIn.pos(x - rightVec.x * f4, y - rightVec.y * f4, z - rightVec.z * f4).tex(xMax, yMax)
				.color(rCol, gCol, bCol, alpha).lightmap(j, k)
				.endVertex();
		;
		worldRendererIn.pos(x + rightVec.x * f4, y + rightVec.y * f4, z + rightVec.z * f4).tex(xMax, yMin)
				.color(rCol, gCol, bCol, alpha).lightmap(j, k)
				.endVertex();
		;
		worldRendererIn.pos(xNext + rightVec.x * f4, yNext + rightVec.y * f4, zNext + rightVec.z * f4).tex(xMin, yMin)
				.color(rCol, gCol, bCol, alpha).lightmap(j, k)
				.endVertex();
		;
		worldRendererIn.pos(xNext - rightVec.x * f4, yNext - rightVec.y * f4, zNext - rightVec.z * f4).tex(xMin, yMax)
				.color(rCol, gCol, bCol, alpha).lightmap(j, k)
				.endVertex();
		;

		/*
		 * worldRendererIn.pos((double)(x + rightVec.x * f4), (double)(y+rightVec.y*f4),
		 * (double)(z+rightVec.z*f4)).tex((double)xMax,
		 * (double)yMax).color(particleRed, particleGreen, particleBlue,
		 * particleAlpha).lightmap(j, k).endVertex();;
		 * worldRendererIn.pos((double)(x - rightVec.x * f4), (double)(y-rightVec.y*f4),
		 * (double)(z-rightVec.z*f4)).tex((double)xMax,
		 * (double)yMin).color(particleRed, particleGreen, particleBlue,
		 * particleAlpha).lightmap(j, k).endVertex();;
		 * worldRendererIn.pos((double)(xNext - rightVec.x * f4),
		 * (double)(yNext-rightVec.y*f4),
		 * (double)(zNext-rightVec.z*f4)).tex((double)xMin,
		 * (double)yMin).color(particleRed, particleGreen, particleBlue,
		 * particleAlpha).lightmap(j, k).endVertex();;
		 * worldRendererIn.pos((double)(xNext + rightVec.x * f4 ),
		 * (double)(yNext+rightVec.y*f4),
		 * (double)(zNext+rightVec.z*f4)).tex((double)xMin,
		 * (double)yMin).color(particleRed, particleGreen, particleBlue,
		 * particleAlpha).lightmap(j, k).endVertex();;
		 */
		// System.out.println("Rotation X: "+rotationX+" Rotation Z:
		// "+rotationZ+" Rotation YZ: "+rotationYZ+" Rotation XY: "+rotationXY+"
		// rotation XZ: "+rotationXZ);
		// super.renderParticle(worldRendererIn, entityIn, partialTicks,
		// rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	@Override
	public void move(double x, double y, double z) {
		setBoundingBox(getBoundingBox().move(x, y, z));
		resetPositionToBB();
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBrightnessForRender(float p_70070_1_) {
		return 15728880;
	}

	public float getBrightness(float p_70013_1_) {
		return 1.0F;
	}

	public static class Factory implements IParticleFactory {

		@Override
		public Particle createParticle(int particleID, Level world, double xCoordIn, double yCoordIn, double zCoordIn,
				double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
			return new EntityBulletTracer(world, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn,
					p_178902_15_[0], p_178902_15_[1], (p_178902_15_[2] / 64f));
		}

	}
}
