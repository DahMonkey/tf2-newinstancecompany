package rafradek.TF2weapons.client.gui.inventory;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.inventory.ContainerConfigurable;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.tileentity.IEntityConfigurable;
import java.io.IOException;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

public class GuiConfigurable extends AbstractContainerScreen {
	public GuiConfigurable(AbstractContainerMenu inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	private static final ResourceLocation UPGRADES_GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID, "textures/gui/container/upgrades.png");

	// public ItemStack[] itemsToRender;

	private boolean isScrolling;
	private boolean wasClicking;
	public int firstIndex;
	public float scroll;
	public CompoundTag tag;
	public String[] keys = new String[0];
	public int[] types = new int[0];
	public GuiComponent[] fields = new GuiComponent[0];
	public BlockPos pos;

	public GuiConfigurable(Inventory playerv, IEntityConfigurable station, Level world,
			BlockPos blockPosition) {
		super(new ContainerConfigurable(Minecraft.getInstance().player, playerv, station, world, blockPosition));
		this.imageWidth = 230;
		this.imageHeight = 225;
		this.pos = blockPosition;
		// this.itemsToRender=new ItemStack[9];
	}

	@Override
	public void initGui() {
		super.initGui();
		int i = 0;
		buttonList.clear();
		if (tag != null) {
			keys = new String[tag.getSize()];
			types = new int[tag.getSize()];
			fields = new GuiComponent[tag.getSize()];
			for (String key : tag.getAllKeys()) {
				keys[i] = key;
				int type = tag.getTag(key).getId();
				types[i] = type;
				if (type == 8) {
					fields[i] = new EditBox(5, fontRenderer, this.leftPos + 10, this.topPos + 10 + i * 20, 120,
							20);
					((EditBox) fields[i]).setText(tag.getString(key));
				}

				i++;
			}
		}
		/*
		 * for (int x = 0; x < 2; x++) for (int y = 0; y < 3; y++) {
		 * this.buttonList.add(buttons[x * 2 + y * 4] = new GuiButton(x * 2 + y * 4,
		 * this.guiLeft + 81 + x * 101, this.guiTop + 47 + y * 30, 12, 12, "+"));
		 * this.buttonList.add(buttons[x * 2 + y * 4 + 1] = new GuiButton(x * 2 + y * 4
		 * + 1, this.guiLeft + 94 + x * 101, this.guiTop + 47 + y * 30, 12, 12, "-")); }
		 * this.buttonList.add(refund = new GuiButton(12, this.guiLeft + 123,
		 * this.guiTop + 121, 100, 20, I18n.format("container.upgrades.refund")));
		 */
		setButtons();
	}

	public void setButtons() {
		/*
		 * for(int i=0;i<12;i++){
		 * //System.out.println("Buttons: "+buttonsItem[i]+" "+firstIndex);
		 * if(i+firstIndex<TF2CraftingManager.INSTANCE.getRecipeList().size()){
		 * buttonsItem[i].stackToDraw=TF2CraftingManager.INSTANCE.getRecipeList(
		 * ).get(i+firstIndex).getRecipeOutput();
		 * buttonsItem[i].selected=i+firstIndex==((ContainerTF2Workbench)this.
		 * inventorySlots).currentRecipe; } else{ buttonsItem[i].stackToDraw=null;
		 * buttonsItem[i].selected=false; } }
		 */
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		boolean flag = Mouse.isButtonDown(0);
		int i = leftPos;
		int j = topPos;
		int k = i + 209;
		int l = j + 30;
		int i1 = k + 14;
		int j1 = l + 96;
		if (!wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) isScrolling = true;
		if (!flag) isScrolling = false;
		wasClicking = flag;
		if (isScrolling) {
			int size = keys.length;
			if (size >= 6) {
				scroll = (mouseY - l - 7.5F) / (j1 - l - 15.0F);
				scroll = Mth.clamp(scroll, 0.0F, 1.0F);
				firstIndex = Math.round(scroll * (size - 6) / 2) * 2;
				setButtons();
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (tag != null) for (int t = 0; t < keys.length; t++) if (fields[t] instanceof EditBox) ((EditBox) fields[t]).drawTextBox();
		/*
		 * this.refund.enabled =
		 * !this.inventorySlots.inventorySlots.get(0).getStack().isEmpty() &&
		 * this.inventorySlots.inventorySlots.get(0).getStack().getTagCompound().
		 * getInteger("TotalSpent") > 0;
		 *
		 * this.renderHoveredToolTip(mouseX, mouseY); for (GuiTooltip tooltip :
		 * this.tooltip) { tooltip.drawButton(mc, mouseX, mouseY, partialTicks); }
		 */
	}

	@Override
	public void drawHoveringText(List<String> textLines, int x, int y) {
		drawHoveringText(textLines, x, y, fontRenderer);
	}

	@Override
	protected void actionPerformed(Button button) throws IOException {
		if (button.id < 12) mc.playerController.sendEnchantPacket(slots.windowId, button.id + firstIndex * 2);
		else if (button.id == 12) mc.playerController.sendEnchantPacket(slots.windowId, -1);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for (int t = 0; t < keys.length; t++) if (fields[t] instanceof EditBox)
			((EditBox) fields[t]).mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		boolean entered = false;
		for (int t = 0; t < this.keys.length; t++) if (fields[t] instanceof EditBox)
			entered |= ((EditBox) fields[t]).textboxKeyTyped(typedChar, keyCode);
		if (!entered) {
			if (keyCode == 1 || this.minecraft.options.keyBindInventory.isActiveAndMatches(keyCode)) sendChanges();
			super.keyTyped(typedChar, keyCode);
		}
		// this.teleportField.setText(Integer.toString(channel));
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
	}

	public void sendChanges() {
		CompoundTag newtag = new CompoundTag();
		for (int t = 0; t < keys.length; t++) if (types[t] == 8) newtag.putString(keys[t], ((EditBox) fields[t]).getText());
		TF2weapons.network.sendToServer(new TF2Message.GuiConfigMessage(newtag, this.pos));
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the
	 * items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(I18n.get("container.inventory", new Object[0]), 36, imageHeight - 96 + 3, 4210752);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.RenderSystem.setShaderTexture(0, UPGRADES_GUI_TEXTURES);
		int x = (width - imageWidth) / 2;
		int y = (height - imageHeight) / 2;
		drawTexturedModalRect(x, y, 0, 0, imageWidth, imageHeight);
		// this.drawTab(0);
		// this.drawTab(1);
		x = leftPos + 210;
		y = topPos + 31;
		int k = y + 96;
		drawTexturedModalRect(x, y + (int) ((k - y - 17) * scroll), 232, 0, 12, 15);
	}
}