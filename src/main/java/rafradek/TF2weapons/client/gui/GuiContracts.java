package rafradek.TF2weapons.client.gui;

import rafradek.TF2weapons.client.compat.GlStateManager;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.Contract;
import java.io.IOException;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class GuiContracts extends Screen {

	private int leftPos;
	private int topPos;

	public Contract selectedContract;
	public int selectedId = -1;
	private static final ResourceLocation CONTRACTS_GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID, "textures/gui/contracts.png");

	public GuiContracts() {}

	@Override
	public void initGui() {
		mc.getConnection().send(new CPacketClientStatus(CPacketClientStatus.State.REQUEST_STATS));
		mc.player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).newContracts = false;
		mc.player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).newRewards = false;
		super.initGui();
		leftPos = width / 2 - 128;
		topPos = height / 2 - 108;
		buttonList.add(new Button(0, leftPos + 7, topPos + 189, 100, 20, I18n.get("gui.contracts.accept")));
		buttonList.add(new Button(1, leftPos + 107, topPos + 189, 71, 20, I18n.get("gui.contracts.reject")));
		buttonList.add(new Button(2, leftPos + 178, topPos + 189, 71, 20, I18n.get("gui.done")));
		if (selectedContract != null) {
			buttonList.get(1).enabled = true;
			buttonList.get(0).enabled = !selectedContract.active || selectedContract.rewards > 0;
			buttonList.get(0).displayString = I18n.get(selectedContract.active ? "gui.contracts.claim" : "gui.contracts.accept");
		} else {
			buttonList.get(1).enabled = false;
			buttonList.get(0).enabled = false;
			buttonList.get(0).displayString = I18n.get("gui.contracts.select");
		}
		for (int i = 0; i < mc.player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts.size(); i++) {
			Contract contract = mc.player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts.get(i);
			buttonList.add(new Button(i + 3, this.leftPos + 7, this.topPos + 16 + i * 20, 74, 20,
					I18n.get("gui.contracts." + contract.className, new Object[0]) + ": " + contract.progress + " CP"));
		}
	}

	@Override
	protected void actionPerformed(Button button) throws IOException {
		if (button.id > 2) {
			selectedId = button.id - 3;
			selectedContract = mc.player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts.get(button.id - 3);
			// this.displayItem(((GuiButtonToggleItem)button).stackToDraw);
		} else if (button.id == 0 && selectedId >= 0) {
			if (!selectedContract.active) {
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(32 + selectedId));
				selectedContract.active = true;
			} else if (selectedContract.rewards > 0) {
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(48 + selectedId));
				selectedContract.rewards = 0;
				if (selectedContract.progress >= Contract.REWARD_HIGH) {
					mc.player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts.remove(selectedId);
					selectedContract = null;
					selectedId = -1;
					buttonList.clear();
					initGui();
				}
			}
		} else if (button.id == 1 && selectedId >= 0) {
			mc.setScreen(new GuiYesNo((result, id) -> {
				if (result) {
					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(64 + selectedId));
					mc.player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).contracts.remove(selectedId);
					selectedContract = null;
					selectedId = -1;
					buttonList.clear();
					initGui();
					mc.player.getStatFileWriter().unlockAchievement(mc.player, TF2Achievements.CONTRACT_DAY,
							(int) (mc.level.getDayTime() / 24000 + 1));

				}
				Minecraft.getInstance().setScreen(this);
			}, "Confirm", "Are you sure you want to decline this contract?", 0));
		} else if (button.id == 2) mc.setScreen(null);
		if (selectedContract != null) {
			buttonList.get(1).enabled = true;
			buttonList.get(0).enabled = !selectedContract.active || selectedContract.rewards > 0;
			buttonList.get(0).displayString = I18n.get(selectedContract.active ? "gui.contracts.claim" : "gui.contracts.accept");
		} else {
			buttonList.get(1).enabled = false;
			buttonList.get(0).enabled = false;
			buttonList.get(0).displayString = I18n.get("gui.contracts.select");
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.RenderSystem.setShaderTexture(0, CONTRACTS_GUI_TEXTURES);
		drawTexturedModalRect(leftPos, topPos, 0, 0, 256, 216);
		super.drawScreen(mouseX, mouseY, partialTicks);
		int contractDay = mc.player.getStatFileWriter().readStat(TF2Achievements.CONTRACT_DAY);
		fontRenderer.drawString(I18n.get("gui.contracts", new Object[0]), leftPos + 8, topPos + 5, 4210752);
		if (contractDay != 0) {
			String str = I18n.get("gui.contracts.contractDay", contractDay - mc.level.getDayTime() / 24000);
			fontRenderer.drawString(str, leftPos + 251 - fontRenderer.width(str), topPos + 5, 4210752);
		}
		if (selectedContract != null) {
			fontRenderer.drawString(I18n.get("gui.contracts." + selectedContract.className, new Object[0]), leftPos + 83, topPos + 18, 0xFFFFFF);
			this.font.drawString(I18n.get("gui.contracts.objectives"), leftPos + 83, topPos + 35, 0xFFFFFF);
			if (!selectedContract.className.equals("kill")) fontRenderer.drawString(I18n.get("gui.contracts.objectives_t",
							I18n.get("entity." + selectedContract.className + ".name")), leftPos + 83, topPos + 47, 0xFFFFFF);
			for (int i = 0; i < selectedContract.objectives.length; i++) {
				String str = I18n.get("objective." + selectedContract.objectives[i].toString().toLowerCase());
				if (selectedContract.objectives[i].advanced) str = I18n.get("objective.advanced") + " " + str;
				str += " (" + selectedContract.objectives[i].getPoints() + " CP)";
				fontRenderer.drawSplitString(str, leftPos + 83, topPos + 67 + i * 20, 164, 0xFFFFFF);
			}
			fontRenderer.drawString(I18n.get("gui.contracts.rewards"), leftPos + 9, topPos + 135, 0xFFFFFF);
			fontRenderer.drawString(I18n.get("gui.contracts.reward1"), leftPos + 9, topPos + 148, 0xFFFFFF);
			fontRenderer.drawString(I18n.get("gui.contracts.reward1m"), leftPos + 9, topPos + 157, 0xFFFFFF);
			fontRenderer.drawString(I18n.get("gui.contracts.reward2"), leftPos + 9, topPos + 170, 0xFFFFFF);
			fontRenderer.drawString(I18n.get("gui.contracts.reward2m"), leftPos + 9, topPos + 179, 0xFFFFFF);
		} else if (contractDay == 0) fontRenderer.drawSplitString(I18n.get("gui.contracts.require"),
				leftPos + 83, topPos + 18, 164, 0xFFFFFF);
	}
}
