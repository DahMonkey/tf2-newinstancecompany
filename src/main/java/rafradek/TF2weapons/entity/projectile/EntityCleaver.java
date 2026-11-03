package rafradek.TF2weapons.entity.projectile;

import net.minecraft.world.phys.BlockHitResult;
import rafradek.TF2weapons.TF2weapons;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityCleaver extends EntityProjectileSimple {

	public EntityCleaver(Level world) {
		this(TF2weapons.CLEAVER, world);
	}

	public EntityCleaver(EntityType<? extends EntityCleaver> type, Level world) {
		super(type, world);
		this.setProjType(2);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		super.initProjectile(shooter, hand, weapon);
		this.usedWeapon.setCount(1);
	}

	@Override
	public void onHitGround(int x, int y, int z, BlockHitResult mop) {
		super.onHitGround(x, y, z, mop);
		if (!this.level.isClientSide && this.damage <= 0 && !this.infinite) {
			this.spawnAtLocation(this.usedWeapon, 0f);
		}
	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		super.onHitMob(entityHit, mop);
		if (!this.level.isClientSide && this.isRemoved() && !this.infinite) {
			if (entityHit.isAlive()) {
				ListTag list = entityHit.getPersistentData().getList("Cleavers", 10);
				list.add(this.usedWeapon.serializeNBT());
				if (!entityHit.getPersistentData().contains("Cleavers"))
					entityHit.getPersistentData().put("Cleavers", list);
			} else
				this.spawnAtLocation(this.usedWeapon, 0f);
		}
	}

	@Override
	public double getGravity() {
		return 0.05f;
	}
}
