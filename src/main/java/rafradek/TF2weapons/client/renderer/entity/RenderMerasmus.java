package rafradek.TF2weapons.client.renderer.entity;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class RenderMerasmus extends HumanoidMobRenderer<EntityMerasmus> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/merasmus.png");

	public RenderMerasmus(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn, new ModelMerasmus(), 0.8F);
		// this.addLayer(new LayerHeldItem(this));

	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMerasmus par1EntityLiving) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntityMerasmus entity, double x, double y, double z, float entityYaw, float partialTicks) {
		((ModelMerasmus) this.mainModel).rightArmPose = entity.isBombSpell() ? ArmPose.BOW_AND_ARROW : ArmPose.EMPTY;
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected void preRenderCallback(EntityMerasmus entitylivingbaseIn, float partialTickTime) {
		float f = 1.5F;
		GlStateManager.translate(0,
				-Mth.sin((partialTickTime + entitylivingbaseIn.tickCount) / 20f) * 0.4 - 0.5, 0);
		GlStateManager.scale(f, f, f);
	}

	public static class ModelMerasmus extends HumanoidModel {

		public ModelMerasmus() {
			super();
		}

		@Override
		public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
				float headPitch, float scaleFactor, Entity entityIn) {
			super.setRotationAngles(limbSwing, 0, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		}
	}
}
