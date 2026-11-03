package rafradek.TF2weapons.entity.projectile;

import rafradek.TF2weapons.TF2weapons;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.PropertyType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityRocket extends EntityProjectileBase {

	public EntityRocket(Level p_i1756_1_) {
		this(TF2weapons.ROCKET, p_i1756_1_);
	}

	public EntityRocket(EntityType<? extends EntityRocket> type, Level p_i1756_1_) {
		super(type, p_i1756_1_);
		if (p_i1756_1_.isClientSide)
			ClientProxy.spawnRocketParticle(this.level, this);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		String name = ItemFromData.getData(weapon).getString(PropertyType.PROJECTILE);
		if (name.equals("cowmangler"))
			this.setProjType(1);
		super.initProjectile(shooter, hand, weapon);
	}

	@Override
	public void onHitGround(int x, int y, int z, HitResult mop) {
		this.explode(mop.getLocation().x + mop.getDirection().getStepX() * 0.02,
				mop.getLocation().y + mop.getDirection().getStepY() * 0.02,
				mop.getLocation().z + mop.getDirection().getStepZ() * 0.02, null, 1f);
	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		this.explode(mop.getLocation().x, mop.getLocation().y, mop.getLocation().z, mop.getEntity(), 1f);
	}

	public double maxMotion() {
		return Math.max(this.getDeltaMovement().x, Math.max(this.getDeltaMovement().y, this.getDeltaMovement().z));
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

	}

	@Override
	public void spawnParticles(double x, double y, double z) {
		if (this.isInWater())
			this.level.addParticle(ParticleTypes.WATER_BUBBLE, x, y, z, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
		else
			this.level.addParticle(ParticleTypes.SMOKE_NORMAL, x, y, z, 0, 0, 0);
	}

	@Override
	protected float getSpeed() {
		return 1.04f;
	}

	@Override
	public double getGravity() {
		return 0;
	}

}
