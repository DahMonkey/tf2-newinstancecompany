package rafradek.TF2weapons.common;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import rafradek.TF2weapons.message.MessageContext;

/** Server-side proxy; the client overrides these in ClientProxy. */
public class CommonProxy {

	/** Called from the mod constructor so the client can add its mod-bus listeners. */
	public void construct(IEventBus modBus) {
	}

	public void registerRenderInformation() {
	}

	public void registerTicks() {
	}

	public void playReloadSound(LivingEntity player, ItemStack stack) {
	}

	public void preInit() {
	}

	public Player getPlayerForSide(MessageContext ctx) {
		return ctx.getSender();
	}

	public void registerItemBlock(BlockItem item) {
	}

	public void displayCorruptedFileError() {
	}

	/** Starts loading the skin of the player a spy is disguised as (client only). */
	public void loadDisguiseSkin(WeaponsCapability cap, String playerName) {
	}

	/** Slows the mouse while scoped with a sniper rifle (client only). */
	public void updateSniperMouseSlow(WeaponsCapability cap) {
	}

	/** Sends the local player's movement keys to the server (client only). */
	public void sendMovementInput(Player player) {
	}
}
