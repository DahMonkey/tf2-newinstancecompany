package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemChargingTarge extends ItemFromData {

	public ItemChargingTarge() {
		super();
		this.setMaxDamage(600);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player living, InteractionHand hand) {
		// if (!living.getCapability(TF2weapons.WEAPONS_CAP,
		// null).effectsCool.containsKey("Charging")) {
		ItemStack stack = living.getItemInHand(hand);
		if (ItemToken.allowUse(living, TF2Class.DEMOMAN)) {

			if (!world.isClientSide)
				living.addEffect(new MobEffectInstance(TF2weapons.charging,
						(int) TF2Attribute.getModifier("Effect Duration", stack, 40, living),
						(int) TF2Attribute.getModifier("Charge Step", stack, 0, living)));
			living.getCooldowns().addCooldown(this,
					(int) (280f / TF2Attribute.getModifier("Charge", stack, 1, living)));
		}
		// living.getCapability(TF2weapons.WEAPONS_CAP,
		// null).effectsCool.put("Charging", 280);
		// }
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	public static ItemStack getChargingShield(LivingEntity living) {
		if (living.getMainHandItem() != null && living.getMainHandItem().getItem() instanceof ItemChargingTarge)
			return living.getMainHandItem();
		else if (living.getOffhandItem() != null
				&& living.getOffhandItem().getItem() instanceof ItemChargingTarge)
			return living.getOffhandItem();
		else
			return ItemStack.EMPTY;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isBarVisible(ItemStack stack) {

		return super.isBarVisible(stack)
				|| Minecraft.getInstance().player.getEffect(TF2weapons.charging) != null;
		/*
		 * Integer value =
		 * Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP,
		 * null).effectsCool .get("Charging"); return value != null && value > 0;
		 */
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBarWidth(ItemStack stack) {
		return Math.round(13f * (1f - (float) this.getDurabilityForDisplayLegacy(stack)));
	}

	public double getDurabilityForDisplayLegacy(ItemStack stack) {
		if (Minecraft.getInstance().player.getEffect(TF2weapons.charging) != null)
			return 1 - ((double) Minecraft.getInstance().player.getEffect(TF2weapons.charging)
					.getDuration() / (double) 40);
		return (1d - super.getBarWidth(stack) / 13d);
		/*
		 * Integer value =
		 * Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP,
		 * null).effectsCool .get("Charging"); return (double) (value != null ? value :
		 * 0) / (double) 280;
		 */
	}

	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		if (TF2Util.isOre("plankWood", repair))
			return true;
		return super.isValidRepairItem(toRepair, repair);
	}

	@Override
	public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
		return net.minecraftforge.common.ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction)
				|| super.canPerformAction(stack, toolAction);
	}
}
