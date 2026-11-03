package rafradek.TF2weapons.client.compat;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** 1.12 GuiTextField over the 1.19 EditBox. */
public class GuiTextField extends EditBox {
	public final int id;

	public GuiTextField(int id, LegacyFont font, int x, int y, int width, int height) {
		super(font.font, x, y, width, height, Component.empty());
		this.id = id;
	}

	public String getText() {
		return this.getValue();
	}

	public void setText(String text) {
		this.setValue(text);
	}

	public void drawTextBox() {
		this.render(LegacyRender.pose(), 0, 0, 0);
	}

	public boolean textboxKeyTyped(char typedChar, int keyCode) {
		return typedChar != 0 ? this.charTyped(typedChar, 0) : this.keyPressed(keyCode, 0, 0);
	}

	public void setFocused(boolean focused) {
		this.setFocus(focused);
	}

	public boolean isFocused() {
		return super.isFocused();
	}

	public void setMaxStringLength(int length) {
		this.setMaxLength(length);
	}

	public void setEnableBackgroundDrawing(boolean draw) {
		this.setBordered(draw);
	}

	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		return super.mouseClicked(mouseX, mouseY, button);
	}
}
