package rafradek.TF2weapons.client.renderer.entity;

import rafradek.TF2weapons.client.compat.RenderItem;

import rafradek.TF2weapons.client.compat.Render;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.RenderHelper;

import rafradek.TF2weapons.entity.EntityPickup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RenderPickup extends Render<EntityPickup> {

	// if you want a model, be sure to add it here:

	public RenderPickup(EntityRendererProvider.Context manager) {
		super(manager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityPickup entity) {
		return TextureAtlas.LOCATION_BLOCKS_TEXTURE;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void doRender(EntityPickup entity, double x, double y, double z, float yaw, float partialTick) {

		if (entity.isDisabled())
			return;
		boolean flag = false;

		RenderItem itemRenderer = Minecraft.getInstance().getItemRenderer();
		if (this.bindEntityTexture(entity)) {
			this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);
			flag = true;
		}

		ItemStack model = entity.getType().model;
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.pushMatrix();
		BakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(model, entity.level, (LivingEntity) null);

		boolean is3d = ibakedmodel.isGui3d();
		float f1 = /* true ? */ Mth.sin((entity.age + partialTick) / 10.0F + entity.hoverStart) * 0.1F + 0.1F;// :
																														// 0;
		float f2 = ibakedmodel.getItemCameraTransforms()
				.getTransform(ItemTransforms.TransformType.GROUND).scale.y;
		GlStateManager.translate((float) x, (float) y + f1 + 0.25F * f2, (float) z);

		if (is3d || this.renderManager.options != null) {
			float f3 = ((entity.age + partialTick) / 20.0F + entity.hoverStart) * (180F / (float) Math.PI);
			GlStateManager.rotate(f3, 0.0F, 1.0F, 0.0F);
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.pushMatrix();

		BakedModel transformedModel = ForgeHooksClient.handleCameraTransforms(ibakedmodel,
				ItemTransforms.TransformType.GROUND, false);
		itemRenderer.renderItem(model, transformedModel);
		GlStateManager.popMatrix();
		GlStateManager.translate(0.0F, 0.0F, 0.09375F);

		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		this.bindEntityTexture(entity);

		if (flag) {
			this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
		}

		super.doRender(entity, x, y, z, yaw, partialTick);
	}

}
