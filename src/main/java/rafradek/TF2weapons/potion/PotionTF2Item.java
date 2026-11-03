package rafradek.TF2weapons.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import rafradek.TF2weapons.client.TF2EffectIcons;

import java.util.function.Consumer;

/** Status effect whose icon is one of the mod's item textures. */
public class PotionTF2Item extends MobEffect {

	public final ResourceLocation texture;
	private final String legacyName;

	public PotionTF2Item(boolean isBadEffectIn, int liquidColorIn, ResourceLocation texture, String legacyName) {
		super(isBadEffectIn ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, liquidColorIn);
		this.texture = texture;
		this.legacyName = legacyName;
	}

	@Override
	public String getDescriptionId() {
		return legacyName;
	}

	@Override
	public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
		consumer.accept(TF2EffectIcons.forTexture(texture));
	}
}
