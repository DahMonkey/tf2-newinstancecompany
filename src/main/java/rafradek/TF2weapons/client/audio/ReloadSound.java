package rafradek.TF2weapons.client.audio;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class ReloadSound extends AbstractSoundInstance implements TickableSoundInstance {

	public boolean done;

	public ReloadSound(SoundEvent soundResource, Entity entity) {
		super(soundResource, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
		x = (float) entity.getX();
		y = (float) entity.getY();
		z = (float) entity.getZ();
		volume = 0.6f;
	}

	@Override
	public void tick() {}

	@Override
	public boolean isStopped() {
		return done;
	}

}
