package rafradek.TF2weapons.client.renderer.entity;

import rafradek.TF2weapons.client.compat.RenderBiped;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import rafradek.TF2weapons.client.compat.ModelBiped;
import rafradek.TF2weapons.client.compat.GlStateManager;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class RenderMerasmus extends RenderBiped<EntityMerasmus> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/merasmus.png");

	public RenderMerasmus(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn, new ModelMerasmus(), 0.8F);
		// this.addLayer(new LayerHeldItem(this));

	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMerasmus par1EntityLiving) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntityMerasmus entity, double x, double y, double z, float entityYaw, float partialTicks) {
		((ModelMerasmus) this.mainModel).rightArmPose = entity.isBombSpell() ? rafradek.TF2weapons.client.compat.ModelBiped.ArmPose.BOW_AND_ARROW : rafradek.TF2weapons.client.compat.ModelBiped.ArmPose.EMPTY;
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected void preRenderCallback(EntityMerasmus entitylivingbaseIn, float partialTickTime) {
		float f = 1.5F;
		GlStateManager.translate(0,
				-Mth.sin((partialTickTime + entitylivingbaseIn.tickCount) / 20f) * 0.4 - 0.5, 0);
		GlStateManager.scale(f, f, f);
	}

	public static class ModelMerasmus extends ModelBiped {

		public ModelMerasmus() {
			super();
		}

		@Override
		public void setRotationAngles(float limbSwing, float animationSpeed, float ageInTicks, float netHeadYaw,
				float headPitch, float scaleFactor, Entity entityIn) {
			super.setRotationAngles(limbSwing, 0, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		}
	}
}
