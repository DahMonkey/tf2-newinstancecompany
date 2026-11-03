package rafradek.TF2weapons.client.particle;

import net.minecraft.world.phys.BlockHitResult;
import rafradek.TF2weapons.client.compat.BufferBuilder;

import net.minecraft.client.particle.Particle;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ParticleBulletHole extends Particle {

	HitResult block;

	public ParticleBulletHole(Level world, HitResult result) {
		super(world, result.getLocation().x + ((BlockHitResult) result).getDirection().getStepX() * 0.012,
				result.getLocation().y + ((BlockHitResult) result).getDirection().getStepY() * 0.012,
				result.getLocation().z + ((BlockHitResult) result).getDirection().getStepZ() * 0.012);
		this.block = result;
		this.rCol = 0.05F;
		this.gCol = 0.05F;
		this.bCol = 0.05F;
		this.alpha = 1f;
		this.lifetime = 280;
		this.quadSize *= 0.9F;
		this.setSize(0.1F, 0.1F);
		this.setParticleTextureIndex(0);
	}

	@Override
	public void onUpdate() {
		this.setDeltaMovement((0), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (0), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (0));
		this.xo = this.getX();
		this.yo = this.getY();
		this.zo = this.getZ();
		// this.move(this.motionX, this.motionY, this.motionZ);

		if (this.level.getBlockState(block.getBlockPos()).getBlock()
				.isAir(this.level.getBlockState(block.getBlockPos()), world, ((BlockHitResult) block).getBlockPos())) {
			this.setExpired();
		}

		if (this.lifetime-- <= 0) {
			this.setExpired();
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		// super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ,
		// rotationYZ, rotationXY, rotationXZ);
		// System.out.println("rot: "+rotationX+" "+rotationZ+" "+rotationYZ+"
		// "+rotationXY+" "+rotationXZ);
		Direction face = this.block.getDirection();
		if (face == Direction.UP)
			super.renderParticle(buffer, entityIn, partialTicks, 1, 0, 0, 0, 1);
		else if (face == Direction.DOWN)
			super.renderParticle(buffer, entityIn, partialTicks, 1, 0, 0, 0, -1);
		else if (face == Direction.NORTH)
			super.renderParticle(buffer, entityIn, partialTicks, 1, 1, 0, 0, 0);
		else if (face == Direction.SOUTH)
			super.renderParticle(buffer, entityIn, partialTicks, -1, 1, 0, 0, 0);
		else if (face == Direction.EAST)
			super.renderParticle(buffer, entityIn, partialTicks, 0, 1, 1, 0, 0);
		else if (face == Direction.WEST)
			super.renderParticle(buffer, entityIn, partialTicks, 0, 1, -1, 0, 0);
	}
}
