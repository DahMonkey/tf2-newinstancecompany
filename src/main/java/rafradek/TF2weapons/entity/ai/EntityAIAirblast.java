package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemAirblast;
import rafradek.TF2weapons.item.ItemFlameThrower;
import rafradek.TF2weapons.item.ItemMeleeWeapon;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.util.TF2Util;
import java.util.List;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityAIAirblast extends Goal {

	public int delay;
	public EntityTF2Character host;

	public EntityAIAirblast(EntityTF2Character entity) {
		host = entity;
		this.setFlags(TF2Util.mutexFlags(0));
	}

	@Override
	public boolean shouldExecute() {
		// System.out.println("executing
		// "+TF2ActionHandler.playerAction.server.get(host));
		// System.out.println("should execute:
		// "+(host.world.getDifficulty().getDifficultyId()>=2));
		return this.host.getMainHandItem().getItem() instanceof ItemFlameThrower
				&& ((ItemFlameThrower) this.host.getMainHandItem().getItem()).canAltFire(this.host.level, host,
						this.host.getMainHandItem())
				&& host.level.getDifficulty().getDifficultyId() >= 2;

	}

	@Override
	public void updateTask() {
		// System.out.println("executing
		// "+TF2ActionHandler.playerAction.server.get(host));
		boolean easier = host.level.getDifficulty() == Difficulty.NORMAL;
		delay--;
		if (delay > 0 || this.host.getRandom().nextFloat() > (easier ? 0.22f : 0.28f)) {
			host.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state &= 5;// System.out.println("reset:");
			return;
		}
		Vec3 eyeVec = new Vec3(host.getX(), host.getY() + host.getEyeHeight(), host.getZ());
		List<Entity> list = host.level.getEntitiesOfClass(Entity.class,
				new AABB(eyeVec.x - 5, eyeVec.y - 5, eyeVec.z - 5, eyeVec.x + 5, eyeVec.y + 5, eyeVec.z + 5));
		boolean airblast = false;
		for (Entity entity : list) {
			// System.out.println(entity+"
			// "+ItemFlameThrower.isPushable(host,entity));
			boolean hasmelee = false;
			if (entity instanceof LivingEntity) {
				ItemStack stack = ((LivingEntity) entity).getMainHandItem();
				hasmelee = stack.getItem() instanceof ItemMeleeWeapon || (!(stack.getItem() instanceof ItemWeapon)
						&& stack.getAttributeModifiers(EquipmentSlot.MAINHAND)
								.containsKey(Attributes.ATTACK_DAMAGE.getName()));
			}
			if (ItemAirblast.isPushable(host, entity)
					&& (hasmelee || entity instanceof ThrowableProjectile || entity instanceof Projectile)) {
				// System.out.println(entity);
				// System.out.println("dystans:
				// "+(entity.getDistanceSq(host.posX, host.posY +
				// (double)host.getEyeHeight(), host.posZ)<25));
				// System.out.println(TF2weapons.getTeam(entity)+"
				// "+TF2weapons.getTeam(host));
				airblast = entity.distanceToSqr(host.getX(), host.getY() + host.getEyeHeight(),
						host.getZ()) < (easier ? 16 : 25)
						&& TF2Util.lookingAt(host, (easier ? 40 : 60), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
								entity.getZ());
				if (airblast)
					break;
			}
		}
		if (airblast) {
			// System.out.println("airblast:");
			((ItemFlameThrower) this.host.getMainHandItem().getItem()).altUse(this.host.getMainHandItem(), host,
					this.host.level);
			this.delay = easier ? 30 : 18;
		} else
			host.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).state &= 5;
	}
}
