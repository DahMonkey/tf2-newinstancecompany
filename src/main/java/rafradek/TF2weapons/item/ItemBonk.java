package rafradek.TF2weapons.item;

import rafradek.TF2weapons.util.TF2Util;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.util.PropertyType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBonk extends ItemFromData {
	public ItemBonk() {
		super();
		this.setMaxStackSize(64);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isBarVisible(ItemStack stack) {
		Integer value = Minecraft.getInstance().player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).effectsCool
				.get(getData(stack).getName());
		return value != null && value > 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBarWidth(ItemStack stack) {
		return Math.round(13f * (1f - (float) this.getDurabilityForDisplayLegacy(stack)));
	}

	public double getDurabilityForDisplayLegacy(ItemStack stack) {
		Integer value = Minecraft.getInstance().player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).effectsCool
				.get(getData(stack).getName());
		return (double) (value != null ? value : 0) / (double) ((int) (getData(stack).getInt(PropertyType.COOLDOWN)
				* (TF2ConfigVars.fastItemCooldown ? 1f : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack itemStackIn = player.getItemInHand(hand);
		Integer value = player.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).effectsCool
				.get(getData(itemStackIn).getName());
		if (value == null || value <= 0) {
			player.startUsingItem(hand);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
		}
		return new InteractionResultHolder<>(InteractionResult.FAIL, itemStackIn);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {

		entityLiving.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).effectsCool.put(getData(stack).getName(),
				(int) (getData(stack).getInt(PropertyType.COOLDOWN)
						* (TF2ConfigVars.fastItemCooldown ? 1f : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
		entityLiving.addEffect(new MobEffectInstance(
				TF2Util.getEffect(getData(stack).getString(PropertyType.EFFECT_TYPE)),
				TF2ConfigVars.longDurationBanner ? ItemFromData.getData(stack).getInt(PropertyType.DURATION) : 160));
		if (!TF2ConfigVars.freeUseItems
				&& !(entityLiving instanceof Player && ((Player) entityLiving).getAbilities().instabuild))
			stack.shrink(1);
		return stack;
	}
}
