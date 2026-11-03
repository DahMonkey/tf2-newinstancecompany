package rafradek.TF2weapons.message;

import rafradek.TF2weapons.message.TF2NetworkWrapper.TargetPoint;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import rafradek.TF2weapons.TF2weapons;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Replacement for the 1.12 SimpleNetworkWrapper subclass. Keeps the old send API so the rest of the
 * mod is unchanged. The optional UDP transport of the original is not ported; everything goes over
 * the normal connection.
 */
public class TF2NetworkWrapper {

	private static final String PROTOCOL = "2";

	public boolean useUdp;

	public final SimpleChannel channel;

	private final Map<Class<?>, IMessageHandler<IMessage, IMessage>> handlers = new HashMap<>();

	public TF2NetworkWrapper(String channelName) {
		channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(channelName, "main"), () -> PROTOCOL,
				PROTOCOL::equals, PROTOCOL::equals);
	}

	/**
	 * Registers a message. In 1.12 the same message class could be registered once per side with
	 * different ids; here each class is registered once and its handler receives both directions.
	 */
	@SuppressWarnings("unchecked")
	public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
			Class<? extends IMessageHandler<REQ, REPLY>> handlerClass, Class<REQ> type, int discriminator,
			Dist side, boolean udp) {
		if (handlers.containsKey(type))
			return;
		IMessageHandler<REQ, REPLY> handler;
		try {
			handler = handlerClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Cannot create message handler " + handlerClass, e);
		}
		handlers.put(type, (IMessageHandler<IMessage, IMessage>) (IMessageHandler<?, ?>) handler);
		channel.messageBuilder(type, discriminator)
				.encoder((msg, buf) -> msg.toBytes(buf))
				.decoder(buf -> {
					try {
						REQ msg = type.getDeclaredConstructor().newInstance();
						msg.fromBytes(buf);
						return msg;
					} catch (ReflectiveOperationException e) {
						throw new RuntimeException("Cannot create message " + type, e);
					}
				})
				.consumerMainThread((REQ msg, Supplier<NetworkEvent.Context> ctx) -> {
					MessageContext context = new MessageContext(ctx.get());
					REPLY reply = handler.onMessage(msg, context);
					if (reply != null) {
						if (context.getSender() != null)
							sendTo(reply, context.getSender());
						else
							sendToServer(reply);
					}
				}).add();
	}

	public void sendToAll(IMessage message) {
		channel.send(PacketDistributor.ALL.noArg(), message);
	}

	public void sendTo(IMessage message, ServerPlayer player) {
		if (player.connection != null)
			channel.send(PacketDistributor.PLAYER.with(() -> player), message);
	}

	public void sendToServer(IMessage message) {
		channel.sendToServer(message);
	}

	public void sendToAllAround(IMessage message, TargetPoint point) {
		channel.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(point.x, point.y, point.z,
				point.range, point.dimension)), message);
	}

	public void sendToAllTracking(IMessage message, Entity entity) {
		channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
	}

	public void sendToDimension(IMessage message, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {
		channel.send(PacketDistributor.DIMENSION.with(() -> dimension), message);
	}

	/** 1.12 NetworkRegistry.TargetPoint replacement. */
	public static class TargetPoint {
		public final double x, y, z, range;
		public final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;

		public TargetPoint(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension, double x,
				double y, double z, double range) {
			this.dimension = dimension;
			this.x = x;
			this.y = y;
			this.z = z;
			this.range = range;
		}

		public TargetPoint(net.minecraft.world.level.Level level, double x, double y, double z, double range) {
			this(level.dimension(), x, y, z, range);
		}
	}

	public static FriendlyByteBuf wrap(io.netty.buffer.ByteBuf buf) {
		return buf instanceof FriendlyByteBuf ? (FriendlyByteBuf) buf : new FriendlyByteBuf(buf);
	}

	public static FriendlyByteBuf buffer() {
		return new FriendlyByteBuf(Unpooled.buffer());
	}
}
