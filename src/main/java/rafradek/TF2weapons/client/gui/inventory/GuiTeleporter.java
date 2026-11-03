package rafradek.TF2weapons.client.gui.inventory;

import rafradek.TF2weapons.client.compat.GuiContainer;
import rafradek.TF2weapons.client.compat.GuiButton;
import rafradek.TF2weapons.client.compat.GuiTextField;

import rafradek.TF2weapons.client.compat.GlStateManager;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.inventory.ContainerEnergy;
import rafradek.TF2weapons.message.TF2Message;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GuiTeleporter extends GuiContainer {

	public EntityTeleporter teleporter;
	public GuiButton doneBtn;
	public GuiButton teleportUpBtn;
	public GuiButton teleportDownBtn;
	public GuiButton exitToggle;
	public GuiButton grab;
	private GuiTextField teleportField;
	public int channel;
	public boolean exit;

	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/container/building.png");

	public GuiTeleporter(ContainerEnergy container) {
		super(container);
		EntityTeleporter teleporter = (EntityTeleporter) container.building;
		this.teleporter = teleporter;
		exit = teleporter.isExit();
		channel = teleporter.getID();
		imageWidth = 212;
		imageHeight = 195;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		teleportField = new GuiTextField(5, fontRenderer, width / 2 - 40, height / 2 - 40, 30, 20);
		teleportField.setMaxStringLength(3);
		teleportField.setFocused(true);
		teleportField.setText(Integer.toString(channel + 1));
		buttonList.add(teleportUpBtn = new GuiButton(1, width / 2 - 60, height / 2 - 40, 20, 20, "+"));
		buttonList.add(teleportDownBtn = new GuiButton(2, width / 2 - 10, height / 2 - 40, 20, 20, "-"));
		buttonList.add(exitToggle = new GuiButton(3, width / 2 + 10, height / 2 - 40, 50, 20, "Exit"));
		buttonList.add(grab = new GuiButton(4, leftPos + 86, topPos + 90, 40, 20, I18n.get("gui.teleporter.drop", new Object[0])));
		if (channel == 127) {
			teleportField.setEditable(false);
			teleportUpBtn.enabled = false;
			teleportDownBtn.enabled = false;
			exitToggle.enabled = false;
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled) return;
		if (button.id == 1) {
			channel++;
			if (channel >= EntityTeleporter.TP_PER_PLAYER - 1) channel = 0;
			teleportField.setText(Integer.toString(channel + 1));
		} else if (button.id == 2) {
			channel--;
			if (channel < 0) channel = EntityTeleporter.TP_PER_PLAYER - 2;
			teleportField.setText(Integer.toString(channel + 1));
		} else if (button.id == 3 && teleporter.isExit()) exit = !exit;
		else if (button.id == 4) {
			mc.setScreen(null);
			TF2weapons.network.sendToServer(new TF2Message.BuildingConfigMessage(teleporter.getId(), (byte) 127, 0));
		}
	}

	@Override
	public void onGuiClosed() {
		TF2weapons.network.sendToServer(new TF2Message.BuildingConfigMessage(teleporter.getId(), (byte) 0, channel));
		TF2weapons.network.sendToServer(new TF2Message.BuildingConfigMessage(teleporter.getId(), (byte) 1, exit ? 1 : 0));
		super.onGuiClosed();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		teleportField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		teleportField.textboxKeyTyped(typedChar, keyCode);
		if (channel == 127) return;
		try {
			channel = Mth.clamp(Integer.parseInt(this.teleportField.getText()) - 1, 0, EntityTeleporter.TP_PER_PLAYER - 2);
		} catch (Exception ex) {}
		// this.teleportField.setText(Integer.toString(channel));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		fontRenderer.drawString(I18n.get("gui.teleporter.info", new Object[0]), 8, 5, 4210752);
		fontRenderer.drawString(I18n.get("container.inventory", new Object[0]), 25, 99, 4210752);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		// this.drawCenteredString(this.fontRendererObj, Integer.toString(channel),
		// this.width / 2 - 25, this.height / 2,
		// 16777215);
		exitToggle.displayString = exit ? "Exit" : "Entry";
		super.drawScreen(mouseX, mouseY, partialTicks);
		teleportField.drawTextBox();
		if (mouseX >= leftPos + 7 && mouseX < leftPos + 23 && mouseY >= topPos + 15 && mouseY < topPos + 75) {
			if (ClientProxy.buildingsUseEnergy) drawHoveringText("Energy: " + teleporter.getInfoEnergy() + "/" + teleporter.energy.getMaxEnergyStored(), mouseX, mouseY);
			else drawHoveringText("Energy use is disabled", mouseX, mouseY);
		}
		if (mouseX >= leftPos + 5 && mouseX < leftPos + 23 && mouseY >=topPos + 112 && mouseY < topPos + 130) drawHoveringText(
					Arrays.asList(I18n.get("gui.teleporter.help", new Object[0]).split(Pattern.quote("\\n"))), mouseX, mouseY);
		// System.out.println("dfs");
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		teleportField.tick();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI_TEXTURES);
		int x = (width - imageWidth) / 2;
		int y = (height - imageHeight) / 2;
		drawTexturedModalRect(x, y, 0, 0, imageWidth, imageHeight);
		this.drawGradientRect(leftPos + 7, topPos + 75 - (int) (((float) teleporter.getInfoEnergy() /
						(float) teleporter.energy.getMaxEnergyStored()) * 60f), leftPos + 23, topPos + 75, 0xFFBF0000, 0xFF7F0000);
	}
}
