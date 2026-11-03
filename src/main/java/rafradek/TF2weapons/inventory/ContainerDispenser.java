package rafradek.TF2weapons.inventory;

import rafradek.TF2weapons.entity.building.EntityDispenser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerDispenser extends ContainerEnergy {

	public ContainerDispenser(EntityDispenser building, Inventory inv) {
		super(building, inv);
		for (int k = 0; k < 3; ++k)
			for (int i1 = 0; i1 < 3; ++i1)
				this.addSlotToContainer(new SlotItemHandler(building.items, i1 + k * 3, 80 + i1 * 18, 15 + k * 18));
	}

}
