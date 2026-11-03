package rafradek.TF2weapons.potion;

import net.minecraft.world.effect.MobEffect;

public class PotionStun extends MobEffect {

	public PotionStun(boolean isBadEffectIn, int liquidColorIn) {
		super(isBadEffectIn, liquidColorIn);
		this.setIconIndex(3, 1);
	}

}
