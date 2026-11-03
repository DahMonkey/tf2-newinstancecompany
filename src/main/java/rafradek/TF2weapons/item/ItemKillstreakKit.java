package rafradek.TF2weapons.item;

import rafradek.TF2weapons.NBTLiterals;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.WeaponData;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemKillstreakKit extends ItemApplicableEffect {

	public static final String[] NAMES = { "standard", "specialized", "pro" };

	public ItemKillstreakKit() {
		this.setCreativeTab(TF2weapons.tabutilitytf2);
		this.setMaxStackSize(1);
	}

	@Override
	public void fillItemCategory(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (TF2Attribute attribute : TF2Attribute.attributes) {
			if (attribute != null && attribute.perKill != 0) {
				for (int level = 0; level < 3; level++) {
					par3List.add(new ItemStack(this, 1, attribute.id + (level << 9)));
				}
			}
		}
	}

	public TF2Attribute getAttribute(ItemStack stack) {
		return TF2Attribute.attributes[Mth.clamp(stack.getDamageValue() & 511, 0, TF2Attribute.attributes.length)];
	}

	public int getLevel(ItemStack stack) {
		return (stack.getDamageValue() >> 9) + 1;
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		return "item." + TF2weapons.MOD_ID + ".killstreakkit."
				+ ItemKillstreakKit.NAMES[Mth.clamp(this.getLevel(stack) - 1, 0, 2)];
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		TF2Attribute attrib = this.getAttribute(stack);

		if (attrib != null) {
			tooltip.add("For each kill gain up to:");
			tooltip.add(attrib.getTranslatedString(
					attrib.defaultValue + attrib.perKill * getBonusMult(this.getLevel(stack), attrib, null), true));
		}
	}

	public static float getBonusMult(int level, TF2Attribute attrib, WeaponData weapon) {
		float base = 1f;
		switch (level) {
		case 1:
			base = 1f;
			break;
		case 2:
			base = 1.25f;
			break;
		case 3:
			base = 1.5f;
			break;
		}
		if (weapon != null && attrib.effect.equals("Fire Rate") && weapon.getBoolean(PropertyType.RELOADS_CLIP))
			base *= 0.89f;
		return base;
	}

	public static float getLevelDrain(int level, TF2Attribute attrib) {
		float base = 0.7f;
		switch (level) {
		case 1:
			base = 0.83f;
			break;
		case 2:
			base = 0.88f;
			break;
		case 3:
			base = 0.93f;
			break;
		}
		return base;
	}

	public static int getCooldown(int level, int kills) {
		float drain = 1f;
		for (int i = 1; i < kills; i++) {
			drain *= getLevelDrain(level, null);
		}
		int cooldown;
		switch (level) {
		case 1:
			cooldown = 900;
			break;
		case 2:
			cooldown = 1050;
			break;
		case 3:
			cooldown = 1200;
			break;
		default:
			return 1200;
		}
		return (int) (cooldown * drain);
	}

	public static float getKillstreakBonus(TF2Attribute attrib, int level, int kills, WeaponData weapon) {
		float levelDrain = 1f;
		for (int i = 0; i < kills / 5; i++)
			levelDrain *= getLevelDrain(level, attrib);
		float value = attrib.defaultValue
				+ (5 * (1 - levelDrain) / (1 - getLevelDrain(level, attrib)) + (kills % 5) * levelDrain)
						* (Math.abs(attrib.perKill) * getBonusMult(level, attrib, weapon));
		if (attrib.perKill < 0) {
			value = 1 / value;
		}
		return value;
	}

	@Override
	public boolean isApplicable(ItemStack stack, ItemStack weapon) {
		return stack.hasTag() && stack.getTag().contains("Weapon") ? super.isApplicable(stack, weapon)
				: weapon.getItem() instanceof ItemWeapon;
	}

	@Override
	public void apply(ItemStack stack, ItemStack weapon) {
		weapon.getTag().putByte(NBTLiterals.STREAK_LEVEL, (byte) this.getLevel(stack));
		weapon.getTag().putShort(NBTLiterals.STREAK_ATTRIB, (short) (stack.getDamageValue() & 511));
	}
}
