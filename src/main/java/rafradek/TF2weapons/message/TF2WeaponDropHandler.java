package rafradek.TF2weapons.message;

import rafradek.TF2weapons.client.ClientHandler;
import rafradek.TF2weapons.message.TF2Message.WeaponDroppedMessage;
import net.minecraft.client.Minecraft;

public class TF2WeaponDropHandler implements IMessageHandler<TF2Message.WeaponDroppedMessage, IMessage> {

	@Override
	public IMessage onMessage(final WeaponDroppedMessage message, MessageContext ctx) {
		Minecraft.getInstance().execute(() -> ClientHandler.playerDropWeapon(message.name));
		return null;
	}

}
