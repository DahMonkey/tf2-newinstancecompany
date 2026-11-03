package rafradek.TF2weapons.client.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Static stand-ins for the 1.12 Gui/FontRenderer/RenderItem drawing calls used by HUD code, drawing
 * with the current {@link LegacyRender} pose.
 */
public final class LegacyGui {

	private static final GuiComponent COMPONENT = new GuiComponent() {
	};

	private LegacyGui() {
	}

	public static int drawString(Font font, String text, float x, float y, int color) {
		return font.drawShadow(LegacyRender.pose(), text, x, y, color);
	}

	public static int drawStringNoShadow(Font font, String text, float x, float y, int color) {
		return font.draw(LegacyRender.pose(), text, x, y, color);
	}

	public static void drawCenteredString(Font font, String text, int x, int y, int color) {
		GuiComponent.drawCenteredString(LegacyRender.pose(), font, text, x, y, color);
	}

	public static void drawTexturedModalRect(int x, int y, int u, int v, int w, int h) {
		COMPONENT.blit(LegacyRender.pose(), x, y, u, v, w, h);
	}

	public static void drawTexturedModalRect(float x, float y, int u, int v, int w, int h) {
		LegacyRender.pose().pushPose();
		LegacyRender.pose().translate(x - (int) x, y - (int) y, 0);
		COMPONENT.blit(LegacyRender.pose(), (int) x, (int) y, u, v, w, h);
		LegacyRender.pose().popPose();
	}

	public static void drawRect(int left, int top, int right, int bottom, int color) {
		GuiComponent.fill(LegacyRender.pose(), left, top, right, bottom, color);
	}

	public static void renderItemAndEffectIntoGUI(LivingEntity entity, ItemStack stack, int x, int y) {
		Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(entity, stack, x, y, 0);
	}

	public static void renderItemAndEffectIntoGUI(ItemStack stack, int x, int y) {
		Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, x, y);
	}

	public static void renderItemOverlays(Font font, ItemStack stack, int x, int y) {
		Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(font, stack, x, y);
	}

	public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, int width, int height,
			float textureWidth, float textureHeight) {
		GuiComponent.blit(LegacyRender.pose(), (int) x, (int) y, u, v, width, height, (int) textureWidth,
				(int) textureHeight);
	}
}
