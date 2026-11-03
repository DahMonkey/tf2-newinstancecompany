package rafradek.TF2weapons.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;

/** Draws a disguised spy with the skin of the player or class they are disguised as. */
public class RenderPlayerDisguised extends PlayerRenderer {

	public RenderPlayerDisguised(EntityRendererProvider.Context context, boolean useSmallArms) {
		super(context, useSmallArms);
	}

	@Override
	public ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
		WeaponsCapability cap = WeaponsCapability.get(entity);
		if (cap.getDisguiseType().startsWith("T:")) {
			return new ResourceLocation(RenderTF2Character.TEXTURE_PATH_BASE
					+ (TF2Util.getTeamForDisplay(entity) == 1 ? "red" : "blu") + "/"
					+ TF2Class.getClass(cap.getDisguiseType().substring(2)).getName() + ".png");
		}
		if (cap.skinDisguise != null)
			return cap.skinDisguise;
		return Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(entity.getGameProfile());
	}
}
