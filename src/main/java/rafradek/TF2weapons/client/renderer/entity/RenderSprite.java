package rafradek.TF2weapons.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/** Camera-facing sprite from the block atlas (like the vanilla thrown item renderer). */
public class RenderSprite<T extends Entity> extends EntityRenderer<T> {

	private TextureAtlasSprite sprite;

	private float scale;

	public RenderSprite(EntityRendererProvider.Context context, float scale, TextureAtlasSprite sprite) {
		super(context);
		this.scale = scale;
		this.sprite = sprite;
	}

	protected TextureAtlasSprite getSprite(T entity) {
		return sprite;
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	@Override
	public void render(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers,
			int light) {
		TextureAtlasSprite textureatlassprite = getSprite(entity);
		if (textureatlassprite != null) {
			pose.pushPose();
			pose.scale(this.scale, this.scale, this.scale);
			pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
			pose.mulPose(Vector3f.YP.rotationDegrees(180.0F));
			float f = textureatlassprite.getU0();
			float f1 = textureatlassprite.getU1();
			float f2 = textureatlassprite.getV0();
			float f3 = textureatlassprite.getV1();
			PoseStack.Pose last = pose.last();
			Matrix4f matrix = last.pose();
			Matrix3f normal = last.normal();
			VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
			vertex(consumer, matrix, normal, light, -0.5F, -0.25F, f, f3);
			vertex(consumer, matrix, normal, light, 0.5F, -0.25F, f1, f3);
			vertex(consumer, matrix, normal, light, 0.5F, 0.75F, f1, f2);
			vertex(consumer, matrix, normal, light, -0.5F, 0.75F, f, f2);
			pose.popPose();
		}
		super.render(entity, yaw, partialTick, pose, buffers, light);
	}

	private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, int light, float x, float y,
			float u, float v) {
		consumer.vertex(matrix, x, y, 0.0F).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(light).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
	}
}
