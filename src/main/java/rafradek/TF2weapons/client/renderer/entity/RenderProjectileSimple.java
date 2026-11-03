package rafradek.TF2weapons.client.renderer.entity;

import rafradek.TF2weapons.client.compat.Render;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;
import rafradek.TF2weapons.client.compat.DefaultVertexFormats;

import rafradek.TF2weapons.client.compat.ModelBase;
import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.GL11;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.model.ModelSyringe;
import rafradek.TF2weapons.entity.projectile.EntityProjectileSimple;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RenderProjectileSimple extends Render<EntityProjectileSimple> {

	private ModelBase model;
	private static final ResourceLocation texturered = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/projectile/syringered.png");
	private static final ResourceLocation textureblu = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/projectile/syringeblu.png");
	private static final ResourceLocation arrow = new ResourceLocation("textures/entity/projectiles/arrow.png");

	public RenderProjectileSimple(EntityRendererProvider.Context manager) {
		super(manager);
		// we could have initialized it above, but here is fine as well:
		model = new ModelSyringe();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityProjectileSimple entity) {

		if (entity.getProjType() == 3)
			return arrow;
		return TF2Util.getTeamForDisplay(entity.shootingEntity) == 0 ? texturered : textureblu;
	}

	@Override
	public void doRender(EntityProjectileSimple entity, double x, double y, double z, float yaw, float partialTick) {
		if (entity.getProjType() == 4 || entity.getProjType() == 5)
			return;
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		GlStateManager.color(0.7F, 0.7F, 0.7F, 1F);
		if (entity.getProjType() != 2 && entity.getProjType() != 3)
			GL11.glScalef(0.25f, 0.25f, 0.25f);
		if (entity.getProjType() != 3) {
			GL11.glRotatef(entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTick, 0.0F,
					1.0F, 0.0F);
			GL11.glRotatef(
					(entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTick) * -1,
					1.0F, 0.0F, 0.0F);
		}

		bindEntityTexture(entity);

		// GL11.glTranslatef((float)entity.posX, (float)entity.posY,
		// entity.posZ);
		if (entity.getCritical() == 2)
			GlStateManager.disableLighting();

		// GlStateManager.color(0.0F, 0.0F, 1.0F, 1F);
		if (entity.getProjType() == 2)
			Minecraft.getInstance().getItemRenderer().renderItem(ItemFromData.getNewStack("cleaver"),
					ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
		else if (entity.getProjType() == 3) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.pushMatrix();
			GlStateManager.rotate(
					entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTick - 90.0F, 0.0F,
					1.0F, 0.0F);
			GlStateManager.rotate(
					entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTick, 0.0F,
					0.0F, 1.0F);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			GlStateManager.enableRescaleNormal();
			float f9 = 0;

			if (f9 > 0.0F) {
				float f10 = -Mth.sin(f9 * 3.0F) * f9;
				GlStateManager.rotate(f10, 0.0F, 0.0F, 1.0F);
			}

			GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(0.05625F, 0.05625F, 0.05625F);
			GlStateManager.translate(-4.0F, 0.0F, 0.0F);

			if (this.renderOutlines) {
				GlStateManager.enableColorMaterial();
				GlStateManager.enableOutlineMode(this.getTeamColor(entity));
			}

			GlStateManager.glNormal3f(0.05625F, 0.0F, 0.0F);
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			bufferbuilder.pos(-7.0D, -2.0D, -2.0D).tex(0.0D, 0.15625D).endVertex();
			bufferbuilder.pos(-7.0D, -2.0D, 2.0D).tex(0.15625D, 0.15625D).endVertex();
			bufferbuilder.pos(-7.0D, 2.0D, 2.0D).tex(0.15625D, 0.3125D).endVertex();
			bufferbuilder.pos(-7.0D, 2.0D, -2.0D).tex(0.0D, 0.3125D).endVertex();
			tessellator.draw();
			GlStateManager.glNormal3f(-0.05625F, 0.0F, 0.0F);
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			bufferbuilder.pos(-7.0D, 2.0D, -2.0D).tex(0.0D, 0.15625D).endVertex();
			bufferbuilder.pos(-7.0D, 2.0D, 2.0D).tex(0.15625D, 0.15625D).endVertex();
			bufferbuilder.pos(-7.0D, -2.0D, 2.0D).tex(0.15625D, 0.3125D).endVertex();
			bufferbuilder.pos(-7.0D, -2.0D, -2.0D).tex(0.0D, 0.3125D).endVertex();
			tessellator.draw();

			for (int j = 0; j < 4; ++j) {
				GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.glNormal3f(0.0F, 0.0F, 0.05625F);
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
				bufferbuilder.pos(-8.0D, -2.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
				bufferbuilder.pos(8.0D, -2.0D, 0.0D).tex(0.5D, 0.0D).endVertex();
				bufferbuilder.pos(8.0D, 2.0D, 0.0D).tex(0.5D, 0.15625D).endVertex();
				bufferbuilder.pos(-8.0D, 2.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
				tessellator.draw();
			}

			if (this.renderOutlines) {
				GlStateManager.disableOutlineMode();
				GlStateManager.disableColorMaterial();
			}

			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
		} else if (entity.getProjType() == 8) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(2, 2, 2);
			Minecraft.getInstance().getItemRenderer().renderItem(ItemFromData.getNewStack("hhhaxe"),
					ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
			GlStateManager.popMatrix();
		} else
			model.render(entity, 0F, 0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		// GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
		// GlStateManager.enableTexture2D();
		if (entity.getCritical() == 2)
			GlStateManager.disableLighting();
		GlStateManager.enableLighting();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);

		GL11.glPopMatrix();

	}

}
