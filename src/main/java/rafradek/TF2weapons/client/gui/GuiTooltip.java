package rafradek.TF2weapons.client.gui;

import rafradek.TF2weapons.client.compat.GuiButton;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class GuiTooltip extends GuiButton {

	private Screen screen;
	private List<String> list;

	public GuiTooltip(int buttonId, int x, int y, int widthIn, int heightIn, List<String> text, Screen screen) {
		super(buttonId, x, y, widthIn, heightIn, text.get(0));
		this.screen = screen;
		list = text;
		enabled = false;
	}

	public GuiTooltip(int x, int y, int widthIn, int heightIn, String text, Screen screen) {
		this(99999, x, y, widthIn, heightIn, Arrays.asList(text.split(Pattern.quote("\\n"))), screen);
	}

	public void setText(String string) {
		list = Arrays.asList(string.split(Pattern.quote("\\n")));
		displayString = string;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible) return;
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		if (hovered) {
			java.util.List<net.minecraft.network.chat.Component> lines = new java.util.ArrayList<>();
			for (String line : list)
				lines.add(net.minecraft.network.chat.Component.literal(line));
			screen.renderComponentTooltip(rafradek.TF2weapons.client.compat.LegacyRender.pose(), lines, mouseX, mouseY);
		}
	}
}
