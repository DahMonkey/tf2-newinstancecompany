package rafradek.TF2weapons.message;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

/** Port shim for Forge 1.12's MessageContext. {@code side} is the logical side receiving the message. */
public class MessageContext {
	public final Dist side;
	public final NetworkEvent.Context context;

	public MessageContext(NetworkEvent.Context context) {
		this.context = context;
		this.side = context.getDirection().getReceptionSide().isClient() ? Dist.CLIENT : Dist.DEDICATED_SERVER;
	}

	public ServerGamePacketListenerImpl getServerHandler() {
		ServerPlayer player = context.getSender();
		return player != null ? player.connection : null;
	}

	public ServerPlayer getSender() {
		return context.getSender();
	}
}
