package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.message.TF2Message.WearableChangeMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class TF2WearableChangeHandler implements IMessageHandler<TF2Message.WearableChangeMessage, IMessage> {

	@Override
	public IMessage onMessage(final WearableChangeMessage message, MessageContext ctx) {
		Minecraft.getInstance().execute(() -> {
			Entity entity = rafradek.TF2weapons.client.ClientProxy.getClientLevel().getEntity(message.entityID);
			if (entity != null) {
				if (message.slot < 20)
					entity.getCapability(TF2weapons.INVENTORY_CAP).orElse(null).setInventorySlotContents(message.slot,
							message.stack);
				else if (entity instanceof EntityTF2Character) {
					((EntityTF2Character) entity).loadout.setStackInSlot(message.slot - 20, message.stack);
				}
			}

		});
		return null;
	}

}
