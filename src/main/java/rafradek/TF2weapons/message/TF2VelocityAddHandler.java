package rafradek.TF2weapons.message;

import rafradek.TF2weapons.message.TF2Message.VelocityAddMessage;
import net.minecraft.client.Minecraft;

public class TF2VelocityAddHandler implements IMessageHandler<TF2Message.VelocityAddMessage, IMessage> {

	@Override
	public IMessage onMessage(final VelocityAddMessage message, MessageContext ctx) {
		Minecraft.getInstance().player.push(message.x, message.y, message.z);
		Minecraft.getInstance().player.hasImpulse = message.airborne;

		return null;
	}

}
