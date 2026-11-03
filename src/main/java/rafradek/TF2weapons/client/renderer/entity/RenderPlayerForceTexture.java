package rafradek.TF2weapons.client.renderer.entity;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;

/** Draws a player with the mercenary texture of the class token they use, when they opted in. */
public class RenderPlayerForceTexture extends PlayerRenderer {

	public RenderPlayerForceTexture(EntityRendererProvider.Context context, boolean useSmallArms) {
		super(context, useSmallArms);
	}

	@Override
	public ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
		if (TF2PlayerCapability.get(entity).isForceClassTexture()
				&& WeaponsCapability.get(entity).getUsedToken() >= 0) {
			return new ResourceLocation(RenderTF2Character.TEXTURE_PATH_BASE
					+ (TF2Util.getTeamForDisplay(entity) == 1 ? "blu" : "red") + "/"
					+ TF2Class.getClass(WeaponsCapability.get(entity).getUsedToken()).getName() + ".png");
		}
		return entity.getSkinTextureLocation();
	}
}
