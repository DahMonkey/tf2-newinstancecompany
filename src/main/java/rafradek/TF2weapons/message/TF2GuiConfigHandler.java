package rafradek.TF2weapons.message;

import rafradek.TF2weapons.client.ClientHandler;
import rafradek.TF2weapons.message.TF2Message.GuiConfigMessage;
import rafradek.TF2weapons.tileentity.IEntityConfigurable;
import java.util.HashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;

public class TF2GuiConfigHandler implements IMessageHandler<TF2Message.GuiConfigMessage, IMessage> {

	public static HashMap<Entity, float[]> shotInfo = new HashMap<>();

	@Override
	public IMessage onMessage(final GuiConfigMessage message, final MessageContext ctx) {

		if (ctx.side == Dist.SERVER) {
			final ServerPlayer player = ctx.getServerHandler().player;
			((ServerLevel) player.level).addScheduledTask(() -> {
				BlockEntity ent = player.level.getBlockEntity(message.getBlockPos());
				if (ent instanceof IEntityConfigurable) {
					((IEntityConfigurable) ent).readConfig(message.getTag());
				}
				/*
				 * if (player.openContainer instanceof ContainerConfigurable) {
				 * ((ContainerConfigurable)player.openContainer).config.readConfig(message.tag);
				 * }
				 */
			});
		} else ClientHandler.createConfigGui(message);

		return null;
	}

}
