package rafradek.TF2weapons.client.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * Stand-in for 1.12's GlStateManager. Matrix calls act on {@link LegacyRender#pose()}; colour and
 * blend state are recorded for the legacy model renderer and mirrored to RenderSystem; fixed-function
 * features that no longer exist (lighting, texture env, rescale normal...) are no-ops.
 */
public final class GlStateManager {

	public static void pushMatrix() {
		LegacyRender.pose().pushPose();
	}

	public static void popMatrix() {
		LegacyRender.pose().popPose();
	}

	public static void translate(double x, double y, double z) {
		LegacyRender.pose().translate(x, y, z);
	}

	public static void translate(float x, float y, float z) {
		LegacyRender.pose().translate(x, y, z);
	}

	public static void scale(double x, double y, double z) {
		LegacyRender.pose().scale((float) x, (float) y, (float) z);
	}

	public static void scale(float x, float y, float z) {
		LegacyRender.pose().scale(x, y, z);
	}

	public static void rotate(float angle, float x, float y, float z) {
		LegacyRender.pose().mulPose(new Vector3f(x, y, z).rotationDegrees(angle));
	}

	public static void loadIdentity() {
		LegacyRender.pose().setIdentity();
	}

	public static void matrixMode(int mode) {
	}

	public static void color(float r, float g, float b, float a) {
		LegacyRender.setColor(r, g, b, a);
		RenderSystem.setShaderColor(r, g, b, a);
	}

	public static void color(float r, float g, float b) {
		color(r, g, b, 1.0F);
	}

	public static void resetColor() {
		color(1, 1, 1, 1);
	}

	public static void enableBlend() {
		LegacyRender.setBlend(true);
		RenderSystem.enableBlend();
	}

	public static void disableBlend() {
		LegacyRender.setBlend(false);
		RenderSystem.disableBlend();
	}

	public static void blendFunc(SourceFactor src, DestFactor dst) {
		RenderSystem.blendFunc(src.factor, dst.factor);
	}

	public static void blendFunc(int src, int dst) {
		RenderSystem.blendFunc(src, dst);
	}

	public static void tryBlendFuncSeparate(SourceFactor a, DestFactor b, SourceFactor c, DestFactor d) {
		RenderSystem.blendFuncSeparate(a.factor, b.factor, c.factor, d.factor);
	}

	public static void tryBlendFuncSeparate(int a, int b, int c, int d) {
		RenderSystem.blendFuncSeparate(a, b, c, d);
	}

	public static void depthMask(boolean flag) {
		RenderSystem.depthMask(flag);
	}

	public static void depthFunc(int func) {
		RenderSystem.depthFunc(func);
	}

	public static void enableDepth() {
		RenderSystem.enableDepthTest();
	}

	public static void disableDepth() {
		RenderSystem.disableDepthTest();
	}

	public static void enableCull() {
		RenderSystem.enableCull();
	}

	public static void disableCull() {
		RenderSystem.disableCull();
	}

	public static void enableTexture2D() {
		RenderSystem.enableTexture();
	}

	public static void disableTexture2D() {
		RenderSystem.disableTexture();
	}

	public static void enableTexture() {
		RenderSystem.enableTexture();
	}

	public static void disableTexture() {
		RenderSystem.disableTexture();
	}

	public static void bindTexture(int id) {
		RenderSystem.setShaderTexture(0, id);
	}

	public static void bindTexture(ResourceLocation location) {
		LegacyRender.setTexture(location);
		RenderSystem.setShaderTexture(0, location);
	}

	/* Fixed-function state that no longer exists in 1.19's core-profile renderer. */
	public static void enableLighting() {
	}

	public static void disableLighting() {
	}

	public static void enableRescaleNormal() {
	}

	public static void disableRescaleNormal() {
	}

	public static void enableColorMaterial() {
	}

	public static void disableColorMaterial() {
	}

	public static void enableAlpha() {
	}

	public static void disableAlpha() {
	}

	public static void alphaFunc(int func, float ref) {
	}

	public static void enableOutlineMode(int color) {
	}

	public static void disableOutlineMode() {
	}

	public static void setActiveTexture(int unit) {
	}

	public static void glTexEnvi(int a, int b, int c) {
	}

	public static void glTexEnv(int a, int b, java.nio.FloatBuffer c) {
	}

	public static void glNormal3f(float x, float y, float z) {
	}

	public static void shadeModel(int mode) {
	}

	public static void enableNormalize() {
	}

	public static void disableNormalize() {
	}

	public static void enablePolygonOffset() {
		RenderSystem.enablePolygonOffset();
	}

	public static void disablePolygonOffset() {
		RenderSystem.disablePolygonOffset();
	}

	public static void doPolygonOffset(float factor, float units) {
		RenderSystem.polygonOffset(factor, units);
	}

	public static void glLineWidth(float width) {
		RenderSystem.lineWidth(width);
	}

	public static void colorMask(boolean r, boolean g, boolean b, boolean a) {
		RenderSystem.colorMask(r, g, b, a);
	}

	public static void clear(int mask) {
		RenderSystem.clear(mask, Minecraft.ON_OSX);
	}

	public enum SourceFactor {
		CONSTANT_ALPHA(32771), CONSTANT_COLOR(32769), DST_ALPHA(772), DST_COLOR(774), ONE(1),
		ONE_MINUS_CONSTANT_ALPHA(32772), ONE_MINUS_CONSTANT_COLOR(32770), ONE_MINUS_DST_ALPHA(773),
		ONE_MINUS_DST_COLOR(775), ONE_MINUS_SRC_ALPHA(771), ONE_MINUS_SRC_COLOR(769), SRC_ALPHA(770),
		SRC_ALPHA_SATURATE(776), SRC_COLOR(768), ZERO(0);

		public final int factor;

		SourceFactor(int factor) {
			this.factor = factor;
		}
	}

	public enum DestFactor {
		CONSTANT_ALPHA(32771), CONSTANT_COLOR(32769), DST_ALPHA(772), DST_COLOR(774), ONE(1),
		ONE_MINUS_CONSTANT_ALPHA(32772), ONE_MINUS_CONSTANT_COLOR(32770), ONE_MINUS_DST_ALPHA(773),
		ONE_MINUS_DST_COLOR(775), ONE_MINUS_SRC_ALPHA(771), ONE_MINUS_SRC_COLOR(769), SRC_ALPHA(770), SRC_COLOR(768),
		ZERO(0);

		public final int factor;

		DestFactor(int factor) {
			this.factor = factor;
		}
	}
}
