package rafradek.TF2weapons.client.gui.inventory;

import rafradek.TF2weapons.client.compat.GuiContainer;
import rafradek.TF2weapons.client.compat.GuiButton;

import rafradek.TF2weapons.client.compat.GlStateManager;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.inventory.ContainerDispenser;
import rafradek.TF2weapons.message.TF2Message;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class GuiDispenser extends GuiContainer {

	public EntityDispenser dispenser;
	public GuiButton doneBtn;
	public GuiButton grab;
	public int channel;
	public boolean exit;
	public int attackFlags;

	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID, "textures/gui/container/building.png");

	public GuiDispenser(ContainerDispenser container) {
		super(container);
		EntityDispenser dispenser = (EntityDispenser) container.building;
		this.dispenser = dispenser;
		imageWidth = 212;
		imageHeight = 195;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(this.grab = new GuiButton(0, this.leftPos + 86, this.topPos + 90, 40, 20,
				I18n.get("gui.teleporter.drop", new Object[0])));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled && button.id == 0) {
			mc.setScreen(null);
			TF2weapons.network.sendToServer(new TF2Message.BuildingConfigMessage(dispenser.getId(), (byte) 127, 1));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		fontRenderer.drawString(I18n.get("container.dispenser", new Object[0]), 8, 5, 4210752);
		fontRenderer.drawString(I18n.get("container.inventory", new Object[0]), 25, 99, 4210752);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (mouseX >= leftPos + 7 && mouseX < leftPos + 23 && mouseY >= topPos + 15 && mouseY < topPos + 75) {
			if (ClientProxy.buildingsUseEnergy) drawHoveringText("Energy: " + dispenser.getInfoEnergy() + "/"
							+ dispenser.energy.getMaxEnergyStored(), mouseX, mouseY);
			else drawHoveringText("Energy use is disabled", mouseX, mouseY);
		}
		if (mouseX >= leftPos + 5 && mouseX < leftPos + 23 && mouseY >= topPos + 112 && mouseY < topPos + 130)
			drawHoveringText(Arrays.asList(I18n.get("gui.dispenser.help", new Object[0]).split(Pattern.quote("\\n"))), mouseX, mouseY);
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI_TEXTURES);
		int x = (width - imageWidth) / 2;
		int y = (height - imageHeight) / 2;
		drawTexturedModalRect(x, y, 0, 0, imageWidth, imageHeight);
		drawTexturedModalRect(x + 79, y + 14, 0, 202, 54, 54);
		drawGradientRect(leftPos + 7, topPos + 75 - (int) (((float) dispenser.getInfoEnergy() /
				(float) dispenser.energy.getMaxEnergyStored()) * 60f), leftPos + 23, topPos + 75, 0xFFBF0000, 0xFF7F0000);
	}
}
