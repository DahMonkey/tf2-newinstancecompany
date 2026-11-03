package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.message.TF2Message.CapabilityMessage;
import rafradek.TF2weapons.util.PropertyType;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class TF2CapabilityHandler implements IMessageHandler<TF2Message.CapabilityMessage, IMessage> {

	@Override
	public IMessage onMessage(final CapabilityMessage message, MessageContext ctx) {
		if (ctx.side.isClient())
			Minecraft.getInstance().execute(() -> {
				Entity ent = Minecraft.getInstance().level.getEntity(message.entityID);
				if (ent != null && ent.getCapability(TF2weapons.WEAPONS_CAP).isPresent()) {
					WeaponsCapability cap = ent.getCapability(TF2weapons.WEAPONS_CAP).orElse(null);
					int prevHealTarget = cap.getHealTarget();
					if (message.entries != null) {
						for (DataEntry<?> param : message.entries) {
							cap.onChangeValue(param.getKey(), param.getValue());
						}
						cap.entityData.setEntryValues(message.entries);
					}
					if (prevHealTarget != cap.getHealTarget() && cap.getHealTarget() > 0) {
						SoundEvent sound = ItemFromData.getSound(
								((LivingEntity) ent).getItemInHand(InteractionHand.MAIN_HAND),
								PropertyType.HEAL_START_SOUND);
						ClientProxy.playWeaponSound((LivingEntity) ent, sound, false, 0,
								((LivingEntity) ent).getItemInHand(InteractionHand.MAIN_HAND));
					}
					cap.ticksTotal = message.totalTime;
					// cap.critTime = message.critTime;
					// cap.collectedHeads = message.heads;
				}
			});
		/*
		 * if(ent !=null){ ent.getEntityData().setTag("TF2", message.tag); }
		 */
		else {
			/*
			 * ctx.getServerHandler().player.getCapability(TF2weapons.WEAPONS_CAP,
			 * null).setHealTarget(message.healTarget);
			 */
			/*
			 * message.entityID = ctx.getServerHandler().player.getEntityId();
			 * TF2weapons.sendTracking(message, ctx.getServerHandler().player);
			 */
		}
		return null;
	}

}
