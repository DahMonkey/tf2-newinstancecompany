package rafradek.TF2weapons.client.renderer.entity;

import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderPlayerForceTexture extends PlayerRenderer {

	public RenderPlayerForceTexture(EntityRenderDispatcher renderManager) {
		super(renderManager);
	}

	public RenderPlayerForceTexture(EntityRenderDispatcher renderManager, boolean useSmallArms) {
		super(renderManager, useSmallArms);
	}

	@Override
	public ResourceLocation getEntityTexture(final AbstractClientPlayer entity) {

		if (TF2PlayerCapability.get(entity).isForceClassTexture()
				&& WeaponsCapability.get(entity).getUsedToken() >= 0) {
			return new ResourceLocation(RenderTF2Character.TEXTURE_PATH_BASE + (TF2Util.getTeamForDisplay(entity) == 1 ? "blu" : "red")
					+ "/" + TF2Class.getClass(WeaponsCapability.get(entity).getUsedToken()).getName() + ".png");
		}
		return PlayerRenderer.class.cast(this.renderManager.getEntityRenderObject(entity)).getEntityTexture(entity);
	}
}
