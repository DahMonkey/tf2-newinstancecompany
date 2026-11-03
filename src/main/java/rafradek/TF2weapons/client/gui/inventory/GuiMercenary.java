package rafradek.TF2weapons.client.gui.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character.Order;
import rafradek.TF2weapons.inventory.ContainerMercenary;
import rafradek.TF2weapons.util.TF2Util;

/**
 * Mercenary trading and loadout screen: the vanilla trading screen with the mercenary's armor, weapon
 * and refill slots in a panel on its right.
 */
public class GuiMercenary extends MerchantScreen {

	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/container/mercenary.png");
	private static final int PANEL_X = 276;

	public final EntityTF2Character mercenary;
	private final ContainerMercenary container;
	private final Inventory inv;
	private Button hireBtn;
	private Button shareBtn;
	private Button orderBtn;
	private final Button[] mainWeaponButton = new Button[3];

	public GuiMercenary(ContainerMercenary menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.container = menu;
		this.mercenary = menu.mercenary;
		this.inv = inv;
	}

	@Override
	protected void init() {
		super.init();
		this.hireBtn = this.addRenderableWidget(new Button(this.leftPos + PANEL_X / 2 - 75, this.topPos - 25, 150, 20,
				Component.literal("Hire mercenary (1 Australium ingot)"), b -> this.press(0)));
		this.shareBtn = this.addRenderableWidget(new Button(this.leftPos + PANEL_X / 2 - 75,
				this.topPos + this.imageHeight + 5, 150, 20, Component.literal("Share loot (1 Australium ingot)"),
				b -> this.press(1)));
		this.orderBtn = this.addRenderableWidget(new Button(this.leftPos + PANEL_X + 3, this.topPos + 123, 48, 20,
				Component.literal("Order"), b -> this.press(2)));
		for (int i = 0; i < 3; i++) {
			int slot = i;
			this.mainWeaponButton[i] = this.addRenderableWidget(new Button(this.leftPos + PANEL_X + 47,
					this.topPos + 12 + i * 18, 4, 6, Component.empty(), b -> this.press(3 + slot)));
		}
		this.updateButtons();
	}

	private void press(int button) {
		if (button == 2) {
			this.mercenary.setOrder(this.mercenary.getOrder() == Order.FOLLOW ? Order.HOLD : Order.FOLLOW);
			this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId,
					10 + this.mercenary.getOrder().ordinal());
		} else if (button == 0) {
			this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
			if (this.mercenary.getOwnerUUID() == null)
				this.mercenary.setOwner(this.minecraft.player);
			else
				this.mercenary.setOwner(null);
		} else if (button == 1) {
			this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
			this.mercenary.setSharing(true);
		} else {
			int slot = button - 3;
			this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 50 + slot);
			this.mercenary.setMainWeapon(this.mercenary.getMainWeapon() == slot ? -1 : slot);
		}
		this.updateButtons();
	}

	private boolean hasAustralium() {
		return this.inv.contains(TF2Util.metaStack(TF2weapons.itemTF2, 1, 2));
	}

	public void updateButtons() {
		if (this.hireBtn == null)
			return;
		if (this.mercenary.isRobot()) {
			this.hireBtn.visible = false;
			this.shareBtn.visible = false;
		}
		if (this.mercenary.getOwnerUUID() == null) {
			this.hireBtn.active = this.hasAustralium();
			this.hireBtn.setMessage(Component.literal("Hire this mercenary (1 Australium ingot)"));
			this.orderBtn.active = false;
			this.shareBtn.visible = false;
		} else if (this.mercenary.getOwnerUUID().equals(this.minecraft.player.getUUID())) {
			this.hireBtn.active = true;
			this.hireBtn.setMessage(Component.literal("Fire this mercenary"));
			this.orderBtn.active = true;
			this.orderBtn.setMessage(Component.literal(this.mercenary.getOrder().toString()));
			if (!this.mercenary.isRobot()) {
				this.shareBtn.visible = true;
				this.shareBtn.active = !this.mercenary.isSharing() && this.hasAustralium();
			}
		} else {
			this.hireBtn.active = false;
			this.hireBtn.setMessage(Component.literal(
					this.mercenary.ownerName == null ? "Hired mercenary" : "Hired by: " + this.mercenary.ownerName));
			this.orderBtn.active = false;
			this.shareBtn.visible = false;
		}
		for (int i = 0; i < 3; i++) {
			this.mainWeaponButton[i].active = this.minecraft.player.getUUID().equals(this.mercenary.getOwnerUUID())
					|| this.minecraft.player.isCreative();
			this.mainWeaponButton[i].setMessage(Component.literal(this.mercenary.getMainWeapon() == i ? "|" : ""));
		}
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		if (this.minecraft.player.tickCount % 10 == 0)
			this.updateButtons();
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		super.render(pose, mouseX, mouseY, partialTicks);
		if (this.hireBtn.isHoveredOrFocused())
			this.renderTooltip(pose, Component.literal("Lost australium can be recovered at Mann Co store"), mouseX,
					mouseY);
		else if (this.shareBtn.isHoveredOrFocused())
			this.renderTooltip(pose, Component.literal("Allows the owner to collect loot from enemies"), mouseX,
					mouseY);
		else if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && !this.hoveredSlot.hasItem()) {
			int id = this.hoveredSlot.index;
			if (id >= 43 && id < 47 && !this.mercenary.loadout.getStackInSlot(id - 43).isEmpty())
				this.renderTooltip(pose, this.mercenary.loadout.getStackInSlot(id - 43), mouseX, mouseY);
		}
	}

	@Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(pose, partialTicks, mouseX, mouseY);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI_TEXTURES);
		int i = this.leftPos + PANEL_X;
		int j = this.topPos;
		this.blit(pose, i, j, 176, 0, 54, 146);
		this.font.draw(pose, "Refill", i + 7, j + 80, 4210752);
		this.font.draw(pose, Integer.toString(this.container.primaryAmmo), i + 10, j + 113, 4210752);
		this.font.draw(pose, Integer.toString(this.container.secondaryAmmo), i + 33, j + 113, 4210752);
		for (int k = 0; k < 4; k++) {
			ItemStack stack = this.mercenary.loadout.getStackInSlot(k);
			if (!stack.isEmpty() && !this.menu.getSlot(k + 43).hasItem())
				this.itemRenderer.renderAndDecorateItem(stack, this.menu.getSlot(k + 43).x + this.leftPos,
						this.menu.getSlot(k + 43).y + this.topPos);
		}
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
		boolean inPanel = mouseX >= left + PANEL_X && mouseX < left + PANEL_X + 54 && mouseY >= top
				&& mouseY < top + 146;
		return !inPanel && super.hasClickedOutside(mouseX, mouseY, left, top, button);
	}
}
