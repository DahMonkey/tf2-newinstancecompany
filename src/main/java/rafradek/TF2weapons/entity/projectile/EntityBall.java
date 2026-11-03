package rafradek.TF2weapons.entity.projectile;

import rafradek.TF2weapons.util.TF2Util;

import rafradek.TF2weapons.TF2weapons;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class EntityBall extends EntityProjectileSimple {

	public Vec3 throwPos;
	public boolean canBePickedUp;

	public EntityBall(Level world) {
		super(world);
		this.throwPos = new Vec3(0, 0, 0);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		super.initProjectile(shooter, hand, weapon);
		this.throwPos = this.position();
	}

	/*
	 * @Override public void onHitGround(int x, int y, int z, RayTraceResult mop) {
	 * if(!this.canBePickedUp){ super.onHitGround(x, y, z, mop); } }
	 */
	@Override
	public boolean useCollisionBox() {
		return true;
	}

	@Override
	public void onHitBlockX() {
		this.canBePickedUp = true;
		this.setDeltaMovement((-this.getDeltaMovement().x * 0.12), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (this.getDeltaMovement().y * 0.3), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (this.getDeltaMovement().z * 0.3));
	}

	@Override
	public void onHitBlockY(Block block) {
		this.canBePickedUp = true;
		this.setDeltaMovement((this.getDeltaMovement().x * 0.3), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (-this.getDeltaMovement().y * 0.12), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (this.getDeltaMovement().z * 0.3));
	}

	@Override
	public void onHitBlockZ() {
		this.canBePickedUp = true;
		this.setDeltaMovement((this.getDeltaMovement().x * 0.3), this.getDeltaMovement().y, this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, (this.getDeltaMovement().y * 0.3), this.getDeltaMovement().z);
		this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, (-this.getDeltaMovement().z * 0.12));
	}

	@Override
	public void onCollideWithPlayer(Player entityIn) {
		if (!this.level.isClientSide && this.canBePickedUp) {
			if (!this.infinite && entityIn.getInventory().add(TF2Util.metaStack(TF2weapons.itemAmmo, 1, 14)))
				this.discard();

		}
	}

	/*
	 * @Override public void onHitMob(Entity entityHit, RayTraceResult mop) {
	 * super.onHitMob(entityHit, mop); if(!this.world.isRemote){
	 *
	 * } }
	 */
	@Override
	public void discard() {
		if (this.impact) {
			this.impact = false;
			this.canBePickedUp = true;
		} else
			super.discard();
	}
}
