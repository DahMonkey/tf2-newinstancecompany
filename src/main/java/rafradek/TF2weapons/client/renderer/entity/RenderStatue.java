package rafradek.TF2weapons.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.EntityStatue;

/** Australium statues (gold-tinted copies) and the Dead Ringer's fake corpse. */
public class RenderStatue extends EntityRenderer<EntityStatue> {

	public RenderStatue(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityStatue entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	@Override
	public void render(EntityStatue entity, float entityYaw, float partialTicks, PoseStack pose,
			MultiBufferSource buffers, int light) {
		if (entity.entity == null)
			return;
		MultiBufferSource target = buffers;
		if (entity.isFeign) {
			entity.entity.setInvisible(false);
			if (entity.entity.getCapability(rafradek.TF2weapons.TF2weapons.WEAPONS_CAP).isPresent())
				WeaponsCapability.get(entity.entity).setInvisible(false);
			entity.entity.animationSpeed = 0;
			entity.entity.yBodyRot = entity.yBodyRot;
			entity.entity.deathTime = entity.tickCount;
		} else {
			target = new MultiBufferSource() {
				@Override
				public VertexConsumer getBuffer(net.minecraft.client.renderer.RenderType type) {
					return new TintedConsumer(buffers.getBuffer(type), 1.0f, 0.8f, 0.0f);
				}
			};
		}
		this.entityRenderDispatcher.render(entity.entity, 0, 0, 0, entityYaw, entity.isFeign ? partialTicks : 1,
				pose, target, light);
	}

	/** Multiplies every vertex color, giving the australium gold look. */
	private static class TintedConsumer implements VertexConsumer {
		private final VertexConsumer parent;
		private final float r, g, b;

		TintedConsumer(VertexConsumer parent, float r, float g, float b) {
			this.parent = parent;
			this.r = r;
			this.g = g;
			this.b = b;
		}

		@Override
		public VertexConsumer vertex(double x, double y, double z) {
			parent.vertex(x, y, z);
			return this;
		}

		@Override
		public VertexConsumer color(int red, int green, int blue, int alpha) {
			parent.color((int) (red * r), (int) (green * g), (int) (blue * b), alpha);
			return this;
		}

		@Override
		public VertexConsumer uv(float u, float v) {
			parent.uv(u, v);
			return this;
		}

		@Override
		public VertexConsumer overlayCoords(int u, int v) {
			parent.overlayCoords(u, v);
			return this;
		}

		@Override
		public VertexConsumer uv2(int u, int v) {
			parent.uv2(u, v);
			return this;
		}

		@Override
		public VertexConsumer normal(float x, float y, float z) {
			parent.normal(x, y, z);
			return this;
		}

		@Override
		public void endVertex() {
			parent.endVertex();
		}

		@Override
		public void defaultColor(int red, int green, int blue, int alpha) {
			parent.defaultColor((int) (red * r), (int) (green * g), (int) (blue * b), alpha);
		}

		@Override
		public void unsetDefaultColor() {
			parent.unsetDefaultColor();
		}
	}
}
