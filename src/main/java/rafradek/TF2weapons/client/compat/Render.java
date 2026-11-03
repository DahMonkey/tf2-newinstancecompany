package rafradek.TF2weapons.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * 1.12-style entity renderer base. Subclasses implement {@link #doRender} exactly as in 1.12; the
 * position arguments are always 0 because the pose stack is already at the entity.
 */
public abstract class Render<T extends Entity> extends EntityRenderer<T> {
	protected final EntityRenderDispatcher renderManager;
	protected float shadowSize;
	protected float shadowOpaque = 1.0F;

	protected Render(EntityRendererProvider.Context context) {
		super(context);
		this.renderManager = context.getEntityRenderDispatcher();
	}

	protected ResourceLocation getEntityTexture(T entity) {
		return null;
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		ResourceLocation tex = getEntityTexture(entity);
		return tex != null ? tex : new ResourceLocation("minecraft", "textures/misc/white.png");
	}

	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
	}

	@Override
	public void render(T entity, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffers,
			int light) {
		this.shadowRadius = this.shadowSize;
		this.shadowStrength = this.shadowOpaque;
		LegacyRender.begin(pose, buffers, light, OverlayTexture.NO_OVERLAY);
		try {
			doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		} finally {
			LegacyRender.setColor(1, 1, 1, 1);
			LegacyRender.end();
		}
		super.render(entity, entityYaw, partialTicks, pose, buffers, light);
	}

	protected boolean bindEntityTexture(T entity) {
		ResourceLocation tex = getEntityTexture(entity);
		if (tex == null)
			return false;
		bindTexture(tex);
		return true;
	}

	public void bindTexture(ResourceLocation location) {
		LegacyRender.setTexture(location);
	}

	public Font getFontRendererFromRenderManager() {
		return this.getFont();
	}

	public EntityRenderDispatcher getRenderManager() {
		return renderManager;
	}
}
