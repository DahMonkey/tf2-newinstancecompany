package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.item.ItemAmmo;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemUsable;
import rafradek.TF2weapons.item.ItemWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TF2UseHandler implements IMessageHandler<TF2Message.UseMessage, IMessage> {

	@Override
	public IMessage onMessage(TF2Message.UseMessage message, MessageContext ctx) {
		Player player = TF2weapons.proxy.getPlayerForSide(ctx);
		if (player != null) {
			ItemStack stack = player.getItemInHand(message.hand);
			if (!stack.isEmpty() && stack.getItem() instanceof ItemUsable) {
				if (message.newAmmo != -1) {
					if (TF2Attribute.getModifier("Ball Release", stack, 0, player) != 0)
						stack = ItemFromData.getNewStack("sandmanball");
					if (((ItemUsable) stack.getItem()).getAmmoType(stack) < ItemAmmo.AMMO_TYPES.length)
						player.getCapability(TF2weapons.PLAYER_CAP).orElse(null).cachedAmmoCount[((ItemUsable) stack.getItem())
								.getAmmoType(stack)] = message.newAmmo;
				}

				if (message.value != -1 && stack.getItem() instanceof ItemWeapon
						&& ((ItemWeapon) stack.getItem()).hasClip(stack)) {
					((ItemWeapon) stack.getItem()).setClip(stack, message.value);
					final ItemStack fstack = stack;
					if (message.reload) {
						WeaponsCapability.get(player).reloadCool = ((ItemWeapon) stack.getItem())
								.getWeaponFirstReloadTime(stack, player);
						WeaponsCapability.get(player).reloadingHand = message.hand;
						Minecraft.getInstance().addScheduledTask(() -> {
							TF2weapons.proxy.playReloadSound(player, fstack);
						});
					} else {
						WeaponsCapability.get(player).stopReload();
					}

				}
			}
		}
		return null;
	}
}
