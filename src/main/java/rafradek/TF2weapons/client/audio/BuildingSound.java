package rafradek.TF2weapons.client.audio;

import rafradek.TF2weapons.entity.building.EntityBuilding;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class BuildingSound extends AbstractTickableSoundInstance {

	public EntityBuilding sentry;
	private int state;

	public BuildingSound(EntityBuilding sentry, SoundEvent location, int state) {
		super(location, SoundSource.NEUTRAL);
		this.sentry = sentry;
		volume = 0.65f;
		repeat = true;
		this.state = state;
	}

	@Override
	public void update() {
		xPosF = (float) sentry.getX();
		yPosF = (float) sentry.getY();
		zPosF = (float) sentry.getZ();
		if (sentry.getHealth() <= 0 || sentry.isRemoved()) stopPlaying();
	}

	public void stopPlaying() {
		donePlaying = true;
	}

}
