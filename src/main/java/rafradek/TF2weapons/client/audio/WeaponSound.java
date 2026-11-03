package rafradek.TF2weapons.client.audio;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.WeaponData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class WeaponSound extends AbstractTickableSoundInstance {
	public LivingEntity entity;
	public int type;
	public WeaponData conf;
	public boolean endsnextTick;
	public WeaponSound playOnEnd;

	public WeaponSound(SoundEvent sound, LivingEntity entity, int type, WeaponData conf) {
		super(sound, SoundSource.NEUTRAL);
		this.type = type;
		this.entity = entity;
		this.conf = conf;
		volume = entity instanceof Player ? TF2ConfigVars.gunVolume : TF2ConfigVars.mercenaryVolume;
	}

	@Override
	public void update() {
		if (endsnextTick) setDone();
		xPosF = (float) entity.getX();
		yPosF = (float) entity.getY();
		zPosF = (float) entity.getZ();
		if (/*
			 * !(entity instanceof EntityPlayer &&
			 * ((EntityPlayer)entity).inventory.currentItem != slot)
			 */ItemFromData.getData(entity.getItemInHand(InteractionHand.MAIN_HAND)) != conf || entity.isRemoved()) setDone();
	}

	public void setDone() {
		ClientProxy.fireSounds.remove(entity);
		if (playOnEnd != null) {
			Minecraft.getInstance().getSoundManager().playSound(playOnEnd);
			ClientProxy.fireSounds.put(entity, playOnEnd);
		}
		donePlaying = true;
	}
}
