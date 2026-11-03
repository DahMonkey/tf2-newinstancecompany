package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.common.WeaponsCapability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class TF2InitClientHandler implements IMessageHandler<TF2Message.InitClientMessage, IMessage> {

	@Override
	public IMessage onMessage(TF2Message.InitClientMessage message, MessageContext ctx) {
		Player player = ctx.getServerHandler().player;
		ctx.context.enqueueWork(() -> {
			TF2PlayerCapability.get(player).breakBlocks = message.breakBlocks;
			WeaponsCapability.get(player).sentryTargets = message.sentryTargets;
			WeaponsCapability.get(player).dispenserPlayer = message.dispenserPlayer;
			WeaponsCapability.get(player).teleporterPlayer = message.teleporterPlayer;
			WeaponsCapability.get(player).teleporterEntity = message.teleporterEntity;
		});
		// System.out.println("setting "+message.value);
		// TF2weapons.proxy.playReloadSound(player,stack);
		return null;
	}
}
