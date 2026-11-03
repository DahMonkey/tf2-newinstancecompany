package rafradek.TF2weapons.item.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.TF2Util;

/**
 * Recipe input for the mod's crafting table: 1.12's Ingredient, accepting ore dictionary names,
 * items, blocks and (metadata or weapon) stacks.
 */
public class TF2Ingredient implements Predicate<ItemStack> {

	public static final TF2Ingredient EMPTY = new TF2Ingredient(stack -> stack.isEmpty(), new ItemStack[0]);

	private final Predicate<ItemStack> test;
	private ItemStack[] display;
	private final java.util.function.Supplier<ItemStack[]> displaySupplier;

	public TF2Ingredient(Predicate<ItemStack> test, ItemStack[] display) {
		this.test = test;
		this.display = display;
		this.displaySupplier = null;
	}

	private TF2Ingredient(Predicate<ItemStack> test, java.util.function.Supplier<ItemStack[]> display) {
		this.test = test;
		this.displaySupplier = display;
	}

	@Override
	public boolean test(ItemStack stack) {
		return stack != null && !stack.isEmpty() && this.test.test(stack);
	}

	public boolean apply(ItemStack stack) {
		return this.test(stack);
	}

	/** Example stacks, for the crafting table's recipe preview. */
	public ItemStack[] getMatchingStacks() {
		if (this.display == null)
			this.display = this.displaySupplier.get();
		return this.display;
	}

	public static TF2Ingredient fromStacks(ItemStack... stacks) {
		List<TF2Ingredient> list = new ArrayList<>();
		for (ItemStack stack : stacks)
			list.add(of(stack));
		return new TF2Ingredient(stack -> list.stream().anyMatch(ing -> ing.test(stack)),
				stacks.clone());
	}

	public static TF2Ingredient of(Object object) {
		if (object instanceof TF2Ingredient)
			return (TF2Ingredient) object;
		if (object instanceof Ingredient) {
			Ingredient ing = (Ingredient) object;
			return new TF2Ingredient(ing, ing::getItems);
		}
		if (object instanceof ItemLike) {
			Item item = ((ItemLike) object).asItem();
			return new TF2Ingredient(stack -> stack.getItem() == item, new ItemStack[] { new ItemStack(item) });
		}
		if (object instanceof ItemStack) {
			ItemStack target = ((ItemStack) object).copy();
			Predicate<ItemStack> test;
			if (target.getItem() instanceof ItemFromData && target.hasTag())
				test = stack -> stack.getItem() == target.getItem()
						&& ItemFromData.getData(stack) == ItemFromData.getData(target);
			else if (target.hasTag())
				test = stack -> stack.getItem() == target.getItem() && stack.getDamageValue() == target.getDamageValue()
						&& Objects.equals(stack.getTag(), target.getTag());
			else if (target.isDamageableItem())
				test = stack -> stack.getItem() == target.getItem();
			else
				test = stack -> stack.getItem() == target.getItem() && stack.getDamageValue() == target.getDamageValue();
			return new TF2Ingredient(test, new ItemStack[] { target });
		}
		if (object instanceof TagKey) {
			@SuppressWarnings("unchecked")
			TagKey<Item> tag = (TagKey<Item>) object;
			Ingredient ing = Ingredient.of(tag);
			return new TF2Ingredient(stack -> stack.is(tag), ing::getItems);
		}
		if (object instanceof String) {
			String ore = (String) object;
			return new TF2Ingredient(stack -> TF2Util.isOre(ore, stack), () -> oreExamples(ore));
		}
		throw new IllegalArgumentException("Invalid recipe ingredient: " + object);
	}

	private static ItemStack[] oreExamples(String ore) {
		List<ItemStack> list = new ArrayList<>();
		for (Item item : ForgeRegistries.ITEMS.getValues()) {
			ItemStack stack = new ItemStack(item);
			if (TF2Util.isOre(ore, stack))
				list.add(stack);
		}
		if (ore.equals("ingotCopper") || ore.equals("ingotLead") || ore.equals("ingotAustralium")
				|| ore.equals("nuggetAustralium")) {
			int meta = ore.equals("ingotCopper") ? 0 : ore.equals("ingotLead") ? 1 : ore.equals("ingotAustralium") ? 2 : 9;
			list.add(0, TF2Util.metaStack(rafradek.TF2weapons.TF2weapons.itemTF2, 1, meta));
		}
		if (list.isEmpty())
			list.add(new ItemStack(Items.BARRIER));
		return list.toArray(new ItemStack[0]);
	}
}
