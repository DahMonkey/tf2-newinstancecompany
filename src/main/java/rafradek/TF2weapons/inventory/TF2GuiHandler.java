package rafradek.TF2weapons.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import rafradek.TF2weapons.TF2EventsCommon;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.block.BlockCabinet;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.tileentity.TileEntityAmmoFurnace;
import rafradek.TF2weapons.tileentity.TileEntityUpgrades;

/**
 * Creates the mod's menus; replaces 1.12's IGuiHandler. GUI ids: 0 wearables, 1 workbench,
 * 2 upgrade station, 3 ammo furnace, 4 mercenary, 5 building, 6 robot deploy, 7 configurable block.
 * For entity GUIs (4, 5) the x coordinate carries the entity id, as in 1.12.
 */
public class TF2GuiHandler {

	public static AbstractContainerMenu createServerMenu(int id, int windowId, Player player, Level world, int x,
			int y, int z) {
		AbstractContainerMenu menu = LegacyContainer.withContext(TF2weapons.MENU_TYPES[id], windowId,
				() -> create(id, player, world, x, y, z, false));
		if (id == 0 && menu instanceof ContainerWearables && player instanceof ServerPlayer)
			menu.addSlotListener(new TF2EventsCommon.TF2ContainerListener((ServerPlayer) player));
		return menu;
	}

	public static AbstractContainerMenu createClientMenu(int id, int windowId, Inventory inv, FriendlyByteBuf buf) {
		int x = buf.readInt();
		int y = buf.readInt();
		int z = buf.readInt();
		Player player = inv.player;
		return LegacyContainer.withContext(TF2weapons.MENU_TYPES[id], windowId,
				() -> create(id, player, player.level, x, y, z, true));
	}

	private static AbstractContainerMenu create(int id, Player player, Level world, int x, int y, int z,
			boolean client) {
		BlockPos pos = new BlockPos(x, y, z);
		BlockEntity tile = id == 4 || id == 5 ? null : world.getBlockEntity(pos);
		switch (id) {
		case 0:
			return new ContainerWearables(player.getInventory(),
					player.getCapability(TF2weapons.INVENTORY_CAP).orElse(null), client, player);
		case 1:
			return new ContainerTF2Workbench(player, player.getInventory(), world, pos);
		case 2:
			if (tile instanceof TileEntityUpgrades) {
				TileEntityUpgrades upgrades = (TileEntityUpgrades) tile;
				if (!client && upgrades.attributes.size() == 0)
					upgrades.generateUpgrades(player.getRandom());
				return new ContainerUpgrades(player, player.getInventory(), upgrades, world, pos);
			}
			break;
		case 3:
			if (tile instanceof TileEntityAmmoFurnace)
				return new ContainerAmmoFurnace(player.getInventory(), (TileEntityAmmoFurnace) tile);
			break;
		case 4: {
			Entity entity = world.getEntity(x);
			if (entity instanceof EntityTF2Character)
				return new ContainerMercenary(player, (EntityTF2Character) entity, world);
			break;
		}
		case 5: {
			Entity entity = world.getEntity(x);
			if (entity instanceof EntityDispenser)
				return new ContainerDispenser((EntityDispenser) entity, player.getInventory());
			if (entity instanceof EntityBuilding)
				return new ContainerEnergy((EntityBuilding) entity, player.getInventory());
			break;
		}
		case 6:
			break;
		case 7:
			break;
		}
		// Something changed between opening and creating the menu: give an empty menu.
		return new ContainerEmpty();
	}

	/** Fallback menu with no slots. */
	public static class ContainerEmpty extends LegacyContainer {
		@Override
		public boolean canInteractWith(Player player) {
			return false;
		}
	}
}
