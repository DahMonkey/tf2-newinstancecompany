package rafradek.TF2weapons.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Base for the mod's particles: a 1.19 sprite particle that keeps the 1.12 helpers they use, picking
 * vanilla particle sprites by their old texture-sheet index, or drawing a sprite from the block atlas.
 */
public abstract class LegacyParticle extends TextureSheetParticle {

	private boolean blockAtlas;
	protected boolean fullBright = true;

	protected LegacyParticle(Level world, double x, double y, double z) {
		super((ClientLevel) world, x, y, z);
	}

	protected LegacyParticle(Level world, double x, double y, double z, double xd, double yd, double zd) {
		super((ClientLevel) world, x, y, z, xd, yd, zd);
	}

	/** Sprite from 1.12's particles.png by index. */
	public void setParticleTextureIndex(int index) {
		String name;
		if (index >= 0 && index < 8)
			name = "generic_" + index;
		else if (index == 32)
			name = "bubble";
		else if (index == 48)
			name = "flame";
		else if (index == 65 || index == 66)
			name = "critical_hit";
		else if (index >= 160 && index < 168)
			name = "spark_" + (index - 160);
		else
			name = "generic_0";
		TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance().getTextureManager()
				.getTexture(TextureAtlas.LOCATION_PARTICLES);
		this.setSprite(atlas.getSprite(new ResourceLocation("minecraft", name)));
		this.blockAtlas = false;
	}

	/** A sprite stitched into the block atlas (the mod's own particle icons). */
	public void setParticleTexture(TextureAtlasSprite sprite) {
		if (sprite != null) {
			this.setSprite(sprite);
			this.blockAtlas = true;
		} else
			this.setParticleTextureIndex(0);
	}

	public void setExpired() {
		this.remove();
	}

	public void multipleParticleScaleBy(float scale) {
		this.scale(scale);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return this.blockAtlas ? ParticleRenderType.TERRAIN_SHEET : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	protected int getLightColor(float partialTick) {
		return this.fullBright ? 15728880 : super.getLightColor(partialTick);
	}
}
