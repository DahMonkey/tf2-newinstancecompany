package rafradek.TF2weapons.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public interface IEntityTF2 {

	default boolean hasHead() {
		return false;
	}

	default AABB getHeadBox() {
		return null;
	};

	default boolean hasDamageFalloff() {
		return true;
	}

	default boolean isBuilding() {
		return false;
	}

	default boolean isBackStabbable(LivingEntity attacker, ItemStack knife) {
		return true;
	}

	default float getBackstabDamageReduction(LivingEntity attacker, ItemStack knife, float mult) {
		return mult;
	}
}
