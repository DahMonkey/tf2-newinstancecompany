package rafradek.TF2weapons.client.compat;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 1.12 GuiScreen API on top of a 1.19 Screen. */
public abstract class GuiScreen extends Screen implements LegacyScreenAccess {
	public Minecraft mc;
	public LegacyFont fontRenderer;
	protected RenderItem itemRender = new RenderItem();
	public final List<GuiButton> buttonList = new LegacyButtonList(this);
	protected PoseStack currentPose = new PoseStack();
	protected float zLevel;

	protected GuiScreen() {
		super(Component.empty());
	}

	protected GuiScreen(Component title) {
		super(title);
	}

	@Override
	protected void init() {
		this.mc = this.minecraft;
		this.fontRenderer = new LegacyFont(this.font);
		this.buttonList.clear();
		this.initGui();
	}

	public void initGui() {
	}

	void addLegacyButton(GuiButton button) {
		this.addRenderableWidget(button);
	}

	void removeLegacyButton(GuiButton button) {
		this.removeWidget(button);
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		this.currentPose = pose;
		LegacyRender.begin(pose, null, LightTexture.FULL_BRIGHT);
		try {
			this.drawScreen(mouseX, mouseY, partialTicks);
		} finally {
			LegacyRender.end();
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.render(this.currentPose, mouseX, mouseY, partialTicks);
	}

	public void drawDefaultBackground() {
		this.renderBackground(this.currentPose);
	}

	@Override
	public void legacyActionPerformed(GuiButton button) {
		try {
			this.actionPerformed(button);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void actionPerformed(GuiButton button) throws IOException {
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean handled = super.mouseClicked(mouseX, mouseY, button);
		try {
			this.mouseClicked((int) mouseX, (int) mouseY, button);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return handled || true;
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.mouseReleased((int) mouseX, (int) mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}

	protected void mouseReleased(int mouseX, int mouseY, int state) {
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		this.handleMouseWheel((int) Math.signum(delta) * 120);
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	/** Replacement for reading Mouse.getEventDWheel() in handleMouseInput. */
	protected void handleMouseWheel(int delta) {
	}

	@Override
	public boolean charTyped(char c, int modifiers) {
		try {
			this.keyTyped(c, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.charTyped(c, modifiers);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode != InputConstants.KEY_ESCAPE) {
			try {
				this.keyTyped((char) 0, keyCode);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	/** keyCode uses GLFW key codes in 1.19. */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
	}

	@Override
	public void tick() {
		this.updateScreen();
	}

	public void updateScreen() {
	}

	@Override
	public void removed() {
		this.onGuiClosed();
	}

	public void onGuiClosed() {
	}

	@Override
	public boolean isPauseScreen() {
		return this.doesGuiPauseGame();
	}

	public boolean doesGuiPauseGame() {
		return true;
	}

	/* drawing helpers */

	public void drawTexturedModalRect(int x, int y, int u, int v, int w, int h) {
		this.blit(LegacyRender.pose(), x, y, u, v, w, h);
	}

	public void drawCenteredString(LegacyFont font, String text, int x, int y, int color) {
		drawCenteredString(LegacyRender.pose(), font.font, text, x, y, color);
	}

	public void drawString(LegacyFont font, String text, int x, int y, int color) {
		drawString(LegacyRender.pose(), font.font, text, x, y, color);
	}

	public static void drawRect(int left, int top, int right, int bottom, int color) {
		fill(LegacyRender.pose(), left, top, right, bottom, color);
	}

	public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
		this.fillGradient(LegacyRender.pose(), left, top, right, bottom, startColor, endColor);
	}

	public static void drawModalRectWithCustomSizedTexture(int x, int y, float u, float v, int width, int height,
			float textureWidth, float textureHeight) {
		blit(LegacyRender.pose(), x, y, u, v, width, height, (int) textureWidth, (int) textureHeight);
	}

	public void renderToolTip(ItemStack stack, int x, int y) {
		this.renderTooltip(LegacyRender.pose(), stack, x, y);
	}

	public void drawHoveringText(String text, int x, int y) {
		this.renderTooltip(LegacyRender.pose(), Component.literal(text), x, y);
	}

	public void drawHoveringText(List<String> lines, int x, int y) {
		List<Component> components = new ArrayList<>();
		for (String line : lines)
			components.add(Component.literal(line));
		this.renderComponentTooltip(LegacyRender.pose(), components, x, y);
	}

	public void bindTexture(ResourceLocation texture) {
		RenderSystem.setShaderTexture(0, texture);
	}

	public static boolean isShiftKeyDown() {
		return Screen.hasShiftDown();
	}

	public static boolean isCtrlKeyDown() {
		return Screen.hasControlDown();
	}
}
