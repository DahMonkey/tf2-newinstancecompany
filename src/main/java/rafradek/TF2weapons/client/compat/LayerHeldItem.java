package rafradek.TF2weapons.client.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** 1.12 LayerHeldItem for legacy biped models. */
public class LayerHeldItem<T extends LivingEntity> implements LayerRenderer<T> {
	protected final RenderLivingBase<T> livingEntityRenderer;

	public LayerHeldItem(RenderLivingBase<T> renderer) {
		this.livingEntityRenderer = renderer;
	}

	@Override
	public void doRenderLayer(T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
			float netHeadYaw, float headPitch, float scale) {
		boolean rightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
		ItemStack left = rightHanded ? entity.getOffhandItem() : entity.getMainHandItem();
		ItemStack right = rightHanded ? entity.getMainHandItem() : entity.getOffhandItem();
		if (!left.isEmpty() || !right.isEmpty()) {
			GlStateManager.pushMatrix();
			if (this.livingEntityRenderer.getMainModel().isChild) {
				GlStateManager.translate(0.0F, 0.75F, 0.0F);
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
			}
			this.renderHeldItem(entity, right, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT);
			this.renderHeldItem(entity, left, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT);
			GlStateManager.popMatrix();
		}
	}

	protected void renderHeldItem(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType transform,
			HumanoidArm side) {
		if (stack.isEmpty())
			return;
		GlStateManager.pushMatrix();
		if (entity.isShiftKeyDown())
			GlStateManager.translate(0.0F, 0.2F, 0.0F);
		this.translateToHand(side);
		GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
		boolean left = side == HumanoidArm.LEFT;
		GlStateManager.translate((left ? -1 : 1) / 16.0F, 0.125F, -0.625F);
		Minecraft.getInstance().getItemRenderer().renderStatic(entity, stack, transform, left, LegacyRender.pose(),
				LegacyRender.buffers(), entity.level, LegacyRender.light(), net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
				entity.getId() + transform.ordinal());
		GlStateManager.popMatrix();
	}

	protected void translateToHand(HumanoidArm side) {
		((ModelBiped) this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625F, side);
	}
}
