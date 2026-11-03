package rafradek.TF2weapons.client.compat;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/** 1.12 RenderLivingBase re-implemented on the legacy render context. */
public abstract class RenderLivingBase<T extends LivingEntity> extends Render<T> {
	protected ModelBase mainModel;
	protected List<LayerRenderer<T>> layerRenderers = new ArrayList<>();
	protected boolean renderMarker;

	protected RenderLivingBase(EntityRendererProvider.Context context, ModelBase model, float shadowSize) {
		super(context);
		this.mainModel = model;
		this.shadowSize = shadowSize;
	}

	public <V extends LayerRenderer<T>> boolean addLayer(V layer) {
		return this.layerRenderers.add(layer);
	}

	public ModelBase getMainModel() {
		return this.mainModel;
	}

	protected float interpolateRotation(float prev, float cur, float partialTicks) {
		float f = Mth.wrapDegrees(cur - prev);
		return prev + partialTicks * f;
	}

	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
		this.mainModel.isRiding = entity.isPassenger();
		this.mainModel.isChild = entity.isBaby();
		try {
			float bodyYaw = this.interpolateRotation(entity.yBodyRotO, entity.yBodyRot, partialTicks);
			float headYaw = this.interpolateRotation(entity.yHeadRotO, entity.yHeadRot, partialTicks);
			float netHeadYaw = headYaw - bodyYaw;
			float pitch = entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTicks;
			this.renderLivingAt(entity, x, y, z);
			float age = this.handleRotationFloat(entity, partialTicks);
			this.applyRotations(entity, age, bodyYaw, partialTicks);
			float scale = this.prepareScale(entity, partialTicks);
			float swingAmount = 0.0F;
			float swing = 0.0F;
			if (!entity.isPassenger()) {
				swingAmount = entity.animationSpeedOld + (entity.animationSpeed - entity.animationSpeedOld) * partialTicks;
				swing = entity.animationPosition - entity.animationSpeed * (1.0F - partialTicks);
				if (entity.isBaby())
					swing *= 3.0F;
				if (swingAmount > 1.0F)
					swingAmount = 1.0F;
			}
			this.mainModel.setLivingAnimations(entity, swing, swingAmount, partialTicks);
			this.mainModel.setRotationAngles(swing, swingAmount, age, netHeadYaw, pitch, scale, entity);
			LegacyRender.setOverlay(LivingEntityRenderer.getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks)));
			this.renderModel(entity, swing, swingAmount, age, netHeadYaw, pitch, scale);
			this.renderLayers(entity, swing, swingAmount, partialTicks, age, netHeadYaw, pitch, scale);
		} catch (Exception e) {
			e.printStackTrace();
		}
		GlStateManager.popMatrix();
	}

	protected float getWhiteOverlayProgress(T entity, float partialTicks) {
		return 0.0F;
	}

	public float prepareScale(T entity, float partialTicks) {
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);
		this.preRenderCallback(entity, partialTicks);
		GlStateManager.translate(0.0F, -1.501F, 0.0F);
		return 0.0625F;
	}

	protected void renderModel(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {
		boolean visible = !entity.isInvisible();
		if (!visible)
			return;
		if (!this.bindEntityTexture(entity))
			return;
		this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
	}

	protected void renderLivingAt(T entity, double x, double y, double z) {
		GlStateManager.translate((float) x, (float) y, (float) z);
	}

	protected void applyRotations(T entity, float ageInTicks, float rotationYaw, float partialTicks) {
		GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
		if (entity.deathTime > 0) {
			float f = (entity.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
			f = Mth.sqrt(f);
			if (f > 1.0F)
				f = 1.0F;
			GlStateManager.rotate(f * this.getDeathMaxRotation(entity), 0.0F, 0.0F, 1.0F);
		}
	}

	protected float getSwingProgress(T entity, float partialTicks) {
		return entity.getAttackAnim(partialTicks);
	}

	protected float handleRotationFloat(T entity, float partialTicks) {
		return entity.tickCount + partialTicks;
	}

	protected void renderLayers(T entity, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		for (LayerRenderer<T> layer : this.layerRenderers) {
			GlStateManager.pushMatrix();
			layer.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch,
					scale);
			GlStateManager.popMatrix();
		}
	}

	protected float getDeathMaxRotation(T entity) {
		return 90.0F;
	}

	protected void preRenderCallback(T entity, float partialTickTime) {
	}
}
