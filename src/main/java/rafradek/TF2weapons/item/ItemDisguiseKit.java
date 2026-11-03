package rafradek.TF2weapons.item;

import rafradek.TF2weapons.client.compat.ScaledResolution;
import rafradek.TF2weapons.client.compat.LegacyGui;
import net.minecraft.network.chat.Component;
import rafradek.TF2weapons.client.compat.Tessellator;
import rafradek.TF2weapons.client.compat.BufferBuilder;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.gui.GuiDisguiseKit;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemDisguiseKit extends LegacyItem implements IItemSlotNumber, IItemOverlay {

	public ItemDisguiseKit() {
		this.setCreativeTab(TF2weapons.tabutilitytf2);
		this.setMaxStackSize(1);
		this.setMaxDamage(80);
	}

	public static void startDisguise(LivingEntity living, Level world, String type) {
		String type2 = type;
		if (!world.isClientSide && type.startsWith("T:") && (living instanceof Player)) {
			Player player = world.getClosestPlayer(living.getX(), living.getY(), living.getZ(), 512,
					playerl -> (!TF2Util.isAlliedTo(living, playerl)
							&& WeaponsCapability.get(playerl).getUsedToken() >= 0 && type.substring(2).equalsIgnoreCase(
									TF2Class.getClass(WeaponsCapability.get(playerl).getUsedToken()).getName())));
			if (player != null) {
				type2 = "P:" + player.getName();
			}
		}
		WeaponsCapability.get(living).setDisguiseType(type2);
		if (living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).disguiseTicks == 0)
			// System.out.println("starting disguise");
			if (!world.isClientSide)
				living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).disguiseTicks = 1;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player living, InteractionHand hand) {
		if (world.isClientSide && !TF2ConfigVars.limitedDisguise && ItemToken.allowUse(living, TF2Class.SPY)
				&& TF2Util.getFirstItem(living.getInventory(),
						stack -> TF2Attribute.getModifier("No Disguise Kit", stack, 0, living) != 0).isEmpty())
			ClientProxy.showGuiDisguise();
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, living.getItemInHand(hand));
	}

	public static boolean isDisguised(LivingEntity living, LivingEntity view) {
		if (!living.getCapability(TF2weapons.WEAPONS_CAP).isPresent() || !WeaponsCapability.get(living).isDisguised()
				|| (living.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks != 0
						&& !(view instanceof EntityBuilding)))
			return false;
		String disguisetype = WeaponsCapability.get(living).getDisguiseType();
		if (disguisetype.startsWith("M:") || disguisetype.startsWith("T:"))
			return true;
		if (disguisetype.startsWith("P:")) {
			return living.level.getScoreboard().getPlayersTeam(disguisetype.substring(2)) == view.getTeam();
		}
		return false;
	}

	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return repair.getItem() == TF2weapons.itemTF2 && repair.getDamageValue() == 2;
	}

	@Override
	public boolean showInfoBox(ItemStack stack, Player player) {
		return false;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, Player player) {
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawOverlay(ItemStack stack, Player player, Tessellator tessellator, BufferBuilder buffer,
			ScaledResolution resolution) {
		if (ItemToken.allowUse(player, TF2Class.SPY) && TF2ConfigVars.limitedDisguise
				&& TF2Util
						.getFirstItem(player.getInventory(),
								stackl -> TF2Attribute.getModifier("No Disguise Kit", stackl, 0, player) != 0)
						.isEmpty()) {
			GuiComponent gui = Minecraft.getInstance().gui;
			int width = resolution.getScaledWidth();
			int height = resolution.getScaledHeight();
			for (int i = 0; i < 9; i++) {
				TF2Class clazz = TF2Class.getClass(i);
				LegacyGui.drawCenteredString(Minecraft.getInstance().font, clazz.getLocalizedName().getString(), width / 2 - 225 + i * 50,
						height / 2 + 50, 0xFFFFFFFF);
				LegacyGui.drawCenteredString(Minecraft.getInstance().font, String.valueOf(i + 1), width / 2 - 225 + i * 50,
						height / 2 + 60, 0xFFFFFFFF);
				EntityTF2Character entity = clazz.createEntity(player.level);
				entity.setEntTeam(TF2Util.getTeamForDisplay(player) == 0 ? 1 : 0);
				entity.getCapability(TF2weapons.WEAPONS_CAP).orElse(null).invisTicks = 0;
				GuiDisguiseKit.drawEntityOnScreen(width / 2 - 225 + i * 50, height / 2 + 40, 35, entity);
			}
		}
	}

	@Override
	public boolean catchSlotHotkey(ItemStack stack, Player player) {
		return TF2ConfigVars.limitedDisguise;
	}

	@Override
	public void onSlotSelection(ItemStack stack, Player player, int slot) {
		if (ItemToken.allowUse(player, TF2Class.SPY)
				&& TF2Util
						.getFirstItem(player.getInventory(),
								stackl -> TF2Attribute.getModifier("No Disguise Kit", stackl, 0, player) != 0)
						.isEmpty()) {
			TF2weapons.network.sendToServer(new TF2Message.DisguiseMessage("T:" + TF2Class.getClass(slot)));
		}
	}
}
