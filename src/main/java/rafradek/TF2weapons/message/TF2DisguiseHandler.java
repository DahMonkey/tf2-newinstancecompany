package rafradek.TF2weapons.message;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.item.ItemDisguiseKit;
import rafradek.TF2weapons.message.TF2Message.DisguiseMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class TF2DisguiseHandler implements IMessageHandler<TF2Message.DisguiseMessage, IMessage> {

	@Override
	public IMessage onMessage(final DisguiseMessage message, MessageContext ctx) {
		final ServerPlayer player = ctx.getServerHandler().player;
		ctx.context.enqueueWork(() -> {
			ItemStack stack;
			if (((stack = player.getMainHandItem()) != null && stack.getItem() instanceof ItemDisguiseKit)
					|| ((stack = player.getOffhandItem()) != null
							&& stack.getItem() instanceof ItemDisguiseKit)) {
				ItemDisguiseKit.startDisguise(player, player.level, message.value);
				if (!player.getAbilities().instabuild && !TF2ConfigVars.freeUseItems)
					stack.hurtAndBreak(1, player, broken -> {});
			}
		});
		return null;
	}

}
