package rafradek.TF2weapons.client.renderer.entity;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.boss.EntityHHH;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderHHH extends HumanoidMobRenderer<EntityHHH> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/hhh.png");

	public RenderHHH(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn, new HumanoidModel(), 0.8F);
		this.addLayer(new LayerHeldItem(this));

	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHHH par1EntityLiving) {
		return TEXTURE;
	}

	@Override
	protected void preRenderCallback(EntityHHH entitylivingbaseIn, float partialTickTime) {
		float f = 1.45F;
		if (entitylivingbaseIn.begin > 0)
			GlStateManager.translate(0, entitylivingbaseIn.begin * 0.1f, 0);
		GlStateManager.scale(f, f, f);
	}
}
