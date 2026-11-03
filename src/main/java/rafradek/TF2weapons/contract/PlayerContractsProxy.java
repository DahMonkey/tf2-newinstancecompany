package rafradek.TF2weapons.contract;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class PlayerContractsProxy extends ServerGamePacketListenerImpl {

	private ServerGamePacketListenerImpl origin;

	public PlayerContractsProxy(ServerGamePacketListenerImpl origin, MinecraftServer server, ServerPlayer player) {
		super(server, new Connection(EnumPacketDirection.SERVERBOUND), player);
		this.origin = origin;
	}

	@Override
	public void sendPacket(Packet<?> packet) {
		TF2weapons.network.sendTo(new TF2Message.ContractNewMessage(packet), player);
		origin.sendPacket(packet);
	}
}
