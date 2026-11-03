package rafradek.TF2weapons.common;

import rafradek.TF2weapons.message.MessageContext;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class CommonProxy {
	public void registerRenderInformation() {
		// unused server side. -- see ClientProxy for implementation
	}

	public void registerTicks() {}

	public void playReloadSound(LivingEntity player, ItemStack stack) {}

	public void preInit() {}

	public Player getPlayerForSide(MessageContext ctx) {
		return ctx.getServerHandler().player;
	}

	public void registerItemBlock(BlockItem item) {}

	public void displayCorruptedFileError() {}

}