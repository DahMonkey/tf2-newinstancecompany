package rafradek.TF2weapons.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base for the mod's items. 1.19 items take all their settings in the constructor; this class keeps
 * 1.12's setters (creative tab, stack size, durability, translation name, model properties) working
 * so the item classes keep their original constructors.
 */
public class LegacyItem extends Item {

	/** Model property overrides registered by items; the client registers them with ItemProperties. */
	public static final List<PropertyOverride> PROPERTY_OVERRIDES = new ArrayList<>();

	private CreativeModeTab legacyTab;
	private int legacyStackSize = 64;
	private int legacyMaxDamage;
	private boolean noRepair;
	private String unlocalizedName;

	public LegacyItem() {
		this(new Item.Properties());
	}

	public LegacyItem(Item.Properties properties) {
		super(properties);
	}

	public LegacyItem setCreativeTab(CreativeModeTab tab) {
		this.legacyTab = tab;
		return this;
	}

	public LegacyItem setMaxStackSize(int size) {
		this.legacyStackSize = size;
		return this;
	}

	public LegacyItem setHasSubtypes(boolean subtypes) {
		return this;
	}

	public LegacyItem setMaxDamage(int damage) {
		this.legacyMaxDamage = damage;
		if (damage > 0)
			this.legacyStackSize = 1;
		return this;
	}

	public LegacyItem setNoRepair() {
		this.noRepair = true;
		return this;
	}

	public LegacyItem setFull3D() {
		return this;
	}

	public LegacyItem setUnlocalizedName(String name) {
		this.unlocalizedName = name;
		return this;
	}

	public void addPropertyOverride(ResourceLocation name, PropertyGetter getter) {
		PROPERTY_OVERRIDES.add(new PropertyOverride(this, name, getter));
	}

	public boolean isInCreativeTab(CreativeModeTab tab) {
		return this.allowedIn(tab);
	}

	public CreativeModeTab getCreativeTab() {
		return this.legacyTab;
	}

	@Override
	protected boolean allowedIn(CreativeModeTab tab) {
		return this.legacyTab != null && (tab == CreativeModeTab.TAB_SEARCH || tab == this.legacyTab);
	}

	@Override
	public Collection<CreativeModeTab> getCreativeTabs() {
		return this.legacyTab == null ? Collections.emptyList() : Collections.singletonList(this.legacyTab);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return this.legacyStackSize;
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return this.legacyMaxDamage;
	}

	@Override
	public boolean isDamageable(ItemStack stack) {
		return this.legacyMaxDamage > 0;
	}

	@Override
	public boolean isRepairable(ItemStack stack) {
		return !this.noRepair && super.isRepairable(stack);
	}

	@Override
	public String getDescriptionId() {
		return this.unlocalizedName != null ? "item." + this.unlocalizedName : super.getDescriptionId();
	}

	/** Common-side version of the client ItemPropertyFunction. */
	@FunctionalInterface
	public interface PropertyGetter {
		float get(ItemStack stack, Level world, LivingEntity entity);
	}

	public record PropertyOverride(Item item, ResourceLocation name, PropertyGetter getter) {
	}
}
