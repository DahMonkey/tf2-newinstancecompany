package rafradek.TF2weapons.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rafradek.TF2weapons.client.TF2EventsClient;
import rafradek.TF2weapons.client.compat.IParticleFactory;
import rafradek.TF2weapons.util.TF2Util;

/** Bullet tracer streak drawn along its direction of travel. */
public class EntityBulletTracer extends LegacyParticle {

	private float duration;
	private boolean nextDead;
	private final float length;
	private boolean motionless;

	public EntityBulletTracer(Level world, double startX, double startY, double startZ, double x, double y, double z,
			int speed, int color, float length) {
		super(world, startX, startY, startZ);
		this.quadSize = 0.2f;
		this.gravity = 0;
		this.hasPhysics = false;
		double dist = new Vec3(startX, startY, startZ).distanceTo(new Vec3(x, y, z));
		if (speed > 0) {
			this.duration = (float) dist / speed;
			this.xd = (x - startX) / this.duration;
			this.yd = (y - startY) / this.duration;
			this.zd = (z - startZ) / this.duration;
			this.length = length;
		} else {
			this.motionless = true;
			this.duration = length;
			this.length = (float) dist * 1000;
			this.xd = (x - startX) / dist * 0.001;
			this.yd = (y - startY) / dist * 0.001;
			this.zd = (z - startZ) / dist * 0.001;
		}
		this.lifetime = 200;
		this.setParticleTexture(TF2EventsClient.pelletIcon != null ? TF2EventsClient.pelletIcon[0] : null);
		this.quadSize *= 2;
		if (color == 0)
			this.setColor(0.97f, 0.76f, 0.51f);
		else
			this.setColor((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
	}

	public EntityBulletTracer(Level world, double startX, double startY, double startZ, double x, double y, double z,
			int duration, int crits, LivingEntity shooter, int type, float length) {
		this(world, startX, startY, startZ, x, y, z, duration, 0, length);
		if (crits != 2)
			this.setColor(0.97f, 0.76f, 0.51f);
		else {
			int color = TF2Util.getTeamColor(shooter);
			this.setColor(Mth.clamp((color >> 16) / 255f, 0.2f, 1f), Mth.clamp((color >> 8 & 255) / 255f, 0.2f, 1f),
					Mth.clamp((color & 255) / 255f, 0.2f, 1f));
		}
	}

	@Override
	public void tick() {
		if (this.nextDead)
			this.remove();
		this.nextDead = true;
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.setPos(this.x + this.xd, this.y + this.yd, this.z + this.zd);
		this.xd *= 1.025D;
		this.yd *= 1.025D;
		this.zd *= 1.025D;
		if (this.duration > 0) {
			this.duration--;
			if (this.duration <= 0)
				this.remove();
		}
		if (this.age++ >= this.lifetime)
			this.remove();
	}

	@Override
	public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
		Vec3 cam = camera.getPosition();
		float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x);
		float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y);
		float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z);
		Vec3 look = new Vec3(camera.getLookVector());
		Vec3 rightVec = new Vec3(this.xd, this.yd, this.zd).cross(look).normalize();
		float f4 = 0.1F * this.quadSize;
		float len = this.motionless ? this.length : 2 * this.length * (this.duration < 1 ? this.duration - (int) this.duration : 1);
		float xNext = (float) (x + this.xd * len);
		float yNext = (float) (y + this.yd * len);
		float zNext = (float) (z + this.zd * len);
		float u0 = this.getU0(), u1 = this.getU1(), v0 = this.getV0(), v1 = this.getV1();
		int light = this.getLightColor(partialTicks);
		buffer.vertex(x - rightVec.x * f4, y - rightVec.y * f4, z - rightVec.z * f4).uv(u1, v1)
				.color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		buffer.vertex(x + rightVec.x * f4, y + rightVec.y * f4, z + rightVec.z * f4).uv(u1, v0)
				.color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		buffer.vertex(xNext + rightVec.x * f4, yNext + rightVec.y * f4, zNext + rightVec.z * f4).uv(u0, v0)
				.color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		buffer.vertex(xNext - rightVec.x * f4, yNext - rightVec.y * f4, zNext - rightVec.z * f4).uv(u0, v1)
				.color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
	}

	public static class Factory implements IParticleFactory {
		@Override
		public Particle createParticle(int particleID, Level world, double x, double y, double z, double xSpeed,
				double ySpeed, double zSpeed, int... params) {
			return new EntityBulletTracer(world, x, y, z, xSpeed, ySpeed, zSpeed, params[0], params[1],
					params[2] / 64f);
		}
	}
}
