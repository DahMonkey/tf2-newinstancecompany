package rafradek.TF2weapons.potion;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotionTF2Item extends MobEffect {

	public ResourceLocation texture;

	public PotionTF2Item(boolean isBadEffectIn, int liquidColorIn, ResourceLocation texture) {
		super(isBadEffectIn, liquidColorIn);
		this.texture = texture;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInventoryEffect(int x, int y, MobEffectInstance effect, Minecraft mc) {
		mc.RenderSystem.setShaderTexture(0, texture);
		// mc.ingameGUI.drawTexturedModalRect(x+6,y+7,0,0,16,16);
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder renderer = tessellator.getBuffer();
		renderer.begin(7, DefaultVertexFormat.POSITION_TEX);

		renderer.pos(x + 7, y + 23, 0.0D).tex(0.0D, 1D).endVertex();
		renderer.pos(x + 23, y + 23, 0.0D).tex(1.0D, 1D).endVertex();
		renderer.pos(x + 23, y + 7, 0.0D).tex(1.0D, 0.0D).endVertex();
		renderer.pos(x + 7, y + 7, 0.0D).tex(0.0D, 0.0D).endVertex();
		tessellator.draw();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderHUDEffect(int x, int y, MobEffectInstance effect, net.minecraft.client.Minecraft mc, float alpha) {
		mc.RenderSystem.setShaderTexture(0, texture);
		// mc.ingameGUI.drawTexturedModalRect(x+3,y+3,0,0,16,16);
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder renderer = tessellator.getBuffer();

		renderer.begin(7, DefaultVertexFormat.POSITION_TEX);

		renderer.pos(x + 4, y + 20, 0.0D).tex(0.0D, 1D).endVertex();
		renderer.pos(x + 20, y + 20, 0.0D).tex(1.0D, 1D).endVertex();
		renderer.pos(x + 20, y + 4, 0.0D).tex(1.0D, 0.0D).endVertex();
		renderer.pos(x + 4, y + 4, 0.0D).tex(0.0D, 0.0D).endVertex();
		tessellator.draw();
	}
}
