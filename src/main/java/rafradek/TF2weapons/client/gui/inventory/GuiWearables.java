package rafradek.TF2weapons.client.gui.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.inventory.ContainerWearables;

public class GuiWearables extends AbstractContainerScreen<ContainerWearables> {
	/** The old x position of the mouse pointer */
	private float oldMouseX;
	/** The old y position of the mouse pointer */
	private float oldMouseY;

	public static final ResourceLocation WEARABLES_TEXTURE = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/container/wearables.png");

	public GuiWearables(ContainerWearables container) {
		super(container, container.player.getInventory(), Component.empty());
		this.imageWidth = 199;
	}

	@Override
	protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
		this.font.draw(pose, I18n.get("item.rafradek_tf2_weapons.ammoBelt.name"), 97, 8, 4210752);
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		this.oldMouseX = mouseX;
		this.oldMouseY = mouseY;
		this.renderTooltip(pose, mouseX, mouseY);
	}

	@Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, WEARABLES_TEXTURE);
		int i = this.leftPos;
		int j = this.topPos;
		this.blit(pose, i, j, 0, 0, this.imageWidth, this.imageHeight);
		drawEntityOnScreen(i + 51, j + 75, 30, i + 51 - this.oldMouseX, j + 75 - 50 - this.oldMouseY,
				this.minecraft.player);
	}

	/**
	 * Draws an entity on the screen looking toward the cursor.
	 */
	public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity ent) {
		InventoryScreen.renderEntityInInventory(posX, posY, scale, mouseX, mouseY, ent);
	}
}
