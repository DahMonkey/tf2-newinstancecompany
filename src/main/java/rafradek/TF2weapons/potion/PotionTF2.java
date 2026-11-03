package rafradek.TF2weapons.potion;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.TF2EffectIcons;

import java.util.function.Consumer;

/**
 * Status effect drawn with one of vanilla's effect icons. 1.12 picked the icon by its position in
 * the old inventory icon sheet ({@code iconX}, {@code iconY}); the same position is mapped to the
 * matching vanilla effect here.
 */
public class PotionTF2 extends MobEffect {

	private static final MobEffect[][] ICON_SHEET = {
			{ MobEffects.MOVEMENT_SPEED, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.DIG_SPEED, MobEffects.DIG_SLOWDOWN,
					MobEffects.DAMAGE_BOOST, MobEffects.WEAKNESS, MobEffects.POISON, MobEffects.REGENERATION },
			{ MobEffects.INVISIBILITY, MobEffects.HUNGER, MobEffects.JUMP, MobEffects.CONFUSION, MobEffects.NIGHT_VISION,
					MobEffects.BLINDNESS, MobEffects.DAMAGE_RESISTANCE, MobEffects.FIRE_RESISTANCE },
			{ MobEffects.WATER_BREATHING, MobEffects.WITHER, MobEffects.ABSORPTION, MobEffects.LEVITATION,
					MobEffects.GLOWING, MobEffects.LUCK, MobEffects.UNLUCK, MobEffects.HEALTH_BOOST } };

	private final int iconX, iconY;
	private final String legacyName;

	public PotionTF2(boolean isBadEffectIn, int liquidColorIn, int iconX, int iconY, String legacyName) {
		super(isBadEffectIn ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, liquidColorIn);
		this.iconX = iconX;
		this.iconY = iconY;
		this.legacyName = legacyName;
	}

	public MobEffect getIconEffect() {
		return ICON_SHEET[Math.min(iconY, 2)][Math.min(iconX, 7)];
	}

	@Override
	public String getDescriptionId() {
		return legacyName;
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		if (this == TF2weapons.bleeding)
			return duration % 10 == 0;
		return false;
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (this == TF2weapons.bleeding && !entity.level.isClientSide) {
			((ServerLevel) entity.level).sendParticles(DustParticleOptions.REDSTONE, entity.getX(),
					entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 7, entity.getBbWidth() / 2,
					entity.getBbHeight() / 2, entity.getBbWidth() / 2, 0);
			if (entity.getLastHurtByMob() instanceof Player)
				entity.setLastHurtByPlayer((Player) entity.getLastHurtByMob());
			entity.hurt(DamageSource.MAGIC, 0.41f * (amplifier + 1));
		}
	}

	@Override
	public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
		consumer.accept(TF2EffectIcons.forVanillaIcon(this::getIconEffect));
	}
}
