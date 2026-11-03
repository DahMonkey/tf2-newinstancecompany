package rafradek.TF2weapons.client.audio;

import net.minecraft.client.resources.sounds.SoundInstance;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class BuildingSound extends AbstractTickableSoundInstance {

	public EntityBuilding sentry;
	private int state;

	public BuildingSound(EntityBuilding sentry, SoundEvent location, int state) {
		super(location, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
		this.sentry = sentry;
		volume = 0.65f;
		looping = true;
		this.state = state;
	}

	@Override
	public void tick() {
		x = (float) sentry.getX();
		y = (float) sentry.getY();
		z = (float) sentry.getZ();
		if (sentry.getHealth() <= 0 || sentry.isRemoved()) stopPlaying();
	}

	public void stopPlaying() {
		this.stop();
	}

}
