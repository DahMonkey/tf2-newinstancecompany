package rafradek.TF2weapons.client.compat;

import net.minecraft.client.Minecraft;

/** 1.12 ScaledResolution, read from the 1.19 window. */
public class ScaledResolution {
	private final int width, height;
	private final double scale;

	public ScaledResolution() {
		this(Minecraft.getInstance());
	}

	public ScaledResolution(Minecraft mc) {
		this.width = mc.getWindow().getGuiScaledWidth();
		this.height = mc.getWindow().getGuiScaledHeight();
		this.scale = mc.getWindow().getGuiScale();
	}

	public int getScaledWidth() {
		return width;
	}

	public int getScaledHeight() {
		return height;
	}

	public double getScaledWidth_double() {
		return width;
	}

	public double getScaledHeight_double() {
		return height;
	}

	public int getScaleFactor() {
		return (int) scale;
	}
}
