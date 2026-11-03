package rafradek.TF2weapons.client.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.LightTexture;

/** The parts of 1.12's OpenGlHelper the mod used. */
public final class OpenGlHelper {
	public static final int defaultTexUnit = 33984;
	public static final int lightmapTexUnit = 33986;
	public static final int GL_TEXTURE2 = 33986;
	public static final int GL_COMBINE = 34160;
	public static final int GL_COMBINE_RGB = 34161;
	public static final int GL_COMBINE_ALPHA = 34162;
	public static final int GL_SOURCE0_RGB = 34176;
	public static final int GL_SOURCE1_RGB = 34177;
	public static final int GL_SOURCE2_RGB = 34178;
	public static final int GL_SOURCE0_ALPHA = 34184;
	public static final int GL_SOURCE1_ALPHA = 34185;
	public static final int GL_SOURCE2_ALPHA = 34186;
	public static final int GL_OPERAND0_RGB = 34192;
	public static final int GL_OPERAND1_RGB = 34193;
	public static final int GL_OPERAND2_RGB = 34194;
	public static final int GL_OPERAND0_ALPHA = 34200;
	public static final int GL_OPERAND1_ALPHA = 34201;
	public static final int GL_OPERAND2_ALPHA = 34202;
	public static final int GL_PREVIOUS = 34168;
	public static final int GL_CONSTANT = 34166;
	public static final int GL_PRIMARY_COLOR = 34167;
	public static final int GL_INTERPOLATE = 34165;

	public static void glBlendFunc(int a, int b, int c, int d) {
		RenderSystem.blendFuncSeparate(a, b, c, d);
	}

	public static void setLightmapTextureCoords(int target, float u, float v) {
		LegacyRender.setLight(LightTexture.pack((int) u >> 4, (int) v >> 4));
	}

	public static void setActiveTexture(int unit) {
	}

	public static boolean isFramebufferEnabled() {
		return true;
	}

	public static boolean shadersSupported = true;
}
