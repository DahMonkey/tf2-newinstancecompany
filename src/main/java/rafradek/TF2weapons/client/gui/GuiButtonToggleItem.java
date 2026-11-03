package rafradek.TF2weapons.client.gui;

import rafradek.TF2weapons.client.compat.GuiButton;

import rafradek.TF2weapons.client.compat.GlStateManager;
import rafradek.TF2weapons.client.compat.RenderHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class GuiButtonToggleItem extends GuiButton {

	public boolean selected;
	public ItemStack stackToDraw = ItemStack.EMPTY;

	public GuiButtonToggleItem(int buttonId, int x, int y, int widthIn, int heightIn) {
		super(buttonId, x, y, widthIn, heightIn, "");
	}

	@Override
	protected int getYImage(boolean mouseOver) {
		int i = 1;
		if (!enabled) i = 0;
		else if (mouseOver || selected) i = 2;
		return i;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible || stackToDraw == null || stackToDraw.isEmpty()) return;
		super.drawButton(mc, mouseX, mouseY, partialTicks);
		rafradek.TF2weapons.client.compat.LegacyGui.renderItemAndEffectIntoGUI(stackToDraw, x + 1, y + 1);
		rafradek.TF2weapons.client.compat.LegacyGui.renderItemOverlays(mc.font, stackToDraw, x + 1, y + 1);
	}

	@Override
	protected boolean clicked(double mouseX, double mouseY) {
		return super.clicked(mouseX, mouseY) && !stackToDraw.isEmpty();
	}
}
