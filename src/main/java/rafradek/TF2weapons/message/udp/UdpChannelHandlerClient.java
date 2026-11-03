package rafradek.TF2weapons.message.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import rafradek.TF2weapons.TF2weapons;
import java.lang.reflect.Constructor;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;

public class UdpChannelHandlerClient extends SimpleChannelInboundHandler<DatagramPacket> {

	private Constructor<MessageContext> constr;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
		ByteBuf buffer = msg.content();

		int seq = buffer.readUnsignedShort();
		int msgid = buffer.readByte();

		IMessage message = TF2weapons.network.messages[msgid].newInstance();
		// buffer.discardReadBytes();
		message.fromBytes(buffer);
		IMessageHandler<IMessage, IMessage> handler = TF2weapons.network.handlerList.get(message.getClass());
		if (constr == null) {
			constr = MessageContext.class.getDeclaredConstructor(INetHandler.class, Dist.class);
			constr.setAccessible(true);
		}
		MessageContext context = constr.newInstance(Minecraft.getInstance().player.connection, Dist.CLIENT);
		handler.onMessage(message, context);
		// System.out.println("PacketFrom: "+msg.sender().getAddress()+ "
		// "+msg.sender().getPort()+" ");

	}

}
