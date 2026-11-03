package rafradek.TF2weapons.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IWeaponItem {

	float getWeaponDamage(ItemStack stack, LivingEntity living, Entity target);

}
