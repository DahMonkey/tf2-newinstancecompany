package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.client.ClientProxy;

import java.util.Map.Entry;

/** Applies the server's networked config values on the client. */
public class TF2InitHandler implements IMessageHandler<TF2Message.InitMessage, IMessage> {

	@Override
	public IMessage onMessage(TF2Message.InitMessage message, MessageContext ctx) {
		try {
			for (Entry<String, String> entry : message.property.entrySet())
				TF2ConfigVars.applyNetworked(entry.getKey(), entry.getValue());
			ClientProxy.buildingsUseEnergy = message.energyUse;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
