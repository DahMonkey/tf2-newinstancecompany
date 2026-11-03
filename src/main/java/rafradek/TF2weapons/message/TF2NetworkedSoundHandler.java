package rafradek.TF2weapons.message;

import rafradek.TF2weapons.client.audio.NetworkedSound;
import rafradek.TF2weapons.message.TF2Message.NetworkedSoundMessage;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.Entity;

public class TF2NetworkedSoundHandler implements IMessageHandler<TF2Message.NetworkedSoundMessage, IMessage> {

	HashMap<Integer, SoundInstance> sounds = new HashMap<>();

	@Override
	public IMessage onMessage(final NetworkedSoundMessage message, MessageContext ctx) {
		Minecraft.getInstance().execute(() -> {
			if (sounds.containsKey(message.id))
				sounds.remove(message.id);
			NetworkedSound sound;
			if (message.pos != null)
				sound = new NetworkedSound(message.pos, message.event, message.category, message.volume, message.pitch,
						message.id, message.repeat);
			else {
				Entity entity = rafradek.TF2weapons.client.ClientProxy.getClientLevel().getEntity(message.target);
				sound = new NetworkedSound(entity, message.event, message.category, message.volume, message.pitch,
						message.id, message.repeat);
			}
			sounds.put(message.id, sound);
			Minecraft.getInstance().getSoundManager().isActive(sound);
		});
		return null;
	}

}
