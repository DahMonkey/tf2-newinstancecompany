package rafradek.TF2weapons.message;

import rafradek.TF2weapons.client.ClientHandler;
import rafradek.TF2weapons.message.TF2Message.ParticleSpawnMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TF2ParticleSpawnHandler implements IMessageHandler<ParticleSpawnMessage, IMessage> {

	@Override
	@OnlyIn(Dist.CLIENT)
	public IMessage onMessage(final ParticleSpawnMessage message, MessageContext ctx) {
		Minecraft.getInstance().execute(() -> {
			ClientHandler.processParticleSpawnMessage(message);
		});
		return null;
	}

}
