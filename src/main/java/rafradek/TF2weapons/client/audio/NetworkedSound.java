package rafradek.TF2weapons.client.audio;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class NetworkedSound extends AbstractTickableSoundInstance {

	private Entity parent;
	private boolean isStatic;
	private int id;

	public NetworkedSound(Entity parent, SoundEvent soundIn, SoundSource categoryIn, float volume, float pitch,
			int id, boolean looping) {
		super(soundIn, categoryIn, SoundInstance.createUnseededRandom());
		this.parent = parent;
		this.volume = volume;
		this.pitch = pitch;
		this.id = id;
		this.looping = looping;
	}

	public NetworkedSound(Vec3 pos, SoundEvent soundIn, SoundSource categoryIn, float volume, float pitch, int id,
			boolean looping) {
		super(soundIn, categoryIn, SoundInstance.createUnseededRandom());
		isStatic = true;
		this.volume = volume;
		this.pitch = pitch;
		this.id = id;
		this.looping = looping;
		x = (float) pos.x;
		y = (float) pos.y;
		z = (float) pos.z;
	}

	@Override
	public void tick() {
		if (isStatic) return;
		if (parent != null && !parent.isRemoved()) {
			x = (float) parent.getX();
			y = (float) parent.getY();
			z = (float) parent.getZ();
		} else this.stop();
	}

}
