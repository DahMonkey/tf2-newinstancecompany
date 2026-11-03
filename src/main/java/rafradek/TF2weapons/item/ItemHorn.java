package rafradek.TF2weapons.item;

import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.common.WeaponsCapability.RageType;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemHorn extends LegacyItem implements IBackpackItem {

	public ItemHorn() {
		this.setMaxStackSize(1);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public void inventoryTick(ItemStack par1ItemStack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		this.checkItem(par1ItemStack, par2World, par3Entity, par4, par5);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity entityLiving, int timeLeft) {
		ItemStack backpack = ItemBackpack.getBackpack(entityLiving);
		if (backpack.getItem() instanceof ItemSoldierBackpack
				&& this.getUseDuration(stack) - timeLeft >= ItemFromData.getData(backpack)
						.getInt(PropertyType.FIRE_SPEED)
				&& WeaponsCapability.get(entityLiving).getRage(RageType.BANNER) >= 1f)
			((ItemSoldierBackpack) backpack.getItem()).setActive(entityLiving, backpack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack itemStackIn = player.getItemInHand(hand);
		ItemStack backpack = ItemBackpack.getBackpack(player);
		if (ItemToken.allowUse(player, TF2Class.SOLDIER) && backpack.getItem() instanceof ItemSoldierBackpack
				&& (WeaponsCapability.get(player).getRage(RageType.BANNER) >= 1f)) {
			player.startUsingItem(hand);
			if (TF2Util.getTeamForDisplay(player) == 1)
				player.playSound(ItemFromData.getSound(backpack, PropertyType.HORN_BLU_SOUND), 0.8f, 1f);
			else
				player.playSound(ItemFromData.getSound(backpack, PropertyType.HORN_RED_SOUND), 0.8f, 1f);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
		}
		return new InteractionResultHolder<>(InteractionResult.FAIL, itemStackIn);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isBarVisible(ItemStack stack) {
		if (!(ItemBackpack.getBackpack(Minecraft.getInstance().player).getItem() instanceof ItemSoldierBackpack))
			return false;
		return WeaponsCapability.get(Minecraft.getInstance().player).getRage(RageType.BANNER) != 1f;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13f * (1f - (float) this.getDurabilityForDisplayLegacy(stack)));
	}

	public double getDurabilityForDisplayLegacy(ItemStack stack) {
		if (!(ItemBackpack.getBackpack(Minecraft.getInstance().player).getItem() instanceof ItemSoldierBackpack))
			return 0;
		return 1 - WeaponsCapability.get(Minecraft.getInstance().player).getRage(RageType.BANNER);
	}
}
