package rafradek.TF2weapons.message;

/** Port shim for Forge 1.12's SimpleNetworkWrapper handler interface. Handlers run on the main thread. */
public interface IMessageHandler<REQ extends IMessage, REPLY extends IMessage> {
	REPLY onMessage(REQ message, MessageContext ctx);
}
