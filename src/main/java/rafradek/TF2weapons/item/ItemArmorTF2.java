package rafradek.TF2weapons.item;

import net.minecraft.world.entity.ai.attributes.Attribute;
import com.google.common.collect.HashMultimap;
import net.minecraft.network.chat.Component;
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

	public ItemArmorTF2(ArmorMaterial materialIn, String description, float kresistance) {
		super(materialIn, EquipmentSlot.FEET, new Properties().tab(rafradek.TF2weapons.TF2weapons.tabutilitytf2));
		this.description = description;
		this.knockbackReduction = kresistance;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced) {
		tooltip.add(Component.literal(description));
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create(super.getDefaultAttributeModifiers(equipmentSlot));

		if (equipmentSlot == this.slot && this.knockbackReduction != 0) {
			multimap.put(Attributes.KNOCKBACK_RESISTANCE,
					new AttributeModifier(knockbackUUID, "Knockback modifier", this.knockbackReduction, AttributeModifier.Operation.fromValue(0)));
		}

		return multimap;
	}
}
