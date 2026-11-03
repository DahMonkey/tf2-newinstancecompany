package rafradek.TF2weapons.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ParticleBulletHole extends LegacyParticle {

	private final BlockPos block;
	private final Direction face;

	public ParticleBulletHole(Level world, HitResult result) {
		super(world, result.getLocation().x, result.getLocation().y, result.getLocation().z);
		BlockHitResult hit = (BlockHitResult) result;
		this.face = hit.getDirection();
		this.block = hit.getBlockPos();
		this.setPos(this.x + this.face.getStepX() * 0.012, this.y + this.face.getStepY() * 0.012,
				this.z + this.face.getStepZ() * 0.012);
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.rCol = this.gCol = this.bCol = 0.05F;
		this.alpha = 1f;
		this.lifetime = 280;
		this.quadSize = 0.09F;
		this.fullBright = false;
		this.setParticleTextureIndex(0);
	}

	@Override
	public void tick() {
		this.xd = this.yd = this.zd = 0;
		if (this.level.getBlockState(this.block).isAir() || this.age++ >= this.lifetime)
			this.remove();
	}

	@Override
	public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
		Vec3 cam = camera.getPosition();
		float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x());
		float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y());
		float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z());
		Quaternion rot = this.face.getRotation();
		Vector3f[] corners = new Vector3f[] { new Vector3f(-1, 0, -1), new Vector3f(-1, 0, 1), new Vector3f(1, 0, 1),
				new Vector3f(1, 0, -1) };
		float size = this.getQuadSize(partialTicks);
		for (Vector3f corner : corners) {
			corner.transform(rot);
			corner.mul(size);
			corner.add(px, py, pz);
		}
		float u0 = this.getU0(), u1 = this.getU1(), v0 = this.getV0(), v1 = this.getV1();
		int light = this.getLightColor(partialTicks);
		buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
	}
}
