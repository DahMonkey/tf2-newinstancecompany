package rafradek.TF2weapons.client.gui.inventory;

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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class GuiDispenser extends AbstractContainerScreen {

	public EntityDispenser dispenser;
	public Button doneBtn;
	public Button grab;
	public int channel;
	public boolean exit;
	public int attackFlags;

	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID, "textures/gui/container/building.png");

	public GuiDispenser(EntityDispenser dispenser) {
		super(new ContainerDispenser(dispenser, Minecraft.getInstance().player.inventory));
		this.dispenser = dispenser;
		xSize = 212;
		ySize = 195;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(this.grab = new Button(0, this.guiLeft + 86, this.guiTop + 90, 40, 20,
				I18n.get("gui.teleporter.drop", new Object[0])));
	}

	@Override
	protected void actionPerformed(Button button) throws IOException {
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
		if (mouseX >= guiLeft + 7 && mouseX < guiLeft + 23 && mouseY >= guiTop + 15 && mouseY < guiTop + 75) {
			if (ClientProxy.buildingsUseEnergy) drawHoveringText("Energy: " + dispenser.getInfoEnergy() + "/"
							+ dispenser.energy.getMaxEnergyStored(), mouseX, mouseY);
			else drawHoveringText("Energy use is disabled", mouseX, mouseY);
		}
		if (mouseX >= guiLeft + 5 && mouseX < guiLeft + 23 && mouseY >= guiTop + 112 && mouseY < guiTop + 130)
			drawHoveringText(Arrays.asList(I18n.get("gui.dispenser.help", new Object[0]).split(Pattern.quote("\\n"))), mouseX, mouseY);
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.RenderSystem.setShaderTexture(0, GUI_TEXTURES);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		drawTexturedModalRect(x + 79, y + 14, 0, 202, 54, 54);
		drawGradientRect(guiLeft + 7, guiTop + 75 - (int) (((float) dispenser.getInfoEnergy() /
				(float) dispenser.energy.getMaxEnergyStored()) * 60f), guiLeft + 23, guiTop + 75, 0xFFBF0000, 0xFF7F0000);
	}
}
