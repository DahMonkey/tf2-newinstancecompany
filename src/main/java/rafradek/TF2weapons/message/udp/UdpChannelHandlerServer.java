package rafradek.TF2weapons.message.udp;

import rafradek.TF2weapons.message.IMessage;
import rafradek.TF2weapons.message.IMessageHandler;
import rafradek.TF2weapons.message.MessageContext;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.internal.SocketUtils;
import rafradek.TF2weapons.TF2weapons;
import java.lang.reflect.Constructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;

public class UdpChannelHandlerServer extends SimpleChannelInboundHandler<DatagramPacket> {

	public TF2UdpServer server;

	private Constructor<MessageContext> constr;

	public UdpChannelHandlerServer(TF2UdpServer server) {
		this.server = server;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
		ByteBuf buffer = msg.content();

		int plid = buffer.readShort();
		int seq = buffer.readUnsignedShort();
		int msgid = buffer.readByte();

		if (msgid == (byte) -128) {
			server.outboundTargets.put((short) plid,
					SocketUtils.socketAddress(msg.sender().getAddress().getHostAddress(), msg.sender().getPort()));
		} else {
			ServerPlayer player = server.playerList.get((short) plid);
			IMessage message = TF2weapons.network.messages[msgid].newInstance();
			// buffer.discardReadBytes();
			message.fromBytes(buffer);
			IMessageHandler<IMessage, IMessage> handler = TF2weapons.network.handlerList.get(message.getClass());
			if (constr == null) {
				constr = MessageContext.class.getDeclaredConstructor(INetHandler.class, Dist.class);
				constr.setAccessible(true);
			}
			MessageContext context = constr.newInstance(player.connection, Dist.SERVER);
			handler.onMessage(message, context);
		}
		// System.out.println("PacketFrom: "+msg.sender().getAddress()+ "
		// "+msg.sender().getPort()+" ");

	}

}
