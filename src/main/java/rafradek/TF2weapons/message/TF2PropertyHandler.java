package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemMedigun;
import rafradek.TF2weapons.message.TF2Message.PropertyMessage;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class TF2PropertyHandler implements IMessageHandler<TF2Message.PropertyMessage, IMessage> {

	@Override
	public IMessage onMessage(final PropertyMessage message, MessageContext ctx) {

		if (ctx.side.isClient())
			Minecraft.getInstance().execute(() -> {
				if (rafradek.TF2weapons.client.ClientProxy.getClientLevel() == null)
					return;
				Entity ent = rafradek.TF2weapons.client.ClientProxy.getClientLevel().getEntity(message.entityID);
				if (ent != null && ent.getCapability(TF2weapons.WEAPONS_CAP).isPresent()) {
					if (ent instanceof LivingEntity
							&& ((LivingEntity) ent).getItemInHand(InteractionHand.MAIN_HAND) != null
							&& ((LivingEntity) ent).getItemInHand(InteractionHand.MAIN_HAND)
									.getItem() instanceof ItemMedigun)
						if (message.name.equals("HealTarget")
								&& ent.getPersistentData().getInt("HealTarget") != message.intValue
								&& message.intValue > 0) {
							SoundEvent sound = ItemFromData.getSound(
									((LivingEntity) ent).getItemInHand(InteractionHand.MAIN_HAND),
									PropertyType.HEAL_START_SOUND);
							ClientProxy.playWeaponSound((LivingEntity) ent, sound, false, 0,
									((LivingEntity) ent).getItemInHand(InteractionHand.MAIN_HAND));
						}

					if (message.type == 0)
						ent.getPersistentData().putInt(message.name, message.intValue);
					else if (message.type == 1)
						ent.getPersistentData().putFloat(message.name, message.floatValue);
					else if (message.type == 2)
						ent.getPersistentData().putByte(message.name, message.byteValue);
					else if (message.type == 3)
						ent.getPersistentData().putString(message.name, message.stringValue);
				}
			});
		else {
			if (message.type == 0)
				ctx.getServerHandler().player.getPersistentData().putInt(message.name, message.intValue);
			else if (message.type == 1)
				ctx.getServerHandler().player.getPersistentData().putFloat(message.name, message.floatValue);
			else if (message.type == 2)
				ctx.getServerHandler().player.getPersistentData().putByte(message.name, message.byteValue);
			else if (message.type == 3)
				ctx.getServerHandler().player.getPersistentData().putString(message.name, message.stringValue);
			// System.out.println("send: "+message.name+" "+message.intValue+"
			// "+message.floatValue);
			message.entityID = ctx.getServerHandler().player.getId();
			TF2Util.sendTracking(message, ctx.getServerHandler().player);
		}
		return null;
	}

}
