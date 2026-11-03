package rafradek.TF2weapons.client.gui.inventory;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.gui.GuiButtonToggleItem;
import rafradek.TF2weapons.inventory.ContainerTF2Workbench;
import rafradek.TF2weapons.item.crafting.IRecipeTF2;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import java.io.IOException;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class GuiTF2Crafting extends AbstractContainerScreen {
	private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/container/cabinet.png");

	public GuiButtonToggleItem[] buttonsItem;
	public NonNullList<ItemStack> itemsToRender;
	public int firstIndex;
	public float scroll;
	public int tabid;
	public ItemStack craftingTabStack = TF2Util.metaStack(TF2weapons.itemAmmo, 1, 1);
	public ItemStack chestTabStack = new ItemStack(Blocks.CHEST);

	// public TileEntityCabinet cabinet;
	private boolean isScrolling;

	private boolean wasClicking;

	public GuiTF2Crafting(Inventory playerv, Level world, BlockPos blockPosition) {
		super(new ContainerTF2Workbench(Minecraft.getInstance().player, playerv, world, blockPosition));
		// this.cabinet=cabinet;
		imageWidth = 176;
		imageHeight = 180;
		itemsToRender = NonNullList.<ItemStack>withSize(9, ItemStack.EMPTY);
		buttonsItem = new GuiButtonToggleItem[12];
	}

	@Override
	public void initGui() {
		super.initGui();
		for (int x = 0; x < 3; x++) for (int y = 0; y < 4; y++) buttonList.add(buttonsItem[x + y * 3] = new GuiButtonToggleItem(x + y * 3,
				leftPos + 7 + x * 18, topPos + 14 + y * 18, 18, 18));
		setButtons();
	}

	public void setButtons() {
		for (int i = 0; i < 12; i++) {
			// System.out.println("Buttons: "+buttonsItem[i]+" "+firstIndex);
			if (i + firstIndex < TF2CraftingManager.INSTANCE.getRecipeList().size()) {
				buttonsItem[i].stackToDraw = ContainerTF2Workbench.getReplacement(mc.player,
						TF2CraftingManager.INSTANCE.getRecipeList().get(i + firstIndex).getRecipeOutput().copy());
				buttonsItem[i].selected = i + firstIndex == ((ContainerTF2Workbench) slots).currentRecipe;
			} else {
				buttonsItem[i].stackToDraw = ItemStack.EMPTY;
				buttonsItem[i].selected = false;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		boolean flag = Mouse.isButtonDown(0);
		int i = leftPos;
		int j = topPos;
		int k = i + 61;
		int l = j + 14;
		int i1 = k + 14;
		int j1 = l + 72;
		if (!wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) isScrolling = true;
		if (!flag) isScrolling = false;
		wasClicking = flag;
		if (isScrolling) {
			int size = TF2CraftingManager.INSTANCE.getRecipeList().size();
			scroll = (mouseY - l - 7.5F) / (j1 - l - 15.0F);
			scroll = Mth.clamp(scroll, 0.0F, 1.0F);
			firstIndex = Math.round(scroll * (size - 12) / 3) * 3;
			setButtons();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (getSlotUnderMouse() != null && getSlotUnderMouse().getStack().isEmpty() && mc.player.getInventory().getItem().isEmpty()
				&& getSlotUnderMouse().inventory instanceof CraftingContainer &! itemsToRender.get(getSlotUnderMouse().slotNumber - 1).isEmpty())
			renderToolTip(itemsToRender.get(getSlotUnderMouse().slotNumber - 1), mouseX, mouseY);
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void actionPerformed(Button button) throws IOException {
		if (button.id >= 12) return;
		int currentRecipe = button.id + firstIndex;
		((ContainerTF2Workbench)slots).currentRecipe = currentRecipe;
		mc.playerController.sendEnchantPacket(slots.windowId, currentRecipe);
		setButtons();
		slots.onCraftMatrixChanged(null);
		itemsToRender = NonNullList.withSize(9, ItemStack.EMPTY);
		if (currentRecipe < 0 || currentRecipe >= TF2CraftingManager.INSTANCE.getRecipeList().size()) return;
		Recipe recipe = TF2CraftingManager.INSTANCE.getRecipeList().get(currentRecipe);
		for (int i = 1; i < 10; i++) {
			this.handleMouseClick(this.slots.getSlot(i), i, 0, ClickType.QUICK_MOVE);

		}
		if (recipe instanceof IRecipeTF2) {
			for (int i = 0; i < 9; i++) itemsToRender.set(i, ((IRecipeTF2) recipe).getSuggestion(i));
			return;
		}
		List<Ingredient> input = recipe.getIngredients();
		for (int i = 0; i < input.size(); i++) {
			int space = 0;
			if (recipe instanceof ShapedRecipes) space = (3 - ((ShapedRecipes) recipe).recipeWidth)
						* (i / ((ShapedRecipes) recipe).recipeWidth);
			else if (recipe instanceof ShapedOreRecipe) space = (3 - ((ShapedOreRecipe) recipe).getRecipeWidth())
					* (i / ((ShapedOreRecipe) recipe).getRecipeWidth());
			if (input.get(i).getMatchingStacks().length > 0) {
				itemsToRender.set(i + space, input.get(i).getMatchingStacks()[0]);
				if (itemsToRender.get(i + space).getDamageValue() == 32767) itemsToRender.get(i + space).setDamageValue(0);
			}
			for (int j = 10; j < 46; j++) if (input.get(i).apply(slots.getSlot(j).getStack())) {
				handleMouseClick(slots.getSlot(j), j, 0, ClickType.PICKUP);
				handleMouseClick(slots.getSlot(i + space + 1), i + space + 1, 1, ClickType.PICKUP);
				handleMouseClick(slots.getSlot(j), j, 0, ClickType.PICKUP);
				break;
			}
		}
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the
	 * items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(I18n.get("container.crafting", new Object[0]), 8, 5, 4210752);
		fontRenderer.drawString(I18n.get("container.inventory", new Object[0]), 8, imageHeight - 96 + 3,
				4210752);
		for (int i = 0; i < 12; i++) if (buttonsItem[i].stackToDraw != null && buttonsItem[i].isMouseOver())
			renderToolTip(buttonsItem[i].stackToDraw, mouseX - leftPos, mouseY - topPos);
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.RenderSystem.setShaderTexture(0, CRAFTING_TABLE_GUI_TEXTURES);
		int x = (width - imageWidth) / 2;
		int y = (height - imageHeight) / 2;
		drawTexturedModalRect(x, y, 0, 0, imageWidth, imageHeight);
		// this.drawTab(0);
		// this.drawTab(1);
		x = leftPos + 62;
		y = topPos + 15;
		int k = y + 72;
		drawTexturedModalRect(x, y + (int) ((k - y - 17) * scroll), 232, 0, 12, 15);
		GlStateManager.enableLighting();
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
		itemRender.zLevel = 0;
		for (x = 0; x < 3; x++) for (y = 0; y < 3; y++) {
			ItemStack stack = itemsToRender.get(x + y * 3);
			if (stack != null && !stack.isEmpty()) itemRender.renderItemIntoGUI(stack, leftPos + 86 + 18 * x, topPos + 23 + 18 * y);
		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		mc.RenderSystem.setShaderTexture(0, CRAFTING_TABLE_GUI_TEXTURES);
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
		zLevel = 120;
		drawTexturedModalRect(85 + leftPos, 22 + topPos, 85, 22, 54, 54);
		zLevel = 0;
	}

}