package rafradek.TF2weapons.potion;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.util.ReflectionAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PotionTF2 extends MobEffect {

	public PotionTF2(boolean isBadEffectIn, int liquidColorIn, int x, int y) {
		super(isBadEffectIn, liquidColorIn);
		this.setIconIndex(x, y);

	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		if (this == TF2weapons.bleeding)
			return duration % 10 == 0;
		return false;
	}

	@Override
	public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
		if (this == TF2weapons.bleeding && !entityLivingBaseIn.level.isClientSide) {
			((ServerLevel) entityLivingBaseIn.level).addParticle(ParticleTypes.REDSTONE, entityLivingBaseIn.getX(),
					entityLivingBaseIn.getY() + entityLivingBaseIn.getBbHeight() / 2, entityLivingBaseIn.getZ(), 7,
					entityLivingBaseIn.getBbWidth() / 2, entityLivingBaseIn.getBbHeight() / 2, entityLivingBaseIn.getBbWidth() / 2, 0);
			if (entityLivingBaseIn.getLastHurtByMob() instanceof Player) {
				try {
					ReflectionAccess.entityRecentlyHit.setInt(entityLivingBaseIn, 100);
				} catch (Exception e) {}
				entityLivingBaseIn.setLastHurtByMob(entityLivingBaseIn.getLastHurtByMob());
			}
			entityLivingBaseIn.hurt(DamageSource.MAGIC, 0.41f * (amplifier + 1));
		}
	}
}
