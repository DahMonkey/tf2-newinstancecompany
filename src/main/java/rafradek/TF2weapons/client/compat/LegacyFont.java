package rafradek.TF2weapons.client.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;

/** 1.12 FontRenderer API over the 1.19 Font, drawing into the current legacy pose. */
public class LegacyFont {
	public final Font font;
	public final int lineHeight;
	public final int FONT_HEIGHT;

	public LegacyFont(Font font) {
		this.font = font;
		this.lineHeight = font.lineHeight;
		this.FONT_HEIGHT = font.lineHeight;
	}

	public static LegacyFont of(net.minecraft.client.Minecraft mc) {
		return new LegacyFont(mc.font);
	}

	public int drawString(String text, float x, float y, int color) {
		return font.draw(LegacyRender.pose(), text, x, y, color);
	}

	public int drawString(String text, float x, float y, int color, boolean shadow) {
		return shadow ? font.drawShadow(LegacyRender.pose(), text, x, y, color)
				: font.draw(LegacyRender.pose(), text, x, y, color);
	}

	public int drawStringWithShadow(String text, float x, float y, int color) {
		return font.drawShadow(LegacyRender.pose(), text, x, y, color);
	}

	public void drawSplitString(String text, int x, int y, int wrapWidth, int color) {
		font.drawWordWrap(FormattedText.of(text), x, y, wrapWidth, color);
	}

	public int width(String text) {
		return font.width(text);
	}

	public int getStringWidth(String text) {
		return font.width(text);
	}

	public List<String> listFormattedStringToWidth(String text, int width) {
		List<String> lines = new ArrayList<>();
		font.getSplitter().splitLines(text, width, net.minecraft.network.chat.Style.EMPTY)
				.forEach(line -> lines.add(line.getString()));
		return lines;
	}

	public String trimStringToWidth(String text, int width) {
		return font.plainSubstrByWidth(text, width);
	}

	public int getStringWidth(Component text) {
		return font.width(text);
	}
}
