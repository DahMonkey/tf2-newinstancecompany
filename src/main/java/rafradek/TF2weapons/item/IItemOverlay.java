package rafradek.TF2weapons.item;

import rafradek.TF2weapons.client.compat.ScaledResolution;
import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IItemOverlay {
	public boolean showInfoBox(ItemStack stack, Player player);

	public String[] getInfoBoxLines(ItemStack stack, Player player);

	@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
	public void drawOverlay(ItemStack stack, Player player, Tessellator tessellator, BufferBuilder buffer,
			ScaledResolution resolution);
}
