package rafradek.TF2weapons.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;

public class GuiButtonToggleItem extends Button {

	public boolean selected;
	public ItemStack stackToDraw = ItemStack.EMPTY;

	public GuiButtonToggleItem(int buttonId, int x, int y, int widthIn, int heightIn) {
		super(buttonId, x, y, widthIn, heightIn, "");
	}

	@Override
	protected int getHoverState(boolean mouseOver) {
		int i = 1;
		if (!enabled) i = 0;
		else if (mouseOver || selected) i = 2;
		return i;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible || stackToDraw == null || stackToDraw.isEmpty()) return;
		super.drawButton(mc, mouseX, mouseY, partialTicks);
		zLevel = 100.0F;
		mc.getItemRenderer().zLevel = 100.0F;
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.enableLighting();
		GlStateManager.enableRescaleNormal();
		mc.getItemRenderer().renderItemAndEffectIntoGUI(stackToDraw, x + 1, y + 1);
		mc.getItemRenderer().renderItemOverlays(mc.font, stackToDraw, x + 1, y + 1);
		GlStateManager.disableLighting();
		RenderHelper.disableStandardItemLighting();
		mc.getItemRenderer().zLevel = 0.0F;
		zLevel = 0.0F;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		return super.mousePressed(mc, mouseX, mouseY) && !stackToDraw.isEmpty();
	}
}
