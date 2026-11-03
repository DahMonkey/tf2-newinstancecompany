package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.common.WeaponsCapability.RageType;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemSoldierBackpack extends ItemBackpack {

	public static ItemStack horn = ItemStack.EMPTY;

	public ItemSoldierBackpack() {
		super();
		this.addPropertyOverride(new ResourceLocation("active"), (ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) -> {
			if (entityIn != null && WeaponsCapability.get(entityIn) != null
					&& WeaponsCapability.get(entityIn).isRageActive(RageType.BANNER))
				return 1;
			return 0;
		});

	}

	public MobEffect getBuff(ItemStack stack) {
		return MobEffect.getPotionFromResourceLocation(getData(stack).getString(PropertyType.EFFECT_TYPE));
	}

	@Override
	public RageType getRageType(ItemStack stack, LivingEntity living) {
		return RageType.BANNER;
	}

	@Override
	public float getMaxRage(ItemStack stack, LivingEntity living) {
		return 1f;
	}

	@Override
	public ItemStack getBackpackItemToUse(ItemStack stack, LivingEntity player) {
		if (horn.isEmpty())
			horn = new ItemStack(TF2weapons.itemHorn);
		return horn;
	}

	public void addRage(ItemStack stack, float damage, LivingEntity target, LivingEntity attacker) {
		if (target instanceof EntityTF2Character && !(attacker instanceof Player))
			damage *= 0.5f;
		else if (!(target instanceof Player))
			damage *= 0.35f;
		this.addRage(stack, attacker, damage / getData(stack).getFloat(PropertyType.DAMAGE));
	}

	@Override
	public void onArmorTickAny(Level world, final LivingEntity player, ItemStack itemStack) {
		super.onArmorTickAny(world, player, itemStack);
		if (!world.isClientSide) {
			if (player.tickCount % 5 == 0 && WeaponsCapability.get(player).isRageActive(RageType.BANNER)) {
				/*
				 * ); itemStack.getTagCompound().setFloat("Rage", Math.max(0f,
				 * itemStack.getTagCompound().getFloat("Rage") - 1f / duration)); if
				 * (itemStack.getTagCompound().getFloat("Rage") <= 0)
				 * itemStack.getTagCompound().setBoolean("Active", false);
				 */
				for (LivingEntity living : world.getEntitiesOfClass(LivingEntity.class,
						player.getBoundingBox().inflate(10, 10, 10), input -> TF2Util.isAlliedTo(player, input)))
					TF2Util.addAndSendEffect(living, new MobEffectInstance(this.getBuff(itemStack), 25));
			}
		}
	}

	public void setActive(LivingEntity player, ItemStack stack) {
		float duration = TF2Attribute.getModifier("Effect Duration", stack,
				getData(stack).getInt(PropertyType.DURATION), player) * (TF2ConfigVars.longDurationBanner ? 2 : 5);
		WeaponsCapability.get(player).setRageActive(RageType.BANNER, true, (1f / duration) * 20f);
	}
}
