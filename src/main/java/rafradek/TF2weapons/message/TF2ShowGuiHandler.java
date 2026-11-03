package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientHandler;
import rafradek.TF2weapons.message.TF2Message.ShowGuiMessage;
import net.minecraftforge.api.distmarker.Dist;

public class TF2ShowGuiHandler implements IMessageHandler<TF2Message.ShowGuiMessage, IMessage> {

	@Override
	public IMessage onMessage(final ShowGuiMessage message, MessageContext ctx) {
		if (ctx.side == Dist.SERVER) {
			if (message.id != 99)
				TF2weapons.openGui((net.minecraft.world.entity.player.Player) ctx.getServerHandler().player, message.id,
						ctx.getServerHandler().player.level, 0, 0, 0);
			else {

			}
		} else {
			if (message.id == 100) {
				ClientHandler.createGui(message);
			}
		}
		return null;
	}

}
