package rafradek.TF2weapons.client.compat;

import com.mojang.blaze3d.platform.Lighting;

/** 1.12 RenderHelper replacement. */
public final class RenderHelper {
	public static void enableStandardItemLighting() {
		Lighting.setupFor3DItems();
	}

	public static void disableStandardItemLighting() {
	}

	public static void enableGUIStandardItemLighting() {
		Lighting.setupForFlatItems();
	}
}
