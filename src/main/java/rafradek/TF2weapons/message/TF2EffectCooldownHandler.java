package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.message.TF2Message.EffectCooldownMessage;
import net.minecraft.client.Minecraft;

public class TF2EffectCooldownHandler implements IMessageHandler<TF2Message.EffectCooldownMessage, IMessage> {

	@Override
	public IMessage onMessage(final EffectCooldownMessage message, MessageContext ctx) {
		if (ctx.side.isClient())
			Minecraft.getInstance().execute(() -> {
				WeaponsCapability cap = rafradek.TF2weapons.client.ClientProxy.getLocalPlayer().getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
				cap.effectsCool.put(message.name, message.time);
				// cap.critTime = message.critTime;
				// cap.collectedHeads = message.heads;
			});
		return null;
	}

}
