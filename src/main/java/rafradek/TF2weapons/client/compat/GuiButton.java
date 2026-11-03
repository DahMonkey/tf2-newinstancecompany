package rafradek.TF2weapons.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * 1.12-style button: has an id and a display string, and clicking it calls the owning legacy
 * screen's actionPerformed. Subclasses can override {@link #drawButton} as in 1.12.
 */
public class GuiButton extends Button {
	public int id;
	public String displayString;
	public boolean enabled = true;
	public boolean hovered;

	public GuiButton(int id, int x, int y, int width, int height, String text) {
		super(x, y, width, height, Component.literal(text), GuiButton::onLegacyPress);
		this.id = id;
		this.displayString = text;
	}

	public GuiButton(int id, int x, int y, String text) {
		this(id, x, y, 200, 20, text);
	}

	private static void onLegacyPress(Button button) {
		if (Minecraft.getInstance().screen instanceof LegacyScreenAccess)
			((LegacyScreenAccess) Minecraft.getInstance().screen).legacyActionPerformed((GuiButton) button);
	}

	public int getButtonWidth() {
		return this.width;
	}

	public int getButtonHeight() {
		return this.height;
	}

	public void setButtonWidth(int w) {
		this.width = w;
	}

	public boolean isMouseOver() {
		return this.isHovered;
	}

	@Override
	public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		this.active = this.enabled;
		this.hovered = this.isHovered;
		if (!this.getMessage().getString().equals(this.displayString))
			this.setMessage(Component.literal(this.displayString));
		LegacyRender.begin(pose, null, net.minecraft.client.renderer.LightTexture.FULL_BRIGHT);
		try {
			this.drawButton(Minecraft.getInstance(), mouseX, mouseY, partialTicks);
		} finally {
			LegacyRender.end();
		}
	}

	/** Default look; override for custom buttons (1.12 drawButton). */
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		super.renderButton(LegacyRender.pose(), mouseX, mouseY, partialTicks);
	}

	/** 1.12 helper: textured rectangle in the current pose. */
	public void drawTexturedModalRect(int x, int y, int u, int v, int w, int h) {
		this.blit(LegacyRender.pose(), x, y, u, v, w, h);
	}

	public void drawCenteredString(LegacyFont font, String text, int x, int y, int color) {
		drawCenteredString(LegacyRender.pose(), font.font, text, x, y, color);
	}
}
