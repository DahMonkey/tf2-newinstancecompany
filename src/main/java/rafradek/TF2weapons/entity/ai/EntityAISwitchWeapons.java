package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.util.PropertyType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

public class EntityAISwitchWeapons extends Goal {

	public EntityTF2Character host;
	public int counter;

	public EntityAISwitchWeapons(EntityTF2Character ent) {
		this.host = ent;
	}

	@Override
	public boolean canUse() {
		return ++this.counter > 2 && this.host.getDiff() > 2 && this.host.getTarget() != null;
	}

	@Override
	public void stop() {}

	@Override
	public void tick() {
		this.counter = 0;
		// System.out.println("Tick");
		LivingEntity target = this.host.getTarget();
		ItemStack stack = this.host.getMainHandItem();
		ItemStack stack2 = this.host.getMainHandItem();
		if (!stack.isEmpty() && stack.getItem() instanceof ItemWeapon) {
			boolean outRange = ItemFromData.getData(stack).getFloat(PropertyType.EFFICIENT_RANGE) != 0 && this.host
					.distanceToSqr(target) > ItemFromData.getData(stack).getFloat(PropertyType.EFFICIENT_RANGE);
			boolean outAmmo = ((ItemWeapon) stack.getItem()).hasClip(stack)
					&& stack.getDamageValue() == stack.getMaxDamage();
			if (this.host.usedSlot == 0 && (outRange || outAmmo)) {
				this.host.usedSlot = 1;
			} else if (this.host.usedSlot == 1 && (outRange || outAmmo)) {
				this.host.usedSlot = 0;
			}
		}
		// this.host.getLookHelper().setLookPosition(target.posX+x*2,target.posY+target.getEyeHeight(),target.posZ+z*2,
		// 90f, 90.0F);

	}
}
