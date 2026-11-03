package rafradek.TF2weapons.client.gui.inventory;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.inventory.ContainerDispenser;
import rafradek.TF2weapons.inventory.ContainerEnergy;

/** Picks the building screen (GUI id 5) from the building the menu was opened for. */
public class GuiBuildingFactory {

	@SuppressWarnings("unchecked")
	public static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> U create(M menu) {
		Screen screen = null;
		if (menu instanceof ContainerDispenser && ((ContainerEnergy) menu).building instanceof EntityDispenser)
			screen = new GuiDispenser((ContainerDispenser) menu);
		else if (menu instanceof ContainerEnergy) {
			ContainerEnergy energy = (ContainerEnergy) menu;
			if (energy.building instanceof EntitySentry)
				screen = new GuiSentry(energy);
			else if (energy.building instanceof EntityTeleporter)
				screen = new GuiTeleporter(energy);
		}
		return (U) screen;
	}
}
