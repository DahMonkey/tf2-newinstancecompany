package rafradek.TF2weapons.client.gui.inventory;

import net.minecraft.world.Container;
import rafradek.TF2weapons.client.compat.GuiContainer;

import rafradek.TF2weapons.client.compat.GlStateManager;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.inventory.ContainerAmmoFurnace;
import rafradek.TF2weapons.tileentity.TileEntityAmmoFurnace;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GuiAmmoFurnace extends GuiContainer {

	private static final ResourceLocation FURNACE_GUI_TEXTURES = new ResourceLocation(
			TF2weapons.MOD_ID + ":textures/gui/container/ammofurnace.png");
	/** The player inventory bound to this GUI. */
	private final Inventory inventory;
	private final Container tileFurnace;

	public GuiAmmoFurnace(ContainerAmmoFurnace container) {
		super(container);
		inventory = net.minecraft.client.Minecraft.getInstance().player.getInventory();
		tileFurnace = container.tileFurnace;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the
	 * items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = TF2weapons.blockAmmoFurnace.getName().getString();
		fontRenderer.drawString(s, this.imageWidth / 2 - this.fontRenderer.width(s) / 2, 6, 4210752);
		fontRenderer.drawString(this.inventory.getDisplayName().getString(), 8, this.imageHeight - 96 + 2, 4210752);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, FURNACE_GUI_TEXTURES);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		drawTexturedModalRect(i, j, 0, 0, this.imageWidth, this.imageHeight);
		if (TileEntityAmmoFurnace.isBurning(this.tileFurnace)) {
			int k = this.getBurnLeftScaled(13);
			drawTexturedModalRect(i + 64, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
		}
		int l = this.getCookProgressScaled(24);
		drawTexturedModalRect(i + 87, j + 34, 176, 14, l + 1, 16);
	}

	private int getCookProgressScaled(int pixels) {
		int i = field(2);
		int j = field(3);
		return j != 0 && i != 0 ? i * pixels / j : 0;
	}

	private int getBurnLeftScaled(int pixels) {
		int i = field(1);
		if (i == 0) i = 200;
		return field(0) * pixels / i;
	}


	private int field(int id) {
		return this.tileFurnace instanceof rafradek.TF2weapons.inventory.IFieldHolder
				? ((rafradek.TF2weapons.inventory.IFieldHolder) this.tileFurnace).getField(id)
				: 0;
	}
}
