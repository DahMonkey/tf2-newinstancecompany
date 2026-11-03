package rafradek.TF2weapons.entity.building;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

public class SentryLookHelper extends LookControl {

	private Mob entity;
	/**
	 * The amount of change that is made each update for an entity facing a
	 * direction.
	 */
	private float deltaLookYaw;
	/**
	 * The amount of change that is made each update for an entity facing a
	 * direction.
	 */
	private float deltaLookPitch;
	/** Whether or not the entity is trying to look at something. */
	private boolean isLooking;
	private double posX;
	private double posY;
	private double posZ;

	public SentryLookHelper(Mob entitylivingIn) {
		super(entitylivingIn);
		this.entity = entitylivingIn;
	}

	/**
	 * Sets position to look at using entity
	 */
	@Override
	public void setLookPositionWithEntity(Entity entityIn, float deltaYaw, float deltaPitch) {
		this.setPos((entityIn.getX()), this.getY(), this.getZ());

		if (entityIn instanceof LivingEntity)
			this.setPos(this.getX(), (entityIn.getY() + entityIn.getEyeHeight()), this.getZ());
		else
			this.setPos(this.getX(), ((entityIn.getBoundingBox().minY + entityIn.getBoundingBox().maxY) / 2.0D), this.getZ());

		this.setPos(this.getX(), this.getY(), (entityIn.getZ()));
		this.deltaLookYaw = deltaYaw;
		this.deltaLookPitch = deltaPitch;
		this.isLooking = true;
	}

	/**
	 * Sets position to look at
	 */
	@Override
	public void setLookPosition(double x, double y, double z, float deltaYaw, float deltaPitch) {
		this.setPos((x), this.getY(), this.getZ());
		this.setPos(this.getX(), (y), this.getZ());
		this.setPos(this.getX(), this.getY(), (z));
		this.deltaLookYaw = deltaYaw;
		this.deltaLookPitch = deltaPitch;
		this.isLooking = true;
	}

	/**
	 * Updates look
	 */
	@Override
	public void onUpdateLook() {
		this.entity.setXRot((0.0F));

		if (this.isLooking) {
			this.isLooking = false;
			double d0 = this.getX() - this.entity.getX();
			double d1 = this.getY() - (this.entity.getY() + this.entity.getEyeHeight());
			double d2 = this.getZ() - this.entity.getZ();
			double d3 = Mth.sqrt((float)d0 * d0 + d2 * d2);
			float f = (float) (Mth.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
			float f1 = (float) (-(Mth.atan2(d1, d3) * (180D / Math.PI)));
			this.entity.setXRot((this.updateRotation(this.entity.getXRot(), f1, this.deltaLookPitch)));
			this.entity.yHeadRot = this.updateRotation(this.entity.yHeadRot, f, this.deltaLookYaw);
		} else {
			float fullrot = this.entity.tickCount % 120;
			if (fullrot > 60) {
				fullrot = 120 - fullrot;
			}
			fullrot /= 60f;
			this.entity.yHeadRot = this.updateRotation(this.entity.yHeadRot,
					this.entity.yBodyRot - 45 + fullrot * 90, 10.0F);
		}

		float f2 = Mth.wrapDegrees(this.entity.yHeadRot - this.entity.yBodyRot);

		if (!this.entity.getNavigation().isDone()) {
			if (f2 < -75.0F) {
				this.entity.yHeadRot = this.entity.yBodyRot - 75.0F;
			}

			if (f2 > 75.0F) {
				this.entity.yHeadRot = this.entity.yBodyRot + 75.0F;
			}
		}
	}

	private float updateRotation(float p_75652_1_, float p_75652_2_, float p_75652_3_) {
		float f = Mth.wrapDegrees(p_75652_2_ - p_75652_1_);

		if (f > p_75652_3_)
			f = p_75652_3_;

		if (f < -p_75652_3_)
			f = -p_75652_3_;

		return p_75652_1_ + f;
	}

	@Override
	public boolean getIsLooking() {
		return this.isLooking;
	}

	@Override
	public double getLookPosX() {
		return this.getX();
	}

	@Override
	public double getLookPosY() {
		return this.getY();
	}

	@Override
	public double getLookPosZ() {
		return this.getZ();
	}
}
