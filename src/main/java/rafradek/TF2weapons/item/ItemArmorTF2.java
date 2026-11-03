package rafradek.TF2weapons.item;

import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemArmorTF2 extends ArmorItem {

	public String description;
	public UUID knockbackUUID = UUID.fromString("7941f9c1-13ac-4ae0-b54b-cbd8d5eec6df");
	public float knockbackReduction;

	public ItemArmorTF2(ArmorMaterial materialIn, int renderIndexIn, EquipmentSlot equipmentSlotIn,
			String description, float kresistance) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.description = description;
		this.knockbackReduction = kresistance;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		tooltip.add(description);
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

		if (equipmentSlot == this.armorType && this.knockbackReduction != 0) {
			multimap.put(Attributes.KNOCKBACK_RESISTANCE.getName(),
					new AttributeModifier(knockbackUUID, "Knockback modifier", this.knockbackReduction, 0));
		}

		return multimap;
	}
}
