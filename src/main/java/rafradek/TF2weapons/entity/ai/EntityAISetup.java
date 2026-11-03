package rafradek.TF2weapons.entity.ai;

import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.mercenary.EntityEngineer;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character.Order;
import rafradek.TF2weapons.item.ItemPDA;
import rafradek.TF2weapons.util.TF2Util;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityAISetup extends Goal {

	public EntityEngineer engineer;
	public int buildType;
	public boolean found;
	public Vec3 target;

	public EntityAISetup(EntityEngineer engineer) {
		this.engineer = engineer;
		this.setFlags(TF2Util.mutexFlags(3));
	}

	@Override
	public boolean canUse() {

		if (this.engineer.getMainWeapon() != -1)
			return false;

		if (this.engineer.getOwner() != null && this.engineer.getOrder() == Order.FOLLOW)
			return false;

		if (this.engineer.grabbed != null) {
			this.buildType = this.engineer.grabbedid + 1;
			return true;
		}
		if (this.engineer.isInWater() || !(this.engineer.loadout.getStackInSlot(3).getItem() instanceof ItemPDA))
			return false;

		boolean dispensernear = TF2Util.findAmmoSource(engineer, 16, false) != null;

		boolean sentryalive = this.engineer.sentry != null && this.engineer.sentry.isAlive();
		boolean dispenseralive = this.engineer.dispenser != null && this.engineer.dispenser.isAlive();

		boolean sentryhome = sentryalive
				&& this.engineer.isWithinHomeDistanceFromPosition(this.engineer.sentry.blockPosition());
		boolean dispenserhome = dispenseralive
				&& this.engineer.isWithinHomeDistanceFromPosition(this.engineer.dispenser.blockPosition());

		int sentryCost = EntityBuilding.getCost(0, this.engineer.loadout.getStackInSlot(2));
		if (sentryalive)
			sentryCost /= 2;
		int dispenserCost = EntityBuilding.getCost(1, this.engineer.loadout.getStackInSlot(2));
		if (dispenseralive)
			dispenserCost /= 2;

		buildType = (this.engineer.getWepCapability().getMetal() >= sentryCost && !(sentryhome)
				&& (dispensernear || this.engineer.getWepCapability().getMetal() >= sentryCost + dispenserCost)) ? 1
						: (this.engineer.getWepCapability().getMetal() >= dispenserCost && !dispenserhome) ? 2 : 0;
		if (buildType > 0) {
			this.engineer.loadout.getStackInSlot(3).getTag().putByte("Building", (byte) this.buildType);
			this.engineer.switchSlot(3);
			/*
			 * this.engineer.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new
			 * ItemStack(TF2weapons.itemBuildingBox, 1, 16 + buildType * 2 +
			 * this.engineer.getEntTeam()));
			 * this.engineer.getHeldItem(EnumHand.MAIN_HAND).setTagCompound(new
			 * NBTTagCompound());
			 */
		}
		// System.out.println("Promote: "+buildType);

		return buildType > 0;
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {
		this.engineer.getNavigation().stop();
		this.engineer.switchSlot(0);
		if (this.engineer.loadout.getStackInSlot(3).getItem() instanceof ItemPDA)
			this.engineer.loadout.getStackInSlot(3).getTag().putByte("Building", (byte) 0);
	}

	@Override
	public void tick() {
		if (this.buildType > 0) {
			if (this.target != null && this.engineer.distanceToSqr(this.target.x, this.target.y, this.target.z) < 2) {
				EntityBuilding building = this.spawn();
				if (this.engineer.grabbed == null)
					this.engineer.getWepCapability().setMetal(this.engineer.getWepCapability().getMetal()
							- EntityBuilding.getCost(this.buildType - 1, this.engineer.loadout.getStackInSlot(2)));

				if (building instanceof EntitySentry)
					this.engineer.sentry = (EntitySentry) building;
				else if (building instanceof EntityDispenser)
					this.engineer.dispenser = (EntityDispenser) building;
				this.engineer.grabbed = null;
				this.engineer.grabbedid = 0;
				return;
			}
			if (this.engineer.getNavigation().isDone()) {

				int size = 1 + buildType == 1 ? 1 : 0;
				Vec3 Vec3 = DefaultRandomPos.findRandomTarget(this.engineer, size + 1, size);
				if (Vec3 != null) {
					AABB box = new AABB(Vec3.x - 0.5, Vec3.y, Vec3.z - 0.5, Vec3.x + 0.5,
							Vec3.y + 1, Vec3.z + 0.5);
					List<AABB> list = this.engineer.level.getCollisionBoxes(this.engineer, box);

					if (list.isEmpty() && !this.engineer.level.isMaterialInBB(box, Material.WATER)) {
						this.engineer.getNavigation().moveTo(Vec3.x, Vec3.y, Vec3.z, 1);
						this.target = Vec3;
					}
				}
				/*
				 * for(AxisAlignedBB entry:list){ System.out.println(entry); }
				 */

			}
		}
	}

	public EntityBuilding spawn() {
		EntityBuilding building;
		if (buildType == 1) {
			if (this.engineer.sentry != null && this.engineer.sentry.isAlive()) {
				this.engineer.sentry.detonate();
			}
			building = new EntitySentry(this.engineer.level);
		} else {
			if (this.engineer.dispenser != null && this.engineer.dispenser.isAlive()) {
				this.engineer.dispenser.detonate();
			}
			building = new EntityDispenser(this.engineer.level);
		}

		ItemStack pda = this.engineer.loadout.getStackInSlot(3);
		if (building instanceof EntitySentry) {
			TF2Util.addModifierSafe(building, Attributes.FOLLOW_RANGE, new AttributeModifier(
					"upgraderange", TF2Attribute.getModifier("Sentry Range", pda, 1f, engineer) - 1f, 2), true);
			((EntitySentry) building).attackRateMult = TF2Attribute.getModifier("Sentry Fire Rate", pda, 1, engineer);
			((EntitySentry) building).setHeat((int) TF2Attribute.getModifier("Piercing", pda, 0, engineer));
		}
		TF2Util.addModifierSafe(building, Attributes.MAX_HEALTH,
				new AttributeModifier(EntityBuilding.UPGRADE_HEALTH_UUID, "upgradehealth",
						TF2Attribute.getModifier("Building Health", pda, 1f, engineer) - 1f, 2),
				true);
		if (building instanceof EntityDispenser) {
			((EntityDispenser) building).setRange(TF2Attribute.getModifier("Dispenser Range", pda, 1, engineer));
		}

		if (this.engineer.grabbed != null) {
			building.readFromNBT(this.engineer.grabbed);
			building.setConstructing(true);
			building.redeploy = true;
		}

		BlockState blockTarget = this.engineer.level.getBlockState(new BlockPos(target));
		if (!blockTarget.getBlock().isPassable(this.engineer.level, new BlockPos(target)))
			building.setPos(target.x, target.y + 1.3, target.z);
		else
			building.setPos(target.x, target.y + 0.3, target.z);
		building.setEntTeam(this.engineer.getEntTeam());
		building.setOwner(this.engineer);
		if (building instanceof EntitySentry && TF2Attribute.getModifier("Weapon Mode",
				this.engineer.loadout.getStackInSlot(2), 0, this.engineer) == 2)
			((EntitySentry) building).setMini(true);

		this.engineer.level.addFreshEntity(building);
		this.target = null;
		this.buildType = 0;
		this.stop();
		this.engineer.buildCount += 1;
		return building;
	}
}
