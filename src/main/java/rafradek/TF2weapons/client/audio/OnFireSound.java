package rafradek.TF2weapons.client.audio;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

public class OnFireSound extends AbstractTickableSoundInstance {

	public Entity target;

	public OnFireSound(SoundEvent soundIn, Entity target) {
		super(soundIn, target.getSoundCategory());
		xPosF = (float) target.getX();
		yPosF = (float) target.getY();
		zPosF = (float) target.getZ();
		volume = 0.6f;
		pitch = 1f;
		this.target = target;
		repeat = true;
	}

	@Override
	public void update() {
		xPosF = (float) target.getX();
		yPosF = (float) target.getY();
		zPosF = (float) target.getZ();
		if (target.tickCount - target.getPersistentData().getInt("LastHitBurn") > 5) donePlaying = true;
	}

}
