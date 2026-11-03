package rafradek.TF2weapons.client.compat;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/** LWJGL2 Mouse/Keyboard queries the mod used, answered through GLFW. */
public final class LegacyInput {
	public static boolean isMouseButtonDown(int button) {
		return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), button) == GLFW.GLFW_PRESS;
	}

	public static boolean isKeyDown(int glfwKey) {
		return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), glfwKey) == GLFW.GLFW_PRESS;
	}

	public static double getMouseX() {
		return Minecraft.getInstance().mouseHandler.xpos() * Minecraft.getInstance().getWindow().getGuiScaledWidth()
				/ Minecraft.getInstance().getWindow().getScreenWidth();
	}

	public static double getMouseY() {
		return Minecraft.getInstance().mouseHandler.ypos() * Minecraft.getInstance().getWindow().getGuiScaledHeight()
				/ Minecraft.getInstance().getWindow().getScreenHeight();
	}
}
