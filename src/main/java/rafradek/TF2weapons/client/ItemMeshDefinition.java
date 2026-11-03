package rafradek.TF2weapons.client;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;

/** 1.12 ItemMeshDefinition: picks an item model for a stack. Used by {@link compat.ModelLoader}. */
@FunctionalInterface
public interface ItemMeshDefinition {
	ModelResourceLocation getModelLocation(ItemStack stack);
}
