package rafradek.TF2weapons.client.audio;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

public class OnFireSound extends AbstractTickableSoundInstance {

	public Entity target;

	public OnFireSound(SoundEvent soundIn, Entity target) {
		super(soundIn, target.getSoundSource(), SoundInstance.createUnseededRandom());
		x = (float) target.getX();
		y = (float) target.getY();
		z = (float) target.getZ();
		volume = 0.6f;
		pitch = 1f;
		this.target = target;
		looping = true;
	}

	@Override
	public void tick() {
		x = (float) target.getX();
		y = (float) target.getY();
		z = (float) target.getZ();
		if (target.tickCount - target.getPersistentData().getInt("LastHitBurn") > 5) this.stop();
	}

}
