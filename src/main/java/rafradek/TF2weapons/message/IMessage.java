package rafradek.TF2weapons.message;

import io.netty.buffer.ByteBuf;

/** Port shim for Forge 1.12's SimpleNetworkWrapper message interface. */
public interface IMessage {
	void fromBytes(ByteBuf buf);

	void toBytes(ByteBuf buf);
}
