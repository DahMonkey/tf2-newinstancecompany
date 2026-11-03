package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message.PlayerCapabilityMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class TF2PlayerCapabilityHandler implements IMessageHandler<PlayerCapabilityMessage, IMessage> {

	@Override
	public IMessage onMessage(final PlayerCapabilityMessage message, MessageContext ctx) {
		Minecraft.getInstance().execute(() -> {
			Entity ent = Minecraft.getInstance().level.getEntity(message.entityID);
			if (ent != null && ent.getCapability(TF2weapons.WEAPONS_CAP).isPresent()) {
				TF2PlayerCapability cap = ent.getCapability(TF2weapons.PLAYER_CAP).orElse(null);
				if (message.entries != null) {
					for (net.minecraft.network.syncher.SynchedEntityData.DataItem<?> param : message.entries) {
						cap.onChangeValue(param.getAccessor(), param.getValue());
					}
					if (message.global)
						cap.dataManagerGlobal.assignValues(message.entries);
					else
						cap.entityData.assignValues(message.entries);
				}
			}
		});
		return null;
	}

}
