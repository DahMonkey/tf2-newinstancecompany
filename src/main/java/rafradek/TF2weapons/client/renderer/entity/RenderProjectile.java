package rafradek.TF2weapons.client.renderer.entity;

import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderProjectile extends EntityRenderer<EntityProjectileBase> {
	private EntityModel model;
	private final ResourceLocation texturered;
	private final ResourceLocation textureblu;

	public RenderProjectile(EntityModel model, ResourceLocation redTexture, ResourceLocation bluTexture,
			EntityRenderDispatcher manager) {
		super(manager);
		// we could have initialized it above, but here is fine as well:
		this.model = model;
		this.texturered = redTexture;
		this.textureblu = bluTexture;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityProjectileBase entity) {

		return TF2Util.getTeamForDisplay(entity.shootingEntity) == 0 ? texturered : textureblu;
	}

	@Override
	public void doRender(EntityProjectileBase entity, double x, double y, double z, float yaw, float partialTick) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y + (entity.useCollisionBox() ? entity.getBbHeight() / 2f : 0), (float) z);
		GlStateManager.color(0.7F, 0.7F, 0.7F, 1F);
		GL11.glRotatef(entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTick - 90.0F,
				0.0F, 1.0F, 0.0F);
		GL11.glRotatef(entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTick, 0.0F,
				0.0F, 1.0F);

		bindEntityTexture(entity);
		if (entity.getCritical() == 2) {
			GlStateManager.disableLighting();
			model.render(entity, 0F, 0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			GlStateManager.enableLighting();
		} else
			model.render(entity, 0F, 0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		GL11.glPopMatrix();
	}
}
