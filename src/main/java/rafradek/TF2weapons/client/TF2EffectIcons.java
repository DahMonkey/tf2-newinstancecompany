package rafradek.TF2weapons.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Supplier;

/** Icon renderers for the mod's status effects (inventory list and HUD). */
public final class TF2EffectIcons {

	public static IClientMobEffectExtensions forVanillaIcon(Supplier<MobEffect> icon) {
		return new IClientMobEffectExtensions() {
			@Override
			public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen,
					PoseStack poseStack, int x, int y, int blitOffset) {
				TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(icon.get());
				RenderSystem.setShaderTexture(0, sprite.atlas().location());
				GuiComponent.blit(poseStack, x + 7, y + 7, blitOffset, 18, 18, sprite);
				return true;
			}

			@Override
			public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, PoseStack poseStack, int x, int y,
					float z, float alpha) {
				TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(icon.get());
				RenderSystem.setShaderTexture(0, sprite.atlas().location());
				RenderSystem.setShaderColor(1, 1, 1, alpha);
				GuiComponent.blit(poseStack, x + 3, y + 3, (int) z, 18, 18, sprite);
				RenderSystem.setShaderColor(1, 1, 1, 1);
				return true;
			}
		};
	}

	public static IClientMobEffectExtensions forTexture(ResourceLocation texture) {
		return new IClientMobEffectExtensions() {
			@Override
			public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen,
					PoseStack poseStack, int x, int y, int blitOffset) {
				RenderSystem.setShaderTexture(0, texture);
				GuiComponent.blit(poseStack, x + 7, y + 7, blitOffset, 0, 0, 16, 16, 16, 16);
				return true;
			}

			@Override
			public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, PoseStack poseStack, int x, int y,
					float z, float alpha) {
				RenderSystem.setShaderTexture(0, texture);
				RenderSystem.setShaderColor(1, 1, 1, alpha);
				GuiComponent.blit(poseStack, x + 4, y + 4, (int) z, 0, 0, 16, 16, 16, 16);
				RenderSystem.setShaderColor(1, 1, 1, 1);
				return true;
			}
		};
	}
}
