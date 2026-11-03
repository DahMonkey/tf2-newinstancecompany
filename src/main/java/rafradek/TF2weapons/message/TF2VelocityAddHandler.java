package rafradek.TF2weapons.message;

import rafradek.TF2weapons.message.TF2Message.VelocityAddMessage;
import net.minecraft.client.Minecraft;

public class TF2VelocityAddHandler implements IMessageHandler<TF2Message.VelocityAddMessage, IMessage> {

	@Override
	public IMessage onMessage(final VelocityAddMessage message, MessageContext ctx) {
		rafradek.TF2weapons.client.ClientProxy.getLocalPlayer().push(message.x, message.y, message.z);
		rafradek.TF2weapons.client.ClientProxy.getLocalPlayer().hasImpulse = message.airborne;

		return null;
	}

}
