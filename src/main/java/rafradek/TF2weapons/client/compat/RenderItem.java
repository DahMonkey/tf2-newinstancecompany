package rafradek.TF2weapons.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

/** 1.12 RenderItem GUI drawing over the 1.19 ItemRenderer. */
public class RenderItem {
	public float zLevel;

	private ItemRenderer renderer() {
		return Minecraft.getInstance().getItemRenderer();
	}

	private void withPose(Runnable action) {
		PoseStack modelView = com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();
		modelView.pushPose();
		modelView.mulPoseMatrix(LegacyRender.pose().last().pose());
		com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
		float old = renderer().blitOffset;
		renderer().blitOffset = this.zLevel;
		action.run();
		renderer().blitOffset = old;
		modelView.popPose();
		com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
	}

	public void renderItemIntoGUI(ItemStack stack, int x, int y) {
		withPose(() -> renderer().renderGuiItem(stack, x, y));
	}

	public void renderItemAndEffectIntoGUI(ItemStack stack, int x, int y) {
		withPose(() -> renderer().renderAndDecorateItem(stack, x, y));
	}

	public void renderItemOverlays(net.minecraft.client.gui.Font font, ItemStack stack, int x, int y) {
		withPose(() -> renderer().renderGuiItemDecorations(font, stack, x, y));
	}

	public void renderItemOverlays(LegacyFont font, ItemStack stack, int x, int y) {
		renderItemOverlays(font.font, stack, x, y);
	}

	public void renderItemOverlayIntoGUI(LegacyFont font, ItemStack stack, int x, int y, String text) {
		withPose(() -> renderer().renderGuiItemDecorations(font.font, stack, x, y, text));
	}
}
