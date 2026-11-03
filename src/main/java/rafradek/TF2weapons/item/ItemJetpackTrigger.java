package rafradek.TF2weapons.item;

import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemJetpackTrigger extends ItemUsable implements IBackpackItem {

	public ItemJetpackTrigger() {
		super();
		this.setCreativeTab(TF2weapons.tabutilitytf2);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isBarVisible(ItemStack stack) {
		if (!(ItemBackpack.getBackpack(rafradek.TF2weapons.client.ClientProxy.getLocalPlayer()).getItem() instanceof ItemJetpack))
			return false;
		return ItemBackpack.getBackpack(rafradek.TF2weapons.client.ClientProxy.getLocalPlayer()).getTag().getShort("Charge") > 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBarWidth(ItemStack stack) {
		return Math.round(13f * (1f - (float) this.getDurabilityForDisplayLegacy(stack)));
	}

	public double getDurabilityForDisplayLegacy(ItemStack stack) {
		ItemStack backpack = ItemBackpack.getBackpack(rafradek.TF2weapons.client.ClientProxy.getLocalPlayer());
		if (!(backpack.getItem() instanceof ItemJetpack))
			return 0;
		return (double) (backpack.getTag().getShort("Charge"))
				/ ((ItemJetpack) backpack.getItem()).getCooldown(stack, null);
	}

	@Override
	public void inventoryTick(ItemStack par1ItemStack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		super.inventoryTick(par1ItemStack, par2World, par3Entity, par4, par5);
		this.checkItem(par1ItemStack, par2World, par3Entity, par4, par5);
	}

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		ItemStack jetpack = ItemBackpack.getBackpack(living);
		if (!world.isClientSide && jetpack.getItem() instanceof ItemJetpack
				&& ((ItemJetpack) jetpack.getItem()).canActivate(jetpack, living)) {
			((ItemJetpack) jetpack.getItem()).activateJetpack(jetpack, living, false);
		}
		return false;
	}

	@Override
	public void altUse(ItemStack stack, LivingEntity living, Level world) {
		this.use(stack, living, world, InteractionHand.MAIN_HAND, null);
	}

	@Override
	public boolean fireTick(ItemStack stack, LivingEntity living, Level world) {
		return false;
	}

	@Override
	public boolean altFireTick(ItemStack stack, LivingEntity living, Level world) {
		return false;
	}

	@Override
	public short getAltFiringSpeed(ItemStack item, LivingEntity player) {
		return (short) this.getFiringSpeed(item, player);
	}

	@Override
	public boolean showInfoBox(ItemStack stack, Player player) {
		return true;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, Player player) {
		ItemStack backpack = ItemBackpack.getBackpack(player);
		if (backpack.getItem() instanceof ItemJetpack) {
			String charge = "";
			int progress = 20 - (int) ((float) backpack.getTag().getShort("Charge")
					/ (float) ((ItemJetpack) backpack.getItem()).getCooldown(backpack, player) * 20f);
			for (int i = 0; i < 20; i++) {
				if (i < progress)
					charge = charge + "|";
				else
					charge = charge + ".";
			}
			return new String[] { "CHARGES: " + backpack.getTag().getByte("Charges"), charge };
		}
		return new String[] { "CHARGES: 0", "" };
	}
}
