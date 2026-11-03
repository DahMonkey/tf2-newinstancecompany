package rafradek.TF2weapons.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;

/**
 * Render context used by the 1.12-style rendering code kept by this port. 1.12 drew with global
 * OpenGL state; 1.19 passes a PoseStack and buffer source around. Renderers call {@link #begin} with
 * the 1.19 objects, after which the legacy {@link GlStateManager}, {@link ModelRenderer} and friends
 * draw into them.
 */
public final class LegacyRender {

	private static final class Frame {
		PoseStack pose;
		MultiBufferSource buffers;
		int light, overlay;
		float r = 1, g = 1, b = 1, a = 1;
		ResourceLocation texture;
		boolean blend;
	}

	private static final ArrayDeque<Frame> STACK = new ArrayDeque<>();
	private static Frame current = newFrame(new PoseStack(), null, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

	private static Frame newFrame(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		Frame f = new Frame();
		f.pose = pose;
		f.buffers = buffers;
		f.light = light;
		f.overlay = overlay;
		return f;
	}

	public static void begin(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		STACK.push(current);
		current = newFrame(pose, buffers, light, overlay);
	}

	public static void begin(PoseStack pose, MultiBufferSource buffers, int light) {
		begin(pose, buffers, light, OverlayTexture.NO_OVERLAY);
	}

	public static void end() {
		if (!STACK.isEmpty())
			current = STACK.pop();
	}

	public static PoseStack pose() {
		return current.pose;
	}

	public static MultiBufferSource buffers() {
		if (current.buffers == null)
			current.buffers = Minecraft.getInstance().renderBuffers().bufferSource();
		return current.buffers;
	}

	public static int light() {
		return current.light;
	}

	public static void setLight(int packed) {
		current.light = packed;
	}

	public static int overlay() {
		return current.overlay;
	}

	public static void setOverlay(int overlay) {
		current.overlay = overlay;
	}

	public static void setColor(float r, float g, float b, float a) {
		current.r = r;
		current.g = g;
		current.b = b;
		current.a = a;
	}

	public static float red() {
		return current.r;
	}

	public static float green() {
		return current.g;
	}

	public static float blue() {
		return current.b;
	}

	public static float alpha() {
		return current.a;
	}

	public static void setTexture(ResourceLocation texture) {
		current.texture = texture;
	}

	public static ResourceLocation texture() {
		return current.texture;
	}

	public static void setBlend(boolean blend) {
		current.blend = blend;
	}

	public static RenderType renderType() {
		ResourceLocation tex = current.texture != null ? current.texture : MISSING;
		return current.blend || current.a < 1 ? RenderType.entityTranslucent(tex) : RenderType.entityCutoutNoCull(tex);
	}

	public static VertexConsumer buffer() {
		return buffers().getBuffer(renderType());
	}

	private static final ResourceLocation MISSING = new ResourceLocation("minecraft", "textures/misc/white.png");
}
