package rafradek.TF2weapons.client.renderer.tileentity;

import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;
import rafradek.TF2weapons.client.compat.DefaultVertexFormats;

import rafradek.TF2weapons.client.compat.GlStateManager;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.block.BlockOverheadDoor;
import rafradek.TF2weapons.tileentity.TileEntityOverheadDoor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class RenderDoor extends BlockEntityRenderer<TileEntityOverheadDoor> {

	@Override
	public void render(TileEntityOverheadDoor te, double x, double y, double z, float partialTicks, int destroyStage,
			float alpha) {
		BlockPos orig = te.getBlockPos();
		if (!te.getLevel().isEmptyBlock(new BlockPos(orig.getX(), te.minBounds.getY(), orig.getZ()))
				|| te.getLevel().getBlockState(orig).getBlock() != TF2weapons.blockOverheadDoor)
			return;
		BlockState state = te.getLevel().getBlockState(orig).setValue(BlockOverheadDoor.HOLDER, false)
				.setValue(BlockOverheadDoor.SLIDING, false);
		this.bindTexture(TextureAtlas.LOCATION_BLOCKS_TEXTURE);
		float amountScrolled = te.amountScrolled - te.motion + partialTicks * te.motion;
		float off = amountScrolled - (int) amountScrolled;
		for (int i = 0; i <= amountScrolled; i++) {
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			BlockPos blockpos = orig.below(i);
			// GlStateManager.translate(-blockpos.getX(), -blockpos.getY(),
			// -blockpos.getZ());

			float yscale = 1;
			if (i == 0) {
				GlStateManager.scale(1f, off, 1f);
				yscale = 1 / off;
			}
			GlStateManager.translate((float) x, (float) (y - i + 1 - off) * yscale, (float) z);
			bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);

			bufferbuilder.setTranslation(-blockpos.getX(), -blockpos.getY(), -blockpos.getZ());
			BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
			blockrendererdispatcher.getBlockModelRenderer().renderModel(te.getLevel(),
					blockrendererdispatcher.getModelForState(state), state, blockpos, bufferbuilder, false,
					Mth.getPositionRandom(blockpos));
			tessellator.draw();
			bufferbuilder.setTranslation(0, 0, 0);
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
		super.render(te, x, y, z, partialTicks, destroyStage, alpha);
	}
}
