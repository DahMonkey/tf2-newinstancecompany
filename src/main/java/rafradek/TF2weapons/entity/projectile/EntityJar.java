package rafradek.TF2weapons.entity.projectile;

import net.minecraft.world.phys.BlockHitResult;
import rafradek.TF2weapons.util.TF2HitResult;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.TF2weapons;
import net.minecraft.world.entity.EntityType;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemUsable;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityJar extends EntityProjectileBase {

	public int gasExplodeCooldown = 40;

	public EntityJar(Level p_i1756_1_) {
		this(TF2weapons.JAR, p_i1756_1_);
	}

	public EntityJar(EntityType<? extends EntityJar> type, Level p_i1756_1_) {
		super(type, p_i1756_1_);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		String name = ItemFromData.getData(weapon).getString(PropertyType.PROJECTILE);
		if (name.equals("gas"))
			this.setProjType(1);
		super.initProjectile(shooter, hand, weapon);
	}

	@Override
	public void explode(double x, double y, double z, Entity direct, float power) {
		if (!this.level.isClientSide) {
			int coatedCount = 0;
			for (LivingEntity living : this.level.getEntitiesOfClass(LivingEntity.class,
					this.getBoundingBox().inflate(5, 5, 5)))
				if (!this.hitEntities.contains(living)) {
					if (living.isAffectedByPotions() && living.distanceToSqr(this) < 25 && living != this.shootingEntity
							&& !TF2Util.isAlliedTo(this.shootingEntity, living)) {
						living.addEffect(new MobEffectInstance(
								TF2Util.getEffect(
										ItemFromData.getData(this.usedWeapon).getString(PropertyType.EFFECT_TYPE)),
								300));
						if (TF2Util.isEnemy(this.shootingEntity, living))
							coatedCount++;
					} else
						living.clearFire();

					this.hitEntities.add(living);
				}
			/*
			 * if(coatedCount>=5 && this.shootingEntity instanceof EntityPlayer)
			 * ((EntityPlayer)
			 * this.shootingEntity).awardStat(TF2Achievements.JARATE_MULTIPLE);
			 */
			if (!this.isGasExploded()) {
				this.playSound(ItemUsable.getSound(usedWeapon, PropertyType.EXPLOSION_SOUND), 1f, 1f);
				// this.playSound(TF2Sounds.JAR_EXPLODE, 1.5f, 1f);
				this.level.levelEvent(2002, this.blockPosition(),
						TF2Util.getEffect(
								ItemFromData.getData(this.usedWeapon).getString(PropertyType.EFFECT_TYPE))
								.getColor());
			}
			if (this.isGas()) {
				this.setDeltaMovement((0.), this.getDeltaMovement().y, this.getDeltaMovement().z);
				this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (0.));
				this.setProjType(2);
			} else
				this.discard();
		}
	}

	@Override
	public void onHitGround(int x, int y, int z, BlockHitResult mop) {
		this.explode(mop.getLocation().x + ((BlockHitResult) mop).getDirection().getStepX() * 0.05,
				mop.getLocation().y + ((BlockHitResult) mop).getDirection().getStepY() * 0.05,
				mop.getLocation().z + ((BlockHitResult) mop).getDirection().getStepZ() * 0.05, null, 1f);
		if (this.isGas()) {
			this.setDeltaMovement((0), this.getDeltaMovement().y, this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x, (0), this.getDeltaMovement().z);
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (0));
			this.setPos((mop.getLocation().x), this.getY(), this.getZ());
			this.setPos(this.getX(), (mop.getLocation().y), this.getZ());
			this.setPos(this.getX(), this.getY(), (mop.getLocation().z));
			if (mop.getDirection() == Direction.UP) {
				this.setSticked(true);
			}

		}
	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		this.explode(mop.getLocation().x, mop.getLocation().y, mop.getLocation().z, TF2HitResult.getEntity(mop), 1f);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isGasExploded()) {
			this.explode(this.getX(), this.getY(), this.getZ(), null, 1f);
			if (this.gasExplodeCooldown-- <= 0) {
				this.discard();
			}
		}
	}

	public boolean isGas() {
		return this.getProjType() == 1 || this.getProjType() == 2;
	}

	public boolean isGasExploded() {
		return this.getProjType() == 2;
	}

	@Override
	public void spawnParticles(double x, double y, double z) {
		if (this.isInWater())
			this.level.addParticle(ParticleTypes.BUBBLE, x, y, z, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
		if (this.isGasExploded()) {
			for (int i = 0; i < 8; ++i) {
				Vec3 pos = TF2Util.radiusRandom2D(5f, this.random);
				double xa = this.getX() + pos.x;
				double ya = this.getY() + 0.5;
				double za = this.getZ() + pos.y;
				ClientProxy.spawnGasSmokeParticle(this.level, xa, ya, za, 0x2B3E00);
			}

		}
	}

	@Override
	protected float getSpeed() {
		return 1.04f;
	}

}
