package rafradek.TF2weapons.entity.building;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

/** Sentry head control: turns toward targets, and sweeps left and right when idle. */
public class SentryLookHelper extends LookControl {

	private final Mob entity;
	private float deltaLookYaw;
	private float deltaLookPitch;
	private boolean isLooking;
	private double posX;
	private double posY;
	private double posZ;

	public SentryLookHelper(Mob entitylivingIn) {
		super(entitylivingIn);
		this.entity = entitylivingIn;
	}

	@Override
	public void setLookAt(Entity entityIn, float deltaYaw, float deltaPitch) {
		this.posX = entityIn.getX();
		if (entityIn instanceof LivingEntity)
			this.posY = entityIn.getY() + entityIn.getEyeHeight();
		else
			this.posY = (entityIn.getBoundingBox().minY + entityIn.getBoundingBox().maxY) / 2.0D;
		this.posZ = entityIn.getZ();
		this.deltaLookYaw = deltaYaw;
		this.deltaLookPitch = deltaPitch;
		this.isLooking = true;
	}

	@Override
	public void setLookAt(double x, double y, double z, float deltaYaw, float deltaPitch) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.deltaLookYaw = deltaYaw;
		this.deltaLookPitch = deltaPitch;
		this.isLooking = true;
	}

	@Override
	public void tick() {
		this.entity.setXRot(0.0F);

		if (this.isLooking) {
			this.isLooking = false;
			double d0 = this.posX - this.entity.getX();
			double d1 = this.posY - (this.entity.getY() + this.entity.getEyeHeight());
			double d2 = this.posZ - this.entity.getZ();
			double d3 = Math.sqrt(d0 * d0 + d2 * d2);
			float f = (float) (Mth.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
			float f1 = (float) (-(Mth.atan2(d1, d3) * (180D / Math.PI)));
			this.entity.setXRot(this.updateRotation(this.entity.getXRot(), f1, this.deltaLookPitch));
			this.entity.yHeadRot = this.updateRotation(this.entity.yHeadRot, f, this.deltaLookYaw);
		} else {
			float fullrot = this.entity.tickCount % 120;
			if (fullrot > 60)
				fullrot = 120 - fullrot;
			fullrot /= 60f;
			this.entity.yHeadRot = this.updateRotation(this.entity.yHeadRot, this.entity.yBodyRot - 45 + fullrot * 90,
					10.0F);
		}

		float f2 = Mth.wrapDegrees(this.entity.yHeadRot - this.entity.yBodyRot);
		if (!this.entity.getNavigation().isDone()) {
			if (f2 < -75.0F)
				this.entity.yHeadRot = this.entity.yBodyRot - 75.0F;
			if (f2 > 75.0F)
				this.entity.yHeadRot = this.entity.yBodyRot + 75.0F;
		}
	}

	private float updateRotation(float current, float wanted, float max) {
		float f = Mth.wrapDegrees(wanted - current);
		if (f > max)
			f = max;
		if (f < -max)
			f = -max;
		return current + f;
	}

	public boolean getIsLooking() {
		return this.isLooking;
	}

	@Override
	public double getWantedX() {
		return this.posX;
	}

	@Override
	public double getWantedY() {
		return this.posY;
	}

	@Override
	public double getWantedZ() {
		return this.posZ;
	}
}
