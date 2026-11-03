package rafradek.TF2weapons.item;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IItemOverlay {
	public boolean showInfoBox(ItemStack stack, Player player);

	public String[] getInfoBoxLines(ItemStack stack, Player player);

	public void drawOverlay(ItemStack stack, Player player, Tesselator tessellator, BufferBuilder buffer,
			ScaledResolution resolution);
}
