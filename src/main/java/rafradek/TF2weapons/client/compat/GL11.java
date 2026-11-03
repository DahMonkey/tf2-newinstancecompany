package rafradek.TF2weapons.client.compat;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * The subset of LWJGL2's GL11 the mod used. Matrix functions go to the legacy pose stack; enable and
 * disable only act on state that still exists in 1.19.
 */
public final class GL11 {
	public static final int GL_ALPHA_TEST = 3008;
	public static final int GL_BLEND = 3042;
	public static final int GL_DEPTH_TEST = 2929;
	public static final int GL_TEXTURE_2D = 3553;
	public static final int GL_CULL_FACE = 2884;
	public static final int GL_LIGHTING = 2896;
	public static final int GL_QUADS = 7;
	public static final int GL_TRIANGLES = 4;
	public static final int GL_TRIANGLE_STRIP = 5;
	public static final int GL_TRIANGLE_FAN = 6;
	public static final int GL_LINES = 1;
	public static final int GL_LINE_STRIP = 3;
	public static final int GL_POLYGON = 9;
	public static final int GL_SRC_ALPHA = 770;
	public static final int GL_ONE_MINUS_SRC_ALPHA = 771;
	public static final int GL_ONE = 1;
	public static final int GL_ZERO = 0;
	public static final int GL_GREATER = 516;
	public static final int GL_LEQUAL = 515;
	public static final int GL_EQUAL = 514;
	public static final int GL_ALWAYS = 519;
	public static final int GL_SMOOTH = 7425;
	public static final int GL_FLAT = 7424;
	public static final int GL_MODELVIEW = 5888;
	public static final int GL_PROJECTION = 5889;
	public static final int GL_TEXTURE = 5890;
	public static final int GL_COLOR_BUFFER_BIT = 16384;
	public static final int GL_DEPTH_BUFFER_BIT = 256;
	public static final int GL_TEXTURE_ENV = 8960;
	public static final int GL_TEXTURE_ENV_MODE = 8704;
	public static final int GL_TEXTURE_MIN_FILTER = 10241;
	public static final int GL_TEXTURE_MAG_FILTER = 10240;
	public static final int GL_NEAREST = 9728;
	public static final int GL_LINEAR = 9729;
	public static final int GL_RESCALE_NORMAL = 32826;
	public static final int GL_NORMALIZE = 2977;
	public static final int GL_COLOR_MATERIAL = 2903;
	public static final int GL_FOG = 2912;

	public static void glEnable(int cap) {
		switch (cap) {
		case GL_BLEND -> GlStateManager.enableBlend();
		case GL_DEPTH_TEST -> RenderSystem.enableDepthTest();
		case GL_CULL_FACE -> RenderSystem.enableCull();
		case GL_TEXTURE_2D -> RenderSystem.enableTexture();
		default -> {
		}
		}
	}

	public static void glDisable(int cap) {
		switch (cap) {
		case GL_BLEND -> GlStateManager.disableBlend();
		case GL_DEPTH_TEST -> RenderSystem.disableDepthTest();
		case GL_CULL_FACE -> RenderSystem.disableCull();
		case GL_TEXTURE_2D -> RenderSystem.disableTexture();
		default -> {
		}
		}
	}

	public static void glPushMatrix() {
		GlStateManager.pushMatrix();
	}

	public static void glPopMatrix() {
		GlStateManager.popMatrix();
	}

	public static void glTranslatef(float x, float y, float z) {
		GlStateManager.translate(x, y, z);
	}

	public static void glTranslated(double x, double y, double z) {
		GlStateManager.translate(x, y, z);
	}

	public static void glRotatef(float angle, float x, float y, float z) {
		GlStateManager.rotate(angle, x, y, z);
	}

	public static void glScalef(float x, float y, float z) {
		GlStateManager.scale(x, y, z);
	}

	public static void glScaled(double x, double y, double z) {
		GlStateManager.scale(x, y, z);
	}

	public static void glColor4f(float r, float g, float b, float a) {
		GlStateManager.color(r, g, b, a);
	}

	public static void glColor3f(float r, float g, float b) {
		GlStateManager.color(r, g, b);
	}

	public static void glDepthMask(boolean flag) {
		RenderSystem.depthMask(flag);
	}

	public static void glBlendFunc(int src, int dst) {
		RenderSystem.blendFunc(src, dst);
	}

	public static void glDepthFunc(int func) {
		RenderSystem.depthFunc(func);
	}

	public static void glAlphaFunc(int func, float ref) {
	}

	public static void glShadeModel(int mode) {
	}

	public static void glMatrixMode(int mode) {
	}

	public static void glLoadIdentity() {
		GlStateManager.loadIdentity();
	}

	public static void glNormal3f(float x, float y, float z) {
	}

	public static void glLineWidth(float w) {
		RenderSystem.lineWidth(w);
	}

	public static void glTexParameteri(int target, int pname, int param) {
		org.lwjgl.opengl.GL11.glTexParameteri(target, pname, param);
	}

	public static void glClear(int mask) {
		GlStateManager.clear(mask);
	}
}
