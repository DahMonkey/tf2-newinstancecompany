package rafradek.TF2weapons.message;

import rafradek.TF2weapons.client.ClientHandler;
import rafradek.TF2weapons.message.TF2Message.NetworkedSoundMessage;

public class TF2NetworkedSoundHandler implements IMessageHandler<TF2Message.NetworkedSoundMessage, IMessage> {

	@Override
	public IMessage onMessage(final NetworkedSoundMessage message, MessageContext ctx) {
		ctx.context.enqueueWork(() -> ClientHandler.playNetworkedSound(message));
		return null;
	}

}
