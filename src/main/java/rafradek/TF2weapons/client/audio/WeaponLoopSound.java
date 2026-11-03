package rafradek.TF2weapons.client.audio;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemUsable;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class WeaponLoopSound extends WeaponSound {

	private boolean firing;
	private boolean crit;

	public WeaponLoopSound(SoundEvent sound, LivingEntity entity, boolean firing, WeaponData conf, boolean crit, int type) {
		super(sound, entity, type, conf);
		looping = true;
		this.firing = firing;
		this.crit = crit;
	}

	@Override
	public void tick() {
		super.tick();
		if (endsnextTick || this.isStopped()) return;
		ItemStack stack = entity.getItemInHand(InteractionHand.MAIN_HAND);
		boolean boost = TF2Util.calculateCritPre(stack, entity) == 2;
		boolean playThis = (boost && crit) || (!boost && !crit);
		if (((ItemUsable) stack.getItem()).canFireInternal(entity.level, entity, stack,
				InteractionHand.MAIN_HAND)/*
									 * stack.getTagCompound().getShort("minigunticks")>=17*
									 * TF2Attribute.getModifier("Minigun Spinup", stack, 1,entity)
									 */) {
			int action = entity.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state;
			if (((action & 1) != 0 && firing && playThis) || (!firing && (action & 3) == 2)) {
				// this.volume=1.0f;
			} else setDone();
		} else
			this.setDone();
	}

}
