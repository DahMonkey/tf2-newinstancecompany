package rafradek.TF2weapons.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityMuzzleFlash extends LegacyParticle {

	private final LivingEntity owner;
	private final InteractionHand hand;

	public EntityMuzzleFlash(Level world, LivingEntity shooter, InteractionHand hand) {
		super(world, shooter.getX(), shooter.getY(), shooter.getZ());
		this.lifetime = 5;
		this.hand = hand;
		this.owner = shooter;
		this.gravity = 0;
		this.setParticleTextureIndex(65);
		this.quadSize = 0.1f;
		this.setColor(0.97f, 0.76f, 0.51f);
		this.followOwner();
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
	}

	@Override
	public void tick() {
		super.tick();
		this.quadSize *= 0.92f;
		this.alpha *= 0.8f;
		this.followOwner();
	}

	private void followOwner() {
		float move = Minecraft.getInstance().player == this.owner ? 0.48f : 0.24f;
		float side = this.hand == InteractionHand.MAIN_HAND ? 1 : -1;
		float yaw = this.owner.yHeadRot / 180.0F * (float) Math.PI;
		float pitch = this.owner.getXRot() / 180.0F * (float) Math.PI;
		double px = this.owner.getX() - Mth.cos(yaw) * move * side - Mth.sin(yaw) * Mth.cos(pitch);
		double py = this.owner.getY() + this.owner.getEyeHeight() - move * 0.45f - Mth.sin(pitch);
		double pz = this.owner.getZ() - Mth.sin(yaw) * move * side + Mth.cos(yaw) * Mth.cos(pitch);
		this.setPos(px, py, pz);
		this.xd = this.yd = this.zd = 0;
	}
}
